package com.kryeit.stuff;

import com.kryeit.stuff.config.ConfigReader;
import com.kryeit.stuff.config.StaticConfig;
import com.kryeit.votifier.utils.JSONObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jdbi.v3.core.Jdbi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Analytics {
    private static final Executor playerSessionExecutor = Executors.newSingleThreadExecutor();
    private static final Timer playerTrackerTimer = new Timer();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Map<UUID, Session> sessions = new HashMap<>();
    private static Jdbi jdbi = null;

    static {
        if (StaticConfig.production) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:clickhouse://kryeit.com:8123/kryeit");
            config.setUsername("default");
            config.setPassword(ConfigReader.CLICKHOUSE_PASSWORD);
            jdbi = Jdbi.create(new HikariDataSource(config));

            playerTrackerTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    StringBuilder inserts = new StringBuilder();
                    List<ServerPlayerEntity> playerList = MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList();

                    new ArrayList<>(playerList).forEach(player -> {
                        String insert = "('%s', %s, %s, %s), ".formatted(player.getUuidAsString(), player.getBlockX(), player.getBlockY(), player.getBlockZ());
                        inserts.append(insert);
                    });
                    if (inserts.isEmpty()) return;

                    jdbi.useHandle(h -> h.createScript(
                                    "INSERT INTO kryeit.player_movement (player, x, y, z) FORMAT Values %s"
                                            .formatted(inserts))
                            .execute());
                }
            }, 0, 60_000);
        }
    }

    public static void storeSessionStart(UUID player, String ipAddress) {
        if (!StaticConfig.production) return;

        URI uri = URI.create("https://www.ipqualityscore.com/api/json/ip/%s/%s?strictness=0&allow_public_access_points=true&lighter_penalties=true"
                .formatted(ConfigReader.IPGS_KEY, ipAddress));

        HttpRequest request = HttpRequest.newBuilder(uri).build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(s -> s.replace("\\/", "/"))
                .thenAccept(s -> {
                    JSONObject object = new JSONObject(s);
                    boolean vpn = object.getBoolean("vpn");
                    String country = object.getString("country_code");
                    String region = object.getString("region");

                    sessions.put(player, new Session(country, region, vpn, System.currentTimeMillis()));
                });
    }

    public static void storeSessionEnd(UUID uuid) {
        if (!StaticConfig.production) return;

        Session session = sessions.remove(uuid);

        if (session == null) return;
        playerSessionExecutor.execute(() -> {
            jdbi.useHandle(handle -> handle.createUpdate("""
                            INSERT INTO kryeit.sessions (player, start_time, end_time, country, region, vpn)
                            VALUES (:player, :start_time, :end_time, :country, :region, :vpn)
                            """)
                    .bind("player", uuid)
                    .bind("country", session.country)
                    .bind("region", session.region)
                    .bind("vpn", session.vpn)
                    .bind("start_time", session.loginTime / 1000)
                    .bind("end_time", System.currentTimeMillis() / 1000)
                    .execute());
        });
    }

    private record Session(String country, String region, boolean vpn, long loginTime) {
    }
}

package com.kryeit.stuff;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GerenteClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GerenteClient.class);
    private static final Gson gson = new Gson();
    private final String internalApiKey;
    private final String baseUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<UUID, PlayerJoinInfo> playerInfos = new ConcurrentHashMap<>();
    private final ScheduledExecutorService statusUpdater = Executors.newScheduledThreadPool(1);
    private final Supplier<Float> tpsSupplier;
    private ScheduledFuture<?> nextUpdate;

    public GerenteClient(String internalApiKey, String baseUrl, Supplier<Float> tpsSupplier) {
        this.internalApiKey = internalApiKey;
        this.baseUrl = baseUrl;
        this.tpsSupplier = tpsSupplier;
    }

    public String generateDiscordConnectionCode(long userID) {
        JsonObject body = new JsonObject();
        body.addProperty("platform", "DISCORD");
        body.addProperty("userID", userID + "");

        HttpRequest request = requestBuilder("/api/internal/connection-code")
                .method("GET", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        JsonObject response = sendRequest(request, JsonObject.class);
        return response.get("code").getAsString();
    }

    public void updatePlayerStats(UUID playerUUID, JsonObject stats) {
        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/stats")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(stats.toString()))
                .build();

        sendRequest(request);
    }

    public PlayerJoinInfo handlePlayerJoin(UUID playerUUID, String playerName) {
        String encodedName = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/joined?name=" + encodedName)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        PlayerJoinInfo playerJoinInfo = sendRequest(request, PlayerJoinInfo.class);
        playerInfos.put(playerUUID, playerJoinInfo);
        return playerJoinInfo;
    }

    public void updateServerStatus(boolean running, String statusMessage, List<StatusPlayer> connectedPlayers) {
        if (nextUpdate != null) nextUpdate.cancel(false);
        nextUpdate = statusUpdater.schedule(() -> updateServerStatus(running, statusMessage, connectedPlayers), 20, TimeUnit.SECONDS);

        JsonObject body = new JsonObject();
        body.addProperty("running", running);
        body.addProperty("statusMessage", statusMessage);
        body.addProperty("tps", tpsSupplier.get());
        body.add("connectedPlayers", gson.toJsonTree(connectedPlayers));

        HttpRequest request = requestBuilder("/api/internal/server/status")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        sendRequest(request);
    }

    public byte[] getSkin(UUID playerUUID, boolean headOnly) {
        HttpRequest request = requestBuilder("/api/players/" + playerUUID + "/" + (headOnly ? "head-skin" : "skin")).build();

        return sendRequest(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    public ServerStatus getStatus() {
        HttpRequest request = requestBuilder("/api/server/status").build();

        return sendRequest(request, ServerStatus.class);
    }

    // TODO live status
    // TODO login

    public void updatePreferences(UUID playerUUID, Map<String, Object> preferences) {
        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/preferences")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(preferences)))
                .build();

        sendRequest(request);
    }

    public int generateDiscordOTP(long discordAccountID) {
        JsonObject body = new JsonObject();
        body.addProperty("platform", "DISCORD");
        body.addProperty("userID", String.valueOf(discordAccountID));

        return generateOTP(body);
    }

    public int generateMinecraftOTP(UUID minecraftAccountID, String minecraftName) {
        JsonObject body = new JsonObject();
        body.addProperty("platform", "MINECRAFT");
        body.addProperty("userID", minecraftAccountID.toString());
        body.addProperty("userName", minecraftName);

        return generateOTP(body);
    }

    public LastSeenResponse getLastSeen(UUID playerUUID) {
        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/last-seen").build();
        return sendRequest(request, LastSeenResponse.class);
    }

    private int generateOTP(JsonObject body) {
        HttpRequest request = requestBuilder("/api/internal/connection-code")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        JsonObject response = sendRequest(request, JsonObject.class);
        return response.get("code").getAsInt();
    }

    public List<PlayerSearchResult> searchPlayers(String query, boolean exact) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpRequest request = requestBuilder("/api/players?query=" + encodedQuery + "&exact=" + exact).build();

        return sendRequest(request, new TypeToken<>() {
        });
    }

    public void banPlayer(UUID playerUUID, String banReason, long bannedUntil) {
        JsonObject body = new JsonObject();
        body.addProperty("reason", banReason);
        body.addProperty("bannedUntil", bannedUntil);

        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/ban")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        sendRequest(request);
    }

    public void unbanPlayer(UUID playerUUID) {
        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/unban")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        sendRequest(request);
    }

    public PlayerInfo getPlayerInfo(UUID playerUUID, Collection<StatKey> stats) {
        String statsQueryParam = stats.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "?stats=" + URLEncoder.encode(statsQueryParam, StandardCharsets.UTF_8)).build();
        return sendRequest(request, PlayerInfo.class);
    }

    public int getRank(UUID playerUUID, StatKey statistic) {
        HttpRequest request = requestBuilder("/api/players/" + playerUUID + "/rank?key=" + statistic.key() + "&namespace=" + statistic.namespace()).build();
        return sendRequest(request, JsonObject.class).get("rank").getAsInt();
    }

    public List<LeaderboardEntry> getLeaderboard(int limit, int offset, boolean sortAscending, StatKey statistic) {
        HttpRequest request = requestBuilder("/api/leaderboard?limit=" + limit + "&offset=" + offset + "&ascending=" + sortAscending + "&key=" + statistic.key() + "&namespace=" + statistic.namespace()).build();

        return sendRequest(request, new TypeToken<>() {
        });
    }

    public boolean connectDiscord(UUID minecraftUUID, int code) {
        HttpRequest request = requestBuilder("/api/internal/players/" + minecraftUUID + "/connect?otp=" + code)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            sendRequest(request, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (InvalidResponseCodeException ignored) {
            return false;
        }
    }

    public Optional<PlayerJoinInfo> getCachedJoinInfo(UUID playerUUID) {
        return Optional.ofNullable(playerInfos.get(playerUUID));
    }

    private HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path));
    }

    private <T> T sendRequest(HttpRequest request, Class<T> clazz) {
        String body = sendRequest(request, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(body, clazz);
    }

    private <T> T sendRequest(HttpRequest request, TypeToken<T> typeToken) {
        String body = sendRequest(request, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(body, typeToken);
    }

    private void sendRequest(HttpRequest request) {
        try {
            sendRequest(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            LOGGER.error("Failed to send request {}", request, e);
        }
    }

    private <T> T sendRequest(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest authorizedRequest = HttpRequest.newBuilder(request, (h, v) -> true)
                .header("Authorization", internalApiKey)
                .build();

        try {
            HttpResponse<T> response = httpClient.send(authorizedRequest, bodyHandler);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new InvalidResponseCodeException("Gerente HTTP request failed: HTTP error code: " + response.statusCode() + ", body: " + response.body());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public record StatusPlayer(boolean afk, UUID uuid, String name) {
    }

    public record PlayerJoinInfo(boolean firstJoin, Timestamp bannedUntil, String banReason, List<String> badges,
                                 boolean muted, JsonObject preferences, List<Role> roles) {
    }

    public record LastSeenResponse(boolean connected, long lastSeen) {
    }

    public record PlayerSearchResult(String name, UUID uuid) {
    }

    public record ServerStatus(boolean running, String statusMessage, float tps, List<StatusPlayer> connectedPlayers) {
    }

    public record PlayerInfo(String minecraftName, String discordId, Long lastSeen, Long bannedUntil, String banReason,
                             String mutedUntil, JsonObject preferences, Map<String, Integer> selectedStats,
                             List<Role> roles) {
    }

    public record StatKey(String namespace, String key) {
        @Override
        public @NotNull String toString() {
            return namespace + "/" + key;
        }
    }

    public record LeaderboardEntry(String name, UUID uuid, int value, String formattedValue) {
    }

    private static class InvalidResponseCodeException extends RuntimeException {
        public InvalidResponseCodeException(String message) {
            super(message);
        }
    }

    public record Role(String name, String id, Long discordId, String prefix) {
    }
}

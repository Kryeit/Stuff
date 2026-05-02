package com.kryeit.stuff;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.kryeit.idler.afk.AfkPlayer;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utils {
    public static MutableComponent prefix(ServerPlayer player) {
        Component cog = Component.literal("⛭").withStyle(style -> style.withBold(true)).withStyle(ChatFormatting.GOLD); // "group.kryeitor"
        Component camera = Component.literal("📷").withStyle(ChatFormatting.GREEN); // group.photographer
        Component anchor = Component.literal("⚓").withStyle(ChatFormatting.RED); // group.postbuilder
        Component diamond = Component.literal("♢").withStyle(ChatFormatting.LIGHT_PURPLE); // group.booster
        Component pig1 = Component.literal("\uD83D\uDC3D").withStyle(ChatFormatting.LIGHT_PURPLE); // group.potato-war.winner
        Component pig2 = Component.literal("\uD83D\uDC3D").withStyle(ChatFormatting.GRAY); // group.potato-war.2nd
        Component pig3 = Component.literal("\uD83D\uDC3D").withStyle(s -> s.withColor(0xa95b0e)); // group.potato-war.3rd

        return Stuff.GERENTE.getCachedJoinInfo(player.getUUID())
                .map(info -> {
                    MutableComponent text = Component.literal("");
                    boolean isEmpty = true;
                    for (GerenteClient.Role role : info.roles()) {
                        if (role.prefix() != null) {
                            text = text.append(Component.literal(role.prefix()));
                            isEmpty = false;
                        }
                    }
                    return isEmpty ? Component.empty() : text.append(" ");
                })
                .orElse(Component.empty());
    }

    public static String getMapLink(ServerPlayer player) {
        return getMapLink(player.blockPosition());
    }

    public static float getTPS() {
        try {
            Spark spark = SparkProvider.get();
            return (float) spark.tps().poll(StatisticWindow.TicksPerSecond.MINUTES_1);
        } catch (IllegalStateException ignored) {
            return 0;
        }

    }

    public static String getMapLink(BlockPos position) {
        // example link https://map.kryeit.com/#overworld:-3664:0:8222:58252:-0.39:0:0:0:perspective
        int x = position.getX();
        int z = position.getZ();

        return "https://map.kryeit.com/#overworld:" + x + ":0:" + z + ":0:0:0:0:0:perspective";
    }

    public static List<ServerPlayer> getAfkPlayers() {
        List<ServerPlayer> afkPlayers = new ArrayList<>();
        MinecraftServerSupplier.getServer().getPlayerList().getPlayers().forEach(player -> {
            AfkPlayer afkPlayer = (AfkPlayer) player;
            if (afkPlayer != null && afkPlayer.idler$isAfk() && !Stuff.checkPermission(player.getUUID(), "stuff.afk")) {
                afkPlayers.add(player);
            }
        });
        return afkPlayers;
    }

    public static JsonObject getStatsJson(ServerPlayer player) {
        JsonObject stats = getMinecraftStats(player);

        User user = GriefDefender.getCore().getUser(player.getUUID());
        int claimBlocks = user == null ? 0 : user.getPlayerData().getInitialClaimBlocks() + user.getPlayerData().getAccruedClaimBlocks() + user.getPlayerData().getBonusClaimBlocks();

        JsonObject custom = new JsonObject();
        custom.addProperty("kryeit:claim_blocks", claimBlocks);

        stats.add("kryeit:custom", custom);
        return stats;
    }

    private static JsonObject getMinecraftStats(@Nullable ServerPlayer player) {
        System.out.println("getStatsJson called for player: " + (player != null ? player.getName().getString() : "null"));

        if (player == null) {
            System.out.println("Player is null, returning empty JSON");
            return new JsonObject();
        }

        try {
            UUID playerUuid = player.getUUID();
            System.out.println("Player UUID: " + playerUuid);

            Path statsPath = Paths.get("world/stats/" + playerUuid + ".json");
            System.out.println("Stats file path: " + statsPath.toAbsolutePath());

            if (Files.exists(statsPath)) {
                System.out.println("Stats file exists, reading content");
                String content = Files.readString(statsPath);
                System.out.println("Stats content length: " + content.length() + " bytes");
                return JsonParser.parseString(content).getAsJsonObject().getAsJsonObject("stats");
            } else {
                System.out.println("Stats file does not exist at path: " + statsPath.toAbsolutePath());
            }

            System.out.println("Returning empty JSON due to missing stats file");
            return new JsonObject();
        } catch (Exception e) {
            System.out.println("Exception while reading player stats for: " + player.getName().getString());
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            return new JsonObject();
        }
    }

    public static String readSecret(String envName) {
        String secret = System.getenv(envName);
        if (secret != null) return secret;

        String path = System.getenv(envName + "_FILE");
        try (InputStream stream = Files.newInputStream(Path.of(path))) {
            return new String(stream.readAllBytes()).strip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

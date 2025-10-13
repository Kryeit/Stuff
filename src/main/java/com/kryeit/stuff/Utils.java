package com.kryeit.stuff;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.kryeit.idler.afk.AfkPlayer;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Utils {
    public static MutableText prefix(ServerPlayerEntity player) {
        MutableText cog = Text.literal("⛭").setStyle(Style.EMPTY.withBold(true)).formatted(Formatting.GOLD); // "group.kryeitor"
        MutableText camera = Text.literal("📷").formatted(Formatting.GREEN); // group.photographer
        MutableText anchor = Text.literal("⚓").formatted(Formatting.RED); // group.postbuilder
        MutableText diamond = Text.literal("♢").formatted(Formatting.LIGHT_PURPLE); // group.booster
        MutableText pig1 = Text.literal("\uD83D\uDC3D").formatted(Formatting.LIGHT_PURPLE); // group.potato-war.winner
        MutableText pig2 = Text.literal("\uD83D\uDC3D").formatted(Formatting.GRAY); // group.potato-war.2nd
        MutableText pig3 = Text.literal("\uD83D\uDC3D").styled(s -> s.withColor(0xa95b0e)); // group.potato-war.3rd

        return Stuff.GERENTE.getCachedJoinInfo(player.getUuid())
                .map(info -> {
                    MutableText text = Text.literal("");
                    for (GerenteClient.Role role : info.roles()) {
                        text.append(Text.literal(role.prefix()));
                    }
                    return text.append(" ");
                })
                .orElse(Text.empty());
    }

    public static String getMapLink(ServerPlayerEntity player) {
        return getMapLink(player.getBlockPos());
    }

    public static float getTPS() {
        try {
            Spark spark = SparkProvider.get();
            return (float) spark.tps().poll(StatisticWindow.TicksPerSecond.MINUTES_1);
        } catch (IllegalStateException ignored) {
            return 0;
        }

    }

    public static String getMapLink(Vec3i position) {
        // example link https://map.kryeit.com/#overworld:-3664:0:8222:58252:-0.39:0:0:0:perspective
        int x = position.getX();
        int z = position.getZ();

        return "https://map.kryeit.com/#overworld:" + x + ":0:" + z + ":0:0:0:0:0:perspective";
    }

    public static List<ServerPlayerEntity> getAfkPlayers() {
        List<ServerPlayerEntity> afkPlayers = new ArrayList<>();
        MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList().forEach(player -> {
            AfkPlayer afkPlayer = (AfkPlayer) player;
            if (afkPlayer != null && afkPlayer.idler$isAfk() && !Stuff.checkPermission(player.getUuid(), "stuff.afk")) {
                afkPlayers.add(player);
            }
        });
        return afkPlayers;
    }

    public static List<ServerPlayerEntity> getAfkPlayersSorted() {
        List<ServerPlayerEntity> players = getAfkPlayers();
        players.sort(Comparator.comparingLong(ServerPlayerEntity::getLastActionTime));
        return players;
    }

    public static ItemStack getItemStack(String namespace, String path) {
        return Registries.ITEM.getOrEmpty(Identifier.of(namespace, path)).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    public static JsonObject getStatsJson(ServerPlayerEntity player) {
        JsonObject stats = getMinecraftStats(player);

        User user = GriefDefender.getCore().getUser(player.getUuid());
        int claimBlocks = user == null ? 0 : user.getPlayerData().getInitialClaimBlocks() + user.getPlayerData().getAccruedClaimBlocks() + user.getPlayerData().getBonusClaimBlocks();

        JsonObject custom = new JsonObject();
        custom.addProperty("kryeit:claim_blocks", claimBlocks);

        stats.add("kryeit:custom", custom);
        return stats;
    }

    private static JsonObject getMinecraftStats(@Nullable ServerPlayerEntity player) {
        System.out.println("getStatsJson called for player: " + (player != null ? player.getName().getString() : "null"));

        if (player == null) {
            System.out.println("Player is null, returning empty JSON");
            return new JsonObject();
        }

        try {
            UUID playerUuid = player.getUuid();
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
}

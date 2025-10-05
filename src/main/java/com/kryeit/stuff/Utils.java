package com.kryeit.stuff;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kryeit.idler.afk.AfkPlayer;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Utils {
    public static Component prefix(ServerPlayer player) {
        Component cog = Component.literal("⛭").withStyle(style -> style.withBold(true)).withStyle(ChatFormatting.GOLD);
        Component camera = Component.literal("📷").withStyle(ChatFormatting.GREEN);
        Component anchor = Component.literal("⚓").withStyle(ChatFormatting.RED);
        Component diamond = Component.literal("♢").withStyle(ChatFormatting.LIGHT_PURPLE);
        Component pig1 = Component.literal("\uD83D\uDC3D").withStyle(ChatFormatting.LIGHT_PURPLE);
        Component pig2 = Component.literal("\uD83D\uDC3D").withStyle(ChatFormatting.GRAY);
        Component pig3 = Component.literal("\uD83D\uDC3D").withStyle(s -> s.withColor(0xa95b0e));

        return Stuff.GERENTE.getCachedJoinInfo(player.getUUID())
                .map(info -> {
                    MutableComponent text = Component.literal("");
                    for (GerenteClient.Role role : info.roles()) {
                        text = text.append(Component.literal(role.prefix()));
                    }
                    return text.append(" ");
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

    public static List<ServerPlayer> getAfkPlayersSorted() {
        List<ServerPlayer> players = getAfkPlayers();
        players.sort(Comparator.comparingLong(ServerPlayer::getLastActionTime));
        return players;
    }

    public static ItemStack getItemStack(String namespace, String path) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    public static JsonObject getStatsJson(@Nullable ServerPlayer player) {
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
}

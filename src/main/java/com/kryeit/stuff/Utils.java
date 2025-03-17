package com.kryeit.stuff;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kryeit.idler.afk.AfkPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
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
    public static boolean isServerFull() {
        return MinecraftServerSupplier.getServer().getCurrentPlayerCount() >= MinecraftServerSupplier.getServer().getMaxPlayerCount();
    }

    // Run a command as the server, arguments is a String, the command
    public static void runCommand(String arguments) {
        MinecraftServerSupplier.getServer().getCommandManager().executeWithPrefix(MinecraftServerSupplier.getServer().getCommandSource(), arguments);
    }

    public static MutableText prefix(ServerPlayerEntity player) {
        MutableText cog = Text.literal("⛭").setStyle(Style.EMPTY.withBold(true)).formatted(Formatting.GOLD);
        MutableText camera = Text.literal("📷").formatted(Formatting.GREEN);
        MutableText anchor = Text.literal("⚓").formatted(Formatting.RED);
        MutableText diamond = Text.literal("♢").formatted(Formatting.LIGHT_PURPLE);
        MutableText pig1 = Text.literal("\uD83D\uDC3D").formatted(Formatting.LIGHT_PURPLE);
        MutableText pig2 = Text.literal("\uD83D\uDC3D").formatted(Formatting.GRAY);
        MutableText pig3 = Text.literal("\uD83D\uDC3D").styled(s -> s.withColor(0xa95b0e));

        MutableText text = Text.literal("");

        if (Permissions.check(player, "group.kryeitor", false)) {
            text.append(cog);
        }

        if (Permissions.check(player, "group.photographer", false)) {
            text.append(camera);
        }

        if (Permissions.check(player, "group.booster", false)) {
            text.append(diamond);
        }

        if (Permissions.check(player, "group.postbuilder", false)) {
            text.append(anchor);
        }

        if (Permissions.check(player, "group.potato-war.winner", false)) text.append(pig1);
        if (Permissions.check(player, "group.potato-war.2nd", false)) text.append(pig2);
        if (Permissions.check(player, "group.potato-war.3rd", false)) text.append(pig3);

        return text.append(" ");
    }

    public static String getMapLink(ServerPlayerEntity player) {
        // example link https://map.kryeit.com/#overworld:-3664:0:8222:58252:-0.39:0:0:0:perspective
        int x = (int) player.getPos().getX();
        int z = (int) player.getPos().getZ();

        return "https://map.kryeit.com/#overworld:" + x + ":0:" + z + ":0:0:0:0:0:perspective";
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
            if (afkPlayer != null && afkPlayer.idler$isAfk() && !Permissions.check(player, "stuff.afk")) {
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

    public static JsonObject getStatsJson(@Nullable ServerPlayerEntity delayedPlayer) {
        System.out.println("getStatsJson called for player: " + (delayedPlayer != null ? delayedPlayer.getName().getString() : "null"));

        if (delayedPlayer == null) {
            System.out.println("Player is null, returning empty JSON");
            return new JsonObject();
        }

        try {
            UUID playerUuid = delayedPlayer.getUuid();
            System.out.println("Player UUID: " + playerUuid);

            Path statsPath = Paths.get("world/stats/" + playerUuid + ".json");
            System.out.println("Stats file path: " + statsPath.toAbsolutePath());

            if (Files.exists(statsPath)) {
                System.out.println("Stats file exists, reading content");
                String content = Files.readString(statsPath);
                System.out.println("Stats content length: " + content.length() + " bytes");
                return JsonParser.parseString(content).getAsJsonObject();
            } else {
                System.out.println("Stats file does not exist at path: " + statsPath.toAbsolutePath());
            }

            System.out.println("Returning empty JSON due to missing stats file");
            return new JsonObject();
        } catch (Exception e) {
            System.out.println("Exception while reading player stats for: " +
                    delayedPlayer.getName().getString());
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            return new JsonObject();
        }
    }
}

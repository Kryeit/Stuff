package com.kryeit.stuff;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Utils {

    public static boolean isServerFull() {
        return MinecraftServerSupplier.getServer().getMaxPlayerCount() <= MinecraftServerSupplier.getServer().getCurrentPlayerCount();
    }

    // Run a command as the server, arguments is a String, the command
    public static void runCommand(String arguments) {
        MinecraftServerSupplier.getServer().getCommandManager().executeWithPrefix(MinecraftServerSupplier.getServer().getCommandSource(), arguments);
    }

    public static MutableText prefix(ServerPlayerEntity player) {
        MutableText cog = Text.literal("⛭").setStyle(Style.EMPTY.withBold(true)).formatted(Formatting.GOLD);
        MutableText anchor = Text.literal("⚓").formatted(Formatting.RED);
        MutableText diamond = Text.literal("♢").formatted(Formatting.LIGHT_PURPLE);

        MutableText text = Text.literal("");

        if (Permissions.check(player, "group.kryeitor", false)) {
            text.append(cog);
        }

        if (Permissions.check(player, "group.booster", false)) {
            text.append(diamond);
        }

        if (Permissions.check(player, "group.postbuilder", false)) {
            text.append(anchor);
        }

        return text.append(" ");
    }

    public static String getMapLink(ServerPlayerEntity player) {
        // example link https://map.kryeit.com/#overworld:-3664:0:8222:58252:-0.39:0:0:0:perspective
        int x = (int) player.getPos().getX();
        int z = (int) player.getPos().getZ();

        return  "https://map.kryeit.com/#overworld:" + x + ":0:" + z + ":0:0:0:0:0:perspective";
    }
}

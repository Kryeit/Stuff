package com.kryeit.stuff;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.text.Normalizer;

public class Utils {

    public static boolean isServerFull() {
        return MinecraftServerSupplier.getServer().getMaxPlayerCount() <= MinecraftServerSupplier.getServer().getCurrentPlayerCount();
    }

    public static MutableText prefix(ServerPlayerEntity player) {
        MutableText cog = Text.literal("⛭").setStyle(Style.EMPTY.withBold(true)).setStyle(Style.EMPTY.withFormatting(Formatting.GOLD));
        MutableText anchor = Text.literal("⚓").setStyle(Style.EMPTY.withBold(true)).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
        
        MutableText text = Text.literal("");

        if (Permissions.check(player, "group.kryeitor")) {
            text.append(cog);
        }

        if (Permissions.check(player, "group.postbuilder")) {
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

    public static Formatting getFormattingForTab(ServerPlayerEntity player) {
        if (Permissions.check(player, "group.staff")) {
            return Formatting.GREEN;
        }

        if (Permissions.check(player, "group.kryeitor")) {
            return Formatting.GOLD;
        }

        return Formatting.WHITE;
    }
}

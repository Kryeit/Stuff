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

    public static MutableText prefix(ServerPlayerEntity player) {
        MutableText balanza = Text.literal("⚖").setStyle(Style.EMPTY.withBold(true)).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN));
        MutableText cog = Text.literal("⛭").setStyle(Style.EMPTY.withBold(true)).setStyle(Style.EMPTY.withFormatting(Formatting.GOLD));
        MutableText anchor = Text.literal("⚓").setStyle(Style.EMPTY.withBold(true)).setStyle(Style.EMPTY.withFormatting(Formatting.RED));


        MutableText text = Text.literal("");

        if (Permissions.check(player, "group.staff")) {
            text.append(balanza);
        }

        if (Permissions.check(player, "group.kryeitor")) {
            text.append(cog);
        }

        if (Permissions.check(player, "group.postbuilder")) {
            text.append(anchor);
        }

        return text.append(" ");
    }
}

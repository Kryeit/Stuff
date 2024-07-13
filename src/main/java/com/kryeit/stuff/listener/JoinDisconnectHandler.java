package com.kryeit.stuff.listener;

import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class JoinDisconnectHandler {
    public static void onJoin(ServerPlayNetworkHandler networkHandler, MinecraftServer server) {
        int current = server.getCurrentPlayerCount() + 1;
        int max = server.getMaxPlayerCount();

        PlayerListHeaderS2CPacket packet = getPacket(current, max);
        server.getPlayerManager().sendToAll(packet);
        networkHandler.sendPacket(packet);
    }

    public static void onDisconnect(ServerPlayNetworkHandler networkHandler, MinecraftServer server) {
        int current = server.getCurrentPlayerCount() - 1;
        int max = server.getMaxPlayerCount();

        server.getPlayerManager().sendToAll(getPacket(current, max));
    }

    private static PlayerListHeaderS2CPacket getPacket(int current, int max) {
        MutableText text = Text.literal("\n      ").formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(current)).formatted(Formatting.WHITE))
                .append(Text.literal(" / ").formatted(Formatting.GRAY))
                .append(Text.literal(String.valueOf(max)).formatted(Formatting.WHITE))
                .append(Text.literal(" players online      ").formatted(Formatting.GRAY));

        return new PlayerListHeaderS2CPacket(Text.empty(), text);
    }
}

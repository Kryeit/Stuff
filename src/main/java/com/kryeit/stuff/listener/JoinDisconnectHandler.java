package com.kryeit.stuff.listener;

import com.kryeit.idler.afk.AfkPlayer;
import com.kryeit.stuff.GerenteClient;
import com.kryeit.stuff.Stuff;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class JoinDisconnectHandler {
    public static void onJoin(MinecraftServer server) {
        int current = server.getPlayerCount();
        int max = server.getMaxPlayers();

        server.getPlayerList().broadcastSystemMessage(getPacket(current, max).footer(), false);
        updateServerStatus(server);
    }

    public static void onDisconnect(MinecraftServer server) {
        onJoin(server);
    }

    private static void updateServerStatus(MinecraftServer server) {
        List<GerenteClient.StatusPlayer> players = server.getPlayerList().getPlayers().stream()
                .map(p -> new GerenteClient.StatusPlayer(((AfkPlayer) p).idler$isAfk(), p.getUUID(), p.getName().getString()))
                .toList();
        Stuff.runActionAsync(() -> Stuff.GERENTE.updateServerStatus(true, "Online", players));
    }

    private static ClientboundTabListPacket getPacket(int current, int max) {
        MutableComponent text = Component.literal("\n      ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(current)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(max)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" players online      ").withStyle(ChatFormatting.GRAY));

        return new ClientboundTabListPacket(Component.empty(), text);
    }
}
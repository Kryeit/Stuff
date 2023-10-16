package com.kryeit.stuff.listener;

import com.kryeit.stuff.MinecraftServerSupplier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.UUID;

public class ServerLogin implements ServerPlayConnectionEvents.Init {

    @Override
    public void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.player;

        File playerDataDirectory = new File("world/playerdata/");

        File[] playerDataFiles = playerDataDirectory.listFiles();

        if (playerDataFiles == null) return;

        for (File playerDataFile : playerDataFiles) {
            String fileName = playerDataFile.getName();
            if (!fileName.endsWith(".dat")) continue;
            UUID id = UUID.fromString(fileName.substring(0, fileName.length() - 4));
            if (player.getUuid().equals(id)) {
                // Has joined before
                return;
            }
        }

        // Has NOT joined before
        MinecraftServerSupplier.getServer().getPlayerManager().broadcast(
                Text.literal("Welcome " + player.getName() + " to Kryeit!").setStyle(Style.EMPTY.withColor(Formatting.AQUA)),
                false
        );

        player.sendMessage(Text.literal("Kryeit is a long standing Create mod server, it has its pecularities.\n" +
                "Teleportation: there is a grid of posts all over the world, use /post to see the closest.\n" +
                "Claiming: there is a claim system, use /claim to protect your stuff.\n" +
                "Missions: there is weekly missions to earn coins, use H to see them.").setStyle(Style.EMPTY.withColor(Formatting.AQUA)));
    }
}

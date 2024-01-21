package com.kryeit.stuff.listener;

import com.kryeit.stuff.Utils;
import com.kryeit.votifier.MinecraftServerSupplier;
import com.kryeit.votifier.model.Vote;
import com.kryeit.votifier.model.VotifierEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayerVote implements VotifierEvent {
    @Override
    public void onVoteReceived(Vote vote) {
        String name = vote.getUsername();
        for (ServerPlayerEntity player : MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList()) {
            if (!player.getName().getString().equals(name)) {
                player.sendMessage(
                        Text.literal("Someone voted! +60 CB").formatted(Formatting.GRAY),
                        true);
                Utils.runCommand("/adjustclaimblocks " + player.getName().getString() + " 60");
            }
        }

        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);
        if (player == null) return;
        player.sendMessage(
                Text.literal("Thanks for voting! +200 CB").formatted(Formatting.GRAY),
                false);
    }
}

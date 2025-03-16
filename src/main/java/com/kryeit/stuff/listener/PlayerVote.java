package com.kryeit.stuff.listener;

import com.kryeit.stuff.Utils;
import com.kryeit.stuff.compat.GriefDefenderImpl;
import com.kryeit.votifier.MinecraftServerSupplier;
import com.kryeit.votifier.model.Vote;
import com.kryeit.votifier.model.VotifierEvent;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Random;

public class PlayerVote implements VotifierEvent {

    private final Random random = new Random();

    Map<Double, ItemStack> voteRewards = Map.of(
            0.05, Utils.getItemStack("polymerstuff", "kryeit_cog"),
            0.08, Utils.getItemStack("createdeco", "netherite_nugget"),
            0.1, Utils.getItemStack("minecraft", "saddle"),
            0.77, ItemStack.EMPTY
    );

    @Override
    public void onVoteReceived(Vote vote) {
        String name = vote.getUsername();
        for (ServerPlayerEntity player : MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList()) {
            if (!player.getName().getString().equals(name)) {
                int cb = 40;
                if (Permissions.check(player, "group.collaborator", false)) {
                    cb = 50;
                }
                player.sendMessage(
                        Text.literal("Someone voted! +" + cb + " CB").formatted(Formatting.GRAY),
                        true);
                GriefDefenderImpl.giveClaimBlocks(player.getUuid(), cb);
            }
        }

        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);
        if (player == null) return;

        ItemStack reward = rollReward();
        if (!reward.isEmpty()) {
            player.getInventory().insertStack(reward);
            String itemName = reward.getName().getString();
            player.sendMessage(
                    Text.literal("Thanks for voting! +200 CB and a " + itemName).formatted(Formatting.GRAY),
                    false);
        } else {
            player.sendMessage(
                    Text.literal("Thanks for voting! +200 CB").formatted(Formatting.GRAY),
                    false);
        }

        GriefDefenderImpl.giveClaimBlocks(player.getUuid(), 200);
    }

    private ItemStack rollReward() {
        double roll = random.nextDouble();
        double cumulativeProbability = 0.0;

        for (Map.Entry<Double, ItemStack> entry : voteRewards.entrySet()) {
            cumulativeProbability += entry.getKey();
            if (roll < cumulativeProbability) {
                return entry.getValue().copy();
            }
        }

        return ItemStack.EMPTY;
    }
}
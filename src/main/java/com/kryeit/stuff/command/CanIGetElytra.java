package com.kryeit.stuff.command;

import com.kryeit.stuff.Stuff;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

public class CanIGetElytra {

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        long lastKill = Stuff.dragonKillers.getLastKillTime(player.getUUID());

        if (lastKill == 0) {
            player.sendSystemMessage(Component.literal("You haven't killed the ender dragon yet! Go kill it to get an elytra"));
        } else {
            long currentTimePlayed = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
            long ticksSinceLastKill = currentTimePlayed - lastKill;
            long hoursSinceLastKill = ticksSinceLastKill / (20 * 60 * 60); // Convert ticks to hours
            player.sendSystemMessage(Component.literal("You killed the ender dragon " + hoursSinceLastKill + " hours ago"));
        }

        if (Stuff.dragonKillers.canKillAnotherDragon(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You can kill the ender dragon again to get another elytra"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("canigetelytra")
                .executes(CanIGetElytra::execute)
        );
    }

}

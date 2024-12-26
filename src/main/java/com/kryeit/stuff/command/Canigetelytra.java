package com.kryeit.stuff.command;

import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.storage.DragonKillers;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class Canigetelytra {

    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        long lastKill = Stuff.dragonKillers.getLastKillTime(player.getUuid());

        if (lastKill == 0) {
            player.sendMessage(Text.of("You haven't killed the ender dragon yet! Go kill it to get an elytra"));
        } else {
            long currentTimePlayed = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
            long ticksSinceLastKill = currentTimePlayed - lastKill;
            long hoursSinceLastKill = ticksSinceLastKill / (20 * 60 * 60); // Convert ticks to hours
            player.sendMessage(Text.of("You killed the ender dragon " + hoursSinceLastKill + " hours ago"));
        }

        if (Stuff.dragonKillers.canKillAnotherDragon(player.getUuid())) {
            player.sendMessage(Text.of("You can kill the ender dragon again to get another elytra"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("canigetelytra")
                .executes(Canigetelytra::execute)
        );
    }

}

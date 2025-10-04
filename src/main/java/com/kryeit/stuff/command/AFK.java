package com.kryeit.stuff.command;

import com.kryeit.idler.afk.AfkPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class AFK {

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        AfkPlayer afkPlayer = (AfkPlayer) player;
        afkPlayer.idler$enableAfk();

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("afk")
                .executes(AFK::execute)
        );
    }

}

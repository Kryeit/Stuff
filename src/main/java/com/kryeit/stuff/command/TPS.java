package com.kryeit.stuff.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TPS {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tps")
                .executes(context -> {
                    String command = "spark tps";
                    context.getSource().getServer().getCommandManager().executeWithPrefix(
                            context.getSource().withSilent(), command
                    );
                    return 1;
                })
        );
    }
}


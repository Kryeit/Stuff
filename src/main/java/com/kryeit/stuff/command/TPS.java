package com.kryeit.stuff.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class TPS {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tps")
                .executes(context -> {
                    context.getSource().getServer().getCommands().performPrefixedCommand(
                            context.getSource(), "spark tps"
                    );
                    return 1;
                })
        );
    }
}


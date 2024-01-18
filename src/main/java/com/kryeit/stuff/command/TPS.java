package com.kryeit.stuff.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TPS {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Get the command node for /spark
        CommandNode<ServerCommandSource> sparkNode = dispatcher.getRoot().getChild("spark");

        if (sparkNode != null) {
            // Get the subcommand node for /tps
            CommandNode<ServerCommandSource> tpsNode = sparkNode.getChild("tps");

            if (tpsNode != null) {
                // Register the /tps command as an alias to /spark tps
                dispatcher.register(CommandManager.literal("tps").redirect(tpsNode));
            }
        }
    }
}

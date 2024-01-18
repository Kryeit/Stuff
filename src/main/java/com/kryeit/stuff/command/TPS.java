package com.kryeit.stuff.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TPS {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Register the /tps command as an alias to /spark tps
        dispatcher.register(CommandManager.literal("tps")
                .redirect(dispatcher.getRoot().getChild("spark tps")));
    }
}

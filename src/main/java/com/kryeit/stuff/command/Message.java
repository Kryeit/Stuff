package com.kryeit.stuff.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class Message {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Register the /m command as an alias to /msg
        dispatcher.register(CommandManager.literal("m")
                .redirect(dispatcher.getRoot().getChild("msg")));
    }
}

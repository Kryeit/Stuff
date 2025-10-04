package com.kryeit.stuff.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class Landmarks {
    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        player.sendMessage(Text.literal("Landmarks -> https://landmarks.kryeit.com")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://landmarks.kryeit.com"))));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("landmarks")
                .executes(Landmarks::execute)
        );

        dispatcher.register(CommandManager.literal("forum")
                .executes(Landmarks::execute)
        );
    }
}

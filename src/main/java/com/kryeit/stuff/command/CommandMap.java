package com.kryeit.stuff.command;

import com.kryeit.stuff.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CommandMap {
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        source.sendSystemMessage(Component.literal("Bluemap -> https://map.kryeit.com/")
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Utils.getMapLink(player)))));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("map")
                .executes(CommandMap::execute)
        );
    }
}

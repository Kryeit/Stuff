package com.kryeit.stuff.command;

import com.kryeit.stuff.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

public class SendCoords {
    public static int execute(CommandContext<CommandSourceStack> context, ServerPlayer receiver) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        source.sendSystemMessage(Component.literal("Sent coordinates to " + receiver.getName().getString()));

        receiver.sendSystemMessage(Component.literal(player.getName().getString() + " has sent you their coordinates: (" +
                        (int) player.getX() + ", " + (int) player.getY() + ", " + (int) player.getZ() + ")")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Utils.getMapLink(player)))));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sendcoords")
                .then(Commands.argument("player", EntityArgument.player()))
                .executes(context -> execute(context, context.getArgument("player", ServerPlayer.class)))
        ); // TODO test
    }
}

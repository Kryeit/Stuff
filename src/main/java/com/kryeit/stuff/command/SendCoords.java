package com.kryeit.stuff.command;

import com.kryeit.stuff.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class SendCoords {
    public static int execute(CommandContext<ServerCommandSource> context, ServerPlayerEntity receiver) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        source.sendFeedback(() -> Text.literal("Sent coordinates to " + receiver.getName().getString()), false);

        receiver.sendMessage(Text.literal(player.getName().getString() + " has sent you their coordinates: (" +
                        (int) player.getX() + ", " + (int) player.getY() + ", " + (int) player.getZ() + ")")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Utils.getMapLink(player)))));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sendcoords")
                .then(CommandManager.argument("player", EntityArgumentType.player()))
                .executes(context -> execute(context, context.getArgument("player", ServerPlayerEntity.class)))
        ); // TODO test
    }
}

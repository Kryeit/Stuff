package com.kryeit.stuff.command;

import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.Utils;
import com.kryeit.stuff.command.completion.PlayerAutocompletion;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Supplier;

public class SendCoords {
    public static int execute(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        ServerPlayerEntity receiver = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);

        if (receiver == null) {
            Supplier<Text> message = () -> Text.of("Player not found");
            source.sendFeedback(message, false);
            return 0;
        }

        receiver.sendMessage(Text.literal(player.getName().getString() + " has sent you their coordinates: (" +
                (int) player.getX() + ", " + (int) player.getY() + ", " + (int) player.getZ() + ")")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Utils.getMapLink(player)))));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sendcoords")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(PlayerAutocompletion.suggestOnlinePlayers())
                                .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                        )
        );
    }
}

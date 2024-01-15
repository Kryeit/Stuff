package com.kryeit.stuff.command;

import com.kryeit.stuff.MinecraftServerSupplier;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.UUID;

public class Reply {
    private static final HashMap<UUID, UUID> lastMessageSender = new HashMap<>();

    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        UUID playerUUID = player.getUuid();
        if (!lastMessageSender.containsKey(playerUUID)) {
            player.sendMessage(Text.literal("No one to reply to."), false);
            return 1;
        }

        UUID targetUUID = lastMessageSender.get(playerUUID);
        ServerPlayerEntity targetPlayer = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(targetUUID);
        if (targetPlayer == null) {
            player.sendMessage(Text.literal("Player is not online."), false);
            return 1;
        }
        String message = StringArgumentType.getString(context, "message");
        Text formattedMessage = Text.literal(player.getName().getString() + " whispers: " + message).formatted(Formatting.LIGHT_PURPLE);
        targetPlayer.sendMessage(formattedMessage, false);

        Text confirmationMessage = Text.literal("You whispered to " + targetPlayer.getName().getString() + ": " + message).formatted(Formatting.GRAY)
                .formatted(Formatting.LIGHT_PURPLE);
        player.sendMessage(confirmationMessage, false);
        lastMessageSender.put(targetUUID, playerUUID); // Update for reciprocal /reply
        return 1;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reply")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(Reply::execute)
                )
        );

        dispatcher.register(CommandManager.literal("r")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(Reply::execute)
                )
        );
    }

    public static void onMessageReceived(UUID senderUUID, UUID receiverUUID) {
        lastMessageSender.put(receiverUUID, senderUUID);
    }
}
package com.kryeit.stuff.command;

import com.kryeit.stuff.auth.UserApi;
import com.kryeit.stuff.command.completion.PlayerAutocompletion;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.sql.Timestamp;
import java.util.function.Supplier;

public class LastSeen {

    public static int execute(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        Timestamp timestamp = UserApi.getLastSeenByName(name);

        if (timestamp == null) {
            Supplier<Text> message = () -> Text.of("Player not found");
            source.sendFeedback(message, false);
            return 0;
        }

        long time = timestamp.getTime();
        long currentTime = System.currentTimeMillis();
        long difference = currentTime - time;

        long days = difference / 86400000;
        long hours = (difference % 86400000) / 3600000;
        long minutes = ((difference % 86400000) % 3600000) / 60000;

        String message = name + " was last seen ";

        if (days > 0) message += days + "d ";
        if (hours > 0) message += hours + "h ";
        if (minutes > 0) message += minutes + "m ";
        if (days == 0 && hours == 0 && minutes == 0) message += "1m ";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss");
        String formattedDate = sdf.format(timestamp);

        message += "ago, on the " + formattedDate;

        String finalMessage = message;
        Supplier<Text> feedback = () -> Text.of(finalMessage);
        source.sendFeedback(feedback, false);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lastseen")
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(PlayerAutocompletion.suggestOnlinePlayers())
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                )
        );
    }

}

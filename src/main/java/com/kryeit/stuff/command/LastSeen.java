package com.kryeit.stuff.command;

import com.kryeit.stuff.GerenteClient;
import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.command.completion.PlayerAutocompletion;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.sql.Timestamp;

public class LastSeen {

    // TODO last seen
    public static int execute(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        Stuff.runActionAsync(() -> {
            if (source.getServer().getPlayerList().getPlayerByName(name) != null) {
                return new GerenteClient.LastSeenResponse(true, 0);
            }

            return Stuff.GERENTE.searchPlayers(name, true)
                    .stream()
                    .map(GerenteClient.PlayerSearchResult::uuid)
                    .findAny()
                    .map(Stuff.GERENTE::getLastSeen)
                    .orElse(new GerenteClient.LastSeenResponse(false, 0));

        }, lastSeenResponse -> {
            if (lastSeenResponse.connected()) {
                source.sendSystemMessage(Component.literal(name + " is currently online"));
                return;
            }

            if (lastSeenResponse.lastSeen() == 0) {
                source.sendSystemMessage(Component.literal(name + " not found"));
                return;
            }

            long currentTime = System.currentTimeMillis();
            long difference = currentTime - lastSeenResponse.lastSeen();

            long days = difference / 86400000;
            long hours = (difference % 86400000) / 3600000;
            long minutes = ((difference % 86400000) % 3600000) / 60000;

            String message = name + " was last seen ";

            if (days > 0) message += days + "d ";
            if (hours > 0) message += hours + "h ";
            if (minutes > 0) message += minutes + "m ";
            if (days == 0 && hours == 0 && minutes == 0) message += "1m ";

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss");
            String formattedDate = sdf.format(new Timestamp(lastSeenResponse.lastSeen()));

            message += "ago, on the " + formattedDate + " UTC";

            source.sendSystemMessage(Component.literal(message));
        });

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lastseen")
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests(PlayerAutocompletion.suggestOnlinePlayers())
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                )
        );
    }

}

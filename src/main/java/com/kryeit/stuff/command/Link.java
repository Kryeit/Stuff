package com.kryeit.stuff.command;

import com.kryeit.stuff.Stuff;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class Link {
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        int code = context.getArgument("otp", Integer.class);

        Stuff.runActionAsync(
                () -> Stuff.GERENTE.connectDiscord(player.getUUID(), code),
                codeValid -> {
                    if (codeValid) {
                        source.sendSystemMessage(Component.literal("Your Discord account has been successfully connected"));
                    } else {
                        source.sendSystemMessage(Component.literal("This code is either invalid or has expired. You need to generate it in a Discord chat using /otp"));
                    }
                });

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("link")
                .then(Commands.argument("otp", IntegerArgumentType.integer())
                        .executes(Link::execute))
        );
    }
}

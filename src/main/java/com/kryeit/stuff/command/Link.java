package com.kryeit.stuff.command;

import com.kryeit.stuff.Stuff;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Link {
    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        int code = context.getArgument("otp", Integer.class);

        Stuff.runActionAsync(
                () -> Stuff.GERENTE.connectDiscord(player.getUuid(), code),
                codeValid -> {
                    if (codeValid.success()) {
                        source.sendFeedback(() -> Text.literal("Your Discord account has been successfully connected"), false);
                    } else {
                        source.sendFeedback(() -> Text.literal("This code is either invalid or has expired. You need to generate it in a Discord chat using /otp"), false);
                    }
                });

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("link")
                .then(CommandManager.argument("otp", IntegerArgumentType.integer())
                        .executes(Link::execute))
        );
    }
}

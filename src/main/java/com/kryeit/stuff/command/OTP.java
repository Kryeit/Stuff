package com.kryeit.stuff.command;

import com.kryeit.stuff.Stuff;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class OTP {
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        Stuff.runActionAsync(
                () -> Stuff.GERENTE.generateMinecraftOTP(player.getUUID(), player.getName().getString()),
                code -> source.sendSystemMessage(Component.literal("Your code is " + formatCode(code)))
        );

        return Command.SINGLE_SUCCESS;
    }

    private static String formatCode(int code) {
        String stringCode = String.valueOf(code);
        return stringCode.substring(0, 3) + " " + stringCode.substring(3, 6);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("otp")
                .executes(OTP::execute)
        );
    }
}

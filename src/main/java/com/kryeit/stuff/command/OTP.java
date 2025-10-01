package com.kryeit.stuff.command;

import com.kryeit.stuff.Stuff;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class OTP {
    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        Stuff.runActionAsync(
                () -> Stuff.GERENTE.generateMinecraftOTP(player.getUuid(), player.getEntityName()),
                code -> source.sendMessage(Text.literal("Your code is " + formatCode(code)))
        );

        return Command.SINGLE_SUCCESS;
    }

    private static String formatCode(int code) {
        String stringCode = String.valueOf(code);
        return stringCode.substring(0, 3) + " " + stringCode.substring(3, 6);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("otp")
                .executes(OTP::execute)
        );
    }
}

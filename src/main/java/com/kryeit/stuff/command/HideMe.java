package com.kryeit.stuff.command;

import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.compat.BluemapImpl;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class HideMe {
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        Stuff.runActionAsync(() -> {
                    Stuff.GERENTE.updatePreferences(player.getUUID(), Map.of("show_on_map", false));
                    BluemapImpl.changePlayerVisibility(player.getUUID(), false);
                    return null;
                },
                v -> player.sendSystemMessage(Component.literal("Now you won't be shown in the BlueMap"))
        );

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hideme")
                .executes(HideMe::execute)
        );
    }
}

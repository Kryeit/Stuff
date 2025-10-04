package com.kryeit.stuff.command;

import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.compat.BluemapImpl;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class ShowMe {
    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        Stuff.runActionAsync(() -> {
                    Stuff.GERENTE.updatePreferences(player.getUuid(), Map.of("show_on_map", true));
                    BluemapImpl.changePlayerVisibility(player.getUuid(), true);
                    return null;
                },
                v -> source.sendFeedback(() -> Text.literal("Now you are shown in the BlueMap"), false)
        );
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showme")
                .executes(ShowMe::execute)
        );
    }
}

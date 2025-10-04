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

public class HideMe {
    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        Stuff.runActionAsync(() -> {
                    Stuff.GERENTE.updatePreferences(player.getUuid(), Map.of("show_on_map", false));
                    BluemapImpl.changePlayerVisibility(player.getUuid(), false);
                    return null;
                },
                v -> player.sendMessage(Text.literal("Now you won't be shown in the BlueMap"))
        );

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hideme")
                .executes(HideMe::execute)
        );
    }
}

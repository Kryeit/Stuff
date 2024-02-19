package com.kryeit.stuff.command;

import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.bluemap.BluemapImpl;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.function.Supplier;

public class ShowMe {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        BluemapImpl.changePlayerVisibility(player.getUuid(), true);
        try {
            Stuff.hiddenPlayers.deletePlayer(player.getUuid());
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendMessage(Text.literal("Now you won't be shown in the BlueMap"));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showme")
                .executes(ShowMe::execute)
        );
    }
}

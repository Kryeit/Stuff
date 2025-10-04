package com.kryeit.stuff.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class Kofi {
    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        player.sendMessage(Text.literal("""
                        Kofi -> https://ko-fi.com/kryeit
                        
                        Collaborator advantages:\
                        - Color name both on Discord and in Minecraft
                        - Claiming advantages:
                            · 300CB/hour instead of 200
                            · 5.000.000 max CB instead of 2.000.000
                            · Claims expire after 365  days of inactiveness instead of 120
                         - Won't get kicked by the AFK system when the server is full""")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ko-fi.com/kryeit"))));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("kofi")
                .executes(Kofi::execute)
        );
        dispatcher.register(CommandManager.literal("donate")
                .executes(Kofi::execute)
        );
    }
}

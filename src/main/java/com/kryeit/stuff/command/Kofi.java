package com.kryeit.stuff.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class Kofi {
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        player.sendSystemMessage(Component.literal("""
                Kofi -> https://ko-fi.com/kryeit
                
                Collaborator advantages:
                - Color name both on Discord and in Minecraft
                - Claiming advantages:
                    · 300CB/hour instead of 200
                    · 5.000.000 max CB instead of 2.000.000
                    · Claims expire after 365 days of inactiveness instead of 120
                 - Won't get kicked by the AFK system when the server is full""")
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ko-fi.com/kryeit"))));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kofi")
                .executes(Kofi::execute)
        );
        dispatcher.register(Commands.literal("donate")
                .executes(Kofi::execute)
        );
    }
}

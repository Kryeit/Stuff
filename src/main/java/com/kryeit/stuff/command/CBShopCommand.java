package com.kryeit.stuff.command;

import com.kryeit.stuff.shop.ClaimBlockShopMenu;
import com.kryeit.stuff.shop.ShopContainer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public class CBShopCommand {
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        ShopContainer container = new ShopContainer(27);
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, player1) -> new ClaimBlockShopMenu(id, inventory, container),
                Component.literal("Claim Block Shop")
        ));

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cbshop")
                .executes(CBShopCommand::execute)
        );
    }
}

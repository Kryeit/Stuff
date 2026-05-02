package com.kryeit.stuff.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class NetherCoords {

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        ResourceKey<Level> dimension = player.level().dimension();
        if (dimension.equals(Level.OVERWORLD)) {
            x /= 8;
            z /= 8;
            player.sendSystemMessage(Component.literal("Build at these Nether coordinates: X=" + x + ", Y=" + y + ", Z=" + z));
        } else if (dimension.equals(Level.NETHER)) {
            x *= 8;
            z *= 8;
            player.sendSystemMessage(Component.literal("Build at these Overworld coordinates: X=" + x + ", Y=" + y + ", Z=" + z));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nethercoords")
                .executes(NetherCoords::execute)
        );
    }

}

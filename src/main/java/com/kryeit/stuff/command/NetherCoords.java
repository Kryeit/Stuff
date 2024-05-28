package com.kryeit.stuff.command;

import com.kryeit.stuff.afk.AfkPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class NetherCoords {

    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        RegistryKey<World> dimension = player.getWorld().getRegistryKey();
        if (dimension.equals(World.OVERWORLD)) {
            x /= 8;
            z /= 8;
            player.sendMessage(Text.of("Build at these Nether coordinates: X=" + x + ", Y=" + y + ", Z=" + z), false);
        } else if (dimension.equals(World.NETHER)) {
            x *= 8;
            z *= 8;
            player.sendMessage(Text.of("Build at these Overworld coordinates: X=" + x + ", Y=" + y + ", Z=" + z), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nethercoords")
                .executes(NetherCoords::execute)
        );
    }

}

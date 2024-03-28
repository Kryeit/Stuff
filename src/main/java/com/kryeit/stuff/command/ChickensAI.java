package com.kryeit.stuff.command;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.kryeit.stuff.afk.AfkPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class ChickensAI {

    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        World world = player.getEntityWorld();

        Claim claim = GriefDefender.getCore().getClaimAt(GriefDefender.getCore().getWorldUniqueId(world)
                , (int) player.getX(), (int) player.getY(), (int) player.getZ());

        if (claim == null || !claim.canBreak(player, player.getBlockPos(), null) || claim.isWilderness()) {
            Supplier<Text> message = () -> Text.of("You can't use this command here");
            source.sendFeedback(message, false);
            return 0;
        }

        // Bounding box of the claim
        Box box = new Box(claim.getLesserBoundaryCorner().getX(), claim.getLesserBoundaryCorner().getY(), claim.getLesserBoundaryCorner().getZ(),
                claim.getGreaterBoundaryCorner().getX(), claim.getGreaterBoundaryCorner().getY(), claim.getGreaterBoundaryCorner().getZ());

        world.getEntitiesByType(EntityType.CHICKEN, box, chicken -> {
            chicken.setAiDisabled(true);
            return false;
        });

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("chickensai")
                .executes(ChickensAI::execute)
        );
    }

}

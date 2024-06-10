package com.kryeit.stuff.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerAccessor {

    @Invoker("isBedObstructed")
    boolean invokeIsBedObstructed(BlockPos pos, Direction direction);
}

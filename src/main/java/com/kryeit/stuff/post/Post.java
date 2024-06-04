package com.kryeit.stuff.post;

import com.kryeit.stuff.MinecraftServerSupplier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public class Post {

    private static final int GAP = 5000;
    private static final int WIDTH = 31;
    private final int x;
    private final int z;

    public Post(Vec3d pos) {
        this.x = (int) Math.round(pos.getX() / GAP) * GAP;
        this.z = (int) Math.round(pos.getZ() / GAP) * GAP;
    }
    
    public boolean isInside(Vec3d pos) {
        int halfWidth = (WIDTH - 1)/ 2;

        boolean insideX = pos.getX() >= (x - halfWidth) && pos.getX() <= (x + halfWidth);
        boolean insideZ = pos.getZ() >= (z - halfWidth) && pos.getZ() <= (z + halfWidth);

        return insideX && insideZ;
    }
}

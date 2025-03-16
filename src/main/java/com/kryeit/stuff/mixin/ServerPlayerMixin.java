package com.kryeit.stuff.mixin;

import com.kryeit.idler.afk.AfkPlayer;
import com.kryeit.stuff.Utils;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayerEntity.class, priority = 999)
public abstract class ServerPlayerMixin {

    @Inject(method = "getPlayerListName", at = @At("RETURN"), cancellable = true)
    private void replacePlayerListName(CallbackInfoReturnable<Text> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        AfkPlayer afkPlayer = (AfkPlayer) player;
        MutableText name = player.getName().copy().formatted(Formatting.WHITE);

        if (afkPlayer.idler$isAfk()) {
            name = name.formatted(Formatting.GRAY);
        }

        cir.setReturnValue(Utils.prefix(player).append(name));
    }

    // Solves End -> Overworld teleportation issue
    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    private void onMoveToWorld(ServerWorld destination, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        RegistryKey<World> fromWorldKey = player.getWorld().getRegistryKey();
        RegistryKey<World> toWorldKey = destination.getRegistryKey();
        if (fromWorldKey.equals(World.END) && toWorldKey.equals(World.OVERWORLD)) {
            player.teleport(destination,
                    destination.getSpawnPos().getX(),
                    destination.getSpawnPos().getY(),
                    destination.getSpawnPos().getZ(),
                    player.getYaw(),
                    player.getPitch());

            cir.setReturnValue(player);
        }
    }
}

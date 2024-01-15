package com.kryeit.stuff.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayerDeath implements ServerLivingEntityEvents.AfterDeath {

    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.literal("You've died on: (" +
                    (int) player.getPos().getX() + ", " +
                    (int) player.getPos().getY() + ", " +
                    (int) player.getPos().getZ() + ")").formatted(Formatting.GRAY));
        }
    }
}

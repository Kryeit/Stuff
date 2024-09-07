package com.kryeit.stuff.listener;

import com.kryeit.stuff.storage.DragonKillers;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.kryeit.stuff.Stuff.dragonKillers;

public class DragonDeath implements ServerLivingEntityEvents.AfterDeath {

    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity.getType().equals(EntityType.ENDER_DRAGON)) {
            Entity source = damageSource.getSource();
            if (source == null) return;

            ServerPlayerEntity player = null;
            if (source.getType().equals(EntityType.PLAYER)) {
                player = (ServerPlayerEntity) source;
            } else if (source instanceof ProjectileEntity projectile) {
                if (projectile.getOwner() instanceof ServerPlayerEntity) {
                    player = (ServerPlayerEntity) projectile.getOwner();
                }
            }

            if (player != null && !DragonKillers.canKillAnotherDragon(player.getUuid())) {
                player.getInventory().offerOrDrop(Items.ELYTRA.getDefaultStack());
                player.sendMessage(Text.of("You've killed the ender dragon for the first time! Here's an elytra :)"));
                dragonKillers.addKiller(player.getUuid());
            }
        }
    }
}
// DragonDeath.java
package com.kryeit.stuff.listener;

import com.kryeit.stuff.Stuff;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import static com.kryeit.stuff.Stuff.dragonKillers;

@EventBusSubscriber(modid = Stuff.MODID)
public class DragonDeath {

    @SubscribeEvent
    public static void onDragonDeath(LivingDeathEvent event) {
        if (event.getEntity().getType().equals(EntityType.ENDER_DRAGON)) {
            Entity source = event.getSource().getEntity();
            if (source == null) return;

            ServerPlayer player = null;
            if (source.getType().equals(EntityType.PLAYER)) {
                player = (ServerPlayer) source;
            } else if (source instanceof Projectile projectile) {
                if (projectile.getOwner() instanceof ServerPlayer) {
                    player = (ServerPlayer) projectile.getOwner();
                }
            }

            if (player != null && dragonKillers.canKillAnotherDragon(player.getUUID())) {
                player.getInventory().add(Items.ELYTRA.getDefaultInstance());
                player.sendSystemMessage(Component.literal("You've killed the ender dragon! Here's an elytra :)"));
                player.sendSystemMessage(Component.literal("Kill it again after 100 hours of playtime to get another elytra"));
                dragonKillers.addKiller(player.getUUID());
            }
        }
    }
}
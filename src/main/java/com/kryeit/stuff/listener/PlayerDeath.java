// PlayerDeath.java
package com.kryeit.stuff.listener;

import com.kryeit.stuff.Stuff;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Stuff.MODID)
public class PlayerDeath {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.literal("You've died on: (" +
                    (int) player.position().x + ", " +
                    (int) player.position().y + ", " +
                    (int) player.position().z + ")").withStyle(ChatFormatting.GRAY));
        }
    }
}
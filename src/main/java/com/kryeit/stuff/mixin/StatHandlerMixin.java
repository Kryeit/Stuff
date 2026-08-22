package com.kryeit.stuff.mixin;

import com.kryeit.idler.afk.AfkPlayer;
import com.kryeit.stuff.Stuff;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatsCounter.class)
public class StatHandlerMixin {
    @Inject(method = "increment", at = @At("HEAD"))
    private void changeAmount(CallbackInfo ci, @Local(argsOnly = true) Player player, @Local(argsOnly = true) Stat<?> statRef, @Local(argsOnly = true) LocalIntRef valueRef) {
        Integer multiplier = Stuff.statisticModifiers.get(statRef.getName());
        if (multiplier != null) {
            valueRef.set(valueRef.get() * multiplier);
        }

        AfkPlayer afkPlayer = (AfkPlayer) player;
        if (afkPlayer.idler$isAfk() && statRef.getName().equals("minecraft.custom:minecraft.play_time")) {
//            player.awardStat(ModStats.AFK_TIME, valueRef.get());
            valueRef.set(0);
        }
    }
}

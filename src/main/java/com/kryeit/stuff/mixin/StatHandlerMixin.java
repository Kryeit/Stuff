package com.kryeit.stuff.mixin;

import com.kryeit.idler.afk.AfkPlayer;
import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.storage.ModStats;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatHandler.class)
public class StatHandlerMixin {
    @Inject(method = "increaseStat", at = @At("HEAD"))
    private void changeAmount(CallbackInfo ci, @Local(argsOnly = true) PlayerEntity player, @Local(argsOnly = true) Stat<?> statRef, @Local(argsOnly = true) LocalIntRef valueRef) {
        Integer multiplier = Stuff.statisticModifiers.get(statRef.getName());
        if (multiplier != null) {
            valueRef.set(valueRef.get() * multiplier);
        }

        AfkPlayer afkPlayer = (AfkPlayer) player;
        if (afkPlayer.idler$isAfk() && statRef.getName().equals("minecraft.custom:minecraft.play_time")) {
            player.increaseStat(ModStats.AFK_TIME, valueRef.get());
            valueRef.set(0);
        }
    }
}

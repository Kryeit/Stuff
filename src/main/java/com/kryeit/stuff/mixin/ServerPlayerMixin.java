package com.kryeit.stuff.mixin;

import com.kryeit.idler.afk.AfkPlayer;
import com.kryeit.stuff.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayer.class, priority = 999)
public abstract class ServerPlayerMixin {

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void replacePlayerListName(CallbackInfoReturnable<Component> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        AfkPlayer afkPlayer = (AfkPlayer) player;
        MutableComponent name = player.getName().copy().withStyle(ChatFormatting.WHITE);

        if (afkPlayer.idler$isAfk()) {
            name = name.withStyle(ChatFormatting.GRAY);
        }

        cir.setReturnValue(Utils.prefix(player).append(name));
    }
}
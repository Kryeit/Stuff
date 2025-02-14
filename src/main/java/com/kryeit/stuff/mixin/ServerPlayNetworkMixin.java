package com.kryeit.stuff.mixin;

import com.kryeit.stuff.afk.AfkPlayer;
import com.kryeit.stuff.afk.Config;
import com.kryeit.stuff.auth.UserApi;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// This class has been mostly made by afkdisplay mod

// https://github.com/beabfc/afkdisplay
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateAfkStatus(CallbackInfo ci) {
        AfkPlayer afkPlayer = (AfkPlayer) player;
        int timeoutSeconds = Config.PacketOptions.timeoutSeconds;
        if (afkPlayer.stuff$isAfk() || timeoutSeconds <= 0) return;
        if (UserApi.getLastSeen(player.getUuid()) == null) return;
        long afkDuration = System.currentTimeMillis() - UserApi.getLastSeen(player.getUuid()).getTime();
        if (afkDuration > timeoutSeconds * 1000L) {
            afkPlayer.stuff$enableAfk();
        }
    }

    @Inject(method = "onPlayerMove", at = @At("HEAD"))
    private void checkPlayerLook(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (Config.PacketOptions.resetOnLook && packet.changesLook()) {
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            if (pitch != packet.getPitch(pitch) || yaw != packet.getYaw(yaw))
                UserApi.updateLastSeen(player.getUuid());
        }

        // Enzo's alergy
        //if (player.getName().getString().equals("Enzoquest10")) {
        //    if (!player.getWorld().getEntitiesByClass(CatEntity.class, player.getBoundingBox().expand(10.0D, 10.0D, 10.0D), null).isEmpty()) {
        //        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 1));
        //    }
        //}
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (Permissions.check(player, "stuff.muted")) ci.cancel();
    }
}

package com.kryeit.stuff.mixin;

import com.kryeit.stuff.Utils;
import com.kryeit.stuff.afk.AfkPlayer;
import com.kryeit.stuff.afk.Config;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
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
        long afkDuration = Util.getMeasuringTimeMs() - this.player.getLastActionTime();
        if (afkDuration > timeoutSeconds * 1000L) {
            afkPlayer.stuff$enableAfk();
            if (!Permissions.check(player, "stuff.afk", false)) {
                this.player.networkHandler.disconnect(Text.of("You've been kicked to leave room for other players"));
            }
        }
    }

    @Inject(method = "onPlayerMove", at = @At("HEAD"))
    private void checkPlayerLook(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (Config.PacketOptions.resetOnLook && packet.changesLook()) {
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            if (pitch != packet.getPitch(pitch) || yaw != packet.getYaw(yaw)) player.updateLastActionTime();
        }
    }
}

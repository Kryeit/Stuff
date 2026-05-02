package com.kryeit.stuff.mixin;

import com.kryeit.stuff.GerenteClient;
import com.kryeit.stuff.Stuff;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(ServerboundChatPacket packet, CallbackInfo ci) {
        boolean muted = Stuff.GERENTE.getCachedJoinInfo(player.getUUID())
                .map(GerenteClient.PlayerJoinInfo::muted)
                .orElse(false);

        if (muted) ci.cancel();
    }
}
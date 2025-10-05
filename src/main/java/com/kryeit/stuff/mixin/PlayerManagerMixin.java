package com.kryeit.stuff.mixin;

import com.kryeit.stuff.Analytics;
import com.kryeit.stuff.listener.JoinDisconnectHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {
    @Shadow
    public abstract MinecraftServer getServer();

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void onPlayerConnect(Connection p_11262_, ServerPlayer p_11263_, CommonListenerCookie p_301988_, CallbackInfo ci) {
        JoinDisconnectHandler.onJoin(getServer());
    }

    @Inject(method = "remove", at = @At("TAIL"))
    public void onPlayerDisconnect(ServerPlayer player, CallbackInfo ci) {
        JoinDisconnectHandler.onDisconnect(getServer());

        Analytics.storeSessionEnd(player.getUUID());
    }
}
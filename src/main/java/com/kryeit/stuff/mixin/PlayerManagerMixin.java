package com.kryeit.stuff.mixin;

import com.kryeit.stuff.Utils;
import com.kryeit.stuff.listener.JoinDisconnectHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    public abstract MinecraftServer getServer();

    @Shadow
    @Final
    protected int maxPlayers;

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        JoinDisconnectHandler.onJoin(getServer());
    }

    @Inject(method = "remove", at = @At("TAIL"))
    public void onPlayerDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        JoinDisconnectHandler.onDisconnect(getServer());
    }

    @Redirect(method = "checkCanJoin", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int getPlayerListSize(List<?> list) {
        if (list.size() < maxPlayers) {
            return list.size();
        }

        List<ServerPlayerEntity> afkPlayers = Utils.getAfkPlayersSorted();
        if (!afkPlayers.isEmpty()) {
            afkPlayers.get(0).networkHandler.disconnect(Text.of("You were kicked to make room for new players."));
            return list.size() - 1;
        } else {
            return list.size();
        }
    }
}

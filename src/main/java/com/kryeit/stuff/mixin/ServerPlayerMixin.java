package com.kryeit.stuff.mixin;

import com.kryeit.stuff.Utils;
import com.kryeit.stuff.afk.AfkPlayer;
import com.kryeit.stuff.afk.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.kryeit.stuff.Stuff.lastActiveTime;

// This class has been mostly made by afkdisplay mod
// https://github.com/beabfc/afkdisplay
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends Entity implements AfkPlayer {
    @Shadow
    @Final
    public MinecraftServer server;
    @Unique
    public ServerPlayerEntity stuff$player = (ServerPlayerEntity) (Object) this;
    @Unique
    private boolean stuff$isAfk;

    public ServerPlayerMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    public boolean stuff$isAfk() {
        return this.stuff$isAfk;
    }

    @Unique
    public void stuff$enableAfk() {
        if (stuff$isAfk()) return;
        stuff$setAfk(true);
    }

    @Unique
    public void stuff$disableAfk() {
        if (!stuff$isAfk) return;
        lastActiveTime.put(stuff$player.getUuid(), System.currentTimeMillis());
        stuff$setAfk(false);
    }

    @Unique
    private void stuff$setAfk(boolean isAfk) {
        this.stuff$isAfk = isAfk;
        this.server
                .getPlayerManager()
                .sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, stuff$player));
    }

    @Inject(method = "updateLastActionTime", at = @At("TAIL"))
    private void onActionTimeUpdate(CallbackInfo ci) {
        stuff$disableAfk();
    }

    public void setPosition(double x, double y, double z) {
        if (Config.PacketOptions.resetOnMovement && (this.getX() != x || this.getY() != y || this.getZ() != z)) {
            stuff$player.updateLastActionTime();
        }

        super.setPosition(x, y, z);
    }

    @Inject(method = "getPlayerListName", at = @At("RETURN"), cancellable = true)
    private void replacePlayerListName(CallbackInfoReturnable<Text> cir) {

        MutableText name = stuff$player.getName().copy().formatted(Formatting.WHITE);

        if (Config.PlayerListOptions.enableListDisplay && stuff$isAfk) {
            Formatting color = Formatting.byName(Config.PlayerListOptions.afkColor);
            if (color == null) color = Formatting.RESET;
            name = name.formatted(color);
        }

        cir.setReturnValue(Utils.prefix(stuff$player).append(name));
    }

    // Solves End -> Overworld teleportation issue
    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    private void onMoveToWorld(ServerWorld destination, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        RegistryKey<World> fromWorldKey = player.getWorld().getRegistryKey();
        RegistryKey<World> toWorldKey = destination.getRegistryKey();
        if (fromWorldKey.equals(World.END) && toWorldKey.equals(World.OVERWORLD)) {
            player.teleport(destination,
                    destination.getSpawnPos().getX(),
                    destination.getSpawnPos().getY(),
                    destination.getSpawnPos().getZ(),
                    player.getYaw(),
                    player.getPitch());

            cir.setReturnValue(player);
        }
    }
}

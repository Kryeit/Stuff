package com.kryeit.stuff.mixin;

import com.kryeit.stuff.afk.AfkPlayer;
import com.kryeit.stuff.afk.Config;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
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

// This class has been mostly made by afkdisplay mod
// https://github.com/beabfc/afkdisplay
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends Entity implements AfkPlayer {
    @Shadow
    @Final
    public MinecraftServer server;
    @Unique
    public ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    @Unique
    private boolean isAfk;

    public ServerPlayerMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    public boolean stuff$isAfk() {
        return this.isAfk;
    }

    @Unique
    public void stuff$enableAfk() {
        if (stuff$isAfk()) return;
        setAfk(true);
    }

    @Unique
    public void stuff$disableAfk() {
        if (!isAfk) return;
        setAfk(false);
    }

    @Unique
    private void setAfk(boolean isAfk) {
        this.isAfk = isAfk;
        this.server
                .getPlayerManager()
                .sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
    }

    @Inject(method = "updateLastActionTime", at = @At("TAIL"))
    private void onActionTimeUpdate(CallbackInfo ci) {
        stuff$disableAfk();
    }

    public void setPosition(double x, double y, double z) {
        if (Config.PacketOptions.resetOnMovement && (this.getX() != x || this.getY() != y || this.getZ() != z)) {
            player.updateLastActionTime();
        }
        super.setPosition(x, y, z);
    }

    @Inject(method = "getPlayerListName", at = @At("RETURN"), cancellable = true)
    private void replacePlayerListName(CallbackInfoReturnable<Text> cir) {
        MutableText cog = Text.literal("⚙").setStyle(Style.EMPTY.withBold(true)).setStyle(Style.EMPTY.withFormatting(Formatting.GOLD));
        MutableText anchor = Text.literal("⚓").setStyle(Style.EMPTY.withBold(true)).setStyle(Style.EMPTY.withFormatting(Formatting.RED));

        MutableText text = player.getName().copy().setStyle(Style.EMPTY.withFormatting(Formatting.WHITE));
        if (Config.PlayerListOptions.enableListDisplay && isAfk) {
            Formatting color = Formatting.byName(Config.PlayerListOptions.afkColor);
            if (color == null) color = Formatting.RESET;
            text = text.formatted(color);
        }

        if (Permissions.check(player, "group.kryeitor")) {
            cir.setReturnValue(cog.append(text));
        } else if (Permissions.check(player, "group.postbuilder")) {
            cir.setReturnValue(anchor.append(text));
        }else {
            cir.setReturnValue(text);
        }
    }
}

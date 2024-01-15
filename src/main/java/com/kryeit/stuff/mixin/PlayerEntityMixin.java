package com.kryeit.stuff.mixin;

import com.kryeit.stuff.Utils;
import com.kryeit.stuff.afk.AfkPlayer;
import com.kryeit.stuff.afk.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        MutableText name = cir.getReturnValue().copy().setStyle(
                Style.EMPTY.withFormatting(Utils.getFormattingForTab(player))
        );

        AfkPlayer afkPlayer = (AfkPlayer) player;
        if (Config.PlayerListOptions.enableListDisplay && afkPlayer.stuff$isAfk()) {
            Formatting color = Formatting.byName(Config.PlayerListOptions.afkColor);
            if (color == null) color = Formatting.RESET;
            name = name.formatted(color);
        }

        cir.setReturnValue(Utils.prefix(player).append(name));
    }
}

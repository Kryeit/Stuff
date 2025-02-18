package com.kryeit.stuff.mixin;

import com.kryeit.stuff.Analytics;
import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.auth.UserApi;
import com.kryeit.stuff.config.StaticConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Shadow
    @Nullable
    GameProfile profile;
    @Final
    @Shadow
    ClientConnection connection;

    @Shadow
    @Final
    MinecraftServer server;

    @Inject(at = @At("RETURN"), method = "acceptPlayer")
    private void init(CallbackInfo ci) {
        UUID id = this.profile.getId();
        String name = this.profile.getName();

        if (connection.getAddress() instanceof InetSocketAddress address && StaticConfig.production) {
            Analytics.storeSessionStart(id, address.getAddress().getHostAddress());
        }

        UserApi.createUser(id, name);
        // Has NOT joined before
        if (UserApi.getLastSeen(id) == null) {
            MinecraftServerSupplier.getServer().getPlayerManager().broadcast(
                    Text.literal("Welcome " + name + " to Kryeit!").formatted(Formatting.AQUA),
                    false
            );
        }

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(id);
        if (player == null) return;
        UserApi.updateLastSeen(player.getUuid());

        if (player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) > 72000)
            return;

        player.sendMessage(Text.literal("Kryeit is fairly vanilla, but it has custom systems:").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal(" - Claim system (use /claim and /abandon)").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal(" - Mission system (use /missions)").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal(" - Teleport system (use /post and /setpost)").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal("For more information: https://kryeit.com/discord, in #guides forum channel")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://kryeit.com/discord")))
        );
        player.sendMessage(Text.literal("To contribute to Kryeit's development see /donate").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal("Read the /rules and have fun!").formatted(Formatting.AQUA));
    }
}

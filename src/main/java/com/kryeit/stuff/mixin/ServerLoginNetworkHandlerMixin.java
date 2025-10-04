package com.kryeit.stuff.mixin;

import com.kryeit.stuff.Analytics;
import com.kryeit.stuff.GerenteClient;
import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.compat.BluemapImpl;
import com.kryeit.stuff.config.StaticConfig;
import com.mojang.authlib.GameProfile;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

        GerenteClient.PlayerJoinInfo joinInfo = Stuff.GERENTE.handlePlayerJoin(id, name);

        if (connection.getAddress() instanceof InetSocketAddress address && StaticConfig.enableAnalytics) {
            Analytics.storeSessionStart(id, address.getAddress().getHostAddress());
        }

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(id);
        if (player == null) return;
        server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));

        LuckPerms luckPerms = LuckPermsProvider.get();
        luckPerms.getUserManager().modifyUser(id, user -> {
            Set<String> roleIds = joinInfo.roles().stream()
                    .map(GerenteClient.Role::id)
                    .collect(Collectors.toSet());

            for (String role : roleIds) {
                user.data().add(Node.builder("group.synced." + role).build());
            }

            for (Group group : user.getInheritedGroups(user.getQueryOptions())) {
                if (!roleIds.contains(group.getName().substring("synced.".length())) && group.getName().startsWith("synced.")) {
                    user.data().remove(Node.builder("group." + group.getName()).build());
                }
            }
        });

        if (joinInfo.firstJoin()) {
            MinecraftServerSupplier.getServer().getPlayerManager().broadcast(
                    Text.literal("Welcome " + name + " to Kryeit!").formatted(Formatting.AQUA),
                    false
            );
        }

        if (joinInfo.banReason() != null) {
            connection.disconnect(Text.literal("You're banned. Reason: " + joinInfo.banReason()));
        }

        BluemapImpl.changePlayerVisibility(id, joinInfo.preferences().get("show_on_map").getAsBoolean());

        if (player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) > 72000)
            return;

        player.sendMessage(Text.literal("Kryeit is fairly vanilla, but it has custom systems:").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal(" - Claim system (use /claim and /abandon)").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal(" - Mission system (use /missions)").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal(" - Teleport system (use /post and /setpost)").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal("For more information use /discord, in #guides forum channel")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://kryeit.com/discord")))
        );
        player.sendMessage(Text.literal("To contribute to Kryeit's development see /donate").formatted(Formatting.AQUA));
        player.sendMessage(Text.literal("Read the /rules and have fun!").formatted(Formatting.GOLD));
    }
}

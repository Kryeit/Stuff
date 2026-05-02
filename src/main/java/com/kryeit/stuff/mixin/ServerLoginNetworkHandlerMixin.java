package com.kryeit.stuff.mixin;

import com.google.gson.JsonElement;
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
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginNetworkHandlerMixin {

    @Final
    @Shadow
    Connection connection;

    @Shadow
    @Final
    MinecraftServer server;

    @Shadow @Nullable private GameProfile authenticatedProfile;

    @Inject(at = @At("RETURN"), method = "handleHello")
    private void init(CallbackInfo ci) {
        UUID id = this.authenticatedProfile.getId();
        String name = this.authenticatedProfile.getName();

        GerenteClient.PlayerJoinInfo joinInfo = Stuff.GERENTE.handlePlayerJoin(id, name);

        if (connection.getRemoteAddress() instanceof InetSocketAddress address && StaticConfig.enableAnalytics) {
            Analytics.storeSessionStart(id, address.getAddress().getHostAddress());
        }

        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player == null) return;
        server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));

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
            MinecraftServerSupplier.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("Welcome " + name + " to Kryeit!").withStyle(ChatFormatting.AQUA),
                    false
            );
        }

        if (joinInfo.banReason() != null) {
            connection.disconnect(Component.literal("You're banned. Reason: " + joinInfo.banReason()));
        }

        JsonElement showOnMap = joinInfo.preferences().get("show_on_map");
        BluemapImpl.changePlayerVisibility(id, showOnMap == null || showOnMap.getAsBoolean());

        if (player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) > 72000)
            return;

        player.sendSystemMessage(Component.literal("Kryeit is fairly vanilla, but it has custom systems:").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal(" - Claim system (use /claim and /abandon)").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal(" - Mission system (use /missions)").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal(" - Teleport system (use /post and /setpost)").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("For more information use /discord, in #guides forum channel")
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://kryeit.com/discord")))
        );
        player.sendSystemMessage(Component.literal("To contribute to Kryeit's development see /donate").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Read the /rules and have fun!").withStyle(ChatFormatting.GOLD));
    }
}
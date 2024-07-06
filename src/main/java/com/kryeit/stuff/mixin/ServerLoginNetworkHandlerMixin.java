package com.kryeit.stuff.mixin;

import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.Utils;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {

    @Shadow @Nullable private GameProfile profile;

    @Inject(at = @At("RETURN"), method = "acceptPlayer")
    private void init(CallbackInfo ci) {
        UUID id = this.profile.getId();
        String name = this.profile.getName();

        if (Utils.isServerFullEnough())
            Utils.kickAFKPlayers();

        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(id);
        File playerDataDirectory = new File("world/playerdata/");

        File[] playerDataFiles = playerDataDirectory.listFiles();

        if (playerDataFiles == null) return;

        assert player != null;
        boolean hasPlayedMoreThanOneHour = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) > 72000;

        for (File playerDataFile : playerDataFiles) {
            String fileName = playerDataFile.getName();
            if (!fileName.endsWith(".dat")) continue;
            UUID otherId = UUID.fromString(fileName.substring(0, fileName.length() - 4));
            if (id.equals(otherId) && hasPlayedMoreThanOneHour) {
                // Has joined before
                Stuff.lastActiveTime.put(id, System.currentTimeMillis());
                return;
            }
        }

        // Has NOT joined before
        MinecraftServerSupplier.getServer().getPlayerManager().broadcast(
                Text.literal("Welcome " + name + " to Kryeit!").formatted(Formatting.AQUA),
                false
        );

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

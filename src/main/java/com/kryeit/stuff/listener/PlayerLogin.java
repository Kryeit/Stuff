package com.kryeit.stuff.listener;

import com.kryeit.stuff.Utils;
import com.kryeit.stuff.auth.UserApi;
import io.github.fabricators_of_create.porting_lib.entity.events.PlayerEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerLogin implements PlayerEvents.PlayerLoggedInOrOut {
    @Override
    public void handleConnection(PlayerEntity player) {
        UserApi.updateLastSeenAndStats(player.getUuid(), Utils.getStatsJson((ServerPlayerEntity) player));
    }
}

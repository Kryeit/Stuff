package com.kryeit.stuff.compat;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import net.fabricmc.loader.api.FabricLoader;

import java.util.UUID;
import java.util.function.IntSupplier;

public class GriefDefenderImpl {

    public static final String ID = "griefdefender";

    public static boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded(ID);
    }

    public static void giveClaimBlocks(UUID playerID, int amount) {
        IntSupplier supplier = () -> {
            User user = GriefDefender.getCore().getUser(playerID);
            if (user == null) {
                return -1;
            }
            user.getPlayerData().setBonusClaimBlocks(user.getPlayerData().getBonusClaimBlocks() + amount);
            return user.getPlayerData().getInitialClaimBlocks() + user.getPlayerData().getAccruedClaimBlocks() + user.getPlayerData().getBonusClaimBlocks();
        };
        supplier.getAsInt();
    }
}

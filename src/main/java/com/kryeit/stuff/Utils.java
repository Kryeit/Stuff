package com.kryeit.stuff;

public class Utils {

    public static boolean isServerFullEnough() {
        return MinecraftServerSupplier.getServer().getMaxPlayerCount() - 2 <= MinecraftServerSupplier.getServer().getCurrentPlayerCount();
    }
}

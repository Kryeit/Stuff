package com.kryeit.stuff.storage;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class ModStats {
    public static final Identifier AFK_TIME = new Identifier("stuff", "afk_time");

    public static void registerStats() {
        Registry.register(Registries.CUSTOM_STAT, "missions_rerolled", AFK_TIME);
        Stats.CUSTOM.getOrCreateStat(AFK_TIME, StatFormatter.TIME);
    }
}

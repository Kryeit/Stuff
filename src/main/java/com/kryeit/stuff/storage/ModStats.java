package com.kryeit.stuff.storage;


import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class ModStats {
    public static final ResourceLocation AFK_TIME = ResourceLocation.fromNamespaceAndPath("stuff", "afk_time");

    public static void registerStats() {
        Registry.register(BuiltInRegistries.CUSTOM_STAT, "afk_time", AFK_TIME);
        Stats.CUSTOM.get(AFK_TIME, StatFormatter.TIME);
    }
}

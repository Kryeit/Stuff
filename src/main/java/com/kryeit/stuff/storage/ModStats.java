package com.kryeit.stuff.storage;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

public class ModStats {
    public static final ResourceLocation AFK_TIME = ResourceLocation.fromNamespaceAndPath("stuff", "afk_time");

    public static void registerStats(RegisterEvent event) {
        event.register(BuiltInRegistries.CUSTOM_STAT.key(), registry -> {
//            registry.register(AFK_TIME, AFK_TIME);
//            Stats.CUSTOM.get(AFK_TIME, StatFormatter.TIME);
        });
    }
}

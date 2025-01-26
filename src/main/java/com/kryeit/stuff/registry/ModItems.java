package com.kryeit.stuff.registry;

import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.content.item.util.AutoModeledPolymerItem;
import com.kryeit.stuff.content.item.util.ModeledItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item KRYEIT_COG = register("kryeit_cog", new ModeledItem(new Item.Settings()));


    public static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(new Identifier(Stuff.MODID, "group"), ItemGroup.create(ItemGroup.Row.BOTTOM, -1)
                .icon(KRYEIT_COG::getDefaultStack)
                .displayName(Text.translatable("itemgroup." + Stuff.MODID))
                .entries(((context, entries) -> {
                    entries.add(KRYEIT_COG);
                })).build()
        );
    }


    public static <T extends Item> T register(String path, T item) {
        Registry.register(Registries.ITEM, new Identifier(Stuff.MODID, path), item);
        if (item instanceof AutoModeledPolymerItem modeledPolymerItem) {
            modeledPolymerItem.defineModels(new Identifier(Stuff.MODID, path));
        }
        return item;
    }
}

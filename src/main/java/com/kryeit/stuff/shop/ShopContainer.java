package com.kryeit.stuff.shop;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class ShopContainer extends SimpleContainer {
    public ShopContainer(int size) {
        super(size);
    }

    @Override
    public boolean canTakeItem(Container p_273520_, int p_272681_, ItemStack p_273702_) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int p_18952_, ItemStack p_18953_) {
        return false;
    }

}

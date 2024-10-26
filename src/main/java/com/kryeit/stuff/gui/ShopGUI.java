package com.kryeit.stuff.gui;

import com.kryeit.stuff.Utils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ShopGUI extends SimpleGui {

    private static final ItemStack COPYCAT_STEP = Utils.getItemStack("create", "copycat_step");

    public ShopGUI(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X1, player, false);
        this.setTitle(Text.literal("Shop"));

        ItemStack copycats = COPYCAT_STEP.setCustomName(Text.literal("Copycat shop").formatted(Formatting.GOLD));
        this.addSlot(copycats);

        this.open();
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {

        if (element.getItemStack().getItem() == COPYCAT_STEP.getItem()) {
            new CopycatsGUI(player);
        }

        return false;
    }

}

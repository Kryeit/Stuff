package com.kryeit.stuff.gui;

import com.kryeit.stuff.Utils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

public class ShopGUI extends SimpleGui {

    private static final ItemStack COPYCAT_STEP = Utils.getItemStack("create", "copycat_step");
    private static final ItemStack STAFF_HEAD = Utils.getItemStack("minecraft", "player_head");

    public ShopGUI(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X1, player, false);
        this.setTitle(Text.literal("Shop"));

        ItemStack copycats = COPYCAT_STEP.setCustomName(Text.literal("Copycat shop").formatted(Formatting.GOLD));
        this.addSlot(copycats);

        String[] staffNames = {"MuriPlz", "MrRedRhino", "__Tesseract"};
        ItemStack staff = GuiUtils.getPlayerHeadItem(staffNames[new Random().nextInt(staffNames.length)],
                Text.literal("Player head shop").formatted(Formatting.GOLD),
                Text.empty());

        this.addSlot(staff);

        this.open();
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {

        if (element.getItemStack().getItem() == COPYCAT_STEP.getItem()) {
            new CopycatsGUI(player);
        }

        if (element.getItemStack().getItem() == STAFF_HEAD.getItem()) {
            new PlayersGUI(player);
        }

        return false;
    }

}

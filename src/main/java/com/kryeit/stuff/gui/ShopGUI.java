package com.kryeit.stuff.gui;

import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.Utils;
import com.kryeit.stuff.ui.GuiTextures;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Random;

public class ShopGUI extends SimpleGui {

    private static final ItemStack COPYCAT_STEP = Utils.getItemStack("create", "copycat_step");
    private static final ItemStack STAFF_HEAD = Utils.getItemStack("minecraft", "player_head");

    public ShopGUI(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.setTitle(GuiTextures.SHOP.apply(Text.literal("Shop")));

        ItemStack copycats = COPYCAT_STEP.copy();
        copycats.setCustomName(Text.literal("Copycat shop").formatted(Formatting.GOLD));

        NbtList loreList = new NbtList();
        loreList.add(NbtString.of(Text.Serializer.toJson(Text.literal("A variety of Copycats to choose from :)").formatted(Formatting.LIGHT_PURPLE))));

        copycats.getOrCreateSubNbt("display").put("Lore", loreList);
        this.setSlot(20, copycats);


        // Discs in the 13

        String[] staffNames = {"MuriPlz", "MrRedRhino", "__Tesseract", "RatInATopHat427"};
        String[] onlineStaffNames = MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList().stream()
                .filter(playerEntity -> Arrays.asList(staffNames).contains(playerEntity.getName().getString()))
                .map(playerEntity -> playerEntity.getName().getString())
                .toArray(String[]::new);

        if (onlineStaffNames.length == 0) {
            onlineStaffNames = new String[]{MinecraftServerSupplier.getServer().getPlayerNames()[new Random().nextInt(MinecraftServerSupplier.getServer().getPlayerNames().length)]};
        }

        ItemStack staff = GuiUtils.getPlayerHeadItem(onlineStaffNames[new Random().nextInt(onlineStaffNames.length)],
                Text.literal("Player head shop").formatted(Formatting.GOLD),
                Text.literal("A place to buy the heads of online players!").formatted(Formatting.LIGHT_PURPLE));
        this.setSlot(24, staff);

        this.open();
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {

        if (element == null) return false;

        if (element.getItemStack().getItem() == COPYCAT_STEP.getItem()) {
            new CopycatsGUI(player);
        }

        if (element.getItemStack().getItem() == STAFF_HEAD.getItem()) {
            new PlayersGUI(player);
        }

        return false;
    }

}

package com.kryeit.stuff.gui;

import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.Utils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.List;

public class CopycatsGUI extends PaginatedGUI {
    LinkedHashMap<Item, Integer> items = new LinkedHashMap<>();

    private static final ItemStack COPYCAT_FENCE = Utils.getItemStack("copycats", "copycat_fence");
    private static final ItemStack COPYCAT_DOOR = Utils.getItemStack("copycats", "copycat_door");
    private static final ItemStack COPYCAT_IRON_DOOR = Utils.getItemStack("copycats", "copycat_iron_door");
    private static final ItemStack COPYCAT_TRAPDOOR = Utils.getItemStack("copycats", "copycat_trapdoor");
    private static final ItemStack COPYCAT_IRON_TRAPDOOR = Utils.getItemStack("copycats", "copycat_iron_trapdoor");
    private static final ItemStack COPYCAT_WOODEN_BUTTON = Utils.getItemStack("copycats", "copycat_wooden_button");
    private static final ItemStack COPYCAT_STONE_BUTTON = Utils.getItemStack("copycats", "copycat_stone_button");
    private static final ItemStack COPYCAT_WOODEN_PRESSURE_PLATE = Utils.getItemStack("copycats", "copycat_wooden_pressure_plate");
    private static final ItemStack COPYCAT_STONE_PRESSURE_PLATE = Utils.getItemStack("copycats", "copycat_stone_pressure_plate");
    private static final ItemStack COPYCAT_LADDER = Utils.getItemStack("copycats", "copycat_ladder");
    private static final ItemStack COPYCAT_FLUID_PIPE = Utils.getItemStack("copycats", "copycat_fluid_pipe");
    private static final ItemStack COPYCAT_SHAFT = Utils.getItemStack("copycats", "copycat_shaft");
    private static final ItemStack COPYCAT_COGWHEEL = Utils.getItemStack("copycats", "copycat_cogwheel");
    private static final ItemStack COPYCAT_LARGE_COGWHEEL = Utils.getItemStack("copycats", "copycat_large_cogwheel");
    private static final ItemStack COPYCAT_HEADSTOCK = Utils.getItemStack("railway", "copycat_headstock");

    private static final ItemStack IRON_COIN = Utils.getItemStack("createdeco", "iron_coin");

    int REQUIRED_COINS = 5;

    public CopycatsGUI(ServerPlayerEntity player) {
        super(player, "Copycats");

        items.put(COPYCAT_FENCE.getItem(), 4);
        items.put(COPYCAT_DOOR.getItem(), 4);
        items.put(COPYCAT_IRON_DOOR.getItem(), 4);
        items.put(COPYCAT_TRAPDOOR.getItem(), 5);
        items.put(COPYCAT_IRON_TRAPDOOR.getItem(), 5);
        items.put(COPYCAT_WOODEN_BUTTON.getItem(), 8);
        items.put(COPYCAT_STONE_BUTTON.getItem(), 8);
        items.put(COPYCAT_WOODEN_PRESSURE_PLATE.getItem(), 6);
        items.put(COPYCAT_STONE_PRESSURE_PLATE.getItem(), 6);
        items.put(COPYCAT_LADDER.getItem(), 6);
        items.put(COPYCAT_FLUID_PIPE.getItem(), 4);
        items.put(COPYCAT_SHAFT.getItem(), 8);
        items.put(COPYCAT_COGWHEEL.getItem(), 6);
        items.put(COPYCAT_LARGE_COGWHEEL.getItem(), 4);
        items.put(COPYCAT_HEADSTOCK.getItem(), 2);

        populate();
        this.open();
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        super.onClick(index, type, action, element);

        GuiElementInterface slot = this.getSlot(index);
        if (slot == null) return false;
        ItemStack clickedItem = slot.getItemStack();
        if (clickedItem == null) return false;

        if (!items.containsKey(clickedItem.getItem())) {
            return false;
        }

        int itemAmount = items.get(clickedItem.getItem());

        int playerCoins = getPlayerCoinCount();
        if (playerCoins >= REQUIRED_COINS) {
            if (hasInventorySpace(clickedItem.getItem(), itemAmount)) {
                removePlayerCoins(REQUIRED_COINS);
                player.giveItemStack(new ItemStack(clickedItem.getItem(), itemAmount));

                player.sendMessage(Text.literal("Successfully purchased " + itemAmount + " " + clickedItem.getName().getString()), false);
            } else {
                player.sendMessage(Text.literal("Your inventory is full. Clear some space before making a purchase."), false);
            }
        } else {
            player.sendMessage(Text.literal("You do not have enough coins to make this purchase."), false);
        }

        return false;
    }

    @Override
    public void populate() {
        this.clearItems();

        int start = page * 3 * 7;
        int end = start + 3 * 7;

        int index = 0;
        for (Item item : items.keySet()) {
            if (index >= start && index < end) {
                int row = (index - start) / 7;
                int col = (index - start) % 7 + 1;
                int slotIndex = row * 9 + col;

                ItemStack itemStack = new ItemStack(item, items.get(item));
                NbtList loreList = new NbtList();
                loreList.add(NbtString.of(Text.Serializer.toJson(Text.literal("Buy 1 for " + REQUIRED_COINS + " iron coins").formatted(Formatting.LIGHT_PURPLE))));
                itemStack.getOrCreateSubNbt("display").put("Lore", loreList);
                this.setSlot(slotIndex, itemStack);
            }
            index++;
        }
    }

    private int getPlayerCoinCount() {
        int coinCount = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == IRON_COIN.getItem()) {
                coinCount += stack.getCount();
            }
        }
        return coinCount;
    }

    private void removePlayerCoins(int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == IRON_COIN.getItem()) {
                int remove = Math.min(stack.getCount(), remaining);
                stack.decrement(remove);
                remaining -= remove;
                if (remaining <= 0) break;
            }
        }
    }

    private boolean hasInventorySpace(Item item, int amount) {
        int remaining = amount;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (stack.isEmpty()) {
                return true;
            } else if (stack.getItem() == item && stack.getCount() < stack.getMaxCount()) {
                int spaceAvailable = stack.getMaxCount() - stack.getCount();
                remaining -= spaceAvailable;

                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

}

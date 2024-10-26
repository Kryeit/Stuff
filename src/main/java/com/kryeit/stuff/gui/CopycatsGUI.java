package com.kryeit.stuff.gui;

import com.kryeit.stuff.Utils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;

public class CopycatsGUI extends SimpleGui {
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

    private static final ItemStack IRON_COIN = Utils.getItemStack("createdeco", "iron_coin");

    public CopycatsGUI(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        this.setTitle(Text.literal("Copycat shop"));

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

        for (Item item : items.keySet()) {
            int amount = items.get(item);
            ItemStack itemStack = new ItemStack(item, amount);

            itemStack.setCustomName(Text.literal(itemStack.getName().getString()).formatted(Formatting.GOLD));

            NbtList loreList = new NbtList();
            loreList.add(NbtString.of(Text.Serializer.toJson(Text.literal("Buy " + amount + " for 1 Iron coin").formatted(Formatting.LIGHT_PURPLE))));

            itemStack.getOrCreateSubNbt("display").put("Lore", loreList);

            this.addSlot(itemStack);
        }

        ItemStack back = Utils.getItemStack("createdeco", "decal_left");
        back.setCustomName(Text.literal("Go back").formatted(Formatting.RED));
        this.setSlot(18, back);

        this.open();
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        ItemStack clickedItem = this.getSlot(index).getItemStack();
        if (clickedItem == null) return false;

        if (clickedItem.getItem() == Utils.getItemStack("createdeco", "decal_left").getItem()) {
            new ShopGUI(player);
            return false;
        }

        if (!items.containsKey(clickedItem.getItem())) {
            return false;
        }

        int requiredCoins = 1;
        int itemAmount = items.get(clickedItem.getItem());

        int playerCoins = getPlayerCoinCount();
        if (playerCoins >= requiredCoins) {
            if (hasInventorySpace(clickedItem.getItem(), itemAmount)) {
                removePlayerCoins(requiredCoins);
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

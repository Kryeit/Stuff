package com.kryeit.stuff.gui;

import com.kryeit.stuff.Utils;
import com.kryeit.stuff.MinecraftServerSupplier;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PlayersGUI extends SimpleGui {
    private static final ItemStack COPPER_COIN = Utils.getItemStack("createdeco", "copper_coin");
    private static final Item PLAYER_HEAD_ITEM = Utils.getItemStack("minecraft", "player_head").getItem();

    int REQUIRED_COINS = 1;

    public PlayersGUI(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.setTitle(Text.literal("Player head shop"));

        // Add online player heads to the shop
        List<ServerPlayerEntity> onlinePlayers = MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList();
        for (ServerPlayerEntity onlinePlayer : onlinePlayers) {
            ItemStack playerHead = new ItemStack(PLAYER_HEAD_ITEM);
            NbtCompound skullOwner = new NbtCompound();
            skullOwner.putString("Name", onlinePlayer.getName().getString());
            skullOwner.putUuid("Id", onlinePlayer.getUuid());
            playerHead.getOrCreateNbt().put("SkullOwner", skullOwner);

            playerHead.setCustomName(Text.literal(onlinePlayer.getName().getString() + "'s player head").formatted(Formatting.GOLD));

            NbtList loreList = new NbtList();
            loreList.add(NbtString.of(Text.Serializer.toJson(Text.literal("Buy 1 for " + REQUIRED_COINS + " Copper coin").formatted(Formatting.LIGHT_PURPLE))));

            playerHead.getOrCreateSubNbt("display").put("Lore", loreList);

            this.addSlot(playerHead);
        }

        ItemStack back = Utils.getItemStack("createdeco", "decal_left");
        back.setCustomName(Text.literal("Go back").formatted(Formatting.RED));
        this.setSlot(18, back);

        this.open();
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        GuiElementInterface slot = this.getSlot(index);
        if (slot == null) return false;
        ItemStack clickedItem = slot.getItemStack();
        if (clickedItem == null) return false;

        if (clickedItem.getItem() == Utils.getItemStack("createdeco", "decal_left").getItem()) {
            new ShopGUI(player);
            return false;
        }

        if (clickedItem.getItem() != PLAYER_HEAD_ITEM) {
            return false;
        }

        int itemAmount = 1;

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

    private int getPlayerCoinCount() {
        int coinCount = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == COPPER_COIN.getItem()) {
                coinCount += stack.getCount();
            }
        }
        return coinCount;
    }

    private void removePlayerCoins(int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == COPPER_COIN.getItem()) {
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
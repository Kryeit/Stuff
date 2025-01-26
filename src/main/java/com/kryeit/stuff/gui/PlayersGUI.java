package com.kryeit.stuff.gui;

import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.Utils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PlayersGUI extends PaginatedGUI {
    private static final ItemStack COPPER_COIN = Utils.getItemStack("createdeco", "copper_coin");
    public static final Item PLAYER_HEAD_ITEM = Utils.getItemStack("minecraft", "player_head").getItem();

    int REQUIRED_COINS = 3;

    public PlayersGUI(ServerPlayerEntity player) {
        super(player, "Player Heads");
        this.open();
    }

    @Override
    protected void populate() {
        for (int i = 0; i < this.getSize(); i++) {
            this.setSlot(i, ItemStack.EMPTY);
        }

        int page = this.page;

        int start = page * 3 * 7;
        int end = start + 3 * 7;

        List<ServerPlayerEntity> onlinePlayers = MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList();
        for (int i = start; i < end; i++) {
            if (i >= onlinePlayers.size()) {
                break;
            }

            ServerPlayerEntity onlinePlayer = onlinePlayers.get(i);
            ItemStack playerHead = GuiUtils.getPlayerHeadItem(onlinePlayer.getName().getString(),
                    Text.literal(onlinePlayer.getName().getString() + "'s player head").formatted(Formatting.GOLD),
                    Text.literal("Buy 1 for " + REQUIRED_COINS + " Copper coin").formatted(Formatting.LIGHT_PURPLE));

            int row = (i - start) / 7;
            int col = (i - start) % 7 + 1;
            int slotIndex = row * 9 + col;

            this.setSlot(slotIndex, playerHead);
        }
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        GuiElementInterface slot = this.getSlot(index);
        if (slot == null) return false;
        ItemStack clickedItem = slot.getItemStack();
        if (clickedItem == null) return false;

        if (clickedItem.getItem() != PLAYER_HEAD_ITEM) {
            return false;
        }

        int itemAmount = 1;

        int playerCoins = getPlayerCoinCount();
        if (playerCoins >= REQUIRED_COINS) {
            if (hasInventorySpace(clickedItem.getItem(), itemAmount)) {
                removePlayerCoins(REQUIRED_COINS);
                player.giveItemStack(clickedItem.copy());

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
package com.kryeit.stuff.shop;

import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.compat.GriefDefenderImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ClaimBlockShopMenu extends ChestMenu {
    private static final List<ResourceLocation> COINS = List.of(
            ResourceLocation.fromNamespaceAndPath("createdeco", "copper_coin"),
            ResourceLocation.fromNamespaceAndPath("createdeco", "iron_coin"),
            ResourceLocation.fromNamespaceAndPath("createdeco", "gold_coin")
    );

    private static final Set<Integer> PAYMENT_SLOTS = Set.of(10, 11, 12);
    private static final int BUY_SLOT = 16;

    private final ShopContainer shopContainer;

    public ClaimBlockShopMenu(int containerId, Inventory playerInventory, ShopContainer shopContainer) {
        super(MenuType.GENERIC_9x3, containerId, playerInventory, shopContainer, 3);
        this.shopContainer = shopContainer;

        // Replace the default slot so only coins can be placed here
        for (int slotNumber : PAYMENT_SLOTS) {
            Slot paymentSlot = new Slot(shopContainer, slotNumber, slotX(slotNumber), slotY(slotNumber)) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return COINS.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
                }
            };
            paymentSlot.index = slotNumber;
            this.slots.set(slotNumber, paymentSlot);
        }

        updateClaimBlockCount(0);
        placeDecoration();
    }

    private void placeDecoration() {
        // top row
        for (int i = 0; i < 9; i++) {
            shopContainer.setItem(i, new ItemStack(Items.BLACK_STAINED_GLASS_PANE));
        }

        // bottom row
        for (int i = 18; i < 27; i++) {
            shopContainer.setItem(i, new ItemStack(Items.BLACK_STAINED_GLASS_PANE));
        }

        // middle
        shopContainer.setItem(9, new ItemStack(Items.BLACK_STAINED_GLASS_PANE));
        shopContainer.setItem(13, new ItemStack(Items.BLACK_STAINED_GLASS_PANE));
        shopContainer.setItem(15, new ItemStack(Items.BLACK_STAINED_GLASS_PANE));
        shopContainer.setItem(17, new ItemStack(Items.BLACK_STAINED_GLASS_PANE));

        // arrows
        ItemStack rightArrow = itemStackByRL("createdeco", "decal_right");
        shopContainer.setItem(14, rightArrow);
    }

    private ItemStack itemStackByRL(String namespace, String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path)));
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (PAYMENT_SLOTS.contains(slotId)) {
            super.clicked(slotId, dragType, clickType, player);

            updateClaimBlockCount(computeBuyableClaimBlocks(computePaymentValue(), player));
            return;
        }

        if (slotId == BUY_SLOT) {
            tryPurchase(player);
            return;
        }

        if (slotId >= 0 && slotId < shopContainer.getContainerSize()) {
            return; // decoration slots: never let vanilla move anything in/out
        }

        super.clicked(slotId, dragType, clickType, player);
    }

    private void updateClaimBlockCount(int count) {
        ItemStack confirmStack;
        if (count > 0) {
            MutableComponent name = Component.literal("Click to buy ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(format(count)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" claim blocks").withStyle(ChatFormatting.GREEN));
            confirmStack = named(new ItemStack(Items.LIME_DYE), name);
        } else {
            MutableComponent name = Component.literal("Place coins in the empty slots to buy claim blocks").withStyle(ChatFormatting.RED);
            confirmStack = named(new ItemStack(Items.RED_DYE), name);
        }

        shopContainer.setItem(BUY_SLOT, confirmStack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // no shift-click shortcuts into/out of the shop
    }

    @Override
    public void removed(Player player) {
        // Refund whatever coins are still sitting in the payment slot when the GUI closes.
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer) player).hasDisconnected()) {
            for (int slot : PAYMENT_SLOTS) {
                player.drop(shopContainer.removeItemNoUpdate(slot), false);
            }
        } else {
            Inventory inventory = player.getInventory();
            if (inventory.player instanceof ServerPlayer) {
                for (int slot : PAYMENT_SLOTS) {
                    inventory.placeItemBackInInventory(shopContainer.removeItemNoUpdate(slot));
                }
            }
        }
        super.removed(player);
    }

    private Collection<ItemStack> getPaymentItems() {
        return PAYMENT_SLOTS.stream()
                .map(shopContainer::getItem)
                .toList();
    }

    private void tryPurchase(Player player) {
        int paymentValue = computePaymentValue();
        int claimBlocks = computeBuyableClaimBlocks(paymentValue, player);
        if (claimBlocks <= 0) return;

        int newClaimBlocks = GriefDefenderImpl.giveClaimBlocks(player.getUUID(), claimBlocks);
        if (newClaimBlocks == -1) {
            player.sendSystemMessage(Component.literal("Failed to grant your claim blocks. Your coins have not been spent, please try again or contact staff.")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        Stuff.CB_SHOP_STORAGE.incrementCoinsSpent(player.getUUID(), paymentValue);
        for (Integer slot : PAYMENT_SLOTS) shopContainer.setItem(slot, ItemStack.EMPTY);

        MutableComponent successMessage = Component.literal("Purchased ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(format(claimBlocks)).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" claim blocks. You now have ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(format(newClaimBlocks)).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(".").withStyle(ChatFormatting.GREEN));

        player.sendSystemMessage(successMessage);
        broadcastChanges();
        player.closeContainer();
    }

    private static ItemStack named(ItemStack stack, Component name) {
        stack.set(DataComponents.CUSTOM_NAME, name.copy().withStyle(style -> style.withItalic(false)));
        return stack;
    }

    private static String format(int number) {
        return String.format("%,d", number);
    }

    private static int slotX(int index) {
        return 8 + (index % 9) * 18;
    }

    private static int slotY(int index) {
        return 18 + (index / 9) * 18;
    }

    private static int computeBuyableClaimBlocks(int paymentValue, Player player) {
        long spentCoins = Stuff.CB_SHOP_STORAGE.getCoinsSpent(player.getUUID());
        double coefficient = 536.007900577615; // 2902 scaled by 1/9th (formula was made for iron coins, lowest value coin is copper)
        return (int) (coefficient * (Math.pow(spentCoins + paymentValue, 0.7687) - Math.pow(spentCoins, 0.7687)));
    }

    private int computePaymentValue() {
        int paymentValue = 0;
        for (ItemStack item : getPaymentItems()) {
            int coinIndex = COINS.indexOf(BuiltInRegistries.ITEM.getKey(item.getItem()));
            paymentValue += (int) Math.pow(9, coinIndex) * item.getCount();
        }
        return paymentValue;
    }
}

package com.kryeit.stuff.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Redirect(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"))
    private ItemEntity dropItem(PlayerEntity player, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        ItemEntity item = player.dropItem(stack, throwRandomly, retainOwnership);
        item.setInvulnerable(true);
        item.age = -30_000; // 30 minutes
        return item;
    }
}

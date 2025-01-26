package com.kryeit.stuff.gui;

import com.kryeit.stuff.MinecraftServerSupplier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GuiUtils {
    public static ItemStack getPlayerHeadItem(String name, Text title, Text lore) {
        ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);

        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);

        if (player == null) {
            return playerHead;
        }

        NbtCompound nbt = new NbtCompound();
        nbt.putString("SkullOwner", name);

        NbtCompound displayTag = new NbtCompound();
        displayTag.putString("Name", Text.Serializer.toJson(title));

        NbtList loreList = new NbtList();
        loreList.add(NbtString.of(Text.Serializer.toJson(lore)));
        displayTag.put("Lore", loreList);

        nbt.put("display", displayTag);
        playerHead.setNbt(nbt);

        return playerHead;
    }

}

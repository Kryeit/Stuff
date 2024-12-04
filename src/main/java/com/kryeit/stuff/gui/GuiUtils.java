package com.kryeit.stuff.gui;

import com.kryeit.stuff.MinecraftServerSupplier;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.kryeit.stuff.gui.PlayersGUI.PLAYER_HEAD_ITEM;

public class GuiUtils {

    public static ItemStack getPlayerHeadItem(String name, Text title, Text lore) {
        ItemStack playerHead = new ItemStack(PLAYER_HEAD_ITEM);

        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);

        if (player == null) {
            return playerHead;
        }

        GameProfile gameProfile = new GameProfile(player.getUuid(), player.getName().getString());

        Property skinTexture = gameProfile.getProperties().get("textures").stream()
                .filter(property -> property.getName().equals("textures"))
                .findFirst()
                .orElse(null);

        if (skinTexture != null) {
            NbtCompound skullOwner = new NbtCompound();
            skullOwner.putString("Name", player.getName().getString());
            skullOwner.putUuid("Id", player.getUuid());

            // Add texture data to the skullOwner tag
            NbtCompound properties = new NbtCompound();
            NbtList texturesList = new NbtList();
            NbtCompound texture = new NbtCompound();
            texture.putString("Value", skinTexture.getValue());
            texturesList.add(texture);
            properties.put("textures", texturesList);
            skullOwner.put("Properties", properties);

            playerHead.getOrCreateNbt().put("SkullOwner", skullOwner);
        }

        playerHead.setCustomName(title);

        NbtList loreList = new NbtList();
        loreList.add(NbtString.of(Text.Serializer.toJson(lore)));
        playerHead.getOrCreateSubNbt("display").put("Lore", loreList);

        return playerHead;
    }
}

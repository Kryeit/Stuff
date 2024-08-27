package com.kryeit.stuff;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundManager {
    private static int time = 0;
    private static PlayerEntity player;

    public static void play(PlayerEntity player) {
        SoundManager.player = player;
        time = 0;
    }

    public static void tick() {
        if (player == null) return;
        switch (time) {
            case 0 ->
                    player.playSound(SoundEvent.of(new Identifier("minecraft", "block.note_block.flute")), SoundCategory.MASTER, 1, 1.19f);
            case 9 ->
                    player.playSound(SoundEvent.of(new Identifier("minecraft", "block.note_block.flute")), SoundCategory.MASTER, 1, 1.19f);
            case 18 ->
                    player.playSound(SoundEvent.of(new Identifier("minecraft", "block.note_block.flute")), SoundCategory.MASTER, 1, 1.19f);
            case 21 ->
                    player.playSound(SoundEvent.of(new Identifier("minecraft", "block.note_block.flute")), SoundCategory.MASTER, 1, 1.19f);
            case 24 ->
                    player.playSound(SoundEvent.of(new Identifier("minecraft", "block.note_block.flute")), SoundCategory.MASTER, 1, 1.5f);
        }
        time++;
    }
}

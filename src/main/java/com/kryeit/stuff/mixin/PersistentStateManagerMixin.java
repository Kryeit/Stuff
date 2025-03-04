package com.kryeit.stuff.mixin;

import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.*;

import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Map;

@Mixin(PersistentStateManager.class)
public abstract class PersistentStateManagerMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistentStateManager.class);

    @Shadow
    @Final
    private Map<String, PersistentState> loadedStates;

    @Shadow
    abstract File getFile(String id);

    /**
     * @author MrRedRhino
     * @reason Catch ConcurrentModificationException
     */
    @Overwrite
    public void save() {
        try {
            this.loadedStates.forEach((id, state) -> {
                if (state != null) {
                    state.save(this.getFile(id));
                }
            });
        } catch (ConcurrentModificationException e) {
            LOGGER.error("Error occured while saving persistent states", e);
        }
    }
}

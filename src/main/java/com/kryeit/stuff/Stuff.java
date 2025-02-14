package com.kryeit.stuff;

import com.kryeit.stuff.command.*;
import com.kryeit.stuff.compat.BluemapImpl;
import com.kryeit.stuff.config.ConfigReader;
import com.kryeit.stuff.config.StaticConfig;
import com.kryeit.stuff.listener.DragonDeath;
import com.kryeit.stuff.listener.PlayerDeath;
import com.kryeit.stuff.listener.PlayerVote;
import com.kryeit.stuff.registry.ModItems;
import com.kryeit.stuff.storage.Database;
import com.kryeit.stuff.storage.DragonKillers;
import com.kryeit.stuff.storage.MapVisibilityStorage;
import com.kryeit.stuff.ui.GuiTextures;
import com.kryeit.stuff.ui.UiResourceCreator;
import com.kryeit.votifier.model.VotifierEvent;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.fluids.transfer.FluidManipulationBehaviour;
import com.simibubi.create.infrastructure.config.AllConfigs;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class Stuff implements DedicatedServerModInitializer {
    public static final String MODID = "stuff";
    private static final Logger LOGGER = LogUtils.getLogger();
    // public static Queue queue = new Queue();
    public static MapVisibilityStorage hiddenPlayers;
    public static DragonKillers dragonKillers = new DragonKillers();

    public static final boolean DEV = FabricLoader.getInstance().isDevelopmentEnvironment();
    @SuppressWarnings("PointlessBooleanExpression")
    public static final boolean DYNAMIC_ASSETS = true && DEV;

    @Override
    public void onInitializeServer() {
        try {
            ConfigReader.readFile(Path.of("config/stuff"));
            hiddenPlayers = new MapVisibilityStorage("config/stuff/hiddenPlayers");
        } catch (IOException e) {
            LOGGER.error("Failed to load map visibility storage", e);
        }

        registerEvents();
        registerCommands();

        setupPatbox();
    }

    private void setupPatbox() {
        ModItems.register();

        UiResourceCreator.setup();
        GuiTextures.register();

        // Resource pack
        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();
    }

    public void registerEvents() {
        //     ServerPlayConnectionEvents.INIT.register(new QueueHandler(queue));
        ServerLivingEntityEvents.AFTER_DEATH.register(new PlayerDeath());
        ServerLivingEntityEvents.AFTER_DEATH.register(new DragonDeath());
        VotifierEvent.EVENT.register(new PlayerVote());

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            createModConfigs();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                if (!StaticConfig.production) return;
                Analytics.storeSessionEnd(player.getUuid());
            });

            Database.closeDataSource();
        });
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicatedServer, commandFunction) -> {
            Kofi.register(dispatcher);
            Map.register(dispatcher);
            Rules.register(dispatcher);
            SendCoords.register(dispatcher);
            TPS.register(dispatcher);
            AFK.register(dispatcher);
            ShowMe.register(dispatcher);
            HideMe.register(dispatcher);
            NetherCoords.register(dispatcher);
            Login.register(dispatcher);
            Trains.register(dispatcher);
            Shop.register(dispatcher);
            CanIGetElytra.register(dispatcher);

            ChickensAI.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (UUID id : hiddenPlayers.getPlayers()) {
                BluemapImpl.changePlayerVisibility(id, false);
            }
        });
    }

    public void createModConfigs() {
        AllConfigs.server().kinetics.maxBlocksMoved.set(6144);
        AllConfigs.server().trains.maxTrackPlacementLength.set(128);
        AllConfigs.server().schematics.maxSchematicPacketSize.set(1024);
        AllConfigs.server().schematics.schematicannonDelay.set(1);
//        AllConfigs.server().schematics.schematicannonFuelUsage.set(0.05);
//        AllConfigs.server().schematics.schematicannonGunpowderWorth.set(20.);

        AllConfigs.server().kinetics.maxDataSize.set(4000000);
        AllConfigs.server().fluids.bottomlessFluidMode.set(FluidManipulationBehaviour.BottomlessFluidMode.DENY_BY_TAG);

        AllConfigs.server().trains.trainTurningTopSpeed.set(20.);
        AllConfigs.server().trains.poweredTrainTopSpeed.set(32.);
        AllConfigs.server().trains.manualTrainSpeedModifier.set(1.);
    }
}

package com.kryeit.stuff;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kryeit.stuff.command.*;
import com.kryeit.stuff.config.StaticConfig;
import com.kryeit.stuff.listener.DragonDeath;
import com.kryeit.stuff.listener.PlayerDeath;
import com.kryeit.stuff.listener.PlayerVote;
import com.kryeit.stuff.storage.DragonKillers;
import com.kryeit.stuff.storage.ModStats;
import com.kryeit.votifier.model.VotifierEvent;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Stuff implements DedicatedServerModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Stuff.class);
    public static final GerenteClient GERENTE = new GerenteClient(Utils.readSecret("GERENTE_API_KEY"), System.getenv("GERENTE_URL"), Utils::getTPS);
    //    public static final GerenteClient GERENTE = new GerenteClient("internal", "http://localhost:8080", Utils::getTPS);
    public static DragonKillers dragonKillers = new DragonKillers();
    private static final Queue<Runnable> toRunNextTick = new ConcurrentLinkedQueue<>();
    private static final Executor asyncExecutor = Executors.newSingleThreadExecutor();
    public static Map<String, Integer> statisticModifiers;

    @Override
    public void onInitializeServer() {
        registerEvents();
        registerCommands();
        statisticModifiers = readStatisticMultiplierConfig();
        ModStats.registerStats();
    }

    private static Map<String, Integer> readStatisticMultiplierConfig() {
        File file = new File("config/stuff/statistic-multipliers.json");
        file.getParentFile().mkdirs();

        try {
            if (file.createNewFile()) return Map.of();

            try (BufferedReader reader = Files.newReader(file, StandardCharsets.UTF_8)) {
                Map<String, Integer> result = new Gson().fromJson(reader, new TypeToken<>() {
                });
                return result == null ? Map.of() : result;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load statistic-multipliers.json", e);
            return Map.of();
        }
    }

    public static <R> void runActionAsync(Supplier<R> job, Consumer<R> runOnTick) {
        asyncExecutor.execute(() -> {
            R result = job.get();
            toRunNextTick.offer(() -> runOnTick.accept(result));
        });
    }

    public static void runActionAsync(Runnable job) {
        asyncExecutor.execute(job);
    }

    public static <T> CompletableFuture<T> runAsync(Supplier<T> job) {
        return CompletableFuture.supplyAsync(job, asyncExecutor);
    }

    public void registerEvents() {
        ServerLivingEntityEvents.AFTER_DEATH.register(new PlayerDeath());
        ServerLivingEntityEvents.AFTER_DEATH.register(new DragonDeath());
        VotifierEvent.EVENT.register(new PlayerVote());

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            createModConfigs();
            Backup.createBackups();

            GERENTE.updateServerStatus(true, "Online", List.of());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((networkHandler, server) -> {
            ServerPlayerEntity player = networkHandler.getPlayer();
            GERENTE.updatePlayerStats(player.getUuid(), Utils.getStatsJson(player));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                if (!StaticConfig.enableAnalytics) return;
                Analytics.storeSessionEnd(player.getUuid());
            });

            Stuff.GERENTE.updateServerStatus(false, "Offline", List.of());
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            while (true) {
                Runnable action = toRunNextTick.poll();
                if (action == null) break;
                action.run();
            }
        });
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicatedServer, commandFunction) -> {
            Kofi.register(dispatcher);
            CommandMap.register(dispatcher);
            Rules.register(dispatcher);
            SendCoords.register(dispatcher);
            TPS.register(dispatcher);
            AFK.register(dispatcher);
            ShowMe.register(dispatcher);
            HideMe.register(dispatcher);
            NetherCoords.register(dispatcher);
            Trains.register(dispatcher);
            CanIGetElytra.register(dispatcher);
            LastSeen.register(dispatcher);
            OTP.register(dispatcher);
            Link.register(dispatcher);

            ChickensAI.register(dispatcher);
        });
    }

    public void createModConfigs() {
//        AllConfigs.server().kinetics.maxBlocksMoved.set(6144);
//        AllConfigs.server().trains.maxTrackPlacementLength.set(128);
//        AllConfigs.server().schematics.maxSchematicPacketSize.set(1024);
//        AllConfigs.server().schematics.schematicannonDelay.set(1);
////        AllConfigs.server().schematics.schematicannonFuelUsage.set(0.05);
////        AllConfigs.server().schematics.schematicannonGunpowderWorth.set(20.);
//
//        AllConfigs.server().kinetics.maxDataSize.set(4000000);
//        AllConfigs.server().fluids.bottomlessFluidMode.set(FluidManipulationBehaviour.BottomlessFluidMode.DENY_BY_TAG);
//
//        AllConfigs.server().trains.trainTurningTopSpeed.set(20.);
//        AllConfigs.server().trains.poweredTrainTopSpeed.set(32.);
//        AllConfigs.server().trains.manualTrainSpeedModifier.set(1.);
    }

    public static boolean checkPermission(UUID playerUUID, String permission) {
        User user = LuckPermsProvider.get().getUserManager().getUser(playerUUID);
        return user != null && user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}

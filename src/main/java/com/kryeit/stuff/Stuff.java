package com.kryeit.stuff;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kryeit.stuff.command.*;
import com.kryeit.stuff.config.StaticConfig;
import com.kryeit.stuff.storage.AfkTimeTracker;
import com.kryeit.stuff.storage.DragonKillers;
import com.mojang.brigadier.CommandDispatcher;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(Stuff.MODID)
public class Stuff {
    public static final String MODID = "stuff";

    private static final Logger LOGGER = LoggerFactory.getLogger(Stuff.class);
    public static final GerenteClient GERENTE = new GerenteClient(Utils.readSecret("GERENTE_API_KEY"), System.getenv("GERENTE_URL"), Utils::getTPS);
    //    public static final GerenteClient GERENTE = new GerenteClient("internal", "http://localhost:8080", Utils::getTPS);
    public static DragonKillers dragonKillers = new DragonKillers();
    private static final Queue<Runnable> toRunNextTick = new ConcurrentLinkedQueue<>();
    private static final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();
    public static Map<String, Integer> statisticModifiers;
    private final AfkTimeTracker afkTimeTracker = new AfkTimeTracker();

    public Stuff(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(this);
        // modBus.addListener(ModStats::registerStats);

        statisticModifiers = readStatisticMultiplierConfig();
    }

    @SubscribeEvent
    public void serverStarted(ServerStartedEvent event) {
        GERENTE.updateServerStatus(true, "Online", List.of());
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GERENTE.updatePlayerStats(player.getUUID(), Utils.getStatsJson(player, afkTimeTracker));
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        event.getServer().getPlayerList().getPlayers().forEach(player -> {
            if (!StaticConfig.enableAnalytics) return;
            Analytics.storeSessionEnd(player.getUUID());
        });

        Stuff.GERENTE.updateServerStatus(false, "Offline", List.of());

        Stuff.GERENTE.close();
        afkTimeTracker.close();
        asyncExecutor.close();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        while (true) {
            Runnable action = toRunNextTick.poll();
            if (action == null) break;
            action.run();
        }

        afkTimeTracker.tick(event.getServer());
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

    @SubscribeEvent
    public void onCommandRegistration(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        Kofi.register(dispatcher);
        CommandMap.register(dispatcher);
        Rules.register(dispatcher);
        SendCoords.register(dispatcher);
        TPS.register(dispatcher);
        AFK.register(dispatcher);
        ShowMe.register(dispatcher);
        HideMe.register(dispatcher);
        NetherCoords.register(dispatcher);
        CanIGetElytra.register(dispatcher);
        LastSeen.register(dispatcher);
        OTP.register(dispatcher);
        Link.register(dispatcher);
    }

    public static boolean checkPermission(UUID playerUUID, String permission) {
        User user = LuckPermsProvider.get().getUserManager().getUser(playerUUID);
        return user != null && user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}

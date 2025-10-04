package com.kryeit.stuff.command.completion;

import com.kryeit.stuff.GerenteClient;
import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.Stuff;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PlayerAutocompletion {
    public static SuggestionProvider<ServerCommandSource> suggestOnlinePlayers() {
        return (context, builder) -> suggestMatchingPlayerNames(builder, MinecraftServerSupplier.getServer().getPlayerManager().getPlayerList());
    }

    private static CompletableFuture<Suggestions> suggestMatchingPlayerNames(SuggestionsBuilder builder, Collection<ServerPlayerEntity> players) {
        return Stuff.runAsync(() -> {
            Stuff.GERENTE.searchPlayers(builder.getRemaining(), false).stream()
                    .map(GerenteClient.PlayerSearchResult::name)
                    .forEach(builder::suggest);

            return builder.build();
        });
    }
}

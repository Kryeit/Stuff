package com.kryeit.stuff.command.completion;

import com.kryeit.stuff.GerenteClient;
import com.kryeit.stuff.Stuff;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class PlayerAutocompletion {
    public static SuggestionProvider<CommandSourceStack> suggestOnlinePlayers() {
        return (context, builder) -> suggestMatchingPlayerNames(builder);
    }

    private static CompletableFuture<Suggestions> suggestMatchingPlayerNames(SuggestionsBuilder builder) {
        return Stuff.runAsync(() -> {
            Stuff.GERENTE.searchPlayers(builder.getRemaining(), false).stream()
                    .map(GerenteClient.PlayerSearchResult::name)
                    .forEach(builder::suggest);

            return builder.build();
        });
    }
}

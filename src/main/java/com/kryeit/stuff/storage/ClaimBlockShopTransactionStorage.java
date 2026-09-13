package com.kryeit.stuff.storage;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.UUID;

public class ClaimBlockShopTransactionStorage implements AutoCloseable {
    private final MVStore store = MVStore.open("mods/stuff/cb_shop_transactions.mvstore");
    private final MVMap<UUID, Long> afkTimes = store.openMap("cb_shop_transactions");

    public long getCoinsSpent(UUID player) {
        Long value = afkTimes.get(player);
        return value == null ? 0 : value;
    }

    public void incrementCoinsSpent(UUID player, long coins) {
        afkTimes.operate(player, coins, new MVMap.DecisionMaker<>() {
            @Override
            public Long selectValue(Long existingValue, Long providedValue) {
                if (existingValue == null) return providedValue;
                return existingValue + providedValue;
            }

            @Override
            public MVMap.Decision decide(Long existingValue, Long providedValue) {
                return MVMap.Decision.PUT;
            }
        });
    }

    @Override
    public void close() {
        store.close();
    }
}

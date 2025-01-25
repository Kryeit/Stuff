package com.kryeit.stuff.auth;

import com.kryeit.stuff.storage.Database;

import java.util.UUID;

public class UserApi {

    public static long getLastSeen(UUID uuid) {
        return Database.getJdbi().withHandle(handle ->
            handle.createQuery("SELECT last_seen FROM users WHERE uuid = :uuid")
                .bind("uuid", uuid.toString())
                .mapTo(Long.class)
                .findOne()
                .orElse(-1L)
        );
    }

    public static void updateLastSeen(UUID uuid) {
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("UPDATE users SET last_seen = NOW() WHERE uuid = :uuid")
                .bind("uuid", uuid.toString())
                .execute();
        });
    }
}

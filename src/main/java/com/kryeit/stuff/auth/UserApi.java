package com.kryeit.stuff.auth;

import com.kryeit.stuff.storage.Database;

import java.sql.Timestamp;
import java.util.UUID;

public class UserApi {

    public static Timestamp getLastSeen(UUID uuid) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT last_seen FROM users WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapTo(Timestamp.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public static void createUser(UUID id, String name, String stats) {
        Database.getJdbi().useHandle(h -> h.createUpdate("""
                        INSERT INTO users (uuid, username, roles, stats)
                        VALUES (:uuid, :username, '{DEFAULT}', :stats)
                        ON CONFLICT ON CONSTRAINT users_pkey DO NOTHING
                        """)
                .bind("uuid", id)
                .bind("username", name)
                .bind("stats", stats)
                .execute());
    }

    public static void updateLastSeenAndStats(UUID uuid, String stats) {
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("UPDATE users SET last_seen = NOW(), stats = :stats WHERE uuid = :uuid")
                .bind("uuid", uuid)
                .bind("stats", stats)
                .execute();
        });
    }
}

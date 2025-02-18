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

    public static void updateLastSeen(UUID uuid) {
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("UPDATE users SET last_seen = NOW() WHERE uuid = :uuid")
                .bind("uuid", uuid)
                .execute();
        });
    }

    public static void createUser(UUID id, String name) {
        Database.getJdbi().useHandle(handle -> {
            handle.createUpdate("INSERT INTO users (uuid, name, creation, roles, last_seen) VALUES (:uuid, :name, NOW(), '{DEFAULT}', NOW())")
                .bind("uuid", id)
                .bind("name", name)
                .execute();
        });
    }
}

package com.kryeit.stuff.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public record User(UUID uuid, String username, Timestamp creation, Timestamp lastSeen, List<Role> roles, JsonObject stats) {

    public enum Role {
        DEFAULT,
        KRYEITOR,
        COLLABORATOR,
        STAFF
        ;
    }
}
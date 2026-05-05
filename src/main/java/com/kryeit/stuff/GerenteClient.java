package com.kryeit.stuff;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GerenteClient implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GerenteClient.class);
    private static final Gson gson = new Gson();
    private final String internalApiKey;
    private final String baseUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<UUID, PlayerJoinInfo> playerInfos = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    private final Supplier<Float> tpsSupplier;
    private ScheduledFuture<?> nextUpdate;
    private final Map<WebSocketEventType, Set<Consumer<Object>>> webSocketListeners = new HashMap<>();

    public GerenteClient(String internalApiKey, String baseUrl, Supplier<Float> tpsSupplier) {
        this.internalApiKey = internalApiKey;
        this.baseUrl = baseUrl;
        this.tpsSupplier = tpsSupplier;
    }

    public void registerWebSocketEventListener(WebSocketEventType eventType, Consumer<Object> listener) {
        webSocketListeners.computeIfAbsent(eventType, k -> new HashSet<>()).add(listener);
    }

    public void connectToWebsocket() {
        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:8080/api/server/status/live"), new WebSocketListener())
                .thenAccept(socket -> socket.request(1));
    }

    public void updatePlayerStats(UUID playerUUID, JsonObject stats) {
        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/stats")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(stats.toString()))
                .build();

        sendRequest(request);
    }

    public PlayerJoinInfo handlePlayerJoin(UUID playerUUID, String playerName) {
        String encodedName = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/joined?name=" + encodedName)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        PlayerJoinInfo playerJoinInfo = sendRequest(request, PlayerJoinInfo.class);
        playerInfos.put(playerUUID, playerJoinInfo);
        return playerJoinInfo;
    }

    public void updateServerStatus(boolean running, String statusMessage, List<StatusPlayer> connectedPlayers) {
        if (nextUpdate != null) nextUpdate.cancel(false);
        nextUpdate = scheduler.schedule(() -> updateServerStatus(running, statusMessage, connectedPlayers), 20, TimeUnit.SECONDS);

        JsonObject body = new JsonObject();
        body.addProperty("running", running);
        body.addProperty("statusMessage", statusMessage);
        body.addProperty("tps", tpsSupplier.get());
        body.add("connectedPlayers", gson.toJsonTree(connectedPlayers));

        HttpRequest request = requestBuilder("/api/internal/server/status")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        sendRequest(request);
    }

    public byte[] getSkin(UUID playerUUID, boolean headOnly) {
        HttpRequest request = requestBuilder("/api/players/" + playerUUID + "/" + (headOnly ? "head-skin" : "skin")).build();

        return sendRequest(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    public ServerStatus getStatus() {
        HttpRequest request = requestBuilder("/api/server/status").build();

        return sendRequest(request, ServerStatus.class);
    }

    public void updatePreferences(UUID playerUUID, Map<String, Object> preferences) {
        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/preferences")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(preferences)))
                .build();

        sendRequest(request);
    }

    public int generateDiscordOTP(long discordAccountID) {
        JsonObject body = new JsonObject();
        body.addProperty("platform", "DISCORD");
        body.addProperty("userID", String.valueOf(discordAccountID));

        return generateOTP(body);
    }

    public int generateMinecraftOTP(UUID minecraftAccountID, String minecraftName) {
        JsonObject body = new JsonObject();
        body.addProperty("platform", "MINECRAFT");
        body.addProperty("userID", minecraftAccountID.toString());
        body.addProperty("userName", minecraftName);

        return generateOTP(body);
    }

    public Optional<UUID> getConnectedUser(long discordID) {
        HttpRequest request = requestBuilder("/api/internal/connected-user?id=" + discordID).build();

        JsonElement uuid = sendRequest(request, JsonObject.class).get("uuid");
        return uuid == null || uuid.isJsonNull() ? Optional.empty() : Optional.of(UUID.fromString(uuid.getAsString()));
    }

    private int generateOTP(JsonObject body) {
        HttpRequest request = requestBuilder("/api/internal/connection-code")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        JsonObject response = sendRequest(request, JsonObject.class);
        return response.get("code").getAsInt();
    }

    public List<PlayerSearchResult> searchPlayers(String query, boolean exact, boolean bannedOnly) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpRequest request = requestBuilder("/api/players?query=" + encodedQuery + "&exact=" + exact + "&bannedOnly=" + bannedOnly).build();

        return sendRequest(request, new TypeToken<>() {
        });
    }

    public List<PlayerSearchResult> searchPlayers(String query, boolean exact) {
        return searchPlayers(query, exact, false);
    }

    public void banPlayer(UUID playerUUID, String banReason, long bannedUntil) {
        JsonObject body = new JsonObject();
        body.addProperty("reason", banReason);
        body.addProperty("bannedUntil", bannedUntil);

        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/ban")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        sendRequest(request);
    }

    public void unbanPlayer(UUID playerUUID) {
        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "/unban")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        sendRequest(request);
    }

    public PlayerInfo getPlayerInfo(UUID playerUUID, Collection<StatKey> stats) {
        String statsQueryParam = stats.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        HttpRequest request = requestBuilder("/api/internal/players/" + playerUUID + "?stats=" + URLEncoder.encode(statsQueryParam, StandardCharsets.UTF_8)).build();
        return sendRequest(request, PlayerInfo.class);
    }

    public int getRank(UUID playerUUID, StatKey statistic) {
        HttpRequest request = requestBuilder("/api/players/" + playerUUID + "/rank?key=" + statistic.key() + "&namespace=" + statistic.namespace()).build();
        return sendRequest(request, JsonObject.class).get("rank").getAsInt();
    }

    public List<LeaderboardEntry> getLeaderboard(int limit, int offset, boolean sortAscending, StatKey statistic) {
        HttpRequest request = requestBuilder("/api/leaderboard?limit=" + limit + "&offset=" + offset + "&ascending=" + sortAscending + "&key=" + statistic.key() + "&namespace=" + statistic.namespace()).build();

        return sendRequest(request, new TypeToken<>() {
        });
    }

    public ConnectionResult connectDiscord(UUID minecraftUUID, int code) {
        return doConnect(String.valueOf(minecraftUUID), code);
    }

    public ConnectionResult connectMinecraft(long discordID, int code) {
        return doConnect(String.valueOf(discordID), code);
    }

    private ConnectionResult doConnect(String userId, int code) {
        HttpRequest request = requestBuilder("/api/internal/connections/connect?otp=" + code + "&userId=" + userId)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = sendAuthenticatedRequest(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return new ConnectionResult(true, "");
        } else {
            return new ConnectionResult(false, response.body());
        }
    }

    public Optional<StarboardEntry> getStarboardEntry(long messageID) {
        HttpRequest request = requestBuilder("/api/internal/starboard/entries/" + messageID).build();

        JsonObject response = sendRequest(request, JsonObject.class);
        if (response.get("exists").getAsBoolean()) {
            return Optional.of(gson.fromJson(response.get("entry"), StarboardEntry.class));
        } else {
            return Optional.empty();
        }
    }

    public void addStarboardEntry(long messageID, long starboardMessageID, long threadID, String authorName) {
        JsonObject body = new JsonObject();
        body.addProperty("originalMessage", messageID);
        body.addProperty("starboardMessage", starboardMessageID);
        body.addProperty("thread", threadID);
        body.addProperty("authorName", authorName);

        HttpRequest request = requestBuilder("/api/internal/starboard/entries")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        sendRequest(request);
    }

    public void setStarboardEntryReactionCount(long messageID, int newCount) {
        HttpRequest request = requestBuilder("/api/internal/starboard/entries/" + messageID + "/count?newCount=" + newCount)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        sendRequest(request);
    }

    public void markStarboardEntryAsUpdated(long messageID, int count) {
        HttpRequest request = requestBuilder("/api/internal/starboard/entries/" + messageID + "/updated?count=" + count)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        sendRequest(request);
    }

    public List<StarboardEntry> getStarboardEntriesToUpdate() {
        HttpRequest request = requestBuilder("/api/internal/starboard/entries/updatable").build();

        return sendRequest(request, new TypeToken<>() {
        });
    }

    public Optional<PlayerJoinInfo> getCachedJoinInfo(UUID playerUUID) {
        return Optional.ofNullable(playerInfos.get(playerUUID));
    }

    public List<ListPlayersEntry> listPlayers(int limit, int offset, boolean onlyDiscordConnected) {
        HttpRequest request = requestBuilder("/api/internal/players?limit=" + limit + "&offset=" + offset + "&onlyConnected=" + onlyDiscordConnected).build();

        return sendRequest(request, new TypeToken<>() {
        });
    }

    public List<RoleDefinition> getRoleDefinitions() {
        HttpRequest request = requestBuilder("/api/internal/roles/definitions").build();

        return sendRequest(request, new TypeToken<>() {
        });
    }

    private HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path));
    }

    private <T> T sendRequest(HttpRequest request, Class<T> clazz) {
        String body = sendRequest(request, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(body, clazz);
    }

    private <T> T sendRequest(HttpRequest request, TypeToken<T> typeToken) {
        String body = sendRequest(request, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(body, typeToken);
    }

    private void sendRequest(HttpRequest request) {
        try {
            sendRequest(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            LOGGER.error("Failed to send request {}", request, e);
        }
    }

    private <T> HttpResponse<T> sendAuthenticatedRequest(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest authorizedRequest = HttpRequest.newBuilder(request, (h, v) -> true)
                .header("Authorization", internalApiKey)
                .build();

        try {
            return httpClient.send(authorizedRequest, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T sendRequest(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) {
        HttpResponse<T> response = sendAuthenticatedRequest(request, bodyHandler);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new InvalidResponseCodeException("Gerente HTTP request failed: HTTP error code: " + response.statusCode() + ", body: " + response.body());
        }
        return response.body();
    }

    @Override
    public void close() {
        scheduler.close();
    }

    public record ConnectionResult(boolean success, String message) {

    }

    public record ListPlayersEntry(UUID uuid, String discordId, String minecraftName, List<Role> roles) {
    }

    public record RoleDefinition(String id, String discordId, String prefix, String name) {
    }

    public record StatusPlayer(boolean afk, UUID uuid, String name) {
    }

    public record PlayerJoinInfo(boolean firstJoin, Timestamp bannedUntil, String banReason, List<String> badges,
                                 boolean muted, JsonObject preferences, List<Role> roles) {
    }

    public record PlayerSearchResult(String name, UUID uuid) {
    }

    public record ServerStatus(boolean running, String statusMessage, float tps, List<StatusPlayer> connectedPlayers) {
    }

    public record PlayerInfo(String minecraftName, String discordId, Long lastSeen, boolean connected, Long bannedUntil,
                             String banReason,
                             String mutedUntil, JsonObject preferences, Map<String, Integer> selectedStats,
                             List<Role> roles) {
    }

    public record StatKey(String namespace, String key) {
        @Override
        public @NotNull String toString() {
            return namespace + "/" + key;
        }
    }

    public record LeaderboardEntry(String name, UUID uuid, int value, String formattedValue) {
    }

    private static class InvalidResponseCodeException extends RuntimeException {
        public InvalidResponseCodeException(String message) {
            super(message);
        }
    }

    public record Role(String name, String id, String discordId, String prefix) {
    }

    public record StarboardEntry(long originalMessage, long starboardMessage, long thread, int count,
                                 String authorName) {
    }

    private class WebSocketListener implements WebSocket.Listener {
        private final StringBuilder partialMessage = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partialMessage.append(data);
            if (last) {
                JsonObject eventBody = gson.fromJson(partialMessage.toString(), JsonObject.class);
                ServerStatus eventData = gson.fromJson(eventBody, ServerStatus.class);

                Set<Consumer<Object>> listeners = webSocketListeners.get(WebSocketEventType.SERVER_STATUS_UPDATE);
                if (listeners != null) {
                    listeners.forEach(l -> l.accept(eventData));
                }

                partialMessage.setLength(0);
            }

            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            LOGGER.info("Gerente websocket connected");
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            LOGGER.error("Gerente websocket error", error);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            LOGGER.error("Gerente websocket connection closed, reconnecting... {} - {}", statusCode, reason);
            scheduler.schedule(GerenteClient.this::connectToWebsocket, 5, TimeUnit.SECONDS);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
    }

    public enum WebSocketEventType {
        SERVER_STATUS_UPDATE(ServerStatus.class);

        private final Class<?> dataType;

        WebSocketEventType(Class<?> dataType) {
            this.dataType = dataType;
        }

        public <T> T getDataType() {
            return (T) dataType;
        }
    }
}

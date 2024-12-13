package com.kryeit.stuff.command;

import com.google.gson.Gson;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Login {

    private static final String LOGIN_API_URL = "https://kryeit.com/api/login/link";
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static class LoginPayload {
        String username;
        String uuid;
        String authApiSecret;

        LoginPayload(String username, String uuid, String authApiSecret) {
            this.username = username;
            this.uuid = uuid;
            this.authApiSecret = authApiSecret;
        }
    }

    private static class LoginResponse {
        String link;
    }

    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendFeedback(() -> Text.of("Can't execute from console"), false);
            return 0;
        }

        try {
            LoginPayload payload = new LoginPayload(
                    player.getName().getString(),
                    player.getUuidAsString(),
                    getAuthSecret()
            );

            String jsonPayload = gson.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LOGIN_API_URL))
                    .header("Content-Type", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                LoginResponse loginResponse = gson.fromJson(response.body(), LoginResponse.class);
                player.sendMessage(Text.literal("Login link: " + loginResponse.link)
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, loginResponse.link))), false);
            } else {
                player.sendMessage(Text.of("Failed to send login link. Status: " + response.statusCode()), false);
            }

        } catch (IOException | InterruptedException e) {
            player.sendMessage(Text.of("An error occurred: " + e.getMessage()), false);
            Thread.currentThread().interrupt();
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("login")
                .executes(Login::execute)
        );
    }

    private static String getAuthSecret() {
        try (InputStream inputStream = Login.class.getResourceAsStream("/auth_secret.txt")) {
            return new String(inputStream.readAllBytes()).trim();
        } catch (IOException e) {
            throw new RuntimeException("Could not read auth secret", e);
        }
    }
}
package com.kryeit.stuff.command;

import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.command.completion.PlayerAutocompletion;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class Trains {
    public static int execute(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();

        if (source.getPlayer() == null) {
            Supplier<Text> message = () -> Text.of("Can't execute from console");
            source.sendFeedback(message, false);
            return 0;
        }

        GameProfile profile = MinecraftServerSupplier.getServer().getUserCache().findByName(name).orElse(null);
        if (profile == null) {
            Supplier<Text> message = () -> Text.of("Player not found");
            source.sendFeedback(message, false);
            return 0;
        }

        UUID playerUuid = profile.getId();

        List<Train> trains = new ArrayList<>();

        for (Train train : Create.RAILWAYS.trains.values()) {
            if (train.owner.equals(playerUuid)) {
                trains.add(train);
            }
        }

        if (trains.isEmpty()) {
            source.getPlayer().sendMessage(Text.of("No trains found"));
            return 0;
        }

        source.getPlayer().sendMessage(Text.of("Found " + trains.size() + " trains:"));

        for (Train train : trains) {
            if (train == null) continue;
            Vec3i trainPosition = getTrainPosition(train);
            source.getPlayer().sendMessage(Text.of("Train " + train.name.getString() + " at " + trainPosition)
                    .copy().setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + source.getPlayer().getName().getString() + " " + trainPosition.getX() + " " + trainPosition.getY() + " " + trainPosition.getZ()))));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("trains")
                        .requires(Permissions.require("group.staff"))
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(PlayerAutocompletion.suggestOnlinePlayers())
                                .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                        )
        );
    }

    public static Vec3i getTrainPosition(Train train) {
        Vec3i position = Vec3i.ZERO;
        int count = 0;

        for (Carriage carriage : train.carriages) {
            Vec3d anchorPosition = carriage.bogeys.get(true).getAnchorPosition();
            Vec3i carriagePosition = new Vec3i((int) anchorPosition.x, (int) anchorPosition.y, (int) anchorPosition.z);
            position = position.add(carriagePosition);
            count++;
        }

        if (count > 0) {
            position = new Vec3i(position.getX() / count, position.getY() / count, position.getZ() / count); // Average position
        }

        return position;
    }
}
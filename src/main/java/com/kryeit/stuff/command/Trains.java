package com.kryeit.stuff.command;

import com.kryeit.stuff.GerenteClient;
import com.kryeit.stuff.Stuff;
import com.kryeit.stuff.Utils;
import com.kryeit.stuff.command.completion.PlayerAutocompletion;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class Trains {
    public static int execute(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        Stuff.runActionAsync(() -> {
            List<GerenteClient.PlayerSearchResult> searchResults = Stuff.GERENTE.searchPlayers(name, true);
            GerenteClient.PlayerSearchResult target = searchResults.isEmpty() ? null : searchResults.get(0);

            if (target == null) return new Feedback("Player not found", 0);

            List<Train> trains = new ArrayList<>();

            for (Train train : Create.RAILWAYS.trains.values()) {
                if (train.owner.equals(target.uuid())) {
                    trains.add(train);
                }
            }

            if (trains.isEmpty()) return new Feedback("No trains found", 0);

            MutableText result = Text.literal("Found " + trains.size() + " trains:");

            for (Train train : trains) {
                if (train == null) continue;
                Vec3i trainPosition = getTrainPosition(train);
                ClickEvent clickEvent;

                if (isStaff(source)) {
                    clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getName().getString() + " " + trainPosition.getX() + " " + trainPosition.getY() + " " + trainPosition.getZ());
                } else {
                    clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, Utils.getMapLink(trainPosition));
                }

                result.append(Text.of("- ")
                        .copy()
                        .append(Text.of(train.name.getString())
                                .copy()
                                .setStyle(Style.EMPTY.withColor(Formatting.AQUA)))
                        .append(Text.of(" at "))
                        .append(Text.of(getFormattedCoords(trainPosition))
                                .copy()
                                .setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                        .setStyle(Style.EMPTY.withClickEvent(clickEvent)
                        ));

            }
            return new Feedback(result, 1);
        }, f -> source.sendFeedback(f::message, false));

        return Command.SINGLE_SUCCESS;
    }

    public static String getFormattedCoords(Vec3i position) {
        return "(" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")";
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("trains")
                .executes(context -> execute(context, context.getSource().getPlayer().getName().getString()))
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .requires(Trains::isStaff)
                        .suggests(PlayerAutocompletion.suggestOnlinePlayers())
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                )
        );
    }

    private static boolean isStaff(ServerCommandSource source) {
        return Stuff.checkPermission(source.getPlayer().getUuid(), "group.staff");
    }

    public static Vec3i getTrainPosition(Train train) {
        Vec3i position = Vec3i.ZERO;
        int count = 0;

        for (Carriage carriage : train.carriages) {
            Vec3d anchorPosition = carriage.bogeys.get(true).getAnchorPosition();
            if (anchorPosition == null) continue;
            Vec3i carriagePosition = new Vec3i((int) anchorPosition.x, (int) anchorPosition.y, (int) anchorPosition.z);
            position = position.add(carriagePosition);
            count++;
        }

        if (count > 0) {
            position = new Vec3i(position.getX() / count, position.getY() / count, position.getZ() / count); // Average position
        }

        return position;
    }

    private record Feedback(MutableText message, int code) {
        public Feedback(String message, int code) {
            this(Text.literal(message), code);
        }
    }
}

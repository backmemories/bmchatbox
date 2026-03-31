package net.bmcb.chat;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.bmcb.network.FlavorPackets;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class FlavorCommand {

    private static final List<String> SABORES = List.of(
            "demo", "menta_melina", "dia_nublado", "fresa_francesa", "vainilla_vanilla", "bonito_morado"
    );

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("bmchat")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(CommandManager.literal("sabor")

                                    .then(CommandManager.literal("@a")
                                            .then(CommandManager.argument("sabor", StringArgumentType.word())
                                                    .suggests((context, builder) -> {
                                                        SABORES.forEach(builder::suggest);
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(context -> {
                                                        String sabor = StringArgumentType.getString(context, "sabor");
                                                        if (!SABORES.contains(sabor)) {
                                                            context.getSource().sendFeedback(
                                                                    () -> Text.literal("§cSabor inválido. Opciones: " + String.join(", ", SABORES)), false);
                                                            return 0;
                                                        }
                                                        context.getSource().getServer().getPlayerManager().getPlayerList().forEach(p -> {
                                                            String nombre = p.getGameProfile().name();
                                                            ServerPlayNetworking.send(p, new FlavorPackets.SyncFlavorPayload(nombre, sabor));
                                                        });
                                                        context.getSource().sendFeedback(
                                                                () -> Text.literal("§aSabor de todos cambiado a " + sabor), true);
                                                        return 1;
                                                    })
                                            )
                                    )

                                    .then(CommandManager.argument("jugador", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                context.getSource().getServer().getPlayerManager().getPlayerList()
                                                        .forEach(p -> builder.suggest(p.getGameProfile().name()));
                                                return builder.buildFuture();
                                            })
                                            .then(CommandManager.argument("sabor", StringArgumentType.word())
                                                    .suggests((context, builder) -> {
                                                        SABORES.forEach(builder::suggest);
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(context -> {
                                                        String jugador = StringArgumentType.getString(context, "jugador");
                                                        String sabor   = StringArgumentType.getString(context, "sabor");
                                                        if (!SABORES.contains(sabor)) {
                                                            context.getSource().sendFeedback(
                                                                    () -> Text.literal("§cSabor inválido. Opciones: " + String.join(", ", SABORES)), false);
                                                            return 0;
                                                        }
                                                        ServerPlayerEntity player = context.getSource().getServer()
                                                                .getPlayerManager().getPlayer(jugador);
                                                        if (player == null) {
                                                            context.getSource().sendFeedback(
                                                                    () -> Text.literal("§cJugador no encontrado: " + jugador), false);
                                                            return 0;
                                                        }
                                                        ServerPlayNetworking.send(player, new FlavorPackets.SyncFlavorPayload(jugador, sabor));
                                                        context.getSource().sendFeedback(
                                                                () -> Text.literal("§aSabor de " + jugador + " cambiado a " + sabor), true);
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
            );
        });
    }
}
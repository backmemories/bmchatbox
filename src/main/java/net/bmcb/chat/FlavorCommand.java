package net.bmcb.chat;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.bmcb.network.FlavorPackets;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;


import java.util.List;

public class FlavorCommand {

    private static final List<String> SABORES = List.of(
            "demo", "menta_melina", "dia_nublado", "fresa_francesa", "vainilla_vanilla", "bonito_morado"
    );

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("bmchat")
                            .then(ClientCommandManager.literal("sabor")

                                    // --- @a ---
                                    .then(ClientCommandManager.literal("@a")
                                            .then(ClientCommandManager.argument("sabor", StringArgumentType.word())
                                                    .suggests((context, builder) -> {
                                                        SABORES.forEach(builder::suggest);
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(context -> {
                                                        String sabor = StringArgumentType.getString(context, "sabor");
                                                        if (!SABORES.contains(sabor)) {
                                                            context.getSource().sendFeedback(Text.literal("§cSabor inválido. Opciones: " + String.join(", ", SABORES)));
                                                            return 0;
                                                        }
                                                        MinecraftClient client = MinecraftClient.getInstance();
                                                        if (client.getNetworkHandler() != null) {
                                                            client.getNetworkHandler().getPlayerList().forEach(p -> {
                                                                String nombre = p.getProfile().name();
                                                                ClientPlayNetworking.send(new FlavorPackets.SetFlavorPayload(nombre, sabor));
                                                            });
                                                        }
                                                        context.getSource().sendFeedback(Text.literal("Sabor de todos cambiado a " + sabor));
                                                        return 1;
                                                    })
                                            )
                                    )

                                    // --- @p ---
                                    .then(ClientCommandManager.literal("@p")
                                            .then(ClientCommandManager.argument("sabor", StringArgumentType.word())
                                                    .suggests((context, builder) -> {
                                                        SABORES.forEach(builder::suggest);
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(context -> {
                                                        String sabor = StringArgumentType.getString(context, "sabor");
                                                        if (!SABORES.contains(sabor)) {
                                                            context.getSource().sendFeedback(Text.literal("§cSabor inválido. Opciones: " + String.join(", ", SABORES)));
                                                            return 0;
                                                        }
                                                        MinecraftClient client = MinecraftClient.getInstance();
                                                        if (client.player == null) return 0;
                                                        String miNombre = client.player.getGameProfile().name();
                                                        ClientPlayNetworking.send(new FlavorPackets.SetFlavorPayload(miNombre, sabor));
                                                        context.getSource().sendFeedback(Text.literal("Sabor propio cambiado a " + sabor));
                                                        return 1;
                                                    })
                                            )
                                    )

                                    // --- jugador por nombre ---
                                    .then(ClientCommandManager.argument("jugador", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                MinecraftClient client = MinecraftClient.getInstance();
                                                if (client.getNetworkHandler() != null) {
                                                    client.getNetworkHandler().getPlayerList().forEach(p ->
                                                            builder.suggest(p.getProfile().name())
                                                    );
                                                }
                                                return builder.buildFuture();
                                            })
                                            .then(ClientCommandManager.argument("sabor", StringArgumentType.word())
                                                    .suggests((context, builder) -> {
                                                        SABORES.forEach(builder::suggest);
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(context -> {
                                                        String jugador = StringArgumentType.getString(context, "jugador");
                                                        String sabor   = StringArgumentType.getString(context, "sabor");
                                                        if (!SABORES.contains(sabor)) {
                                                            context.getSource().sendFeedback(Text.literal("§cSabor inválido. Opciones: " + String.join(", ", SABORES)));
                                                            return 0;
                                                        }
                                                        MinecraftClient client = MinecraftClient.getInstance();
                                                        if (client.player == null) return 0;
                                                        ClientPlayNetworking.send(new FlavorPackets.SetFlavorPayload(jugador, sabor));
                                                        context.getSource().sendFeedback(Text.literal("Sabor de " + jugador + " cambiado a " + sabor));
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
            );
        });
    }
}
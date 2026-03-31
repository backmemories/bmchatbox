package net.bmcb.network;

import net.bmcb.chat.FlavorManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class FlavorPackets {

    // -----------------------------------------------
    // 📦 Packet Cliente → Servidor: pedir cambio
    // -----------------------------------------------
    public record SetFlavorPayload(String playerName, String flavor)
            implements CustomPayload {

        public static final CustomPayload.Id<SetFlavorPayload> ID =
                new CustomPayload.Id<>(Identifier.of("bmchatbox", "set_flavor"));

        public static final PacketCodec<PacketByteBuf, SetFlavorPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING, SetFlavorPayload::playerName,
                        PacketCodecs.STRING, SetFlavorPayload::flavor,
                        SetFlavorPayload::new
                );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // -----------------------------------------------
    // 📦 Packet Servidor → Cliente: sincronizar
    // -----------------------------------------------
    public record SyncFlavorPayload(String playerName, String flavor)
            implements CustomPayload {

        public static final CustomPayload.Id<SyncFlavorPayload> ID =
                new CustomPayload.Id<>(Identifier.of("bmchatbox", "sync_flavor"));

        public static final PacketCodec<PacketByteBuf, SyncFlavorPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING, SyncFlavorPayload::playerName,
                        PacketCodecs.STRING, SyncFlavorPayload::flavor,
                        SyncFlavorPayload::new
                );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // -----------------------------------------------
    // 🔧 Registro (llamar desde el servidor)
    // -----------------------------------------------
    public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(SetFlavorPayload.ID, SetFlavorPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncFlavorPayload.ID, SyncFlavorPayload.CODEC);

        // Cuando el servidor recibe un pedido de cambio de sabor
        ServerPlayNetworking.registerGlobalReceiver(SetFlavorPayload.ID, (payload, context) -> {
            String playerName = payload.playerName();
            String flavor = payload.flavor();

            context.server().execute(() -> {
                // Guardar en el servidor
                FlavorManager.setFlavor(playerName, flavor);

                // Sincronizar a todos los clientes
                SyncFlavorPayload sync = new SyncFlavorPayload(playerName, flavor);
                context.server().getPlayerManager().getPlayerList().forEach(p ->
                        ServerPlayNetworking.send(p, sync)
                );
            });
        });
    }
}
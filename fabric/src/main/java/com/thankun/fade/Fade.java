package com.thankun.fade;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Fade implements ModInitializer {
    public static final String MOD_ID = "fade";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static final CustomPacketPayload.Type<FadeSyncPayload> FADE_SYNC_TYPE = 
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sync"));
    private static final Set<UUID> FADE_MOD_USERS = new HashSet<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(FADE_SYNC_TYPE, FadeSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FADE_SYNC_TYPE, FadeSyncPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FADE_SYNC_TYPE, (payload, context) -> {
            if (payload.clientHasMod()) {
                context.server().execute(() -> {
                    ServerPlayer player = context.player();
                    FADE_MOD_USERS.add(player.getUUID());
                    LOGGER.info("(미완)[Fade Server] {} 유저는 Fade 클라 장착 확인됨. FADE 기능 활성화!", player.getScoreboardName());
                });
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (FADE_MOD_USERS.remove(player.getUUID())) {
                LOGGER.info("(미완)[Fade Server] {} 유저 퇴장. 명단에서 제거됨.", player.getScoreboardName());
            }
        });

        LOGGER.info("[Fade] 공통 모드 및 패킷 네트워크 등록 완료.");
    }

    public static boolean isFadeUser(ServerPlayer player) {
        return FADE_MOD_USERS.contains(player.getUUID());
    }
    public record FadeSyncPayload(boolean clientHasMod) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, FadeSyncPayload> CODEC = StreamCodec.of(
            (buf, val) -> buf.writeBoolean(val.clientHasMod()), 
            buf -> new FadeSyncPayload(buf.readBoolean())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return FADE_SYNC_TYPE;
        }
    }
}
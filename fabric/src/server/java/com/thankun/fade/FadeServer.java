package com.thankun.fade;

import com.thankun.fade.server.command.FadeCommand;
import com.thankun.fade.server.config.FadeServerConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FadeServer implements DedicatedServerModInitializer {
    
    private static final Set<UUID> FADE_USERS = new HashSet<>();
    public static boolean isFadeClientUser(ServerPlayer player) {
        if (player == null) return false;
        return FADE_USERS.contains(player.getUUID());
    }

    @Override
    public void onInitializeServer() {
        FadeServerConfig.load();
        
        Fade.LOGGER.info("[Fade] Never gonna give you up, never gonna let you down, never gonna run around and desert you.");
        
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            FadeServerConfig.currentServer = server;
            FadeServerConfig.load();
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            FadeServerConfig.currentServer = null;
            FADE_USERS.clear();
        });
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> 
            FadeCommand.register(dispatcher)
        );
        ServerPlayNetworking.registerGlobalReceiver(Fade.FADE_SYNC_TYPE, (payload, context) -> {
            if (payload.clientHasMod()) {
                ServerPlayer player = context.player();
                if (player != null) {
                    FADE_USERS.add(player.getUUID());
                    Fade.LOGGER.info("[Fade Server] 플레이어 {} 님이 Fade 클라이언트 모드 작동을 확인했습니다.", player.getName().getString());
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (player != null) {
                FADE_USERS.remove(player.getUUID());
                Fade.LOGGER.info("[Fade Server] 플레이어 {} 님이 퇴장하여 Fade 클라이언트 명단에서 제거되었습니다.", player.getName().getString());
            }
        });
    }
}
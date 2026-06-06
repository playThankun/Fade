package com.thankun.fade.client;

import com.thankun.fade.Fade;
import com.thankun.fade.client.config.FadeConfig;
import com.thankun.fade.client.gui.FadeConfigScreen;
import com.thankun.fade.client.mixin.ScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class FadeClient implements ClientModInitializer {
    public static final KeyMapping.Category FADE_CATEGORY = 
            KeyMapping.Category.register(Identifier.parse("fade:general"));

    public static KeyMapping openConfigKey;

    public static boolean isFadeServerConnected = false;

    @Override
    public void onInitializeClient() {
        // 1. 키 바인딩 등록
        openConfigKey = new KeyMapping(
                "key.fade.open_config",
                GLFW.GLFW_KEY_Z,
                FADE_CATEGORY
        );
        KeyMappingHelper.registerKeyMapping(openConfigKey);
        
        // 2. 클라이언트 틱 이벤트
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfigKey.consumeClick()) {
                if (client.player != null) {
                    client.setScreen(new FadeConfigScreen(client.screen, client.options));
                }
            }
        });

        // 3. 서버 조인 이벤트
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ClientPlayNetworking.canSend(Fade.FADE_SYNC_TYPE)) {
                ClientPlayNetworking.send(new Fade.FadeSyncPayload(true));
                
                // 🚀 내부 변수 값 변경
                isFadeServerConnected = true;
                Fade.LOGGER.info("[Fade Client] 서버에서 Fade 프로토콜을 감지했습니다. 클라이언트 최적화 커스텀 GUI 활성화!");
            } else {
                // 🚀 일반 바닐라 서버일 때
                isFadeServerConnected = false;
                Fade.LOGGER.info("[Fade Client] 바닐라 서버 접속 감지: 클라이언트 렌더링 변조를 휴면 상태로 전환합니다.");
            }
        });

        // 서버 퇴장 이벤트
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            isFadeServerConnected = false;
        });

        // 4. 비디오 설정 화면 버튼 배치
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof VideoSettingsScreen videoScreen) {
                Button button = Button.builder(
                    Component.literal("Fade"),
                    btn -> client.setScreen(new FadeConfigScreen(videoScreen, client.options))
                )
                .bounds(10, 10, 80, 20)
                .build();

                ScreenAccessor accessor = (ScreenAccessor) screen;
                accessor.fade$addRenderableOnly(button);
                accessor.fade$addWidget(button);
            }
        });
    }
}
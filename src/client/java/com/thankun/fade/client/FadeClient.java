package com.thankun.fade.client;

import com.thankun.fade.client.config.FadeConfig;
import com.thankun.fade.client.gui.FadeConfigScreen;
import com.thankun.fade.client.mixin.ScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;

public class FadeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        FadeConfig.load();

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
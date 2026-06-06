package com.thankun.fade.client.config;

import com.thankun.fade.client.gui.FadeConfigScreen;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FadeSodiumConfig implements ConfigEntryPoint {

    @Override
    public void registerConfigLate(ConfigBuilder builder) {

       builder.registerOwnModOptions()

        .setIcon(Identifier.parse("fade:icon.png"))

        .addPage(
                builder.createExternalPage()

                        .setName(Component.literal("Fade"))

                        .setScreenConsumer(parent -> {
                        Minecraft.getInstance().setScreen(
                        new FadeConfigScreen(parent, Minecraft.getInstance().options)
                 );
      }
                        )
        );
    }
}
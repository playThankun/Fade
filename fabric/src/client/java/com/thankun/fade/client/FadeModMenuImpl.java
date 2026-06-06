package com.thankun.fade.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.thankun.fade.client.gui.FadeConfigScreen;
import net.minecraft.client.Minecraft;

public class FadeModMenuImpl implements ModMenuApi {
    public FadeModMenuImpl() {
        System.out.println("######################################");
        System.out.println("FADE MODMENU ATTACHED SUCCESSFULLY!");
        System.out.println("######################################");
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new FadeConfigScreen(parent, Minecraft.getInstance().options);
    }
}
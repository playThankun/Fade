package com.thankun.fade.client.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {

    @Invoker("addRenderableOnly")
    <T extends Renderable> T fade$addRenderableOnly(T renderable);

    @Invoker("addWidget")
    GuiEventListener fade$addWidget(GuiEventListener widget);
}
package com.thankun.fade.client.gui;

import com.mojang.serialization.Codec;
import com.thankun.fade.client.config.FadeConfig;
import com.thankun.fade.client.util.FadeFilterUtil;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import java.util.Arrays;

public class FadeConfigScreen extends OptionsSubScreen {
    public static final OptionInstance<Boolean> CULL_FALLING_BLOCKS = OptionInstance.createBoolean(
        "options.fade.cull_falling_blocks",
        OptionInstance.noTooltip(),
        false,
        value -> {
            FadeConfig.cullFallingBlocks = value;
            FadeConfig.save();
        }
    );

    public static final OptionInstance<Boolean> FADE_ITEM_AS_VANISH = OptionInstance.createBoolean(
        "options.fade.fade_item_as_vanish",
        option -> Tooltip.create(Component.translatableWithFallback("options.fade.fade_item_as_vanish.tooltip", "If enabled, items will vanish when culled by FADE. If disabled, items remain visible.")),
        true,
        value -> {
            FadeConfig.fadeItemAsVanish = value;
            FadeConfig.save();
            if (net.minecraft.client.Minecraft.getInstance().screen instanceof FadeConfigScreen currentScreen) {
                currentScreen.updateBoxActivation();
            }
        }
    );

    public static final OptionInstance<OperationMode> OPERATION_MODE = new OptionInstance<>(
        "options.fade.operation_mode",
        option -> net.minecraft.client.gui.components.Tooltip.create(Component.translatable("options.fade.operation_mode.tooltip")),
        (caption, value) -> Component.translatableWithFallback("options.fade.operation_mode." + value.name().toLowerCase(), value.fallbackName),
        new OptionInstance.Enum<>(Arrays.asList(OperationMode.values()), Codec.INT.xmap(
            i -> OperationMode.values()[Math.max(0, Math.min(i, OperationMode.values().length - 1))], 
            v -> v.ordinal()
        )),
        OperationMode.FADE_OUT,
        value -> FadeConfig.save()
    );

    public static final OptionInstance<Integer> PLAYER_CULL_DISTANCE = new OptionInstance<>(
        "options.fade.player_cull_distance",
        OptionInstance.noTooltip(),
        (caption, value) -> {
            Component label = Component.translatableWithFallback("options.fade.player_cull_distance", "Cull Distance");
            return Component.translatable("options.generic_value", label, Component.literal(value + " Blocks"));
        },
        new OptionInstance.IntRange(0, 256),
        64,
        value -> FadeConfig.save()
    );

    public static final OptionInstance<CullTarget> CULL_TARGET = new OptionInstance<>(
        "options.fade.cull_target",
        OptionInstance.noTooltip(),
        (caption, value) -> Component.translatableWithFallback("options.fade.cull_target." + value.name().toLowerCase(), value.fallbackName),
        new OptionInstance.Enum<>(Arrays.asList(CullTarget.values()), Codec.INT.xmap(
            i -> CullTarget.values()[Math.max(0, Math.min(i, CullTarget.values().length - 1))], 
            v -> v.ordinal()
        )),
        CullTarget.PLAYERS_ONLY,
        value -> {
            FadeConfig.save();
            if (net.minecraft.client.Minecraft.getInstance().screen instanceof FadeConfigScreen currentScreen) {
                currentScreen.updateBoxActivation();
            }
        }
    );

    public static final OptionInstance<CullMode> CULL_MODE = new OptionInstance<>(
        "options.fade.cull_mode",
        OptionInstance.noTooltip(),
        (caption, value) -> Component.translatableWithFallback("options.fade.cull_mode." + value.name().toLowerCase(), value.fallbackName),
        new OptionInstance.Enum<>(Arrays.asList(CullMode.values()), Codec.INT.xmap(
            i -> CullMode.values()[Math.max(0, Math.min(i, CullMode.values().length - 1))], 
            v -> v.ordinal()
        )),
        CullMode.VANISH,
        value -> {
            FadeConfig.save();
            if (net.minecraft.client.Minecraft.getInstance().screen instanceof FadeConfigScreen currentScreen) {
                currentScreen.updateBoxActivation();
            }
        }
    );

    private EditBox customFilterBox;

    public FadeConfigScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, Component.translatableWithFallback("fade.mod.setting", "Fade Mod Settings"));
    }

    @Override
    protected void addOptions() {
        if (this.list != null) {
            this.list.addSmall(OPERATION_MODE, CULL_MODE);
            this.list.addSmall(PLAYER_CULL_DISTANCE, CULL_TARGET);
            this.list.addSmall(CULL_FALLING_BLOCKS, FADE_ITEM_AS_VANISH);
        }
    }

    @Override
    protected void init() {
        FadeConfig.load(); 
        FADE_ITEM_AS_VANISH.set(FadeConfig.fadeItemAsVanish);
        
        this.addTitle();
        this.addContents(); 
        
        if (this.minecraft != null) {
            this.customFilterBox = new EditBox(
                this.minecraft.font, 
                this.width / 2 - 150, 0, 300, 20, 
                Component.literal("Custom Targets")
            );
            this.customFilterBox.setHint(Component.translatable("options.fade.custom_targets.placeholder"));
            this.customFilterBox.setTooltip(Tooltip.create(Component.translatable("options.fade.custom_targets.tooltip")));
            this.customFilterBox.setMaxLength(2048);

            this.customFilterBox.setValue(FadeConfig.customFilterString);
            this.customFilterBox.setResponder(value -> {
                Thread.ofVirtual().start(() -> {
                    FadeConfig.customFilterString = value;
                    FadeConfig.save();
                });
            });

            this.updateBoxActivation();
            this.addWidget(this.customFilterBox);
        }

        this.addFooter(); 
        
        if (this.customFilterBox != null) {
            this.addRenderableWidget(this.customFilterBox);
        }

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements(); 
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        if (this.customFilterBox != null) {
            this.customFilterBox.setX(this.width / 2 - 150);
            this.customFilterBox.setY(this.height - 65); 
        }
    }

    public void updateBoxActivation() {
        if (this.customFilterBox != null) {
            boolean isCustomMode = CULL_TARGET.get() == CullTarget.CUSTOM;
            this.customFilterBox.setEditable(isCustomMode);
            this.customFilterBox.active = isCustomMode;
            
            if (!isCustomMode && this.customFilterBox.isFocused()) {
                this.customFilterBox.setFocused(false);
            }
        }
    }

    @Override
    public void onClose() {
        if (this.customFilterBox != null) {
            FadeFilterUtil.rebuildFilterCache(this.customFilterBox.getValue());
        }
        super.onClose();
    }

    public enum OperationMode {
        FADE_OUT("Normal (Fade Close)"), OPTIMIZE("Optimize (Cull Far)");
        public final String fallbackName;
        OperationMode(String fallbackName) { this.fallbackName = fallbackName; }
    }

    public enum CullTarget {
        PLAYERS_ONLY("Players Only"), ENTITIES_ONLY("Entities Only"), ALL("All Entities"), CUSTOM("Custom Filter (Advanced)");
        public final String fallbackName;
        CullTarget(String fallbackName) { this.fallbackName = fallbackName; }
    }

    public enum CullMode {
        VANISH("Vanish (0%)"), FADE("Fade (Semi-Trans)"), OFF("Off (100%)");
        public final String fallbackName;
        CullMode(String fallbackName) { this.fallbackName = fallbackName; }
    }
}
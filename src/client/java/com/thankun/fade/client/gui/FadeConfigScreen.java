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

    public static final OptionInstance<Double> PLAYER_CULL_DISTANCE = new OptionInstance<>(
        "options.fade.player_cull_distance",
        OptionInstance.noTooltip(),
        (caption, value) -> {
            Component label = Component.translatableWithFallback("options.fade.player_cull_distance", "Cull Distance");
            return Component.translatable("options.generic_value", label, Component.literal(value.intValue() + " Blocks"));
        },
        new OptionInstance.IntRange(0, 256).xmap(Double::valueOf, Double::intValue, true),
        64.0,
        value -> FadeConfig.save()
    );

    // 💡 [개선 완료] 타겟 옵션이 바뀔 때마다 하단 텍스트 박스의 활성화 상태를 업데이트하도록 이벤트를 연결합니다.
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
            // ⚡ 스크린이 열려있고 에딧박스가 생성된 상태라면, 토글 버튼을 누를 때마다 즉시 상태를 갱신합니다.
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
        value -> FadeConfig.save()
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
        }
    }

    @Override
    protected void init() {
        this.addTitle();
        this.addContents(); // 중앙의 4개 옵션 버튼이 여기서 먼저 쫙 깔립니다.
        
        if (this.minecraft != null) {
            // Y축 좌표는 repositionElements()에서 레이아웃 계산 후 동적으로 잡아줄 거라 일단 0으로 둡니다.
            this.customFilterBox = new EditBox(
                this.minecraft.font, 
                this.width / 2 - 150, 0, 300, 20, 
                Component.literal("Custom Targets")
            );
            this.customFilterBox.setHint(Component.translatable("options.fade.custom_targets.placeholder"));
            this.customFilterBox.setTooltip(Tooltip.create(Component.translatable("options.fade.custom_targets.tooltip")));

            this.customFilterBox.setValue(FadeConfig.customFilterString);
            this.customFilterBox.setResponder(value -> {
                FadeConfig.customFilterString = value;
                FadeConfig.save();
            });

            this.updateBoxActivation();

            // 💡 [수정] addToFooter를 과감히 삭제하고, 스크린 인터페이스에 직접 독립 리스너로 등록합니다.
            this.addWidget(this.customFilterBox);
        }

        this.addFooter(); // 맨 밑바닥 완료 버튼 생성
        
        // 💡 에딧박스를 화면에 그리기 리스트에 정식 등록 (visitWidgets 이전에 등록해야 레이어 순서가 맞습니다)
        if (this.customFilterBox != null) {
            this.addRenderableWidget(this.customFilterBox);
        }

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements(); // 👈 여기서 최종 위치를 마법처럼 재조정합니다.
    }

    // ⚡ [새로 추가] 모장 레이아웃 정렬이 끝난 타이밍을 노려 에딧박스를 완벽한 위치로 이동시킵니다.
   // ⚡ 모장 레이아웃 함수 호출을 피하고, 스크린 고유 값인 this.height로 우회 정렬합니다.
    @Override
    protected void repositionElements() {
        // 1. 먼저 부모 클래스가 리스트와 완료 버튼 위치를 잡게 둡니다.
        super.repositionElements();
        
        // 2. ✨ [우회 좌표 계산]
        // 보통 마크 하단 푸터 완료 버튼들의 기준선이 바닥(this.height)에서 약 36~40픽셀 위쪽에 잡힙니다.
        // 에딧박스가 완료 버튼과 겹치지 않고 그 바로 위에 뜨도록 바닥에서 65픽셀 띄운 위치로 강제 고정합니다.
        if (this.customFilterBox != null) {
            // X축은 화면 정중앙
            this.customFilterBox.setX(this.width / 2 - 150);
            
            // Y축은 전체 화면 높이에서 65픽셀을 뺀 지점 (완료 버튼 바로 윗 공간)
            this.customFilterBox.setY(this.height - 65); 
        }
    }

    // 💡 [새로 추가] CULL_TARGET이 CUSTOM일 때만 에딧박스를 활성화하는 전담 메서드
    // 💡 에러가 나던 setFocusable 라인을 지우고 순정 호환되는 속성들로만 재구성했습니다.
    public void updateBoxActivation() {
        if (this.customFilterBox != null) {
            boolean isCustomMode = CULL_TARGET.get() == CullTarget.CUSTOM;
            
            // 1. 텍스트 상자 수정 가능 여부 설정 (false면 커서가 안 깜빡이고 입력이 막힘)
            this.customFilterBox.setEditable(isCustomMode);
            
            // 2. 모장 순정 UI 활성화 여부 (false면 상자가 어둡게 회색조 처리됨)
            this.customFilterBox.active = isCustomMode;
            
            // 3. 비활성화될 때 혹시 잡혀있을지 모를 포커스를 안전하게 해제
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
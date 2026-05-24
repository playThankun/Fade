package com.thankun.fade.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thankun.fade.client.gui.FadeConfigScreen;
import com.thankun.fade.client.util.FadeFilterUtil; 
import com.thankun.fade.client.util.FadeRenderUtil;
import com.thankun.fade.client.config.FadeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.UUID;

@Mixin(EntityRenderDispatcher.class)
public class FadeEntityRenderMixin {

    @Inject(
        method = "submit",
        at = @At("HEAD"),
        cancellable = true
    )
    private void fade$executeProtocol(
            EntityRenderState renderState,
            CameraRenderState camera,
            double x, double y, double z,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CallbackInfo ci
    ) {

        Minecraft client = Minecraft.getInstance();

        if (client.player == null) {
            return;
        }

        // 플레이어 여부
        boolean isPlayer = renderState instanceof AvatarRenderState;
        
        // 현재 렌더링하려는 대상이 바닥에 떨어진 아이템 엔티티인지 확인
        boolean isItemEntity = renderState instanceof ItemEntityRenderState;

        // 거리 계산
        double distanceSq = x * x + y * y + z * z;

        // UUID 기반 자기 자신 판별
        UUID selfUUID = client.player.getUUID();
        UUID renderUUID = FadeRenderUtil.getUUID(renderState);

        boolean isSelf =
                renderUUID != null &&
                renderUUID.equals(selfUUID);

        // UUID 못 찾았을 때 임시 거리 보호
        if (!isSelf) {
            isSelf = isPlayer && distanceSq < 0.01;
        }

        // 자기 자신 렌더 보호
        if (isSelf) {
            return;
        }

        FadeConfigScreen.OperationMode currentMode = FadeConfigScreen.OPERATION_MODE.get();
        double minDistance = FadeConfigScreen.PLAYER_CULL_DISTANCE.get(); 
        double limitDistanceSq = minDistance * minDistance;

        if (currentMode == FadeConfigScreen.OperationMode.OPTIMIZE) {
            if (distanceSq <= limitDistanceSq) {
                return;
            }
        } else {
            if (distanceSq >= limitDistanceSq) {
                return;
            }
        }

        FadeConfigScreen.CullTarget targetMode = FadeConfigScreen.CULL_TARGET.get();

        boolean shouldApplyCull = switch (targetMode) {
            case PLAYERS_ONLY -> isPlayer;
            case ENTITIES_ONLY -> !isPlayer;
            case ALL -> true;
            
            case CUSTOM -> {
                if (isItemEntity && FadeConfig.customFilterString.contains("item")) {
                    yield true;
                }
                
                String entityId = null;
                if (isPlayer) {
                    entityId = "minecraft:player";
                } else if (renderState.entityType != null) {
                    entityId = BuiltInRegistries.ENTITY_TYPE.getKey(renderState.entityType).toString(); 
                }

                if (entityId == null) {
                    entityId = renderState.getClass().getSimpleName().toLowerCase()
                            .replace("renderstate", "")
                            .replace("state", "");
                }
                yield FadeFilterUtil.matchesCustomFilter(entityId, isPlayer);
            }
        };

        if (!shouldApplyCull) {
            return;
        }

        FadeConfigScreen.CullMode mode = FadeConfigScreen.CULL_MODE.get();

        switch (mode) {
            case VANISH -> ci.cancel();
            
            case FADE -> {
                if (renderState instanceof LivingEntityRenderState livingState) {
                    livingState.isInvisible = true;
                } else if (isItemEntity) {
                    ci.cancel(); 
                }
            }
            case OFF -> {
            }
        }
    }
}
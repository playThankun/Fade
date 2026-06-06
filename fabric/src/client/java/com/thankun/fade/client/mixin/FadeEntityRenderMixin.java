package com.thankun.fade.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thankun.fade.client.config.FadeConfig;
import com.thankun.fade.client.gui.FadeConfigScreen;
import com.thankun.fade.client.gui.FadeConfigScreen.OperationMode;
import com.thankun.fade.client.util.FadeFilterUtil;
import com.thankun.fade.client.util.FadeRenderUtil;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderDispatcher.class})
public class FadeEntityRenderMixin {
    @Inject(
        method = {"submit"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fade$executeProtocol(EntityRenderState renderState, CameraRenderState camera, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector collector, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        boolean isFallingBlock = renderState.entityType == EntityType.FALLING_BLOCK;
        if (isFallingBlock && !FadeConfig.cullFallingBlocks) {
            return;
        }

        boolean isPlayer = renderState instanceof AvatarRenderState;
        boolean isItemEntity = renderState instanceof ItemEntityRenderState;
        
        double distanceSq = x * x + y * y + z * z;
        UUID selfUUID = client.player.getUUID();
        UUID renderUUID = FadeRenderUtil.getUUID(renderState);
        
        boolean isSelf = renderUUID != null && renderUUID.equals(selfUUID);
        if (!isSelf) {
            isSelf = isPlayer && distanceSq < 0.01;
        }
        if (isSelf) {
            return;
        }
        FadeConfigScreen.OperationMode currentMode = (FadeConfigScreen.OperationMode)FadeConfigScreen.OPERATION_MODE.get();
        double minDistance = (double)(Integer)FadeConfigScreen.PLAYER_CULL_DISTANCE.get();
        double limitDistanceSq = minDistance * minDistance;
        
        if (currentMode == OperationMode.OPTIMIZE) {
            if (distanceSq <= limitDistanceSq) {
                return;
            }
        } else if (distanceSq >= limitDistanceSq) {
            return;
        }

        boolean isTargeted = false;
        FadeConfigScreen.CullTarget targetMode = (FadeConfigScreen.CullTarget)FadeConfigScreen.CULL_TARGET.get();
        
        switch (targetMode) {
            case PLAYERS_ONLY:
                isTargeted = isPlayer;
                break;
                
            case ENTITIES_ONLY:
                isTargeted = !isPlayer;
                break;
                
            case ALL:
                isTargeted = true;
                break;
                
            case CUSTOM:
                if (isItemEntity && FadeConfig.customFilterString.contains("item")) {
                    isTargeted = true;
                } else {
                    if (isPlayer && renderUUID != null) {
                        String playerName = FadeRenderUtil.getPlayerName(renderUUID);
                        if (playerName != null && FadeConfig.customFilterString.contains(playerName)) {
                            isTargeted = true;
                            break;
                        }
                    }

                    String entityId = null;
                    if (isPlayer) {
                        entityId = "minecraft:player";
                    } else if (renderState.entityType != null) {
                        entityId = BuiltInRegistries.ENTITY_TYPE.getKey(renderState.entityType).toString();
                    }

                    if (entityId == null) {
                        entityId = renderState.getClass().getSimpleName().toLowerCase().replace("renderstate", "").replace("state", "");
                    }

                    isTargeted = FadeFilterUtil.matchesCustomFilter(entityId, isPlayer);
                }
                break;
                
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

        if (isTargeted) {
            FadeConfigScreen.CullMode mode = (FadeConfigScreen.CullMode)FadeConfigScreen.CULL_MODE.get();
            switch (mode) {
                case VANISH:
                    ci.cancel();
                    break;
                    
                case FADE:
                    if (renderState instanceof LivingEntityRenderState) {
                        LivingEntityRenderState livingState = (LivingEntityRenderState)renderState;
                        livingState.isInvisible = true;
                    } else if (isItemEntity) {
                        if (FadeConfig.fadeItemAsVanish) {
                            ci.cancel();
                        }
                    } else if (isFallingBlock) {
                        ci.cancel();
                    }
                    break;
                    
                case OFF:
                    break;
            }
        }
    }
}
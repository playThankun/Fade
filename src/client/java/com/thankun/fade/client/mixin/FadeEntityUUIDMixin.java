package com.thankun.fade.client.mixin;

import com.thankun.fade.client.util.FadeRenderUtil;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class FadeEntityUUIDMixin {

    @Inject(
        method = "extractRenderState",
        at = @At("TAIL")
    )
    private void fade$storeUUID(
            Entity entity,
            EntityRenderState state,
            float partialTick,
            CallbackInfo ci
    ) {

        FadeRenderUtil.storeUUID(
                state,
                entity.getUUID()
        );
    }
}
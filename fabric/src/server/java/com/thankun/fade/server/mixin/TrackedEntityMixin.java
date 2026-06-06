package com.thankun.fade.server.mixin;

import com.thankun.fade.server.config.FadeServerConfig;
import com.thankun.fade.server.util.FadeServerFilterUtil;
import java.lang.reflect.Method;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    targets = {"net.minecraft.server.level.ChunkMap$TrackedEntity"}
)
public abstract class TrackedEntityMixin {
    @Shadow
    @Final
    Entity entity;
    
    @Shadow
    @Final
    Set<ServerGamePacketListenerImpl> seenBy;

    @Inject(
        method = {"updatePlayer"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fade$overridePlayerTracking(ServerPlayer player, CallbackInfo ci) {
        if (this.entity == null || this.entity.level().isClientSide()) {
            return;
        }

        if (player == this.entity || player.level() != this.entity.level()) {
            return;
        }

        boolean isFallingBlock = this.entity instanceof FallingBlockEntity;
        if (isFallingBlock && !FadeServerConfig.cullFallingBlocks) {
            return;
        }

        boolean isPlayer = this.entity instanceof Player;
        boolean shouldCull = false;
        String target = FadeServerConfig.cullTarget;
        
        if ("PLAYERS_ONLY".equalsIgnoreCase(target)) {
            shouldCull = isPlayer;
        } else if ("ENTITIES_ONLY".equalsIgnoreCase(target)) {
            shouldCull = !isPlayer;
        } else if ("ALL".equalsIgnoreCase(target)) {
            shouldCull = true;
        }
        String entityId = EntityType.getKey(this.entity.getType()).toString();
        if (FadeServerFilterUtil.matchesCustomFilter(entityId, isPlayer)) {
            shouldCull = true;
        }

        if (shouldCull) {
            double distanceSq = player.distanceToSqr(this.entity);
            double limitDistanceSq = (double)(FadeServerConfig.playerCullDistance * FadeServerConfig.playerCullDistance);
            
            boolean isFadeOutMode = "FADE_OUT".equalsIgnoreCase(FadeServerConfig.operationMode);
            boolean satisfyCullCondition = isFadeOutMode ? distanceSq <= limitDistanceSq : distanceSq > limitDistanceSq;

            if (satisfyCullCondition) {
                boolean isCurrentlySeen = player.connection != null && this.seenBy.contains(player.connection);
                if (isCurrentlySeen) {
                    this.fade$invokeRemovePlayer(player);
                }
                ci.cancel();
            }
        }
    }

    private void fade$invokeRemovePlayer(ServerPlayer player) {
        try {
            Method removePlayerMethod = this.getClass().getDeclaredMethod("removePlayer", ServerPlayer.class);
            removePlayerMethod.setAccessible(true);
            removePlayerMethod.invoke(this, player);
        } catch (Exception e) {
        }
    }
}
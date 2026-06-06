package com.thankun.fade.server.mixin;

import java.util.Set;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    targets = {"net.minecraft.server.level.ChunkMap$TrackedEntity"}
)
public interface TrackedEntityAccessor {
    @Accessor("seenBy")
    Set<ServerGamePacketListenerImpl> getSeenBy();

    @Accessor("entity")
    Entity getEntity();
}

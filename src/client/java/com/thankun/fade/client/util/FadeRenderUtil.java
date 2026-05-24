package com.thankun.fade.client.util;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class FadeRenderUtil {

    private static final Map<EntityRenderState, UUID> UUIDS =
            new WeakHashMap<>();
            

    public static void storeUUID(EntityRenderState state, UUID uuid) {
        UUIDS.put(state, uuid);
        
        
    }

    public static UUID getUUID(EntityRenderState state) {
        return UUIDS.get(state);
    }
}
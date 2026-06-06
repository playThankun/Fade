package com.thankun.fade.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thankun.fade.Fade;
import com.thankun.fade.server.util.FadeServerFilterUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;

public class FadeServerConfig {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("fade-server.json").toFile();
    public static String lang = "ko_kr";
    public static String operationMode = "FADE_OUT";
    public static int playerCullDistance = 16;
    public static String cullTarget = "PLAYERS_ONLY";
    public static String cullMode = "VANISH";
    public static boolean cullFallingBlocks = false;
    public static boolean fadeItemAsVanish = true;
    
    public static String customFilterString = "creeper, zombie, @p, item";
    public static MinecraftServer currentServer = null;

    public static void save() {
        ServerConfigData data = new ServerConfigData();
        if (cullMode == null || cullMode.trim().toUpperCase().contains("FADE")) {
            cullMode = "VANISH";
        } else {
            cullMode = cullMode.trim().toUpperCase();
        }

        if ("en_us".equalsIgnoreCase(lang)) {
            data._comment_mode = "★ [Warning] FADE mode is NOT supported on the server. Automatically falling back to VANISH.";
        } else {
            data._comment_mode = "★ [경고] 서버 환경은 FADE 모드를 지원하지 않습니다. 자동으로 VANISH 모드로 리셋됩니다.";
        }

        data.lang = lang.toLowerCase();
        data.operationMode = operationMode;
        data.playerCullDistance = playerCullDistance;
        data.cullTarget = cullTarget;
        data.cullMode = cullMode;
        data.cullFallingBlocks = cullFallingBlocks;
        data.fadeItemAsVanish = fadeItemAsVanish;
        data.customFilterString = customFilterString;
        FadeServerFilterUtil.rebuildFilterCache(customFilterString);

        try {
            File parent = CONFIG_FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(data, writer);
                Fade.LOGGER.info("[Fade Server] Config synchronized to file: {}", CONFIG_FILE.getAbsolutePath());
            }

            refreshAllTrackedEntities();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
        } else {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ServerConfigData data = (ServerConfigData)GSON.fromJson(reader, ServerConfigData.class);
                if (data != null) {
                    lang = data.lang != null ? data.lang : "ko_kr";
                    operationMode = data.operationMode != null ? data.operationMode : "FADE_OUT";
                    playerCullDistance = data.playerCullDistance;
                    cullTarget = data.cullTarget != null ? data.cullTarget : "PLAYERS_ONLY";
                    String loadedMode = data.cullMode != null ? data.cullMode.trim().toUpperCase() : "VANISH";
                    if (loadedMode.contains("FADE")) {
                        cullMode = "VANISH";
                    } else {
                        cullMode = loadedMode;
                    }
                    
                    cullFallingBlocks = data.cullFallingBlocks;
                    fadeItemAsVanish = data.fadeItemAsVanish;
                    
                    customFilterString = data.customFilterString != null ? data.customFilterString : "creeper, zombie, @p, item";
                    FadeServerFilterUtil.rebuildFilterCache(customFilterString);
                    Fade.LOGGER.info("[Fade Server] Config loaded successfully. (Mode: VANISH [FADE Disabled])");
                }
            } catch (Exception e) {
                e.printStackTrace();
                save();
            }
        }
    }

    public static void refreshAllTrackedEntities() {
        if (currentServer != null) {
            try {
                for(ServerLevel level : currentServer.getAllLevels()) {
                    ChunkMap chunkMap = level.getChunkSource().chunkMap;
                    Field entityMapField = ChunkMap.class.getDeclaredField("entityMap");
                    entityMapField.setAccessible(true);
                    Int2ObjectMap<?> entityMap = (Int2ObjectMap)entityMapField.get(chunkMap);
                    if (entityMap != null) {
                        ObjectIterator var5 = entityMap.values().iterator();

                        while(var5.hasNext()) {
                            Object trackedEntity = var5.next();
                            Field concreteEntityField = trackedEntity.getClass().getDeclaredField("entity");
                            concreteEntityField.setAccessible(true);
                            Entity entity = (Entity)concreteEntityField.get(trackedEntity);
                            if (entity != null && (cullFallingBlocks || !(entity instanceof FallingBlockEntity))) {
                                String entityId = EntityType.getKey(entity.getType()).toString();
                                boolean isPlayer = entity instanceof Player;
                                boolean isTargeted = FadeServerFilterUtil.matchesCustomFilter(entityId, isPlayer);
                                if (isTargeted) {
                                    Method updatePlayersMethod = trackedEntity.getClass().getDeclaredMethod("updatePlayers", List.class);
                                    updatePlayersMethod.setAccessible(true);
                                    updatePlayersMethod.invoke(trackedEntity, level.players());
                                }
                            }
                        }
                    }
                }
                Fade.LOGGER.info("[Fade Server] 외부 최적화 필터 유틸을 적용하여 엔티티 추적 새로고침 완료.");
            } catch (Exception e) {
                Fade.LOGGER.error("[Fade Server] Error refreshing entities: " + e.getMessage());
            }
        }
    }

    public static void setOperationMode(String mode) { operationMode = mode; save(); }
    public static void setPlayerCullDistance(int distance) { playerCullDistance = distance; save(); }
    public static void setCullTarget(String target) { cullTarget = target; save(); }
    public static void setCullMode(String mode) { cullMode = mode; save(); }
    public static void setCullFallingBlocks(boolean allowed) { cullFallingBlocks = allowed; save(); }
    public static void setFadeItemAsVanish(boolean enabled) { fadeItemAsVanish = enabled; save(); }
    public static void setCustomFilterString(String filter) { customFilterString = filter; save(); }

    public static class ServerConfigData {
        public String _comment_mode = "";
        public String lang = "ko_kr";
        public String cullMode = "VANISH";
        public String operationMode = "FADE_OUT";
        public int playerCullDistance = 16;
        public String cullTarget = "PLAYERS_ONLY";
        public boolean cullFallingBlocks = false;
        public boolean fadeItemAsVanish = true;
        public String customFilterString = "creeper, zombie, @p, item";
    }
}
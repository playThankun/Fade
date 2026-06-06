package com.thankun.fade.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thankun.fade.client.gui.FadeConfigScreen;
import com.thankun.fade.client.util.FadeFilterUtil;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FadeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("fade.json").toFile();
    
    public static class ConfigData {
        public String operationMode = "FADE_OUT";
        public int playerCullDistance = 64;
        public String cullTarget = "PLAYERS_ONLY";
        public String cullMode = "VANISH";
        public int fadeDistance = 8;
        public boolean cullFallingBlocks = false;
        public String customFilterString = "creeper, zombie, @p, item";
        public boolean fadeItemAsVanish = true;
    }
    
    public static final ConfigData CONFIG = new ConfigData();

    public static String customFilterString = "creeper, zombie, @p, item";
    public static boolean cullFallingBlocks = false;
    public static boolean fadeItemAsVanish = true;

    public static void save() {
        ConfigData data = new ConfigData();
        data.fadeDistance = CONFIG.fadeDistance;
        
        data.operationMode = FadeConfigScreen.OPERATION_MODE.get().name();
        data.playerCullDistance = FadeConfigScreen.PLAYER_CULL_DISTANCE.get();
        data.cullTarget = FadeConfigScreen.CULL_TARGET.get().name();
        data.cullMode = FadeConfigScreen.CULL_MODE.get().name();
        data.cullFallingBlocks = FadeConfigScreen.CULL_FALLING_BLOCKS.get();
        data.customFilterString = customFilterString;
        data.fadeItemAsVanish = fadeItemAsVanish; 

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(data, writer);
        } catch (IOException e) { e.printStackTrace(); }
        FadeFilterUtil.rebuildFilterCache(customFilterString);
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) { save(); return; }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            
            CONFIG.fadeDistance = data.fadeDistance;
            customFilterString = data.customFilterString != null ? data.customFilterString : "creeper, zombie, @p, item";
            cullFallingBlocks = data.cullFallingBlocks;
            fadeItemAsVanish = data.fadeItemAsVanish;
            
            FadeConfigScreen.OPERATION_MODE.set(FadeConfigScreen.OperationMode.valueOf(data.operationMode));
            FadeConfigScreen.PLAYER_CULL_DISTANCE.set(data.playerCullDistance);
            FadeConfigScreen.CULL_TARGET.set(FadeConfigScreen.CullTarget.valueOf(data.cullTarget));
            FadeConfigScreen.CULL_MODE.set(FadeConfigScreen.CullMode.valueOf(data.cullMode));
            FadeConfigScreen.CULL_FALLING_BLOCKS.set(cullFallingBlocks);
            

            FadeFilterUtil.rebuildFilterCache(customFilterString);

        } catch (Exception e) { 
            e.printStackTrace(); 
            save();
        }
    }

    public static ConfigData getStorage() {
        return CONFIG;
    }
}
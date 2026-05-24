package com.thankun.fade.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thankun.fade.client.gui.FadeConfigScreen;
import com.thankun.fade.client.util.FadeFilterUtil; // 💡 캐시 자동 동기화
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FadeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("fade.json").toFile();

    // 💡 1. 단일화된 사양으로 JSON 포맷 확정 (엔티티 거리 필드 삭제)
    public static class ConfigData {
        public String operationMode = "FADE_OUT";
        public double playerCullDistance = 64.0;   // 렌더링을 제한할 통합 기준 거리
        public String cullTarget = "PLAYERS_ONLY";
        public String cullMode = "VANISH";
        public int fadeDistance = 8;
        public String customFilterString = "creeper, zombie, @p, item"; // 커스텀 텍스트 기본값
    }

    // 💡 2. GUI 에딧박스와 실시간 데이터 소통을 위한 전역 변수
    public static String customFilterString = "creeper, zombie, @p, item";

    public static void save() {
        ConfigData data = new ConfigData();
        data.fadeDistance = CONFIG.fadeDistance;
        
        // 💡 3. 화면의 단일화된 세팅 및 텍스트 박스 상태를 완벽 수집
        data.operationMode = FadeConfigScreen.OPERATION_MODE.get().name();
        data.playerCullDistance = FadeConfigScreen.PLAYER_CULL_DISTANCE.get();
        data.cullTarget = FadeConfigScreen.CULL_TARGET.get().name();
        data.cullMode = FadeConfigScreen.CULL_MODE.get().name();
        data.customFilterString = customFilterString;

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(data, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) { save(); return; }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            
            CONFIG.fadeDistance = data.fadeDistance;
            customFilterString = data.customFilterString != null ? data.customFilterString : "creeper, zombie, @p, item";
            
            // 💡 4. JSON에서 읽은 데이터를 깔끔해진 GUI 토글 인스턴스에 강제 매핑
            FadeConfigScreen.OPERATION_MODE.set(FadeConfigScreen.OperationMode.valueOf(data.operationMode));
            FadeConfigScreen.PLAYER_CULL_DISTANCE.set(data.playerCullDistance);
            FadeConfigScreen.CULL_TARGET.set(FadeConfigScreen.CullTarget.valueOf(data.cullTarget));
            FadeConfigScreen.CULL_MODE.set(FadeConfigScreen.CullMode.valueOf(data.cullMode));

            // ⚡ 5. 게임 최초 시동 시 유저의 커스텀 필터 텍스트를 기반으로 고속 해시셋 캐시 인젝션
            FadeFilterUtil.rebuildFilterCache(customFilterString);

        } catch (Exception e) { 
            e.printStackTrace(); 
            // 파일이 깨졌거나 구버전 호환 실패 시 세이프 모드로 세이브 복구
            save();
        }
    }

    public static final ConfigData CONFIG = new ConfigData();

    public static ConfigData getStorage() {
        return CONFIG;
    }
}
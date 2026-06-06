package com.thankun.fade.client.util;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap; // 🚀 스레드 안전한 캐시를 위해 추가
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

public class FadeRenderUtil {

    private static final Map<EntityRenderState, UUID> UUIDS = new WeakHashMap<>();
    
    // 🚀 UUID로 닉네임을 빠르게 찾기 위한 경량 캐시 맵 추가
    private static final Map<UUID, String> UUID_TO_NAME_CACHE = new ConcurrentHashMap<>();

    public static void storeUUID(EntityRenderState state, UUID uuid) {
        UUIDS.put(state, uuid);
    }

    public static UUID getUUID(EntityRenderState state) {
        return UUIDS.get(state);
    }

    /**
     * 🚀 UUID를 기반으로 플레이어의 닉네임을 가져오는 메서드
     * 캐시에 있으면 즉시 반환하고, 없으면 Minecraft 네트워크 정보에서 찾아서 캐싱합니다.
     */
    public static String getPlayerName(UUID uuid) {
        if (uuid == null) return null;

        // 1. 이미 캐시에 등록된 이름이 있다면 바로 리턴 (연산 최적화)
        String cachedName = UUID_TO_NAME_CACHE.get(uuid);
        if (cachedName != null) {
            return cachedName;
        }

        // 2. 캐시에 없다면 마인크래프트 클라이언트의 플레이어 정보 목록에서 탐색
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null) {
            PlayerInfo playerInfo = client.getConnection().getPlayerInfo(uuid);
            if (playerInfo != null && playerInfo.getProfile() != null) {
                String name = playerInfo.getProfile().name();
                if (name != null) {
                    // 다음 연산을 위해 소문자로 변환하여 캐시에 저장 (필터 매칭 편의성)
                    String lowerName = name.toLowerCase();
                    UUID_TO_NAME_CACHE.put(uuid, lowerName);
                    return lowerName;
                }
            }
        }
        return null;
    }

    /**
     * 🚀 서버 이동이나 로그아웃 시 캐시가 쌓여있는 것을 방지하기 위한 초기화 메서드
     * (필요할 때 패킷 핸들러나 클라이언트 초기화 단에서 호출해주면 좋습니다.)
     */
    public static void clearCache() {
        UUID_TO_NAME_CACHE.clear();
    }
}
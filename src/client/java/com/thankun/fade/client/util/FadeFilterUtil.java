package com.thankun.fade.client.util;

import com.thankun.fade.client.gui.FadeConfigScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import java.util.HashSet;
import java.util.Set;

public class FadeFilterUtil {

    // 💡 메모리 절약과 성능을 위한 고속 비트 플래그 및 해시셋 캐시
    private static boolean targetAllPlayers = false;  // @a 또는 @p 감지용
    private static boolean targetAllEntities = false; // @e 감지용
    private static final Set<String> CUSTOM_TARGET_SET = new HashSet<>();

    /**
     * ⚡ [Config 저장 / 설정창 닫힐 때 딱 1번 호출] 
     * 유저가 입력한 문자열을 복수형과 셀렉터까지 고려해서 광속 캐시로 굽는 메서드
     */
    public static void rebuildFilterCache(String rawInput) {
        CUSTOM_TARGET_SET.clear();
        targetAllPlayers = false;
        targetAllEntities = false;

        if (rawInput == null || rawInput.isBlank()) return;

        // 콤마(,) 기준으로 쪼개서 복수형(배열) 완벽 지원
        String[] tokens = rawInput.split(",");
        for (String token : tokens) {
            String trimmed = token.trim().toLowerCase();
            
            if (trimmed.isEmpty()) continue;

            // 1. 모장 순정 감성 @p, @a, @e 셀렉터 우회 처리
            if (trimmed.equals("@p") || trimmed.equals("@a")) {
                targetAllPlayers = true;
                continue;
            }
            if (trimmed.equals("@e")) {
                targetAllEntities = true;
                continue;
            }
            
            // 2. 네임스페이스 제거 (minecraft:creeper -> creeper)
            if (trimmed.contains(":")) {
                trimmed = trimmed.split(":")[1];
            }
            
            // 3. 최종 정제된 복수형 이름들 셋에 적재
            CUSTOM_TARGET_SET.add(trimmed);
        }
    }

    /**
     * ⚡ [실시간 렌더러 루프 매 프레임 수천 번 호출]
     * 믹스인 단에서 요청 시 O(1) 속도로 타겟 여부를 판단하는 초고속 매칭 엔진
     */
    public static boolean matchesCustomFilter(EntityRenderState renderState, boolean isPlayer) {
        // 1. @e (모든 엔티티 차단)가 켜져 있으면 무조건 패스
        if (targetAllEntities) {
            return true;
        }

        // 2. @p 나 @a 가 켜져 있고 현재 대상이 플레이어라면 패스
        if (targetAllPlayers && isPlayer) {
            return true;
        }

        // 3. 그 외에 유저가 콤마로 등록한 특정 몹 이름(복수형) 매칭 검사
        // 모장 매핑 기준 클래스명에서 접미사 제거 (예: ZombieRenderState -> zombie)
        String stateName = renderState.getClass().getSimpleName().toLowerCase()
                .replace("renderstate", "")
                .replace("state", "");

        return CUSTOM_TARGET_SET.contains(stateName);
    }
}
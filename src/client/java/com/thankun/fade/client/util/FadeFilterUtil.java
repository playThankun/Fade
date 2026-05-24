package com.thankun.fade.client.util;

import java.util.HashSet;
import java.util.Set;

public class FadeFilterUtil {

    private static boolean targetAllPlayers = false; 
    private static boolean targetAllEntities = false;
    private static final Set<String> CUSTOM_TARGET_SET = new HashSet<>();

    public static void rebuildFilterCache(String rawInput) {
        CUSTOM_TARGET_SET.clear();
        targetAllPlayers = false;
        targetAllEntities = false;

        if (rawInput == null || rawInput.isBlank()) return;

        String[] tokens = rawInput.split(",");
        for (String token : tokens) {
            String trimmed = token.trim().toLowerCase();
            
            if (trimmed.isEmpty()) continue;

            if (trimmed.equals("@p") || trimmed.equals("@a")) {
                targetAllPlayers = true;
                continue;
            }
            if (trimmed.equals("@e")) {
                targetAllEntities = true;
                continue;
            }
            
            CUSTOM_TARGET_SET.add(trimmed);
        }
    }

    /**
     * * @param entityId
     *  @param isPlayer
     */
    public static boolean matchesCustomFilter(String entityId, boolean isPlayer) {
        if (targetAllEntities) {
            return true;
        }

        if (targetAllPlayers && isPlayer) {
            return true;
        }

        if (entityId == null) {
            return false;
        }

        for (String target : CUSTOM_TARGET_SET) {
            if (target.contains(":")) {
                if (entityId.equals(target)) {
                    return true;
                }
            } else {
                String[] parts = entityId.split(":");
                String entityName = parts.length > 1 ? parts[1] : parts[0];
                
                if (entityName.equals(target)) {
                    return true;
                }
            }
        }

        return false;
    }
}
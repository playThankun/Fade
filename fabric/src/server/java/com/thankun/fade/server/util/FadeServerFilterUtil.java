package com.thankun.fade.server.util;

import java.util.HashSet;
import java.util.Set;

public class FadeServerFilterUtil {
    private static boolean targetAllPlayers = false;
    private static boolean targetAllEntities = false;
    private static final Set<String> CUSTOM_TARGET_SET = new HashSet();

    public static void rebuildFilterCache(String rawInput) {
        CUSTOM_TARGET_SET.clear();
        targetAllPlayers = false;
        targetAllEntities = false;
        if (rawInput != null && !rawInput.isBlank()) {
            String[] tokens = rawInput.split(",");

            for(String token : tokens) {
                String trimmed = token.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    if (!trimmed.equals("@p") && !trimmed.equals("@a")) {
                        if (trimmed.equals("@e")) {
                            targetAllEntities = true;
                        } else {
                            CUSTOM_TARGET_SET.add(trimmed);
                        }
                    } else {
                        targetAllPlayers = true;
                    }
                }
            }

        }
    }

    public static boolean matchesCustomFilter(String entityId, boolean isPlayer) {
        if (targetAllEntities) {
            return true;
        } else if (targetAllPlayers && isPlayer) {
            return true;
        } else if (entityId == null) {
            return false;
        } else {
            for(String target : CUSTOM_TARGET_SET) {
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
}

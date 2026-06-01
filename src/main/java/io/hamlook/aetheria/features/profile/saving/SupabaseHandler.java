package io.hamlook.aetheria.features.profile.saving;

import io.hamlook.aetheria.Aetheria;
import io.hamlook.aetheria.features.profile.WaiterLogs;
import io.hamlook.aetheria.features.profile.data.ProfileData;

public class SupabaseHandler {

    private SupabaseHandler() {}

    public static void pushProfileAsync(String playerName, ProfileData data) {
        Aetheria.logger.info("[SupabaseHandler] Profile upload disabled for: " + playerName);
        WaiterLogs.addLog("[SupabaseHandler] Profile upload disabled for: " + playerName);
        WaiterLogs.saveLogs();
    }
}

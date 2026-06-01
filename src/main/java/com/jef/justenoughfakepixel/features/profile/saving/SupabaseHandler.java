package com.jef.justenoughfakepixel.features.profile.saving;

import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.features.profile.WaiterLogs;
import com.jef.justenoughfakepixel.features.profile.data.ProfileData;

public class SupabaseHandler {

    private SupabaseHandler() {}

    public static void pushProfileAsync(String playerName, ProfileData data) {
        JefMod.logger.info("[SupabaseHandler] Profile upload disabled for: " + playerName);
        WaiterLogs.addLog("[SupabaseHandler] Profile upload disabled for: " + playerName);
        WaiterLogs.saveLogs();
    }
}

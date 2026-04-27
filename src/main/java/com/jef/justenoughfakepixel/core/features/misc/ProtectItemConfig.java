package com.jef.justenoughfakepixel.core.features.misc;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;

public class ProtectItemConfig {

    @Expose
    @ConfigOption(name = "Show Protected Star", desc = "Show a star overlay on items protected by /jefprotect")
    @ConfigEditorBoolean
    public boolean showProtectedStar = true;
}
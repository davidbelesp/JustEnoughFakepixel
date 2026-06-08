package io.hamlook.aetheria.core.features.misc;

import com.google.gson.annotations.Expose;
import io.hamlook.aetheria.core.moulconfig.gui.config.ConfigAnnotations.*;
import io.hamlook.aetheria.utils.Position;

public class SearchBarConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Shows a search bar in supported GUIs")
    @ConfigEditorBoolean
    public boolean searchBar = true;

    @Expose
    @ConfigOption(name = "Highlight Color", desc = "Color used to highlight matching items in search results")
    @ConfigEditorColour
    public String searchBarHighlightColor = "0:102:255:0:0";

    @Expose
    @ConfigOption(name = "Edit Search Bar Position", desc = "Drag to reposition the search bar")
    @ConfigEditorButton(runnableId = "openSearchBarEditor", buttonText = "Edit")
    public boolean editSearchBarPosDummy = false;

    @Expose
    public Position searchBarPos = new Position(0, -30, true, false);

    @Expose
    @ConfigOption(name = "Persist Search", desc = "Keep main searchbar text between GUI opens")
    @ConfigEditorBoolean
    public boolean persistSearchText = true;

    @Expose
    @ConfigOption(name = "Persist Item List Search", desc = "Keep Item List local search text between GUI opens (only applies when not using global search)")
    @ConfigEditorBoolean
    public boolean persistItemListSearch = false;

    @Expose
    @ConfigOption(name = "Persist Storage Search", desc = "Keep Storage Overlay search text between GUI opens")
    @ConfigEditorBoolean
    public boolean persistStorageSearch = false;
}

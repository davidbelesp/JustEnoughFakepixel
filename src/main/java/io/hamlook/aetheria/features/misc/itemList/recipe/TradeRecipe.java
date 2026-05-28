package io.hamlook.aetheria.features.misc.itemList.recipe;

import com.google.gson.JsonObject;
import io.hamlook.aetheria.features.misc.itemList.SkyblockItem;

public class TradeRecipe extends ForgeRecipe {
    public TradeRecipe(SkyblockItem t, JsonObject d) { super(t, d, "trade"); }
}
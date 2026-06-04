package io.hamlook.aetheria.features.price;

import com.google.gson.annotations.SerializedName;

public class ItemPrice {

    @SerializedName(value = "item",alternate = {"itemID"})
    public String itemID;
    public Price prices;
    public PriceType priceType;
    public long timestamp;

    public ItemPrice(String itemID, Price prices, PriceType priceType) {
        this.itemID = itemID;
        this.prices = prices;
        this.priceType = priceType;
        this.timestamp = System.currentTimeMillis();
    }

}

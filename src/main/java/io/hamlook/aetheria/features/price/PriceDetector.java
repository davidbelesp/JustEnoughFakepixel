package io.hamlook.aetheria.features.price;

import com.google.gson.Gson;
import io.hamlook.aetheria.Aetheria;
import io.hamlook.aetheria.features.profile.ProfileParser;
import io.hamlook.aetheria.init.RegisterEvents;
import io.hamlook.aetheria.repo.CapeAPI;
import io.hamlook.aetheria.repo.OtherDataAPI;
import io.hamlook.aetheria.utils.ColorUtils;
import io.hamlook.aetheria.utils.item.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterEvents
public class PriceDetector {

    public static boolean scanning = false;

    private static final Map<String, List<ItemPrice>> priceMap = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final String MOD_SECRET = "a7c0e73c-3b0b-4789-8c80-741dd09ba1bc";
    private static final long DEDUP_INTERVAL_MS = 120_000;
    private static final long REPARSE_COOLDOWN_MS = 1_000;

    private static int tickCounter = 0;
    private static long lastParseTime = 0;
    private static long lastFetchTime = 0;
    private static boolean initialised = false;
    private static int sendIntervalTicks;
    private static long fetchIntervalMs;

    private static boolean shouldAdd(String itemID) {
        List<ItemPrice> existing = priceMap.get(itemID);
        if (existing == null || existing.isEmpty()) return true;
        ItemPrice last = existing.get(existing.size() - 1);
        return System.currentTimeMillis() - last.timestamp >= DEDUP_INTERVAL_MS;
    }

    public static double parseRawDouble(String raw) {
        String s = raw.trim().replace(",", "");
        if (s.isEmpty()) return 0.0;
        char suffix = Character.toUpperCase(s.charAt(s.length() - 1));
        double multiplier = 1.0;
        if (suffix == 'K') { multiplier = 1_000.0; s = s.substring(0, s.length() - 1); }
        else if (suffix == 'M') { multiplier = 1_000_000.0; s = s.substring(0, s.length() - 1); }
        else if (suffix == 'B') { multiplier = 1_000_000_000.0; s = s.substring(0, s.length() - 1); }
        return Double.parseDouble(s) * multiplier;
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        scanning = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!initialised) {
            initialised = true;
            sendIntervalTicks = (int) (OtherDataAPI.getPriceUploadInterval() / 50);
            fetchIntervalMs = OtherDataAPI.getPriceFetchInterval();
            lastFetchTime = System.currentTimeMillis();
            PriceMap.fetch();
        }

        tickCounter++;
        if (tickCounter >= sendIntervalTicks) {
            tickCounter = 0;
            sendPrices();
        }

        long now = System.currentTimeMillis();
        if (now - lastFetchTime >= fetchIntervalMs) {
            lastFetchTime = now;
            PriceMap.fetch();
        }
    }

    public static void sendNow() {
        tickCounter = 0;
        sendPrices();
    }

    private static void sendPrices() {
        if (priceMap.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        String json = gson.toJson(priceMap);

        if (mc.thePlayer != null) {
            Aetheria.logger.info("Sending " + priceMap.size() + " item price entries to API");
        }

        new Thread(() -> {
            try {
                URL url = new URL(CapeAPI.getAPIUrl("upload-price"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-mod-secret", MOD_SECRET);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                Aetheria.logger.info("Sent all prices, response: " + responseCode);
            } catch (Exception e) {
                Aetheria.logger.info("Failed to send prices: " + e.getMessage());
            }
        }).start();

        priceMap.clear();
    }

    @SubscribeEvent
    public void onDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (scanning) return;
        long now = System.currentTimeMillis();
        if (now - lastParseTime < REPARSE_COOLDOWN_MS) return;

        if (!(event.gui instanceof GuiContainer)) return;
        GuiContainer gui = (GuiContainer) event.gui;
        if (!(gui.inventorySlots instanceof ContainerChest)) return;
        ContainerChest chest = (ContainerChest) gui.inventorySlots;
        String title = ColorUtils.stripColor(chest.getLowerChestInventory().getName());

        if (title.contains("Auction Browser")) {
            scanning = true;
            parseAuctionHouse(chest);
            return;
        }

        boolean result = parseBZMenus(chest);
        scanning = result;
        lastParseTime = now;
    }

    private static void addToPriceMap(List<ItemPrice> itemPrices) {
        int added = 0;
        for (ItemPrice ip : itemPrices) {
            if (!shouldAdd(ip.itemID)) continue;
            priceMap.computeIfAbsent(ip.itemID, k -> new ArrayList<>()).add(ip);
            added++;
        }
        Aetheria.logger.info("Added " + added + "/" + itemPrices.size() + " items to priceMap");
    }

    private static String extractPrice(String s) {
        return s.replaceAll("[^0-9.]", "");
    }

    private static String parseOrderPrice(List<String> lore, String sectionHeader) {
        for (String s : lore) {
            if (s.equals("This item does not support")) return null;
            if (s.startsWith(sectionHeader)) {
                int index = lore.indexOf(s) + 1;
                if (index >= lore.size()) return "";
                String line = lore.get(index);
                if (line.startsWith("-")) {
                    String pricePart = line.contains("|") ? line.substring(0, line.indexOf("|")).trim() : line;
                    String priceS = pricePart.replaceAll("[^0-9.]", "");
                    return priceS.isEmpty() ? "" : priceS;
                }
            }
        }
        return "";
    }

    public static boolean parseBZMenus(ContainerChest chest) {
        List<ItemPrice> itemPrices = new ArrayList<>();
        String title = chest.getLowerChestInventory().getName();

        if (title.contains("\u279c")) {
            int chestSlots = chest.getLowerChestInventory().getSizeInventory();
            for (int i = 0; i < chestSlots; i++) {
                Slot slot = chest.getSlot(i);
                if (slot == null || !slot.getHasStack()) continue;
                ItemStack stack = slot.getStack();
                if (!ItemUtils.isSkyblockItem(stack)) continue;

                List<String> lore = ProfileParser.getLore(stack);
                String buyPrice = "", sellPrice = "";

                for (String s : lore) {
                    if (s.startsWith("Buy price:")) buyPrice = extractPrice(s);
                    if (s.startsWith("Sell price:")) sellPrice = extractPrice(s);
                }
                if (buyPrice.isEmpty() || sellPrice.isEmpty()) continue;

                String internalName = ItemUtils.getInternalName(stack);
                itemPrices.add(new ItemPrice(internalName, new Price(parseRawDouble(buyPrice), parseRawDouble(sellPrice), -1, -1), PriceType.BAZAAR));
            }
        } else {
            Slot slot = chest.getSlot(13);
            if (slot == null || !slot.getHasStack()) return false;
            ItemStack stack = slot.getStack();
            if (!ItemUtils.isSkyblockItem(stack)) return false;

            String internalName = ItemUtils.getInternalName(stack);

            Slot iBuy = chest.getSlot(10);
            Slot iSell = chest.getSlot(11);
            Slot buyOffer = chest.getSlot(15);
            Slot sellOffer = chest.getSlot(16);
            if (buyOffer == null || sellOffer == null || !buyOffer.getHasStack() || !sellOffer.getHasStack()) return false;
            if (iBuy == null || iSell == null || !iBuy.getHasStack() || !iSell.getHasStack()) return false;

            ItemStack buy = buyOffer.getStack();
            ItemStack sell = sellOffer.getStack();
            ItemStack iBuyStack = iBuy.getStack();
            ItemStack iSellStack = iSell.getStack();
            if (buy == null || sell == null || iBuyStack == null || iSellStack == null) return false;

            List<String> oBuyLore = ProfileParser.getLore(buy);
            String oBuyPrice = parseOrderPrice(oBuyLore, "Top Orders");
            if (oBuyPrice == null) return false;

            List<String> oSellLore = ProfileParser.getLore(sell);
            String oSellPrice = parseOrderPrice(oSellLore, "Top Offers");
            if (oSellPrice == null) return false;

            String iBuyPrice = "", iSellPrice = "";
            for (String s : ProfileParser.getLore(iBuyStack)) {
                if (s.startsWith("Price per unit:") || s.startsWith("Price pet unit:")) iBuyPrice = extractPrice(s);
            }
            for (String s : ProfileParser.getLore(iSellStack)) {
                if (s.startsWith("Price per unit:") || s.startsWith("Price pet unit:")) iSellPrice = extractPrice(s);
            }
            if (iBuyPrice.isEmpty() || iSellPrice.isEmpty()) return false;
            if (oBuyPrice.isEmpty() && oSellPrice.isEmpty()) return false;

            itemPrices.add(new ItemPrice(internalName, new Price(parseRawDouble(iBuyPrice), parseRawDouble(iSellPrice), parseRawDouble(oBuyPrice), parseRawDouble(oSellPrice)), PriceType.BZ_WITH_OFFER));
        }

        if (!itemPrices.isEmpty()) addToPriceMap(itemPrices);
        return !itemPrices.isEmpty();
    }

    public static void parseAuctionHouse(ContainerChest chest) {
        List<ItemPrice> itemPrices = new ArrayList<>();
        int totalSlots = chest.getInventory().size();

        for (int i = 0; i < totalSlots; i++) {
            Slot slot = chest.getSlot(i);
            if (slot == null || !slot.getHasStack()) continue;
            ItemStack stack = slot.getStack();
            if (!ItemUtils.isSkyblockItem(stack)) continue;

            List<String> lore = ProfileParser.getLore(stack);
            String price = "";
            PriceType type = PriceType.AUCTION;
            String internalName = ItemUtils.getInternalName(stack);

            for (String s : lore) {
                if (s.startsWith("Buy it now:")) {
                    price = extractPrice(s);
                    type = PriceType.BIN;
                }
                if (s.startsWith("Starting bid:") || s.startsWith("Top bid:")) {
                    price = extractPrice(s);
                }
            }
            if (price.isEmpty()) continue;

            itemPrices.add(new ItemPrice(internalName, new Price(parseRawDouble(price), parseRawDouble(price), -1, -1), type));
        }

        if (!itemPrices.isEmpty()) addToPriceMap(itemPrices);
    }

}

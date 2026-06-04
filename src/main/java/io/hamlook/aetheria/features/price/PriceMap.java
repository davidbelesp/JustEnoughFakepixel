package io.hamlook.aetheria.features.price;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.hamlook.aetheria.Aetheria;
import io.hamlook.aetheria.repo.CapeAPI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceMap {

    private static final Gson gson = new Gson();
    private static final Map<String, List<ItemPrice>> priceMap = new HashMap<>();

    public static Map<String, List<ItemPrice>> getPriceMap() {
        return priceMap;
    }

    public static void fetch() {
        new Thread(() -> {
            try {
                URL url = new URL(CapeAPI.getAPIUrl("price"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        Type type = new TypeToken<Map<String, List<ItemPrice>>>() {}.getType();
                        Map<String, List<ItemPrice>> fetched = gson.fromJson(sb.toString(), type);
                        if (fetched != null) {
                            synchronized (priceMap) {
                                priceMap.clear();
                                priceMap.putAll(fetched);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Aetheria.logger.info("[PriceDetector] Failed to fetch prices: " + e.getMessage());
            }
        }).start();
    }

}

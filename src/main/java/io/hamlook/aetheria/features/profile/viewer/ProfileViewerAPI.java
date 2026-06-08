package io.hamlook.aetheria.features.profile.viewer;

import com.google.gson.*;
import io.hamlook.aetheria.Aetheria;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewerAPI {

    public static ConcurrentHashMap<String, Long> lastFetches = new ConcurrentHashMap<>();
    public static HashMap<String, PlayerProfile> profileHashMap = new HashMap<>();
    public static List<String> cachedPlayerList = new ArrayList<>();

    public static final long FETCH_INTERVAL = 1800000;
    public static final String MOD_SECRET = "a7c0e73c-3b0b-4789-8c80-741dd09ba1bc";

    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(EnumMap.class, (JsonDeserializer<EnumMap<?, ?>>) (json, typeOfT, context) -> {
                if (!(typeOfT instanceof ParameterizedType)) return null;

                Type[] typeArguments = ((ParameterizedType) typeOfT).getActualTypeArguments();
                Class enumClass = (Class) typeArguments[0];
                EnumMap map = new EnumMap(enumClass);

                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                    try {
                        Enum key = Enum.valueOf(enumClass, entry.getKey());
                        Object value = context.deserialize(entry.getValue(), typeArguments[1]);
                        map.put(key, value);
                    } catch (IllegalArgumentException e) {
                        Aetheria.logger.info("Skipped unknown enum key from API: " + entry.getKey());
                    }
                }
                return map;
            })
            .create();

    private static final ExecutorService networkExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ATHR-ProfileViewerAPI");
        t.setDaemon(true);
        return t;
    });

    public static void fetchPlayerListAsync() {
        if (!cachedPlayerList.isEmpty()) return;
        networkExecutor.execute(() -> {
            try {
                URL url = new URL("https://capeapi.qzz.io/game/players");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
                conn.setRequestProperty("x-mod-secret", MOD_SECRET);
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    String json = readResponse(conn);
                    String[] players = gson.fromJson(json, String[].class);
                    if (players != null) {
                        cachedPlayerList = Arrays.asList(players);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static PlayerProfile getData(String user) {
        fetchFromAPI(user);
        return profileHashMap.getOrDefault(user, null);
    }

    public static void fetchFromAPI(String username) {
        if (System.currentTimeMillis() - lastFetches.getOrDefault(username, 0L) <= FETCH_INTERVAL) return;
        lastFetches.put(username, System.currentTimeMillis());
        Aetheria.logger.info("Profile fetch disabled for username-based remote requests: " + username);
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString().trim();
        }
    }
}

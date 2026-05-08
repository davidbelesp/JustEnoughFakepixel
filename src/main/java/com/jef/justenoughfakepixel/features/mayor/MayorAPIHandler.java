package com.jef.justenoughfakepixel.features.mayor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.features.capes.CapeManager;
import com.jef.justenoughfakepixel.repo.CapeAPI;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MayorAPIHandler {

    public static Gson GSON = new GsonBuilder().create();

    public static boolean sendElectionData(ElectionData electionData) {
        if (electionData.isNull()) return false;
        try {

            URL url = URI.create(CapeAPI.getAPIUrl("set_election")).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("x-mod-secret", CapeManager.MOD_SECRET);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try(OutputStream os = conn.getOutputStream()) {
                os.write(GSON.toJson(electionData).getBytes(StandardCharsets.UTF_8));
            }

            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            JefMod.logger.info("Error While Sending Election Data: " + e.getMessage());
            return false;
        }
    }

    public static boolean sendMayorData(String mayor){
        if(mayor == null || mayor.isEmpty()) return false;
        try {

            URL url = URI.create(CapeAPI.getAPIUrl("set_election")).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("x-mod-secret", CapeManager.MOD_SECRET);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            String body = "{\"current\": \"" + mayor + "\"}";
            try(OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            JefMod.logger.info("Error While Sending Mayor Data: " + e.getMessage());
            return false;
        }
    }

}

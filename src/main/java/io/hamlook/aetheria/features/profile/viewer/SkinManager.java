package io.hamlook.aetheria.features.profile.viewer;

import io.hamlook.aetheria.core.ATHRConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SkinManager {

    private static final File SKIN_DIR = new File(ATHRConfig.configDirectory, "cachedSkins");
    private static final Map<String, ResourceLocation> loadedSkins = new ConcurrentHashMap<>();
    private static final Set<String> fetching = new HashSet<>();
    private static final Set<String> sessionUpdated = new HashSet<>();

    public static ResourceLocation getSkin(String username) {
        if (loadedSkins.containsKey(username)) {
            return loadedSkins.get(username);
        }

        if (!fetching.contains(username)) {
            fetching.add(username);
            fetchSkinAsync(username);
        }

        return DefaultPlayerSkin.getDefaultSkinLegacy();
    }

    private static void fetchSkinAsync(String username) {
        new Thread(() -> {
            try {
                if (!SKIN_DIR.exists()) {
                    SKIN_DIR.mkdirs();
                }

                File skinFile = new File(SKIN_DIR, username + ".png");

                if (!skinFile.exists() || !sessionUpdated.contains(username)) {
                    sessionUpdated.add(username);
                }

                if (skinFile.exists()) {
                    BufferedImage finalImg = ImageIO.read(skinFile);
                    if (finalImg != null) {
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            DynamicTexture dynTex = new DynamicTexture(finalImg);
                            ResourceLocation loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("skin_" + username, dynTex);
                            loadedSkins.put(username, loc);
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (!loadedSkins.containsKey(username)) {
                    fetching.remove(username);
                }
            }
        }, "ATHR-SkinFetcher-" + username).start();
    }
}

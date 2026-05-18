package com.jef.justenoughfakepixel.features.profile.viewer.ui;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.gui.GuiTextures;
import com.jef.justenoughfakepixel.features.profile.data.ProfileData;
import com.jef.justenoughfakepixel.features.profile.viewer.PlayerProfile;
import com.jef.justenoughfakepixel.features.profile.viewer.ProfileViewerAPI;
import com.jef.justenoughfakepixel.features.profile.viewer.ui.modules.PVButton;
import com.jef.justenoughfakepixel.features.profile.viewer.ui.modules.PlayerModule;
import com.jef.justenoughfakepixel.features.profile.viewer.ui.util.StringDrawer;
import com.jef.justenoughfakepixel.utils.render.NineSliceUtils;
import com.jef.justenoughfakepixel.utils.render.ResolutionUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ProfileViewerGUI extends GuiScreen {

    // UI Data
    public static ResourceLocation CONTAINER_BG = GuiTextures.CAPES_UI;
    public static float uiScale = 1f;
    private static int tab = 0;
    private int boxW;
    private int boxH;
    private int boxX;
    private int boxY;


    // Player Data
    public String username;
    public int profileIndex = 0;
    public PlayerProfile playerProfile;
    public ProfileData activeProfileData;

    // State Trackers
    public boolean isFetching = true;
    public boolean hasError = false;

    // Buttons
    public PVButton profileButton;
    public ProfileViewerGUI(String username) {
        this.username = username;
        uiScale = JefConfig.feature.overlays.profileViewer.pvScale* ResolutionUtils.getXStatic(1);

        new Thread(() -> {
            try {
                if (ProfileViewerAPI.profileHashMap.containsKey(username)) {
                    this.playerProfile = ProfileViewerAPI.profileHashMap.get(username);
                } else {
                    this.playerProfile = ProfileViewerAPI.fetchUser(username);
                    if (this.playerProfile != null) {
                        ProfileViewerAPI.profileHashMap.put(username, this.playerProfile);
                    }
                }

                if (this.playerProfile != null && this.playerProfile.profiles != null && !this.playerProfile.profiles.isEmpty()) {
                    this.activeProfileData = this.playerProfile.profiles.get(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.hasError = true;
            } finally {
                this.isFetching = false;
            }
        }, "JEF-GUI-FetchThread").start();
    }

    @Override
    public void initGui() {
        super.initGui();
        CONTAINER_BG = GuiTextures.storageBackground(3);
        uiScale = JefConfig.feature.overlays.profileViewer.pvScale * ResolutionUtils.getXStatic(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int maxWidth = (int)(this.width * 0.9f);
        boxW = Math.min(maxWidth, getScaled(900));
        boxH = (int)(boxW * 0.62f);
        boxX = (this.width / 2) - (boxW / 2);
        boxY = (this.height / 2) - (boxH / 2);


        int centerX = boxX + (boxW / 2);
        int centerY = boxY + (boxH / 2);

        if (isFetching) {
            NineSliceUtils.draw(CONTAINER_BG, boxX, boxY, boxW, boxH, 6, 18);
            String text = "Fetching data...";
            int textWidth = fontRendererObj.getStringWidth(text);
            drawString(fontRendererObj, text, centerX - (textWidth / 2), centerY - (fontRendererObj.FONT_HEIGHT / 2), 0xFFFFAA00); // Yellow/Orange

        } else if (hasError) {
            NineSliceUtils.draw(CONTAINER_BG, boxX, boxY, boxW, boxH, 6, 18);
            String text = "An error occurred while fetching!";
            int textWidth = fontRendererObj.getStringWidth(text);
            drawString(fontRendererObj, text, centerX - (textWidth / 2), centerY - (fontRendererObj.FONT_HEIGHT / 2), 0xFFFF5555); // Light Red

        } else if (this.playerProfile == null) {
            NineSliceUtils.draw(CONTAINER_BG, boxX, boxY, boxW, boxH, 6, 18);
            String text = this.username + " (Not In Database)";
            int textWidth = fontRendererObj.getStringWidth(text);
            drawString(fontRendererObj, text, centerX - (textWidth / 2), centerY - (fontRendererObj.FONT_HEIGHT / 2), 0xFFAAAAAA); // Gray

        } else {
            int leftBoxWidth = drawBasicBG();
            int scale = getScaled(200);
            int playerX = boxX + (leftBoxWidth / 2);
            int playerY = (int) (boxY + boxH * 0.95f);
            PlayerModule.draw(playerX, playerY, scale, this.username, mouseX, mouseY);

            int profileX =  boxX + (leftBoxWidth / 2);
            int profileY =  boxY - getScaled(40);
            int profileW =  getScaled(240);
            int profileH =  getScaled(35);
            String profile = "§aProfile: §f" + this.activeProfileData.baseData.playerProfile;
            if(profileButton == null){
                profileButton = new PVButton(0,profileX,profileY,profileW,profileH,profile);
                this.buttonList.add(profileButton);
            }else{
                profileButton.xPosition = profileX;
                profileButton.yPosition = profileY;
                profileButton.width = profileW;
                profileButton.height = profileH;
                profileButton.displayString = profile;
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button.id == profileButton.id){
            profileIndex++;
            if(profileIndex >= playerProfile.profiles.size()){
                profileIndex = 0;
            }
            this.activeProfileData = this.playerProfile.profiles.get(profileIndex);
        }
    }

    public int drawBasicBG() {
        String name = this.username + " §8(Fetched)";

        String updateTimeText = this.playerProfile.update_time;
        String syncTimeText = this.playerProfile.updated_at;

        Instant updateT = Instant.parse(updateTimeText);
        Instant syncT = Instant.parse(syncTimeText);
        ZoneId targetZone = ZoneId.systemDefault();

        ZonedDateTime localizedUpd = updateT.atZone(targetZone);
        ZonedDateTime localizedSync = syncT.atZone(targetZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm");

        String updateTime = "§8Uploaded On: §f" + formatter.format(localizedUpd);
        String syncTime = "§8Sync: §f" +formatter.format(localizedSync);

        int textWidth = fontRendererObj.getStringWidth(updateTime);
        int nameWidth = fontRendererObj.getStringWidth(name);
        int syncNameWidth = fontRendererObj.getStringWidth(syncTime);
        if(nameWidth > textWidth) textWidth = nameWidth;
        if(syncNameWidth > nameWidth && syncNameWidth > textWidth) textWidth = syncNameWidth;

        float textScale = Math.max(0.25f, getScaledF(1)) * 3f;
        int leftBoxWidth = (int)(textWidth * textScale + getScaledF(20));
        int gap = getScaled(10);
        int totalCombinedWidth = leftBoxWidth + gap + boxW;

        boxX = (this.width / 2) - (totalCombinedWidth / 2);
        int rightBoxX = boxX + leftBoxWidth + gap;

        int textX = boxX + getScaled(10);
        int nameY = boxY + getScaled(15);
        int updateY = boxY + getScaled(50);
        int syncY = boxY + getScaled(80);

        NineSliceUtils.draw(CONTAINER_BG, boxX, boxY, leftBoxWidth, boxH, 6, 18);
        NineSliceUtils.draw(CONTAINER_BG, rightBoxX, boxY, boxW, boxH, 6, 18);

        StringDrawer.drawString(name, textX, nameY, textScale, false);
        StringDrawer.drawString(updateTime, textX, updateY, textScale * 0.75f, false);
        StringDrawer.drawString(syncTime, textX, syncY, textScale * 0.75f, false);

        return leftBoxWidth;
    }

    public static int getScaled(double initial){
        return (int)(initial*uiScale);
    }

    public static float getScaledF(double initial){
        return (float) (initial*uiScale);
    }

}
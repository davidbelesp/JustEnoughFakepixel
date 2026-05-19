package com.jef.justenoughfakepixel.features.uptime;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.overlay.Overlay;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@RegisterEvents
public class UptimeOverlay extends Overlay {

    @Getter
    private static UptimeOverlay instance;

    // Big overlay state
    private boolean showingExpiry = false;
    private int expiryTicksLeft = 0;
    private static final int EXPIRY_DURATION_TICKS = 100; // ~5 seconds

    public UptimeOverlay() {
        super(120, 20);
        instance = this;
    }

    // ── Overlay contract ─────────────────────────────────────────────────────

    @Override
    public Position getPosition() {
        return JefConfig.feature.misc.uptimeConfig.uptimePos;
    }

    @Override
    public float getScale() {
        return JefConfig.feature.misc.uptimeConfig.uptimeScale;
    }

    @Override
    public int getBgColor() {
        return ChromaColour.specialToChromaRGB(JefConfig.feature.misc.uptimeConfig.uptimeBgColor);
    }

    @Override
    public int getCornerRadius() {
        return JefConfig.feature.misc.uptimeConfig.uptimeCornerRadius;
    }

    @Override
    protected boolean isEnabled() {
        return JefConfig.feature != null && JefConfig.feature.misc.uptimeConfig.uptimeEnabled;
    }

    @Override
    public List<String> getLines(boolean preview) {
        List<String> lines = new ArrayList<>();
        if (preview) {
            lines.add("§b§lTimer");
            lines.add("§f" + formatCountdown(3661000L));
            return lines;
        }
        UptimeManager mgr = UptimeManager.getInstance();
        if (!mgr.isRunning()) return lines;

        lines.add("§b§lTimer");
        long rem = mgr.getRemainingMs();
        String color = getCountdownColor(rem, mgr.getTotalDurationMs());
        lines.add(color + formatCountdown(rem));
        return lines;
    }

    // ── Tick: poll expiry and show big screen message ────────────────────────

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;

        UptimeManager mgr = UptimeManager.getInstance();

        // Check if timer just expired
        if (mgr.pollExpired()) {
            showingExpiry = true;
            expiryTicksLeft = EXPIRY_DURATION_TICKS;
        }

        if (showingExpiry) {
            if (expiryTicksLeft-- <= 0) {
                showingExpiry = false;
            }
        }
    }

    // ── Render: big expiry message overlaid on screen ───────────────────────

    @Override
    public void render(boolean preview) {
        // Normal small overlay
        super.render(preview);

        // Big screen-center message
        if (!preview && showingExpiry && isEnabled()) {
            renderExpiryMessage();
        }
    }

    private void renderExpiryMessage() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();

        // Pulsing alpha: fade out in last 40 ticks
        float alpha = expiryTicksLeft > 40
                ? 1f
                : (expiryTicksLeft / 40f);
        int a = (int) (alpha * 255);

        String line1 = "§c§l⏰ TIMER DONE! ⏰";
        String line2 = "§eYour countdown has ended.";

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Dark semi-transparent background panel
        int panelW = 220, panelH = 40;
        int px = (sw - panelW) / 2;
        int py = sh / 3 - panelH / 2;
        int bgColor = (a / 2 << 24) | 0x000000;
        drawRoundedRect(px - 6, py - 6, px + panelW + 6, py + panelH + 6, 6, bgColor);

        // Scale up text (2x)
        GL11.glTranslatef(sw / 2f, sh / 3f, 0);
        GL11.glScalef(2f, 2f, 1f);

        int w1 = mc.fontRendererObj.getStringWidth(line1);
        int w2 = mc.fontRendererObj.getStringWidth(line2);

        // Apply alpha via color arg (ARGB packed)
        int textAlpha = (a << 24) | 0xFFFFFF;
        mc.fontRendererObj.drawStringWithShadow(line1, -w1 / 2f, -12f, textAlpha);
        mc.fontRendererObj.drawStringWithShadow(line2, -w2 / 2f, 2f, textAlpha);

        GL11.glPopMatrix();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String formatCountdown(long ms) {
        long s = ms / 1000;
        long d = s / 86400;
        s %= 86400;
        long h = s / 3600;
        s %= 3600;
        long m = s / 60;
        s %= 60;

        if (d > 0) return String.format("%dd %dh %dm %ds", d, h, m, s);
        if (h > 0) return String.format("%dh %dm %ds", h, m, s);
        if (m > 0) return String.format("%dm %ds", m, s);
        return String.format("%d.%ds", s, (ms % 1000) / 100);
    }

    private static String getCountdownColor(long rem, long total) {
        if (total <= 0) return "§c";
        double pct = (double) rem / total;
        if (pct > 0.5) return "§a";
        if (pct > 0.25) return "§e";
        return "§c";
    }
}

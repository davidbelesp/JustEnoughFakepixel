package com.jef.justenoughfakepixel.features.profile;

import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class GuiWaiter {

    public static final GuiWaiter INSTANCE = new GuiWaiter();
    private final Deque<PendingWait> queue = new ArrayDeque<>();
    private GuiWaiter() {}

    public static void waitFor(String expectedTitle, int tickDelay, int pressSlot,
                               Consumer<ContainerChest> callback) {
        JefMod.logger.info("[GuiWaiter] Queued wait for: '" + expectedTitle + "' (queue size now " + (INSTANCE.queue.size() + 1) + ")");
        INSTANCE.queue.add(new PendingWait(expectedTitle, tickDelay, pressSlot, callback, null, null));
    }

    public static void waitFor(String expectedTitle, int tickDelay, int pressSlot,
                               String returnTitle, Consumer<ContainerChest> callback,
                               Consumer<ContainerChest> onReturn) {
        JefMod.logger.info("[GuiWaiter] Queued wait for: '" + expectedTitle + "' with return to '" + returnTitle + "' (queue size now " + (INSTANCE.queue.size() + 1) + ")");
        INSTANCE.queue.add(new PendingWait(expectedTitle, tickDelay, pressSlot, callback, returnTitle, onReturn));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (queue.isEmpty()) return;

        PendingWait head = queue.peek();

        if (!head.guiReceived) {
            ContainerChest chest = getOpenChest(head.expectedTitle);
            if (chest == null) {
                // Log every 20 ticks (~1 second) so we can see if it's stuck polling
                head.pollTicks++;
                if (head.pollTicks % 20 == 0) {
                    String current = getCurrentTitle();
                    JefMod.logger.info("[GuiWaiter] Still waiting for '" + head.expectedTitle
                            + "' — current screen title: '" + current + "' (" + head.pollTicks + " ticks)");
                }
                return;
            }
            JefMod.logger.info("[GuiWaiter] GUI matched: '" + head.expectedTitle + "' — starting " + head.ticksRemaining + "-tick delay");
            head.container   = chest;
            head.guiReceived = true;
            return;
        }

        if (--head.ticksRemaining > 0) return;

        JefMod.logger.info("[GuiWaiter] Firing callback for: '" + head.expectedTitle + "'");
        queue.poll();
        head.callback.accept(head.container);

        if (head.pressSlot > 0) {
            JefMod.logger.info("[GuiWaiter] Clicking slot " + head.pressSlot + " to navigate away from '" + head.expectedTitle + "'");
            Minecraft mc = Minecraft.getMinecraft();
            mc.playerController.windowClick(
                    head.container.windowId, head.pressSlot, 0, 0, mc.thePlayer
            );
        }

        if (head.returnTitle != null && head.onReturn != null) {
            JefMod.logger.info("[GuiWaiter] Queuing return wait for: '" + head.returnTitle + "'");
            queue.addFirst(new PendingWait(head.returnTitle, 2, -1, head.onReturn, null, null));
        }
    }

    private static String getCurrentTitle() {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) {
            return Minecraft.getMinecraft().currentScreen == null ? "null" : Minecraft.getMinecraft().currentScreen.getClass().getSimpleName();
        }
        Container container = ((GuiContainer) Minecraft.getMinecraft().currentScreen).inventorySlots;
        if (!(container instanceof ContainerChest)) return "(non-chest container)";
        return ColorUtils.stripColor(
                ((ContainerChest) container).getLowerChestInventory()
                        .getDisplayName().getUnformattedText()
        ).trim();
    }

    private static ContainerChest getOpenChest(String expectedTitle) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return null;
        Container container = ((GuiContainer) Minecraft.getMinecraft().currentScreen).inventorySlots;
        if (!(container instanceof ContainerChest)) return null;
        String title = ColorUtils.stripColor(
                ((ContainerChest) container).getLowerChestInventory()
                        .getDisplayName().getUnformattedText()
        ).trim();
        return title.equals(expectedTitle) ? (ContainerChest) container : null;
    }

    private static class PendingWait {
        final String                   expectedTitle;
        final Consumer<ContainerChest> callback;
        final String                   returnTitle;
        final Consumer<ContainerChest> onReturn;
        final int                      pressSlot;
        int                            ticksRemaining;
        int                            pollTicks = 0;
        ContainerChest                 container;
        boolean                        guiReceived = false;

        PendingWait(String expectedTitle, int tickDelay, int pressSlot,
                    Consumer<ContainerChest> callback,
                    String returnTitle, Consumer<ContainerChest> onReturn) {
            this.expectedTitle  = expectedTitle;
            this.ticksRemaining = Math.max(tickDelay, 1);
            this.pressSlot      = pressSlot;
            this.callback       = callback;
            this.returnTitle    = returnTitle;
            this.onReturn       = onReturn;
        }
    }
}
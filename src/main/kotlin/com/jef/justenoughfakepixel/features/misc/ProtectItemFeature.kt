package com.jef.justenoughfakepixel.features.misc

import com.jef.justenoughfakepixel.core.JefConfig
import com.jef.justenoughfakepixel.core.config.gui.GuiTextures
import com.jef.justenoughfakepixel.events.ItemTossEvent
import com.jef.justenoughfakepixel.events.RenderItemOverlayEvent
import com.jef.justenoughfakepixel.events.SlotClickEvent
import com.jef.justenoughfakepixel.init.RegisterEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@RegisterEvents
class ProtectItemFeature {

    private val mc = Minecraft.getMinecraft()

    // ── Drop prevention (hotbar / hand — Q key outside inventory) ───────────
    // Fired by MixinEntityPlayerSP_ItemToss via EntityPlayerSP#dropOneItem.
    // This covers dropping the currently held item when NO container GUI is open.

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onItemDrop(event: ItemTossEvent) {
        val stack = event.item ?: return
        val uuid = ProtectItemCommand.getItemUuid(stack) ?: return
        if (!ProtectedItemStorage.contains(uuid)) return

        event.isCanceled = true
        notifyBlocked(stack.displayName)
    }

    // ── Drop prevention (inventory GUI) ─────────────────────────────────────
    // Fired by MixinGuiContainer_ProtectItem via GuiContainer#handleMouseClick.
    //
    // Case 1 — slotId == -999, clickType != 5:
    //   Player clicked outside the window while holding an item on the cursor.
    //   mc.thePlayer.inventory.itemStack holds the cursor item in this moment.
    //   Matches Skytils' CLICKOUTOFWINDOW protection type.
    //
    // Case 2 — clickType == 4, slotId != -999, slot != null, slot.hasStack:
    //   Player pressed the drop key (Q) while hovering a slot in an inventory GUI.
    //   Matches Skytils' DROPKEYININVENTORY protection type.

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onSlotClick(event: SlotClickEvent) {
        // Case 1: click outside window (throws cursor item)
        if (event.slotId == -999 && event.clickType != 5) {
            val cursorItem = mc.thePlayer?.inventory?.itemStack ?: return
            val uuid = ProtectItemCommand.getItemUuid(cursorItem) ?: return
            if (!ProtectedItemStorage.contains(uuid)) return

            event.isCanceled = true
            notifyBlocked(cursorItem.displayName)
            return
        }

        // Case 2: drop key (Q) pressed on a slot
        if (event.clickType == 4 && event.slotId != -999) {
            val slot = event.slot ?: return
            if (!slot.hasStack) return
            val stack = slot.stack ?: return
            val uuid = ProtectItemCommand.getItemUuid(stack) ?: return
            if (!ProtectedItemStorage.contains(uuid)) return

            event.isCanceled = true
            notifyBlocked(stack.displayName)
        }
    }

    // ── Shared helper ────────────────────────────────────────────────────────

    private fun notifyBlocked(displayName: String) {
        mc.thePlayer?.addChatMessage(
            ChatComponentText(
                "${EnumChatFormatting.RED}[JEF] " +
                        "${EnumChatFormatting.RED}$displayName§c is protected and cannot be dropped!"
            )
        )
    }

    // ── Star overlay on protected items ─────────────────────────────────────

    @SubscribeEvent
    fun onItemOverlay(event: RenderItemOverlayEvent) {
        if (JefConfig.feature?.misc?.protectItem?.showProtectedStar != true) return
        val uuid = ProtectItemCommand.getItemUuid(event.stack) ?: return
        if (!ProtectedItemStorage.contains(uuid)) return

        drawStar(event.x, event.y)
    }

    private fun drawStar(x: Int, y: Int) {
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableDepth()
        mc.textureManager.bindTexture(GuiTextures.PROTECT_ITEM_STAR)
        net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture(
            x, y, 0f, 0f, 16, 16, 16f, 16f
        )
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
    }
}
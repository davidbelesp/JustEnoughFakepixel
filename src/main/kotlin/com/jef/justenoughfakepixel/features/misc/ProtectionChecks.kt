package com.jef.justenoughfakepixel.features.misc

import com.jef.justenoughfakepixel.utils.ColorUtils
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.ContainerChest


object ProtectionChecks {


    fun isDangerousGui(gui: GuiContainer?, container: ContainerChest?): Boolean {
        if (gui !is GuiChest || container == null) return false

        val title = getCleanTitle(container)
        return isDangerousTitle(title)
    }


    fun hasSellItem(container: ContainerChest?): Boolean {
        if (container == null) return false

        val inventory = container.lowerChestInventory
        val size = inventory.sizeInventory

        for (i in 0 until size) {
            val stack = inventory.getStackInSlot(i) ?: continue
            if (!stack.hasDisplayName()) continue

            val displayName = ColorUtils.stripColor(stack.displayName)
            if (displayName == "Sell Item") {
                return true
            }
        }

        return false
    }


    fun shouldBlockMovement(gui: GuiContainer?, container: ContainerChest?): Boolean {
        return isDangerousGui(gui, container) || hasSellItem(container)
    }

    private fun getCleanTitle(container: ContainerChest): String {
        val rawTitle = container.lowerChestInventory.displayName.unformattedText
        return ColorUtils.stripColor(rawTitle).lowercase()
    }


    private fun isDangerousTitle(title: String): Boolean {
        // Auction House
        if (title.contains("auction")) return true

        // Trading menus
        if (title.startsWith("you ") || title.contains("trading")) return true
        if (title.contains("trades") || title.contains("exchange")) return true

        // Salvage menus
        if (title.contains("salvage")) return true

        // Shop menus (but not workshop)
        if (title.contains("shop") && !title.contains("workshop")) return true

        // Bazaar sell offers
        if (title.contains("sell offer") || title.contains("create sell offer")) return true

        return false
    }
}

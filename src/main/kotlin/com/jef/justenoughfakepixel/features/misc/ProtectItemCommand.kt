package com.jef.justenoughfakepixel.features.misc

import com.jef.justenoughfakepixel.core.JefConfig
import com.jef.justenoughfakepixel.core.config.command.SimpleCommand
import com.jef.justenoughfakepixel.init.RegisterCommand
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

@RegisterCommand
class ProtectItemCommand : SimpleCommand() {

    private val mc = Minecraft.getMinecraft()
    private val PREFIX = "${EnumChatFormatting.AQUA}[JEF] ${EnumChatFormatting.RESET}"

    override fun getName() = "jefprotect"
    override fun getUsage() = "/jefprotect [list|clear]"

    override fun execute(sender: ICommandSender, args: Array<String>) {
        val player = mc.thePlayer ?: return

        // Subcommands
        if (args.isNotEmpty()) {
            when (args[0].lowercase()) {
                "list" -> {
                    val uuids = ProtectedItemStorage.protectedUuids
                    if (uuids.isEmpty()) {
                        player.addChatMessage(ChatComponentText("$PREFIX${EnumChatFormatting.YELLOW}No protected items."))
                    } else {
                        player.addChatMessage(ChatComponentText("$PREFIX${EnumChatFormatting.GREEN}Protected items (${uuids.size}):"))
                        uuids.forEach { player.addChatMessage(ChatComponentText("  §7- $it")) }
                    }
                    return
                }
                "clear" -> {
                    ProtectedItemStorage.protectedUuids.clear()
                    ProtectedItemStorage.save()
                    player.addChatMessage(ChatComponentText("$PREFIX${EnumChatFormatting.GREEN}Cleared all protected items."))
                    return
                }
                else -> {
                    player.addChatMessage(ChatComponentText("$PREFIX${EnumChatFormatting.RED}Usage: /jefprotect [list|clear]"))
                    return
                }
            }
        }

        // No args → protect/unprotect held item
        val held = player.heldItem
        if (held == null) {
            player.addChatMessage(ChatComponentText("$PREFIX${EnumChatFormatting.RED}You are not holding an item!"))
            return
        }

        val uuid = getItemUuid(held)
        if (uuid == null) {
            player.addChatMessage(ChatComponentText("$PREFIX${EnumChatFormatting.RED}This item has no SkyBlock UUID and cannot be protected."))
            return
        }

        if (ProtectedItemStorage.contains(uuid)) {
            ProtectedItemStorage.remove(uuid)
            player.addChatMessage(ChatComponentText("$PREFIX${EnumChatFormatting.RED}${held.displayName}§7 is no longer protected."))
        } else {
            ProtectedItemStorage.add(uuid)
            player.addChatMessage(ChatComponentText("$PREFIX${EnumChatFormatting.GREEN}${held.displayName}§7 is now protected!"))
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        if (args.size == 1) return listOf("list", "clear")
        return emptyList()
    }

    companion object {
        /**
         * Returns the SkyBlock item UUID from ExtraAttributes, or null if absent.
         */
        fun getItemUuid(stack: net.minecraft.item.ItemStack?): String? {
            stack ?: return null
            if (!stack.hasTagCompound()) return null
            val extra = stack.tagCompound?.getCompoundTag("ExtraAttributes") ?: return null
            return if (extra.hasKey("uuid")) extra.getString("uuid") else null
        }
    }
}

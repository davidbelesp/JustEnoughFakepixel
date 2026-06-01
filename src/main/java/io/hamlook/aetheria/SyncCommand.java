package io.hamlook.aetheria;

import io.hamlook.aetheria.command.SimpleCommand;
import io.hamlook.aetheria.init.RegisterCommand;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import io.hamlook.aetheria.utils.data.SkyblockData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

@RegisterCommand
public class SyncCommand extends SimpleCommand {

    @Override
    public String getName() {
        return "sync";
    }

    @Override
    public String getUsage() {
        return "/" + getName();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) return;

        if (!SkyblockData.isOnSkyblock()) {
            ChatUtils.sendMessage("§cPlease Join SkyBlock in order to sync, this is to prove that you are not using the username of someone else.");
            return;
        }

        if(System.currentTimeMillis() - lastUse < 240000 && !SYNC_CODE.isEmpty()) {
            IChatComponent text = new ChatComponentText("§a[SkyAtlas] Your sync code is: §e§l" + SYNC_CODE);
            text.setChatStyle(new ChatStyle().setChatClickEvent(
                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, SYNC_CODE)
            ).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ChatComponentText("§aClick to show in chat"))));
            Minecraft.getMinecraft().addScheduledTask(() -> {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(text);
                        ChatUtils.sendMessage(
                                "§r§aPlease paste this code in the §9#sync§a channel on Discord within 5 minutes!");
                    }
            );
            return;
        }

        ChatUtils.sendMessage("§e[SkyAtlas] Sync is disabled in this build because external data submission has been removed.");
    }
}

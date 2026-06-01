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

        ChatUtils.sendMessage("§e[SkyAtlas] Sync is disabled in this build because external data submission has been removed.");
    }
}

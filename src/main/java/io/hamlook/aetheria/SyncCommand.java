package io.hamlook.aetheria;

import io.hamlook.aetheria.command.ASMCommand;
import io.hamlook.aetheria.init.RegisterCommand;
import io.hamlook.aetheria.utils.chat.ChatUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@RegisterCommand
public class SyncCommand extends ASMCommand {

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
        ChatUtils.sendMessage("§e[SkyAtlas] Sync is disabled in this build because outbound username sharing has been removed.");
    }
}

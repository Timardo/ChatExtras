package net.timardo.chatextras;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandListAfk implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ChatExtras.PLAYER_HOLDER.sendAfkList(sender);
        return true;
    }
}

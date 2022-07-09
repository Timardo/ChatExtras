package net.timardo.chatextras;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAfk implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            ChatExtras.playerHolder.toggleAfk(player);
            return true;
        }
        
        sender.sendMessage("This command can only be run by a player.");
        return false;
    }
}

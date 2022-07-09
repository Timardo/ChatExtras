package net.timardo.chatextras;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nametagedit.plugin.NametagEdit;

public class CommandReply implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player)sender;
        
        if (!ChatExtras.playerHolder.hasLastWhisper(player)) {
            sender.sendMessage(ChatColor.RED + "You have no one to reply to :(. Send a message first or receive one from your friend!");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You cannot reply with nothing to say..");
            return true;
        }
        
        Player receiver = Bukkit.getPlayer(ChatExtras.playerHolder.getLastWhisper(player));
        
        if (receiver != null) {
            ChatExtras.playerHolder.setLastWhisper(player, receiver);
            ChatExtras.playerHolder.setLastWhisper(receiver, player);
            
            if (ChatExtras.playerHolder.isAfk(receiver)) {
                sender.sendMessage(ChatColor.GRAY + " * " + ChatColor.RESET + NametagEdit.getApi().getNametag(receiver).getPrefix() + receiver.getDisplayName() + ChatColor.GRAY + " is currently " + ChatColor.YELLOW + "AFK" + ChatColor.GRAY + " and may not see your message.");
            }
        }
        
        player.performCommand("dm " + ChatExtras.playerHolder.getLastWhisper(player) + " " + String.join(" ", args));
        return true;
    }
}

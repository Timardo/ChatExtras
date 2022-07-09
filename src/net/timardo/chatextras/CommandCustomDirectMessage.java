package net.timardo.chatextras;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.api.INametagApi;
import com.nametagedit.plugin.api.data.Nametag;

public class CommandCustomDirectMessage implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "This command can be currently only used by players.");
            return true;
        }
        
        if (args.length <= 1) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /dm <player> <message>");
            return true;
        }
        
        Player sender = (Player) commandSender;
        Player receiver = Bukkit.getPlayer(args[0]);
        
        if (receiver == null) {
            commandSender.sendMessage(ChatColor.RED + "This player is currently unreachable.");
            return true;
        }
        
        INametagApi api = NametagEdit.getApi();
        Nametag senderPrefix = api.getNametag(sender);
        Nametag receiverPrefix = api.getNametag((Player)receiver);
        args[0] = "";
        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
        String onSendingEnd = ChatColor.GRAY + "[me -> " + ChatColor.RESET + receiverPrefix.getPrefix() + receiver.getDisplayName() + ChatColor.GRAY + "]" + ChatColor.RESET + message;
        String onReceivingEnd = ChatColor.GRAY + "[" + ChatColor.RESET + senderPrefix.getPrefix() + sender.getDisplayName() + ChatColor.GRAY + " -> me]" + ChatColor.RESET + message;
        sender.sendMessage(onSendingEnd);
        receiver.sendMessage(onReceivingEnd);
        return true;
    }

}

package net.timardo.chatextras;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.api.INametagApi;

public class PlayerHolder {
    
    private Map<UUID, PlayerData> playerDataMap = new HashMap<UUID, PlayerData>();
    
    public PlayerData getData(Player player) {
        PlayerData data = this.playerDataMap.get(player.getUniqueId());
        
        if (data == null) {
            this.addPlayer(player);
            return this.getData(player);
        }
        
        return data;
    }
    
    public void addPlayer(Player player) {
        this.playerDataMap.put(player.getUniqueId(), new PlayerData());
    }
    
    public void removePlayer(Player player) {
        this.playerDataMap.remove(player.getUniqueId());
    }

    public void setPlayerAFK(Player player, boolean afk) {
        if (!afk) this.getData(player).lastTimeActive = System.currentTimeMillis();
        if (afk == this.getData(player).isAfk) return;
        this.getData(player).isAfk = afk;
        
        INametagApi api = NametagEdit.getApi();
        
        if (afk) {
            String originalPrefix = api.getNametag(player).getPrefix();
            this.getData(player).originalPrefix = originalPrefix;
            api.setPrefix(player, ChatColor.GRAY + "[AFK] " + ChatColor.RESET + originalPrefix);
            Bukkit.broadcastMessage(ChatColor.GRAY + " * " + ChatColor.RESET + originalPrefix + player.getDisplayName() + ChatColor.GRAY + " is AFK.");
        } else if (this.getData(player).originalPrefix != null) {
            api.setPrefix(player, this.getData(player).originalPrefix);
            Bukkit.broadcastMessage(ChatColor.GRAY + " * " + ChatColor.RESET + this.getData(player).originalPrefix + player.getDisplayName() + ChatColor.GRAY + " is no longer AFK.");
        }
    }
    
    public void toggleAfk(Player player) {
        this.setPlayerAFK(player, !this.getData(player).isAfk);
    }

    public boolean hasLastWhisper(Player player) {
        return this.getData(player).hasLastWhisper();
    }

    public String getLastWhisper(Player player) {
        return this.getData(player).lastWhisper;
    }

    public void setLastWhisper(Player sender, Player receiver) {
        this.getData(sender).lastWhisper = receiver.getName();
    }

    public boolean isAfk(Player player) {
        return this.getData(player).isAfk;
    }

    public void checkAfkPlayers() {
        for (Entry<UUID, PlayerData> entry : this.playerDataMap.entrySet()) {
            if (!entry.getValue().isAfk && entry.getValue().lastTimeActive + 300_000L <= System.currentTimeMillis()) {
                this.setPlayerAFK(Bukkit.getPlayer(entry.getKey()), true);
            }
        }
    }
}
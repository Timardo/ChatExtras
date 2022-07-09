package net.timardo.chatextras;

public class PlayerData {
    
    public boolean isAfk;
    public String lastWhisper;
    public String originalPrefix;
    public long lastTimeActive;
    
    public boolean hasLastWhisper() {
        return !(this.lastWhisper == null || this.lastWhisper.isEmpty());
    }
}

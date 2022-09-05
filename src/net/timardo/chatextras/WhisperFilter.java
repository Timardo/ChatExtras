package net.timardo.chatextras;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WhisperFilter extends AbstractFilter {
    
    private static final String[] WHISPER_FILTER = new String[] {"/w ", "/tell ", "/msg ", "/minecraft:w ", "/minecraft:tell ", "/minecraft:msg ", "/dm ", "/chatextras:dm ", "/r ", "/chatextras:r "};
    public static boolean shouldFilter = true;
    @Override
    public Result filter(LogEvent event) {
        if (!shouldFilter) return Result.NEUTRAL; // only filter with the plugin enabled
        
        if (event.getMessage() != null && event.getMessage().getFormattedMessage().contains("issued server command:") && StringUtils.containsAny(event.getMessage().getFormattedMessage(), WHISPER_FILTER)) {
            Player commandSender = getPlayerFromMessage(event.getMessage().getFormattedMessage());
            boolean logToFile = true;
            Result result = Result.NEUTRAL;
            
            try {
                if (commandSender.hasPermission("chat.hidewhisper.all")) {
                    result = Result.DENY;
                    logToFile = false;
                } else if (commandSender.hasPermission("chat.hidewhisper.console")) {
                    result = Result.DENY;
                }
            } catch (NullPointerException e) { } // player not found, ignore permissions and log
            
            if (logToFile) {
                ChatExtras.CHAT_LOGGER.log(commandSender, getCommandFromMessage(event.getMessage().getFormattedMessage()));
            }
            
            return result;
        }
        
        return Result.NEUTRAL;
    }

    private static Player getPlayerFromMessage(String message) {
        String trimmed = message.substring(0, message.indexOf(" issued server command: "));
        String[] split = trimmed.split(" ");
        return Bukkit.getPlayer(split[split.length - 1]);
    }
    
    private String getCommandFromMessage(String message) {
        return message.substring(message.indexOf(" issued server command: ") + " issued server command: ".length());
    }
}

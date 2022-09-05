package net.timardo.chatextras;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.bukkit.entity.Player;

public class ChatLogger {

    private static final String CHAT_LOGS_PATH = "ChatLogs";
    private PrintWriter writer;
    private LocalDate latestDate;
    
    /**
     * Logs a message from a player to the ChatLogs folder in file with current date
     */
    public void log(Player player, String message) {
        this.checkDate();
        
        try {
            this.writer.println(String.format("[%1s] %2s: %3s", LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString(), player.getDisplayName(), message));
        } catch (NullPointerException e) { // NPE can be caused by null Player
            e.printStackTrace(); // TODO: fancy handling?
        }
    }
    
    /**
     * Safely close PrintWriter stream
     */
    public void close() {
        if (this.writer != null) {
            this.writer.close();
        }
    }

    private void checkDate() {
        LocalDate newDate = LocalDate.now();
        
        if (this.latestDate == null || this.latestDate.isBefore(newDate)) {
            this.latestDate = newDate;
            this.close();
            String fileName = this.latestDate.toString() + ".log";
            File logFile = new File(CHAT_LOGS_PATH + File.separator + fileName);
            FileWriter writer = null;
            
            try {
                writer = new FileWriter(logFile, StandardCharsets.UTF_8, true);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            
            this.writer = new PrintWriter(writer, true);
        }
    }
}

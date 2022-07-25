package net.timardo.chatextras;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class ChatExtras extends JavaPlugin {
    
    public static ChatExtras instance;
    private static final String[] WHISPER_FILTER = new String[] {"w ", "tell ", "msg ", "minecraft:w ", "minecraft:tell ", "minecraft:msg ", "dm ", "chatextras:dm ", "/r ", "/chatextras:r "};
    public static PlayerHolder playerHolder = new PlayerHolder();
    
    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands();
        this.registerEventHandlers();
        this.registerLoggerFilter();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> playerHolder.checkAfkPlayers(), 0, 20);
        System.out.println("ChatExtras finished initialization");
    }
    
    private void registerCommands() {
        this.getCommand("afk").setExecutor(new CommandAfk());
        this.getCommand("r").setExecutor(new CommandReply());
        this.getCommand("dm").setExecutor(new CommandCustomDirectMessage());
    }

    @SuppressWarnings("deprecation")
    private void registerEventHandlers() {
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        // God damn it Bukkit, is it really that hard to add handler lists for abstract event superclasses...
        try {
            System.out.println("Registering handlers...");
            ClassPath classPath = ClassPath.from(getClassLoader());
            ImmutableSet<ClassInfo> classSet = classPath.getTopLevelClasses("org.bukkit.event.player");
            
            for (ClassInfo info : classSet) {
                String className = info.getName();
                Class<?> clazz = Class.forName(className);
                
                if (!PlayerEvent.class.isAssignableFrom(clazz)) continue;
                
                
                if (PlayerAdvancementDoneEvent.class.isAssignableFrom(clazz) ||
                        PlayerAnimationEvent.class.isAssignableFrom(clazz) ||
                        PlayerChangedWorldEvent.class.isAssignableFrom(clazz) ||
                        PlayerExpChangeEvent.class.isAssignableFrom(clazz) ||
                        PlayerGameModeChangeEvent.class.isAssignableFrom(clazz) ||
                        PlayerItemDamageEvent.class.isAssignableFrom(clazz) ||
                        PlayerItemMendEvent.class.isAssignableFrom(clazz) ||
                        PlayerKickEvent.class.isAssignableFrom(clazz) ||
                        PlayerLevelChangeEvent.class.isAssignableFrom(clazz) ||
                        PlayerMoveEvent.class.isAssignableFrom(clazz) ||
                        PlayerPickupItemEvent.class.isAssignableFrom(clazz) ||
                        PlayerQuitEvent.class.isAssignableFrom(clazz) ||
                        PlayerRecipeDiscoverEvent.class.isAssignableFrom(clazz) ||
                        PlayerShowEntityEvent.class.isAssignableFrom(clazz) ||
                        PlayerHideEntityEvent.class.isAssignableFrom(clazz) ||
                        PlayerStatisticIncrementEvent.class.isAssignableFrom(clazz) ||
                        PlayerVelocityEvent.class.isAssignableFrom(clazz) ||
                        PlayerCommandPreprocessEvent.class.isAssignableFrom(clazz) ||
                        PlayerChannelEvent.class.isAssignableFrom(clazz) ||
                        PlayerItemBreakEvent.class.isAssignableFrom(clazz)) // process conditionally
                    continue;
                
                try {
                    Method getHandlers = clazz.getMethod("getHandlerList");
                    HandlerList handlerList = (HandlerList) getHandlers.invoke(null);
                    handlerList.register(new RegisteredListener(new Listener() {}, new EventExecutor() {
                        
                        @Override
                        public void execute(Listener paramListener, Event paramEvent) throws EventException {
                            ChatExtras.playerHolder.setPlayerAFK(((PlayerEvent)paramEvent).getPlayer(), false);
                        }
                    }, EventPriority.LOWEST, this, true));
                } catch (NoSuchMethodException e) {
                    // ignore
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    private void registerLoggerFilter() {
        ((Logger)LogManager.getRootLogger()).addFilter(new AbstractFilter() {
            
            @Override
            public org.apache.logging.log4j.core.Filter.Result filter(org.apache.logging.log4j.core.LogEvent event) {
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
                    
                    if (logToFile) logToFile(event.getMessage().getFormattedMessage());
                    
                    return result;
                }
                
                return Result.NEUTRAL;
            };
            
            private void logToFile(String formattedMessage) {
                // TODO
            }

            private static Player getPlayerFromMessage(String message) {
                String trimmed = message.substring(0, message.indexOf(" issued server command: "));
                String[] split = trimmed.split(" ");
                return Bukkit.getPlayer(split[split.length - 1]);
            }
        });
    }
}

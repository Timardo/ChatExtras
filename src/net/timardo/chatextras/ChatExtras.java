package net.timardo.chatextras;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
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
    
    public static final PlayerHolder PLAYER_HOLDER = new PlayerHolder();
    public static final ChatLogger CHAT_LOGGER = new ChatLogger();
    public static ChatExtras instance;
    
    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands();
        this.registerEventHandlers();
        this.registerLoggerFilter();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> PLAYER_HOLDER.checkAfkPlayers(), 0, 20);
        System.out.println("ChatExtras finished initialization");
    }
    
    @Override
    public void onDisable() {
        WhisperFilter.shouldFilter = false; // disable this instance of WhisperFilter
        CHAT_LOGGER.close();
    }
    
    private void registerCommands() {
        this.getCommand("afk").setExecutor(new CommandAfk());
        this.getCommand("r").setExecutor(new CommandReply());
        this.getCommand("dm").setExecutor(new CommandCustomDirectMessage());
        this.getCommand("listafk").setExecutor(new CommandListAfk());
    }

    /**
     * Method registers handlers for all events that should cancel AFK status of a player. Because Bukkit
     * has really stupid way of registering handlers this hacky solution is the cleanest way of not creating 40
     * separate methods for each event
     */
    @SuppressWarnings("deprecation")
    private void registerEventHandlers() {
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        // God damn it Bukkit, is it really that hard to add handler lists for abstract event superclasses...
        try {
            System.out.println("Registering handlers...");
            ClassPath classPath = ClassPath.from(getClassLoader());
            ImmutableSet<ClassInfo> classSet = classPath.getTopLevelClasses("org.bukkit.event.player"); // get all player event classes
            
            for (ClassInfo info : classSet) {
                String className = info.getName();
                Class<?> clazz = Class.forName(className);
                // filter all events that should not cancel player's AFK status
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
                        PlayerItemBreakEvent.class.isAssignableFrom(clazz) ||
                        !PlayerEvent.class.isAssignableFrom(clazz)) // safety check
                    continue;
                
                try {
                    Method getHandlers = clazz.getMethod("getHandlerList");
                    HandlerList handlerList = (HandlerList) getHandlers.invoke(null);
                    handlerList.register(new RegisteredListener(new Listener() {}, new EventExecutor() {
                        
                        @Override
                        public void execute(Listener paramListener, Event paramEvent) throws EventException {
                            ChatExtras.PLAYER_HOLDER.setPlayerAFK(((PlayerEvent)paramEvent).getPlayer(), false);
                        }
                    }, EventPriority.LOWEST, this, true));
                } catch (NoSuchMethodException e) {
                    // ignore
                }
            }
        } catch (IOException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * The JVM still holds the old/original version of WhisperFilter instance and all classes that it refers to after unloading this plugin
     * The WhisperFilter instance stays active until the server stops
     * The shouldFilter boolean is set to false on plugin disable and turns this WhisperFilter instance off
     * Too much reloads (1000+) of this plugin may cause slightly higher RAM usage as GC cannot remove the old instance of WhisperFilter
     */
    private void registerLoggerFilter() {
        ((Logger)LogManager.getRootLogger()).addFilter(new WhisperFilter());
        WhisperFilter.shouldFilter = true;
    }
}

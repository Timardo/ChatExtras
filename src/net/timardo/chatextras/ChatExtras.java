package net.timardo.chatextras;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class ChatExtras extends JavaPlugin {
    
    private static ChatExtras instance;
    public static PlayerHolder playerHolder = new PlayerHolder();
    
    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        this.getCommand("afk").setExecutor(new CommandAfk());
        this.getCommand("r").setExecutor(new CommandReply());
        this.getCommand("dm").setExecutor(new CommandCustomDirectMessage());
        
        // God damn it Bukkit, is it really that hard to add handler lists for abstract event superclasses...
        try {
            System.out.println("Registering handlers...");
            ClassPath classPath = ClassPath.from(getClassLoader());
            ImmutableSet<ClassInfo> classSet = classPath.getTopLevelClasses("org.bukkit.event.player");
            
            for (ClassInfo info : classSet) {
                String className = info.getName();
                Class<?> clazz = Class.forName(className);
                
                if (!PlayerEvent.class.isAssignableFrom(clazz)) continue;
                
                if (clazz.equals(PlayerAdvancementDoneEvent.class) ||
                        clazz.equals(PlayerAnimationEvent.class) ||
                        clazz.equals(PlayerChangedWorldEvent.class) ||
                        clazz.equals(PlayerExpChangeEvent.class) ||
                        clazz.equals(PlayerGameModeChangeEvent.class) ||
                        clazz.equals(PlayerItemDamageEvent.class) ||
                        clazz.equals(PlayerItemMendEvent.class) ||
                        clazz.equals(PlayerKickEvent.class) ||
                        clazz.equals(PlayerLevelChangeEvent.class) ||
                        clazz.equals(PlayerMoveEvent.class) ||
                        clazz.equals(PlayerPickupItemEvent.class) ||
                        clazz.equals(PlayerQuitEvent.class) ||
                        clazz.equals(PlayerRecipeDiscoverEvent.class) ||
                        clazz.equals(PlayerShowEntityEvent.class) ||
                        clazz.equals(PlayerHideEntityEvent.class) ||
                        clazz.equals(PlayerStatisticIncrementEvent.class) ||
                        clazz.equals(PlayerVelocityEvent.class) || 
                        clazz.equals(PlayerCommandPreprocessEvent.class)) // process conditionally
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
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> playerHolder.checkAfkPlayers(), 0, 20);
        
        System.out.println("ChatExtras finished initialization");
    }

    public static Plugin getInstance() {
        return instance;
    }
}

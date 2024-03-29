package net.timardo.chatextras;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import com.nametagedit.plugin.NametagEdit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.protocol.game.PacketPlayInFlying;

public class EventListener implements Listener {
    
    private static final String[] WHISPER_COMMANDS = new String[] {"w ", "tell ", "msg ", "minecraft:w ", "minecraft:tell ", "minecraft:msg ", "dm ", "chatextras:dm "};

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
        ChannelPipeline pipeline = ((CraftPlayer)player).getHandle().b.b.m.pipeline();
        pipeline.addBefore("packet_handler", "ChatExtras:" + player.getUniqueId().toString(), new ChannelDuplexHandler() {
            
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof PacketPlayInFlying packet && packet.d()) { // hasRotation
                    ChatExtras.PLAYER_HOLDER.setPlayerAFK(player, false);
                }
                
                super.channelRead(ctx, msg);
            }
        });
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        Player player = quitEvent.getPlayer();
        Channel channel = ((CraftPlayer)player).getHandle().b.b.m;
        channel.eventLoop().submit(() -> channel.pipeline().remove("ChatExtras:" + player.getUniqueId().toString()));
        ChatExtras.PLAYER_HOLDER.removePlayer(player);
    }
    
    @EventHandler
    public void onCommandEvent(PlayerCommandPreprocessEvent commandEvent) {
        String fullMessage = commandEvent.getMessage().substring(1);
        
        if (!((fullMessage.startsWith("afk") && fullMessage.length() == 3) || fullMessage.startsWith("afk ") || fullMessage.startsWith("chatextras:afk"))) {
            ChatExtras.PLAYER_HOLDER.setPlayerAFK(commandEvent.getPlayer(), false);
        }
        
        if (StringUtils.startsWithAny(fullMessage, WHISPER_COMMANDS)) { // a whisper command
            Player sender = commandEvent.getPlayer();
            String[] args = fullMessage.split(" ", 3);
            
            if (args.length <= 2) return; // only register last whispers for commands that actually send a message
            
            Player receiver = Bukkit.getPlayer(args[1]);
            
            if (receiver != null) {
                ChatExtras.PLAYER_HOLDER.setLastWhisper(sender, receiver);
                ChatExtras.PLAYER_HOLDER.setLastWhisper(receiver, sender);
                
                if (ChatExtras.PLAYER_HOLDER.isAfk(receiver)) {
                    sender.sendMessage(ChatColor.GRAY + " * " + ChatColor.RESET + NametagEdit.getApi().getNametag(receiver).getPrefix() + receiver.getDisplayName() + ChatColor.GRAY + " is currently " + ChatColor.YELLOW + "AFK" + ChatColor.GRAY + " and may not see your message.");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR) // make priority as high as possible to check if the chat event was cancelled and therefore should not be logged
    public void onChat(AsyncPlayerChatEvent chatEvent) {
        if (chatEvent.isCancelled()) return;
        ChatExtras.CHAT_LOGGER.log(chatEvent.getPlayer(), chatEvent.getMessage());
    }
}

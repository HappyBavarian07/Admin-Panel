package dte.hooksystem.utils;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dte.hooksystem.HookSystem;

public class OnlineOperators implements Listener
{
	//Container of static methods
	private OnlineOperators(){}

	private static Set<Player> onlineOPs;

	static
	{
		onlineOPs = Bukkit.getOperators().stream()
				.filter(OfflinePlayer::isOnline)
				.map(OfflinePlayer::getPlayer)
				.collect(toSet());
		
		HookSystem.getInstance().registerListeners(new OnlineOperators());
	}
	
	public static Set<Player> get()
	{
		return new HashSet<>(onlineOPs);
	}
	
	@EventHandler
	public void registerOnJoin(PlayerJoinEvent event) 
	{
		ifOperatorEvent(event, onlineOPs::add);
	}

	@EventHandler
	public void deregisterOnLeave(PlayerQuitEvent event) 
	{
		ifOperatorEvent(event, onlineOPs::remove);
	}
	
	private void ifOperatorEvent(PlayerEvent event, Consumer<Player> operatorAction) 
	{
		Player player = event.getPlayer();

		if(player.isOp())
			operatorAction.accept(player);
	}
}
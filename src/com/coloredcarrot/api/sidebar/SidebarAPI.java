package com.coloredcarrot.api.sidebar;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.List;

/**
 * All rights reserved.
 * @version 2.8
 * @author ColoredCarrot
 */
public class SidebarAPI
extends JavaPlugin
{
	
	private static SidebarAPI instance;
	private static String version;
	private static PlaceholderAPIPlugin placeholderAPI;
	private static List<Sidebar> sidebars = new ArrayList<Sidebar>();

	@Override
	public void onEnable()
	{
		
		instance = this;
		version = getDescription().getVersion();
		
		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
		{
			
			try { placeholderAPI = (PlaceholderAPIPlugin) getServer().getPluginManager().getPlugin("PlaceholderAPI"); }
			catch (ClassCastException | NullPointerException e) {  }
			
			if (placeholderAPI != null)
			{
				getLogger().info("Hooked PlaceholderAPI v" + placeholderAPI.getDescription().getVersion());
				new SidebarPlaceholders().hook();
			}
			
		}
		else
			placeholderAPI = null;
		
		Config.load();
		
		Updater.checkForUpdate(Config.getBoolean_updater_autoDownload());
		
		getLogger().info("Enabled SidebarAPI v" + version);
		
	}
	
	@Override
	public void onDisable()
	{
		
		getLogger().info("Disabled SidebarAPI v" + version);
		
	}
	
	/**
	 * Gets the instance of this plugin.
	 * @return the instance
	 */
	public static SidebarAPI getInstance()
	{
		return instance;
	}
	
	/**
	 * Gets the version of this plugin. E.g.: 2.9.0
	 * @return the version
	 */
	public static String getVersion()
	{
		return version;
	}
	
	protected static void registerSidebar(Sidebar sidebar)
	{
		sidebars.add(sidebar);
	}
	
	protected static void unregisterSidebar(Sidebar sidebar)
	{
		sidebars.remove(sidebar);
	}
	
	/**
	 * Gets the PlaceholderAPIPlugin instance.
	 * @return (PlaceholderAPIPlugin) - the instance of null if the plugin isn't hooked.
	 * @since 2.4
	 */
	public static PlaceholderAPIPlugin getPlaceholderAPI()
	{
		return placeholderAPI;
	}
	
	/**
	 * Gets the Sidebar Object associated with the specified player.
	 * Note that this will only return a Sidebar Object if it has been shown to the player and then not been hidden again.
	 * Note also that this will only return a Sidebar Object if the specified player's scoreboard sidebar had been created with this API.
	 * @param forPlayer (Player) - the player
	 * @return
	 */
	public static Sidebar getSidebar(Player forPlayer)
	{
		
		if (forPlayer == null)
			throw new NullPointerException("forPlayer cannot be null!");
		
		Objective obj = forPlayer.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
		
		if (obj == null)
			return null;
		
		if (!sidebars.isEmpty())
			for (Sidebar sidebar : sidebars)
				if (sidebar.getTitle().equals(obj.getDisplayName()))
					return sidebar;
		
		return null;
		
	}
	
}

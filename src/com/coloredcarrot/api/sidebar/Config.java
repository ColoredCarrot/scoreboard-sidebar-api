package com.coloredcarrot.api.sidebar;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * All rights reserved.
 * @author ColoredCarrot
 * @since 2.6
 */
public class Config
{

	private static FileConfiguration yaml;
	private static File file;
	
	public static void load()
	{
		
		SidebarAPI.getInstance().getDataFolder().mkdirs();
		
		file = new File(SidebarAPI.getInstance().getDataFolder(), "config.yml");
		
		if (!file.exists())
			SidebarAPI.getInstance().saveResource("config.yml", true);
		
		yaml = YamlConfiguration.loadConfiguration(file);
		
	}
	
	public static boolean getBoolean_updater_autoDownload()
	{
		return yaml.getBoolean("updater.auto-download");
	}
	
}

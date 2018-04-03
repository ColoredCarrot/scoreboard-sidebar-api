package com.coloredcarrot.api.sidebar;

import org.apache.commons.io.FileUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * All rights reserved.
 * @author ColoredCarrot
 * @since 2.6
 */
public class Updater
{
	
	public static final String RESOURCE_ID = "21042";
	public static final String DOWNLOAD_URL = "https://www.spigotmc.org/resources/21042";
	
	private static boolean autoDownloadUpdate = false;
	private static BukkitTask task = null;
	
	/**
	 * Gets whether to auto-download a new update to the updates folder of this plugin.
	 * @return
	 */
	public static boolean isAutoDownloadUpdate()
	{
		return autoDownloadUpdate;
	}
	
	public static void setAutoDownloadUpdate(boolean autoDownloadUpdate)
	{
		Updater.autoDownloadUpdate = autoDownloadUpdate;
	}
	
	public static void checkForUpdate()
	{
		checkForUpdate(autoDownloadUpdate);
	}
	
	public static void checkForUpdate(final boolean autoDownloadUpdate)
	{
		
		if (task != null)
			return;
		
		task = (new BukkitRunnable()
		{
			
			@Override
			public void run()
			{
				
				HttpURLConnection connection = null;

				try
				{

					String version;

					connection = (HttpURLConnection) new URL("https://api.inventivetalent.org/spigot/resource-simple/" + RESOURCE_ID).openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
					connection.setRequestMethod("GET");

					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

					String content = "";
					String line = null;

					while ((line = in.readLine()) != null)
						content += line;

					in.close();

					JSONObject json = null;

					try { json = (JSONObject) new JSONParser().parse(content); }
					catch (ParseException e) {  }

					String currentVersion = null;

					if (json != null && json.containsKey((Object) "version") && (version = (String) json.get((Object) "version")) != null && !version.isEmpty())
						currentVersion = version;

					if (currentVersion == null)
					{
						SidebarAPI.getInstance().getLogger().warning("[Updater] Could not check for updates. Please report at http://coloredcarrot.com/contact and include a way of contacting you if you wish.");
						return;
					}

					if (!currentVersion.equals(SidebarAPI.getVersion()))
					{
						
						if (!autoDownloadUpdate)
						{
							SidebarAPI.getInstance().getLogger().info("[Updater] New version is available for download: " + currentVersion);
							SidebarAPI.getInstance().getLogger().info("[Updater] It is recommended to download it as soon as possible at " + DOWNLOAD_URL);
						}
						else
						{
							
							try
							{

								File dir = new File(SidebarAPI.getInstance().getDataFolder(), "updater");
								dir.mkdirs();

								File file = new File(dir, "newest_version.jar");
								if (file.exists())
									file.delete();

								FileUtils.copyURLToFile(new URL("http://download.coloredcarrot.com/bukkit-spigot-plugin/SidebarAPI.jar"), file);
								
								SidebarAPI.getInstance().getLogger().info("[Updater] Downloaded new version " + currentVersion + " to: /plugins/SidebarAPI/updater/newest_version.jar");
								SidebarAPI.getInstance().getLogger().info("[Updater] To complete installation, delete the old plugin file, move the donwloaded file to your plugins directory and restart the server.");

							}
							catch (Exception e)
							{
								SidebarAPI.getInstance().getLogger().warning("[Updater] Failed to auto-download new version " + currentVersion + ".");
								SidebarAPI.getInstance().getLogger().warning("[Updater] It is recommended to download it manually as soon as possible at " + DOWNLOAD_URL);
								e.printStackTrace();
							}
							
						}
						
					}
					else
						SidebarAPI.getInstance().getLogger().info("[Updater] Plugin is up-to-date.");

				}
				catch (IOException e)
				{

					SidebarAPI.getInstance().getLogger().warning("[Updater] Could not check for updates. Please report at http://coloredcarrot.com/contact and include a way of contacting you if you wish.");

					if (connection != null)
					{

						try { SidebarAPI.getInstance().getLogger().warning("[Updater] Http response code: " + connection.getResponseCode()); }
						catch (IOException e1) {  }

					}

				}
				
				task = null;

			}

		}).runTaskLater(SidebarAPI.getInstance(), 3);
		
	}
	
}

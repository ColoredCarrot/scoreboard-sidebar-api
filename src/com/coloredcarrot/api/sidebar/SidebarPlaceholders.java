package com.coloredcarrot.api.sidebar;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

/**
 * All rights reserved.
 * @since 2.5
 * @author ColoredCarrot
 */
public class SidebarPlaceholders
extends
EZPlaceholderHook
{

	protected SidebarPlaceholders()
	{
		super(SidebarAPI.getInstance(), "sidebar");
	}

	/**
	 * Valid placeholders (in full form) (n can be any integer above -1):
	 * - %sidebar_title%
	 * - %sidebar_linenextvar_n%
	 * - %sidebar_line_n_var_n%
	 */
	@Override
	public String onPlaceholderRequest(Player player, String identifier)
	{
		
		try
		{
			
			if (player == null)
				return "";

			if (identifier.equals("title"))
			{
				
				Sidebar sidebar = SidebarAPI.getSidebar(player);
				
				if (sidebar == null)
					return "";
				else
					return sidebar.getTitle();

			}

			else if (identifier.startsWith("linenextvar_"))
			{

				int line = Integer.valueOf(identifier.split("inenextvar_")[1]);

				Sidebar sidebar = SidebarAPI.getSidebar(player);

				if (sidebar == null)
					return "";
				
				return sidebar.getEntries().get(line).getNext();
				
			}

			else if (identifier.startsWith("line_") && identifier.contains("_var_"))
			{
				
				int line = Integer.valueOf(identifier.split("ine_")[1].split("_var_")[0]);
				int var = Integer.valueOf(identifier.split("_var_")[1]);
				
				Sidebar sidebar = SidebarAPI.getSidebar(player);

				if (sidebar == null)
					return "";
				
				return sidebar.getEntries().get(line).getVariations().get(var);
				
			}

		}
		catch (Exception e) {  }
		
		return "";
		
	}

}

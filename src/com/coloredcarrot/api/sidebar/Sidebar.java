/*
 * MIT License
 *
 * Copyright (c) 2018 ColoredCarrot
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.coloredcarrot.api.sidebar;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public class Sidebar implements ConfigurationSerializable
{

    private static transient ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();

    static
    {
        ConfigurationSerialization.registerClass(Sidebar.class);
    }

    private           List<SidebarString> entries;
    private transient Scoreboard          bukkitScoreboard;
    private transient Objective           bukkitObjective;
    private transient BukkitTask          updateTask;
    private           String              title;
    private           Player              setPlaceholdersOnUpdate = null;

    /**
     * Constructs a new Sidebar.
     *
     * @param title              (String) - the title of the sidebar
     * @param plugin             (Plugin) - your plugin
     * @param updateDelayInTicks (int) - how many server ticks to wait in between each update. 20 = 1 second
     * @param entries            (SidebarString...) - all the entries
     */
    public Sidebar(String title, Plugin plugin, int updateDelayInTicks, SidebarString... entries)
    {

        bukkitScoreboard = bukkitManager.getNewScoreboard();

        bukkitObjective = bukkitScoreboard.registerNewObjective("obj", "dummy");

        this.entries = new ArrayList<>();
        this.entries.addAll(Arrays.asList(entries));

        this.title = title;

        update();

        setUpdateDelay(plugin, updateDelayInTicks);

        SidebarAPI.registerSidebar(this);

    }

    @SuppressWarnings("unchecked")
    public Sidebar(Map<String, Object> map)
    {

        entries = (List<SidebarString>) map.get("entries");
        title = (String) map.get("title");

        if (map.containsKey("placeholders"))
            setPlaceholdersOnUpdate = Bukkit.getPlayer(UUID.fromString((String) map.get("placeholders")));

    }

    @Override
    public Map<String, Object> serialize()
    {

        Map<String, Object> map = new HashMap<>();

        map.put("entries", entries);
        map.put("title", title);

        if (setPlaceholdersOnUpdate != null)
            map.put("placeholders", setPlaceholdersOnUpdate.getUniqueId().toString());

        return map;

    }

    /**
     * Gets the player that will be used for setting the placeholders in the update function.
     *
     * @return (Player) - The player or null.
     * @see #update()
     */
    public Player getPlaceholderPlayerForUpdate()
    {
        return setPlaceholdersOnUpdate;
    }

    /**
     * Sets the player that will be used for setting the placeholders in the update function.
     * If set to null, the placeholders will not be set.
     *
     * @param player (Player) - the player or null
     * @return (Sidebar) - this Sidebar Object, for chaining.
     * @see #update()
     */
    public Sidebar setPlaceholderPlayerForUpdate(Player player)
    {
        setPlaceholdersOnUpdate = player;
        return this;
    }

    /**
     * Sets how many server ticks to wait in between each update.
     *
     * @param plugin       (Plugin) - your plugin
     * @param delayInTicks (int) - the ticks
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar setUpdateDelay(Plugin plugin, int delayInTicks)
    {

        if (delayInTicks < 1)
            throw new IllegalArgumentException("delayInTicks cannot be less than 1!");

        if (updateTask != null)
            updateTask.cancel();

        updateTask = (new BukkitRunnable()
        {

            @Override
            public void run()
            {
                update();
            }

        }).runTaskTimer(plugin, delayInTicks, delayInTicks);

        return this;

    }

    /**
     * Sets all placeholders for every SidebarString and every variation.
     *
     * @param forPlayer (Player) - what player to set the placeholders for
     * @return (Sidebar) - this Sidebar Object, for chaining.
     * @since 2.4
     */
    public Sidebar setAllPlaceholders(Player forPlayer)
    {

        for (SidebarString entry : entries)
            entry.setPlaceholders(forPlayer);

        return this;

    }

    /**
     * Gets the title of this Sidebar.
     *
     * @return (String) - the title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the title of this Sidebar.
     *
     * @param title (String) - the new title
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar setTitle(String title)
    {
        this.title = title;
        return this;
    }

    /**
     * Gets a list of all entries.
     *
     * @return (List : SidebarString) - all entries.
     */
    public List<SidebarString> getEntries()
    {
        return entries;
    }

    /**
     * Overrides all current entries.
     *
     * @param entries (List: SidebarString) - the new entries
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar setEntries(List<SidebarString> entries)
    {
        this.entries = entries;
        return this;
    }

    /**
     * Adds an entry.
     *
     * @param entries (SidebarString) - the entry
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar addEntry(SidebarString... entries)
    {
        this.entries.addAll(Arrays.asList(entries));
        return this;
    }

    /**
     * Removes an entry.
     *
     * @param entry (SidebarString) - the entry
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar removeEntry(SidebarString entry)
    {
        entries.remove(entry);
        return this;
    }

    /**
     * Removes the entry referring to a specific line.
     *
     * @param num (int) - the line
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar removeEntry(int num)
    {
        entries.remove(num);
        return this;
    }

    /**
     * Shows this Sidebar to a player.
     *
     * @param player (Player) - the player
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar showTo(Player player)
    {
        player.setScoreboard(bukkitScoreboard);
        return this;
    }

    /**
     * Hides this Sidebar from a player.
     *
     * @param player (Player) - the player
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar hideFrom(Player player)
    {
        player.setScoreboard(bukkitManager.getMainScoreboard());
        return this;
    }

    /**
     * Updates the sidebar (it's entries and title).
     * If {@link #getPlaceholderPlayerForUpdate()} is not null, this will also run {@link #setAllPlaceholders(Player)} with {@link #getPlaceholderPlayerForUpdate()} as the argument.
     *
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar update()
    {

        if (setPlaceholdersOnUpdate != null)
            setAllPlaceholders(setPlaceholdersOnUpdate);

        redoBukkitObjective();

        for (int i = entries.size(); i > 0; i--)
            bukkitObjective.getScore(entries.get(entries.size() - i).getNext()).setScore(i);

        // this method had been causing issues
		/*for (int i = entries.size(); i > 0; i--)
		{
			
			String line = entries.get(entries.size() - i).getNext();
			
			Team team = bukkitScoreboard.getTeam("team-" + i);
			if (team != null)
				team.unregister();
			
			String[] teamValues = generateTeamStrings(line);
			
			team = bukkitScoreboard.registerNewTeam("team-" + i);
			
			team.setPrefix(teamValues[0]);
			team.addEntry(teamValues[1].equals("") ? ChatColor.RESET.toString() : teamValues[1]);
			team.setSuffix(teamValues[2].equals("") ? ChatColor.RESET.toString() : teamValues[2]);
			
			bukkitObjective.getScore(team.getEntries().toArray(new String[1])[0]).setScore(i);
			
		}*/

        return this;

    }

    // this method had been causing issues
	/*private String[] generateTeamStrings(String line)
	{
		
		String prefix = line.length() > 16 ? line.substring(0, 15) : line;
		line = line.length() > 16 ? line.substring(16) : "";
		
		String value = line.length() > 16 ? line.substring(0, 15) : line;
		line = line.length() > 16 ? line.substring(16) : "";
		
		String suffix = line.length() > 16 ? line.substring(0, 15) : line;
		
		return new String[]
				{
						prefix,
						value,
						suffix
				};
		
	}*/

    /**
     * Adds an empty entry.
     * The entry won't conflict with any other empty entries made this way.
     *
     * @return (Sidebar) - this Sidebar Object, for chaining.
     */
    public Sidebar addEmpty()
    {

        entries.add(new SidebarString(new String(new char[entries.size()]).replace("\0", " ")));

        return this;

    }

    private void redoBukkitObjective()
    {

        bukkitObjective.unregister();
        bukkitObjective = bukkitScoreboard.registerNewObjective("obj", "dummy");

        bukkitObjective.setDisplayName(title);
        bukkitObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

    }

}

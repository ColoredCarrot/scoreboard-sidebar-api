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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.scoreboard.Team;

import com.google.common.collect.ImmutableSet;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public class Sidebar implements ConfigurationSerializable, Sidebars {

	private static final Set<String> EMPTY_COLOR_STRINGS = ImmutableSet.of("§f", "§f§r", "§r", "§r§f");

	private static transient ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();

	static {
		ConfigurationSerialization.registerClass(Sidebar.class);
	}

	private final transient Plugin owningPlugin;
	private List<SidebarString> entries;
	private transient Scoreboard bukkitScoreboard;
	private transient Objective bukkitObjective;
	private transient BukkitTask updateTask;
	private String title;
	private Player setPlaceholdersOnUpdate = null;
	private transient Team[] teams = new Team[15];
	private int prevEntries = 0;

	/**
	 * Constructs a new Sidebar.
	 *
	 * @param title
	 *            (String) - the title of the sidebar
	 * @param plugin
	 *            (Plugin) - your plugin
	 * @param updateDelayInTicks
	 *            (int) - how many server ticks to wait in between each update.
	 *            20 = 1 second
	 * @param entries
	 *            (SidebarString...) - all the entries
	 */
	public Sidebar(String title, Plugin plugin, int updateDelayInTicks, SidebarString... entries) {

		this.title = title;
		this.entries = new ArrayList<>(Arrays.asList(entries));
		this.owningPlugin = plugin;

		bukkitScoreboard = bukkitManager.getNewScoreboard();

		bukkitObjective = bukkitScoreboard.registerNewObjective("obj", "dummy");
		bukkitObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		bukkitObjective.setDisplayName(this.title);

		for (int i = 0; i < 15; i++) {
			Team team = bukkitScoreboard.registerNewTeam("team" + String.valueOf(i));
			team.addEntry(ChatColor.values()[i].toString() + "§r");
			teams[i] = team;
		}
		update();

		setUpdateDelay(plugin, updateDelayInTicks);

		SidebarAPI.registerSidebar(this);

	}

	@SuppressWarnings("unchecked")
	public Sidebar(Map<String, Object> map) {

		entries = (List<SidebarString>) map.get("entries");
		title = (String) map.get("title");

		if (map.containsKey("placeholders"))
			setPlaceholdersOnUpdate = Bukkit.getPlayer(UUID.fromString((String) map.get("placeholders")));

		owningPlugin = SidebarAPI.getInstance();

	}

	@Override
	public Map<String, Object> serialize() {

		Map<String, Object> map = new HashMap<>();

		map.put("entries", entries);
		map.put("title", title);

		if (setPlaceholdersOnUpdate != null)
			map.put("placeholders", setPlaceholdersOnUpdate.getUniqueId().toString());

		return map;

	}

	/**
	 * Gets the player that will be used for setting the placeholders in the
	 * update function.
	 *
	 * @return (Player) - The player or null.
	 * @see #update()
	 */
	public Player getPlaceholderPlayerForUpdate() {
		return setPlaceholdersOnUpdate;
	}

	/**
	 * Sets the player that will be used for setting the placeholders in the
	 * update function. If set to null, the placeholders will not be set.
	 *
	 * @param player
	 *            (Player) - the player or null
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 * @see #update()
	 */
	public Sidebar setPlaceholderPlayerForUpdate(Player player) {
		setPlaceholdersOnUpdate = player;
		return this;
	}

	/**
	 * Sets how many server ticks to wait in between each update.
	 *
	 * @param plugin
	 *            (Plugin) - your plugin
	 * @param delayInTicks
	 *            (int) - the ticks
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar setUpdateDelay(Plugin plugin, int delayInTicks) {

		if (delayInTicks < 1)
			throw new IllegalArgumentException("delayInTicks cannot be less than 1!");

		if (updateTask != null)
			updateTask.cancel();

		updateTask = (new BukkitRunnable() {
			@Override
			public void run() {
				update();
			}
		}).runTaskTimer(plugin, delayInTicks, delayInTicks);

		return this;

	}

	/**
	 * Sets all placeholders for every SidebarString and every variation.
	 *
	 * @param forPlayer
	 *            (Player) - what player to set the placeholders for
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 * @since 2.4
	 */
	public Sidebar setAllPlaceholders(Player forPlayer) {
		for (SidebarString entry : entries)
			entry.setPlaceholders(forPlayer);
		return this;
	}

	/**
	 * Gets the title of this Sidebar.
	 *
	 * @return (String) - the title.
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title of this Sidebar.
	 *
	 * @param title
	 *            (String) - the new title
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar setTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * Gets a list of all entries.
	 *
	 * @return (List : SidebarString) - all entries.
	 */
	@Override
	public List<SidebarString> getEntries() {
		return entries;
	}

	/**
	 * Overrides all current entries.
	 *
	 * @param entries
	 *            (List: SidebarString) - the new entries
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar setEntries(List<SidebarString> entries) {
		this.entries = entries;
		return this;
	}

	/**
	 * Adds an entry.
	 *
	 * @param entries
	 *            (SidebarString) - the entry
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar addEntry(SidebarString... entries) {
		this.entries.addAll(Arrays.asList(entries));
		return this;
	}

	/**
	 * Removes an entry.
	 *
	 * @param entry
	 *            (SidebarString) - the entry
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar removeEntry(SidebarString entry) {
		entries.remove(entry);
		return this;
	}

	/**
	 * Removes the entry referring to a specific line.
	 *
	 * @param num
	 *            (int) - the line
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar removeEntry(int num) {
		entries.remove(num);
		return this;
	}

	/**
	 * Shows this Sidebar to a player.
	 *
	 * @param player
	 *            (Player) - the player
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar showTo(Player player) {
		player.setScoreboard(bukkitScoreboard);
		return this;
	}

	/**
	 * Hides this Sidebar from a player.
	 *
	 * @param player
	 *            (Player) - the player
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar hideFrom(Player player) {
		player.setScoreboard(bukkitManager.getMainScoreboard());
		return this;
	}

	/**
	 * Updates the sidebar (it's entries and title). If
	 * {@link #getPlaceholderPlayerForUpdate()} is not null, this will also run
	 * {@link #setAllPlaceholders(Player)} with
	 * {@link #getPlaceholderPlayerForUpdate()} as the argument.
	 *
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar update() {

		if (setPlaceholdersOnUpdate != null)
			setAllPlaceholders(setPlaceholdersOnUpdate);

		// Anti-flicker only works for <=15 entries
		if (entries.size() <= 15)
			updateAntiFlicker();
		else
			updateFallback();

		return this;

	}

	private void updateFallback() {

		if (setPlaceholdersOnUpdate != null)
			setAllPlaceholders(setPlaceholdersOnUpdate);

		redoBukkitObjective();

		for (int i = entries.size(); i > 0; i--)
			bukkitObjective.getScore(entries.get(entries.size() - i).getNext()).setScore(i);

	}

	private void redoBukkitObjective() {
		bukkitObjective.unregister();
		bukkitObjective = bukkitScoreboard.registerNewObjective("obj", "dummy");
		bukkitObjective.setDisplayName(title);
		bukkitObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	/*
	 * Code by fren_gor
	 */
	private void updateAntiFlicker() {

		if (prevEntries != entries.size()) {
			redoBukkitTeams();
			for (int i = 0; i < entries.size(); i++)
				bukkitObjective.getScore(ChatColor.values()[i] + "§r").setScore(entries.size() - 1 - i);
		}

		prevEntries = entries.size();
		for (int i = 0; i < prevEntries; i++) {
			SidebarString entry = entries.get(i);
			String entryStr = ChatColor.translateAlternateColorCodes('&',
					entry.getNextAndTrim(owningPlugin.getLogger(), false));

			if (entryStr.startsWith("§r") || entryStr.startsWith("§f"))
				entryStr = entryStr.substring(2);

			if (entryStr.length() <= 16) {
				// Simple case: prefix is sufficient to show whole string
				teams[i].setPrefix(entryStr);
				teams[i].setSuffix("");
			} else {

				if (!entryStr.contains("§")) {
					teams[i].setPrefix(entryStr.substring(0, 16));
					teams[i].setSuffix(entryStr.substring(16));
				} else {
					/*
					 * This is an especially difficult case. Color symbols
					 * cannot be split over team prefix and suffix.
					 */
					boolean carryingColor = false;
					String[] sections = entryStr.split("§");
					StringBuilder color = new StringBuilder();
					int len = 0;
					for (String section : sections) {
						if (section.length() == 0)
							continue;

						if (section.length() == 1) {
							if (carryingColor)
								color.append("§").append(section);
							else
								color = new StringBuilder("§").append(section);
							carryingColor = true;
						} else {
							if (carryingColor)
								color.append('§').append(section, 0, 1);
							else
								color = new StringBuilder("§").append(section, 0, 1);
							len += section.length() - 1;
							carryingColor = false;
						}
						if (len >= 16 || i == sections.length - 1) {
							boolean colorOnSplit = entryStr.charAt(15) == '§';
							String teamSuffix = entryStr.substring(colorOnSplit ? 17 : 16, entryStr.length());
							String teamPrefix = entryStr.substring(0, colorOnSplit ? 15 : 16);

							teams[i].setPrefix(teamPrefix);

							// Set suffix
							// Color needs only be included if it has any effect
							// (i.e. if it is not "empty"/"whitespace")
							String colorStr = color.toString().toLowerCase(Locale.ENGLISH);
							teams[i].setSuffix(
									EMPTY_COLOR_STRINGS.contains(colorStr) ? teamSuffix : color + teamSuffix);

							break;
						}

					}

				}

			}

		}
	}

	/**
	 * Adds an empty entry. The entry won't conflict with any other empty
	 * entries made this way.
	 *
	 * @return (Sidebar) - this Sidebar Object, for chaining.
	 */
	public Sidebar addEmpty() {

		entries.add(new SidebarString(new String(new char[entries.size()]).replace("\0", " ")));

		return this;

	}

	/**
	 * Gets the Scoreboard used by this Sidebar.
	 *
	 * @return The Scoreboard associated with this Sidebar
	 * @since 2.9
	 */
	public Scoreboard getTheScoreboard() {
		return bukkitScoreboard;
	}

	private void redoBukkitTeams() {
		for (int i = 0; i < 15; i++) {
			bukkitScoreboard.resetScores(ChatColor.values()[i] + "§r");
			teams[i].setSuffix("");
			teams[i].setPrefix("");
		}
	}

}

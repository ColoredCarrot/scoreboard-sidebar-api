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
import java.util.Map;
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

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public class LongSidebar implements ConfigurationSerializable, Sidebars {

	private static transient ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();

	static {
		ConfigurationSerialization.registerClass(LongSidebar.class);
	}

	private final transient Plugin owningPlugin;
	private List<SidebarString> entries;
	private transient Scoreboard bukkitScoreboard;
	private transient Objective bukkitObjective;
	private transient Objective bukkitObjective1;
	private transient Objective bukkitObjective2;
	private transient BukkitTask updateTask;
	private String title;
	private Player setPlaceholdersOnUpdate = null;
	private transient Team[] teams = new Team[15];
	private transient Team[] teams1 = new Team[15];
	private transient Team[] teams2 = new Team[15];
	private transient int updateState = 0;

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
	public LongSidebar(String title, Plugin plugin, int updateDelayInTicks, SidebarString... entries) {

		this.title = title;
		this.entries = new ArrayList<>(Arrays.asList(entries));
		this.owningPlugin = plugin;

		bukkitScoreboard = bukkitManager.getNewScoreboard();

		bukkitObjective = bukkitScoreboard.registerNewObjective("obj", "dummy");
		bukkitObjective1 = bukkitScoreboard.registerNewObjective("obj1", "dummy");
		bukkitObjective2 = bukkitScoreboard.registerNewObjective("obj2", "dummy");
		bukkitObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		bukkitObjective.setDisplayName(this.title);
		bukkitObjective1.setDisplayName(this.title);
		bukkitObjective2.setDisplayName(this.title);

		for (int i = 0; i < 15; i++) {
			Team team = bukkitScoreboard.registerNewTeam("team" + String.valueOf(i));
			Team team1 = bukkitScoreboard.registerNewTeam("iteam" + String.valueOf(i));
			Team team2 = bukkitScoreboard.registerNewTeam("iiteam" + String.valueOf(i));
			teams[i] = team;
			teams1[i] = team1;
			teams2[i] = team2;
		}
		update();

		setUpdateDelay(plugin, updateDelayInTicks);

		SidebarAPI.registerSidebar((Sidebars) this);

	}

	@SuppressWarnings("unchecked")
	public LongSidebar(Map<String, Object> map) {

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
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 * @see #update()
	 */
	public LongSidebar setPlaceholderPlayerForUpdate(Player player) {
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
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar setUpdateDelay(Plugin plugin, int delayInTicks) {

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
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 * @since 2.4
	 */
	public LongSidebar setAllPlaceholders(Player forPlayer) {
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
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar setTitle(String title) {
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
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar setEntries(List<SidebarString> entries) {
		this.entries = entries;
		return this;
	}

	/**
	 * Adds an entry.
	 *
	 * @param entries
	 *            (SidebarString) - the entry
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar addEntry(SidebarString... entries) {
		this.entries.addAll(Arrays.asList(entries));
		return this;
	}

	/**
	 * Removes an entry.
	 *
	 * @param entry
	 *            (SidebarString) - the entry
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar removeEntry(SidebarString entry) {
		entries.remove(entry);
		return this;
	}

	/**
	 * Removes the entry referring to a specific line.
	 *
	 * @param num
	 *            (int) - the line
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar removeEntry(int num) {
		entries.remove(num);
		return this;
	}

	/**
	 * Shows this Sidebar to a player.
	 *
	 * @param player
	 *            (Player) - the player
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar showTo(Player player) {
		player.setScoreboard(bukkitScoreboard);
		return this;
	}

	/**
	 * Hides this Sidebar from a player.
	 *
	 * @param player
	 *            (Player) - the player
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar hideFrom(Player player) {
		player.setScoreboard(bukkitManager.getMainScoreboard());
		return this;
	}

	/**
	 * Updates the sidebar (it's entries and title). If
	 * {@link #getPlaceholderPlayerForUpdate()} is not null, this will also run
	 * {@link #setAllPlaceholders(Player)} with
	 * {@link #getPlaceholderPlayerForUpdate()} as the argument.
	 *
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar update() {

		if (setPlaceholdersOnUpdate != null)
			setAllPlaceholders(setPlaceholdersOnUpdate);

		updateAntiFlicker();

		return this;

	}

	/*
	 * Code by fren_gor
	 */
	private void updateOne() {

		redoBukkitObjective1();

		for (int i = 0; i < entries.size(); i++) {

			SidebarString entry = entries.get(i);
			String entryStr = ChatColor.translateAlternateColorCodes('&',
					entry.getNextAndTrim(owningPlugin.getLogger(), true));

			teams[i].setSuffix("");

			if (entryStr.length() <= 16) {
				// Simple case: prefix is sufficient to show whole string
				teams[i].setPrefix(entryStr);

				teams[i].addEntry(ChatColor.values()[i] + "§r");

				bukkitObjective.getScore(ChatColor.values()[i] + "§r").setScore(entries.size() - 1 - i);

			} else {

				String s1 = entryStr.length() <= 16 ? entryStr : entryStr.substring(0, 16);
				String s2 = "";
				if (entryStr.length() > 16)
					s2 = entryStr.length() <= 50 ? entryStr.substring(16) : entryStr.substring(16, 50);
				String s3 = "";
				if (entryStr.length() > 50)
					s3 = entryStr.substring(50);

				if (!entryStr.contains("§")) {
					if (entryStr.length() <= 50) {

						teams[i].setPrefix(s1);
						teams[i].addEntry(ChatColor.values()[i] + "§r" + s2);
						bukkitObjective.getScore(ChatColor.values()[i] + "§r" + s1).setScore(entries.size() - 1 - i);

					} else {

						teams[i].setPrefix(s1);
						teams[i].addEntry(ChatColor.values()[i] + "§r" + s2);
						teams[i].setSuffix(s3);
						bukkitObjective.getScore(ChatColor.values()[i] + "§r" + s3).setScore(entries.size() - 1 - i);
					}

				} else {

					boolean color1 = true;
					boolean color2 = true;

					if (s1.endsWith("§")) {
						s1 = s1.substring(0, s1.length() - 1);
						if (entryStr.length() > 16) {
							s2 = "§" + s2;
							color1 = false;
						}
					}

					if (s2.endsWith("§")) {
						s2 = s2.substring(0, s2.length() - 1);
						if (entryStr.length() > 50) {
							s3 = "§" + s3;
							color2 = false;
						}
					}

					String color = getLastChatColor(s1);

					if (entryStr.length() <= 50) {

						teams[i].setPrefix(s1);
						if (color1) {
							teams[i].addEntry(ChatColor.values()[i] + color + s2);
							bukkitObjective.getScore(ChatColor.values()[i] + color + s2)
									.setScore(entries.size() - 1 - i);
						} else {
							teams[i].addEntry(ChatColor.values()[i] + s2);
							bukkitObjective.getScore(ChatColor.values()[i] + s2).setScore(entries.size() - 1 - i);
						}
					} else {
						teams[i].setPrefix(s1);
						if (color2) {

							teams[i].addEntry(ChatColor.values()[i] + color + s2);
							teams[i].setSuffix(getLastChatColor(s2) + s3);
							bukkitObjective.getScore(ChatColor.values()[i] + color + s2)
									.setScore(entries.size() - 1 - i);

						} else {
							teams[i].addEntry(ChatColor.values()[i] + s2);
							teams[i].setSuffix(s3);
							bukkitObjective.getScore(ChatColor.values()[i] + s2).setScore(entries.size() - 1 - i);
						}

					}

				}

			}
		}

		bukkitObjective2.setDisplaySlot(DisplaySlot.SIDEBAR);

	}

	/*
	 * Code by fren_gor
	 */
	private static List<Character> colors = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
			'c', 'd', 'e', 'f');

	/*
	 * Code by fren_gor
	 */
	static String getLastChatColor(String s) {
		String color = "§r";
		for (int i = 0; i < s.length(); i++) {

			if (s.charAt(i) == '§') {
				if (colors.contains(s.charAt(i + 1))) {
					color = "§" + s.charAt(i + 1);
				}
			}

		}

		return color;

	}

	/*
	 * Code by fren_gor
	 */
	private void updateTwo() {

		redoBukkitObjective2();

		for (int i = 0; i < entries.size(); i++) {

			SidebarString entry = entries.get(i);
			String entryStr = ChatColor.translateAlternateColorCodes('&',
					entry.getNextAndTrim(owningPlugin.getLogger(), true));

			if (entryStr.length() <= 16) {
				// Simple case: prefix is sufficient to show whole string
				teams1[i].setPrefix(entryStr);
				teams1[i].setSuffix("");

				teams1[i].addEntry(ChatColor.values()[i] + "§r");

				bukkitObjective1.getScore(ChatColor.values()[i] + "§r").setScore(entries.size() - 1 - i);

			} else {

				String s1 = entryStr.length() <= 16 ? entryStr : entryStr.substring(0, 16);
				String s2 = "";
				if (entryStr.length() > 16)
					s2 = entryStr.length() <= 50 ? entryStr.substring(16) : entryStr.substring(16, 50);
				String s3 = "";
				if (entryStr.length() > 50)
					s3 = entryStr.substring(50);

				if (!entryStr.contains("§")) {
					if (entryStr.length() <= 50) {

						teams1[i].setPrefix(s1);
						teams1[i].addEntry(ChatColor.values()[i] + "§r" + s2);
						bukkitObjective1.getScore(ChatColor.values()[i] + "§r" + s1).setScore(entries.size() - 1 - i);

					} else {

						teams1[i].setPrefix(s1);
						teams1[i].addEntry(ChatColor.values()[i] + "§r" + s2);
						teams1[i].setSuffix(s3);
						bukkitObjective1.getScore(ChatColor.values()[i] + "§r" + s3).setScore(entries.size() - 1 - i);
					}

				} else {

					boolean color1 = true;
					boolean color2 = true;

					if (s1.endsWith("§")) {
						s1 = s1.substring(0, s1.length() - 1);
						if (entryStr.length() > 16) {
							s2 = "§" + s2;
							color1 = false;
						}
					}

					if (s2.endsWith("§")) {
						s2 = s2.substring(0, s2.length() - 1);
						if (entryStr.length() > 50) {
							s3 = "§" + s3;
							color2 = false;
						}
					}

					String color = getLastChatColor(s1);

					if (entryStr.length() <= 50) {

						teams1[i].setPrefix(s1);
						if (color1) {
							teams1[i].addEntry(ChatColor.values()[i] + color + s2);
							bukkitObjective1.getScore(ChatColor.values()[i] + color + s2)
									.setScore(entries.size() - 1 - i);
						} else {
							teams1[i].addEntry(ChatColor.values()[i] + s2);
							bukkitObjective1.getScore(ChatColor.values()[i] + s2).setScore(entries.size() - 1 - i);
						}
					} else {
						teams1[i].setPrefix(s1);
						if (color2) {

							teams1[i].addEntry(ChatColor.values()[i] + color + s2);
							teams1[i].setSuffix(getLastChatColor(s2) + s3);
							bukkitObjective1.getScore(ChatColor.values()[i] + color + s2)
									.setScore(entries.size() - 1 - i);

						} else {
							teams1[i].addEntry(ChatColor.values()[i] + s2);
							teams1[i].setSuffix(s3);
							bukkitObjective1.getScore(ChatColor.values()[i] + s2).setScore(entries.size() - 1 - i);
						}

					}

				}

			}
		}

		bukkitObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	/*
	 * Code by fren_gor
	 */
	private void updateThree() {

		redoBukkitObjective3();

		for (int i = 0; i < entries.size(); i++) {

			SidebarString entry = entries.get(i);
			String entryStr = ChatColor.translateAlternateColorCodes('&',
					entry.getNextAndTrim(owningPlugin.getLogger(), true));

			teams2[i].setSuffix("");

			if (entryStr.length() <= 16) {
				// Simple case: prefix is sufficient to show whole string
				teams2[i].setPrefix(entryStr);

				teams2[i].addEntry(ChatColor.values()[i] + "§r");

				bukkitObjective2.getScore(ChatColor.values()[i] + "§r").setScore(entries.size() - 1 - i);

			} else {

				String s1 = entryStr.length() <= 16 ? entryStr : entryStr.substring(0, 16);
				String s2 = "";
				if (entryStr.length() > 16)
					s2 = entryStr.length() <= 50 ? entryStr.substring(16) : entryStr.substring(16, 50);
				String s3 = "";
				if (entryStr.length() > 50)
					s3 = entryStr.substring(50);

				if (!entryStr.contains("§")) {
					if (entryStr.length() <= 50) {

						teams2[i].setPrefix(s1);
						teams2[i].addEntry(ChatColor.values()[i] + "§r" + s2);
						bukkitObjective2.getScore(ChatColor.values()[i] + "§r" + s1).setScore(entries.size() - 1 - i);

					} else {

						teams2[i].setPrefix(s1);
						teams2[i].addEntry(ChatColor.values()[i] + "§r" + s2);
						teams2[i].setSuffix(s3);
						bukkitObjective2.getScore(ChatColor.values()[i] + "§r" + s3).setScore(entries.size() - 1 - i);
					}

				} else {

					boolean color1 = true;
					boolean color2 = true;

					if (s1.endsWith("§")) {
						s1 = s1.substring(0, s1.length() - 1);
						if (entryStr.length() > 16) {
							s2 = "§" + s2;
							color1 = false;
						}
					}

					if (s2.endsWith("§")) {
						s2 = s2.substring(0, s2.length() - 1);
						if (entryStr.length() > 50) {
							s3 = "§" + s3;
							color2 = false;
						}
					}

					String color = getLastChatColor(s1);

					if (entryStr.length() <= 50) {

						teams2[i].setPrefix(s1);
						if (color1) {
							teams2[i].addEntry(ChatColor.values()[i] + color + s2);
							bukkitObjective2.getScore(ChatColor.values()[i] + color + s2)
									.setScore(entries.size() - 1 - i);
						} else {
							teams2[i].addEntry(ChatColor.values()[i] + s2);
							bukkitObjective2.getScore(ChatColor.values()[i] + s2).setScore(entries.size() - 1 - i);
						}
					} else {
						teams2[i].setPrefix(s1);
						if (color2) {

							teams2[i].addEntry(ChatColor.values()[i] + color + s2);
							teams2[i].setSuffix(getLastChatColor(s2) + s3);
							bukkitObjective2.getScore(ChatColor.values()[i] + color + s2)
									.setScore(entries.size() - 1 - i);

						} else {
							teams2[i].addEntry(ChatColor.values()[i] + s2);
							teams2[i].setSuffix(s3);
							bukkitObjective2.getScore(ChatColor.values()[i] + s2).setScore(entries.size() - 1 - i);
						}

					}

				}

			}
		}

		bukkitObjective1.setDisplaySlot(DisplaySlot.SIDEBAR);

	}

	/*
	 * Code by fren_gor
	 */
	private void updateAntiFlicker() {

		if (updateState == 0) {
			updateOne();
			updateState++;
		} else if (updateState == 1) {
			updateTwo();
			updateState++;
		} else {
			updateThree();
			updateState = 0;
		}

	}

	/**
	 * Adds an empty entry. The entry won't conflict with any other empty
	 * entries made this way.
	 *
	 * @return (LongSidebar) - this LongSidebar Object, for chaining.
	 */
	public LongSidebar addEmpty() {

		entries.add(new SidebarString(new String(new char[entries.size()]).replace("\0", " ")));

		return this;

	}

	/**
	 * Gets the Scoreboard used by this Sidebar.
	 *
	 * @return The Scoreboard associated with this Sidebar
	 * @since 2.9
	 */
	public Scoreboard getScoreboard() {
		return bukkitScoreboard;
	}

	private void redoBukkitObjective1() {
		bukkitObjective.unregister();
		bukkitObjective = bukkitScoreboard.registerNewObjective("obj", "dummy");
		bukkitObjective.setDisplayName(title);
	}

	private void redoBukkitObjective2() {
		bukkitObjective1.unregister();
		bukkitObjective1 = bukkitScoreboard.registerNewObjective("obj1", "dummy");
		bukkitObjective1.setDisplayName(title);
	}

	private void redoBukkitObjective3() {
		bukkitObjective2.unregister();
		bukkitObjective2 = bukkitScoreboard.registerNewObjective("obj2", "dummy");
		bukkitObjective2.setDisplayName(title);
	}

}

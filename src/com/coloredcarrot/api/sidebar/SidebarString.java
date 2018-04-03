package com.coloredcarrot.api.sidebar;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All rights reserved.
 * @author ColoredCarrot
 * @since 2.0
 */
public class SidebarString
implements ConfigurationSerializable
{
	
	/**
	 * Generates a scrolling animation for the text.
	 * For example, this:
	 * {@code generateScrollingAnimation("Hello", 10)}
	 * will generate a SidebarString with the following variations:
	 * "He", "el", "ll", "lo"
	 * @param text (String) - the text
	 * @param displayWidth (int) - how many letters to fit into one animation
	 * @return (SidebarString) - the generated SidebarString, ready for use in a Sidebar.
	 */
	public static SidebarString generateScrollingAnimation(String text, int displayWidth)
	{
		
		if (text.length() <= displayWidth)
			return new SidebarString(text);

		SidebarString sidebarString = new SidebarString();
		
		for (int i = 0; i <= text.length() - displayWidth; i++)
			sidebarString.addVariation(text.substring(i, displayWidth + i));
		
		return sidebarString;
		
	}
	
	// since 2.9, not working yet
	/*public static SidebarString generateScrollingAnimation(String text, int displayWidth, char colorChar)
	{
		
		if (text.length() <= displayWidth)
			return new SidebarString(text);

		SidebarString s = new SidebarString();
		
		Character curColor = null;
		boolean previousWasColorChar = false;
		
		for (int i = 0; i <= text.length() - displayWidth; i++)
		{
			
			String sub = "";
			//boolean previousWasColorChar = false;
			
			for (char c : text.substring(i, displayWidth + i).toCharArray())
			{
				
				if (previousWasColorChar && c == colorChar)
				{
					sub += "��";
					previousWasColorChar = false;
				}
				else if (previousWasColorChar)
				{
					sub += "�" + c;
					curColor = c;
					previousWasColorChar = false;
				}
				else if (c == colorChar)
				{
					previousWasColorChar = true;
				}
				else
				{
					
					if (curColor == null)
						sub += c;
					else
						sub += "�" + curColor + c;
					
				}
				
			}
			
			s.addVariation(sub);
			
		}
		
		return s;
		
	}*/
	
	static
	{
		ConfigurationSerialization.registerClass(SidebarString.class);
	}
	
	private List<String> animated = new ArrayList<String>();
	private transient int i = 0, curStep;
	/** @since 2.8 */private int step = 1;
	
	/**
	 * Constructs a new SidebarString.
	 * @param variations (String...) - the variations (for animated text)
	 */
	public SidebarString(String... variations)
	{
	
		if (variations != null && variations.length > 0)
			animated.addAll(Arrays.asList(variations));
		
		curStep = step;
		
	}
	
	/**
	 * Constructs a new SidebarString.
	 * @param step (int) - see {@link #setStep(int)}
	 * @param variations (String...) - the variations (for animated text)
	 * @since 2.8
	 */
	public SidebarString(int step, String... variations)
	{
		
		if (step <= 0)
			throw new IllegalArgumentException("step cannot be smaller than or equal to 0!");
		
		this.step = step;
		
		if (variations != null && variations.length > 0)
			animated.addAll(Arrays.asList(variations));
		
		curStep = step;
		
	}
	
	/**
	 * Constructs a new SidebarString.
	 * If setPlaceholdersForPlayer is not null, the placeholders for the variations will be set.
	 * @param setPlaceholdersForPlayer (Player) - what player to set the placeholders for
	 * @param variations (String...) - the variations (for animated text) (may be null)
	 * @throws SidebarOptionalException if the PlaceholderAPI is not hooked.
	 * @since 2.4
	 */
	public SidebarString(Player setPlaceholdersForPlayer, String... variations)
			throws SidebarOptionalException
	{
		
		addVariation(setPlaceholdersForPlayer, variations);
		
		curStep = step;
		
	}
	
	/**
	 * Constructs a new SidebarString.
	 * If setPlaceholdersForPlayer is not null, the placeholders for the variations will be set.
	 * @param setPlaceholdersForPlayer (Player) - what player to set the placeholders for
	 * @param step (int) - see {@link #setStep(int)}
	 * @param variations (String...) - the variations (for animated text) (may be null)
	 * @throws SidebarOptionalException if the PlaceholderAPI is not hooked.
	 * @since 2.8
	 */
	public SidebarString(Player setPlaceholdersForPlayer, int step, String... variations)
			throws SidebarOptionalException
	{
		
		if (step <= 0)
			throw new IllegalArgumentException("step cannot be smaller than or equal to 0!");
		
		addVariation(setPlaceholdersForPlayer, variations);
		
		this.step = step;
		
		curStep = step;
		
	}
	
	@SuppressWarnings("unchecked")
	public SidebarString(Map<String, Object> map)
	{
		
		animated = (List<String>) map.get("data");
		
		try { step = map.get("step") == null ? 0 : (Integer) map.get("step"); }
		catch (ClassCastException | NullPointerException e) { step = 0; }
		
	}
	
	@Override
	public Map<String, Object> serialize()
	{
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("data", animated);
		map.put("step", step);
		
		return map;
		
	}
	
	public SidebarString cleanVariations(Player p)
	{
		
		// say this:
		// "�7hel"
		// "7hell"
		// "hello"
		// "ello "
		// "llo �"
		// "lo �c"
		// "o �cg"
		// " �cgu"
		
		List<String> newAnimated = new ArrayList<String>();
		boolean lastStartedWithColorChar = false;
		
		for (String var : animated)
		{
			
			if (var.startsWith("�") && lastStartedWithColorChar)
			{
				newAnimated.add(var);
				lastStartedWithColorChar = true;
			}
			else if (var.startsWith("�"))
				lastStartedWithColorChar = true;
			else if (lastStartedWithColorChar)
				lastStartedWithColorChar = false;
			else
				newAnimated.add(var);
			
		}
		
		animated = newAnimated;
		
		return this;
		
	}
	
	/**
	 * If the PlaceholderAPI is hooked, sets the placeholders of all variants in this SidebarString.
	 * @param forPlayer (Player) - what player to set the placeholders for
	 * @return (SidebarString) - this SidebarString Object, for chaining.
	 * @throws SidebarOptionalException if the PlaceholderAPI is not hooked.
	 * @since 2.4
	 */
	public SidebarString setPlaceholders(Player forPlayer)
			throws SidebarOptionalException
	{
		
		if (SidebarAPI.getPlaceholderAPI() == null)
			throw new SidebarOptionalException("PlaceholderAPI not hooked!");
		
		for (int i = 0; i < animated.size(); i++)
			animated.set(i, PlaceholderAPI.setPlaceholders(forPlayer, animated.get(i)));
		
		return this;
		
	}
	
	/**
	 * Gets the text that comes after the last one, for animated text.
	 * This method only returns the next variant if the step permits it; which is always by default.
	 * @return (String) - the next text.
	 */
	public String getNext()
	{
		
		if (curStep == step)
			i++;
		
		curStep++;
		
		if (curStep > step)
			curStep = 0;
		
		if (i > animated.size())
			i = 1;
		
		return animated.get(i - 1);
		
	}
	
	/**
	 * Resets the animation to the starting point.
	 * Since 2.8, this also resets the current step value so the next call of {@link #getNext()} returns the next variation.
	 * @return (SidebarString) - this SidebarString Object, for chaining.
	 */
	public SidebarString reset()
	{
		i = 0;
		curStep = step;
		return this;
	}
	
	/**
	 * Returns the step that is currently active.
	 * @return (SidebarString) - this SidebarString Object, for chaining.
	 * @see #setStep(int)
	 * @since 2.8
	 */
	public int getStep()
	{
		return step;
	}
	
	/**
	 * Sets the step of this SidebarString.
	 * The "step" defines how many times the method {@link #getNext()} needs to be run before the actual new variant will be returned.
	 * @param step (int) - the step, must be > 0
	 * @return (SidebarString) - this SidebarString Object, for chaining.
	 * @since 2.8
	 */
	public SidebarString setStep(int step)
	{
		
		if (step <= 0)
			throw new IllegalArgumentException("step cannot be smaller than or equal to 0!");
		
		this.step = step;
		
		curStep = step;
		
		return this;
		
	}
	
	/**
	 * Gets all variations of this text.
	 * @return (List: String) - all animations.
	 */
	public List<String> getVariations()
	{
		return animated;
	}
	
	/**
	 * Adds a variation.
	 * @param variations (String...) - the variations to add
	 * @return (SidebarString) - this SidebarString Object, for chaining.
	 */
	public SidebarString addVariation(String... variations)
	{
		animated.addAll(Arrays.asList(variations));
		return this;
	}
	
	/**
	 * Adds a variation.
	 * If setPlaceholdersForPlayer is not null, the placeholders will be set for that player in the variation.
	 * @param setPlaceholdersForPlayer (Player) - what player to set the placeholders for (may be null)
	 * @param variations (String...) - the variation(s) to add
	 * @return (SidebarString) - this SidebarString Object, for chaining.
	 * @since 2.4
	 */
	public SidebarString addVariation(Player setPlaceholdersForPlayer, String... variations)
	{
		
		if (setPlaceholdersForPlayer != null && SidebarAPI.getPlaceholderAPI() == null)
			throw new SidebarOptionalException("PlaceholderAPI not hooked!");
		
		if (variations != null && variations.length > 0)
		{
			
			if (setPlaceholdersForPlayer != null)
				for (int i = 0; i < variations.length; i++)
					variations[i] = PlaceholderAPI.setPlaceholders(setPlaceholdersForPlayer, variations[i]);

			animated.addAll(Arrays.asList(variations));
			
		}
		
		return this;
		
	}
	
	/**
	 * Removes a variation.
	 * @param variation (String) - the variation
	 * @return (SidebarString) - this SidebarString Object, for chaining.
	 */
	public SidebarString removeVariation(String variation)
	{
		animated.remove(variation);
		return this;
	}

}

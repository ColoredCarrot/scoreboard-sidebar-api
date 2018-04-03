/*
 * Copyright 2018 ColoredCarrot
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.coloredcarrot.api.sidebar;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config
{
    
    private static FileConfiguration yaml;
    private static File              file;
    
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

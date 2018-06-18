package com.coloredcarrot.api.sidebar;

import java.util.List;

public interface Sidebars {

	public abstract String getTitle();
	
	public abstract List<SidebarString> getEntries();

}

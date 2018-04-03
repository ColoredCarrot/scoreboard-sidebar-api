package com.coloredcarrot.api.sidebar;

public class SidebarOptionalException
extends RuntimeException
{

	/**
	 * Eclipse-generated serial version UID
	 */
	private static final long serialVersionUID = -7130462576821421098L;

	public SidebarOptionalException(String message)
	{
		super(message);
	}
	
	public SidebarOptionalException(String message, Throwable throwable)
	{
		super(message, throwable);
	}
	
}

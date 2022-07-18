/*
 	Copyright (c) 2010 @Cult s.r.l. All rights reserved.
	
	@Cult s.r.l. makes no representations or warranties about the 
	suitability of the software, either express or implied, including
	but not limited to the implied warranties of merchantability, fitness
	for a particular purpose, or non-infringement. 
	
	@Cult s.r.l.not be liable for any damage suffered by 
	licensee as a result of using, modifying or distributing this software 
	or its derivates.
	
	This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.web.logic.integration;

import java.net.InetAddress;

/**
 * An UID maker.
 *
 * @author Andrea Gazzarini.
 * @since 1.0
 */
public abstract class IDMaker 
{
	private static long currentID;

	static 
	{
		StringBuffer sb = new StringBuffer();
		String ipAddress = "127.0.0.1";
		try 
		{
			InetAddress ip = InetAddress.getLocalHost();
			ipAddress = ip.getHostAddress();
			sb.append(ipAddress.substring(ipAddress.lastIndexOf('.') + 1));
		} catch (Exception ignore) 
		{
			// Nothing to do here
		} 

		sb.append(System.currentTimeMillis());
		currentID = Long.parseLong(sb.toString());
	}

	/**
	 * Obtain the next available ID.
	 *
	 * @return long the next available ID.
	 */
	public static synchronized long getID() 
	{
		return currentID++;
	}
}
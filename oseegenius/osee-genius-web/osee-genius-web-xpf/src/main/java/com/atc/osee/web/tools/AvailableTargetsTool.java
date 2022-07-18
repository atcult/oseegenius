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
package com.atc.osee.web.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.SkipSetters;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.log.Log;

/**
 * Velocity tool for managing external targets.
 * 
 * IMP: names and in general targets configuration is a bad duplicate of pazpar2 config.
 * That is because pazpar2 doesn't have an API that exposes configured targets.
 * 
 * @author agazzarini
 * @since 1.0
 */
@DefaultKey("targets")
@SkipSetters
@ValidScope(Scope.APPLICATION)
public class AvailableTargetsTool extends SafeConfig
{
	private final List<String> targetList = new ArrayList<String>(20);
	private final Map<String, String> targets = new HashMap<String, String>(26);
	
	private String disableAllQueryString;
	
	@Override
	public void configure(final ValueParser params) 
	{
		final String i = params.getString("id", null);
		final String n = params.getString("names", null);
		
		if (i != null && n != null)
		{
			final String [] ids = i.split(",");
			final String [] names = n.split(",");
			if (ids.length != 0 && names.length != 0 && ids.length == names.length)
			{
				final StringBuilder builder = new StringBuilder();
				for (int index = 0; index < ids.length; index++)
				{
					if (index > 0)
					{
						builder.append("&");
					}
					
					try 
					{
						final String id = ids[index].trim();
						builder.append("pz:allow[").append(id).append("]=0");

						targetList.add(id);
						targets.put(id, names[index].trim());
					} catch (Exception ignore)
					{
						Log.error("Unable to configure external targets on W side.", ignore);
					}
				}
				disableAllQueryString = builder.toString();
				Log.info("OseeGenius has been configured with " + targets.size() + " targets.");
				Log.debug(String.valueOf(targets));
			}
		}
	}
	
	/**
	 * Returns true if this OG instance has no external targets.
	 * 
	 * @return true if this OG instance has no external targets.
	 */
	public boolean isEmpty()
	{
		return targetList.isEmpty();
	}
	
	/**
	 * Returns the list containing ids of external targets.
	 * 
	 * @return the list containing ids of external targets.
	 */
	public List<String> getTargetList()
	{
		return targetList;
	}	 

	/**
	 * Returns the name of the external target associated with the given id.
	 * 
	 * @param id the target identifier.
	 * @return the name of the external target associated with the given id.
	 */
	public String getName(final String id)
	{
		return targets.get(id);
	}
	
	public String getDisableAllQueryString()
	{
		return disableAllQueryString;
	}
}

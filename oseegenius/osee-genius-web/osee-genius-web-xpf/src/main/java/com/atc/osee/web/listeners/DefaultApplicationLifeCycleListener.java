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
package com.atc.osee.web.listeners;

import javax.servlet.ServletContext;

import com.atc.osee.logic.search.ISearchEngine;

/**
 * A default implementation of OseeGenius -W- lifecycle listener.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class DefaultApplicationLifeCycleListener extends OseeGeniusApplicationLifeCycleListener 
{
	@Override
	protected void onContextActivation(final ServletContext application, final ISearchEngine searchEngine) 
	{
		// Do nothing here...
	}

	@Override
	protected void onContextDeactivation(final ServletContext application) 
	{
		// Do nothing here...
	}
}
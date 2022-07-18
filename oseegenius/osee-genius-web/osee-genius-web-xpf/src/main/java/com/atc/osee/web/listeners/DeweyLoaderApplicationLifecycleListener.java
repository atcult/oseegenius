package com.atc.osee.web.listeners;

import javax.servlet.ServletContext;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;

/**
 * A OseeGenius -W- lifecycle application listener that loads CDD hierarchy.
 * 
 * @author agazzarini
 * @since 1,0
 */
public class DeweyLoaderApplicationLifecycleListener extends DefaultApplicationLifeCycleListener 
{
	/**
	 * Loads CDD hierarchy on application start-up.
	 * 
	 * @param application the OseeGenius -W- application context.
	 * @param searchEngine a valid OseeGenius -S- reference.
	 */
	protected void onContextActivation(final ServletContext application, final ISearchEngine searchEngine) 
	{
		try 
		{
			BrowsingFacetsLoadHelper.loadBrowsingHierarchies(application, searchEngine);
		} catch (Exception exception)
		{
			Log.error(
					MessageCatalog._100004_MALFORMED_IR_URL, 
					exception);
		}
	}
}
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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.SkipSetters;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;
import com.atc.osee.web.plugin.AccountPlugin;
import com.atc.osee.web.plugin.CirculationPlugin;
import com.atc.osee.web.plugin.CommunityPlugin;
import com.atc.osee.web.plugin.LibraryPlugin;
import com.atc.osee.web.plugin.nullobjects.NullObjectAccountPlugin;
import com.atc.osee.web.plugin.nullobjects.NullObjectCirculationPlugin;
import com.atc.osee.web.plugin.nullobjects.NullObjectCommunityPlugin;
import com.atc.osee.web.plugin.nullobjects.NullObjectLibraryPlugin;

/**
 * Osee Genius -W- License handler POJO.
 * A wrapper around Osee Genius -W- configuration that contols
 * activation / deactivation of several license and configuration level 
 * features.
 * 
 * @author agazzarini
 * @since 1.0
 */
@DefaultKey("license")
@SkipSetters
@ValidScope(Scope.APPLICATION)
public class LicenseTool extends SafeConfig
{	
	private static final String ACCOUNT_PLUGIN_IMPLEMENTOR_PROPERTY_NAME = "account-plugin-implementor";
	private static final String CIRCULATION_PLUGIN_IMPLEMENTOR__PROPERTY_NAME = "circulation-plugin-implementor";
	private static final String COMMUNITY_PLUGIN_IMPLEMENTOR__PROPERTY_NAME = "community-plugin-implementor";
	private static final String LIBRARY_PLUGIN_IMPLEMENTOR__PROPERTY_NAME = "library-plugin-implementor";
	private static final String HOLDING_DATA_VIEWER_ENABLED_PROPERTY_NAME = "holding-data-viewer-enabled";
	private static final String MLT_ENABLED_PROPERTY_NAME = "more-like-this-viewer-enabled";
	private static final String TH_EXPLORER_ENABLED_PROPERTY_NAME = "th-explorer-enabled";
	private static final String TH_EXPLORER_STANDALONE_PROPERTY_NAME = "th-explorer-standalone";
	private static final String FEDERATED_SEARCH_ENABLED_PROPERTY_NAME = "federated-search-enabled";
	private static final String DESIDERATA_ENABLED_PROPERTY_NAME = "desiderata-enabled";
	private static final String PIN_FILTERS_ENABLED_PROPERTY_NAME = "pin-filters-capability-enabled";
	private static final String FIELD_COLLAPSING_ENABLED_PROPERTY_NAME = "field-collapsing-enabled";
	
	private boolean accountPluginEnabled;
	private boolean circulationPluginEnabled;	
	private boolean communityPluginEnabled;
	
	private boolean holdingDataViewerEnabled;
	private boolean moreLikeThisViewerEnabled;
	
	private boolean thExplorerEnabled;
	private boolean thExplorerStandalone;
	
	private boolean desiderataEnabled;
	
	private AccountPlugin accountPlugin;
	private CirculationPlugin circulationPlugin;
	private CommunityPlugin communityPlugin;
	
	private boolean libraryPluginEnabled;
	private LibraryPlugin libraryPlugin;
	
	private boolean federatedSearchEnabled;
	private boolean pinFiltersEnabled;
	private boolean fieldCollapsingEnabled;
		
	@Override
	protected void configure(final ValueParser values)
	{
		super.configure(values);
				
		federatedSearchEnabled = values.getBoolean(FEDERATED_SEARCH_ENABLED_PROPERTY_NAME, false);
		
		holdingDataViewerEnabled = values.getBoolean(HOLDING_DATA_VIEWER_ENABLED_PROPERTY_NAME, false);
		moreLikeThisViewerEnabled = values.getBoolean(MLT_ENABLED_PROPERTY_NAME, false);
		thExplorerEnabled = values.getBoolean(TH_EXPLORER_ENABLED_PROPERTY_NAME, false);
		thExplorerStandalone = values.getBoolean(TH_EXPLORER_STANDALONE_PROPERTY_NAME, true);
		
		desiderataEnabled = values.getBoolean(DESIDERATA_ENABLED_PROPERTY_NAME, false);
		pinFiltersEnabled = values.getBoolean(PIN_FILTERS_ENABLED_PROPERTY_NAME, false);
		fieldCollapsingEnabled = values.getBoolean(FIELD_COLLAPSING_ENABLED_PROPERTY_NAME, false);
		configureDatabasePlugins(values);
	}

	/**
	 * Returns a valid instance of the account plugin.
	 * Returns null if this plugin hasn't been activated.
	 * 
	 * @return a valid instance of the account plugin, null if is not activated.
	 */ 
	public AccountPlugin getAccountPlugin()
	{
		return accountPlugin;
	}
	
	/**
	 * Returns a valid instance of the community plugin.
	 * 
	 * @return a valid instance of the circulation plugin, null if is not activated.
	 */
	public CommunityPlugin getCommunityPlugin()
	{
		return communityPlugin;
	}
	
	/**
	 * Returns a valid instance of the circulation plugin.
	 * Returns null if this plugin hasn't been activated.
	 * 
	 * @return a valid instance of the circulation plugin, null if is not activated.
	 */
	public CirculationPlugin getCirculationPlugin()
	{
		return circulationPlugin;
	}

	/**
	 * Returns true if the account plugin has been enabled.
	 * 
	 * @return true if the account plugin has been enabled.
	 */
	public boolean isAccountPluginEnabled()
	{
		return accountPluginEnabled;
	}

	/**
	 * Returns true if the community plugin has been enabled.
	 * 
	 * @return true if the community plugin has been enabled.
	 */
	public boolean isCommunityPluginEnabled()
	{
		return accountPluginEnabled && communityPluginEnabled;
	}
	
	/**
	 * Returns true if the circulation plugin has been enabled.
	 * 
	 * @return true if the circulation plugin has been enabled.
	 */
	public boolean isCirculationPluginEnabled()
	{
		return circulationPluginEnabled;
	}

	/**
	 * Returns true if the library plugin has been enabled.
	 * 
	 * @return true if the library plugin has been enabled.
	 */
	public boolean isLibraryPluginEnabled() 
	{
		return libraryPluginEnabled;
	}

	/**
	 * Returns the library plugin associated with this OGW instance.
	 * 
	 * @return the library plugin associated with this OGW instance.
	 */
	public LibraryPlugin getLibraryPlugin() 
	{
		return libraryPlugin;
	}

	/**
	 * Returns true if holding data viewer has been enabled.
	 * Note that the viewer needs its "functional" counterpart on Osee Genius -S- side.
	 * 
	 * @return true if holding data viewer has been enabled.
	 */
	public boolean isHoldingDataViewerEnabled() 
	{
		return holdingDataViewerEnabled;
	}
	
	/**
	 * Returns true if more like this viewer has been enabled.
	 * 
	 * @return true if more like this viewer has been enabled.
	 */
	public boolean isMoreLikeThisViewerEnabled() 
	{
		return moreLikeThisViewerEnabled;
	}
	
	/**
	 * Returns true if thesaurus explorer component has been enabled.
	 * 
	 * @return true if thesaurus explorer component has been enabled.
	 */	
	public boolean isThExplorerEnabled()
	{
		return thExplorerEnabled;
	}

	/**
	 * Returns true if federated search has been enabled.
	 * 
	 * @return true if federated search has been enabled.
	 */	
	public boolean isFederatedSearchEnabled()
	{
		return federatedSearchEnabled;
	}
	
	/**
	 * Returns true if thesaurus explorer component has been enabled in standalone mode.
	 * 
	 * @return true if thesaurus explorer component has been enabled in standalone mode.
	 */	
	public boolean isThExplorerStandalone()
	{
		return thExplorerEnabled && thExplorerStandalone;
	}

	/**
	 * Returns true if desiderata has been enabled.
	 * 
	 * @return true if desiderata has been enabled.
	 */
	public boolean isDesiderataEnabled()
	{
		return desiderataEnabled;
	}
	
	/**
	 * Configures plugins that do database operations.
	 * 
	 * @param values the incoming configuration settings.
	 */
	private void configureDatabasePlugins(final ValueParser values)
	{
		try
		{
			Context context = new InitialContext();			
			try 
			{
				accountPlugin = PluginFactory.lookup(values.getString(ACCOUNT_PLUGIN_IMPLEMENTOR_PROPERTY_NAME), context, values);
				accountPluginEnabled = true;
			} catch (Exception exception) 
			{
				accountPlugin = new NullObjectAccountPlugin();
				accountPluginEnabled = false;
				Log.info(MessageCatalog._000006_ACCOUNT_PLUGIN_DISABLED);
			}
			
			try 
			{
				communityPlugin = PluginFactory.lookup(values.getString(COMMUNITY_PLUGIN_IMPLEMENTOR__PROPERTY_NAME), context, values);
				communityPluginEnabled = true;
			} catch (Exception exception) 
			{
				communityPlugin = new NullObjectCommunityPlugin();
				communityPluginEnabled = false;
				Log.info(MessageCatalog._000010_COMMUNITY_PLUGIN_DISABLED);
			}
			
			try 
			{
				libraryPlugin = PluginFactory.lookup(values.getString(LIBRARY_PLUGIN_IMPLEMENTOR__PROPERTY_NAME), context, values);
				libraryPluginEnabled = true;
			} catch (Exception exception) 
			{
				libraryPlugin = new NullObjectLibraryPlugin();
				libraryPluginEnabled = false;
				Log.info(MessageCatalog._000011_LIBRARY_PLUGIN_DISABLED);
			}			

			try 
			{
				circulationPlugin = PluginFactory.lookup(values.getString(CIRCULATION_PLUGIN_IMPLEMENTOR__PROPERTY_NAME), context, values);
				circulationPluginEnabled = true;
			} catch (Exception exception) 
			{
				circulationPlugin = new NullObjectCirculationPlugin();
				circulationPluginEnabled = false;
				Log.info(MessageCatalog._000007_CIRCULATION_PLUGIN_DISABLED);
			}		
		} catch (NamingException exception)
		{
			Log.error(MessageCatalog._000005_NAMING_SERVICE_FAILURE, "");
		}		
	}

	/**
	 * Returns true if field collapsing has been enabled on this Oseegenius instance.
	 * 
	 * @return true if field collapsing has been enabled on this Oseegenius instance.
	 */
	public boolean isFieldCollapsingEnabled() 
	{
		return fieldCollapsingEnabled;
	}	
	
	/**
	 * Returns true if filters can be blocked on this Oseegenius instance.
	 * 
	 * @return true if filters can be blocked on this Oseegenius instance.
	 */
	public boolean isPinFiltersEnabled() 
	{
		return pinFiltersEnabled;
	}	
}
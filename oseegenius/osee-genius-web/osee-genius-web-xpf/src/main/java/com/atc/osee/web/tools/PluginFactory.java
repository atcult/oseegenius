package com.atc.osee.web.tools;

import javax.naming.Context;
import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.log.Log;
import com.atc.osee.web.plugin.DatabasePlugin;

/**
 * A factory for OseeGenius -W- plugins.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class PluginFactory 
{
	/**
	 * Creates a database plugin.
	 * 
	 * @param configurationParameter the configuration of the plugin.
	 * @param namingContext the naming context.
	 * @param <T> nothing.
	 * @return the concrete implementation of the requested plugin.
	 * @throws Exception in case the plugin cannot be created.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DatabasePlugin> T lookup(final String configurationParameter, final Context localNamingContext, final ValueParser values) throws Exception
	{
		try 
		{
			if (configurationParameter != null && configurationParameter.trim().length() != 0)
			{
				String [] clazzAndDriver = configurationParameter.split("-");
				T plugin = (T) Class.forName(clazzAndDriver[0]).newInstance();
				
				if (clazzAndDriver.length > 1)
				{ 
					DataSource datasource = (DataSource) localNamingContext.lookup(clazzAndDriver[1]);
					plugin.init(datasource);
				}
				plugin.init(values);
				return plugin;
			}
			throw new IllegalArgumentException();
		} catch (Exception exception)
		{
			Log.debug(exception);
			throw exception;
		}					
	}
}

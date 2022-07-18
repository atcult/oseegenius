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
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.IConstants;
import com.atc.osee.web.plugin.CoverPlugin;
import com.atc.osee.web.plugin.EmbeddedCoverPlugin;

/**
 * Cover plugin manager tool.
 * Covers are handled in OseeGenius using a so-called provider-chain, that is, a
 * list of cover providers that will be queried sequentially until a cover for a given item 
 * is not found.
 * 
 * This component is the main responsible of that chain.
 * 
 * @author agazzarini
 * @since 1.0
 */
@DefaultKey("cover")
@ValidScope(Scope.APPLICATION)
public class CoverPluginManagerTool extends SafeConfig
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CoverPluginManagerTool.class);
	
	private List<CoverPlugin> chainOfResponsibility;
	private String isbnAttributeName;

	private int smallCoverHeight;
	private int smallCoverWidth;
	private int mediumCoverHeight;
	private int mediumCoverWidth;
	private int bigCoverHeight;
	private int bigCoverWidth;
	
	private EmbeddedCoverPlugin embeddedCoverPlugin;
	
	@Override
	protected void configure(final ValueParser values)
	{
		try 
		{
			embeddedCoverPlugin = (EmbeddedCoverPlugin) Class.forName(values.getString("embedded-url-strategy")).newInstance();
			embeddedCoverPlugin.configure(values);
		} catch (Exception exception)
		{
			LOGGER.debug("Unable to get & create the embedded cover strategy.", exception);
		}

		smallCoverHeight = values.getInt("small-cover-height", 70);
		smallCoverWidth = values.getInt("small-cover-width", 50);
		mediumCoverHeight = values.getInt("medium-cover-height", 180);
		mediumCoverWidth = values.getInt("medium-cover-width", 120);
		bigCoverHeight = values.getInt("big-cover-height", 300);
		bigCoverWidth = values.getInt("big-cover-width", 180);

		isbnAttributeName = values.getString("isbn-attribute-name", "isbn");
		
		String pluginNames = values.getString("chain-of-responsibility");
		if (pluginNames != null && pluginNames.trim().length() != 0)
		{
			String [] pluginClazzNames = pluginNames.split(IConstants.COMMA);
			chainOfResponsibility = new ArrayList<CoverPlugin>(pluginClazzNames.length);
			
			for (String clazzName : pluginClazzNames)
			{
				try 
				{
					CoverPlugin plugin = (CoverPlugin)Class.forName(clazzName).newInstance();
					plugin.configure(values);
					chainOfResponsibility.add(plugin);
					LOGGER.debug("Added a new CoverPlugin to Cover Chain of Responsibility: " + clazzName);
				} catch (Exception exception) 
				{
					LOGGER.error("Cannot instantiate " + clazzName + " as a valid CoverPlugin", exception);					
				}
			}
		}
	}
	
	/**
	 * Returns the managed provider chain.
	 * 
	 * @return the managed provider chain.
	 */
	public List<CoverPlugin> getChainOfResponsibility()
	{
		return chainOfResponsibility;
	}

	/**
	 * Returns the name of the attribute that will be used as ISBN identifier.
	 * This kind of configuration is needed because some provider couldn't use the 
	 * "isbn" or "ISBN" word for identyfing that attribute.
	 * 
	 * @return the name of the attribute that will be used as ISBN identifier.
	 */
	public String getIsbnAttributeName() 
	{
		return isbnAttributeName;
	}
	
	/**
	 * Returns the URL of the medium cover of the given document.
	 * 
	 * @param document the SOLR document.
	 * @return the URL of the medium cover of the given document.
	 */
	public String getMediumCoverUrl(final SolrDocument document) 
	{
		return embeddedCoverPlugin.getMediumCoverUrl(document);
	}

	/**
	 * Returns the URL of the small cover of the given document.
	 * 
	 * @param document the SOLR document.
	 * @return the URL of the small cover of the given document.
	 */
	public String getSmallCoverUrl(final SolrDocument document) 
	{
		return embeddedCoverPlugin.getSmallCoverUrl(document);
	}
	
	/**
	 * Returns the URL of the big cover of the given document.
	 * 
	 * @param document the SOLR document.
	 * @return the URL of the big cover of the given document.
	 */
	public String getBigCoverUrl(final SolrDocument document) 
	{
		return embeddedCoverPlugin.getBigCoverUrl(document);
	}

	/**
	 * Returns the height (In pixel) that will be used for small-size covers.
	 * 
	 * @return the height (In pixel) that will be used for small-size covers.
	 */
	public int getSmallCoverHeight() 
	{
		return smallCoverHeight;
	}
	
	/**
	 * Returns the width (In pixel) that will be used for small-size covers.
	 * 
	 * @return the width (In pixel) that will be used for small-size covers.
	 */
	public int getSmallCoverWidth() 
	{
		return smallCoverWidth;
	}
	
	/**
	 * Returns the height(In pixel) that will be used for medium-size covers.
	 * 
	 * @return the height (In pixel) that will be used for medium-size covers.
	 */
	public int getMediumCoverHeight() 
	{
		return mediumCoverHeight;
	}

	/**
	 * Returns the width (in pixel) that will be used for medium-size covers.
	 * 
	 * @return the width (in pixel) that will be used for medium-size covers.
	 */
	public int getMediumCoverWidth() 
	{
		return mediumCoverWidth;
	}

	/**
	 * Returns the height (in pixel) that will be used for big-size covers.
	 * 
	 * @return the height (in pixel) that will be used for big-size covers.
	 */
	public int getBigCoverHeight() 
	{
		return bigCoverHeight;
	}

	/**
	 * Returns the width (in pixel) that will be used for big-size covers.
	 * 
	 * @return the width(in pixel) that will be used for big-size covers.
	 */
	public int getBigCoverWidth() 
	{
		return bigCoverWidth;
	}		
}
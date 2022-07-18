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

import java.io.Serializable;

import com.atc.osee.web.IConstants;

/**
 * External search engine value object.
 * Wraps configuration parameters of an external search provider that 
 * has been  configured on this OseeGenius -W- instance.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ExternalSearchProvider implements Serializable 
{
	private static final long serialVersionUID = 5457765330054258234L;
	
	private final String icon;
	private final String label;
	private final String url;
	
	/**
	 * Builds a new external search with the given data.
	 * 
	 * @param icon the provider icon. Reccommended a 24X24 image. 
	 * @param url the provider (search) url.
	 */
	public ExternalSearchProvider(final String icon, final String url)
	{
		this(icon, IConstants.EMPTY_STRING, url);
	}
	
	/**
	 * Builds a new external search with the given data.
	 * 
	 * @param icon the provider icon. Reccommended a 24X24 image. 
	 * @param label the provider label.
	 * @param url the provider (search) url.
	 */
	public ExternalSearchProvider(final String icon, final String label, final String url)
	{
		this.icon = icon;
		this.label = label;
		this.url = url;
	}
	
	/**
	 * Returns the icon of this provider.
	 * 
	 * @return the icon of this provider.
	 */
	public String getIcon()
	{
		return icon;
	}
	
	/**
	 * Returns the search url of this provider.
	 * 
	 * @return the search url of this provider.
	 */
	public String getUrl()
	{
		return url;
	}	
	
	/**
	 * Returns the label of this provider.
	 * 
	 * @return the label of this provider.
	 */
	public String getLabel()
	{
		return label;
	}
}
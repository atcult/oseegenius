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
package com.atc.osee.web.plugin.cover;

import java.util.Locale;

import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.plugin.CoverPlugin;

/**
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class SyndeticsProviderCoverPlugin implements CoverPlugin
{
	private static final String URL_PREFIX = "http://syndetics.com/index.aspx?isbn=";

	private String username;
	
	@Override
	public void configure(final Object data) 
	{
		ValueParser configuration = (ValueParser) data;
		username = configuration.getString("syndetics-account");
	}

	@Override
	public String getSmallCoverUrl(final String isbn, final String an, Locale locale) 
	{
		return URL_PREFIX + isbn + "/sc.gif&client=" + username;
	}
	
	@Override
	public String getMediumCoverUrl(final String isbn, final String an, Locale locale) 
	{
		return URL_PREFIX + isbn + "/mc.gif&client=" + username;
	}
	
	@Override
	public String getBigCoverUrl(final String isbn, final String an, Locale locale) 
	{
		return URL_PREFIX + isbn + "/lc.gif&client=" + username;
	}	
}

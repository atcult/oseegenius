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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.atc.osee.web.plugin.CoverPlugin;

/** 
 * Plugin provider for cbt cover on filesystem
 * @author Alice Guercio
 * @since 1.2
 */
public class FileCoverPlugin implements CoverPlugin
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileCoverPlugin.class);	
	
	@Override
	public String getSmallCoverUrl(final String isbn, final String an, Locale locale) 
	{
		return getFileCover(an);  
	}
	
	@Override
	public String getMediumCoverUrl(final String isbn, final String an, Locale locale)  
	{
		return getFileCover(an);  
	}

	@Override
	public String getBigCoverUrl(final String isbn, final String an, Locale locale) 
	{
		return getFileCover(an);  
	}

	@Override
	public void configure(final Object data) 
	{
		// Nothing to be done here...
	}	
	
	private String getFileCover(String an) {
		String SUFFIX = "_FCOV.jpg";
		return "img/cover/" + an + SUFFIX;		
	}
}
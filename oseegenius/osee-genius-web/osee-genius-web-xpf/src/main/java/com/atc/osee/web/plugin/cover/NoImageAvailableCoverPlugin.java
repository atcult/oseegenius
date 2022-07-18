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

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.plugin.CoverPlugin;

/**
 *  A plugin that ignores that document identifier and load no-cover images.
 *  
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class NoImageAvailableCoverPlugin implements CoverPlugin
{
	private String italianNoCoverSmallImage;
	private String italianNoCoverMediumImage;
	private String italianNoCoverBigImage;

	private String englishNoCoverSmallImage;
	private String englishNoCoverMediumImage;
	private String englishNoCoverBigImage;

	private boolean useI18nImages;

	private String hostname;
	
	@Override
	public String getSmallCoverUrl(final String isbn, final String an, Locale locale) 
	{
		if (!useI18nImages || Locale.ITALIAN.equals(locale))
		{
			return italianNoCoverSmallImage;
		} else
		{
			return englishNoCoverSmallImage;
		}
	}
	
	@Override
	public String getMediumCoverUrl(final String isbn, final String an, Locale locale) 
	{
		if (!useI18nImages || Locale.ITALIAN.equals(locale))
		{
			return italianNoCoverMediumImage;
		} else
		{
			return englishNoCoverMediumImage;
		}		
	}

	@Override
	public String getBigCoverUrl(final String isbn, final String an, Locale locale) 
	{
		if (!useI18nImages || Locale.ITALIAN.equals(locale))
		{
			return italianNoCoverBigImage;
		} else
		{
			return englishNoCoverBigImage;
		}			
	}	
	
	@Override
	public void configure(final Object data) 
	{
		ValueParser configuration = (ValueParser) data;
		
		useI18nImages = configuration.getBoolean("use-no-cover-i18n-images", false);
		
		hostname = configuration.getString("hostname", null);
		
		italianNoCoverSmallImage = createURL(configuration.getString("no-cover-small-image-path"), configuration);
		italianNoCoverMediumImage = createURL(configuration.getString("no-cover-medium-image-path"), configuration);
		italianNoCoverBigImage = createURL(configuration.getString("no-cover-big-image-path"), configuration);
		
		if (useI18nImages)
		{
			englishNoCoverSmallImage = createURL(configuration.getString("eng-no-cover-small-image-path"), configuration);
			englishNoCoverMediumImage = createURL(configuration.getString("eng-no-cover-medium-image-path"), configuration);
			englishNoCoverBigImage = createURL(configuration.getString("eng-no-cover-big-image-path"), configuration);
		}
	}
	
	/**
	 * Creates the base URL that will be used for the image.
	 * 
	 * @param imagePath the image path.
	 * @param configuration the OseeGenius -W- (velocity) configuration
	 * @return the base URL that will be used for the image.
	 */
	private String createURL(final String imagePath, final ValueParser configuration)
	{
		if (imagePath.startsWith("http")) 
		{
			return imagePath;
		}
		
		HttpServletRequest request = (HttpServletRequest) configuration.get("request");
		
		String slash = "/";
		String value = (imagePath.startsWith(slash)) ? imagePath : slash + imagePath;
		
		String s = new StringBuilder()
			.append(request.getScheme())
			.append("://")
			.append(hostname != null ? hostname : request.getServerName())
			.append(":" + request.getServerPort())
			.append(request.getContextPath())
			.append(value)
			.toString();
		return s;
	}
}

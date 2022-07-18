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
package com.atc.osee.web.plugin;

import java.util.Locale;

/**
 * OseeGenius -W- cover plugin.
 * 
 * @author Giorgio Gazzarini
 * @since 1.0
 */
public interface CoverPlugin extends Plugin
{
	/**
	 * Returns the URL of the small cover for the given document.
	 * 
	 * @param isbn the document isbn.
	 * @return the URL of the small cover for the given document.
	 */
	String getSmallCoverUrl(String isbn, String an, Locale locale);

	/**
	 * Returns the URL of the medium cover for the given document.
	 * 
	 * @param isbn the document isbn.
	 * @return the URL of the medium cover for the given document.
	 */
	String getMediumCoverUrl(String isbn, String an, Locale locale);
	
	/**
	 * Returns the URL of the big cover for the given document.
	 * 
	 * @param isbn the document isbn.
	 * @return the URL of the big cover for the given document.
	 */
	String getBigCoverUrl(String isbn, String an, Locale locale);	
}
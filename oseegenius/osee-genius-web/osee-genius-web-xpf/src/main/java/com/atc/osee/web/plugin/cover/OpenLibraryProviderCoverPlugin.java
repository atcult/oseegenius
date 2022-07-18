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

import com.atc.osee.web.plugin.CoverPlugin;

/**
 * Book covers can be accessed using ISBN.
 * The covers are available in 3 sizes:
 * S: Small, suitable for use as a thumbnail on a results page on Open Library,
 * M: Medium, suitable for display on a details page on Open Library and,
 * L: Large
 *  
 *  The URL pattern to access book covers is:
 *  
 *  http://covers.openlibrary.org/b/$key/$value-$size.jpg
 *  
 *  Where:
 *  
 *  key can be any one of ISBN, OCLC, LCCN, OLID and ID (case-insensitive)
 *  value is the value of the chosen key size can be one of S, M and L for small, medium and large respectively.
 *  By default it returns a blank image if the cover cannot be found. If you append ?default=false to the end of the URL, then it returns a 404 instead.
 *  
 *  The following example returns small sized cover image for book with ISBN 0385472579.
 *  
 *  http://covers.openlibrary.org/b/isbn/0385472579-S.jpg 
 * 
 * http://openlibrary.org/dev/docs/api/covers
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class OpenLibraryProviderCoverPlugin implements CoverPlugin
{
	private static final String URL_PREFIX = "http://covers.openlibrary.org/b/isbn/";
	
	@Override
	public String getSmallCoverUrl(final String isbn, final String an, Locale locale) 
	{
		return URL_PREFIX + isbn.replace("-","") + "-S.jpg?default=false";
	}
	
	@Override
	public String getMediumCoverUrl(final String isbn, final String an, Locale locale) 
	{
		return URL_PREFIX + isbn.replace("-","") + "-M.jpg?default=false";
	}

	@Override
	public String getBigCoverUrl(final String isbn, final String an,Locale locale) 
	{
		return URL_PREFIX + isbn.replace("-","") + "-L.jpg?default=false";
	}

	@Override
	public void configure(final Object data) 
	{
		// Nothing to be done here...
	}
}
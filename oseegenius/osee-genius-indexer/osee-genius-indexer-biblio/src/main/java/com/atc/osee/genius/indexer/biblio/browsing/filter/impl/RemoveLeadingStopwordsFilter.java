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
package com.atc.osee.genius.indexer.biblio.browsing.filter.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.core.SolrCore;

import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;

/**
 * A filter that removes a predefined set of leading words.
 * At the moment the implementation looks for a file (in conf directory) named 
 * leading-stopwords.txt
 * 
 * @author agazzarini
 * @since 1.0
 */
public class RemoveLeadingStopwordsFilter implements IHeadingFilter 
{
	private static final String FILENAME = "leading-stopwords.txt";
	List<String> leadingStopwords;
	
	@Override
	public String doFilter(final String heading) 
	{
		if (heading != null)// && (heading.indexOf(' ') <= 5 || heading.indexOf('\'') <= 5))
		{
			for (String leadingStopWord : leadingStopwords)
			{
				if (heading.toLowerCase().startsWith(leadingStopWord))
				{
					return heading.substring(leadingStopWord.length()).trim();
				}
			}
		}
		return heading;
	}

	@Override
	public void init(final SolrCore core) throws Exception
	{
		List<String> fileContent = core.getResourceLoader().getLines(FILENAME);
		leadingStopwords = new ArrayList<String>(fileContent.size());
		for (Iterator<String> iterator = fileContent.iterator(); iterator.hasNext();) 
		{
			String word = iterator.next();
			if (!word.endsWith("'"))
			{
				leadingStopwords.add(word + " ");
			} else 
			{
				leadingStopwords.add(word);
			}
		}
		
		fileContent.clear();
	}
}

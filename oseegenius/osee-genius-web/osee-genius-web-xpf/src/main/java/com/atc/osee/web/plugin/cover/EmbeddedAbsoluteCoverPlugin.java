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

import org.apache.solr.common.SolrDocument;
import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.plugin.EmbeddedCoverPlugin;

/**
 * Default cover plugin.
 * It assumes a "medium" and "small" (absolute) cover link exists onto the given document(s),
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class EmbeddedAbsoluteCoverPlugin implements EmbeddedCoverPlugin 
{
	private String smallCoverAttributeName;
	private String mediumCoverAttributeName;
	private String bigCoverAttributeName;
	
	@Override
	public String getMediumCoverUrl(final SolrDocument document)
	{
		String url = (String) document.get(mediumCoverAttributeName);
		return url != null ? url : "";
	}
 
	@Override
	public String getSmallCoverUrl(final SolrDocument document)
	{
		String url =  (String) document.get(smallCoverAttributeName);
		return url != null ? url : "";
	}

	@Override
	public String getBigCoverUrl(final SolrDocument document)
	{
		String url =  (String) document.get(bigCoverAttributeName);
		return url != null ? url : "";
	}

	@Override
	public void configure(final Object data)
	{
		ValueParser configuration = (ValueParser) data;
		smallCoverAttributeName = configuration.getString("small-cover-embedded-attribute-name", ISolrConstants.SMALL_COVER_URL);
		mediumCoverAttributeName = configuration.getString("medium-cover-embedded-attribute-name", ISolrConstants.MEDIUM_COVER_URL);
		mediumCoverAttributeName = configuration.getString("big-cover-embedded-attribute-name", ISolrConstants.BIG_COVER_URL);
	}
}

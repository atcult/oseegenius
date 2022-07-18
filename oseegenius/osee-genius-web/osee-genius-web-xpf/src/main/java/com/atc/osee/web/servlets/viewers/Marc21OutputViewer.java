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
package com.atc.osee.web.servlets.viewers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;

import com.atc.osee.web.ISolrConstants;

/**
 * Web controller for MARC21 perspective view mode.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Marc21OutputViewer 
{
	/**
	 * Injects onto the given set of SOLR document, the corresponding MARC21 raw data.
	 * 
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param queryResponse the SOLR response.
	 * @throws IOException in case of I/O failure.
	 */
	public void format(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final QueryResponse queryResponse) throws IOException
	{		
		 for (SolrDocument document : queryResponse.getResults())
		 {
			 document.setField("marc21", solr2StringifiedMarc((String) document.get(ISolrConstants.MARC_21_FIELD_NAME)));
		 }
	}
	
	/**
	 * Converts the incoming RAW data into a MARC21 string.
	 * 
	 * @param rawData the incoming raw data (as it is stored on SOLR).
	 * @return the MARC21 string.
	 */
	public String solr2StringifiedMarc(final String rawData)
	{
		InputStream inputStream = null;
		try 
		{
			byte [] bytes = rawData.getBytes("UTF-8");
			inputStream = new ByteArrayInputStream(bytes);
			
			Record record = null;
			MarcReader reader = new MarcXmlReader(inputStream);
			if (reader.hasNext())
			{
				record = reader.next();
			}
			if (record != null)
			{
				return record.toString().replaceAll("\n", "<br/>");
			}
		} catch (Exception exception) 
		{
			exception.printStackTrace();
		} 
		return "";
	}		
}

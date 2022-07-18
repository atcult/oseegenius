package com.atc.osee.genius.searcher.response.writer;

import java.io.IOException;
import java.io.Writer;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;

public class MarcXmlResponseWriter implements QueryResponseWriter {

	@Override
	public void write(
			final Writer writer, 
			final SolrQueryRequest request, 
			final SolrQueryResponse response) throws IOException 
	{
		XMLWriter.writeResponse(writer, request, response);
	}

	@Override
	public String getContentType(final SolrQueryRequest request, final SolrQueryResponse response) 
	{
		 return CONTENT_TYPE_XML_UTF8;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void init(final NamedList args) 
	{
		// Nothing to be done here
	}
}

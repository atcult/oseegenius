package com.atc.osee.genius.searcher.response.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;

/**
 * OseeGenius -S- optimized response writer for autocomplete feature.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AutocompleteResponseWriter implements QueryResponseWriter 
{	
	private final static Set<String> FIELDS = new HashSet<String>(1);
	static 
	{
		FIELDS.add("label");
	}
	
	/** 
	 * Response writer outputs a JSON like this:
	{
		 query:'Li',
		 suggestions:['Liberia','Libyan Arab Jamahiriya','Liechtenstein','Lithuania']
	}
	*/
	@SuppressWarnings("rawtypes")
	@Override
	public void write(
			final Writer writer, 
			final SolrQueryRequest request, 
			final SolrQueryResponse response) throws IOException 
	{
		NamedList elements = response.getValues();
		StringBuilder builder = new StringBuilder("{")
			.append("query:'")
			//escape apostrofo per la risposta ajax dell'autocompletamento
			.append(request.getParams().get(CommonParams.Q).replaceAll("'", "\\\\'"))
			.append("',");
		
		Object value = elements.getVal(1);		
		if (value instanceof ResultContext)
		{
			ResultContext context = (ResultContext) value;
			DocList ids = context.docs;
			if (ids != null)
			{
				SolrIndexSearcher searcher = request.getSearcher();
				DocIterator iterator = ids.iterator();
				builder.append("suggestions:[");
				for (int i = 0; i < ids.size(); i++)
				{
					Document document = searcher.doc(iterator.nextDoc(), FIELDS);
					if (i > 0)  { builder.append(","); }
					builder
						.append("'")
						.append(((String) document.get("label")).replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\""))
						.append("'");
				}
				builder.append("]").append("}");
			}
		}
		writer.write(builder.toString());
	}

	@Override
	public String getContentType(final SolrQueryRequest request, final SolrQueryResponse response) 
	{
		 return "application/json;  charset=UTF-8";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void init(final NamedList args) 
	{
		// Nothing to be done here
	}
}

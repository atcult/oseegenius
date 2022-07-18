package com.atc.osee.genius.indexer.biblio.urp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

public class DiacriticsReplacementUpdateRequestProcessor extends UpdateRequestProcessor 
{	
	private final Map<String, String> configuration; 
	private final String [] fieldNames;
	public DiacriticsReplacementUpdateRequestProcessor(
			final UpdateRequestProcessor next, 
			final Map<String, String> configuration, 
			final String [] fieldNames) 
	{
		super(next);
		this.configuration = configuration;
		this.fieldNames = fieldNames;
	}
	
	@Override
	public void processAdd(final AddUpdateCommand cmd) throws IOException 
	{
		if (fieldNames != null && fieldNames.length > 0)
		{
			for (String fieldName : fieldNames)
			{
				replace(cmd.getSolrInputDocument(), fieldName);
			}
		}
		super.processAdd(cmd);
	}
	
	@SuppressWarnings("unchecked")
	private void replace(final SolrInputDocument document, final String fieldName)
	{
		SolrInputField field = document.getField(fieldName);
		if (field != null)
		{
			Object currentValueAsObject = field.getValue();
			if (currentValueAsObject != null)
			{
				if (currentValueAsObject instanceof Collection)
				{
					Collection<Object> collection = (Collection<Object>) currentValueAsObject;
					List<String> result = new ArrayList<String>(collection.size());
					
					for (Object obj : collection)
					{
						result.add(replace((String) obj));
					}
					field.setValue(result, 1.0f);
				} else
				{
					field.setValue(replace((String) currentValueAsObject), 1.0f);					
				}
			}
		}
	}
	
	private String replace(String value)
	{
		for (Entry<String, String> entry : configuration.entrySet())
		{
			value = value.replace(entry.getKey(), entry.getValue());
		}
		return value;
	}
}
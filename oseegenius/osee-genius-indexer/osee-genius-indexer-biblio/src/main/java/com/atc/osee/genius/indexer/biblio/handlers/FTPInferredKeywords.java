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
package com.atc.osee.genius.indexer.biblio.handlers;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the FTP callnum and dewey hierarchy from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPInferredKeywords extends TagHandler implements IConstants
{
	static ResourceBundle callnumBundle = ResourceBundle.getBundle("callnum");
	static ResourceBundle deweysBundle = ResourceBundle.getBundle("dewey");
	
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		Set<String> result = new LinkedHashSet<String>();
		addDeweyKeywords(getDeweys(record), result);
		addCallNumberKeywords(getCallNumberFacetField(record, "050a"),result);
		return result;		
	}
	
	@SuppressWarnings("unchecked")
	private Set<String> getDeweys(final Record record) {
		List<VariableField> fields = record.getVariableFields("082");
		if (fields != null && !fields.isEmpty())
		{
			Set<String> result = new HashSet<String>();
			for (VariableField f : fields)
			{
				DataField field = (DataField) f;
				Subfield subfield = field.getSubfield('a');
				if (subfield != null)
				{
					String data = subfield.getData();
					if (data != null && data.trim().length() != 0)
					{
						result.add(data.trim());
					}
				}
			}
			return result;
		}
		return null;
	}

	private void addCallNumberKeywords(Set<String> source, Set<String> accumulator)
	{
		if (source != null)
		{
			for (String key : source) 
			{
				for(int i = 0; i < key.length(); i++)
				{
					addInferredKeyword(key.substring(0,i+1), callnumBundle, accumulator);
				}
			}		
		}
	}	
	
	private void addDeweyKeywords(Set<String> source, Set<String> accumulator)
	{
		if (source != null)
		{
			for (String key : source) 
			{
				int howManyZero = key.length()-1;
				for(int i = 0; i < key.length(); i++)
				{
					String keyPrefix = key.substring(0,i+1);
					for (int k = 0; k < howManyZero; k++)
					{
						keyPrefix+="0";
					}
					howManyZero--;
					
					addInferredKeyword(keyPrefix, deweysBundle, accumulator);
				}
			}		
		}
	}		
	
	@SuppressWarnings("unchecked")
	private Set<String> getCallNumberFacetField(Record record, String fieldSpec)
	{
		Set<String> result = null;
		String fieldName = fieldSpec.length() == 3 ? fieldSpec : fieldSpec.substring(0, 3);
		List<VariableField> fields = record.getVariableFields(fieldName);
		if (fields != null && !fields.isEmpty())
		{
			result = new LinkedHashSet<String>();
			for (VariableField f : fields)
			{
				DataField field = (DataField) f;
				Subfield subfield = field.getSubfield(fieldSpec.charAt(fieldSpec.length()-1));
				if (subfield != null)
				{
					String data = subfield.getData();
					if (data != null && data.trim().length() != 0)
					{
						int dotIndex = data.indexOf(".");
						if (dotIndex == -1)
						{
							result.add(data);							
						} else 
						{
							result.add(data.substring(0,dotIndex));
						}
					}
				}		
			}
		}
		return result;
	}	
	
	private void addInferredKeyword(String key, ResourceBundle bundle, Set<String> accumulator)
	{
		try 
		{
			if (key.length() > 3)
			{
				key = key.substring(0,3);
			}
			String label = bundle.getString(key).replace("&","").replace("(General)","");
			String [] keywords = label.split(",");
			for (String keyword : keywords) 
			{
				accumulator.add(keyword.trim());
			}
		} catch(Exception exception)
		{
			// Ignore
		}		
	}	
}
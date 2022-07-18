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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.TagHandler;
import com.atc.osee.genius.indexer.biblio.log.MessageCatalog;

/**
 * Starting from a dewey decimal classification extracts its whole hierarchy.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AssignClassificationHierarchyVerbalExpressions extends TagHandler 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AssignClassificationHierarchyVerbalExpressions.class);
	
	private static final Map<String, Properties> FILES = new HashMap<String, Properties>();
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
        Set<String> result = new LinkedHashSet<String>();

        String[] params = tagMappings.split(",");
        
        if (params == null || params.length != 3)
        {
        	LOGGER.error(String.format(
        			MessageCatalog._000012_WRONG_ARGUMENT_LIST, 
        			getClass().getName()));
        	return null;
        }
        
        String file = params [0];
        String tag = params[1];
        int minRequiredLength = Integer.parseInt(params[2]);
        
        try 
        {
	        Properties properties = getFileContent(file, core);
	        
	        String fieldTag = tag.substring(0, 3);
	        char subfieldCode = tag.charAt(3);
	
	        List<VariableField> variableFields = record.getVariableFields(fieldTag);
	        if (!variableFields.isEmpty())
	        {
	            for (VariableField variableField : variableFields)
	            {
	                DataField dataField = (DataField) variableField;
	                List<Subfield> subfields = dataField.getSubfields(subfieldCode);
	                for (Subfield subfield : subfields)
	                {
						String classificationCode = subfield.getData();
	                	if (classificationCode != null && classificationCode.trim().length() >= minRequiredLength)
	                	{
	                		visitHierarchy(result, properties, new StringBuilder(classificationCode.substring(0, minRequiredLength)));
	                	}
	                }
	            }
	        }
	        return result;
        } catch (StringIndexOutOfBoundsException exception)
        {
        	LOGGER.error(
        			String.format(MessageCatalog._000018_INVALID_EXPRESSION, tagMappings),
        			exception);        
        	return null;
        } catch (Exception exception)
        {
        	LOGGER.error(String.format(MessageCatalog._000011_CLASSIFICATION_FILE_READ_FAILURE, file), exception);
        	return null;
        }
	}
	
	/**
	 * Visit the decimal hierarchy of the given identifier (value).
	 *  
	 * @param result the accumulator that will store collected keywords.
	 * @param properties code / value list of decimal classifications.
	 * @param value the dewey decimal classification value.
	 */
	private void visitHierarchy(final Set<String> result, final Properties properties, final StringBuilder value)
	{
		addKeyword(result, properties, value);
		
		for (int index = (value.length() - 1); index > 0; index--)
		{
			value.setCharAt(index, '0');
			addKeyword(result, properties, value);
		}
	}
	
	/**
	 * Adds the decimal classification expression (keyword) associated with the given value.
	 * 
	 * @param result the accumulator that will store collected keywords.
	 * @param properties code / value list of decimal classifications.
	 * @param value the dewey decimal classification value.
	 */
	private void addKeyword(final Set<String> result, final Properties properties, final StringBuilder value)
	{
		String keyword = properties.getProperty(value.toString());
		if (keyword != null && keyword.trim().length() != 0)
		{
			result.add(keyword);
		}
	}
	
	/**
	 * Loads a dewey classification file onto a Properties object and return it.
	 * 
	 * @param name the name of the file,
	 * @param core the SOLR core.
	 * @return a properties with dewey decimal classification.
	 * @throws IOException in case of I/O failure.
	 */
	Properties getFileContent(final String name, final SolrCore core) throws IOException
	{
		Properties result = FILES.get(name);
		if (result == null)
		{
		     result = new Properties();
		     result.load(core.getResourceLoader().openResource(name));	
		     FILES.put(name, result);
		}
		return result;
	}
}
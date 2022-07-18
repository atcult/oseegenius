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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.TagHandler;
import com.atc.osee.genius.indexer.biblio.Utils;

/**
 * Extracts a linked field from a MARC record.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetLink extends TagHandler 
{	
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
        List<String> linkedFields = getValues("8806", record);

        if (linkedFields.isEmpty())
        {
            return linkedFields;
        }
        
        String[] tags = tagMappings.split(":");
        
        Set<String> result = new LinkedHashSet<String>();
        for (String tagMapping : tags)
        {
            String tag = tagMapping.substring(0, 3);
            String subfield = tagMapping.substring(3);

            String separator = null;
            if (subfield.indexOf('\'') != -1)
            {
                separator = subfield.substring(subfield.indexOf('\'') + 1, subfield.length() - 1);
                subfield = subfield.substring(0, subfield.indexOf('\''));
            }

            result.addAll(getLinkedFieldValue(record, tag, subfield, separator));
        }
        return result;
	}
	
	/**
	 * Extracts from the given record, a linked field.
	 * 
	 * @param record the MARC record.
	 * @param tag the source tag.
	 * @param subfield the source subfield.
	 * @param separator the separator.
	 * @return a linked value.
	 */
	@SuppressWarnings("unchecked")
	Set<String> getLinkedFieldValue(final Record record, final String tag, final String subfield, final String separator)
	{
		Set<String> result = new LinkedHashSet<String>();
		boolean havePattern = false;
		Pattern subfieldPattern = null;
		if (subfield.indexOf('[') != -1)
        {
            havePattern = true;
            subfieldPattern = Pattern.compile(subfield);
        }
		
        List<VariableField> fields = record.getVariableFields("880");
        for (VariableField vf : fields)
        {
            DataField dfield = (DataField) vf;
            Subfield link = dfield.getSubfield('6');
            if (link != null && link.getData().startsWith(tag))
            {
                List<Subfield> subList = dfield.getSubfields();
                StringBuffer buf = new StringBuffer("");
                for (Subfield subF : subList)
                {
                    boolean addIt = false;
                    if (havePattern)
                    {
                        Matcher matcher = subfieldPattern.matcher("" + subF.getCode());
                        addIt = matcher.matches();
                    } else
                    {
                    	addIt = subfield.indexOf(subF.getCode()) != -1;
                    }
                    if (addIt)
                    {
                        if (buf.length() > 0)
                        {
                            buf.append(separator != null ? separator : " ");
                        }
                        buf.append(subF.getData().trim());
                    }
                }
                if (buf.length() > 0) 
                {
                    result.add(Utils.cleanData(buf.toString()));
                }
            }
        }
        return result;
	}
}
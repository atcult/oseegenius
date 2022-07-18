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

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the FTP content type from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPSortAuthor extends TagHandler implements IConstants
{
	GetAlphabeticalSubfields alpha = new GetAlphabeticalSubfields();
	
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
        StringBuilder resultBuf = new StringBuilder();

        DataField df = (DataField) record.getVariableField("100");
        // main entry personal name
        if (df != null)
        	addSortString(alpha.getValue("100", record, settings, core, document), resultBuf);

        df = (DataField) record.getVariableField("110");
        // main entry corporate name
        if (df != null)
        	addSortString(alpha.getValue("110", record, settings, core, document), resultBuf);

        df = (DataField) record.getVariableField("111");
        // main entry meeting name
        if (df != null)
        	addSortString(alpha.getValue("111", record, settings, core, document), resultBuf);

        // #BZ : 4081 titoli senza autore ordinati alfabeticamente assieme a quelli con autore.
		// need to sort fields missing 100/110/111 last
		//        if (resultBuf.length() == 0)
		//        {
		//            resultBuf.append(Character.toChars(Character.MAX_CODE_POINT));
		//            resultBuf.append(' '); 
		//        }

        // uniform title, main entry
        df = (DataField) record.getVariableField("240");
        if (df != null)
        	resultBuf.append(getSubfieldA(df, true));

        // 245 (required) title statement
        df = (DataField) record.getVariableField("245");
        if (df != null)
        	resultBuf.append(getSubfieldA(df, true));
        
        // Solr field properties should convert to lowercase
        return resultBuf.toString().trim();
	}
	
	void addSortString(Object obj, StringBuilder builder) {
		if (obj instanceof Collection<?>) {
			builder.append(((Collection<?>)obj).iterator().next());
		} else {
			builder.append(obj);
		}
	}
	
	/**
	 * Returns the second indicator of the given datafield.
	 * 
	 * @param datafield the datafield.
	 * @return the second indicator.
	 */
	protected int getSecondIndicator(final DataField datafield)
    {
        char ind2char = datafield.getIndicator2();
        return (Character.isDigit(ind2char))
	        ? Integer.valueOf(String.valueOf(ind2char))
	        : 0;
    }	
	
	protected String getSubfieldA(final DataField field, boolean skip)
	{
		try 
		{
			if (field != null)
			{
				Subfield a = field.getSubfield('a');
				if (a != null)
				{
					if (skip)
					{
						int nonFilingInt = getSecondIndicator(field);
						String data = a.getData();
						return (String) data.substring(nonFilingInt);
					}
					return a.getData();
				}
			}
			return null;
		} catch (Exception exception)
		{
			return null;
		}
	}	
}
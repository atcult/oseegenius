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

import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import com.atc.osee.genius.indexer.biblio.TagHandler;
import com.atc.osee.genius.indexer.biblio.Utils;

/**
 * Extracts a publication date that can be used for sorting.
 * 
 * @author mbraddi
 * @since 1.3
 */
public class GetPublicationUnimarcDateSort extends TagHandler 
{
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document)
	{
	    int cYearInt = Calendar.getInstance().get(Calendar.YEAR);
		String cYearStr = Integer.toString(cYearInt);
		
		Subfield field100 = ((DataField) record.getVariableField("100")).getSubfield('a');
		
		if (field100 != null)
		{
	    	String dateAsString = field100.getData().substring(9, 13);
	    	dateAsString = 
	    			(dateAsString != null && dateAsString.trim().length() != 0)
	    			? dateAsString
	    			: field100.getData().substring(13, 17);
	    	
	    	if (dateAsString == null || dateAsString.trim().length() == 0)
	    	{
	    		return null;
	    	}
	    	
	    	if (isdddd(dateAsString))
	    	{
	    		return getValidPubDate(dateAsString, cYearInt + 1, 500, record);
	    	} else if (isdddu(dateAsString)) 
	    	{
	    		int myFirst3 = Integer.parseInt(dateAsString.substring(0, 3));
	    		int currFirst3 = Integer.parseInt(cYearStr.substring(0, 3));
	    		if (myFirst3 <= currFirst3)
	    		{
	        		return dateAsString.substring(0, 3) + "-";
	    		}
	    	} else if (isdduu(dateAsString)) 
	    	{
	    		int myFirst2 = Integer.parseInt(dateAsString.substring(0, 2));
	    		int currFirst2 = Integer.parseInt(cYearStr.substring(0, 2));
	    		if (myFirst2 <= currFirst2)
	    		{
	        		return dateAsString.substring(0, 2) + "--";
	    		}
	    	}
		}	
		return null;
	}
	
	/**
	 * Checks iif the given value is a four digits year.
	 * 
	 * @param value the input value.
	 * @return true if the given value is a four digits year.
	 */
	private boolean isdddd(final String value) 
	{
	    return Pattern.compile("^\\d{4}$").matcher(value).matches(); 
	}

	/**
	 * Checks iif the given value is a three digits year.
	 * 
	 * @param value the input value.
	 * @return true if the given value is a three digits year.
	 */
	private boolean isdddu(final String value) 
	{
	    return Pattern.compile("^\\d{3}u$").matcher(value).matches(); 
	}

	/**
	 * Checks iif the given value is a two digits year.
	 * 
	 * @param value the input value.
	 * @return true if the given value is a three digits year.
	 */
	private boolean isdduu(final String value) 
	{
	    return Pattern.compile("^\\d{2}uu$").matcher(value).matches();
	}
	
	/**
	 * Returns a valid publication date.
	 * 
	 * @param dateToCheck the input value.
	 * @param upperLimit the upper limit..
	 * @param lowerLimit the lower limit.
	 * @param record the MARC record.
	 * @return a valid publication date.
	 */
	private String getValidPubDate(final String dateToCheck, final int upperLimit, final int lowerLimit, final Record record) 
	{
		int dateInt = Integer.parseInt(dateToCheck);
		if (dateInt <= upperLimit) 
		{
			if (dateInt >= lowerLimit)
			{
				return dateToCheck;
			} else 
			{
	        	String date210d = getDate(record);
	        	if (date210d != null) 
	        	{
	        		int date210int = Integer.parseInt(date210d);
					if (date210int != 0 && date210int <= upperLimit && date210int >= lowerLimit)
					{
						return date210d;
					}
	        	}
			}
		}
		return null;
	}	
	
    /**
     * Return the date in 210d as a string.
     * 
     * @param record the MARC record.
     * @return the date in 210d as a string.
     */
	public String getDate(final Record record)
    {
        List<String> dates = getValues("210d", record);
        String result = Utils.join(dates, ",");
        return (result != null && result.length() != 0) ? Utils.cleanDate(result) : null;
    }		
}

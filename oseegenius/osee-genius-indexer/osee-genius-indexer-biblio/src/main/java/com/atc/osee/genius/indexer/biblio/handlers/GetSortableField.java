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

import java.util.List;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import com.atc.osee.genius.indexer.biblio.Decorator;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts a sortable field.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class GetSortableField extends TagHandler
{	
	private final static Decorator REMOVE_TRAILING_PUNCTUACTION = new RemoveTrailingPunctuationTagHandler();
	
	/**
	 * Extracts all alphabetical subfields and creates a sortable field.
	 * 
	 * @param datafield the target datafield.
	 * @param skipSubfieldC a flag indicating if the subfield c should be skept.
	 * @return all alphabetical subfields and creates a sortable field.
	 */
	@SuppressWarnings("unchecked")
	protected StringBuilder getAlphaSubfldsAsSortStr(final DataField datafield, final boolean skipSubfieldC)
    {
		StringBuilder result = new StringBuilder();
        int nonFilingInt = getSecondIndicator(datafield);
        boolean firstSubfld = true;

        for (Subfield subfield : (List<Subfield>) datafield.getSubfields())
        {
            char subcode = subfield.getCode();
            if (Character.isLetter(subcode) && (!skipSubfieldC || subcode != 'c'))
            {
                String data = subfield.getData();
                if (firstSubfld)
                {
                    if (nonFilingInt < data.length() - 1)
                    {
                        data = data.substring(nonFilingInt);
                    }
                    firstSubfld = false;
                }
                result.append(data.replaceAll("\\p{Punct}*", "").trim());
            }
        }
        return result;
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
						return (String) REMOVE_TRAILING_PUNCTUACTION.decorate("", data.substring(nonFilingInt));
					}
					return (String) REMOVE_TRAILING_PUNCTUACTION.decorate("", a.getData());
				}
			}
			return null;
		} catch (Exception exception)
		{
			return null;
		}
	}
}
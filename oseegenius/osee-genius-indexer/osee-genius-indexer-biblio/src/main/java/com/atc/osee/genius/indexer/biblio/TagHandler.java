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
package com.atc.osee.genius.indexer.biblio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

/**
 * Tag handler supertype layer.
 * Defines common behaviours (methods) shared between 
 * all tag handlers.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class TagHandler implements ITagHandler 
{
	/**
	 * Returns true if the given field is a control field.
	 * 
	 * @param field the field definition.
	 * @return true if the given field is a control field.
	 */
	protected boolean isControlField(final String field)
    {
        return field.startsWith("00");
    }		

	/**
	 * Extracts, from given record,  the value of the requested tag mapping.
	 * A tag mapping is a directive that instructs this handler about how 
	 * to extract a value from a marc record.
	 * 
	 * @param tagMappings the OMML expression.
	 * @param record the MARC record.
	 * @return a value according with the logic implemented by the concrete implementor. 
	 */
	protected List<String> getValues(final String tagMappings, final Record record)
	{
		String [] tags = tagMappings.split(":");
		for (String tag : tags)
		{
			if (tag.length() > 3)
			{
				List<String> result = new ArrayList<String>();
				List<VariableField> fields = record.getVariableFields(tag.substring(0, 3));
				
				String subFieldNamesString = tag.substring(3);
				char [] subfieldNames = new char[subFieldNamesString.length()];
				subFieldNamesString.getChars(0, subFieldNamesString.length(), subfieldNames, 0);
				if (fields != null)
				{
					for (VariableField f : fields)
					{
						DataField field = (DataField)f;
						StringBuilder buffer = new StringBuilder();
						for (char subfieldName : subfieldNames)
						{
							Subfield subfield = field.getSubfield(subfieldName);
							if (subfield != null)
							{
								if (buffer.length() != 0)
								{
									buffer.append(' ');
								}
								
								String data = subfield.getData();
								buffer.append(data);
							}
						}
						result.add(buffer.toString());
					}
				}		
				return result;
			}
		}
		return null;
	}				
	
	/**
	 * Handles the result of a given tag handler.
	 * Basically the following rules apply:
	 * 
	 * <br/>1) if the collection is null or empty, returns null;
	 * <br/>2) if the collection contains just one element, returns that element;
	 * <br/>3) otherwise returns the whole collection.
	 * 
	 * @param values the collection of results.
	 * @return the result of this tag handler.
	 */
	protected Object handleReturnValues(final Collection<String> values)
	{
		if (values == null || values.isEmpty())
		{
			return null;
		}
		
		values.remove(IConstants.EMPTY_STRING);
		if (values.size() == 1)
		{
			return values.iterator().next();
		}		
		
		return values;
	}
}
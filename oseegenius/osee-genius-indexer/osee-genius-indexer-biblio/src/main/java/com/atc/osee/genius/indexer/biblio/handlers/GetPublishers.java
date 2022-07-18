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

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * A tag handler that breaks up and extract the 260 tag according with this rule:
 * 
 * <pre>
 * Se dopo il primo $b del tag 260 trovi il sottocampo $a, vuol dire che sei di fronte ad un'altra formulazione di editore (altra concatezione di $a e $b).
 * Quindi la casistica delle intestazioni è questa:
 * 
 * 1) $a :$b
 * 2) $a ;$a :$b
 * 3) $a :$b :$b
 * 
 * queste 3 intestazioni possono essere concatenate in un'unico tag 260: ogni volta che dopo un $b trovi un $a, allora vuol dire che devi "spezzare" 
 * la riga perchè ti trovi di fronte ad un'altra formulazione di editore (che può essere quella del caso 1, 2 o 3). Non so se sono stata chiara...
 * Quindi la riga:
 * 
 * 260 $aFREIBURG :$bACADEMIC PRESS FRIBURG ;$aFREIBURG ;$aWIEN :$bHERDER
 * 
 * è costituita dalla concatenazione del caso 1 e 2. Se al primo $b segue un $a, allora comincia un'altra intestazione e avrò:
 * 
 * $aFREIBURG :$bACADEMIC PRESS FRIBURG
 * $aFREIBURG ;$aWIEN :$bHERDER
 * 
 * </pre>
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetPublishers extends TagHandler
{	
	private final static char NEW_HEADING_CRITERIA = 'a';
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
		String [] expressionAndFilter = tagMappings.split(",");
		Set<String> result = new LinkedHashSet<String>();
		String [] tags = expressionAndFilter[0].split(":");
		for (String tag : tags)
		{
			if (tag.length() > 3)
			{
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
						List<Subfield> subfields = field.getSubfields();
						
						char previousCode = '?';
						
						for (Subfield subfield : subfields)
						{
							if (subfield != null)
							{
								if (hasBeenRequested(subfield, subfieldNames))
								{
									if (previousCode != NEW_HEADING_CRITERIA && subfield.getCode() == NEW_HEADING_CRITERIA)
									{
										String value = buffer.toString().trim();
										if (value.length() != 0)
										{
											result.add(value);
										}																				
										buffer.setLength(0);
									}

									previousCode = subfield.getCode();

									String data = subfield.getData();
									data = (data != null && data.trim().length() != 0) ? data.trim() : null;
									if (data != null)
									{
										if (buffer.length() != 0) 
										{
											buffer.append(' ');	
										}
										buffer.append(data);
									}
								}
							}
						}
						String value = buffer.toString().trim();
						if (value.length() != 0)
						{
							result.add(value);
						}
					}
				}		
			}
		}
		
        return handleReturnValues(result);
	}

	/**
	 * Returns true of the given subfield has been requested.
	 * That is, if the given subfield should be part of the result.
	 * 
	 * @param subfield the subfield.
	 * @param subfieldNames the list of requested subfields.
	 * @return true if the given subfield should be included in result.s
	 */
	private boolean hasBeenRequested(final Subfield subfield, final char[] subfieldNames) 
	{
		for (char code : subfieldNames)
		{
			if (code == subfield.getCode())
			{
				return true;
			}
		}
		return false;
	}
}
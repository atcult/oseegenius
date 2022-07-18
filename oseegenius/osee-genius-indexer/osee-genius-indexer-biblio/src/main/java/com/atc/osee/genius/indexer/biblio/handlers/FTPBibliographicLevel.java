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
 * Extracts the FTP bibliographic level from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPBibliographicLevel extends TagHandler implements IConstants
{
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		final String leader = record.getLeader().toString();
    	if (leader == null || leader.trim().length() < 8) {
    		return null;
    	}
    	
		char leaderChar07 = leader.charAt(7);
    	
		if (!isPaper(record, "942"))
		{
	    	switch(leaderChar07)
	    	{
	    		case 'm':
	    		{
	    			if (_097$bContains(record,"014"))
	    			{
	    				return "m";
	    			}
	    			break;
	    		}
	    		case 'a':
	    		{
	    			if (_097$bContains(record,"015"))
	    			{
	    				return "pdm";
    				}
	    			break;
	    		}
	    		case 's':
	    		{
	    			if (_097$bContains(record,"011"))
	    			{
	    				return "tr";
    				}	    			
	    			break;
	    		}
	    		case 'b':
	    		{
	    			if (_097$bContains(record,"012"))
	    			{
	    				return "fr";
    				} else if (_097$bContains(record,"013"))
	    			{
    					return "ar";
    				}
	    			break;
	    		}
	    	}
		} else 
		{
	    	switch(leaderChar07)
	    	{
	    		case 'a':
	    			return "pdm";
	    		case 'b':
	    			return "ar"; 
	    		case 'm':
	    			return "m";
	    		case 's':
	    			return "tr";	   
	    		default:
	    			return null;
	    	}			
		}
		return null;
	}
	
	boolean isPaper(final Record record, final String fieldSpecification)
	{
		List<VariableField> fields = (List<VariableField>) record.getVariableFields(fieldSpecification);
		
		if (fields == null || fields.isEmpty())
		{
    		return true;
		} 
		
		for (final VariableField f : fields)
		{
			DataField field = (DataField) f;
			return (field == null);
		}
		
		return false;
	}	
	
	protected boolean _097$bContains(final Record record, final String expectedValue)
	{
		DataField _097 = (DataField) record.getVariableField("097");
		if (_097 != null )
		{
			Subfield $b = _097.getSubfield('b');
			return ($b != null && expectedValue.equals($b.getData()));
		}
		return false;
	}	
}
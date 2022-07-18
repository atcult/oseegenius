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

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.Format;
import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the formats of a given marc records.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetFormats extends TagHandler implements IConstants
{
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
    	char leaderChar06 = record.getLeader().toString().charAt(6);
    	VariableField field008 = record.getVariableField(_008);
    	VariableField field007 = record.getVariableField(_007);
    	
		if (field008 != null && field008.find("^.{23}[abc]")) 
		{
			return Format.MICROFILM;
		}     	
		
		String field007MusicPattern = "^.{6}[g]";
		
    	switch (leaderChar06)
    	{
    		case A:
				if (field008 != null && field008.find("^.{23}[#dfr]")) 
				{
					return Format.MATERIALE_A_STAMPA;
				} else if (field008 != null && field008.find("^.{23}[s]")) 
				{
					return Format.RISORSA_ELETTRONICA;
				} 
				return Format.MATERIALE_A_STAMPA;
    		case C:
    		case D:
    			return Format.PARTITURE_MUSICALI;
    		case E:
    			return Format.MATERIALE_CARTOGRAFICO_STAMPA;
    		case F:
    			return Format.MATERIALE_CARTOGRAFICO_MANOSCRITTO;
    		case G:
    			if (field007 != null && field007.find("^.{4}[b]")) 
    			{
    				return Format.VIDEOCASSETTA;
    			} else if (field007 != null && field007.find("^.{4}[v]")) 
    			{
    				return Format.VIDEO_DVD;
    			} else 
    			{
    				return Format.VIDEOREGISTRAZIONE;
    			}       		
        	case J:
        		if (field007 != null && field007.find(field007MusicPattern)) 
    			{
    				return Format.CD_MUSICALE;
    			} 
    			
    			if (field007 != null && field007.find("^.{3}[b]")) 
    			{
    				return Format.LP_MUSICALE;
    			} 
    			return Format.MUSICA;
        	case I:
    			if (field007 != null && field007.find("^.{1}[s]")) 
    			{
    				return Format.AUDIO_LIBRO_CON_CASSETTA;
    			} 

    			if (field007 != null && field007.find(field007MusicPattern)) 
    			{
    				return Format.AUDIO_LIBRO_CON_CD;
    			} 
    			
    			return Format.REGISTRAZIONE_SONORA;
    		case M:
    			if (field008 != null && field008.find("^.{23}[a]")) 
    			{
    				return Format.RISORSA_ELETTRONICA;
    			} else 
    			{
    				return Format.SOFTWARE;
    			}
    		case P:
    			return Format.MATERIALE_MISTO; 	    			
    		case T:
    			return Format.MANOSCRITTO;
    		default:
    			return null;
    	}
	}	
}
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

import com.atc.osee.genius.indexer.biblio.BibliographicLevel;
import com.atc.osee.genius.indexer.biblio.Format;
import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the formats of a given marc records.
 * 
 * @author mbraddi
 * @since 1.2
 */
public class ImssGetFormats extends TagHandler implements IConstants
{
	private static final String OBJECT = "obj";
	private static final String ARTICLE_ESSAY = "essay";
	private static final String VIDEO = "slv";
	private static final String CARTOGRAPHIC_MAT = "mcart";
	private static final String AUDIO = "srec";
	private static final String COMPUTER_FILE = "elr";
	private static final String MANUSCRIPT_TYPESCRIPT = "mdat";
	private static final String GRAPHIC_MAT = "mgraph";
	private static final String NO_ICON = "noIcon";
	
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		final String leader = record.getLeader().toString();
    	final char leaderChar06 = leader.charAt(6);
    	final char leaderChar07 = leader.charAt(7);
		
    	switch (leaderChar06)
    	{
    		case A:
    			switch (leaderChar07)
    			{
    				case M:
    					return BibliographicLevel.MONOGRAPH;
    				case S:
    					return BibliographicLevel.SERIAL;
    				case A:
    					return ARTICLE_ESSAY;
    				case B:
    					return ARTICLE_ESSAY;
    				case P: 	
    					return ARTICLE_ESSAY;
    				default:
    					return NO_ICON;
    			}		
    		case G:
    				return VIDEO;      		
        	case E:
        		  return CARTOGRAPHIC_MAT; 
        	case F:
        		return CARTOGRAPHIC_MAT; 
        	case J:
        		return AUDIO;
        	case M:
        		 return COMPUTER_FILE;
        	case T:
        		return MANUSCRIPT_TYPESCRIPT;
        	case K:
        		return GRAPHIC_MAT;
        	case R:
        		return OBJECT;
    		default:
    			return NO_ICON;
    	}
	}	
}
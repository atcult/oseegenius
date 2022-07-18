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

import com.atc.osee.genius.indexer.biblio.BibliographicLevel;
import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the formats of a given marc records.
 * Although could be reusable, this algorithm has been implemented for CBT.
 * 
 * @author agazzarini
 * @since 1.0
 */
public final class CbtFormats extends TagHandler implements IConstants
{
	private static final String MATERIALE_LINGUISTICO = "mling";
	private static final String MUSICA_STAMPATA_O_MICROFORMA = "msom";
	private static final String MATERIALE_CARTOGRAFICO_STAMPATO_O_IN_MICROFORMA = "mcsoim";
	@SuppressWarnings("unused")
	private static final String MATERIALE_CARTOGRAFICO_MANOSCRITTO_INCLUSA_MICROFORMA = "mcmim";
	private static final String MATERIALE_DA_PROIETTARE = "mdpro";
	private static final String DVD = "dvd";
	private static final String VHS = "vhs";
	private static final String REGISTRAZIONE_SONORA_MUSICALE = "rsmusic";
	private static final String REGISTRAZIONE_SONORA_NON_MUSICALE = "rsnomusic";
	private static final String RAPPRESENTAZIONE_GRAFICA_BIDIMENSIONALE = "rgrafbi";
	private static final String SISTEMA_O_SERVIZIO_ONLINE = "soso";
	private static final String FILE_COMPUTER = "file";
	private static final String KIT = "kit";
	private static final String MATERIALE_LINGUISTICO_MANOSCRITTO = "mlingman";
	private static final String MANUFATTO_TRIDIMENSIONALE = "manutri";
	private static final String DOCUMENTO_EBOOK = "docebook";
	private static final String CD_DVDROM = "cddvdrom";
	private static final String BLURAY = "bluray";
	
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
    	
    	VariableField field008 = record.getVariableField(_008);
		
    	switch (leaderChar06)
    	{
    		case A:
    			switch (leaderChar07)
    			{
    				case M:
    					if (field008 != null)
    	    			{
    		    			if (field008.find("^.{23}[s]")) 
    		    			{
    		   					return DOCUMENTO_EBOOK;	    					
    		   				}
    	    			}
    					return BibliographicLevel.MONOGRAPH;
    				case S:
    					return BibliographicLevel.SERIAL;
    				case A:
    				case B: 
    					return BibliographicLevel.MONOGRAPHIC_COMPONENT_PART;
    				default:
    					return MATERIALE_LINGUISTICO;    					
    			}
    		case C:
    		case D:	
    			return MUSICA_STAMPATA_O_MICROFORMA;
    		case E:
    		case F:
    			return MATERIALE_CARTOGRAFICO_STAMPATO_O_IN_MICROFORMA;
    		case G:
    			if (contains(record, "DVD") )
    			{ 
    				return DVD;
    			} else if (contains(record, "VHS"))
    			{
    				return VHS;
    			} else if (contains(record, "Blu-ray disc"))
    			{
    				return BLURAY;
    			}
    			return MATERIALE_DA_PROIETTARE;
        	case J:
    			return REGISTRAZIONE_SONORA_MUSICALE;
        	case I:
    			return REGISTRAZIONE_SONORA_NON_MUSICALE;
        	case K:
    			return RAPPRESENTAZIONE_GRAFICA_BIDIMENSIONALE;
    		case M:
    			if (contains(record, "CDROM","CD-ROM", "CD ROM"))
    			{
    				return CD_DVDROM;
    			}
    			
    			if (contains(record, "DVD-ROM"))
    			{
    				return CD_DVDROM;
    			}
    			
    			if (field008 != null)
    			{
	    			if (field008.find("^.{26}[j]")) 
	    			{
	    				return SISTEMA_O_SERVIZIO_ONLINE;
	    			} else if (field008.find("^.{26}[e]"))
	    			{
	   					return DOCUMENTO_EBOOK;	    					
	   				}
    			}
    			return FILE_COMPUTER;
    		case O:
    			return KIT; 	    			    			
    		case P:
    		case R:
    			return MANUFATTO_TRIDIMENSIONALE;
    		case T:
    			return MATERIALE_LINGUISTICO_MANOSCRITTO;
    		default:
    			return null;
    	}
	}	
	
	@SuppressWarnings("unchecked")
	private boolean contains(final Record record, final String...criterias)
	{
		final List<VariableField> fields = (List<VariableField>) record.getVariableFields("300");
		if (fields != null && !fields.isEmpty())
		{
			for (VariableField f : fields) {						
				DataField field = (DataField)f;
				final List<Subfield> extents = field.getSubfields(A);
				if (extents != null && !extents.isEmpty())
				{
					for (Subfield extent : extents)
					{
						 for(String criteria: criterias){ 
							final String data = extent.getData();
							if (data != null && ( (data.indexOf(criteria) != -1) ||  (data.indexOf(criteria.toLowerCase()) != -1)) )
							{
								return true;
							}
						 }	
					}
				}
			}
		}
		return false;
	}
}
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.MarcDirStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.LeaderImpl;

import com.atc.osee.genius.indexer.biblio.BibliographicLevel;
import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the bibliographic level from a MARC record.
 * According with 
 * 
 *        
 * @author mbraddi
 * @since 1.2
 */
public class ImssGetBibliographicLevel extends TagHandler implements IConstants
{
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		try 
		{
			VariableField field008 = record.getVariableField(_008);
			if (field008 != null && field008.find("^.{24}[o]")) {
				return String.valueOf('o');
			}
			else {
				//if (007 == 'p' && 008[24-27] != 'o')	=>	journal article (it's the same case of 007 ==  'b')
				if("p".equals(Character.toString(record.getLeader().toString().charAt(7)))){
					return "b";
				}
					
			}
			
			return String.valueOf(record.getLeader().toString().charAt(7));
		} catch (Exception exception)
		{
			return BibliographicLevel.OTHER;
		}
	}
}
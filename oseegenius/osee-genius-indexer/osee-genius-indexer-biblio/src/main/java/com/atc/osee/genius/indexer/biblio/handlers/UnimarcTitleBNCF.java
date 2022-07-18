package com.atc.osee.genius.indexer.biblio.handlers;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * A tag handler that extracts 200 from all subfields 
 * 
 * @author ztajoli
 * @since 1.2
 */
public class UnimarcTitleBNCF extends TagHandler {	
	@SuppressWarnings("unchecked")
	public Object getValue(
			final String mappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
				
		String result = null;
		int len_title = 6;
				
		StringBuilder builder = null;
		final DataField f_200 = (DataField) record.getVariableField("200");
		
		if ( f_200  != null) {
			for (Subfield subfield : (List<Subfield>) f_200.getSubfields()){
				if (builder == null) { 	builder = new StringBuilder(); }
				if (builder.length() > 0) { 
					if (subfield.getCode()=='a')
	                	builder.append(" ;");
					builder.append(' '); 
					} 
                builder.append(subfield.getData().trim());
                
			}
			if (builder != null && builder.length() > 0) {
            	result = builder.toString();
            }
			
		}
		
		try {
			if (result.contains("<<")) { len_title = len_title + 5;}
			Subfield s_200a = ((DataField) record.getVariableField("200")).getSubfield('a');
			String sub_200a = s_200a.getData();
			String levelHier = String.valueOf(record.getLeader().toString().charAt(8));
			String ldrType = String.valueOf(record.getLeader().toString().charAt(7));
			Subfield f_461_t = ((DataField) record.getVariableField("461")).getSubfield('t');
			String sub_461t = f_461_t.getData();
			
			if ( (levelHier.equals("2")) && (ldrType.equals("m")) && (sub_461t != null) && (sub_461t.length()>0) 
					                  && (result != null) && (result.length() > 0)
					                  && (sub_200a != null) && (sub_200a.length() < len_title)   ) {
				
				result = "[" + sub_461t + "]. " + result;
			}
			return result;
		} catch(Exception e) {
			return result;
		}
	}	
}

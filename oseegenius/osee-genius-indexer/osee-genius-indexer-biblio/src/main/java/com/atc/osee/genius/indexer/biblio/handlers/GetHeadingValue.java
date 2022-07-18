package com.atc.osee.genius.indexer.biblio.handlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
* 
* @author a guercio
*
*/


public class GetHeadingValue extends TagHandler {

	final static String SEPARATOR = ":_:"; 

	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document) {		
		
		List<String> result = new ArrayList<String>();	
		
		final List<VariableField> fields = record.getVariableFields("606");
				
		for(VariableField f : fields) {
			DataField field = (DataField) f;
			StringBuilder data = new StringBuilder();
			
			Subfield subfield = field.getSubfield('a');
			if (subfield != null) {
				data.append(subfield.getData());
			}	
			
			List<Subfield> subfields_x = field.getSubfields('x');
			for(Subfield subfield_x : subfields_x) {
				if (data.length() > 0) {
					data.append(" - ");
				}
				data.append(subfield_x.getData());			
			}
			Subfield subfield_3 = field.getSubfield('3');
			if (subfield_3 != null) {
				data.append(SEPARATOR).append(subfield_3.getData());
			}
			result.add(data.toString());
		}
		return handleReturnValues(result);	
	
	}
}
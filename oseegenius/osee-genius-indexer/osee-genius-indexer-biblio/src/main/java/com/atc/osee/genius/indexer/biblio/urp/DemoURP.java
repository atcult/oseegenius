package com.atc.osee.genius.indexer.biblio.urp;

import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

public class DemoURP extends UpdateRequestProcessor {

	public DemoURP(UpdateRequestProcessor next) {
		super(next);
	}

	@Override
	public void processAdd(AddUpdateCommand cmd) throws IOException {
		SolrInputDocument doc = cmd.getSolrInputDocument();
		Object originalId = doc.getFieldValue("original_id");
		if (originalId == null) {
			doc.setField("original_id", doc.get("id").getFirstValue());			
		}
			
		doc.setField("catalog_source", "NRA");			
		super.processAdd(cmd);
	}
}

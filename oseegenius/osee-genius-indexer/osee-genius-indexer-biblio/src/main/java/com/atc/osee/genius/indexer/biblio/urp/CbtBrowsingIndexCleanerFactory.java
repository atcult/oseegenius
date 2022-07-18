package com.atc.osee.genius.indexer.biblio.urp;

import java.util.Set;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

import com.atc.osee.genius.indexer.biblio.browsing.IAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.DummyAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus.AmicusNameAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus.AmicusPublisherAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus.AmicusSubjectAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus.AmicusTitleAuthorityAccessObject;

@SuppressWarnings("rawtypes")
public class CbtBrowsingIndexCleanerFactory extends UpdateRequestProcessorFactory {
	private NamedList params;
	
	@Override
	public void init(final NamedList args) 
	{
		this.params = args;
	}
	
	@Override
	public UpdateRequestProcessor getInstance(
			final SolrQueryRequest req, 
			final SolrQueryResponse rsp, 
			final UpdateRequestProcessor next) {
		IAuthorityAccessObject aao = null;
		
		final String name = req.getCore().getName();
		if ("title_browse".equals(name)) {
			aao = new AmicusTitleAuthorityAccessObject();
		} else if ("author_browse".equals(name)) {
			aao = new AmicusNameAuthorityAccessObject();
		} else if ("subject_browse".equals(name)) {
			aao = new AmicusSubjectAuthorityAccessObject();
		} else if ("publisher_browse".equals(name)) {
			aao = new AmicusPublisherAuthorityAccessObject();
		} else if ("publication_place_browse".equals(name)) {
			aao = new DummyAuthorityAccessObject();
		} 
		
		return new CbtBrowsingIndexCleaner(params, req, req.getCore(), next, aao);
	}
}

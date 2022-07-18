/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Affero General Public License version 3 as published by the Free 
 * Software Foundation with the addition of the following permission added to Section 
 * 15 as permitted in Section 7(a). 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. 
 * You should have received a copy of the GNU Affero General Public License along with this program;
 */
package com.atc.osee.z3950.search;

import java.util.Iterator;
import java.util.Map;

import org.jzkit.ServiceDirectory.CollectionDescriptionDBO;
import org.jzkit.configuration.api.Configuration;
import org.jzkit.configuration.api.ConfigurationException;
import org.jzkit.configuration.api.RecordMappingInformationDBO;
import org.jzkit.search.ExplainInformationDTO;
import org.jzkit.search.LandscapeSpecification;
import org.jzkit.search.SearchSession;
import org.jzkit.search.SearchSessionFactory;
import org.jzkit.search.StatelessSearchResultsPageDTO;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.util.QueryModel.InvalidQueryException;
import org.jzkit.search.util.QueryModel.QueryModel;
import org.jzkit.search.util.RecordModel.ExplicitRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.RecordFormatSpecification;
import org.jzkit.search.util.ResultSet.IRResultSetException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class OseeGeniusSearchSessionFactory implements SearchSessionFactory, ApplicationContextAware
{
	 private ApplicationContext ctx;
	  private OseeGeniusStatelessQueryService stateless_query_service = null;

	  public SearchSession getSearchSession()
	  {
	    return (SearchSession)this.ctx.getBean("SearchSession");
	  }

	  public StatelessSearchResultsPageDTO getResultsPageFor(String result_set_id, QueryModel model, LandscapeSpecification landscape, int first_hit, int num_hits, RecordFormatSpecification request_spec, ExplicitRecordFormatSpecification display_spec, Map additional_properties)
	    throws SearchException, IRResultSetException, InvalidQueryException
	  {
	    return this.stateless_query_service.getResultsPageFor(result_set_id, model, landscape, first_hit, num_hits, request_spec, display_spec, additional_properties);
	  }

	  public StatelessSearchResultsPageDTO getResultsPageFor(String result_set_id, String model_type, String model_str, String landscape_str, int first_hit, int num_hits, RecordFormatSpecification request_spec, ExplicitRecordFormatSpecification display_spec, Map additional_properties)
	    throws SearchException, IRResultSetException, InvalidQueryException
	  {
	    return this.stateless_query_service.getResultsPageFor(result_set_id, model_type, model_str, landscape_str, first_hit, num_hits, request_spec, display_spec, additional_properties);
	  }

	  public void setApplicationContext(ApplicationContext ctx)
	  {
	    this.ctx = ctx;
	  }

	  public void init() {
	    this.stateless_query_service = ((OseeGeniusStatelessQueryService)this.ctx.getBean("StatelessQueryService"));
	  }

	  @SuppressWarnings({ "rawtypes", "unchecked" })
	public ExplainInformationDTO explain()
	  {
	    ExplainInformationDTO result = new ExplainInformationDTO();

	    Configuration directory = (Configuration)this.ctx.getBean("JZKitConfig");
	    try
	    {
	      for (Iterator i = directory.enumerateVisibleCollections(); i.hasNext(); ) {
	        CollectionDescriptionDBO ci = (CollectionDescriptionDBO)i.next();
	        RecordMappingInformationDBO rec_info = new RecordMappingInformationDBO();
	        result.getDatabaseInfo().add(rec_info);
	      }
	    }
	    catch (ConfigurationException ce)
	    {
	      ce.printStackTrace();
	    }
	    finally
	    {
	    }

	    return result;
	  }
}

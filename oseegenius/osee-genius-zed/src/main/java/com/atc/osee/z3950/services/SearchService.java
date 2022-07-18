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
package com.atc.osee.z3950.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jzkit.search.SortModel;
import org.jzkit.search.provider.iface.IRQuery;
import org.jzkit.search.provider.iface.Searchable;
import org.jzkit.search.util.QueryFormatter.QueryFormatter;
import org.jzkit.search.util.ResultSet.IRResultSet;
import org.jzkit.search.util.ResultSet.IRResultSetStatus;

import com.atc.osee.z3950.OseeGeniusResultSet;
import com.atc.osee.z3950.backend.BackendSortDTO;

/**
 * SOLR search service client.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class SearchService extends SolrService implements Searchable
{
	private static final Log LOGGER = LogFactory.getLog(SearchService.class);

	private String queryBuilderName;
	private Map<String, String> fieldLists = new HashMap<String, String>();

	/**
	 * Sets the query builder name.
	 * 
	 * @param queryFormatterName the query builder name.
	 */
	public void setQueryBuilderName(final String queryBuilderName) 
	{
		this.queryBuilderName = queryBuilderName;
	}

	/**
	 * Returns the query builder name.
	 * 
	 * @param queryBuilderName the query builder name.
	 */
	public String getQueryBuilderName() 
	{
		return queryBuilderName;
	}

	@Override
	public IRResultSet evaluate(IRQuery query) 
	{
		return evaluate(query, null);
	}

	@Override
	public IRResultSet evaluate(IRQuery query, Object userInfo) 
	{
		return evaluate(query, userInfo, null);
	}

	@Override
	public IRResultSet evaluate(IRQuery q, Object user_info, Observer[] observers) 
	{
		OseeGeniusResultSet result = null;
		try 
		{
			LOGGER.debug("Casting internal query tree using " + queryBuilderName);
			
			QueryFormatter formatter = (QueryFormatter) applicationContext.getBean(queryBuilderName);
			String query = formatter.format(q.getQueryModel().toInternalQueryModel(applicationContext));
			
			String sortString = null;
			if (user_info != null && user_info instanceof SortModel)
			{
				BackendSortDTO sortModel = (BackendSortDTO) user_info;
				if (!sortModel.getAscendingAttributes().isEmpty() || !sortModel.getDescendingAttributes().isEmpty())
				{
					StringBuilder builder = new StringBuilder();
					for (String attribute :sortModel.desc)
					{
						builder.append(attribute).append(" desc,");
					}
					
					for (String attribute :sortModel.asc)
					{
						builder.append(attribute).append(" asc,");
					}					
					
					builder.deleteCharAt(builder.length()-1);
					sortString = builder.toString();
				}
			}
			
			result = new OseeGeniusResultSet(observers, serviceUrl, query, code, fieldLists, q, sortString);
			result.countHits();
			result.setStatus(IRResultSetStatus.COMPLETE);
		} catch (Exception exception) 
		{
			if (result == null) 
			{
				result = new OseeGeniusResultSet(observers, serviceUrl, "", code, fieldLists, q, null);
			}
			LOGGER.error(
					"Evaluate failed... Setting search status to fail and logging exception as message",
					exception);

			result.setStatus(IRResultSetStatus.FAILURE);
			result.postMessage(exception.getMessage());
		}
		return result;
	}

	public void setFieldList(Map<String, String> field_lists) 
	{
		this.fieldLists = field_lists;
	}

	public Map<String, String> getFieldList() 
	{
		return fieldLists;
	}
}
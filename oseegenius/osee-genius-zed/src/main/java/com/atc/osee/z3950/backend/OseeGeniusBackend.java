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
package com.atc.osee.z3950.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jzkit.ServiceDirectory.SearchServiceDescriptionDBO;
import org.jzkit.configuration.api.Configuration;
import org.jzkit.search.LandscapeSpecification;
import org.jzkit.search.StatelessSearchResultsPageDTO;
import org.jzkit.search.landscape.SimpleLandscapeSpecification;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.util.QueryModel.QueryModel;
import org.jzkit.search.util.RecordModel.ArchetypeRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.ExplicitRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.RecordFormatSpecification;
import org.jzkit.search.util.ResultSet.IRResultSetInfo;
import org.jzkit.service.z3950server.ZSetInfo;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.RPNQuery_type;
import org.jzkit.z3950.server.BackendDeleteDTO;
import org.jzkit.z3950.server.BackendPresentDTO;
import org.jzkit.z3950.server.BackendSearchDTO;
import org.jzkit.z3950.server.BackendStatusReportDTO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.atc.osee.z3950.IConstants;
import com.atc.osee.z3950.search.OseeGeniusStatelessQueryService;

/**
 * OseeGenius backend implementation.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class OseeGeniusBackend implements OseeGeniusNonBlockingBackend, ApplicationContextAware 
{
	public static final Log LOGGER = LogFactory.getLog(OseeGeniusBackend.class);
	private static final String DEFAULT_RESULTSET_NAME = "default";
	private static final RecordFormatSpecification DEFAULT_REQUEST_SPECS = new ArchetypeRecordFormatSpecification("F");

	private ApplicationContext applicationContext;
	
	// Workaround for client that sends a resultset name in the search and another in the present
	// OCLC apply something like this. Basically other than storing all valid resultsets we store a dedicated 
	// reference to the latest valid resultset. If a present request comes with an invalid (i.e. not found) result set
	// name, then results will be fetched from this.
	private ZSetInfo latestResultSet;
	
	private final Map<String, ZSetInfo> resultSets = new HashMap<String, ZSetInfo>();
	private String implementationName;
	private String implementationVersion;
	
	/**
	 * Returns the implementation version.
	 * 
	 * @return the implementation version.
	 */
	public String getVersion() 
	{
		return implementationVersion;
	}

	/**
	 * Returns the implementation name.
	 * 
	 * @return the implementation name.
	 */
	public String getImplementationName() 
	{
		return implementationName;
	}

	/**
	 * Sets the implementation name.
	 * 
	 * @param implementationName the implementation name.
	 */
	public void setImplementationName(final String implementationName) 
	{
		this.implementationName = implementationName;
	}

	/**
	 * Returns the implementation version.
	 * 
	 * @return the implementation version.
	 */
	public String getImplementationVersion() 
	{
		return implementationVersion;
	}
	
	/**
	 * Sets the implementation version.
	 * 
	 * @param implementationVersion the implementation version.
	 */
	public void setImplementationVersion(final String implementationVersion) 
	{
		this.implementationVersion = implementationVersion;
	}

	@Override
	public String getImplName() 
	{
		return implementationName;
	}

	@Override
	public void search(final BackendSearchDTO searchData) 
	{
		try 
		{
			final Configuration configuration = (Configuration) applicationContext.getBean("JZKitConfig");
			final SearchServiceDescriptionDBO searchService = configuration.lookupSearchService("osee-genius-searcher");

			final QueryModel qm = new OseeGeniusType1QueryModel((RPNQuery_type) (searchData.query.o), searchService.getPreferences());
			
			final OseeGeniusStatelessQueryService sqs = (OseeGeniusStatelessQueryService) applicationContext.getBean("StatelessQueryService");
			String query_id = null;
			final LandscapeSpecification landscape = new SimpleLandscapeSpecification(searchData.database_names);
			ExplicitRecordFormatSpecification exp = null;

			final StatelessSearchResultsPageDTO res = sqs.getResultsPageFor(query_id, qm, landscape, 0, 0, DEFAULT_REQUEST_SPECS, exp, null);

			LOGGER.debug("Search status = " + res.getSearchStatus());

			if (res.getSearchStatus() == 8) 
			{
				// Search failure
				searchData.search_status = false;
				searchData.result_count = 0;
			} else 
			{
				searchData.search_status = true;
				searchData.result_count = res.getRecordCount();
			}

			searchData.result_set_status = 1;
			searchData.status_report = createStatusReport(res.result_set_info);

			LOGGER.debug("SEARCH: result set name: " + searchData.result_set_name);

			latestResultSet = new ZSetInfo(query_id, qm, landscape);
			final String resultSetName = 
			((searchData.result_set_name != null) && (searchData.result_set_name.length() > 0)) 
				? searchData.result_set_name
				: DEFAULT_RESULTSET_NAME;		
			
			resultSets.put(resultSetName, latestResultSet);

			LOGGER.debug("ResultSets stack #" + this.hashCode()+ " size is " + resultSets.size());
		} catch (SearchException exception) 
		{
			LOGGER.error("Exception while executing SEARCH request.", exception);
			searchData.search_status = false;
			searchData.diagnostic_code = JZKitToDiag1(exception.error_code);
			searchData.result_set_status = 3;
			if (searchData.diagnostic_code > 0) 
			{
				if (exception.getMessage() != null) 
				{
					searchData.diagnostic_addinfo = exception.getMessage();
				}
				searchData.diagnostic_data = exception.diagnostic_data;
			}
		} catch (org.jzkit.search.util.ResultSet.IRResultSetException exception) 
		{
			LOGGER.error("Exception while executing SEARCH request.", exception);
			searchData.search_status = false;
		} catch (org.jzkit.search.util.QueryModel.InvalidQueryException exception) 
		{
			LOGGER.error("Exception while executing SEARCH request.", exception);
			searchData.search_status = false;
		} catch (Exception exception) 
		{
			LOGGER.error("Exception while executing SEARCH request.", exception);
			searchData.search_status = false;
		}

		searchData.assoc.notifySearchResult(searchData);
	}

	@SuppressWarnings("rawtypes")
	private BackendStatusReportDTO createStatusReport(final IRResultSetInfo rsi) 
	{
		final BackendStatusReportDTO result = new BackendStatusReportDTO();
		result.source_id = rsi.name;
		result.result_set_status = rsi.status;
		result.result_count = rsi.total_fragment_count;
		result.status_string = rsi.last_message;
		if (rsi.record_sources != null) 
		{
			result.child_reports = new ArrayList<BackendStatusReportDTO>();
			for (Iterator i = rsi.record_sources.iterator(); i.hasNext();) 
			{
				result.child_reports.add(createStatusReport((IRResultSetInfo) i.next()));
			}
		}
		return result;
	}

	@Override
	public void sort(final BackendSortDTO sortData) 
	{
		final String resultSetName = sortData.getResultSetName(); 
		final ZSetInfo resultSetMetadata = getResultSet(resultSetName);
		if (resultSetMetadata != null) 
		{
			try 
			{
				final OseeGeniusStatelessQueryService sqs = (OseeGeniusStatelessQueryService) applicationContext.getBean(IConstants.STATELESS_QUERY_SERVICE_NAME);
				final LandscapeSpecification landscape = resultSetMetadata.getLandscape();
				String query_id = null;
				ExplicitRecordFormatSpecification exp = null;

				final Map<String, Object> additionalProperties = new HashMap<String, Object>();
				additionalProperties.put("sort", sortData);
				
				final StatelessSearchResultsPageDTO res = sqs.getResultsPageFor(
						query_id, 
						resultSetMetadata.getQueryModel(), 
						landscape, 0, 0,
						DEFAULT_REQUEST_SPECS, 
						exp, 
						additionalProperties);

				LOGGER.debug("SORT (SEARCH) status = " + res.getSearchStatus());

				if (res.getSearchStatus() == 8) 
				{
					// Search failure
					// bsr.search_status = false;
					// bsr.result_count = 0;
				} else {
					// bsr.search_status = true;
					// bsr.result_count = res.getRecordCount();
				}

				// bsr.result_set_status = 1;

				// bsr.status_report = createStatusReport(res.result_set_info);

				LOGGER.debug("SORT: result set name :" + resultSetName);
			} catch (SearchException exception) {
				LOGGER.error("Exception while executing SORT request.", exception);
			} catch (org.jzkit.search.util.ResultSet.IRResultSetException exception) {
				LOGGER.error("Exception while executing SORT request.", exception);
				// bsr.search_status = false;
			} catch (org.jzkit.search.util.QueryModel.InvalidQueryException exception) {
				LOGGER.error("Exception while executing SORT request.", exception);
				// bsr.search_status = false;
			} catch (Exception exception) {
				LOGGER.error("Exception while executing SORT request.", exception);
				// bsr.search_status = false;
			}

			// bsr.assoc.notifySearchResult(bsr);
		}
	}
	
	@Override
	public void present(final BackendPresentDTO presentData) 
	{
		try 
		{
			final String resultSetName = presentData.result_set_name;
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("PRESENT: result set name :" + resultSetName);
			}
			
			final ZSetInfo resultSet = getResultSet(resultSetName);

			if (resultSet != null) 
			{
				// Check that it's not out of range
				final OseeGeniusStatelessQueryService queryService = (OseeGeniusStatelessQueryService) applicationContext.getBean("StatelessQueryService");
				final StatelessSearchResultsPageDTO res = queryService.getResultsPageFor(
						resultSet.getSetname(), resultSet.getQueryModel(),
						resultSet.getLandscape(), presentData.start, presentData.count,
						DEFAULT_REQUEST_SPECS, presentData.explicit, null);
				presentData.total_hits = res.total_hit_count;

				presentData.result_records = res.getRecords();
				if (presentData.start > presentData.count) 
				{
					LOGGER.error(
							"PRESENT request out of range! Request start is " 
							+ presentData.start 
							+ " but RS count is " 
							+ presentData.count);
				} else
				{
					presentData.next_result_set_position = presentData.start + presentData.count;
				}
			} else 
			{
				LOGGER.warn(
						"PRESENT request indicates an invalid resultset name (" 
						+ resultSetName+ "). System tried to return the latest "
						+ "valid resultset but it seems there's no such result (perhaps this is your first request? " 
						+ "without any previous SEARCH?)");
			}
		} catch (Exception exception) 
		{
			LOGGER.error("Exception while executing PRESENT request.", exception);
		} 
		
		presentData.assoc.notifyPresentResult(presentData);
	}

	@Override
	public void deleteResultSet(final BackendDeleteDTO deleteData) 
	{
		final ZSetInfo removedResultSet = resultSets.remove(deleteData.result_set_name);
		if (removedResultSet != null && removedResultSet == latestResultSet)
		{
			latestResultSet = null;
		}
		
		deleteData.assoc.notifyDeleteResult(deleteData);
	}

	/**
	 * Injects the spring application context.
	 * 
	 * @param ctx the Spring application context.
	 */
	public void setApplicationContext(final ApplicationContext ctx) 
	{
		this.applicationContext = ctx;
	}

	private static int JZKitToDiag1(final int code) 
	{
		int result = 0;
		switch (code) 
		{
		case SearchException.INTERNAL_ERROR:
			result = 2;
			break; // Diag-1 : Temporary System Error
		case SearchException.UNKOWN_COLLECTION:
			result = 235;
			break; // Diag-1 : Specified Database Does Not Exist
		case SearchException.UNSUPPORTED_ACCESS_POINT:
			result = 114;
			break; // Diag-1 : Unsupported use attribute
		case SearchException.UNSUPPORTED_ATTR_NAMESPACE:
			result = 113;
			break; // Diag-1 : Unsupported attribute type
		default:
			break;
		}
		return result;
	}

	public static String getDiag1AddinfoString(long bib1_diag_code) 
	{
		return "{1}";
	}
	
	/**
	 * Returns the resultset (in the current session) associated with the given name.
	 * If the resultset cannot be found then the latest valid resultset is returned.
	 * 
	 * @param name the resultset name.
	 * @return the resultset (in the current session) associated with the given name.
	 */
	private ZSetInfo getResultSet(final String name)
	{
		final ZSetInfo result = (ZSetInfo) resultSets.get(name);
		if (result != null)
		{
			return result;
		}
		
		LOGGER.debug(
				"WARNING: The result set with name " 
				+ name 
				+ " cannot be found for this session. " 
				+ "Latest resultset (if not null) will be used.");
		
		return latestResultSet;
	}	
}

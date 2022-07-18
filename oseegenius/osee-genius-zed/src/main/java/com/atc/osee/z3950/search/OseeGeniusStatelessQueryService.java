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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jzkit.search.DeduplicationModel;
import org.jzkit.search.LandscapeSpecification;
import org.jzkit.search.SearchSession;
import org.jzkit.search.SortModel;
import org.jzkit.search.StatelessSearchResultsPageDTO;
import org.jzkit.search.impl.CachedSearchSession;
import org.jzkit.search.impl.LRUCache;
import org.jzkit.search.landscape.InfoTypeSpecification;
import org.jzkit.search.landscape.MixedSpecification;
import org.jzkit.search.landscape.SimpleLandscapeSpecification;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.util.QueryModel.QueryModel;
import org.jzkit.search.util.RecordModel.ExplicitRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.RecordFormatSpecification;
import org.jzkit.search.util.ResultSet.IRResultSetStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SuppressWarnings("unchecked")
public class OseeGeniusStatelessQueryService implements ApplicationContextAware
{
	private static final Log LOGGER = LogFactory.getLog(OseeGeniusStatelessQueryService.class);

	private ApplicationContext ctx;
	private LRUCache sessions_by_id;
	private Map<String, WeakReference<CachedSearchSession>> sessions_by_query = Collections.synchronizedMap(new HashMap());
	private int cache_size = 100;
	private long session_timeout = 180000;

	public OseeGeniusStatelessQueryService(int cache_size) 
	{
		this(cache_size, 0);
	}
	
	public OseeGeniusStatelessQueryService(int cache_size, long session_timeout) 
	{
		LOGGER.debug("OseeGeniusStatelessQueryService(" + cache_size + "," + session_timeout + ")");
		this.cache_size = cache_size;
		this.session_timeout = session_timeout;
		sessions_by_id = new LRUCache(cache_size, session_timeout);
	}

	public void setApplicationContext(ApplicationContext ctx) 
	{
		this.ctx = ctx;
	}

	public StatelessSearchResultsPageDTO getResultsPageFor(
			String resultSetId, QueryModel model,
			LandscapeSpecification landscape, int first_hit, int num_hits,
			RecordFormatSpecification rfs,
			ExplicitRecordFormatSpecification display_spec,
			Map additional_properties) throws SearchException,
			org.jzkit.search.util.ResultSet.IRResultSetException,
			org.jzkit.search.util.QueryModel.InvalidQueryException 
	{	
		StatelessSearchResultsPageDTO result = null;
		CachedSearchSession cachedSession = null;
		String queryString = null;
		
		SortModel sortModel = null;
		if (additional_properties != null)
		{
			sortModel = (SortModel) additional_properties.get("sort");
		}
		try 
		{
			queryString = model.toInternalQueryModel(ctx).toString();
		} catch (Exception exception) 
		{
			throw new org.jzkit.search.util.QueryModel.InvalidQueryException(
					"Unable to convert query to internal query model:" + model,
					exception);
		}

		String queryId = landscape + ":" + queryString;

		LOGGER.debug("getResultsPageFor (Landscape object) - " + landscape + " " + model + " rs:" + rfs + " ds:" + display_spec);

		// If we have been given a result set ID
		if (resultSetId != null) 
		{
			LOGGER.debug("got result set id " + resultSetId);
			cachedSession = (CachedSearchSession) sessions_by_id.get(resultSetId);
			if (cachedSession != null)
			{
				LOGGER.debug("A cached session associated with ResultSet " + resultSetId + " has been found.");
			}
		} else 
		{
			LOGGER.debug("Checking cache for the supplied query string \"" + queryId + "\"");
			// log.debug("cache size: "+sessions_by_query.size()+" keys : "+sessions_by_query.keySet());
			// see if we can detect an open session for the query string
			WeakReference<CachedSearchSession> weakSession = (WeakReference<CachedSearchSession>) sessions_by_query.get(queryId);
			if (weakSession != null) 
			{
				cachedSession = (CachedSearchSession) weakSession.get();
				if ((cachedSession != null) && (cachedSession.isActive())) 
				{
					// Freshen the LRU cache of queries by ID by calling get on
					// the result set ID. This
					// is a structural modification to the LRU map.
					if (sessions_by_id.get(cachedSession.getResultSetId()) != null) 
					{
						LOGGER.debug("Cache Hit on Query/Landscape ID - cache size = " + sessions_by_query.size());
					} else 
					{
						LOGGER.warn("Cache hit on Weak Reference for query, but get returns null. Cached object expired");
						sessions_by_query.remove(queryId);
						cachedSession = null;
					}
				} else {
					LOGGER.debug("Cache Hit, but query expunged from LRU list or not active, removing from sessions_by_query");
					sessions_by_query.remove(queryId);
					cachedSession = null;
				}
			}
		}

		if (sortModel != null)
		{
			sessions_by_id.remove(resultSetId);
			cachedSession = null;
		}
		
		if (cachedSession == null) 
		{
			LOGGER.info("Not located in cache... create new session");
			cachedSession = newSession(landscape, model, rfs, sortModel);
			if (cache_size > 0) 
			{
				LOGGER.info("Session cache enabled - count=" + sessions_by_id.size());
				sessions_by_id.put(cachedSession.getResultSetId(), cachedSession);
				sessions_by_query.put(queryId, new WeakReference(cachedSession));
			}
		}

		LOGGER.debug("Asking for page of results");
		if (cachedSession != null) 
		{
			result = cachedSession.getResultsPageFor(first_hit, num_hits, rfs, display_spec, additional_properties);
		} else 
		{
			LOGGER.warn("Cached search session was null!");
			throw new SearchException("Cached search session was null!");
		}

		// After obtaining the result, if there is no cache, close down the
		// session, it's never used again
		if (cache_size == 0) 
		{
			cachedSession.setActive(false);
			cachedSession.close();
		} else 
		{
			if (result.search_status == IRResultSetStatus.FAILURE) 
			{
				// Cache is enabled but result was failure. We don't cache
				// failed results, client must cause a retry.
				// Remove session from cache of sessions by query.
				LOGGER.debug("Removing failed search from session by query cache");
				sessions_by_query.remove(queryId);
			}
		}

		LOGGER.debug("getResultsPageFor - returning");
		return result;
	}

	public StatelessSearchResultsPageDTO getResultsPageFor(
			String result_set_id, String model_type, String query_string,
			String landscape_str, int first_hit, int num_hits,
			RecordFormatSpecification rfs,
			ExplicitRecordFormatSpecification display_spec,
			Map additional_properties) throws SearchException,
			org.jzkit.search.util.ResultSet.IRResultSetException,
			org.jzkit.search.util.QueryModel.InvalidQueryException {

		
		StatelessSearchResultsPageDTO result = null;
		CachedSearchSession cached_search_session = null;
		QueryModel model = null;
		SortModel sortModel = null;
		if (additional_properties != null)
		{
			sortModel = (SortModel) additional_properties.get("sort");
		}
		
		String qryid = landscape_str + ":" + query_string;

		// If we have been given a result set ID
		if (result_set_id != null) {
			LOGGER.debug("got result set id " + result_set_id);
			cached_search_session = (CachedSearchSession) sessions_by_id
					.get(result_set_id);
			if (cached_search_session != null)
				LOGGER.debug("Cache Hit on Result Set ID");
		} else {
			LOGGER.debug("Checking cache for the supplied query string \"" + qryid
					+ "\"");
			// log.debug("cache size: "+sessions_by_query.size()+" keys : "+sessions_by_query.keySet());

			// see if we can detect an open session for the query string
			// cached_search_session = (CachedSearchSession)
			// sessions_by_query.get(qryid);
			WeakReference wr = (WeakReference) sessions_by_query.get(qryid);
			if (wr != null) {
				LOGGER.debug("Weak Reference Cache Hit.. confirming cache status");
				cached_search_session = (CachedSearchSession) wr.get();
				if (cached_search_session != null) {
					if (sessions_by_id.get(cached_search_session
							.getResultSetId()) != null) {
						LOGGER
								.debug("Cache Hit on Query/Landscape ID - cache size = "
										+ sessions_by_query.size());
					} else {
						LOGGER
								.warn("Cache hit by query ID, but session not available in lru map");
						sessions_by_query.remove(qryid);
						cached_search_session = null;
					}
				} else {
					LOGGER
							.debug("Cache Hit for weak reference, but cached session itself has been expunged, removing from sessions by query (cache size="
									+ cache_size + ")");
					sessions_by_query.remove(qryid);
				}
			} else {
				LOGGER.debug("No Weak Reference Cache Hit");
			}
		}

		if (cached_search_session == null) {
			if ((landscape_str != null) && (query_string != null)
					&& (query_string.length() > 0)) {

				LOGGER.debug("Not located in cache... create new session");

				if (model_type.equalsIgnoreCase("cql")) {
					model = new org.jzkit.search.util.QueryModel.CQLString.CQLString(
							query_string);
				} else if (model_type.equalsIgnoreCase("pqf")) {
					model = new org.jzkit.search.util.QueryModel.PrefixString.PrefixString(
							query_string);
				} else {
					LOGGER.warn("Unsupported query");
					throw new org.jzkit.search.util.QueryModel.InvalidQueryException(
							"Unsupported Query Type. Currently Supported types are pqf and cql");
				}

				LOGGER.debug("Storring new session using ID " + qryid);
				cached_search_session = newSession(landscapeStringToDef(landscape_str), model, rfs, sortModel);
				sessions_by_id.put(cached_search_session.getResultSetId(), cached_search_session);
				sessions_by_query.put(qryid, new WeakReference(cached_search_session));
			} else 
			{
				throw new SearchException(
						"No Query or Cache ID Not Found, landscape="
								+ landscape_str + ",query_string="
								+ query_string);
			}
		}

		if (cached_search_session != null) 
		{
			LOGGER.debug("Asking for page of results");
			result = cached_search_session.getResultsPageFor(first_hit,
					num_hits, rfs, display_spec, additional_properties);
		} else 
		{
			LOGGER.warn("Cached search session was null!");
			throw new SearchException("Cached search session was null!");
		}

		if (result != null)
			result.result_set_idle_time = this.session_timeout;

		LOGGER.debug("Leaving getResultsPageFor, result=" + result);
		return result;
	}

	private CachedSearchSession newSession(LandscapeSpecification landscape, QueryModel model, RecordFormatSpecification rfs, SortModel sortModel) throws SearchException 
	{
		return newSession(landscape, model, null, sortModel, rfs);
	}

	private CachedSearchSession newSession(LandscapeSpecification landscape,
			QueryModel model, DeduplicationModel deduplication_model,
			SortModel sort_model, RecordFormatSpecification rfs)
			throws SearchException {
		LOGGER.debug("newSession");
		CachedSearchSession cached_search_session = new CachedSearchSession((SearchSession) ctx.getBean("SearchSession"), ctx);
		cached_search_session.search(landscape, model, deduplication_model,sort_model, rfs);
		return cached_search_session;
	}

	private LandscapeSpecification landscapeStringToDef(String landscape) {
		// return new InfoTypeSpecification("InfoType",landscape_str);
		MixedSpecification result = new MixedSpecification();

		String[] components = landscape.split(",");
		for (int i = 0; i < components.length; i++) {
			String[] def_parts = components[i].split(":");
			if (def_parts.length == 1) {
				result.addSpecification(new InfoTypeSpecification("InfoType",
						def_parts[0]));
			} else {
				if (def_parts[0].equalsIgnoreCase("infotype")) {
					result.addSpecification(new InfoTypeSpecification(
							"InfoType", def_parts[1]));
				} else if (def_parts[0].equalsIgnoreCase("collection")) {
					result.addSpecification(new SimpleLandscapeSpecification(
							def_parts[1]));
				} else {
					LOGGER.warn("Unhandled landscape specification component: "
							+ def_parts[0] + ":" + def_parts[1]);
				}
			}
		}
		return result;
	}

	public void init() {
		LOGGER.debug("init()");
	}
}

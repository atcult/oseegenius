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
package com.atc.osee.web.servlets.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.Layout;
import com.atc.osee.web.Page;
import com.atc.osee.web.model.history.SearchHistory;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * Show search history servlet.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ShowHistoryServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 1L;
	private static final String SIMPLE_SEARCHES = "simple";
	private static final String ADVANCED_SEARCHES = "advanced";
	private static final String BROWSING = "browsing";
	private static final String FEDERATED = "federated";
	
	/**
	 * Abstract interface for a search history command.
	 * 
	 * @author agazzarini
	 * @since 1.0
	 */
	private interface HistorySectionViewHandler 
	{
		/**
		 * Shows a search history section.
		 * 
		 * @param request the HTTP request.
		 * @param response the HTTP response.
		 * @param searchHistory the anonymous user search history.
		 * 
		 * @throws ServletException in case of servlet container failure.
		 * @throws IOException in case of I/O failure.
		 */
		void service(HttpServletRequest request, HttpServletResponse response, SearchHistory searchHistory)throws ServletException, IOException;
	}
	
	private final HistorySectionViewHandler simpleSectionViewHandler = new HistorySectionViewHandler()
	{
		@Override
		public void service(
				HttpServletRequest request,
				HttpServletResponse response, 
				SearchHistory searchHistory) throws ServletException,
				IOException 
		{
			if (searchHistory.isSimpleSearchListNotEmpty())
			{
				setRequestAttribute(
						request, 
						HttpAttribute.SEARCH_HISTORY_ENTRIES, 
						searchHistory.getSimpleSearchEntries());
				forwardTo(request, response, "/history/simple_searches.vm","history_layout.vm");
			} else
			{
				goToFirstAvailableSectionOrGotoHome(request, response);
			}
		}
	};
	
	private final HistorySectionViewHandler advancedSectionViewHandler = new HistorySectionViewHandler()
	{
		@Override
		public void service(
				HttpServletRequest request,
				HttpServletResponse response, 
				SearchHistory searchHistory) throws ServletException,
				IOException 
		{
			if (searchHistory.isAdvancedSearchListNotEmpty())
			{
				setRequestAttribute(
						request, 
						HttpAttribute.SEARCH_HISTORY_ENTRIES, 
						searchHistory.getAdvancedSearchEntries());
				forwardTo(request, response, "/history/advanced_searches.vm","history_layout.vm");
			} else
			{
				goToFirstAvailableSectionOrGotoHome(request, response);
			}
		}
	};
	
	private final HistorySectionViewHandler browsingSectionViewHandler = new HistorySectionViewHandler()
	{
		@Override
		public void service(
				HttpServletRequest request,
				HttpServletResponse response, 
				SearchHistory searchHistory) throws ServletException,
				IOException 
		{
			if (searchHistory.isBrowsingListNotEmpty())
			{
				setRequestAttribute(
						request, 
						HttpAttribute.SEARCH_HISTORY_ENTRIES, 
						searchHistory.getBrowsingEntries());
				forwardTo(request, response, "/history/browsing.vm","history_layout.vm");
			} else
			{
				goToFirstAvailableSectionOrGotoHome(request, response);
			}
		}
	};	
	
	private final HistorySectionViewHandler federatedSectionViewHandler = new HistorySectionViewHandler()
	{
		@Override
		public void service(
				HttpServletRequest request,
				HttpServletResponse response, 
				SearchHistory searchHistory) throws ServletException,
				IOException 
		{
			if (searchHistory.isFederatedSearchListNotEmpty())
			{
				setRequestAttribute(
						request, 
						HttpAttribute.SEARCH_HISTORY_ENTRIES, 
						searchHistory.getFederatedSearchEntries());
				forwardTo(request, response, "/history/federated_searches.vm","history_layout.vm");
			} else
			{
				goToFirstAvailableSectionOrGotoHome(request, response);
			}
		}
	};

	private void goToFirstAvailableSectionOrGotoHome(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		try 
		{
			String section = getFirstAvailableHistorySection(request);
			HistorySectionViewHandler viewHandler = viewHandlers.get(section); 
			viewHandler.service(request, response, getVisit(request).getSearchHistory());
		} catch (Exception exception)
		{
			//response.sendRedirect("home");
			response.sendRedirect(getHomeServletName());
		}
	}
	
	private Map<String, HistorySectionViewHandler> viewHandlers = new HashMap<String, HistorySectionViewHandler>();
	{
		viewHandlers.put(SIMPLE_SEARCHES, simpleSectionViewHandler);
		viewHandlers.put(ADVANCED_SEARCHES, advancedSectionViewHandler);
		viewHandlers.put(BROWSING, browsingSectionViewHandler);
		viewHandlers.put(FEDERATED, federatedSectionViewHandler);
	}

	@Override
	protected void doPost(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		String historyEntryId = request.getParameter(HttpParameter.ID);
		if (isNotNullOrEmptyString(historyEntryId))
		{
			getVisit(request).getSearchHistory().removeEntry(historyEntryId);
		}
	}
	
	@Override
	protected void doGet(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		
		HttpSession session = request.getSession(false);
		if (session == null || session.isNew())
		{
			SearchHistory history = getVisit(request).getSearchHistory();
			if(history.isEmpty()){
				request.setAttribute("session_time_out", "true");
				forwardTo(
						request, 
						response, 
						Page.HOME_PAGE,
						Layout.HOME_PAGE);
				return;
			}			
		}
		
		String section = request.getParameter(HttpParameter.SECTION);
		
		if (isNullOrEmptyString(section))
		{
			section = (String) request.getSession(true).getAttribute(HttpAttribute.HISTORY_SECTION);
			if (isNullOrEmptyString(section))
			{
				section = getFirstAvailableHistorySection(request);
				request.getSession(true).setAttribute(HttpAttribute.HISTORY_SECTION, section);
			}
		} else 
		{
			if (viewHandlers.containsKey(section))
			{
				request.getSession(true).setAttribute(HttpAttribute.HISTORY_SECTION, section);
			} else
			{
				section = getFirstAvailableHistorySection(request);
				request.getSession(true).setAttribute(HttpAttribute.HISTORY_SECTION, section);
			}
		}

		HistorySectionViewHandler viewHandler = viewHandlers.get(section); 
		viewHandler.service(request, response, getVisit(request).getSearchHistory());
	}
	
	/**
	 * Returns the first available (not empty) search history category.
	 * That will be selected in case no previous selection has been made on a specific category.
	 * 
	 * @param request the HTTP request.
	 * @return the first available (not empty) search history category.
	 */
	private String getFirstAvailableHistorySection(final HttpServletRequest request) 
	{
		SearchHistory history = getVisit(request).getSearchHistory();
		if (history.isSimpleSearchListNotEmpty())
		{
			return SIMPLE_SEARCHES;
		}
		
		if (history.isAdvancedSearchListNotEmpty())
		{
			return ADVANCED_SEARCHES;
		}
		
		if (history.isBrowsingListNotEmpty())
		{
			return BROWSING;
		}
		
		if (history.isFederatedSearchListNotEmpty())
		{
			return FEDERATED;
		}
		
		throw new IllegalArgumentException("Unable to determine an available  (not empty) search history section.");
	}
	
	public String getHomeServletName()
	{
		return "home";
	}
}
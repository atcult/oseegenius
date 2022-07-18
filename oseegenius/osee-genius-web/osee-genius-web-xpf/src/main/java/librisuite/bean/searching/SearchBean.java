/*
 * (c) LibriCore
 * 
 * Created on 17-aug-2004
 * 
 * SearchBean.java
 */
package librisuite.bean.searching;

import javax.servlet.http.HttpServletRequest;

import librisuite.bean.LibrisuiteBean;
import librisuite.business.common.Defaults;
import librisuite.business.common.View;
import librisuite.business.searching.NoResultsFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.libricore.librisuite.controller.SessionUtils;

/**
 * @author Wim Crols
 * @version $Revision: 1.6 $, $Date: 2006/07/11 08:01:05 $
 * @since 1.0
 */
public abstract class SearchBean extends LibrisuiteBean {

	private static Log logger = LogFactory.getLog(SearchBean.class);


	private boolean simpleSearch = true;

	private boolean advancedSearch = false;

	private boolean expertSearch = false;

	private boolean browseIndexes = false;

	
	private int currentSearchingView = -2;

	private String query;

	/**
	 * @return a string identifying the search type (simple, advanced, expert) for
	 * use in actionForward mappings (edit historic queries)
	 * 
	 * @since 1.0
	 */
	abstract public String getSearchType();

	/**
	 * Indicates whether the current search is bibliographic or not (i.e. authority)
	 * @since 1.0
	 */

	public boolean isBibliographic() {
		return currentSearchingView > View.AUTHORITY;
	}
	public boolean isMades() {
		return currentSearchingView < View.AUTHORITY;
	}
	public boolean isAuthoriry() {
		return currentSearchingView == View.AUTHORITY;
	}

	public void setSearchingView(int view) {
		currentSearchingView = view;
	}

	public static SearchBean getInstance(HttpServletRequest httpServletRequest) {
		SearchBean searchBean =
			(SearchBean) getSessionAttribute(httpServletRequest,
				SearchBean.class);
		
		return searchBean;
	}

	/**
	 * This method clears all tab booleans
	 */
	private void clearTabBooleans() {
		this.simpleSearch = false;
		this.advancedSearch = false;
		this.expertSearch = false;
		this.browseIndexes = false;
	}

	/**
	 * @return Returns the advancedSearch.
	 */
	public boolean isAdvancedSearch() {
		return advancedSearch;
	}

	/**
	 * @param advancedSearch The advancedSearch to set.
	 */
	public void setAdvancedSearch(boolean advancedSearch) {
		if (advancedSearch) {
			clearTabBooleans();
			this.advancedSearch = advancedSearch;
		}
	}

	/**
	 * @return Returns the browseIndexes.
	 */
	public boolean isBrowseIndexes() {
		return browseIndexes;
	}

	/**
	 * @param browseIndexes The browseIndexes to set.
	 */
	public void setBrowseIndexes(boolean browseIndexes) {
		if (browseIndexes) {
			clearTabBooleans();
			this.browseIndexes = browseIndexes;
		}
	}

	/**
	 * @return Returns the expertSearch.
	 */
	public boolean isExpertSearch() {
		return expertSearch;
	}

	/**
	 * @param expertSearch The expertSearch to set.
	 */
	public void setExpertSearch(boolean expertSearch) {
		if (expertSearch) {
			clearTabBooleans();
			this.expertSearch = expertSearch;
		}
	}

	/**
	 * @return Returns the simpleSearch.
	 */
	public boolean isSimpleSearch() {
		return simpleSearch;
	}

	

	/**
	 * @param simpleSearch The simpleSearch to set.
	 */
	public void setSimpleSearch(boolean simpleSearch) {
		if (simpleSearch) {
			clearTabBooleans();
			this.simpleSearch = simpleSearch;
		}
	}

	/**
	 * 
	 * @since 1.0
	 */
	/*public SearchEngine getSearchEngine() {
		return searchEngine;
	}*/

	/**
	 * 
	 * @since 1.0
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setQuery(String string) {
		logger.debug("Setting query to '" + string + "'");
		
		query = string;
	}

	

}
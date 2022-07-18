package com.atc.osee.web.model;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.impl.client.CloseableHttpClient;

import com.atc.osee.web.folio.FolioUserModel;
import com.atc.osee.web.model.history.SearchHistory;

/**
 * A user visit.
 * Domain model object that is associated with a user session.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class Visit implements Serializable
{
	private static final long serialVersionUID = -948712467174286636L;
	
	private Locale preferredLocale = Locale.ITALIAN;
	
	private final SearchExperience searchExperience;
	private final SearchExperience thSearchExperience;
	private final AdvancedSearchExperience advancedSearchExperience;
	private final BrowsingExperience browsingExperience;
	private Account account;
	private FolioUserModel folioAccount;
	private SearchHistory searchHistory;
	private ConcurrentHashMap<String, String> indexMap;
	
	private String preferredFontSize = "normal";
	
	/**
	 * Builds a new visit.
	 * 
	 * @param federatedSearchClient the HTTP federated search client.
	 * @param federatedSearchUrl the federated search endpoint URL.
	 */
	public Visit(final CloseableHttpClient federatedSearchClient, final String federatedSearchUrl)
	{
		searchExperience = new SearchExperience(federatedSearchClient, federatedSearchUrl);
		thSearchExperience = new SearchExperience(null, null);
		advancedSearchExperience = new AdvancedSearchExperience();
		browsingExperience = new BrowsingExperience(this);
		searchHistory = new SearchHistory();		
		indexMap = new ConcurrentHashMap<String, String>(10);
	}
	
	public ConcurrentHashMap<String, String> getIndexMap () {
		return indexMap;
	}
	
	public void addIndexToMap (String index, String label) {
		indexMap.put(index, label);
	}

	public String getPreferredFontSize() {
		return preferredFontSize;
	}

	public void setPreferredFontSize(String preferredFontSize) {
		if ("normal".equals(preferredFontSize) || "medium".equals(preferredFontSize) || "large".equals(preferredFontSize))
		{
			this.preferredFontSize = preferredFontSize;
		}
	}

	/**
	 * Sets the preferred locale for this visit.
	 * 
	 * @param preferredLocale the preferred locale for this visit.
	 */
	public void setPreferredLocale(final Locale preferredLocale) 
	{
		this.preferredLocale = preferredLocale;
	}

	/**
	 * Returns the search experience of the visit associated with this visit.
	 * 
	 * @return the search experience of the visit associated with this visit.
	 */
	public SearchExperience getSearchExperience()
	{
		return searchExperience;
	}

	/**
	 * Returns the thgenius search experience of the visit associated with this visit.
	 * 
	 * @return the thgenius search experience of the visit associated with this visit.
	 */
	public SearchExperience getThSearchExperience()
	{
		return thSearchExperience;
	}

	/**
	 * Returns the browsing experience of the visit associated with this visit.
	 * 
	 * @return the browsing experience of the visit associated with this visit.
	 */
	public BrowsingExperience getBrowsingExperience()
	{
		return browsingExperience;
	}
	
	/**
	 * Returns the advanced search experience of the visit associated with this visit.
	 * 
	 * @return the advanced search experience of the visit associated with this visit.
	 */
	public AdvancedSearchExperience getAdvancedSearchExperience()
	{
		return advancedSearchExperience;
	}

	/**
	 * Returns the preferred locale of this visit.
	 * 
	 * @return the preferred locale of this visit.
	 */
	public Locale getPreferredLocale()
	{
		return preferredLocale;
	}
	
	/**
	 * Checks if the current session is not authenticated..
	 * 
	 * @return true if the current session is not authenticated.
	 */
	public boolean isAnonymous()
	{
		return account == null;
	}
	
	/**
	 * Checks if the current session is authenticated..
	 * 
	 * @return true if the current session is authenticated.
	 */
	public boolean isFolioAuthenticated()
	{
		return this.folioAccount != null;
	}
	
	/**
	 * Injects account onto this visit.
	 * Starting from here, this session will be supposed as not anonymous.
	 * 
	 * @param account the OseeGenius user account.
	 */
	public void injectFolioAccount(final FolioUserModel account)
	{
		this.folioAccount = account;
	}
	
	/**
	 * Returns the OseeGenius account associated with this visit.
	 * 
	 * @return the OseeGenius account associated with this visit, null if the session is anonymous.
	 */
	public FolioUserModel getFolioAccount()
	{
		return this.folioAccount;
	}
	
	/**
	 * Returns the search history associated with this visit.
	 * 
	 * @return the search history associated with this visit.
	 */
	public SearchHistory getSearchHistory()
	{
		return searchHistory;
	}
	
	/**
	 * Returns true if the search history contains at least one entry.
	 * 
	 * @return true if the search history contains at least one entry.
	 */
	public boolean isHistoryNotEmpty()
	{
		return !searchHistory.isEmpty();
	}
	
	/**
	 * Checks if the current session is authenticated..
	 * 
	 * @return true if the current session is authenticated.
	 */
	public boolean isAuthenticated()
	{
		return account != null;
	}
	
	/**
	 * Injects account onto this visit.
	 * Starting from here, this session will be supposed as not anonymous.
	 * 
	 * @param account the OseeGenius user account.
	 */
	public void injectAccount(final Account account)
	{
		this.account = account;
	}
	
	/**
	 * Returns the OseeGenius account associated with this visit.
	 * 
	 * @return the OseeGenius account associated with this visit, null if the session is anonymous.
	 */
	public Account getAccount()
	{
		return account;
	}
	
}
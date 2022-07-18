package com.atc.osee.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;

import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.servlets.search.federated.PazPar2;

/**
 * Search experience domain model.
 * 
 * @author agazzarini
 * @author ggazzarini
 * @since 1.0
 */
public final class SearchExperience implements Serializable, ItemSelection
{
	private static final long serialVersionUID = 8581485468726471540L;
	private final List<SearchTab> tabs;
	private SearchTab currentTab;
	
	private int tabIndex;
	
	private final CloseableHttpClient federatedSearchClient;
	private final String federatedSearchUrl;

	private Set<String> selectedItems = new HashSet<String>();
	private Set<String> itemsThatCanBeSent = new HashSet<String>();
	
	private List<SolrDocument> folderContent = new ArrayList<SolrDocument>();	
	private HashMap<String, NamedList<String>> copyFolderContent = new HashMap<String, NamedList<String>>();
	
	private Set<String> selectedCopies = new HashSet<String>();
	
	
	public Set<String> getSelectedCopies() {
		return selectedCopies;
	}
	
	/**
	 * Builds a new search experience with the given data.
	 * 
	 * @param federatedSearchClient the federated search client.
	 * @param federatedSearchUrl the federated search URL.
	 */
	public SearchExperience(final CloseableHttpClient federatedSearchClient, final String federatedSearchUrl)
	{
		this.federatedSearchClient = federatedSearchClient;
		this.federatedSearchUrl = federatedSearchUrl;
		tabs = new ArrayList<SearchTab>();
		addNewTab();
	}

	/**
	 * Returns true if federated search is enabled.
	 * 
	 * @return true if federated search is enabled.
	 */
	public boolean isFederatedSearchEnabled()
	{
		return federatedSearchClient != null && federatedSearchUrl != null && federatedSearchUrl.trim().length() != 0;
	}
	
	/**
	 * Returns the search tab associated with the given identifier.
	 * 
	 * @param tabId the tab identifier.
	 * @return the search tab associated with the given identifier.
	 */
	public SearchTab getTab(final int tabId)
	{		
		for (SearchTab tab : tabs)
		{
			if (tabId == tab.getId())
			{
				currentTab = tab;
			}
		}
		
		return currentTab;
	}
	
	/**
	 * Returns the search tabs.
	 * 
	 * @return the search tabs.
	 */
	public List<SearchTab> getTabs()
	{
		return tabs;
	}

	/**
	 * Returns the current tab.
	 * 
	 * @return the current tab.
	 */
	public SearchTab getCurrentTab()
	{
		return currentTab;
	}
	
	/**
	 * Returns the how many result in  tab.
	 * 
	 * @return the current tab result number.
	 */
	public int getHowManyResultTab()
	{
		return currentTab.howManyResult();
	}
	
	/**
	 * Closes the current tab.
	 */
	public void closeTab()
	{
		int indexOfCurrentTab = tabs.indexOf(currentTab);
		tabs.remove(currentTab);

		if (tabs.size() == 0)
		{
			addNewTab();			
		} else 
		{
			int index = (indexOfCurrentTab - 1);
			currentTab = tabs.get(index  >= 0 ? index : 0);
		}
	}
	
	/**
	 * Adds a new search tab and returns it.
	 * 
	 * @return the new (and current) search tab.
	 */
	public SearchTab addNewTab()
	{
		currentTab = new SearchTab(++tabIndex, this);
		currentTab.setResultsPerPage(10);
		tabs.add(currentTab);		
		
		if (isFederatedSearchEnabled())
		{
			currentTab.injectFederatedSearchStuff(federatedSearchClient, federatedSearchUrl);
		}
		
		return getCurrentTab();
	}
	
	/**
	 * Adds a new search tab and returns it.
	 * 
	 * @return the new (and current) search tab.
	 */
	public SearchTab addNewTab(final PazPar2 pazpar2)
	{
		currentTab = new SearchTab(++tabIndex, this);
		currentTab.setResultsPerPage(10);
		tabs.add(currentTab);		
		
		if (isFederatedSearchEnabled())
		{
			currentTab.injectFederatedSearchStuff(pazpar2);
		}
		
		return getCurrentTab();
	}

	/**
	 * Sets the current tab.
	 * 
	 * @param tabId the tab identifier.
	 */
	public void setCurrentTab(final int tabId)
	{
		for (SearchTab tab : tabs)
		{
			if (tabId == tab.getId())
			{
				currentTab = tab;
			}
		}
	}
	
	/**
	 * Returns true if the tab is empty.
	 * 
	 * @return true if the tab is empty.
	 */
	public boolean isEmpty()
	{
		return tabs.size() == 1 && currentTab.isEmpty();
	}
	
	/**
	 * Resets all tabs.
	 */
	public void resetTabs()
	{
		tabs.clear();
		currentTab.clear();
		tabs.add(currentTab);
	}
	
	/**
	 * Returns the total number of documents that can be exported.
	 * 
	 * @return the total number of documents that can be exported. 
	 */
	public int howManySelectedForExport()
	{
		return selectedItems.size();
	}
	
	/**
	 * Returns the total number of copies selected
	 * 
	 * @return the total number of copies selected
	 */
	public int howManyCopySelected() {
		return selectedCopies.size();
	}
	
	/**
	 * Returns the total number of documents that can be sent by email.
	 * 
	 * @return the total number of documents that can be sent by email. 
	 */
	public int howManySelectedForSend()
	{
		return itemsThatCanBeSent.size();
	}	
	
	/**
	 * Returns the selected documents that can be sent by email.
	 * 
	 * @return the selected documents that can be sent by email.
	 */
	public Set<String> getSelectedItemsForEmail()
	{
		return itemsThatCanBeSent;
	}
	
	/**
	 * Returns the selected documents that can be exported.
	 * 
	 * @return the selected documents that can be exported.
	 */
	public Set<String> getSelectedItemsForExportOrDownload()
	{
		return selectedItems;
	}
	
	/**
	 * Selects / deselects the copies associated with the given identifier.
	 * 
	 * @param id the copy identifier.
	 * @return true if a copy have been selected, false if has been deselected.
	 */
	public boolean toggleCopySelection(final String id)
	{
		if (selectedCopies.contains(id))
		{
			return !selectedCopies.remove(id);
		} else 
		{
			selectedCopies.add(id);
			return true;
		}
	}	
	
	
	/**
	 * Selects / deselects the document associated witht the given identifier.
	 * 
	 * @param id the document identifier.
	 * @return true if a document have been selected, false if has been deselected.
	 */
	public boolean toggleSelection(final String id)
	{
		if (selectedItems.contains(id))
		{
			return !selectedItems.remove(id);
		} else 
		{
			selectedItems.add(id);
			return true;
		}
	}	
	
	/**
	 * Selects / deselects an item that can be sent by email.
	 * 
	 * @param id the document identifier.
	 * @return true if a document have been selected, false if has been deselected.
	 */
	public boolean toggleSendableSelection(final String id)
	{
		if (itemsThatCanBeSent.contains(id))
		{
			return !itemsThatCanBeSent.remove(id);
		} else 
		{
			itemsThatCanBeSent.add(id);
			return true;
		}
	}

	@Override
	public String getTitle() 
	{
		return IConstants.EMPTY_STRING;
	}	
	
	public boolean isSomethingSelected()
	{
		return !folderContent.isEmpty();
	}
	
	public boolean isSomeCopySelected()
	{		
		return !copyFolderContent.isEmpty();
	}
	
	
	
	public Collection<NamedList<String>> getCopyContent() {	
		return copyFolderContent.values();
	}
	
	public HashMap<String, NamedList<String>> getCopyMap() {
		return copyFolderContent;
	}
	
	public List<SolrDocument> getContent()
	{
		return folderContent;
	}
	
	public boolean doesFolderAlreadyContains(final Object id)
	{
		for (SolrDocument folderItem : folderContent)
		{
			if (folderItem.getFieldValue(ISolrConstants.ID_FIELD_NAME).equals(id))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isFolderAlreadySelected()
	{
		return selectedItems.size() != 0 && selectedItems.size() == folderContent.size();
	}
	
	public boolean isCopyFolderAlreadySelected()
	{
		return selectedCopies.size() != 0 && selectedCopies.size() == copyFolderContent.keySet().size();
	}
	
	public boolean isSelected(final String id)
	{
		return selectedItems.contains(id);
	}
		
	public boolean isCopySelected(final String id)  {
		return selectedCopies.contains(id);
	}
	
	public void add2Folder(SolrDocument document)
	{
		if (!doesFolderAlreadyContains(document.get(ISolrConstants.ID_FIELD_NAME)))
		{
			folderContent.add(document);
		}
	}
	
	public void add2CopyFolder(NamedList<String> copy) {
		if (!copyFolderContent.containsKey((String) copy.get("barcode"))){
			copyFolderContent.put((String) copy.get("barcode"),copy);				
		}
	}

	public void cleanSelection() 
	{
		itemsThatCanBeSent.clear();
		selectedItems.clear();
	}
	
	public void cleanCopySelection() 
	{
		selectedCopies.clear();
	}
	
	public void removeCopySelectionFromFolder() 
	{
		for (Iterator<String> iterator = copyFolderContent.keySet().iterator(); iterator.hasNext();)
		{
			String id = iterator.next();
			if (selectedCopies.contains(id))
			{
				iterator.remove();
				selectedCopies.remove(id);				
			}
		}
	}

	public void removeSelectionFromFolder() 
	{
		for (Iterator<SolrDocument> iterator = folderContent.iterator(); iterator.hasNext();)
		{
			Object id = iterator.next().getFieldValue(ISolrConstants.ID_FIELD_NAME);
			if (selectedItems.contains(id))
			{
				iterator.remove();
				selectedItems.remove(id);
				itemsThatCanBeSent.remove(id);
			}
		}
	}

	public Iterator<SolrDocument> selection() 
	{
		return new Iterator<SolrDocument>() 
		{
			private Iterator<SolrDocument> iterator = folderContent.iterator();
			
			private SolrDocument nextSelectedItem;
			
			@Override
			public boolean hasNext() 
			{
				while (iterator.hasNext())
				{
					SolrDocument document = iterator.next();
					Object id = document.getFieldValue(ISolrConstants.ID_FIELD_NAME);
					if (selectedItems.contains(id))
					{
						nextSelectedItem = document;
						return true;
					} else 
					{
						nextSelectedItem = null;
					}
				}
				return false;
			}

			@Override
			public SolrDocument next() 
			{
				return nextSelectedItem;
			}

			@Override
			public void remove() 
			{
				// Not implemented for this iterator.
			}
		};
	}
	
	public List<SolrDocument> getSelectedDocuments()
	{
		List<SolrDocument> result = new ArrayList<SolrDocument>(howManySelectedForExport());
		for (Iterator<SolrDocument> iterator = folderContent.iterator(); iterator.hasNext();)
		{
			SolrDocument document = iterator.next();
			Object id = document.getFieldValue(ISolrConstants.ID_FIELD_NAME);
			if (selectedItems.contains(id))
			{
				result.add(document);
			}
		}
		return result;
	}

	public void selectAll() 
	{
		for (SolrDocument doc : folderContent)
		{
			String uri = (String) doc.getFieldValue(ISolrConstants.ID_FIELD_NAME);
			itemsThatCanBeSent.add(uri);
			selectedItems.add(uri);
		}
	}
	
	public void selectAllCopies() 
	{
		for (String id : copyFolderContent.keySet())
		{				
			selectedCopies.add(id);
		}
	}
}
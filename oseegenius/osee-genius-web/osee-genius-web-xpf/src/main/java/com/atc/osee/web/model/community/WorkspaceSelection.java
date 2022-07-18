package com.atc.osee.web.model.community;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.atc.osee.web.model.ItemSelection;

/**
 * Workspace selected item container.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class WorkspaceSelection implements Serializable, ItemSelection
{
	private static final long serialVersionUID = -6653603407949298976L;
	
	private Set<String> itemsThatCanBeSent = new HashSet<String>();
	
	private Set<String> selectedItems = new HashSet<String>();
	
	/* (non-Javadoc)
	 * @see com.atc.osee.web.model.community.ItemSelection#howManySelectedForSend()
	 */
	@Override
	public int howManySelectedForSend()
	{
		return itemsThatCanBeSent.size();
	}
	
	/* (non-Javadoc)
	 * @see com.atc.osee.web.model.community.ItemSelection#toggleSendableSelection(java.lang.String)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see com.atc.osee.web.model.community.ItemSelection#toggleSelection(java.lang.String)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see com.atc.osee.web.model.community.ItemSelection#howManySelectedForExport()
	 */
	@Override
	public int howManySelectedForExport()
	{
		return selectedItems.size();
	}	
	
	public boolean contains(String id)
	{
		return itemsThatCanBeSent.contains(id);
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

	@Override
	public String getTitle() 
	{
		return DateFormat.getDateInstance(DateFormat.LONG).format(new Date()).replaceAll(" ", "_");
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
	 * Deselects all documents.
	 */
	public void clearSelection()
	{
		selectedItems.clear();
		itemsThatCanBeSent.clear();
	}
}
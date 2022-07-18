package com.atc.osee.web.model;

import java.util.Set;

public interface ItemSelection {

	/**
	 * Returns the total number of documents that can be sent by email.
	 * 
	 * @return the total number of documents that can be sent by email. 
	 */
	int howManySelectedForSend();

	/**
	 * Selects / deselects an item that can be sent by email.
	 * 
	 * @param id the document identifier.
	 * @return true if a document have been selected, false if has been deselected.
	 */
	boolean toggleSendableSelection(final String id);

	/**
	 * Selects / deselects the document associated witht the given identifier.
	 * 
	 * @param id the document identifier.
	 * @return true if a document have been selected, false if has been deselected.
	 */
	boolean toggleSelection(final String id);

	/**
	 * Returns the total number of documents that can be exported.
	 * 
	 * @return the total number of documents that can be exported. 
	 */
	int howManySelectedForExport();

	/**
	 * Returns the selected items that can be exported.
	 * 
	 * @return the selected items that can be exported.
	 */
	Set<String> getSelectedItemsForExportOrDownload();

	/**
	 * Returns a (possibly) human-readable title for the current selection.
	 * 
	 * @return a (possibly) human-readable title for the current selection.
	 */
	String getTitle();

	/**
	 * Returns the selected items that can be sent by email.
	 * 
	 * @return the selected items that can be sent by email.
	 */
	Set<String> getSelectedItemsForEmail();
}
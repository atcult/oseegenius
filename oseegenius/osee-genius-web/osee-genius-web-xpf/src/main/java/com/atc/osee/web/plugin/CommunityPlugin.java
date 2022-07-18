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
package com.atc.osee.web.plugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.community.ItemCommunityData;
import com.atc.osee.web.model.community.Review;
import com.atc.osee.web.model.community.Tag;

/** 
 * OseeGenius -W- Social / Community Plugin interface.
 * 
 * @author Giorgio Gazzarini
 * @since 1.0
 */
public interface CommunityPlugin extends DatabasePlugin
{
	/**
	 * Returns all the reviews associated with the given account grouped by target document.
	 * 
	 * @param accountId the account identifier.
	 * @return all the reviews associated with the given account grouped by target document.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	Map<String, List<Review>> getReviews(int accountId) throws SystemInternalFailureException;

	/**
	 * Returns all the reviews associated with a given account. 
	 * @param documentURI the URI of the document.
	 * @param includeToBeModerated if reviews not yet moderated needs to be included.
	 * @param limit the max number of documents that must be returned.
	 * @param accountId the account identifier.
	 * @return all the reviews associated with a given account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	List<Review> getUserDocumentReviews(String documentURI, boolean includeToBeModerated, int limit, int accountId) throws SystemInternalFailureException;

	/**
	 * Returns the tags associated with the given account.
	 * 
	 * @param accountId the account identifier.
	 * @return the tags associated with the given account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	List<Tag> getTags(int accountId) throws SystemInternalFailureException;
	
	/**
	 * Returns the documents that have been associated with the a (user) tag account.
	 * 
	 * @param accountId the account identifier.
	 * @param tagId the tag identifier.
	 * @return the documents that have been associated with the a (user) tag account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	List<String> getTaggedDocuments(int accountId, long tagId) throws SystemInternalFailureException;
	
	/**
	 * Removes a user tag.
	 * All associations between documents and this tag will be removed. 
	 * 
	 * @param accountId the account identifier.
	 * @param tagId the tag identifier.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	void removeTag(int accountId, long tagId) throws SystemInternalFailureException;
	
	/**
	 * Counts the total nunber of tags associated with a given account.
	 * 
	 * @param accountId the account identifier.
	 * @return the total nunber of tags associated with a given account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	int countTags(int accountId) throws SystemInternalFailureException;

	/**
	 * Counts the total nunber of tags associated with a given account.
	 * 
	 * @param accountId the account identifier.
	 * @return the a map containing wish lists id and their names..
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	Map<Long, String> getWishLists(int accountId) throws SystemInternalFailureException;
	
	/**
	 * Counts the total nunber of reviews associated with a given account.
	 * 
	 * @param accountId the account identifier.
	 * @return the total nunber of reviews associated with a given account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	int countReviews(int accountId) throws SystemInternalFailureException;	
	
	/**
	 * Counts the size of the bibliography of a given account.
	 * 
	 * @param accountId the account identifier.
	 * @return the size of the bibliography of a given account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	int getBibliographySize(int accountId) throws SystemInternalFailureException;		
	
	/**
	 * Returns the bibliography of a given account.
	 * 
	 * @param accountId the account identifier.
	 * @return the bibliography of a given account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	List<String> getBibliography(int accountId) throws SystemInternalFailureException;

	/**
	 * Removes  a set of items from the user bibliography.
	 * 
	 * @param accountId the account identifier.
	 * @param set the document id list.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	void removeFromBibliography(int accountId, Set<String> set)  throws SystemInternalFailureException;

	/**
	 * Returns a list of reviews for a given document.
	 * 
	 * @param documentId the document identifier.
	 * @param includeToBeModerated a flag indicating whenever "unmoderated" documents have to be included in result.
	 * @param maxDocuments the max number of documents to return.
	 * @return a list of reviews for a given document.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	List<Review> getDocumentReviews(String documentId, boolean includeToBeModerated, int maxDocuments) throws SystemInternalFailureException;
	
	/**
	 * Returns a map indicating whenever the given ids are in the user bibliography or not.
	 * 
	 * @param ids the list of item identifiers.
	 * @param accountId the user identifier.
	 * @return a map indicating whenever the given ids are in the user bibliography or not.
	 * @throws SystemInternalFailureException in case of system internal failure.
	 */
	Map<String, Boolean> getBibliographyStatus(List<String> ids, int accountId) throws SystemInternalFailureException;

	/**
	 * Adds a new document to the user bibliography.
	 * 
	 * @param accountId the account identifier.
	 * @param uri the document identifier.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	void addToBibliography(int accountId, String uri) throws SystemInternalFailureException;

	/**
	 * Adds a new user tag to a document.
	 * 
	 * @param accountId the account identifier.
	 * @param uri the document identifier.
	 * @param label the tag label.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	void addNewTag(int accountId, String uri, String label) throws SystemInternalFailureException;

	/**
	 * Adds a new user review to a document.
	 * 
	 * @param accountId the account identifier.
	 * @param uri the document identifier.
	 * @param reviewText the review text.
	 * @throws SystemInternalFailureException in case of system failure.
	 */	
	void addNewReview(int accountId, String uri, String reviewText)  throws SystemInternalFailureException;
	
	/**
	 * Returns the documents associated with a user wish list.
	 * 
	 * @param wishListId the wish list id.
	 * @return the documents associated with a user wish list.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	List<String> getWishListDocuments(long wishListId) throws SystemInternalFailureException;

	/**
	 * Removes a set of items from a wish list.
	 * 
	 * @param wishListId the wish list identifier.
	 * @param selectedItems the set of selected items.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	void removeFromWishList(long wishListId, Set<String> selectedItems) throws SystemInternalFailureException;

	/**
	 * Removes a set of items from a tag.
	 * 
	 * @param tagId the tag id.
	 * @param selectedItems the set of items.
	 * @param accountIdthe account id.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	void removeFromTag(long tagId, Set<String> selectedItems, int accountId) throws SystemInternalFailureException;

	List<Tag> getUserDocumentTags(String uri, int accountId) throws SystemInternalFailureException;

	List<Tag> getDocumentTags(String uri)  throws SystemInternalFailureException;

	boolean isInBibliography(String uri, int accountId) throws SystemInternalFailureException;

	void removeFromBibliography(int id, String documentUri) throws SystemInternalFailureException;

	/**
	 * Creates a new wish list for the given user and immediately associates a document.
	 * 
	 * @param accountId the account identifier.
	 * @param documentURI the document URI.
	 * @param wishlistName the wishlist name.
	 */
	void addNewWishList(int accountId, String documentURI, String wishlistName) throws SystemInternalFailureException;

	Map<Long, String> getDocumentWishLists(String uri, int accountId)  throws SystemInternalFailureException;

	void add2WishList(long parseLong, String uri) throws SystemInternalFailureException;

	void removeFromWishList(long wishListId, String uri)  throws SystemInternalFailureException;

	void removeFromTag(int accountId, long tagId, String uri)  throws SystemInternalFailureException;

	Map<String, ItemCommunityData> getMultipleItemsCommunityData(int accountId, String[] pageIds, boolean loadOnlyUserCommunityData) throws SystemInternalFailureException;
	
	void removeReview(long reviewId)  throws SystemInternalFailureException;
	
	void removeWishList(long wishListId) throws SystemInternalFailureException;
	
	Map<Integer, List<Map<String,Object>>> getMyDesiderata(final int accountId, DataSource dataSource) throws SystemInternalFailureException;
}
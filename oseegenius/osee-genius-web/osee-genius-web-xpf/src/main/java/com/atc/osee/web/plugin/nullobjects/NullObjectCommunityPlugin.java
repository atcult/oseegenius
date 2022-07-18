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
package com.atc.osee.web.plugin.nullobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.community.ItemCommunityData;
import com.atc.osee.web.model.community.Review;
import com.atc.osee.web.model.community.Tag;
import com.atc.osee.web.plugin.CommunityPlugin;

/**
 * NullObject implementation for Community plugin.
 * 
 * @author agazzarini
 * @author ggazzarini
 * @since 1.0
 */
public class NullObjectCommunityPlugin implements CommunityPlugin
{
	private static final List<Tag> EMPTY_TAG_LIST = new ArrayList<Tag>();
	private static final List<Review> EMPTY_REVIEW_ARRAYLIST = new ArrayList<Review>();
	private static final List<String> EMPTY_BIBLIOGRAPHY = new ArrayList<String>();
	
	private static final Map<String, List<Review>> EMPTY_REVIEW_LIST = new HashMap<String, List<Review>>();
	private static final Map<Long, String> EMPTY_WISH_LIST = new HashMap<Long, String>();
	
	@Override
	public void init(final DataSource datasource) 
	{
		// Nothing to be done here...
	}

	@Override
	public List<Tag> getTags(final int accountId) throws SystemInternalFailureException 
	{
		return EMPTY_TAG_LIST;
	}

	@Override
	public int countReviews(final int accountId)
	{
		return 0;
	}

	@Override
	public int countTags(final int accountId) 
	{		
		return 0;
	}

	@Override
	public int getBibliographySize(final int accountId) 
	{
		return 0;
	}

	@Override
	public Map<String, List<Review>> getReviews(final int accountId) throws SystemInternalFailureException 
	{
		return EMPTY_REVIEW_LIST;
	}

	@Override
	public List<String> getBibliography(final int accountId)
	{
		return EMPTY_BIBLIOGRAPHY;
	}

	@Override
	public void removeFromBibliography(final int accountId, final Set<String> ids) 
	{
		// Nothing to be done here...
	}

	@Override
	public List<String> getTaggedDocuments(final int accountId, final long tagId)
	{
		return EMPTY_BIBLIOGRAPHY;
	}

	@Override
	public void removeTag(final int accountId, final long tagId)
	{
		// Nothing to be done here...
	}

	@Override
	public Map<String, Boolean> getBibliographyStatus(final List<String> ids, final int id)
	{
		return null;
	}

	@Override
	public void addToBibliography(final int id, final String documentId)
	{
		// Nothing to be done here...
	}

	@Override
	public void addNewTag(final int id, final String uri, final String label)
	{
		// Nothing to be done here...
	}

	@Override
	public List<Review> getDocumentReviews(
			final String documentId,
			final boolean includeToBeModerated, 
			final int maxDocuments)
	{		
		return null;
	}

	@Override
	public void addNewReview(final int id, final String uri, final String reviewText)
	{
		// Nothing to be done here...
	}

	@Override
	public Map<Long, String> getWishLists(final int accountId) throws SystemInternalFailureException 
	{
		return EMPTY_WISH_LIST;
	}

	@Override
	public List<String> getWishListDocuments(final long wishListId) throws SystemInternalFailureException 
	{
		return EMPTY_BIBLIOGRAPHY;
	}

	@Override
	public void removeFromWishList(final long wishListId, final Set<String> selectedItems)
	{
		// Nothing
	}

	@Override
	public void removeFromTag(final long tagId, final Set<String> selectedItems, final int accountId)
	{
		// Nothing
	}

	@Override
	public List<Review> getUserDocumentReviews(
			final String documentURI, 
			final boolean includeToBeModerated, 
			final int limit, 
			final int accountId) 
	{	
		return EMPTY_REVIEW_ARRAYLIST;
	}

	@Override
	public List<Tag> getUserDocumentTags(final String uri, final int accountId)
	{
		return EMPTY_TAG_LIST;
	}

	@Override
	public List<Tag> getDocumentTags(final String uri)
	{
		return EMPTY_TAG_LIST;
	}

	@Override
	public boolean isInBibliography(final String uri, final int accountId)
	{
		return false;
	}

	@Override
	public void removeFromBibliography(final int id, final String documentUri) 
	{
		// Nothing
	}

	@Override
	public void addNewWishList(final int accountId, final String documentURI, final String wishlistName)
	{
		// Nothing
	}

	@Override
	public Map<Long, String> getDocumentWishLists(final String uri, final int accountId)
	{
		return EMPTY_WISH_LIST;
	}

	@Override
	public void add2WishList(final long parseLong, final String uri) 
	{
		// Nothing
	}

	@Override
	public void removeFromWishList(final long wishListId, final String uri) 
	{
		// Nothing
	}

	@Override
	public void removeFromTag(final int accountId, final long tagId, String uri)
	{
		// Nothing
	}

	@Override
	public Map<String, ItemCommunityData> getMultipleItemsCommunityData(
			final int accountId,
			final String[] pageIds, 
			final boolean loadOnlyUserCommunityData) 
	{
		return null;
	}

	@Override
	public void init(ValueParser configuration) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeReview(final long reviewId)
	{
		// Nothing
	}
	
	@Override
	public void removeWishList(final long wishListId)
	{
		// Nothing
	}

	@Override
	public Map<Integer, List<Map<String, Object>>> getMyDesiderata(int accountId, DataSource ds)
			throws SystemInternalFailureException {
		// TODO Auto-generated method stub
		return null;
	}
}
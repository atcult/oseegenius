package com.atc.osee.web.model.community;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A value object encapsulating community data associated with a given item.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ItemCommunityData implements Serializable 
{
	private static final long serialVersionUID = 3915804776686396838L;
	
	private final List<Tag> tags = new ArrayList<Tag>();
	private final Map<Long, String> wishLists = new HashMap<Long, String>();
	private int reviewsCount;
	private boolean inBibliography;

	private Map<Long, String> userWishLists;

	public Map<Long, String> getUserWishLists() 
	{
		return userWishLists;
	}

	public void setUserWishLists(Map<Long, String> userWishLists) 
	{
		this.userWishLists = userWishLists;
	}

	public void setReviewsCount(final int reviewsCount)
	{
		this.reviewsCount = reviewsCount;
	}
	
	/**
	 * Returns the tags of the item.
	 * 
	 * @return the tags of the item.
	 */
	public List<Tag> getTags() 
	{
		return tags;
	}

	/**
	 * Returns the reviews of the item.
	 * 
	 * @return the reviews of the item.
	 */
	public int getReviewsCount() 
	{
		return reviewsCount;
	}

	/**
	 * Returns the wishlist (map of id / name) of the item.
	 * 
	 * @return the wishlist (map of id / name) of the item.
	 */
	public Map<Long, String> getWishLists() 
	{
		return wishLists;
	}

	/**
	 * Returns true if the item is in bibliography.
	 * 
	 * @return true if the item is in bibliography.
	 */
	public boolean isInBibliography() 
	{
		return inBibliography;
	}

	public void addTag(final Tag tag) 
	{
		tags.add(tag);
	}

	public void addWishlist(long id, String name) 
	{
		wishLists.put(id,name);
		userWishLists.remove(id);
	}

	public void setInBibliography(boolean inBibliography) {
		this.inBibliography = inBibliography;
	}
	
	
}
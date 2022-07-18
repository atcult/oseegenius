package com.atc.osee.web.plugin.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.logic.integration.IDMaker;
import com.atc.osee.web.model.community.ItemCommunityData;
import com.atc.osee.web.model.community.Review;
import com.atc.osee.web.model.community.Tag;
import com.atc.osee.web.plugin.CommunityPlugin;

/**
 * OseeGenius -W- default (RDBMS based) community plugin.
 * 
 * @author agazzarini
 * @since 1.0
 */
public final class CommunityPluginImpl implements CommunityPlugin 
{
	private final static Logger LOGGER = LoggerFactory.getLogger(CommunityPlugin.class);

	private final static String SELECT_USER_DOCUMENT_TAGS = "SELECT TAG.ID, TAG.LABEL FROM TAG, TAG_ASSOCIATION WHERE USER_ID=? AND ITEM_ID=? AND TAG_ID = TAG.ID";
	private final static String SELECT_DOCUMENT_TAGS = "SELECT TAG.ID, TAG.LABEL FROM TAG, TAG_ASSOCIATION WHERE ITEM_ID=? AND TAG_ID = TAG.ID";

	private final static String SELECT_USER_TAGS = "SELECT DISTINCT(TAG.ID), TAG.LABEL FROM TAG, TAG_ASSOCIATION WHERE USER_ID=? AND TAG_ID = TAG.ID";
	private final static String COUNT_USER_TAGS = "SELECT COUNT(*) FROM TAG_ASSOCIATION WHERE USER_ID=?";
	private final static String BIBLIOGRAPHY_SIZE = "SELECT COUNT(ITEM_ID) FROM BIBLIOGRAPHY WHERE USER_ID=?";
	
	private final static String COUNT_USER_REVIEWS = "SELECT COUNT(ID) FROM REVIEW WHERE USER_ID=?";
	private final static String SELECT_USER_REVIEWS = "SELECT * FROM REVIEW WHERE USER_ID=? ORDER BY ITEM_ID";

	private final static String COUNT_DOCUMENTS_REVIEWS = "SELECT ITEM_ID, COUNT(ID) FROM REVIEW WHERE ITEM_ID IN ";
	private final static String COUNT_USER_DOCUMENTS_REVIEWS = "SELECT ITEM_ID,COUNT(ID) FROM REVIEW WHERE USER_ID=? AND ITEM_ID IN ";
	private final static String GROUP_BY_ITEM_ID = " GROUP BY ITEM_ID";
	
	private final static String SELECT_DOCUMENT_REVIEWS = "SELECT * FROM REVIEW WHERE ITEM_ID=? ORDER BY CREATION_DATE DESC";
	private final static String SELECT_USER_DOCUMENT_REVIEWS = "SELECT * FROM REVIEW WHERE ITEM_ID=? AND USER_ID=? ORDER BY CREATION_DATE DESC";

	private final static String SELECT_ALL_DOCUMENT_REVIEWS = "SELECT * FROM REVIEW WHERE ITEM_ID=? ORDER BY CREATION_DATE DESC";
	private final static String INSERT_NEW_REVIEW = "INSERT INTO REVIEW (ID, REVIEW, ITEM_ID, USER_ID, TBR,CREATION_DATE) VALUES (?,?,?,?,'1',?)";
	
	private final static String SELECT_BIBLIOGRAPHY = "SELECT ITEM_ID FROM BIBLIOGRAPHY WHERE USER_ID=?";
	private final static String SELECT_BIBLIOGRAPHY_ITEM = "SELECT COUNT(ID) FROM BIBLIOGRAPHY WHERE USER_ID=? AND ITEM_ID=?";

	private final static String SELECT_BIBLIOGRAPHY_DOCUMENTS_STATUS = "SELECT ITEM_ID FROM BIBLIOGRAPHY WHERE USER_ID=? AND ITEM_ID IN ";
	
	private final static String REMOVE_FROM_BIBLIOGRAPHY = "DELETE FROM BIBLIOGRAPHY WHERE USER_ID=? AND ITEM_ID IN ";
	private final static String REMOVE_TAG = "DELETE FROM TAG_ASSOCIATION WHERE USER_ID=? AND TAG_ID=?";
	private final static String SELECT_DOCUMENTS_BY_USER_TAG = "SELECT ITEM_ID FROM TAG_ASSOCIATION WHERE USER_ID=? AND TAG_ID=?";
	
	private final static String SELECT_USER_TAG_BY_DOCUMENT_1 = "SELECT ITEM_ID, TAG.ID, TAG.LABEL FROM TAG,TAG_ASSOCIATION WHERE USER_ID=? AND ITEM_ID IN  ";
	private final static String SELECT_USER_TAG_BY_DOCUMENT_2 = " AND TAG.ID=TAG_ASSOCIATION.TAG_ID";
	
	private final static String INSERT_BIBLIOGRAPHY_ENTRY = "INSERT INTO BIBLIOGRAPHY (ID, USER_ID, ITEM_ID) VALUES (?,?,?)";

	private final static String SELECT_TAG_LABEL = "SELECT ID FROM TAG WHERE LABEL=?";
//	private final static String SELECT_TAG_ASSOCIATION ="SELECT TAG_ID FROM TAG_ASSOCIATION WHERE TAG_ID=?";
	private final static String INSERT_NEW_TAG_LABEL = "INSERT INTO TAG (ID, LABEL) VALUES (?,?)";
	private final static String INSERT_NEW_TAG_ASSOCIATION = "INSERT INTO TAG_ASSOCIATION (TAG_ID, USER_ID, ITEM_ID) VALUES (?,?,?)";
	
	private final static String REMOVE_FROM_TAG = "DELETE FROM TAG_ASSOCIATION WHERE TAG_ID=? AND USER_ID=? AND ITEM_ID IN ";
	private final static String REMOVE_DOCUMENT_FROM_TAG = "DELETE FROM TAG_ASSOCIATION WHERE TAG_ID=? AND USER_ID=? AND ITEM_ID=?";
	
	private final static String SELECT_USER_WISH_LIST = "SELECT ID,NAME FROM WISH_LIST WHERE USER_ID=?";
	private final static String SELECT_USER_DOCUMENT_WISH_LIST = "SELECT ID,NAME FROM WISH_LIST,WISH_LIST_DOC WHERE USER_ID=? AND ITEM_ID=? AND WISH_LIST.ID = WISH_LIST_ID";
	
	private final static String SELECT_USER_DOCUMENTS_WISH_LIST = "SELECT ITEM_ID,ID,NAME FROM WISH_LIST,WISH_LIST_DOC WHERE USER_ID=? AND WISH_LIST.ID = WISH_LIST_ID AND ITEM_ID IN ";
	
	private final static String SELECT_DOCUMENTS_BY_WISHLIST = "SELECT ITEM_ID FROM WISH_LIST_DOC WHERE WISH_LIST_ID=?";
	private final static String REMOVE_FROM_WISHLIST = "DELETE FROM WISH_LIST_DOC WHERE WISH_LIST_ID=? AND ITEM_ID IN ";
	private final static String REMOVE_DOCUMENT_FROM_WISHLIST = "DELETE FROM WISH_LIST_DOC WHERE WISH_LIST_ID=? AND ITEM_ID=?";

	private final static String INSERT_NEW_WISH_LIST = "INSERT INTO WISH_LIST (ID, NAME, CREATION_DATE, USER_ID) VALUES (?,?,?,?)";
	private final static String ADD_TO_WISH_LIST = "INSERT INTO WISH_LIST_DOC (ITEM_ID, WISH_LIST_ID) VALUES (?,?)";
	
	private final static String REMOVE_REVIEW = "DELETE FROM REVIEW WHERE ID=?";
	private final static String REMOVE_ALL_DOC_FROM_WISH_LIST = "DELETE FROM WISH_LIST_DOC WHERE WISH_LIST_ID=?";
	private final static String REMOVE_WISH_LIST = "DELETE FROM WISH_LIST WHERE ID=?";
	
	private DataSource datasource;
	
	@Override
	public void init(final ValueParser configuration) 
	{
		// Nothing
	}
	
	@Override
	public void init(final DataSource datasource) 
	{
		this.datasource = datasource;
	}

	@Override
	public List<Tag> getTags(final int accountId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_USER_TAGS);
			statement.setInt(1, accountId);
			rs = statement.executeQuery();
			
			List<Tag> result = new ArrayList<Tag>();
			while (rs.next())
			{
				result.add(new Tag(rs.getLong("ID"), rs.getString("LABEL")));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user tag data.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public int countTags(final int accountId) throws SystemInternalFailureException 
	{
		return count(accountId,COUNT_USER_TAGS);
	}

	@Override
	public int countReviews(final int accountId) throws SystemInternalFailureException 
	{
		return count(accountId, COUNT_USER_REVIEWS);
	}

	@Override
	public int getBibliographySize(final int accountId) throws SystemInternalFailureException 
	{
		return count(accountId, BIBLIOGRAPHY_SIZE);
	}
	
	/**
	 * Internal method that executes the given (COUNT) sql command.
	 * 
	 * @param accountId the account identifier.
	 * @param sql the SQL command.
	 * @return the result of the COUNT command.
	 * @throws SystemInternalFailureException in case of database failure.
	 */
	private int count(final int accountId, final String sql) throws SystemInternalFailureException 
	{	
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, accountId);
			rs = statement.executeQuery();
			
			return (rs.next()) ? rs.getInt(1) : 0;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while counting user community data.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public Map<String, List<Review>> getReviews(final int accountId) throws SystemInternalFailureException 
	{
		Map<String, List<Review>> result = new HashMap<String, List<Review>>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_USER_REVIEWS);
			statement.setInt(1, accountId);
			rs = statement.executeQuery();
			
			String currentItemId = null;
			List<Review> reviews = null;
			
			while (rs.next())
			{
				String itemId = rs.getString("ITEM_ID");
				if (currentItemId == null)
				{
					currentItemId = itemId;
					reviews = new ArrayList<Review>();
				} else if (!currentItemId.equals(itemId))
				{
					result.put(currentItemId, reviews);
					reviews = new ArrayList<Review>();
					currentItemId = itemId;
				} 
				//reviews.add(rs.getString("REVIEW"));
				
				reviews.add(new Review(
						rs.getLong("ID"),
						rs.getString("REVIEW"),
						new Date(rs.getTimestamp("CREATION_DATE").getTime()),
						rs.getBoolean("TBR")));
				
			}
			
			if (currentItemId != null)
			{
				result.put(currentItemId, reviews);
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while counting user community data.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public List<String> getBibliography(final int accountId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_BIBLIOGRAPHY);
			statement.setInt(1, accountId);
			rs = statement.executeQuery();
			
			List<String> result = new ArrayList<String>();
			while (rs.next())
			{
				result.add(rs.getString("ITEM_ID"));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user bibliography.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public void removeFromBibliography(final int accountId, final Set<String> ids) throws SystemInternalFailureException 
	{
		if (ids == null || ids.isEmpty())
		{
			return;
		}
		
		Connection connection = null;
		PreparedStatement statement = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(REMOVE_FROM_BIBLIOGRAPHY + buildItemsString(ids));
			statement.setInt(1, accountId);
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while removing bibliography association.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}		
	}

	@Override
	public List<String> getTaggedDocuments(final int accountId, final long tagId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_DOCUMENTS_BY_USER_TAG);
			statement.setInt(1, accountId);
			statement.setLong(2, tagId);
			rs = statement.executeQuery();
			
			List<String> result = new ArrayList<String>();
			while (rs.next())
			{
				result.add(rs.getString("ITEM_ID"));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user tagged documents.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public void removeTag(final int accountId, final long tagId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(REMOVE_TAG);
			statement.setInt(1, accountId);
			statement.setLong(2, tagId);
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while removing bibliography association.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}				
	}

	@Override
	public Map<String, Boolean> getBibliographyStatus(final List<String> ids, final int id) throws SystemInternalFailureException 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void addToBibliography(final int accountId, final String documentId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(INSERT_BIBLIOGRAPHY_ENTRY);
			statement.setLong(1, IDMaker.getID());
			statement.setInt(2, accountId);
			statement.setString(3, documentId);
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while adding a bibliography association.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}						
	}

	@Override
	public void addNewTag(final int accountId, final String uri, String label) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement selectTagStatement = null;
		PreparedStatement selectTagAssociationStatement = null;
		PreparedStatement insertTagStatement = null;
		PreparedStatement insertTagAssociationStatement = null;
		ResultSet tagAssociationRs = null;
		ResultSet rs = null;
		
		label = label.trim();
		
		try 
		{
			connection = datasource.getConnection();
			selectTagStatement = connection.prepareStatement(SELECT_TAG_LABEL);
			selectTagStatement.setString(1, label);
			
			rs = selectTagStatement.executeQuery();
			long tagId = -1;
			if (rs.next())
			{
				tagId = rs.getLong(1);
//				selectTagAssociationStatement = connection.prepareStatement(SELECT_TAG_ASSOCIATION);
//				selectTagAssociationStatement.setLong(1, tagId);
//				tagAssociationRs = selectTagAssociationStatement.executeQuery();
//				if (tagAssociationRs.next())
//				{
//					return;
//				}
			} else 
			{
				tagId = IDMaker.getID();
				insertTagStatement = connection.prepareStatement(INSERT_NEW_TAG_LABEL);
				insertTagStatement.setLong(1, tagId);
				insertTagStatement.setString(2, label);				
				insertTagStatement.executeUpdate();
			}
			
			insertTagAssociationStatement = connection.prepareStatement(INSERT_NEW_TAG_ASSOCIATION);
			insertTagAssociationStatement.setLong(1, tagId);
			insertTagAssociationStatement.setInt(2, accountId);
			insertTagAssociationStatement.setString(3, uri);
			insertTagAssociationStatement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while adding a new tag (association).", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { if (rs != null) rs.close();} catch(Exception exception){}
			try { if (tagAssociationRs != null) tagAssociationRs.close();} catch(Exception exception){}
			try { if (selectTagStatement != null) selectTagStatement.close();} catch(Exception exception){}
			try { if (selectTagAssociationStatement != null) selectTagAssociationStatement.close();} catch(Exception exception){}
			try { if (insertTagStatement != null) insertTagStatement.close();} catch(Exception exception){}
			try { if (insertTagAssociationStatement != null) insertTagAssociationStatement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}								
	}

	@Override
	public List<Review> getDocumentReviews(final String documentId, final boolean includeToBeModerated, final int maxDocuments) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(includeToBeModerated ? SELECT_ALL_DOCUMENT_REVIEWS : SELECT_DOCUMENT_REVIEWS);
			statement.setString(1, documentId);
			rs = statement.executeQuery();
			List<Review> result = new ArrayList<Review>();
			
			while (rs.next())
			{
				result.add(new Review(
						rs.getLong("ID"),
						rs.getString("REVIEW"),
						new Date(rs.getTimestamp("CREATION_DATE").getTime()),
						rs.getBoolean("TBR")));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading document reviews.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { rs.close();} catch(Exception exception){}
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}						
	}

	@Override
	public void addNewReview(final int accountId, final String uri, final String reviewText) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		try 
		{
			//  (ID, REVIEW, ITEM_ID, USER_ID, TBR,CREATION_DATE
			connection = datasource.getConnection();
			statement = connection.prepareStatement(INSERT_NEW_REVIEW);
			statement.setLong(1, IDMaker.getID());
			statement.setString(2, reviewText);
			statement.setString(3, uri);
			statement.setInt(4, accountId);			
			statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while adding a new review.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}							
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<Long, String> getWishLists(int accountId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_USER_WISH_LIST);
			statement.setInt(1, accountId);
			rs = statement.executeQuery();
			Map<Long, String> result = new TreeMap();
			
			while (rs.next())
			{
				result.put(rs.getLong("ID"), rs.getString("NAME"));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user wishlists.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { rs.close();} catch(Exception exception){}
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}			
	}

	@Override
	public List<String> getWishListDocuments(long wishListId) throws SystemInternalFailureException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_DOCUMENTS_BY_WISHLIST);
			statement.setLong(1, wishListId);
			rs = statement.executeQuery();
			
			List<String> result = new ArrayList<String>();
			while (rs.next())
			{
				result.add(rs.getString("ITEM_ID"));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user wishlist documents.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public void removeFromWishList(long wishListId, Set<String> selectedItems) throws SystemInternalFailureException 
	{
		if (selectedItems == null || selectedItems.isEmpty())
		{
			return;
		}
		
		Connection connection = null;
		PreparedStatement statement = null;

		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(REMOVE_FROM_WISHLIST + buildItemsString(selectedItems));
			statement.setLong(1, wishListId);
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while adding a new review.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}									
	}
	
	private String buildItemsString(final Collection<String> items) 
	{
		StringBuilder builder = new StringBuilder("(");
		int count = 0;
		for (String id : items)
		{
			if (count > 0)
			{
				builder.append(",");
			}
			builder.append("'").append(id).append("'");
			count++;
		}
		builder.append(")");
		return builder.toString();
	}
	
	private String buildItemsString(final String[] items) 
	{
		StringBuilder builder = new StringBuilder("(");
		int count = 0;
		for (String id : items)
		{
			if (count > 0)
			{
				builder.append(",");
			}
			builder.append("'").append(id).append("'");
			count++;
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public void removeFromTag(final long tagId, Set<String> selectedItems,final int accountId) throws SystemInternalFailureException 
	{
		if (selectedItems == null || selectedItems.isEmpty())
		{
			return;
		}
		
		Connection connection = null;
		PreparedStatement statement = null;

		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(REMOVE_FROM_TAG + buildItemsString(selectedItems));
			statement.setLong(1, tagId);
			statement.setInt(2, accountId);			
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while adding a new review.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}	
	}

	@Override
	public List<Review> getUserDocumentReviews(
			final String documentURI, 
			final boolean includeToBeModerated, 
			final int limit, 
			final int accountId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_USER_DOCUMENT_REVIEWS);
			statement.setString(1, documentURI);
			statement.setInt(2, accountId);			
			rs = statement.executeQuery();
			List<Review> result = new ArrayList<Review>();
			
			while (rs.next())
			{
				result.add(new Review(
						rs.getLong("ID"),
						rs.getString("REVIEW"),
						new Date(rs.getTimestamp("CREATION_DATE").getTime()),
						rs.getBoolean("TBR")));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading document reviews.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { rs.close();} catch(Exception exception){}
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}					
	}

	@Override
	public List<Tag> getUserDocumentTags(final String uri, final int accountId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_USER_DOCUMENT_TAGS);
			statement.setInt(1, accountId);
			statement.setString(2, uri);			
			rs = statement.executeQuery();
			
			List<Tag> result = new ArrayList<Tag>();
			while (rs.next())
			{
				result.add(new Tag(rs.getLong("ID"), rs.getString("LABEL")));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user tag data.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public List<Tag> getDocumentTags(final String uri) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_DOCUMENT_TAGS);
			statement.setString(1, uri);
			rs = statement.executeQuery();
			
			List<Tag> result = new ArrayList<Tag>();
			while (rs.next())
			{
				result.add(new Tag(rs.getLong("ID"), rs.getString("LABEL")));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user tag data.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public boolean isInBibliography(String uri, int accountId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_BIBLIOGRAPHY_ITEM);			
			statement.setInt(1, accountId);
			statement.setString(2, uri);
			rs = statement.executeQuery();
			if (rs.next())
			{
				return rs.getInt(1) != 0;
			}
			return false;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user tag data.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public void removeFromBibliography(int accountId, String documentUri) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(REMOVE_FROM_BIBLIOGRAPHY + "('" + documentUri + "')");
			statement.setInt(1, accountId);
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while removing bibliography association.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}				
	}

	@Override
	public void addNewWishList(
			final int accountId, 
			final String documentURI, 
			final String wishlistName) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement insertWishListStatement = null;
		PreparedStatement add2WishListStatement = null;
		
		long id = IDMaker.getID();
		
		try 
		{
			connection = datasource.getConnection();
			insertWishListStatement = connection.prepareStatement(INSERT_NEW_WISH_LIST);
			add2WishListStatement = connection.prepareStatement(ADD_TO_WISH_LIST);
			
			insertWishListStatement.setLong(1, id);
			insertWishListStatement.setString(2, wishlistName);
			insertWishListStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
			insertWishListStatement.setInt(4, accountId);
			
			insertWishListStatement.executeUpdate();
			
			add2WishListStatement.setString(1, documentURI);
			add2WishListStatement.setLong(2, id);
			
			add2WishListStatement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while removing bibliography association.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{			
			try 
			{
				insertWishListStatement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				add2WishListStatement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}				
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<Long, String> getDocumentWishLists(String uri, int accountId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_USER_DOCUMENT_WISH_LIST);
			statement.setInt(1, accountId);
			statement.setString(2, uri);
			
			rs = statement.executeQuery();
			Map<Long, String> result = new TreeMap();
			
			while (rs.next())
			{
				result.put(rs.getLong("ID"), rs.getString("NAME"));
			}
			return result;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user wishlists.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { rs.close();} catch(Exception exception){}
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}			
	}

	@Override
	public void add2WishList(final long wishListId, final String documentURI) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement add2WishListStatement = null;
		
		try 
		{
			connection = datasource.getConnection();
			add2WishListStatement = connection.prepareStatement(ADD_TO_WISH_LIST);
			add2WishListStatement.setString(1, documentURI);
			add2WishListStatement.setLong(2, wishListId);
			
			add2WishListStatement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while removing bibliography association.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{			
			try 
			{
				add2WishListStatement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}						
	}

	@Override
	public void removeFromWishList(final long wishListId, final String uri) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;

		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(REMOVE_DOCUMENT_FROM_WISHLIST);
			statement.setLong(1, wishListId);
			statement.setString(2, uri);
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while adding a new review.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}			
	}

	@Override
	public void removeFromTag(int accountId, long tagId, String uri) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;

		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(REMOVE_DOCUMENT_FROM_TAG);
			statement.setLong(1, tagId);
			statement.setInt(2, accountId);			
			statement.setString(3, uri);						
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while adding a new review.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}	
	}
	
	@Override
	public void removeReview(long reviewId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;

		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(REMOVE_REVIEW);
			statement.setLong(1, reviewId);
			statement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while deleting review.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}	
	}
	
	@Override
	public void removeWishList(long wishListId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement removeAllWishListDocStatement = null;
		PreparedStatement removeWishListStatement = null;
		
		try 
		{
			connection = datasource.getConnection();
			removeAllWishListDocStatement = connection.prepareStatement(REMOVE_ALL_DOC_FROM_WISH_LIST);
			removeWishListStatement = connection.prepareStatement(REMOVE_WISH_LIST);
			
			removeAllWishListDocStatement.setLong(1, wishListId);
			removeAllWishListDocStatement.executeUpdate();
			
			removeWishListStatement.setLong(1, wishListId);
			removeWishListStatement.executeUpdate();
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while removing entire wishlist.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{			
			try 
			{
				removeAllWishListDocStatement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				removeWishListStatement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}				
	}

	@Override
	public Map<String, ItemCommunityData> getMultipleItemsCommunityData(
			final int accountId,
			final String[] pageIds, 
			final boolean loadOnlyUserCommunityData) throws SystemInternalFailureException 
	{
		Map<String, ItemCommunityData> result = new HashMap<String, ItemCommunityData>();
		
		Map<Long, String> wishlists = getWishLists(accountId);
		for (String id: pageIds)
		{
			ItemCommunityData itemCommunityData = getOrCreateItemCommunityData(id, result);
			itemCommunityData.setUserWishLists(new HashMap<Long, String>(wishlists));
		}
		
		injectBibliographyStatus(accountId, pageIds, result);
		
		injectReviewsCount(accountId, result, pageIds, loadOnlyUserCommunityData);
		injectTags(accountId, result, pageIds, loadOnlyUserCommunityData);
		injectWishlists(accountId, result, pageIds);
		return result;
	}
	
	private void injectBibliographyStatus(int accountId, String[] pageIds, Map<String, ItemCommunityData> accumulator) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_BIBLIOGRAPHY_DOCUMENTS_STATUS + buildItemsString(pageIds));
			statement.setInt(1, accountId);
			
			rs = statement.executeQuery();
			while (rs.next())
			{
				ItemCommunityData itemData = getOrCreateItemCommunityData(rs.getString("ITEM_ID"), accumulator);
				itemData.setInBibliography(true);
			}
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user wishlists.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { rs.close();} catch(Exception exception){}
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}					
	}

	private void injectWishlists(final int accountId, final Map<String, ItemCommunityData> accumulator, final String[] pageIds) throws SystemInternalFailureException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_USER_DOCUMENTS_WISH_LIST + buildItemsString(pageIds));
			statement.setInt(1, accountId);
			
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				ItemCommunityData itemData = getOrCreateItemCommunityData(rs.getString("ITEM_ID"), accumulator);
				itemData.addWishlist(rs.getLong(2), rs.getString(3));
			}
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading review count for documents.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try {rs.close();} catch (Exception exception){}
			try {statement.close();} catch (Exception exception){}
			try {connection.close();} catch (Exception exception){}
		}		
	}
	
	private void injectTags(
			final int accountId, 
			final Map<String, ItemCommunityData> accumulator, 
			final String[] pageIds, 
			final boolean onlyUserTags) throws SystemInternalFailureException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_USER_TAG_BY_DOCUMENT_1 + buildItemsString(pageIds) + SELECT_USER_TAG_BY_DOCUMENT_2);
			statement.setInt(1, accountId);
			
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				ItemCommunityData itemData = getOrCreateItemCommunityData(rs.getString("ITEM_ID"), accumulator);
				itemData.addTag(new Tag(rs.getLong(2), rs.getString(3)));
			}
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading review count for documents.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try {rs.close();} catch (Exception exception){}
			try {statement.close();} catch (Exception exception){}
			try {connection.close();} catch (Exception exception){}
		}
	}
	
	private void injectReviewsCount(final int accountId, final Map<String, ItemCommunityData> accumulator, final String[] pageIds, boolean onlyUserReviews) throws SystemInternalFailureException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(
					onlyUserReviews
						? COUNT_USER_DOCUMENTS_REVIEWS + buildItemsString(pageIds) + GROUP_BY_ITEM_ID
						: COUNT_DOCUMENTS_REVIEWS + buildItemsString(pageIds) + GROUP_BY_ITEM_ID);
			
			if (onlyUserReviews)
			{
				statement.setInt(1, accountId);
			}
			
			rs = statement.executeQuery();
			while (rs.next())
			{
				ItemCommunityData itemData = getOrCreateItemCommunityData(rs.getString("ITEM_ID"), accumulator);
				itemData.setReviewsCount(rs.getInt(2));
			}
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading user wishlists.", exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try { rs.close();} catch(Exception exception){}
			try { statement.close();} catch(Exception exception){}
			try { connection.close();} catch(Exception exception){}
		}			
	}
	
	private ItemCommunityData getOrCreateItemCommunityData(final String uri, final Map<String, ItemCommunityData> data)
	{
		ItemCommunityData itemData = data.get(uri);
		if (itemData == null)
		{
			itemData = new ItemCommunityData();
			data.put(uri, itemData);
		}
		return itemData;
	}

	@Override
	public Map<Integer, List<Map<String, Object>>> getMyDesiderata(int accountId, DataSource dataSource) throws SystemInternalFailureException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement("SELECT ID_BIBLIO,TITOLO,AUTORE,DATA_CREAZIONE,DESCRIZIONE,EDITORE,ISBN " +
					"FROM DESIDERATA.ELEMENTI,DESIDERATA.STATUS,ORG_NME " + 
					"WHERE " +
					"ID_RICHIEDENTE=? AND " +
					"DESIDERATA.ELEMENTI.ID_STATUS = DESIDERATA.STATUS.ID AND ORG_NME.ORG_NBR = ID_BIBLIO ORDER BY ORG_ENG_SRT_FORM ASC, DATA_CREAZIONE DESC");
			statement.setInt(1, accountId);
			rs = statement.executeQuery();
			
			final Map<Integer, List<Map<String, Object>>> result = new LinkedHashMap<Integer, List<Map<String, Object>>>();
			while (rs.next()) {
				int mlid = rs.getInt("ID_BIBLIO");
				final List<Map<String, Object>> librarySuggestions = getLibrarySuggestions(mlid, result);
				Map<String, Object> entry = new HashMap<String, Object>();
				entry.put("title", rs.getString("TITOLO"));
				entry.put("author", rs.getString("AUTORE"));
				entry.put("date", rs.getDate("DATA_CREAZIONE"));
				entry.put("status", rs.getString("DESCRIZIONE"));
				entry.put("publisher", rs.getString("EDITORE"));
				entry.put("isbn", rs.getString("ISBN"));
				librarySuggestions.add(entry);
			}
			return result;
		} catch (Exception exception) {
			LOGGER.error("Data access failure while loading review count for documents.", exception);
			throw new SystemInternalFailureException();
		}
	}

	private List<Map<String, Object>> getLibrarySuggestions(final int libraryId, final Map<Integer, List<Map<String, Object>>> suggestions) {
		List<Map<String, Object>> result = suggestions.get(libraryId);
		if (result == null) {
			result = new ArrayList<Map<String,Object>>();
			suggestions.put(libraryId, result);
		}
		return result;
	}
}
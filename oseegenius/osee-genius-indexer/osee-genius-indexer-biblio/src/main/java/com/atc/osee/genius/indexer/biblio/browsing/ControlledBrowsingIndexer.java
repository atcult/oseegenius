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
package com.atc.osee.genius.indexer.biblio.browsing;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.log.MessageCatalog;

/**
 * OseeGenius -I-  Biblio Controlled browsing indexer.
 * Acts as main controller for creating authority controlled indexes.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ControlledBrowsingIndexer
{	
	private static final Logger LOGGER = LoggerFactory.getLogger(ControlledBrowsingIndexer.class);
	
	private static final String TERM = "term";
	private static final String IS_PREFERRED_FORM = "isPreferredForm";
	private static final String SORT_KEY = "sortKey";
	private static final String SEE_ALSO = "seeAlso";
	private static final String SEE_INSTEAD = "seeInstead";
	private static final String NOTE = "note";

	private static final String ALL_LOGICAL_VIEWS = "ALL";
	
	private DataSource datasource;

	/**
	 * Indexes the given data only if it the "controlled" test passes.
	 * The entry will be mark as controlled only if the given AAO found a corresponding authority record.
	 * 
	 * @param fieldName the field name.
	 * @param heading the heading value.
	 * @param indexer the SOLR instance (core) that is handling the indexing phase for a given field.
	 * @param aao the authority access object implementation for this field.
	 * @param filter the heading filter.
	 * @param sortKeyStrategy the sort key strategy.
	 */
	public void index(
			final String fieldName, 
			final String headingLabel, 
			final String headingValue, 
			final SolrServer indexer, 
			final IAuthorityAccessObject aao,
			final IHeadingFilter filter,
			final ISortKeyStrategy sortKeyStrategy,
			final Set<String> logicalViews)
	{
		try
		{
			final AuthorityRecord record = aao.getAuthorityRecord(headingValue, filter);
			updateBrowsingIndexWithPreferredForm(fieldName, headingLabel, headingValue, record, indexer, filter, sortKeyStrategy, logicalViews);
			
			for (String alternateLabel : record.getSeenFromTerms())
			{
				updateBrowsingIndexWithNonPreferredForm(
						aao,
						fieldName,
						headingLabel,
						indexer,
						alternateLabel,
						filter,
						sortKeyStrategy, 
						logicalViews);
			}
		} catch (Exception exception)
		{
			LOGGER.error(
					MessageCatalog._000017_CONTROLLED_BROWSING_IO_FAILURE, 
					exception);
		}
	}
	
	/**
	 * Indexes the given data only if it the "controlled" test passes.
	 * The entry will be mark as controlled only if the given AAO found a corresponding authority record.
	 * 
	 * @param fieldName the field name.
	 * @param heading the heading value.
	 * @param indexer the SOLR instance (core) that is handling the indexing phase for a given field.
	 * @param aao the authority access object implementation for this field.
	 * @param filter the heading filter.
	 * @param sortKeyStrategy the sort key strategy.
	 */
	public final void index(
			final String fieldName, 
			final String heading, 
			final SolrServer indexer, 
			final IAuthorityAccessObject aao,
			final IHeadingFilter filter,
			final ISortKeyStrategy sortKeyStrategy,
			final Set<String> logicalViews)
	{
		try
		{
			final AuthorityRecord record = aao.getAuthorityRecord(heading, filter);
			updateBrowsingIndexWithPreferredForm(
					fieldName,
					heading, 
					heading,
					record, 
					indexer, 
					filter, 
					sortKeyStrategy, 
					logicalViews);
			
			for (String alternateLabel : record.getSeenFromTerms())
			{
				updateBrowsingIndexWithNonPreferredForm(
						aao,
						fieldName,
						heading,
						indexer,
						alternateLabel,
						filter,
						sortKeyStrategy,
						logicalViews);
			}
		} catch (Exception exception)
		{
			LOGGER.error(
					MessageCatalog._000017_CONTROLLED_BROWSING_IO_FAILURE, 
					exception);
		}
	}
	
	/**
	 * Updates the lucene index with a preferred term.
	 * 
	 * @param record the authority record.
	 * @param indexer the Solr indexer.
	 * @param filter the heading filter.
	 * @throws SolrServerException in case the indexer fails while adding the new document.
	 * @throws IOException in case of I/O failure.
	 */
	private void updateBrowsingIndexWithPreferredForm(
			final String fieldName,
			final String headingLabel,
			final String headingValue,
			final AuthorityRecord record, 
			final SolrServer indexer,
			final IHeadingFilter filter,
			final ISortKeyStrategy sortKeyStrategy,
			final Set<String> logicalViews) throws IOException, SolrServerException, SQLException
	{
		final SolrInputDocument document = new SolrInputDocument();
		
		String preferredLabel = headingLabel;
		document.addField(
				TERM, 
				(headingLabel != null && headingLabel.trim().length() != 0) ? headingLabel : preferredLabel);
		document.addField(IS_PREFERRED_FORM, true);
				
		final String sortKey = sortKeyStrategy.sortKey( (headingValue != null ? headingValue : preferredLabel), filter).trim();
		
		document.addField(SORT_KEY, sortKey);
		
		if (datasource != null)
		{
			if (logicalViews == null)
			{
				insertOrUpdateSortKey(fieldName, sortKey);
			} else 
			{
				insertOrUpdateSortKey(fieldName, sortKey, logicalViews);			
			}
		}
		
		final Set<String> seeAlso = record.getSeeAlsoTerms();
		if (seeAlso != null)
		{
			document.setField(SEE_ALSO, seeAlso);
		}
		
		final Set<String> seenFrom = record.getSeenFromTerms();
		if (seenFrom != null)
		{
			document.setField("seenInsteadFrom", seenFrom);
		}
		
		final Set<String> seenAlsoFrom = record.getSeenAlsoFromTerms();
		if (seenAlsoFrom != null)
		{
			document.setField("seenAlsoFrom", seenAlsoFrom);
		}
		
		final Set<String> sameAs = record.getEquivalentTerms();
		if (sameAs != null)
		{
			document.setField("sameAs", sameAs);
		}
		
		final String note = record.getNote();
		if (note != null)
		{
			document.addField(NOTE, record.getNote());
		}
		indexer.add(document);
	}
	
	/**
	 * Updates the index adding (or replacing) a non preferred form.
	 * 
	 * @param preferredLabel the preferredLabel for "seeInstead" relationship.
	 * @param writer the lucene index writer.
	 * @param alternateLabel the non preferred term.
	 * @param filter the heading filter.
	 * @throws SolrServerException in case the indexer fails while adding the new document.
	 * @throws IOException in case of I/O failure.
	 * @throws SQLException in case of database fail
	 */
	private void updateBrowsingIndexWithNonPreferredForm(
			final IAuthorityAccessObject aao,
			final String fieldName,
			final String preferredLabel, 
			final SolrServer indexer,
			final String alternateLabel,
			final IHeadingFilter filter,
			final ISortKeyStrategy sortKeyStrategy,
			final Set<String> logicalViews) throws IOException, SolrServerException, SQLException
	{
		final String sortKey = sortKeyStrategy.sortKey(alternateLabel, filter);
		final SolrInputDocument document = new SolrInputDocument();
		document.addField(SORT_KEY, sortKey);		
		document.addField(TERM, alternateLabel);
		
		AuthorityRecord record = aao.getAuthorityRecord(alternateLabel, filter);
		if (!record.isPreferredForm())
		{
			for (String preferredForm : record.getSeeInsteadTerms())
			{
				document.addField(SEE_INSTEAD, preferredForm);
			}
		} else
		{
			document.addField(SEE_INSTEAD, preferredLabel);
		}
		
		if (datasource != null)
		{
			if (logicalViews == null)
			{
				insertOrUpdateSortKey(fieldName, sortKey);
			} else 
			{
				insertOrUpdateSortKey(fieldName, sortKey, logicalViews);			
			}
		}
		
		indexer.add(document);
	}

	public final void setDatasource(final DataSource datasource) 
	{
		this.datasource = datasource;
	}		
	
	private final void insertOrUpdateSortKey(final String tableName, final String sortKey) throws SQLException
	{
		Connection connection = null;
		PreparedStatement insertStatement = null;
		
		try 
		{
			connection = datasource.getConnection();			
			insertStatement = connection.prepareStatement("INSERT INTO " +  tableName + " VALUES (?, NULL)");			
			insertStatement.setString(1, sortKey);
			insertStatement.executeUpdate();
		} catch (Exception exception) 
		{
			// IGNORE
		} finally
		{
			try { if (insertStatement != null) insertStatement.close();} catch (Exception exception) {}
			try { if (connection != null) connection.close();} catch (Exception exception) {}
		}
	}
	
	private final void insertOrUpdateSortKey(final String tableName, final String sortKey, final Set<String> logicalViews) throws SQLException
	{
		Connection connection = null;
		PreparedStatement selectStatement = null;
		PreparedStatement updateStatement = null;
		PreparedStatement insertStatement = null;
		
		ResultSet rs = null;
				
		try 
		{
			connection = datasource.getConnection();			
			selectStatement = connection.prepareStatement("SELECT LOGICAL_VIEW FROM " + tableName + " WHERE SORT_KEY=?");
			updateStatement = connection.prepareStatement("UPDATE " + tableName + " SET LOGICAL_VIEW=? WHERE SORT_KEY=?");
			selectStatement.setString(1, sortKey);
			rs = selectStatement.executeQuery();
			if (rs.next())
			{
				String logicalView = rs.getString(1);
				
				// 1) se il record esiste ed è già associato ad una lv
				if (logicalView != null && logicalView.trim().length() != 0)
				{				
					// Se la vista logica computata è una sola
					if (logicalViews.size() == 1)
					{
						// 1.1) se lv è diversa inserisci ALL
						String incomingLogicalView = logicalViews.iterator().next();
						if (!logicalView.equals(incomingLogicalView))
						{
							updateStatement.setString(1, ALL_LOGICAL_VIEWS);
							updateStatement.setString(2, sortKey);
							updateStatement.executeUpdate();
						}
						// 1.2) se la lv è identica a quella in ingresso non fare niente.
					// Se le viste logiche computate sono + di 1 inserisci ALL
					} else 
					{
						updateStatement.setString(1, ALL_LOGICAL_VIEWS);
						updateStatement.setString(2, sortKey);
						updateStatement.executeUpdate();						
					}
				// 2) il record esiste e non è associato ad una lv	
				} else 
				{
					// Se la lv computata è 1 allora inseriscila 
					if (logicalViews.size() == 1)
					{
						String incomingLogicalView = logicalViews.iterator().next();
						
						// Se la lv computata non è nulla allora inseriscila
						if (incomingLogicalView != null && incomingLogicalView.trim().length() != 0)
						{
							updateStatement.setString(1, incomingLogicalView);
							updateStatement.setString(2, sortKey);
							updateStatement.executeUpdate();
						}
						// Altrimenti niente
					// Se sono più di 1 allora insrisci ALL
					} else 
					{
						updateStatement.setString(1, ALL_LOGICAL_VIEWS);
						updateStatement.setString(2, sortKey);
						updateStatement.executeUpdate();						
					}
				}
			} else
			{
				insertStatement = connection.prepareStatement("INSERT INTO " +  tableName + " VALUES (?,?)");			
				insertStatement.setString(1, sortKey);
				
				if (logicalViews.size() == 1)
				{
					insertStatement.setString(2, logicalViews.iterator().next());
				} else 
				{
					insertStatement.setNull(2, Types.VARCHAR);					
				}
				insertStatement.executeUpdate();				
			}
		} catch (Exception exception) 
		{
			
		} finally
		{
			try { if (insertStatement != null) insertStatement.close();} catch (Exception exception) {}
			try { if (updateStatement != null) updateStatement.close();} catch (Exception exception) {}
			try { if (selectStatement != null) selectStatement.close();} catch (Exception exception) {}
			try { if (connection != null) connection.close();} catch (Exception exception) {}
		}
	}
}
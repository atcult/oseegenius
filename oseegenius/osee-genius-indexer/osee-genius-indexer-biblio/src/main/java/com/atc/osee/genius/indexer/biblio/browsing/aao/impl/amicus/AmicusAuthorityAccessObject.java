package com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.Decorator;
import com.atc.osee.genius.indexer.biblio.browsing.AuthorityRecord;
import com.atc.osee.genius.indexer.biblio.browsing.IAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;
import com.atc.osee.genius.indexer.biblio.browsing.ISortKeyStrategy;
import com.atc.osee.genius.indexer.biblio.browsing.InitialisationException;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.DummyAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.keys.impl.AmicusSortKeyStrategy;
import com.atc.osee.genius.indexer.biblio.handlers.RemoveTrailingPunctuationTagHandler;

/**
 * Authority Access Object implementation for AMICUS LMS.
 * A very important different with others AAO is that this access object ALWAYS
 * return an AuthorityRecord, even if the given heading is not "controlled" (and in this case
 * the result is the same of {@link DummyAuthorityAccessObject}.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class AmicusAuthorityAccessObject implements IAuthorityAccessObject 
{
	private final static Logger LOGGER = LoggerFactory.getLogger(AmicusAuthorityAccessObject.class);
	private final static Decorator REMOVE_TRAILING_PUNCTUATION = new RemoveTrailingPunctuationTagHandler();
	
	private ISortKeyStrategy sortKeyFactory = new AmicusSortKeyStrategy();
	
	protected DataSource datasource;
	protected ICacheStrategy cache;
	
	protected String subjectSeparator;
	final Map<String, Object> headings = new HashMap<String, Object>();
	
	@Override
	public void shutdown() 
	{
		if (cache != null) cache.clear();
		if (headings != null) headings.clear();
		LOGGER.error("Cache for \"" + getClass().getSimpleName()+ " has been cleaned up.");
	}
	
	@Override
	public AuthorityRecord getCachedAuthorityRecord(final String key) 
	{
		return cache.getAuthorityRecord(key);
	}
	
	@Override
	public boolean isValidHeading(final String headingKey) {
		return headings.containsKey(headingKey);
	}
	
	@Override
	public void loadHeadings(final NamedList<Object> configuration) throws Exception {
		String datasourceJndiName = (String) configuration.get("datasource-jndi-name");
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try 
		{
			final Context namingContext = new InitialContext();
			datasource = (DataSource)
					((datasourceJndiName != null && datasourceJndiName.trim().length() != 0) 
						? namingContext.lookup(datasourceJndiName) 
						: namingContext.lookup("jdbc/amicus"));
			
			conn = datasource.getConnection();
			st = conn.prepareStatement(getHeadingsSql());
			
			LOGGER.info(getClass().getName() + " headings collection starts...");

			rs = st.executeQuery();
			int count = 0;
			while (rs.next()) {
				headings.put(rs.getString(1), null);
				count++;
			}
			LOGGER.info(getClass().getName() + " " + count + " headings collected.");
		} catch (final Exception exception) {
			LOGGER.error("An exception has been caught during headings collection. As consequence of that no old headings check will be executed.", exception);
			throw exception;
		} finally {
			if (rs != null)  { try { rs.close(); } catch (Exception ignore) {} }
			if (st != null)  { try { st.close(); } catch (Exception ignore) {} }
			if (conn != null)  { try { conn.close(); } catch (Exception ignore) {} }			
		}
		
		LOGGER.info(getClass().getName() + " found " + headings.size() + " headings.");
	}
	
	protected abstract String getHeadingsSql();
	
	@Override
	public AuthorityRecord getAuthorityRecord(final String headingValue, IHeadingFilter filter) {
		try {	
			String amicusSortForm = createAmicusSortForm(headingValue, filter);		
			AuthorityRecord record = cache.getAuthorityRecord(amicusSortForm);
			return (record != null) ? record : ICacheStrategy.UNCONTROLLED_AUTHORITY_RECORD; 
		} catch(IllegalArgumentException exception) {
			LOGGER.error("Unable to create AMICUS SORT FORM for heading \"" + headingValue + "\"", exception);
			
			return ICacheStrategy.UNCONTROLLED_AUTHORITY_RECORD; 
		} catch (Exception exception)
		{
			LOGGER.error("Unable to control heading \"" + headingValue + "\"", exception);
			return ICacheStrategy.UNCONTROLLED_AUTHORITY_RECORD; 
		} 
	}
	
	/**
	 * AAO initialization.
	 * This access object requires the following configuration parameter:
	 * 
	 * - datasource.jndi.name as the name suggests, the JNDI name of the AMICUS datasource (defaults to jdbc/amicus)
	 */
	@Override
	public void init(final SolrCore core, final NamedList<Object> configuration) throws InitialisationException 
	{
		if (cache != null) return;
		cache = new InMemoryCache();
		
		String datasourceJndiName = (String) configuration.get("datasource-jndi-name");
		subjectSeparator = (String) configuration.get("subject-separator");
		subjectSeparator = subjectSeparator != null && !subjectSeparator.isEmpty() ? subjectSeparator : " -- ";
		try 
		{
			Context namingContext = new InitialContext();
			
			datasource = (DataSource)
					((datasourceJndiName != null && datasourceJndiName.trim().length() != 0) 
						? namingContext.lookup(datasourceJndiName) 
						: namingContext.lookup("jdbc/amicus"));
			
			LOGGER.info("First time request-visit: loading authorities...");
			
			fillInMemoryAuthoritiesStore();
			
			LOGGER.info("Authority in memory store size for "+ getClass().getSimpleName() +  ": " + cache.size());
		} catch (Exception exception)
		{
			LOGGER.error("Unable to initialise AMICUS Authority Access Object. See below for further details.", exception);
		}
	}
	
	/**
	 * Loads all the authorities for a given index into a in-memory store.
	 * 
	 * @throws SQLException in case of database failure.
	 */
	private void fillInMemoryAuthoritiesStore() throws SQLException
	{
		AmicusAuthorityAccessObject instance = this;

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(authoritiesRefSql());
			statement.setFetchSize(1000);
			rs = statement.executeQuery();
			int currentHeadingNumber = -1;
			String currentSortForm = null;
			AuthorityRecord record = null;
			while (rs.next())
			{
				int headingNumber = rs.getInt(1);
				if (currentHeadingNumber == -1)
				{
					currentHeadingNumber = headingNumber;
					record = new AuthorityRecord();
					currentSortForm = rs.getString(2).trim();
				}
				
				if (headingNumber != currentHeadingNumber)
				{
					// Note that the Authority record cannot have a preferred label here...
					// This is important during authorities indexing.
					cache.storeAuthorityRecord(currentSortForm, record);
					currentHeadingNumber = headingNumber;
					record = new AuthorityRecord();
					currentSortForm = rs.getString(2).trim();
				} 
				int relationType = rs.getInt(3);
				switch (relationType)
				{
					case 1: // See instead
						record.addSeeInstead(clean(rs.getString(4), subjectSeparator));
						break;						
					case 2: // Seen from 
						record.addSeenFrom(clean(rs.getString(4), subjectSeparator));
						break;
					case 3: // Related
						record.addSeeAlso(clean(rs.getString(4), subjectSeparator));
						break;
					case 4:
						record.addSeenAlsoFrom(clean(rs.getString(4), subjectSeparator));
						break;
					case 5:
						record.addEquivalent(clean(rs.getString(4), subjectSeparator));
						break;		
				}
			}
			
			if (record != null)
			{
				cache.storeAuthorityRecord(currentSortForm, record);				
			}
		} catch (Exception exception) 
		{
			throw new SQLException(exception);
		} finally
		{
			try { if(rs != null) rs.close();} catch(Exception ignore){}
			try { if(statement != null) statement.close();} catch(Exception ignore){}
			try { if(connection != null) connection.close();} catch(Exception ignore){}
		}
	}
		
	protected abstract String authoritiesRefSql();
	
	/**
	 * Returns the AMICUS sort form of the given heading.
	 * 
	 * @param heading the heading value.
	 * @return the AMICUS sort form of the given heading.
	 * @throws Exception in case the heading cannot be controlled.
	 */
	public String createAmicusSortForm(final String heading, IHeadingFilter filter) throws Exception
	{
		return sortKeyFactory.sortKey(heading, filter); 
	}
	
	/**
	 * Heading labels in AMICUS are stored together with MARC subfield delimiters.
	 * This method removes those delimiters by substituting them with a space.
	 * 
	 * @param amicusHeading the AMICUS heading as we found in database.
	 * @return the heading without MARC delimiters.
	 */
	private static String clean(final String amicusHeading, String separatorReplacement)
	{
		StringBuilder builder = new StringBuilder(amicusHeading);
		for (int index = 0; index < builder.length(); index++)
		{
			char aChar = builder.charAt(index);
			if (aChar == 31)
			{
				if (index == 0)
				{
					builder.delete(index, index + 2);
					index = -1;
					continue;
				} else
				{
					builder.replace(index, index + 2, separatorReplacement);	
					continue;
				}
			} 
		}
		return (String) REMOVE_TRAILING_PUNCTUATION.decorate("", builder.toString());
	}
}
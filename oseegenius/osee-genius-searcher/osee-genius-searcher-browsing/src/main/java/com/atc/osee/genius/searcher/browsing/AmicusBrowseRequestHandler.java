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
package com.atc.osee.genius.searcher.browsing;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OseeGenius AMICUS Browse request handler.
 * A special SOLR extension that lets user browse alphabetically within indexes.
 * 
 * It is configured using standard solr procedure within solrconfig.xml:
 * 
 * <pre>
 * 		<requestHandler name="/browse" class="com.atc.osee.genius.searcher.browsing.BrowseRequestHandler">
 *			<lst name="configuration">
 *				<str name="valid-indexes">subject_browse</str>
 *			</lst> 
 * 		</requestHandler>
 *		</pre>
 *	
 * The client request should like this:
 * 
 * <pre>
 * 		http://127.0.0.1:8983/solr/title_browse/select?from=W&s=5&d=f|b&pos=2
 * </pre>
 * 
 * And finally the response 
 * 
 * <pre>
 * 		<lst name="heading">
 * 			<str name="term">Web pages</str>
 * 			<arr name="seeInstead">
 * 				<str>Web sites</str>
 * 			</arr>
 * 		</lst>
 * 		<lst name="heading">
 * 			<int name="count">2</int>
 * 			<str name="term">Web site development</str>
 * 			<str name="note">
 * 				Here are entered works on the process of designing, publishing, hosting, and programming web sites.
 * 			</str>
 * 		</lst>
 * </pre>
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AmicusBrowseRequestHandler extends RequestHandlerBase
{	
	private static final String NOT_AVAILABLE = "N.A.";
	private static final String SORT_KEY = "sortKey";
	private static final String HEADING = "heading";
	private static final String SEE_INSTEAD = "seeInstead";
	private static final String SEE_ALSO = "seeAlso";
	private static final String TERM = "term";
	private static final String COUNT = "count";
	private static final String NOTE = "note";
	private static final String IS_PREFERRED_FORM = "isPreferredForm";
	private static final String POSITION_IN_RESPONSE= "position-in-response";
	private static final String HEADING_NUMBER = "hdg-nbr";
	
	private static final String FROM_PARAMETER_NAME = "from";
	private static final String PAGE_SIZE_PARAMETER_NAME = "s";

	private static final String LOGICAL_VIEW = "logical_view";
	
	private static final String PREFERRED_POSITION_IN_RESPONSE_PARAMETER_NAME = "pos";
	private static final String BACK_ALLOWED = "back-allowed";
	
	private static final String INCOMING_START_KEY_IS_IN_SORT_FORMAT = "sk";	
	
	private static final String CATALOG_SOURCE = "catalog_source";

	private DataSource datasource;
	
	private AmicusSortFormAnalyzer analyzer = new AmicusSortFormAnalyzer(Version.LUCENE_47);
	
	private final static Map<String, String> HDG_TABLES = new HashMap<String, String>();
	private final static Map<String, String> SORT_KEY_NAMES= new HashMap<String, String>();
	private final static Map<String, String> HDG_NBR_COLUMN_NAMES= new HashMap<String, String>();
	private final static Map<String, String> STRNG_TXT_COLUMN_NAMES= new HashMap<String, String>();
	
	static 
	{
		STRNG_TXT_COLUMN_NAMES.put("title_browse", "");
		STRNG_TXT_COLUMN_NAMES.put("author_browse", "");
		STRNG_TXT_COLUMN_NAMES.put("subject_browse", "");
		STRNG_TXT_COLUMN_NAMES.put("publisher_browse", ",PUBL_HDG_STRNG_TXT_PLCE,PUBL_HDG_STRNG_TXT_NME ");
		STRNG_TXT_COLUMN_NAMES.put("publication_place_browse", ",PUBL_HDG_STRNG_TXT_PLCE,PUBL_HDG_STRNG_TXT_NME ");
		
		HDG_TABLES.put("title_browse", "TTL_HDG");
		HDG_TABLES.put("author_browse", "NME_HDG");
		HDG_TABLES.put("subject_browse", "SBJCT_HDG");
		HDG_TABLES.put("publisher_browse", "PUBL_HDG");
		HDG_TABLES.put("publication_place_browse", "PUBL_HDG");
		
		SORT_KEY_NAMES.put("title_browse", "TTL_HDG_SRT_FORM");
		SORT_KEY_NAMES.put("author_browse", "NME_HDG_SRT_FORM");
		SORT_KEY_NAMES.put("subject_browse", "SBJCT_HDG_SRT_FORM");
		//SORT_KEY_NAMES.put("publisher_browse", "(PUBL_HDG_SRT_FRM_NME||' '||PUBL_HDG_SRT_FRM_PLCE)");
		SORT_KEY_NAMES.put("publisher_browse", "PUBL_HDG_SRT_FRM_NME");
		SORT_KEY_NAMES.put("publication_place_browse", "(PUBL_HDG_SRT_FRM_PLCE||' '||PUBL_HDG_SRT_FRM_NME)");
		
		HDG_NBR_COLUMN_NAMES.put("title_browse", "TTL_HDG_NBR");
		HDG_NBR_COLUMN_NAMES.put("author_browse", "NME_HDG_NBR");
		HDG_NBR_COLUMN_NAMES.put("subject_browse", "SBJCT_HDG_NBR");
		HDG_NBR_COLUMN_NAMES.put("publisher_browse", "PUBL_HDG_NBR");
		HDG_NBR_COLUMN_NAMES.put("publication_place_browse", "PUBL_HDG_NBR");		
	}
	
	private final Map<String, String> viewMappings = new HashMap<String, String>();
	private Map<String, String> firstAvailableHeadings = new HashMap<String, String>();
	private Map<String, String> lastAvailableHeadings = new HashMap<String, String>();
	
	private int fetchSize = 100;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(AmicusBrowseRequestHandler.class);
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void init(NamedList args) 
	{
		super.init(args);
		
		NamedList<Object> mappingsBetweenSolrAndAmicus = (NamedList<Object>) args.get("logical-view-mappings");
		if(mappingsBetweenSolrAndAmicus!= null){
			for (Map.Entry<String,Object> entry : mappingsBetweenSolrAndAmicus)
			{
				viewMappings.put(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}	
		
		try 
		{
			fetchSize = Integer.parseInt((String) args.get("fetch-size"));
		} catch (Exception exception)
		{
			// Nothing, just use the default value.
		}
		
		if (datasource == null)
		{
			try 
			{
				Context namingContext = new InitialContext();
				datasource = (DataSource) namingContext.lookup("jdbc/amicus");
			} catch (Exception exception)
			{
				try 
				{
					Context namingContext = new InitialContext();
					datasource = (DataSource) namingContext.lookup("java:comp/env/jdbc/amicus");
				} catch (Exception exception2)
				{
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, exception2);					
				}
			}
		}
	}

	@Override
	public void handleRequestBody(final SolrQueryRequest request, final SolrQueryResponse response) throws Exception
	{	
		SolrParams requestParameter = request.getParams();
		
		// Check : if the incoming key is already a sort form, then there's no further process (i.e. no sort form procedure)
		String from = requestParameter.getBool(INCOMING_START_KEY_IS_IN_SORT_FORMAT, false)  
				? requestParameter.get(FROM_PARAMETER_NAME) 
				: sortKey(requestParameter.get(FROM_PARAMETER_NAME, ""));
		
		RefCounted<SolrIndexSearcher> refCount = null;
		
		try 
		{
			refCount = request.getCore().getCoreDescriptor().getCoreContainer().getCore("main").getSearcher();
			writeOutHeadingList(
					request.getSearcher(), 
					from, 
					request.getCore().getName(), 
					response, 
					requestParameter.getInt(PAGE_SIZE_PARAMETER_NAME, 10), 
					requestParameter.getInt(PREFERRED_POSITION_IN_RESPONSE_PARAMETER_NAME, 2), 
					refCount.get(),
					requestParameter.get(LOGICAL_VIEW, null));			
		} finally 
		{
			if (refCount != null)
			{
				refCount.decref();
			}
		}
	}

	/**
	 * Executes a browsing moving forward (starting from the "from" pointer).
	 * 
	 * @param thisIndexSearcher the browse index searcher.
	 * @param sortKey the starting point where to start our scan.
	 * @param coreName which corresponds to the index name the user wants to browse.
	 * @param response the SOLR response.
	 * @param pageSize the page size that will be applied to the current browsing request.
	 * @param preferredPositionInResponse the preferred position, within the returned entries, of the specified starting point value.
	 * @param mainIndexSearcher the main index searcher, that will be used for doc frequency count.
	 * @throws IOException in case of I/O failure.
	 */
	@SuppressWarnings({ "rawtypes" })
	void writeOutHeadingList(
			final IndexSearcher thisIndexSearcher, 
			final String sortKey, 
			final String coreName, 
			final SolrQueryResponse response, 
			final int pageSize,
			final int preferredPositionInResponse,
			final IndexSearcher mainIndexSearcher,
			final String logicalView) throws Exception
	{
		List<NamedList> headings = new ArrayList<NamedList>();
		List<String> sortKeys = new ArrayList<String>();
		
		String firstAvailableHeading = getFirstHeading(coreName, thisIndexSearcher, logicalView);
		String lastAvailableHeading = getLastHeading(coreName, thisIndexSearcher, logicalView);
		
		String sortKeyColumnName = SORT_KEY_NAMES.get(coreName);
		if (coreName.equals("publisher_browse")) {
			sortKeyColumnName = "(PUBL_HDG_SRT_FRM_NME||' '||PUBL_HDG_SRT_FRM_PLCE)";
		}
		
		if (sortKey != null)
		{
			switch (preferredPositionInResponse)
			{
				case 0:
					accumulateHeadings(coreName, sortKey, ">", " ORDER BY "+ sortKeyColumnName +" ASC", logicalView, false, pageSize, headings, thisIndexSearcher, mainIndexSearcher, sortKeys);
					break;
				case 1:
					accumulateHeadings(coreName, sortKey, ">=", " ORDER BY "+ sortKeyColumnName +" ASC", logicalView, false, pageSize, headings, thisIndexSearcher, mainIndexSearcher, sortKeys);
					break;				
				default:
					accumulateHeadings(coreName, sortKey, "<", " ORDER BY "+ sortKeyColumnName +" DESC", logicalView, true, preferredPositionInResponse - 1, headings, thisIndexSearcher, mainIndexSearcher, sortKeys);				
					accumulateHeadings(coreName, sortKey, ">=", " ORDER BY "+ sortKeyColumnName +" ASC", logicalView, false, pageSize - (preferredPositionInResponse - 1), headings, thisIndexSearcher, mainIndexSearcher, sortKeys);
					break;				
			}
			
			if (!headings.isEmpty() && firstAvailableHeading != null)
			{
				NamedList firstPageHeading = headings.get(0);
				if (firstPageHeading != null) { 
					String firstHeading = (String) firstPageHeading.get(TERM);
					response.getResponseHeader().add(BACK_ALLOWED, !firstAvailableHeading.equals(firstHeading));
				}
			} else 
			{
				response.getResponseHeader().add(BACK_ALLOWED, false);				
			}
			
			if (!headings.isEmpty() && lastAvailableHeading != null)
			{
				NamedList lastPageHeading = headings.get(headings.size() - 1);
				if (lastPageHeading != null) 
				{
					String lastHeading = (String) lastPageHeading.get(TERM);
					response.getResponseHeader().add("forward-allowed", !lastAvailableHeading.equals(lastHeading));
				}
			} else 
			{
				response.getResponseHeader().add("forward-allowed", false);				
			}
			
			for (NamedList heading : headings)
			{
				if (heading != null) {
					response.add(HEADING, heading);
				}
			}
		}
		
		response.getResponseHeader().add(POSITION_IN_RESPONSE, preferredPositionInResponse);
	}
	
	/**
	 * Returns the first available heading associated with the given data.
	 * 
	 * @param tableName the table name.
	 * @param searcher
	 * @param logicalView
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private String getFirstHeading(final String tableName, final IndexSearcher searcher, final String logicalView) throws SQLException, IOException
	{
		String heading = firstAvailableHeadings.get(tableName + logicalView);
		if (heading == null)
		{
			heading = getHeading(tableName, searcher, " ORDER BY "+  SORT_KEY_NAMES.get(tableName) +" ASC", logicalView);
			firstAvailableHeadings.put(tableName + logicalView, heading);
		}
		return heading;
	}
	
	private String getLastHeading(final String tableName, final IndexSearcher searcher, final String logicalView) throws SQLException, IOException
	{
		String heading = lastAvailableHeadings.get(tableName + logicalView);
		if (heading == null)
		{
			heading = getHeading(tableName, searcher, " ORDER BY "+  SORT_KEY_NAMES.get(tableName) +" DESC", logicalView);
			lastAvailableHeadings.put(tableName + logicalView, heading);
		}
		return heading;		
	}	
	
	private String getHeading(final String tableName, final IndexSearcher searcher, final String orderByClause, final String logicalView) throws SQLException, IOException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{ 	
			String whereCondition = (logicalView == null || logicalView.trim().length() == 0) 
					? " WHERE SUBSTR(USR_VW_IND, 1, 1) = '1'" 
					: " WHERE SUBSTR(USR_VW_IND, " + viewMappings.get(logicalView) + ", 1) = '1'"; 
			connection = datasource.getConnection();
			statement = connection.prepareStatement(
					"SELECT "
					+  SORT_KEY_NAMES.get(tableName) 
					+" FROM " 
					+ HDG_TABLES.get(tableName) + whereCondition + orderByClause);
			rs = statement.executeQuery();
			while (rs.next())
			{
				String txt = rs.getString(1);
				if (txt != null && txt.trim().length() != 0)
				{
					final Query query = new TermQuery(new Term(SORT_KEY, rs.getString(1)));
					TopDocs hits = searcher.search(query, 1);
					if (hits.totalHits != 0)
					{
						Document document = searcher.doc(hits.scoreDocs[0].doc);
						return document.get(TERM);
					}
				}
			}
			return null;
		} finally 
		{
			try { rs.close();} catch (Exception ignore) {}
			try { statement.close();} catch (Exception ignore) {}
			try { connection.close();} catch (Exception ignore) {}
		}
	}	
	
	/**
	 * Returns the heading associated with the given sort key.
	 * 
	 * @param indexName the index name.
	 * @param headingNumber the heading number.
	 * @param sortkey the sort key associated with the requested heading.
	 * @param stringText in case the target index is publisher or publication place.
	 * @param searcher the lucene index searcher.
	 * @param solrSearcher the solr index searcher.
	 * @return the heading associated with the given sort key.
	 * @throws IOException in case of I/O failure.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private NamedList  getBrowsableHeading(
			final String indexName, 
			final int headingNumber,
			final String sortkey, 
			final String stringText, 
			final IndexSearcher searcher, 
			final IndexSearcher solrSearcher,
			final Query mainIndexDocFrequencyWithLogicalViewQuery) throws IOException
	{
		Query query = null;
		if (stringText == null) {
			query = new TermQuery(new Term(SORT_KEY,sortkey.trim()));
		} else {
			query = new PhraseQuery();
			((PhraseQuery)query).add(new Term(TERM, stringText));
		}
		TopDocs hits = searcher.search(query, 1);
		
		// Check: if the heading is not in OseeGenius index we immediately skip it! 
		if (hits.totalHits == 0)
		{
			return null;
//			NamedList result = new NamedList();
//			result.add(TERM, sortkey);
//			result.add(HEADING_NUMBER, headingNumber);
//			result.add(SORT_KEY, sortkey);
//			result.add(COUNT, -1);
//			
//			return result;
		}
		
		NamedList result = new NamedList();
		Document document = searcher.doc(hits.scoreDocs[0].doc);
		boolean isPreferred = ("T".equals(document.get(IS_PREFERRED_FORM)));
		result.add("preferred", isPreferred);
		if (isPreferred)
		{
			int count = 0;
			
			if (mainIndexDocFrequencyWithLogicalViewQuery != null)
			{
				BooleanQuery countQuery = new BooleanQuery();
				countQuery.add(mainIndexDocFrequencyWithLogicalViewQuery, Occur.MUST);
				countQuery.add(new TermQuery(new Term(indexName, sortkey.trim())), Occur.MUST);
				
				count = solrSearcher.search(countQuery, 1).totalHits;
			} else 
			{
				count = solrSearcher.getIndexReader().docFreq(new Term(indexName, sortkey.trim()));
			}
			
			// Sanity check : if the heading is preferred but the doc frequency is 0 then it will be skept
			// because belongs to the wrong logical view.
			if (count > 0)
			{
				result.add(COUNT, count);
			} else
			{
				return null;
			}
			
			result.add(TERM, document.get(TERM));
			result.add(HEADING_NUMBER, headingNumber);
			result.add(SORT_KEY, document.get(SORT_KEY));
			
			String [] seenFrom= document.getValues("seenInsteadFrom");
			if (seenFrom != null && seenFrom.length > 0)
			{
				result.add("seenFrom", seenFrom);
			}
			
			String [] sameAs = document.getValues("sameAs");
			if (sameAs != null && sameAs.length > 0)
			{
				result.add("sameAs", sameAs);
			}
			
			String [] seeAlso = document.getValues(SEE_ALSO);
			if (seeAlso != null && seeAlso.length > 0)
			{
				result.add(SEE_ALSO, seeAlso);
			}
			
			String [] seenAlsoFrom = document.getValues("seenAlsoFrom");
			if (seenAlsoFrom != null && seenAlsoFrom.length > 0)
			{
				result.add("seenAlsoFrom", seenAlsoFrom);
			}
			
			String note = document.get(NOTE);
			if (note != null)
			{
				result.add(NOTE, note);
			}
		} else 
		{
			result.add(TERM, document.get(TERM));
			result.add(SORT_KEY, document.get(SORT_KEY));
			String [] seeInstead = document.getValues(SEE_INSTEAD);
			if (seeInstead != null && seeInstead.length > 0)
			{
				result.add(SEE_INSTEAD, seeInstead);					
			}
		}
		return result;
	}
	
	/**
	 * Builds the sortkey for the browsing index entry.
	 * Note that this is different from the sort key on indexing side because here we apply 
	 * just diacritics removal and lowercasing.
	 * 
	 * @param text the original text.
	 * @return the sortkey
	 * @throws IOException in case of I/O exception.
	 */
	private String sortKey(final String text) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		try 
		{
			TokenStream stream = new ASCIIFoldingFilter(analyzer.tokenStream("sortkey", new StringReader(text.trim())));
			CharTermAttribute term = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) 
			{
				builder.append(term.toString());
			}
		
			stream.end();
			stream.close();
			for (int index = 0; index < builder.length(); index++)
			{
				char aChar = builder.charAt(index);
				if (aChar == 31)
				{
					if (index == 0)
					{
						builder.delete(index, index + 2);
						continue;
					} else
					{
						if (builder.charAt(index -1) != ' ')
						{
							builder.replace(index, index + 2, " ");		
							index++;
						} else
						{
							builder.delete(index, index + 2);		
							continue;
						}
					}
					aChar = builder.charAt(index);
				}	
				
				if (aChar == '-')
				{
					if (index != builder.length()-1)
					{
						if ( (Character.isDigit(builder.charAt(index-1)) || Character.isLetter(builder.charAt(index-1))) 
								&& 
								( Character.isDigit(builder.charAt(index+1)) || Character.isLetter(builder.charAt(index+1))))
						{
							builder.replace(index, index + 1, " ");
						} else
						{
							builder.deleteCharAt(index);					
							continue;
						}
						index--;
						continue;
					} else 
					{
						builder.replace(index, index + 1, " ");
						aChar = builder.charAt(index);
					}
				} else if (aChar == ' ')
				{
					if (index < builder.length() -1 && builder.charAt(index + 1) == ' ')
					{
						builder.deleteCharAt(index);		
						index-=2;
						continue;
					}
				} else
				{
					if (!(Character.isLetter(aChar) || Character.isDigit(aChar) || aChar == ' '))
					{
						builder.deleteCharAt(index);
						continue;
					} 
				}
			}
			return builder.toString().replace("-", "").replace("  ", " ").trim();
		} catch(Exception exception)
		{
			throw new IllegalArgumentException(exception);
		}	
	}
	
	@Override
	public String getDescription()
	{
		return "OseeGenius -S- Browse Request Handler";
	}
	
	@Override
	public String getSource()
	{
		return NOT_AVAILABLE;
	}

	@Override
	public String getVersion()
	{
		return "1.0";
	}
	
	@SuppressWarnings("rawtypes")
	private void accumulateHeadings(
			final String indexName, 
			final String key, 
			final String operator, 
			final String orderByClause, 
			final String logicalView,
			boolean reverseOrderOnScan, 
			int scanSize,
			List<NamedList> headings,
			final IndexSearcher luceneSearcher,
			final IndexSearcher solrSearcher,
			final List<String> sortKeys) throws SQLException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(
			"SELECT " 
							+ HDG_NBR_COLUMN_NAMES.get(indexName)
							+ ","
							+ SORT_KEY_NAMES.get(indexName) 
							+ STRNG_TXT_COLUMN_NAMES.get(indexName)		
							+ " FROM " 
							+ HDG_TABLES.get(indexName) 
							+ " WHERE " + SORT_KEY_NAMES.get(indexName) + " " 
							+ operator 
							+  "  ? "
							+ ( (logicalView != null) ? " AND SUBSTR(USR_VW_IND, " + viewMappings.get(logicalView) + ", 1) = '1'" : " AND SUBSTR(USR_VW_IND, 1, 1) = '1'")
							+ orderByClause);
			statement.setFetchSize(fetchSize);		
			statement.setString(1, key);
			rs = statement.executeQuery();
			int count = 0;
			
			Query mainIndexDocFrequencyWithLogicalViewQuery = null;
			
			if (logicalView != null)
			{
				mainIndexDocFrequencyWithLogicalViewQuery = new TermQuery(new Term(CATALOG_SOURCE, logicalView));
			}
			
			boolean isPublisherOrPublicationPlace = "publisher_browse".equals(indexName) || "publication_place_browse".equals(indexName);
			
			while (rs.next() && count < scanSize)
			{
				String stringText = null;
				if (isPublisherOrPublicationPlace)
				{
					stringText = createStringText(rs.getString(3), rs.getString(4));
				}
						
				final String sortKey = rs.getString(2).trim();
				if (!sortKeys.contains(sortKey)) {
					final NamedList heading = getBrowsableHeading(indexName, rs.getInt(1), sortKey, stringText, luceneSearcher, solrSearcher, mainIndexDocFrequencyWithLogicalViewQuery);
					sortKeys.add(sortKey);
					if (reverseOrderOnScan)
					{
						if (heading != null);
						{
							headings.add(0, heading);
							count++;
						}
					} else
					{
						if (heading != null)
						{
							headings.add(heading);
							count++;
						}
					}
				}
			}
		} catch (Exception exception) 
		{
			throw new SQLException(exception);
		} finally 
		{
			try { if (rs != null) rs.close(); } catch (Exception exception) {}
			try { if (statement != null) statement.close(); } catch (Exception exception) {}
			try { if (connection != null) connection.close(); } catch (Exception exception) {}
		}
	}
	
	private static String createStringText(String placeStringTxt, String publisherStringTxt) 
	{
		try 
		{
			if (placeStringTxt != null && publisherStringTxt != null)
			{
				char s = 31;
				
				placeStringTxt = placeStringTxt.substring(2);
				int indexOfSeparator = placeStringTxt.indexOf(s);
				StringBuilder builder = new StringBuilder();
				if (indexOfSeparator == -1)
				{
					builder.append(placeStringTxt.trim()).append(" : ");				
				} else
				{
					builder.append(placeStringTxt.substring(0, indexOfSeparator));
					if(placeStringTxt.length() > indexOfSeparator + 2) {
						builder.append(" ; ");
						builder.append(placeStringTxt.substring(indexOfSeparator + 2, placeStringTxt.length()));
						builder.append(" : ");
					} else {
						builder.append(" : ");
					}
				}
				
				publisherStringTxt = publisherStringTxt.substring(2);
				indexOfSeparator = publisherStringTxt.indexOf(s);
				if (indexOfSeparator == -1)
				{
					builder.append(publisherStringTxt.trim());				
				} else 
				{
					builder.append(publisherStringTxt.substring(0, indexOfSeparator));
				}
				
				return builder.toString();
			}
			return null;
		} catch (Exception exception)
		{
			LOGGER.error("Unable to create string text.", exception);
			return null;
		}
	}
}
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
import java.util.List;

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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;

/**
 * OseeGenius Browse request handler.
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
public class BrowseRequestHandler extends RequestHandlerBase
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
	
	private static final String FROM_PARAMETER_NAME = "from";
	private static final String PAGE_SIZE_PARAMETER_NAME = "s";

	private static final String LOGICAL_VIEW = "logical_view";
	private static final String CATALOG_SOURCE = "catalog_source";
	
	private static final String PREFERRED_POSITION_IN_RESPONSE_PARAMETER_NAME = "pos";
	private static final String BACK_ALLOWED = "back-allowed";

	private DataSource datasource;
	
	private AmicusSortFormAnalyzer analyzer = new AmicusSortFormAnalyzer(Version.LUCENE_47);
	
	@SuppressWarnings("rawtypes")
	@Override
	public void init(NamedList args) 
	{
		super.init(args);
		if (datasource == null)
		{
			try 
			{
				Context namingContext = new InitialContext();
				datasource = (DataSource) namingContext.lookup("jdbc/browser");
			} catch (Exception exception)
			{
				throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, exception);
			}
		}
	}

	@Override
	public void handleRequestBody(final SolrQueryRequest request, final SolrQueryResponse response) throws Exception
	{
		final CoreContainer container = request.getCore().getCoreDescriptor().getCoreContainer();
		final SolrCore core = container.getCore("main");
		
		RefCounted<SolrIndexSearcher> refCount = null;
		
		try 
		{
			refCount = core.getSearcher();
			final SolrIndexSearcher mainSearcher = refCount.get();
			
			final SolrParams requestParameter = request.getParams();
			final int preferredPositionInResponse = requestParameter.getInt(PREFERRED_POSITION_IN_RESPONSE_PARAMETER_NAME, 2);
			final String coreName = request.getCore().getName();
			
			final String from = requestParameter.getBool("sk", false)  
					? requestParameter.get(FROM_PARAMETER_NAME) 
					: sortKey(coreName, requestParameter.get(FROM_PARAMETER_NAME, ""));	
			
			int pageSize = requestParameter.getInt(PAGE_SIZE_PARAMETER_NAME, 10);
			final String logicalView = requestParameter.get(LOGICAL_VIEW, null);
			
			final SolrIndexSearcher searcher = request.getSearcher();
			
			writeOutHeadingList(
					searcher, 
					from, 
					coreName, 
					response, 
					pageSize, 
					preferredPositionInResponse, 
					mainSearcher,
					logicalView);			
		} finally 
		{
			if (refCount != null)
			{
				refCount.decref();
			}		}
	}

	/**
	 * Executes a browsing moving forward (starting from the "from" pointer).
	 * 
	 * @param searcher the lucene index searcher.
	 * @param sortKey the starting point.
	 * @param indexName the index name the user wants to browse.
	 * @param coreName the SOLR core name.
	 * @param response the SOLR response.
	 * @param pageSize the page size that will be applied to the current browsing request.
	 * @param preferredPositionInResponse the preferred position, within the returned entries, of the specified starting point value.
	 * @throws IOException in case of I(O failure.
	 */
	@SuppressWarnings({ "rawtypes" })
	void writeOutHeadingList(
			final IndexSearcher searcher, 
			final String sortKey, 
			final String coreName, 
			final SolrQueryResponse response, 
			final int pageSize,
			final int preferredPositionInResponse,
			final IndexSearcher mainSearcher,
			final String logicalView) throws Exception
	{
		
		List<NamedList> headings = new ArrayList<NamedList>();
		String firstAvailableHeading = getFirstHeading(coreName, searcher, logicalView);
		String lastAvailableHeading = getLastHeading(coreName, searcher, logicalView);
		
		if (sortKey != null)
		{
			switch (preferredPositionInResponse)
			{
				case 0:
					accumulateHeadings(coreName, sortKey, ">", " ORDER BY SORT_KEY ASC", logicalView, false, pageSize, headings, searcher, mainSearcher);
					break;
				case 1:
					accumulateHeadings(coreName, sortKey, ">=", " ORDER BY SORT_KEY ASC", logicalView, false, pageSize, headings, searcher, mainSearcher);
					break;				
				default:
					accumulateHeadings(coreName, sortKey, "<", " ORDER BY SORT_KEY DESC", logicalView, true, preferredPositionInResponse - 1, headings, searcher, mainSearcher);				
					accumulateHeadings(coreName, sortKey, ">=", " ORDER BY SORT_KEY ASC", logicalView, false, pageSize - (preferredPositionInResponse - 1), headings, searcher, mainSearcher);
					break;				
			}
			
			if (!headings.isEmpty() && firstAvailableHeading != null)
			{
				NamedList firstPageHeading = headings.get(0);
				String firstHeading = (String) firstPageHeading.get(TERM);
				response.getResponseHeader().add(BACK_ALLOWED, !firstAvailableHeading.equals(firstHeading));
			} else 
			{
				response.getResponseHeader().add(BACK_ALLOWED, false);				
			}
			
			if (!headings.isEmpty() && lastAvailableHeading != null)
			{
				NamedList lastPageHeading = headings.get(headings.size() - 1);
				String lastHeading = (String) lastPageHeading.get(TERM);
				response.getResponseHeader().add("forward-allowed", !lastAvailableHeading.equals(lastHeading));
			} else 
			{
				response.getResponseHeader().add("forward-allowed", false);				
			}
			
			for (NamedList heading : headings)
			{
				response.add(HEADING, heading);
			}
		}
		
		response.getResponseHeader().add(POSITION_IN_RESPONSE, preferredPositionInResponse);
	}
	
	private String getFirstHeading(final String tableName, final IndexSearcher searcher, final String logicalView) throws SQLException, IOException
	{
		return getHeading(tableName, searcher, " ORDER BY SORT_KEY ASC LIMIT 1", logicalView);
	}
	
	private String getLastHeading(final String tableName, final IndexSearcher searcher, final String logicalView) throws SQLException, IOException
	{
		return getHeading(tableName, searcher, " ORDER BY SORT_KEY DESC LIMIT 1", logicalView);
	}	
	
	private String getHeading(final String tableName, final IndexSearcher searcher, final String orderByClause, final String logicalView) throws SQLException, IOException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			String whereCondition = (logicalView == null || logicalView.trim().length() == 0) ? "" : " WHERE (LOGICAL_VIEW='"+logicalView+"' OR LOGICAL_VIEW ='ALL')";
			connection = datasource.getConnection();
			statement = connection.prepareStatement("SELECT SORT_KEY FROM " + tableName + whereCondition + orderByClause);
			rs = statement.executeQuery();
			if (rs.next())
			{
				final Query query = new TermQuery(new Term(SORT_KEY, rs.getString(1)));
				TopDocs hits = searcher.search(query, 1);
				if (hits.totalHits != 0)
				{
					Document document = searcher.doc(hits.scoreDocs[0].doc);
					return document.get(TERM);
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
	 * @param sortkey the sort key associated with the requested heading.
	 * @param searcher the lucene index searcher.
	 * @param solrSearcher the solr index searcher.
	 * @return the heading associated with the given sort key.
	 * @throws IOException in case of I/O failure.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private NamedList  getBrowsableHeading(
			final String indexName, 
			final String sortkey, 
			final IndexSearcher searcher, 
			final IndexSearcher solrSearcher,
			final Query logicalViewQuery) throws IOException
	{
		final Query query = new TermQuery(new Term(SORT_KEY,sortkey));
		TopDocs hits = searcher.search(query, 1);
		NamedList result = new NamedList();
		if (hits.totalHits != 0)
		{
			Document document = searcher.doc(hits.scoreDocs[0].doc);

			boolean isPreferred = ("T".equals(document.get(IS_PREFERRED_FORM)));
			result.add("preferred", isPreferred);
			if (isPreferred)
			{
				result.add(TERM, document.get(TERM));
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
				
				if (logicalViewQuery != null)
				{
					BooleanQuery countQuery = new BooleanQuery();
					countQuery.add(logicalViewQuery, Occur.MUST);
					countQuery.add(new TermQuery(new Term(indexName, document.get(SORT_KEY))), Occur.MUST);
					
					result.add(COUNT, solrSearcher.search(countQuery, 1).totalHits);
				} else 
				{
					result.add(COUNT, solrSearcher.getIndexReader().docFreq(new Term(indexName, document.get(SORT_KEY))));
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
		}
		return result;
	}
	
	private boolean isValidIsnPart(final char ch)
	{
		return Character.isDigit(ch) || ch == 'X' || ch == 'x';
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
	private String sortKey(final String indexName,  final String text) throws IOException
	{
		if (indexName.indexOf("isbn") != -1 || indexName.indexOf("issn") != -1)
		{
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < text.length(); i++)
			{
				char ch = text.charAt(i);
				if (isValidIsnPart(ch))
				{
					builder.append(ch);
				}
			}
			return builder.toString();
		}
		
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
			stream.reset();
			
			String tmp = builder.toString();
			if (tmp.indexOf("--") != -1)
			{
				return tmp.replace("--", "");
			}
			
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
					if (index < builder.length() && builder.charAt(index + 1) == ' ')
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
			return builder.toString();
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
			final IndexSearcher solrSearcher) throws SQLException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(
					"SELECT SORT_KEY FROM " 
							+ indexName 
							+ " WHERE SORT_KEY " 
							+ operator 
							+  "  ? "
							+ ( (logicalView != null) ? " AND (LOGICAL_VIEW='" + logicalView +"' OR LOGICAL_VIEW ='ALL')" : "")
							+ orderByClause);
			statement.setString(1, key);
			rs = statement.executeQuery();
			int count = 0;
			Query logicalViewQuery = null;
			
			if (logicalView != null)
			{
				logicalViewQuery = new TermQuery(new Term(CATALOG_SOURCE, logicalView));
			}
		
			while (rs.next() && count < scanSize)
			{
				if (reverseOrderOnScan)
				{
					headings.add(0, getBrowsableHeading(indexName, rs.getString(1), luceneSearcher, solrSearcher, logicalViewQuery));
				} else
				{
					headings.add(getBrowsableHeading(indexName, rs.getString(1), luceneSearcher, solrSearcher, logicalViewQuery));
				}
				count++;
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
}
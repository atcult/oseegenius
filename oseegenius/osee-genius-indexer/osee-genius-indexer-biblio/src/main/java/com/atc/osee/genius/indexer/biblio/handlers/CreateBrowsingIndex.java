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
package com.atc.osee.genius.indexer.biblio.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.IDecorator;
import com.atc.osee.genius.indexer.biblio.browsing.ControlledBrowsingIndexer;
import com.atc.osee.genius.indexer.biblio.browsing.IAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;
import com.atc.osee.genius.indexer.biblio.browsing.ISortKeyStrategy;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.DummyAuthorityAccessObject;

/**
 * A special decorator that creates a browsing index.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class CreateBrowsingIndex implements IDecorator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateBrowsingIndex.class);
	
	protected Map<String, ControlledBrowsingIndexer> indexers = new HashMap<String, ControlledBrowsingIndexer>();
	protected Map<String, SolrServer> browsingSolrServer = new HashMap<String, SolrServer>();
	protected Map<String, IAuthorityAccessObject> aaos = new HashMap<String, IAuthorityAccessObject>();
	protected Map<String, IHeadingFilter> filters = new HashMap<String, IHeadingFilter>();
	protected Map<String, ISortKeyStrategy> sortKeyStrategies = new HashMap<String, ISortKeyStrategy>();
	protected String logicalViewExpression;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object decorate(final Record record, final String fieldName, final Object value) 
	{
		if (value == null)
		{
			return null;
		}
		
		if (value instanceof Collection)
		{
			return decorate(record, fieldName, (Collection<String>) value);
		} 
		return decorate(record, fieldName, String.valueOf(value));
	}
	
	@SuppressWarnings("unchecked")
	public Object decorate(final Record record, final String fieldName, final Collection<String> headings) 
	{
		if (headings == null || headings.isEmpty())
		{
			return headings;
		}
		
		final List<String> result = new ArrayList<String>(headings.size());
		for (String heading : headings)
		{
			result.addAll((Set<String>)decorate(record, fieldName, heading));
		}
		
		return result;
	}
	
	public Object decorate(final Record record, final String fieldName, final String heading) 
	{
		final Set<String> result = new HashSet<String>();
		
		String [] labelAndValue = null;
		if (heading.indexOf(GetCompoundValue.SEPARATOR) != -1)
		{
			labelAndValue = heading.split(GetCompoundValue.SEPARATOR);
		}
		
		final ControlledBrowsingIndexer indexer = indexers.get(fieldName);
		final SolrServer server = browsingSolrServer.get(fieldName);
		final IAuthorityAccessObject aao = aaos.get(fieldName);
		final IHeadingFilter filter = filters.get(fieldName);
		final ISortKeyStrategy strategy = sortKeyStrategies.get(fieldName);
		if (indexer != null && server != null && aao != null)
		{
			final Set<String> logicalViews = getLogicalView(record);
			if (labelAndValue != null)
			{
				indexer.index(
						fieldName, 
						labelAndValue[0], 
						labelAndValue[1], 
						server, 
						aao, 
						filter, 
						strategy,
						logicalViews);				
			} else 
			{
				indexer.index(
						fieldName, 
						heading, 
						server, 
						aao, 
						filter, 
						strategy,
						logicalViews);
			}
		}
	
		final String rawValue = (labelAndValue != null) ? labelAndValue[0] : heading;
		result.add(rawValue);
		if (labelAndValue != null) {
			result.add(labelAndValue[1]);
		}
		
	
		final String sortKey = labelAndValue != null ? strategy.sortKey(labelAndValue[1], filter) : strategy.sortKey(heading, filter); 
		
		result.add(sortKey);
		
		return result;
	}
	
	/**
	 * Adds a new browsing indexer associated with the given fieldname. 
	 * 
	 * @param fieldName the field name.
	 * @param indexer the browsing indexer.
	 * @param aao the authority access object.
	 * @param filter the heading filter.
	 * @param sortKeyStrategy the sort key strategy,
	 */
	public void add(
			final String fieldName, 
			final ControlledBrowsingIndexer indexer, 
			final SolrServer server, 
			final IAuthorityAccessObject aao,
			final IHeadingFilter filter,
			final ISortKeyStrategy sortKeyStrategy)
	{
		indexers.put(fieldName, indexer);
		browsingSolrServer.put(fieldName, server);
		aaos.put(fieldName, aao != null ? aao : new DummyAuthorityAccessObject());
		filters.put(fieldName, filter);
		sortKeyStrategies.put(fieldName, sortKeyStrategy);
	}
	
	/**
	 * As this decorator uses index reader and writers, it needs to flush its work once done.
	 */
	public void release()
	{
		for (Entry<String, SolrServer> entry : browsingSolrServer.entrySet())
		{
			String fieldAndCoreName = entry.getKey();
			try 
			{
				entry.getValue().commit();
			} catch (Exception ignore) 
			{
				LOGGER.error("Browsing Indexer with name "+ fieldAndCoreName + " failed commit.", ignore);
			}
		}
		
		for (Entry<String, IAuthorityAccessObject> entry : aaos.entrySet())
		{
			IAuthorityAccessObject aao = entry.getValue();
			aao.shutdown();
		}
	}
	
	/**
	 * Injects the expression that will be used to get the logical view information.
	 * This is used when we wants to "split" headings among different logical views.
	 * 
	 * @param logicalViewExpression a valid OBML expression.
	 */
	public void setLogicalViewExpression(final String logicalViewExpression)
	{
		if (logicalViewExpression != null && logicalViewExpression.trim().length() != 0)
		{
			this.logicalViewExpression = logicalViewExpression;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Set<String> getLogicalView(final Record record)
	{
		if (logicalViewExpression == null || logicalViewExpression.length() < 4)
		{
			return null;
		}
		
		final Set<String> logicalViews = new HashSet<String>();
		
		if (logicalViewExpression != null && logicalViewExpression.startsWith("\"") && logicalViewExpression.endsWith("\""))
		{
			logicalViews.add(logicalViewExpression.substring(1, logicalViewExpression.length() - 1));
			return logicalViews;
		}
		
		final List<VariableField> fields = (List<VariableField>) record.getVariableFields(logicalViewExpression.substring(0, 3));
		for (VariableField f : fields)
		{
			DataField field = (DataField) f;
			List<Subfield> subfields = field.getSubfields(logicalViewExpression.charAt(3));
			for (Subfield subfield : subfields)
			{
				if (subfield != null)
				{
					String data = subfield.getData();
					if (data != null && data.trim().length() != 0)
					{
						logicalViews.add(data);
					}
				}
			}
		}
		return logicalViews;
	}
}
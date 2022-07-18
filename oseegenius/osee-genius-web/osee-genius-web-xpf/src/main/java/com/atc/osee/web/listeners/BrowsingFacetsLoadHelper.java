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
package com.atc.osee.web.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;

/**
 * Utility class used for lazy loading all hierarchies used 
 * for browsing.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class BrowsingFacetsLoadHelper
{	
	private static final String _0 = "0";
	private static final String _00 = "00";
	
	/**
	 * Loads classification hierarchies.
	 * 
	 * @param applicationContext the OseeGenius -W- application context.
	 * @param searchEngine the search engine.
	 */
	public static void loadBrowsingHierarchies(final ServletContext applicationContext, final ISearchEngine searchEngine)
	{
		if (searchEngine != null)
		{
			try
			{ 
				QueryResponse queryResponse = searchEngine.getHomepageResources();
		    	loadAndStoreDeweyHierarchy(
		    			applicationContext, 
		    			queryResponse.getFacetField(ISolrConstants.DEWEY_FIELD_NAME));
			} catch (Exception exception)
			{
				Log.error(MessageCatalog._100020_BROWSING_CLASSIFICATION_LOAD_FAILURE, exception);
			}
		}
	}
	
	/**
	 * Loads the CDD hierarchy.
	 * 
	 * @param applicationContext the OseeGenius -W- application context.
	 * @param deweyFacets the facet field containing deweys. 
	 */
    private static void loadAndStoreDeweyHierarchy(final ServletContext applicationContext, final FacetField deweyFacets)
    {
		if (deweyFacets == null || deweyFacets.getValueCount() == 0)
		{
			return;
		}
		
		List<Count> counts = deweyFacets.getValues();

    	List<HierarchyClassificationNode> roots = new ArrayList<HierarchyClassificationNode>();
		Map<String, HierarchyClassificationNode> nodes = new HashMap<String, HierarchyClassificationNode>();	
		
		for (Count count : counts)
		{
			if (count.getName().length() < 3) 
			{
				continue;
			}
			
			if (count.getName().length() >= 3) 
			{
				try 
				{
					String name = count.getName().substring(0, 3);
					Integer.parseInt(name); 
					count.setName(name);
					if (nodes.containsKey(name))
					{
						continue;
					}
				} catch (Exception exception) 
				{
					continue;
				}
			}
			
			
			HierarchyClassificationNode node = new HierarchyClassificationNode(count.getName());
			if (count.getName().endsWith(_00))
			{
				processRootNodes(roots, nodes, node);
			} else if (count.getName().endsWith(_0))
			{
				processFirstLevelNodes(roots, nodes, node, _00);
			} else  
			{
				processSecondLevelDeweyNodes(roots, nodes, node);
			}
		}
	
		if (!roots.isEmpty())
		{
			Collections.sort(roots, HierarchyClassificationNode.COMPARATOR);
			applicationContext.setAttribute("browsing", roots);
		}
    }	
    
    /**
     * Processes the main Dewey classes.
     * 
     * @param roots the main classes.
     * @param nodes a map containing dewey nodes.
     * @param node the current node.
     * @return a root node.
     */
	private static HierarchyClassificationNode processRootNodes(
			final List<HierarchyClassificationNode> roots,
			final Map<String, HierarchyClassificationNode> nodes,
			final HierarchyClassificationNode node)
	{
		if (!roots.contains(node))
		{
			roots.add(node);
			nodes.put(node.getMessageKey(), node);
			return node;
		}
		return null;
	}

	/**
	 * TBD.
	 * 
	 * @param roots a 
	 * @param nodes a
	 * @param node a
	 * @param parentSuffix a
 	 * @return a
	 */
	private static HierarchyClassificationNode processFirstLevelNodes(
			final List<HierarchyClassificationNode> roots,
			final Map<String, HierarchyClassificationNode> nodes,
			HierarchyClassificationNode node, final String parentSuffix)
	{
		String parentNodeName = node.getMessageKey().substring(0, 1) + parentSuffix;
		HierarchyClassificationNode parent = nodes.get(parentNodeName);
		if (parent == null)
		{
			parent = new HierarchyClassificationNode(parentNodeName);
			nodes.put(parentNodeName, parent);
			roots.add(parent);
		}

		
		if (!nodes.containsKey(node.getMessageKey()))
		{
			nodes.put(node.getMessageKey(), node);			
		} else
		{
			node = nodes.get(node.getMessageKey());
		}
		
		parent.addChild(node);
		return node;
	}

	/**
	 * Processes the second level of a CDD hierarchy.
	 * 
	 * @param roots the CDD main classes.
	 * @param nodes the CDD nodes.
	 * @param node the current CDD node.
	 */
	private static void processSecondLevelDeweyNodes(
			final List<HierarchyClassificationNode> roots,
			final Map<String, HierarchyClassificationNode> nodes,
			HierarchyClassificationNode node)
	{
		String parentNodeName = node.getMessageKey().substring(0, 2) + _0; 
		HierarchyClassificationNode parent = nodes.get(parentNodeName);

		if (parent == null)
		{
			parent = new HierarchyClassificationNode(parentNodeName);
			if (parent.getMessageKey().endsWith(_00))
			{
				processRootNodes(roots, nodes, parent);
			} else
			{
				processFirstLevelNodes(roots, nodes, parent, _00);
			}
		}
		
		if (!nodes.containsKey(node.getMessageKey()))
		{
			nodes.put(node.getMessageKey(), node);			
		} else
		{
			node = nodes.get(node.getMessageKey());
		}
		
		parent.addChild(node);
	}
}

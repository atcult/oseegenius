package com.atc.osee.web.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;

/**
 * A OseeGenius -W- lifecycle application listener that loads GCT hierarchy.
 * 
 * @author agazzarini
 * @since 1,0
 */
public class AuthorityLoaderApplicationLifecycleListener extends DefaultApplicationLifeCycleListener 
{
	private final static ResourceBundle CGT = ResourceBundle.getBundle("cgt", Locale.ITALIAN);

	/**
	 * Loads CDD hierarchy on application start-up.
	 * 
	 * @param application the OseeGenius -W- application context.
	 * @param searchEngine a valid OseeGenius -S- reference.
	 */
	protected void onContextActivation(final ServletContext application, final ISearchEngine searchEngine) 
	{
		try 
		{
			QueryResponse queryResponse = searchEngine.getHomepageResources();
			FacetField field = queryResponse.getFacetField("cgt");
			
			List<HierarchyClassificationNode> roots = new ArrayList<HierarchyClassificationNode>();
			Map<String, HierarchyClassificationNode> nodes = new HashMap<String, HierarchyClassificationNode>();
			
			// Sanity check: if we don't have cgt immediately return.
			if (field == null || field.getValueCount() == 0)
			{
				return;
			}
			
			for (Count cgtEntry : field.getValues())
			{
				String name = cgtEntry.getName();
				
				if (name.trim().length() == 0)
				{
					continue;
				}
				
				// Check : if the node is a root node (and hasn't been yet collected) then immediately collect it.
				if (isRootName(name))
				{
					if (!nodes.containsKey(name))
					{
						roots.add(new HierarchyClassificationNode(name));
					}
				} else 
				{
					createIfNotYetThere(name, roots, nodes);
				}
			}
			
			Comparator<HierarchyClassificationNode> comparator = new Comparator<HierarchyClassificationNode>() 
			{
				@Override
				public int compare(
						final HierarchyClassificationNode o1,
						final HierarchyClassificationNode o2) 
				{
					if ("TTRENTINO".equals(o1.getMessageKey()))
					{
						return 1;
					}	
					return o1.getMessageKey().compareTo(o2.getMessageKey());
				}
			};
			
			Collections.sort(roots, comparator);
			
			for (Iterator<HierarchyClassificationNode> iterator = roots.iterator(); iterator.hasNext();) 
			{
				HierarchyClassificationNode root = iterator.next();
				if ("TTRENTINO".equals(root.getMessageKey()))
				{
					iterator.remove();
				}
			}
			
			application.setAttribute("cgt", roots);
		} catch (Exception exception)
		{
			Log.error(
					MessageCatalog._100004_MALFORMED_IR_URL, 
					exception);
		}
	}

	private HierarchyClassificationNode createIfNotYetThere(
			final String name, 
			final List<HierarchyClassificationNode> roots, 
			final Map<String, HierarchyClassificationNode> nodes)
	{
		HierarchyClassificationNode node = null;
		if (!nodes.containsKey(name))
		{
			try 
			{
				if (!isRootName(name))
				CGT.getString(name);
			} catch (MissingResourceException exception)
			{
				return createIfNotYetThere(name.substring(0, name.length() -1), roots, nodes);
			}
			
			node = new HierarchyClassificationNode(name);
			nodes.put(name, node);
			
		} else 
		{
			node = nodes.get(name);
		}
		
		if (isRootName(name))
		{
			return getOrCreateRootNode(name, roots, nodes);
		} else 
		{
			String parentName = name.substring(0, name.length() - 1);
			HierarchyClassificationNode parentNode = createIfNotYetThere(parentName, roots, nodes);
			parentNode.addChild(node);
		}
		return node;
	}
	
	private HierarchyClassificationNode getOrCreateRootNode(final String name, final List<HierarchyClassificationNode> roots, final Map<String, HierarchyClassificationNode> nodes)
	{
		for (HierarchyClassificationNode root : roots)
		{
			if (name.equals(root.getMessageKey()))
			{
				return root;
			}
		}
		
		HierarchyClassificationNode newRoot = new HierarchyClassificationNode(name);
		roots.add(newRoot);
		nodes.put(name, newRoot);
		return newRoot;
	}
	
	private boolean isRootName(final String name) 
	{
		return name.length() == 2 
				|| "TTRENTINO".equals(name) 
				|| "TAA".equals(name)
				|| "TT01".equals(name) 
				|| "TT02".equals(name) 
				|| "TT03".equals(name) 
				|| "TT04".equals(name);
	}
}
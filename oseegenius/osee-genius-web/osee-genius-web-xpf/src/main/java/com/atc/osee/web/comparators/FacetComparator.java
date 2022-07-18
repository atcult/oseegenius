package com.atc.osee.web.comparators;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * Comparator for facet elements (For advanced search filter refer to AdvancedFilterComparator)
 * @author aguercio
 *
 */

public class FacetComparator implements Comparator<Count> 
{
	private Map<String, Integer> ORDER = new HashMap<String, Integer>();
	
	public FacetComparator(Map<String, Integer> order) {
		this.ORDER = order;
	}
	
	@Override
	public int compare(Count code1, Count code2) 
	{
		return criteria(code1.getName()).compareTo(criteria(code2.getName()));
	}
	
	private Integer criteria(final String name)
	{
		Integer criteria = ORDER.get(name);
		return(criteria != null) ? criteria : Integer.MIN_VALUE;
	}
}

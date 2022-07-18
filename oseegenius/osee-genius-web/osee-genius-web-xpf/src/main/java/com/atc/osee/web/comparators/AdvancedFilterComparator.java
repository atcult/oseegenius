package com.atc.osee.web.comparators;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.atc.osee.web.model.LimitFacetEntry;

/**
 * Comparator for advanced Search filters elements (For Facets refer to FacetComparator )
 * @author aguercio
 *
 */

public class AdvancedFilterComparator implements Comparator<LimitFacetEntry> 
{
	private Map<String, Integer> ORDER = new HashMap<String, Integer>();
	
	public AdvancedFilterComparator(Map<String, Integer> order) {
		this.ORDER = order;
	}
	
	@Override
	public int compare(LimitFacetEntry code1, LimitFacetEntry code2) 
	{
		return criteria(code1.getName()).compareTo(criteria(code2.getName()));
	}
	
	private Integer criteria(final String name)
	{
		Integer criteria = ORDER.get(name);
		return(criteria != null) ? criteria : Integer.MIN_VALUE;
	}
}

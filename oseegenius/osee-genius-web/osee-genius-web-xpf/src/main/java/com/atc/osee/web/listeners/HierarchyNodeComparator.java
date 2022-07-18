package com.atc.osee.web.listeners;

import java.util.Comparator;

public class HierarchyNodeComparator implements Comparator<HierarchyClassificationNode> 
{
	@Override
	public int compare(
			final HierarchyClassificationNode n1, 
			final HierarchyClassificationNode n2) 
	{
		return n1.getMessageKey().compareTo(n2.getMessageKey());
	}	
}
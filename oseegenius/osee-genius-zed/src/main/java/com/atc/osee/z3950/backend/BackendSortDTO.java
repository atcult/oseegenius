/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Affero General Public License version 3 as published by the Free 
 * Software Foundation with the addition of the following permission added to Section 
 * 15 as permitted in Section 7(a). 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. 
 * You should have received a copy of the GNU Affero General Public License along with this program;
 */
package com.atc.osee.z3950.backend;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jzkit.search.SortModel;
import org.jzkit.search.util.QueryModel.Internal.AttrValue;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.AttributeElement_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.SortKeySpec_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.SortKey_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.SortRequest_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.sortAttributes_inline39_type;

/**
 * A transfer data object for sort request parameters.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class BackendSortDTO implements SortModel
{
	private final String resultSetName;	
	public final List<BigInteger> ascending = new ArrayList<BigInteger>(4);
	public final List<BigInteger> descending = new ArrayList<BigInteger>(4);
	public final List<String> asc = new ArrayList<String>(4);
	public final List<String> desc = new ArrayList<String>(4);
	
	@SuppressWarnings("unchecked")
	public BackendSortDTO(final SortRequest_type sortRequest)
	{
		this.resultSetName = sortRequest.sortedResultSetName;
		final List<SortKeySpec_type> sequence = sortRequest.sortSequence;
		for (SortKeySpec_type sortKey : sequence)
		{
			boolean isAscending = BigInteger.ZERO.equals(sortKey.sortRelation); 
			final SortKey_type sortKeyType = (SortKey_type) sortKey.sortElement.o;
			sortAttributes_inline39_type attributes = (sortAttributes_inline39_type) sortKeyType.o;
			for (Object element : attributes.list)
			{
				final AttributeElement_type attributeElement = (AttributeElement_type) element;
				final BigInteger attributeId = (BigInteger) attributeElement.attributeValue.o;
				
				if (isAscending)
				{
					ascending.add(attributeId);
				} else 
				{
					descending.add(attributeId);
				}
			}
		}
	}

	/**
	 * Returns an iterator over the ascending attributes.
	 * 
	 * @return an iterator over the ascending attributes.
	 */
	public List<String> getAscendingAttributes()
	{
		return asc;
	}

	/**
	 * Returns an iterator over the descending attributes.
	 * 
	 * @return an iterator over the descending attributes.
	 */
	public  List<String> getDescendingAttributes()
	{
		return desc;
	}

	/**
	 * Returns the target resultset name. 
	 * 
	 * @return the target resultset name.
	 */
	public String getResultSetName() 
	{
		return resultSetName;
	}
	
	public void convert2SOLRAttributes(Map<String, AttrValue> attributes)
	{
		for (BigInteger attribute : ascending)
		{
			final AttrValue attributeValue = attributes.get("bib-1.1." + attribute);
			if (attributeValue != null)
			{
				asc.add(attributeValue.getValue()+"_sort");
			}
		}

		for (BigInteger attribute : descending)
		{
			final AttrValue attributeValue = attributes.get("bib-1.1." + attribute);
			if (attributeValue != null)
			{
				desc.add(attributeValue.getValue()+"_sort");
			}
		}
	}
}
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
package com.atc.osee.z3950;

/**
 * Helper classes used for determining a given query features.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class QueryStrategy 
{
	private final boolean useRightTruncation;
	private final String fieldSuffix;
	
	private final boolean useDoubleQuotes;
	private final boolean preProcessingNeeded;
	
	/**
	 * Builds a new query strategy.
	 * 
	 * @param useRightTruncation if right truncation is needed.
	 * @param fieldSuffix the suffix of the query field(s).
	 * @param useDoubleQuotes if exact (double quotes) query is needed.
	 * @param preProcessingNeeded if term preprocessing / normalization is needed.
	 */
	public QueryStrategy(
			final boolean useRightTruncation, 
			final String fieldSuffix, 
			final boolean useDoubleQuotes, 
			final boolean preProcessingNeeded) 
	{
		this.useRightTruncation = useRightTruncation;
		this.fieldSuffix = fieldSuffix;
		this.useDoubleQuotes = useDoubleQuotes;
		this.preProcessingNeeded = preProcessingNeeded;
	}

	/**
	 * Returns true if right truncation should be applied.
	 * 
	 * @return true if right truncation should be applied.
	 */
	public boolean isUseRightTruncation() 
	{
		return useRightTruncation;
	}
	
	/**
	 * Returns the suffix that will be applied to attribute(s).
	 * 
	 * @return the suffix that will be applied to attribute(s).
	 */
	public String getFieldSuffix() 
	{
		return fieldSuffix;
	}
	
	/**
	 * Returns true if exact match (double quotes) has to be used.
	 * 
	 * @return true if exact match (double quotes) has to be used,
	 */
	public boolean isUseDoubleQuotes() 
	{
		return useDoubleQuotes;
	}
	
	/**
	 * Returns true if term normalization / preprocessing is needed.
	 * 
	 * @return true if term normalization / preprocessing is needed.
	 */
	public boolean isPreProcessingNeeded()
	{
		return preProcessingNeeded;
	}
	
	@Override
	public String toString() 
	{
		return new StringBuilder()
			.append("Right truncation:")
			.append(useRightTruncation)
			.append(", field suffix:")
			.append(fieldSuffix)
			.append(", double quotes:")
			.append(useDoubleQuotes)
			.append(", pre-processing:")
			.append(preProcessingNeeded)
			.toString();
	}
}
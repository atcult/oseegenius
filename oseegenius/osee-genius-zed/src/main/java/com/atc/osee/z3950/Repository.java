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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Enumerative interface for Z3950 constants.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class Repository 
{
	// RELATION (2)
	static final String EQUAL = "bib-1.2.3";
	
	// POSITION (3)
	static final String FIRST_IN_FIELD = "bib-1.3.1";
	static final String ANY_POSITION_IN_FIELD = "bib-1.3.3";
	
	// STRUCTURE (4)
	static final String PHRASE = "bib-1.4.1";
	static final String WORD = "bib-1.4.2";
	
	// TRUNCATION (5)
	static final String RIGHT_TRUNCATION = "bib-1.5.1";
	static final String NO_TRUNCATION = "bib-1.5.100";

	// COMPLETENESS (6)
	static final String INCOMPLETE_SUBFIELD = "bib-1.6.1";	
	static final String COMPLETE_SUBFIELD = "bib-1.6.3";	
	
	static final int KEYWORD_SEARCH = (EQUAL + ANY_POSITION_IN_FIELD + WORD + NO_TRUNCATION + INCOMPLETE_SUBFIELD).hashCode();
	static final int KEYWORD_WITH_RIGHT_TRUNCATION_SEARCH = (EQUAL + ANY_POSITION_IN_FIELD + WORD + RIGHT_TRUNCATION + INCOMPLETE_SUBFIELD).hashCode();
	static final int EXACT_MATCH = (EQUAL + FIRST_IN_FIELD + PHRASE + NO_TRUNCATION + COMPLETE_SUBFIELD).hashCode();
	static final int FIRST_CHARS_IN_FIELD = (EQUAL + FIRST_IN_FIELD + PHRASE + RIGHT_TRUNCATION + INCOMPLETE_SUBFIELD).hashCode();
	static final int FIRST_WORDS_IN_FIELD = (EQUAL + FIRST_IN_FIELD + PHRASE + NO_TRUNCATION + INCOMPLETE_SUBFIELD).hashCode();
	
	private static final Logger LOGGER = Logger.getLogger("OSEE-GENIUZ-QUERY-STRATEGY");
	
	private static final Set<Integer> SUPPORTED_RELATION_ATTRIBUTES = new HashSet<Integer>();
	private static final Set<Integer> SUPPORTED_POSITION_ATTRIBUTES = new HashSet<Integer>();
	private static final Set<Integer> SUPPORTED_STRUCTURE_ATTRIBUTES = new HashSet<Integer>();
	private static final Set<Integer> SUPPORTED_TRUNCATION_ATTRIBUTES = new HashSet<Integer>();
	private static final Set<Integer> SUPPORTED_COMPLETENESS_ATTRIBUTES = new HashSet<Integer>();
	
	// truncation, field, double quotes, preprocessing
	
	private static final String _SEARCH_SUFFIX = "_keyword";
	private static final String WILDCARD_COMPLETE_P_SEARCH = "_wildcard_complete_phrase_search";
	private static final QueryStrategy KEYWORD_SEARCH_STRATEGY = new QueryStrategy(false, _SEARCH_SUFFIX, false, false);
	private static final QueryStrategy KEYWORD_WITH_RIGHT_TRUNCATION_STRATEGY = new QueryStrategy(true, _SEARCH_SUFFIX, false, false);
	private static final QueryStrategy EXACT_MATCH_STRATEGY = new QueryStrategy(false, WILDCARD_COMPLETE_P_SEARCH, true, true);
	private static final QueryStrategy FIRST_CHARS_IN_FIELDS_STRATEGY = new QueryStrategy(true, WILDCARD_COMPLETE_P_SEARCH, true, true);
	
	private static final Map<Integer, QueryStrategy> STRATEGIES = new HashMap<Integer, QueryStrategy>();
	
	// Duplicated strings
	private static final String IT_WILL_REPLACED_WITH = ". It will be replaced with ";
	private static final String COMMA = ", ";
	
	static 
	{
		SUPPORTED_RELATION_ATTRIBUTES.add(EQUAL.hashCode());
		
		SUPPORTED_POSITION_ATTRIBUTES.add(FIRST_IN_FIELD.hashCode());
		SUPPORTED_POSITION_ATTRIBUTES.add(ANY_POSITION_IN_FIELD.hashCode());
		
		SUPPORTED_STRUCTURE_ATTRIBUTES.add(PHRASE.hashCode());
		SUPPORTED_STRUCTURE_ATTRIBUTES.add(WORD.hashCode());
		
		SUPPORTED_TRUNCATION_ATTRIBUTES.add(NO_TRUNCATION.hashCode());
		SUPPORTED_TRUNCATION_ATTRIBUTES.add(RIGHT_TRUNCATION.hashCode());
		
		SUPPORTED_COMPLETENESS_ATTRIBUTES.add(COMPLETE_SUBFIELD.hashCode());		
		SUPPORTED_COMPLETENESS_ATTRIBUTES.add(INCOMPLETE_SUBFIELD.hashCode());
		
		STRATEGIES.put(KEYWORD_SEARCH, KEYWORD_SEARCH_STRATEGY);
		STRATEGIES.put(KEYWORD_WITH_RIGHT_TRUNCATION_SEARCH, KEYWORD_WITH_RIGHT_TRUNCATION_STRATEGY);
		STRATEGIES.put(EXACT_MATCH, EXACT_MATCH_STRATEGY);
		STRATEGIES.put(FIRST_CHARS_IN_FIELD, FIRST_CHARS_IN_FIELDS_STRATEGY);
		STRATEGIES.put(FIRST_WORDS_IN_FIELD, FIRST_CHARS_IN_FIELDS_STRATEGY);
	}
	
	/**
	 * Returns a query strategy according with the input data.
	 * 
	 * @param inputRelationAttribute the relation attribute.
	 * @param inputPositionAttribute the position attribute.
	 * @param structure the structure attribute.
	 * @param truncation the truncation attribute.
	 * @param completeness the completeness attribute.
	 * @return a query strategy according with the input data.
	 */
	public static QueryStrategy getQueryStrategy(
			final Object inputRelationAttribute, 
			final Object inputPositionAttribute, 
			final Object structure, 
			final Object truncation, 
			final Object completeness)
	{
		String relation = String.valueOf(inputRelationAttribute);
		String position = String.valueOf(inputPositionAttribute);
		if (!SUPPORTED_RELATION_ATTRIBUTES.contains(relation.hashCode()))
		{
			LOGGER.debug("Un unsupported relation attribute has been found (" + relation + IT_WILL_REPLACED_WITH + EQUAL);
			relation = EQUAL;			
		}
		
		if (!SUPPORTED_POSITION_ATTRIBUTES.contains(position.hashCode()))
		{
			LOGGER.debug("Un unsupported position attribute has been found (" + position + IT_WILL_REPLACED_WITH + ANY_POSITION_IN_FIELD);
			position = ANY_POSITION_IN_FIELD;			
		}

		if (!SUPPORTED_STRUCTURE_ATTRIBUTES.contains(structure.hashCode()))
		{
			LOGGER.debug("Un unsupported structure attribute has been found (" + structure + IT_WILL_REPLACED_WITH + WORD);
			position = WORD;			
		}
		
		if (!SUPPORTED_TRUNCATION_ATTRIBUTES.contains(truncation.hashCode()))
		{
			LOGGER.debug("Un unsupported truncation attribute has been found (" + truncation + IT_WILL_REPLACED_WITH + NO_TRUNCATION);
			position = NO_TRUNCATION;			
		}

		if (!SUPPORTED_COMPLETENESS_ATTRIBUTES.contains(completeness.hashCode()))
		{
			LOGGER.debug("Un unsupported completeness attribute has been found (" + completeness + IT_WILL_REPLACED_WITH + COMPLETE_SUBFIELD);
			position = COMPLETE_SUBFIELD;			
		}
		
		int strategyId = new StringBuilder().append(relation).append(position).append(structure).append(truncation).append(completeness).toString().hashCode();
		
		LOGGER.debug(relation + COMMA + position + COMMA + structure + COMMA + truncation + COMMA + completeness);

		QueryStrategy strategy = STRATEGIES.get(strategyId);
		return strategy;
	}
}
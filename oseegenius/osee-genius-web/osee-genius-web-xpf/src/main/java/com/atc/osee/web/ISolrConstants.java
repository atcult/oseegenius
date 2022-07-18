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
package com.atc.osee.web;

/**
 * Eumeration of SOLR names.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface ISolrConstants
{
	String COLLECTION_CODE_FIELD_NAME = "collection_code";	
	String PUBLICATION_DATE_FIELD_NAME = "publication_date";
	String PUBLICATION_DATE_INTERVALS_FIELD_NAME = "publication_date_interval";
	String DEWEY_1_DIGIT_FIELD_NAME = "dewey_1digit";
	String GEOGRAPHIC_SUBJECT = "geographic_subject";
	String TOPIC = "topic";
	String MAIN_LIBRARY = "ml_id";
	String FULLTEXT = "text";
	String HIGHLIGHT = "highlighting";
	String DEWEY_FIELD_NAME = "dewey";
	String COPIES_FIELD_NAME = "copies";
	String ID_FIELD_NAME = "id";
	String DOCUMENTI_URI = ID_FIELD_NAME;
	String TITLE_FIELD_NAME = "title";
	String PUBLISHER_FIELD_NAME = "publisher";
	String AUTHOR_PERSON_FIELD_NAME = "author_person";
	String AUTHOR_CORPORATION_FIELD_NAME = "author_corporate";
	String AUTHOR_MEETING_FIELD_NAME = "author_conference";
	String EDITION_FIELD_NAME = "edition";
	String LANGUAGE_FIELD_NAME = "language";
	String FORMAT_FIELD_NAME = "format";
	String BIBLIOGRAPHIC_LEVEL_FIELD_NAME = "bibliographic_level";
	String CONTENT_TYPE_FIELD_NAME = "content_type";
	String OTHER_AUTHOR_PERSON_FIELD_NAME = "other_author_person";
	String OTHER_AUTHOR_CORPORATION_FIELD_NAME = "other_author_corporate";
	String OTHER_AUTHOR_MEETING_FIELD_NAME = "other_author_conference";
	String DEWEY_COMPLETE_FIELD_NAME = "dewey_complete";
	String ADDITIONAL_TITLE_FIELD_NAME = "additional_title";
	String PHISICAL_DESCRIPTION_ENG_FIELD_NAME = "physical_description_eng";
	String ISBN_FIELD_NAME = "isbn";
	String ISSN_FIELD_NAME = "issn";
	String SUBJECT_FIELD_NAME = "subject";
	String PART_OF_FIELD_NAME = "is_part_of";
	String FORMATTED_CONTENT_NOTES_ENG_FIELD_NAME = "formatted_content_note";
	String NOTES_ENG_FIELD_NAME = "note";
	String CATEGORY_CODE = "category_code";
	String BIBLIOGRAPHIC_CATALOGUE = "bc";
	String PREFERRED_LABEL = "prefLabel";
	
	String ALL = "*:*";
	String FACET_LIMIT_QUERY_PARAMETER = "facet.limit";
	
	// AG
	String HOMEPAGE_QUERY_TYPE_NAME = "hp";
	String STANDARD_NO_DISMAX_QUERY_TYPE_NAME = "def";
	String LIMITS_QUERY_TYPE_NAME = "limits";
	String MORE_LIKE_THIS_QUERY_TYPE_NAME = "/mlt";
	String DETAILS_QUERY_TYPE_NAME = "/doc";
	String ANY_KEYWORD_SEARCH_QUERY_TYPE_NAME = "any";
	String ADVANCED_SEARCH_QUERY_TYPE_NAME = "adv";
	
	String BROWSING_INDEX_NAME = "i";
	String BROWSING_FROM = "from";
	String DIRECTION = "d";
	
	// AG	
	String AND = "AND";
	
	String MORE_LIKE_THIS_MATCH = "match";
	//???
	String VALUE_PARAMETER_NAME = "value"; 

	String HOLDINGS_COMPONENT_NAME = "holdings";
	
	String  MARC_XML_FIELD_NAME = "marc_xml";
	String  MARC_21_FIELD_NAME = MARC_XML_FIELD_NAME;
	String  SMALL_COVER_URL = "small_cover_url";
	String  MEDIUM_COVER_URL = "medium_cover_url";
	String BIG_COVER_URL = "big_cover_url";
	String POSITION_IN_RESPONSE = "pos";
	String LOGICAL_VIEW = "logical_view";	
	String COPY_STATEMENT = "copy-statement";
	String GROUPED_ITEMS = "groupedItems";	
	
	String AUTHORITY_ID = "authority_group_hdg_id";
}
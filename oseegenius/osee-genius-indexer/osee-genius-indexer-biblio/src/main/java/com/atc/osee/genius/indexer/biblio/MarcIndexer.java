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
package com.atc.osee.genius.indexer.biblio;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.Indexer;
import com.atc.osee.genius.indexer.IndexerResult;
import com.atc.osee.genius.indexer.IndexingException;
import com.atc.osee.genius.indexer.WrongConfigurationException;
import com.atc.osee.genius.indexer.asynch.Data;
import com.atc.osee.genius.indexer.asynch.Dequeuer;
import com.atc.osee.genius.indexer.biblio.browsing.ControlledBrowsingIndexer;
import com.atc.osee.genius.indexer.biblio.browsing.IAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;
import com.atc.osee.genius.indexer.biblio.browsing.ISortKeyStrategy;
import com.atc.osee.genius.indexer.biblio.browsing.filter.impl.DummyHeadingFilter;
import com.atc.osee.genius.indexer.biblio.browsing.keys.impl.AmicusSortKeyStrategy;
import com.atc.osee.genius.indexer.biblio.handlers.*;
import com.atc.osee.genius.indexer.biblio.log.MessageCatalog;

/** 
 * Osee Genius -I- plugin that enables MARC21 and MARCXML record indexing.
 * Acting (indirectly) as a SOLR handler extension, it is configured using standard SOLR procedures 
 * and invoked (at the moment) using the SOLR REST interface.
 * 
 * @author agazzarini
 * @since 1.0
 */
public final class MarcIndexer extends Indexer implements IConstants
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MarcIndexer.class);
	
	private static final String BIBLIO = "biblio";
	
	private static final String VALUE = "value";
	private static final String VALUE_WITH_SEPARATOR = "valueWithSeparator";	
	private static final String COMPOUND_VALUE = "compoundValue";
	private static final String COMPOUND_VALUE_UNIMARC = "compoundValueUnimarc";
	private static final String ALPHA_COMPOUND_VALUE = "alphaCompoundValue";	
	private static final String COMPOUND_MULTIPLE_VALUE = "compoundMultipleValue";
	
	private static final String PUBLISHERS = "publishers";
	private static final String PUBLISHERS_COMPOUND_VALUE = "publishersCompoundValue";	
	
	private static final String REMOVE_TRAILING_PUNCTUATION_LINK_TITLE = "removeTrailingPunctuationLinkTitle";
	private static final String REMOVE_TRAILING_PUNCTUATION = "removeTrailingPunctuation";
	private static final String REMOVE_PARTIAL_PUNCTUATION = "removePartialPunctuation";
	private static final String REMOVE_TRAILING_PUNCTUATION_ASTERISK = "removePunctAndAstersk";
	private static final String BIBLIOGRAPHIC_LEVEL = "bibliographicLevel";
	private static final String BIBLIOGRAPHIC_LEVEL2 = "bibliographicLevel2";
	private static final String IMSS_BIBLIOGRAPHIC_LEVEL = "imssbibliographicLevel";
	private static final String ISBNS = "isbns";
	private static final String ISSNS = "issns";
	private static final String FORMATS = "format";
	private static final String CBT_FORMATS = "cbtformat";	
	private static final String TO_MARC_XML = "toMarcXmlButRemove";
	private static final String TO_MARC_21 = "toMarc21ButRemove";	
	private static final String ALPHA = "alpha";
	private static final String ALPHA_WITH_INDICATOR = "alphaWith2thIndicator";	
	private static final String ALPHA_WITH_INDICATOR_PUBLISHER = "alphaWith2thIndicatorPublisher";
	private static final String ALPHA_WITH_INDICATOR_PUBLISHER_NO_PUNCT = "alphaWith2thIndicatorPubNoPunct";
	private static final String LINK = "link";
	private static final String SORT_TITLE = "sortTitle";
	private static final String SORT_TITLE_WITHOUT_UNIFORM_TITLE = "sortTitleWoUniformTitle";
	private static final String SORT_AUTHOR = "sortAuthor";
	private static final String SORT_UNIMARC_AUTHOR = "sortUnimarcAuthor";
	private static final String SORT_DISCIPLINE = "sortDiscipline";
	private static final String SORT_PUBLICATION_DATE = "sortPublicationDate";
	private static final String PUBLICATION_DATE_INTERVALS = "publicationDateIntervals";
	private static final String SHORT_PUBLICATION_DATE_INTERVALS = "shortPublicationDateIntervals";
	private static final String CONCATENATE_WITH_DOUBLE_MINUS = "concatenateWithDoubleMinus";
	private static final String CONCATENATE_WITH_SINGLE_MINUS = "concatenateWithSingleMinus";
	private static final String TEXT_WITH_SUFFIX = "text";
	private static final String FIRST_CHARACTER = "firstCharacter";
	private static final String CLASSIFICATION_HIERARCHY = "decimalClassificationHierarchy";
	private static final String CLEAN_DATE = "cleanDate";
	private static final String ATTACHMENT = "attachment";
	private static final String LITERAL = "literal";
	private static final String TRIGRAM = "trigram";
	private static final String REMOVE_LEADING_ZEROS = "removeLeadingZeros";
	private static final String NUMERIC_STRING = "nstring";
	private static final String GROUP_LANGUAGES = "groupLanguages";
	private static final String DISTINCT = "distinct";	
	private static final String AUTOCOMPLETE = "autocomplete";		
	private static final String REMOTE_RESOURCE_CONTENT = "remoteResourceContent";		
	private static final String AUTHORITY_ACCESS_OBJECT = "aao";		
	private static final String FIRST_VALUE = "first";		
	private static final String SECOND_VALUE = "second";		
	private static final String THIRD_VALUE = "third";
	private static final String AS_DATE = "asDate";	
	private static final String CESVOT_SUBJECT = "cesvotSubject";		
	private static final String INDEX_TIME = "indexTime";
	private static final String DEFAULT_BROWSER_DATASOURCE_NAME = "jdbc/browser";		
	private static final String GENERATE_GROUP_ID = "groupId";		
	private static final String CGT = "cgt";		
	private static final String FIRST_NUMERIC_VALUE_FOR_EACH_TAG = "firstNumericValueForEachTag";		

	private static final String SKIP_IN = "skipIn";		
	private static final String ALPHA_SKIP_IN = "alphaSkipIn";		
	private static final String LABEL_AND_SKEPT_VALUE = "labelAndSkeptValue";		
	private static final String ALL = "all";
	private static final String ALL_WITH_INDICATOR = "allWith2thIndicator";	
	private static final String EXPAND_DATE = "expandDate";	
	private static final String FILTEREDLINK = "filteredLink";
	private static final String CBT_856_NOTE = "cbt856note";
	private static final String ALL_WITH_INDICATOR_BUT_ALL_INDICATED = "allWithButAllIndicated2thIndicator";	
	private static final String IDVALUE = "idValue";
	private static final String NUMERIC_STRING2 = "nstring2";
	private static final String CRO_FORMATS = "croformat";	
	private static final String URL_TOKENIZER= "urlTokenizer";	
	private static final String ALPHA_MULTILANGUAGE = "alphaMultiLang";
	private static final String ALPHA_MULTILANGUAGE_SUBJ_PERS = "alphaMultiLangSubPerson";
	private static final String ALPHA_MULTILANGUAGE_ALL = "alphaMultiLangAll";
	private static final String ALPHA_MULTILANGUAGE_ASTERISK = "alphaMultiLangAsterisk";
	private static final String IMSS_FORMATS = "imssformat";
	private static final String SPECIAL_VALUE = "specialValue";
	private static final String SPECIAL_STARTING_VALUE = "specialStartingValue";
	private static final String IMSS_FORMATS_NO_ICON = "ImssFormatNoIcon";
	private static final String ALPHA_WITH_IND = "alphaWithIndicator";
	private static final String OLSHEADING = "olsHeading";
	private static final String VALUE_FOR_IMSS = "valueImss";
	private static final String VALUE_FOR_IMSS_AUTH = "valueImssAuth";
	private static final String REMOVE_OLS = "removeOls";
	private static final String ORDERED_VALUE = "orderedValue";
	private static final String VALUE_DUPLICATED = "duplicatedValue";
	private static final String FIX_PUNCTUATION = "fixPunctuation";
	private static final String SKIP_IN_UNIMARC = "skipInUnimarc";
	private static final String UNIMARC_ISSN = "unimarcIssn";
	private static final String SORT_UNIMARC_TITLE= "sortUnimarcTitle";
	private static final String SORT_PUBLICATION_UNIMARC_DATE = "sortPublicationUnimarcDate";
	private static final String ONLY_FROM_FIX_POSITIONS = "onlyFromFixPositions";
	private static final String HEADING_VALUE = "headingValue";
	private static final String HEADING = "heading";
	private static final String PUBLISHERS_UNIMARC = "publishersUnimarc";
	private static final String CONCAT = "concat";
	private static final String ARC_DECORATOR = "arcDecorator";
	private static final String BIB_DECORATOR = "bibDecorator";
	private static final String NOW = "now";
	private static final String VALUE_DATE_PUNCTUATION = "valueDatePunctuation";
	private static final String VALUE_DATE_PUNCTUATION_2 = "valueDatePunctuation2";
	private static final String VALUE_ORDERED = "valueOrdered";
	private static final String OLISUITE_URI = "olisuiteUri";
	private static final String CONDITIONAL_VALUE = "conditionalValue";
	private static final String GET_ARCHIVE_FOND = "getArchiveFond";
	
	private File biblioDirectory;
	
	private SolrParams settings;
	
	private final IHeadingFilter dummyHeadingFilter = new DummyHeadingFilter();
	private final ISortKeyStrategy defaultSortKeyStrategy = new AmicusSortKeyStrategy();

	private static final ITagHandler DUMMY_TAG_HANDLER = new NullObjectTagHandler();
	private static final IDecorator DUMMY_VALUE_HANDLER = new NullObjectValueHandler();
	
	private static final List<IDecorator> EMPTY_CHAIN = new ArrayList<IDecorator>(0);
	
	private boolean synch;
	private boolean useEmbeddedDatabase;
	
	final Map<String, CompiledExpression> expressions = new HashMap<String, CompiledExpression>();
	
	/** Browse and Autocomplete are dependent, i.e. must be processed only after main document has been successfully indexed. */
	final Map<String, CompiledExpression> dependentExpressions = new HashMap<String, CompiledExpression>();
	
	private Map<String, ITagHandler> tagHandlers = new HashMap<String, ITagHandler>();
	{
		tagHandlers.put(ALPHA_WITH_INDICATOR, new GetAllAlphabeticalSubfieldsBy2thIndicator());
		tagHandlers.put(ALPHA_WITH_INDICATOR_PUBLISHER, new GetAllAlphabeticalSubfieldsBy2thIndicatorPublishers());
		tagHandlers.put(ALPHA_WITH_INDICATOR_PUBLISHER_NO_PUNCT, new GetAllAlphabeticalSubfieldsBy2thIndicatorNoPunct());
		tagHandlers.put(DISTINCT, new DistinctValue());
		tagHandlers.put(VALUE, new GetValue());
		tagHandlers.put(VALUE_FOR_IMSS, new GetValueForImss());
		tagHandlers.put(VALUE_FOR_IMSS_AUTH, new GetValueImssAuth());
		tagHandlers.put(FIRST_NUMERIC_VALUE_FOR_EACH_TAG, new GetFirstNumericValueForEachTag());
		tagHandlers.put(VALUE_WITH_SEPARATOR, new ValueWithCustomSeparator());
		tagHandlers.put(COMPOUND_VALUE, new GetCompoundValue());
		tagHandlers.put(COMPOUND_VALUE_UNIMARC, new GetCompoundValueUnimarc());
		tagHandlers.put(COMPOUND_MULTIPLE_VALUE, new GetCompoundMultipleValue());
		tagHandlers.put(ALPHA_COMPOUND_VALUE, new AlphaGetCompoundValue());
		tagHandlers.put(ISBNS, new GetIsbns());
		tagHandlers.put(ISSNS, new GetIssns());
		tagHandlers.put(FORMATS, new GetFormats());
		tagHandlers.put(CBT_FORMATS, new CbtFormats());
		tagHandlers.put(BIBLIOGRAPHIC_LEVEL, new GetBibliographicLevel());
		tagHandlers.put(BIBLIOGRAPHIC_LEVEL2, new GetBibliographicLevel2());
		tagHandlers.put(IMSS_BIBLIOGRAPHIC_LEVEL, new ImssGetBibliographicLevel());
		tagHandlers.put(TO_MARC_XML, new ToMarcXmlWithRemoval());
		tagHandlers.put(TO_MARC_21, new ToMarc21WithRemoval());
		tagHandlers.put(ALPHA, new GetAlphabeticalSubfields());
		tagHandlers.put(LINK, new GetLink());
		tagHandlers.put(SORT_TITLE, new GetSortTitle());
		tagHandlers.put(SORT_DISCIPLINE, new GetSortDiscipline());
		tagHandlers.put(SORT_TITLE_WITHOUT_UNIFORM_TITLE, new GetSortTitleWithoutUniformTitle());
		tagHandlers.put(SORT_AUTHOR, new GetSortAuthor());		
		tagHandlers.put(CONCATENATE_WITH_DOUBLE_MINUS, new ConcatenateWithDoubleMinus());
		tagHandlers.put(CONCATENATE_WITH_SINGLE_MINUS, new ConcatenateWithSingleMinus());
		tagHandlers.put(TEXT_WITH_SUFFIX, new TextHandler());
		tagHandlers.put(SORT_PUBLICATION_DATE, new GetPublicationDateSort());
		tagHandlers.put(CLASSIFICATION_HIERARCHY, new AssignClassificationHierarchyVerbalExpressions());		
		tagHandlers.put(ATTACHMENT, new GetHttpUrl());		
		tagHandlers.put(LITERAL, new Literal());		
		tagHandlers.put(PUBLISHERS, new GetPublishers());		
		tagHandlers.put(PUBLISHERS_COMPOUND_VALUE, new GetPublishersCompoundValue());		
		tagHandlers.put(REMOTE_RESOURCE_CONTENT, new GetRemoteResourceContentHandler());	
		tagHandlers.put(CESVOT_SUBJECT, new CesvotSubject());	
		tagHandlers.put(INDEX_TIME, new GetIndexTime());	
		tagHandlers.put(SKIP_IN, new SkipIn());	
		tagHandlers.put(ALPHA_SKIP_IN, new GetAlphabeticalSubfieldsWithSkip());	
		tagHandlers.put(LABEL_AND_SKEPT_VALUE, new LabelAndSkeptValue());
		tagHandlers.put(ALL,  new GetAllSubfields());
		tagHandlers.put(ALL_WITH_INDICATOR, new GetAllSubfieldsBy2thIndicator());
		tagHandlers.put(FILTEREDLINK,  new FilteredLink());
		tagHandlers.put(CBT_856_NOTE,  new Cbt856NoteGetValue());
		tagHandlers.put(ALL_WITH_INDICATOR_BUT_ALL_INDICATED,  new GetAllSubfieldsButAllIndicatedBy2thIndicator());
		tagHandlers.put(IDVALUE,  new GetIdValue());
		tagHandlers.put(CRO_FORMATS, new CroFormats());
		tagHandlers.put("titleHeading", new TitleBrowseHeading());
		tagHandlers.put("ntTitleHeading", new NTTitleBrowseHeading());
		tagHandlers.put("ntNameHeading", new NTNameBrowseHeading());
		tagHandlers.put("ftp_bibliographic_level", new FTPBibliographicLevel());
		tagHandlers.put("ftp_content_type", new FTPContentType());
		tagHandlers.put("ftp_249", new FTP249());
		tagHandlers.put("ftp_lcd", new FTPLastChangeDate());
		tagHandlers.put("ftp_permalink", new FTPPermalink());
		tagHandlers.put("ftp_abstract", new FTPGetAbstract());
		tagHandlers.put("ftp_fulltext", new FTPFulltext());
		tagHandlers.put("ftp_links", new FTPLinks());
		tagHandlers.put("ftp_external_provider_link", new FTPExternalProviderLink());
		tagHandlers.put("ftp_document_type", new FTPDocumentType());
		tagHandlers.put("ftp_callnum_letters_facet", new FTPCallNumberLettersFacetField());
		tagHandlers.put("ftp_callnum_facet", new FTPCallNumberFacetField());
		tagHandlers.put("ftp_inferred_keywords", new FTPInferredKeywords());
		tagHandlers.put("ftp_sort_author", new FTPSortAuthor());
		tagHandlers.put("ftp_autocomplete", new FTPAutocomplete());
		tagHandlers.put("ftp_anvur_display", new AnvurDisplay());
		tagHandlers.put("ftp_anvur_facet", new AnvurFacet()); 
		tagHandlers.put(ALPHA_MULTILANGUAGE, new GetAlphaMultilanguageSubfields());
		tagHandlers.put(ALPHA_MULTILANGUAGE_SUBJ_PERS, new GetAlphaMultilanguageSubfieldsSubjPers());
		tagHandlers.put(ALPHA_MULTILANGUAGE_ALL, new GetAlphaMultilanguageSubfieldsAll());
		tagHandlers.put(ALPHA_MULTILANGUAGE_ASTERISK, new GetAlphaMultilanguageSubfieldsAsterisk());
		tagHandlers.put(IMSS_FORMATS, new ImssGetFormats());
		tagHandlers.put(SPECIAL_VALUE, new GetSpecialValue());
		tagHandlers.put(SPECIAL_STARTING_VALUE, new GetSpecialStartingValue());
		tagHandlers.put(IMSS_FORMATS_NO_ICON, new ImssGetFormatsNoIcon());
		tagHandlers.put(ALPHA_WITH_IND, new GetAlphaSubfieldsBy2ndIndicator());		
		tagHandlers.put(ORDERED_VALUE, new GetOrderedValue());
		tagHandlers.put(VALUE_DUPLICATED, new GetValueDuplicated());
		tagHandlers.put(SKIP_IN_UNIMARC, new SkipInUnimarc());
		tagHandlers.put(UNIMARC_ISSN, new GetUnimarcIssns());
		tagHandlers.put("barcodeBNCF", new GetBarcodeBNCF());
		tagHandlers.put("collocationBNCF", new GetCollocationBNCF());
		tagHandlers.put("concatenateWithDoubleMinusAndId", new ConcatenateWithDoubleMinusWithId());
		tagHandlers.put("concatenateWithSingleMinusAndId", new ConcatenateWithSingleMinusWithId());
		tagHandlers.put(SORT_UNIMARC_TITLE, new GetSortUnimarcTitle());
		tagHandlers.put(SORT_UNIMARC_AUTHOR, new GetSortUnimarcAuthor());
		tagHandlers.put(SORT_PUBLICATION_UNIMARC_DATE, new GetPublicationUnimarcDateSort());
		tagHandlers.put(ONLY_FROM_FIX_POSITIONS, new GetValuesOnlyFromFixPositionAndNullifAllBlank());
		tagHandlers.put("display_title", new UnimarcTitleBNCF());
		tagHandlers.put(HEADING_VALUE, new GetHeadingValue());
		tagHandlers.put(PUBLISHERS_UNIMARC, new GetPublishersUnimarc());
		tagHandlers.put("inventoryBNCF", new GetInventoryBNCF());
		tagHandlers.put("ValueWithSubFilter", new GetValueWithSubFilter());
		tagHandlers.put("distinctIndic", new DistinctValueWithTwoIndicators());
		tagHandlers.put(CONCAT, new Concat());		
		tagHandlers.put(NOW, new Now());
		tagHandlers.put(VALUE_DATE_PUNCTUATION, new GetValueDatePunct());
		tagHandlers.put(VALUE_DATE_PUNCTUATION_2, new GetValueDatePunct2());
		tagHandlers.put(VALUE_ORDERED, new GetValueOrdered());
		tagHandlers.put(OLISUITE_URI, new GetUriOls());
		tagHandlers.put(CONDITIONAL_VALUE, new GetConditionalValue());
		tagHandlers.put(GET_ARCHIVE_FOND, new GetArchiveFacet());
	}

	// Keep track separately 
	private CreateBrowsingIndex browsingIndexer;
	
	private Autocomplete autocompleteIndexer = new Autocomplete();
	private Heading headingIndexer = new Heading();
	
	
	private Map<String, IDecorator> valueHandlers = new HashMap<String, IDecorator>();
	{
		valueHandlers.put(REMOVE_TRAILING_PUNCTUATION_LINK_TITLE, new RemoveTrailingPunctuationLinkTitle());
		valueHandlers.put(REMOVE_TRAILING_PUNCTUATION, new RemoveTrailingPunctuationTagHandler());
		valueHandlers.put(REMOVE_PARTIAL_PUNCTUATION, new RemovePartialPunctuationTagHandler());
		valueHandlers.put(REMOVE_TRAILING_PUNCTUATION_ASTERISK, new RemoveTrailingPunctuationAstTagHandler());		
		valueHandlers.put(FIRST_CHARACTER, new GetFirstCharacter());
		valueHandlers.put(CLEAN_DATE, new CleanDate());
		valueHandlers.put(TRIGRAM, new Trigram());
		valueHandlers.put(GROUP_LANGUAGES, new GroupLanguages());
		valueHandlers.put(REMOVE_LEADING_ZEROS, new RemoveLeadingZeros());		
		valueHandlers.put(NUMERIC_STRING, new NumericString());			
		valueHandlers.put(FIRST_VALUE, new FirstValue());			
		valueHandlers.put(SECOND_VALUE, new SecondValue());
		valueHandlers.put(THIRD_VALUE, new ThirdValue());
		valueHandlers.put(AS_DATE, new AsDate());	
		valueHandlers.put(PUBLICATION_DATE_INTERVALS, new GetPublicationDateIntervalFacets());
		valueHandlers.put(SHORT_PUBLICATION_DATE_INTERVALS, new GetShortPublicationDateIntervalFacets());
		valueHandlers.put(AUTOCOMPLETE, autocompleteIndexer);
		valueHandlers.put(HEADING, headingIndexer);
		valueHandlers.put(GENERATE_GROUP_ID, new GenerateGroupId());		
		valueHandlers.put(CGT, new Cgt());	
		valueHandlers.put(EXPAND_DATE, new ExpandDate());	
		valueHandlers.put(NUMERIC_STRING2, new NumericString2());
		valueHandlers.put(URL_TOKENIZER, new URLTokenizer());		
		valueHandlers.put("remove_minus", new RemoveMinus());
		valueHandlers.put("ftp_date_intervals", new FTPPubDateIntervals());	
		valueHandlers.put("stars", new Stars());	
		valueHandlers.put("dateAndTimeOfLatestTransaction", new toUTC());
		valueHandlers.put(OLSHEADING, new OlsHeading());
		valueHandlers.put(REMOVE_OLS, new RemoveOLS());
		valueHandlers.put("removePunctuationAll", new RemovePunctuationAllTagHandler() );
		valueHandlers.put(FIX_PUNCTUATION, new FixPunctuation());
		valueHandlers.put("lessOrGreaterThan", new RemoveLessAndGreaterThan());
		valueHandlers.put("detectYearOrRange", new DetectYearOrYearRange());	
		valueHandlers.put("lowerCase", new ToLowerCase());	
		valueHandlers.put("findYear", new GetFirstAvailableYearValue());	
		valueHandlers.put(ARC_DECORATOR, new ArcDecorator());
		valueHandlers.put(BIB_DECORATOR, new BibDecorator());
	}
			
	/**
	 * Deletes the record associated with URI in incoming file.
	 * After that, if there are no errors, moves the file to worked our dir.
	 * 
	 * @param file the file that contains deleted records.
	 */
	private void deleteRecords(final File file)
	{
		BufferedReader reader = null;
		try 
		{
			reader = new BufferedReader(new FileReader(file));
			String documentUri = null;
			
			int deletedCount = 0;
			while ( (documentUri = reader.readLine()) != null)
			{
				if (documentUri.trim().length() != 0)
				{		
					if("\"BIB\"".equals(requestParams.get("logicalView"))) {
						documentUri = documentUri + "BIB";
					}					
					indexer.deleteById(documentUri);
					deletedCount++;
				}
			}
			
			LOGGER.error("Deleting " + deletedCount + " entries from index.");
						
			indexer.commit(true,true,false);
			 
			moveToWorkedDirectory(file);
		} catch (Exception exception) 
		{
			LOGGER.error("Error while deleting documents from index.", exception);
		} 
	}
	
	/**
	 * Indexes all the recognized source files found 
	 * within the configured source directory.
	 * 
	 * @return the indexer result.
	 * @throws IndexingException in case the indexing process fails.
	 */
	public IndexerResult doIndex() throws IndexingException
	{	
		final IndexerResult result = new IndexerResult("marc-indexer");
		try 
		{
			if (request.getContentStreams() != null) {
				synch = true;
			}

			
			Properties solr2MarcSettings = new Properties();
			
			//this variable is used to fix problem when records are in unimarc and utf8 
			boolean isUnimarcUtf8 = requestParams.getBool("unimarc_utf") != null ? requestParams.getBool("unimarc_utf") : false;
			
			//this variable is used for marc-8 encoding
			boolean isMarc_8 = requestParams.getBool("marc_8") != null ? requestParams.getBool("marc_8") : false;
			//this variable is used to know what kind of punctuator I need to use
			Boolean isMarc21 =  requestParams.getBool("marc21") != null ? requestParams.getBool("marc21")  : true;
			
			solr2MarcSettings.load(core.getResourceLoader().openResource(requestParams.get("mappings")));
			loadExpressionMap(solr2MarcSettings);
			solr2MarcSettings = null;

			if (synch)
			{
				for (ContentStream stream : request.getContentStreams())
				{
					if (stream != null) {
						indexSynch(stream.getStream(), solr2MarcSettings, result, 1);			
					}
				}
			}
			
			for (final File file : biblioDirectory.listFiles(new FilenameFilter() 
			{
				@Override
				public boolean accept(File file, String filename) 
				{
					return !filename.endsWith(".mrc");
				}
			}))
			{
				LOGGER.info("Found " + file + " which is supposed to contain deleted records.");			
				
				deleteRecords(file);
				
				LOGGER.info("File " + file + " has been worked out.");
			}
						
			for (final File file : biblioDirectory.listFiles(new FilenameFilter() 
			{
				@Override
				public boolean accept(File file, String filename) 
				{
					return filename.endsWith(".mrc");
				}
			}))
			{
				int totalCount = count(file);
			
				LOGGER.info("File " + file + ": " + totalCount + " MARC record to index.");
				LOGGER.info("File " + file + " indexing process starts...");
				
				index(file, solr2MarcSettings, result, totalCount, isUnimarcUtf8, isMarc21, isMarc_8);
				
				LOGGER.info("File " + file + " indexing has been completed.");
			}
		} catch (IOException exception)
		{
			throw new IndexingException("I/O exception while loading OG2MRC configuration.", exception);
		} finally 
		{
			if (browsingIndexer != null)
			{
				try {  browsingIndexer.release(); } catch (Exception e) {}
			}
		}
		return result;
	}
	
	private final void loadExpressionMap(final Properties solr2MarcSettings) 
	{
		for (String field : solr2MarcSettings.stringPropertyNames())
		{
			final String expression = solr2MarcSettings.getProperty(field).trim();
			if (expression == null || expression.trim().length() == 0)
			{
				continue;
			}
			
			final String [] commandsAndArguments = getParsedMappingLine(expression);
			final String argument = getArgument(commandsAndArguments);
			
			final ITagHandler tagHandler = getTagHandler(commandsAndArguments);
			final List<IDecorator> chain = getValueHandlerChain(commandsAndArguments);
			
			if (isNotDependentField(field))
			{
				expressions.put(field, new CompiledExpression(argument, tagHandler, chain));
			} else 
			{
				dependentExpressions.put(field, new CompiledExpression(argument, tagHandler, chain));				
			}
		}
	}

	/**
	 * A field is not dependent if is not an autocomplete field.
	 * Those kind of fields needs to be processed only after the main document has been indexed.
	 * There's not reason to index an heading of a document that hasn't been indexed (the subsequent search
	 * will lead 0 results).
	 * 
	 * @param field the field to be checked.
	 * @return true if the field is not dependent.
	 */
	private final boolean isNotDependentField(final String field) 
	{
		return !(field.startsWith("autocomplete_"));// || field.endsWith("_browse"));
	}
	
	private final int count(final File file) 
	{	
		DataInputStream stream = null;
		int count = 0;
		try 
		{
			final byte[] leaderReusableBuffer = new byte[24];
			stream = new DataInputStream(new FileInputStream(file));
			while (stream.available() != 0)
			{
				stream.readFully(leaderReusableBuffer);
				final int recordLength = parseRecordLength(leaderReusableBuffer);
				stream.skipBytes(recordLength - 24);
				count++;
			}
			return count;
		} catch (Exception exception)
		{
			LOGGER.info("Unable to count total record in " + file, exception);
			return 0;
		} finally
		{
			try { stream.close();} catch (Exception ignore){}
		}
	}	
		
	/**
	 * Internal method that does the concrete indexing job.
	 * 
	 * @param file the marc source file (marc21 binary file or marc XML).
	 * @param solr2MarcSettings the solr 2 marc mappings file.
	 * @param result the indexer result acting as an accumulator.
	 * @throws IndexingException  in case the indexing fails.
	 */
	private void indexSynch(
			final InputStream inputStream, 
			final Properties solr2MarcSettings, 
			final IndexerResult result,
			final int howManyRecords)
	{		
		final long begin = System.currentTimeMillis();
		
		try
		{
			final MarcReader reader = new MarcStreamReader(inputStream);
			final AtomicInteger counter = new AtomicInteger(0);
			
			while (reader.hasNext())
			{
				final Record record = reader.next();
				
				final SolrInputDocument document = new SolrInputDocument();
				
				try 
				{							
					for (Entry<String, CompiledExpression> entry : expressions.entrySet())
					{
						String field = entry.getKey();
						CompiledExpression expression = entry.getValue();
						long start = System.currentTimeMillis();
						
						Object value = expression.tagHandler.getValue(expression.argument, record, settings, core, document);
						for (IDecorator decorator : expression.chain)
						{
							value = decorator.decorate(record, field, value);							
						}
												
						handleValue(document, field, value);
					}
					
					try 
					{
						indexDocument(document, result);	

						// dependent fields
						for (Entry<String, CompiledExpression> entry : dependentExpressions.entrySet())
						{
							String field = entry.getKey();
							CompiledExpression expression = entry.getValue();
							Object value = expression.tagHandler.getValue(expression.argument, record, settings, core, document);
							for (IDecorator decorator : expression.chain)
							{
								value = decorator.decorate(record, field, value);
							}
							
							handleValue(document, field, value);	
						}
						
					} catch (Exception mainDocumentNotIndexed)
					{
						// Ignore
					}			
				} catch (Exception exception)
				{
					LOGGER.error("Unable to create / manipulate the document associated with id " + document.getFieldValue("id") + ", reason: " + exception.getMessage());
				} finally 
				{
					if (counter.incrementAndGet() % 1000 == 0)
					{
						LOGGER.info("Indexed " + (counter.intValue() + " / " + howManyRecords + " in " + (System.currentTimeMillis() - begin) ));
					}
				}
			}
		} finally 
		{
			LOGGER.info(MessageCatalog._000010_MARC_INDEXER_ENDS);
		}
	}	
	
	/**
	 * Internal method that does the concrete indexing job.
	 * 
	 * @param file the marc source file (marc21 binary file or marc XML).
	 * @param solr2MarcSettings the solr 2 marc mappings file.
	 * @param result the indexer result acting as an accumulator.
	 * @throws IndexingException  in case the indexing fails.
	 */
	private void index(
			final File file, 
			final Properties solr2MarcSettings, 
			final IndexerResult result,
			final int howManyRecords,
			final Boolean isUnimarcUtf8,
			final Boolean isMarc21,
			final Boolean isMarc_8)
	{		
		DataInputStream inputStream = null;
		
		DataSource ds = null;
		try {
			Context ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/amicus"); 
		} catch (Exception ignore) {
			try {
				Context ctx = new InitialContext();
				ds = (DataSource) ctx.lookup("jdbc/amicus"); 				
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		final SynchronousQueue<Data> queue = new SynchronousQueue<Data>();		
		final Dequeuer dequeuer = new Dequeuer(
				queue, 
				howManyRecords, 
				expressions,settings, 
				core, 
				result, 
				dependentExpressions, 
				indexer, 
				requestParams.getInt("threads", Runtime.getRuntime().availableProcessors()), 
				ds, isUnimarcUtf8, isMarc21, isMarc_8);
		try 
		{
			dequeuer.start();
			inputStream = new DataInputStream(new FileInputStream(file));
			
			while (inputStream.available() != 0)
			{
				final byte[] leader = new byte[24];
				inputStream.readFully(leader);
				 
				final int recordLength = parseRecordLength(leader);
				final byte[] body = new byte[recordLength - 24];
				inputStream.readFully(body);
			
				queue.put(new Data(leader, body, recordLength));
			}
			
			while (dequeuer.isAlive())
			{
				Thread.sleep(1000);
			}
		} catch (Exception exception)
		{
			LOGGER.error("Fatal failure while indexing....", exception);
		} finally 
		{
			if (moveAfterComplete)
			{
				moveToWorkedDirectory(file);
			}		
			
			try { inputStream.close(); } catch (Exception e) { }
			if (useEmbeddedDatabase)
			{
				enableWal();
			}
		} 
	}
	
    private int parseRecordLength(byte[] leaderData) throws IOException {
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(
                leaderData));
        int length = -1;
        char[] tmp = new char[5];
        isr.read(tmp);
        try {
            length = Integer.parseInt(new String(tmp));
        } catch (NumberFormatException e) {
            throw new MarcException("unable to parse record length", e);
        }
        return(length);
    }
	
	/**
	 * Loads a tag handler.
	 * 
	 * @param commandsAndArguments from mapping file.
	 * @return the tag handler implementation that will handle the given mapping.
	 */
	private ITagHandler getTagHandler(final String[] commandsAndArguments) 
	{
		ITagHandler result = (commandsAndArguments != null && commandsAndArguments.length != 0) 
			? tagHandlers.get(commandsAndArguments[commandsAndArguments.length - 2]) 
            : DUMMY_TAG_HANDLER;
			
		return result != null ? result : DUMMY_TAG_HANDLER;	
	}

	/**
	 * Loads a value handler.
	 * 
	 * @param commandsAndArguments from mapping file.
	 * @return the value handler implementation that will handle the given mapping.
	 */
	private List<IDecorator> getValueHandlerChain(final String[] commandsAndArguments) 
	{
		int arraySize = commandsAndArguments.length;
		int howManyDecorators = arraySize - 2;
		if (howManyDecorators <= 0) 
		{
			return EMPTY_CHAIN;
		}
		
		List<IDecorator> chain = new ArrayList<IDecorator>(howManyDecorators);
		for (int i = (howManyDecorators - 1); i >= 0; i--)
		{
			IDecorator handler = valueHandlers.get(commandsAndArguments[i]);
			chain.add(handler != null ? handler : DUMMY_VALUE_HANDLER);
		}
		
		return chain;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void doConfigure(final SolrParams settings, final SolrCore core) throws WrongConfigurationException 
	{
		this.core = core;
		this.settings = settings;
		
		autocompleteIndexer.setSolr(new EmbeddedSolrServer(core.getCoreDescriptor().getCoreContainer(), "autocomplete"), "autocomplete");
		
		
		/**Authority**/
		Autocomplete autocompleteAuthIndexer = new Autocomplete();
		valueHandlers.put("autocomplete_auth", autocompleteAuthIndexer);
		
		if(authorityCoreIsEnabled(core.getCoreDescriptor().getCoreContainer())){
			 autocompleteAuthIndexer.setSolr(new EmbeddedSolrServer(core.getCoreDescriptor().getCoreContainer(), "autocomplete_auth"), "autocomplete_auth");
		}
		/**Authority**/	
	
		
		/** Heading **/
		if(headingCoreIsEnabled(core.getCoreDescriptor().getCoreContainer())) {
			headingIndexer.setSolr(new EmbeddedSolrServer(core.getCoreDescriptor().getCoreContainer(), "heading"), "heading");
		}
		/** Heading **/
		
		try 
		{
			this.biblioDirectory = new File(commonSettings.get("source.directory"), BIBLIO);
			if (!biblioDirectory.isDirectory() || !biblioDirectory.canRead())
			{
				throw new IllegalArgumentException("Bibliographic records directory is not a valid directory.");
			}
		} catch (Exception exception)
		{
			throw new WrongConfigurationException(exception);
		}
		
		synch = requestParams.getBool("synch", false);
		
		if (!synch)
		{
			LOGGER.info("Asynchronous indexer in use.");
		} else
		{
			LOGGER.info("Synchronous indexer in use.");			
		}
		
		// Browsing infrastructure setup
		NamedList<Object> browsingSettings = (NamedList<Object>) ((NamedList<Object>) globalConfiguration.get(BIBLIO)).get("browsing");
		if (browsingSettings != null)
		{
			browsingIndexer = new CreateBrowsingIndex();
			
			String logicalViewAsRequestParameter = requestParams.get("logicalView");
			if (logicalViewAsRequestParameter != null)
			{
				LOGGER.info("Logical View (request parameter) : " + logicalViewAsRequestParameter);		
				browsingIndexer.setLogicalViewExpression(logicalViewAsRequestParameter);				
			}
			
			try 
			{
				dummyHeadingFilter.init(core);
			} catch (Exception exception) 
			{
				// Nothing
			}
			
			DataSource datasource = null;
			
			Boolean useEmbeddedDatabaseParameter = (Boolean) browsingSettings.get("use-embedded-db");
			useEmbeddedDatabase = useEmbeddedDatabaseParameter == null || useEmbeddedDatabaseParameter;
			boolean walHasBeenDisabled = false;
			
			for (Entry<String, Object> entry :  browsingSettings)
			{
				String attributeName = entry.getKey();
				if ("logical-view-expression".equals(attributeName))
				{
					String logicalViewExpression = (String) entry.getValue();
					browsingIndexer.setLogicalViewExpression(logicalViewExpression);				
					continue;
				} else if ("datasource-jndi-name".equals(attributeName))
				{
					try 
					{
						Context namingContext = new InitialContext();
						datasource = (DataSource) namingContext.lookup((String) entry.getValue());
						datasource.getConnection().close();
					} catch (Exception exception)
					{
						throw new WrongConfigurationException(exception);
					} 
					continue;
				}
				
				if (useEmbeddedDatabase && datasource == null)
				{
					try 
					{
						Context namingContext = new InitialContext();
						datasource = (DataSource) namingContext.lookup(DEFAULT_BROWSER_DATASOURCE_NAME);
						datasource.getConnection().close();
					} catch (Exception exception)
					{
						throw new WrongConfigurationException("From 25/04/2012 OseeGenius Browsing uses an external database so property \"datasource-jndi-name\" must be configured.");						
					}
				} else if ("use-embedded-db".equals(attributeName))
				{
					// Just skip it (we already processed this attribute)
					continue;
				}
				
				NamedList<Object> attributeSettings = (NamedList<Object>) entry.getValue(); 

				
				try 
				{
					ControlledBrowsingIndexer indexer = new ControlledBrowsingIndexer();

					if (useEmbeddedDatabase)
					{
						// 1. Year 0: table for this attribute does not exist
						createTableForAttributeIfDoesntExist(attributeName, datasource);
						
						// 2. inject the datasource (we are using the embedded HSQL database)
						indexer.setDatasource(datasource);
						
						if (!walHasBeenDisabled)
						{
							// 3. disable WAL activity
							disableWal(datasource);
							walHasBeenDisabled = true;
						}
					}
					
					// 3. Loads and initialises Authority Access Object (AAO)
					IAuthorityAccessObject aao = (IAuthorityAccessObject) Class.forName((String) attributeSettings.get(AUTHORITY_ACCESS_OBJECT)).newInstance();
					aao.init(core, attributeSettings);

					// 4. Loads the appropriate heading filter
					IHeadingFilter filter = getHeadingFilter(attributeSettings, core);
					
					// 5. Loads the appropriate sort key strategy 
					ISortKeyStrategy sortKeyStrategy = getSortKeyStrategy(attributeSettings);
					
					// 6. Creates a reference to that SOLR core.
					SolrServer server = new EmbeddedSolrServer(core.getCoreDescriptor().getCoreContainer(), attributeName);
					
					browsingIndexer.add(attributeName, indexer, server, aao, filter, sortKeyStrategy);
					
					valueHandlers.put("createBrowsingIndex", browsingIndexer);
				} catch (Exception exception)
				{
					LOGGER.error(
							MessageCatalog._000014_UNABLE_TO_INITIALISE_AAO, 
							exception);
					throw new WrongConfigurationException(exception);
				}
			}			
		}
	}
	
	private boolean authorityCoreIsEnabled(CoreContainer core){
		
		boolean toReturn=false;

		Collection<String> coresNames = core.getAllCoreNames();
		Iterator<String> iterator = coresNames.iterator();
		while(iterator.hasNext()){
			if("auth".equals(iterator.next())){
				toReturn=true;
				break;
			}
		}
		
		return toReturn;
	}
	
	private boolean headingCoreIsEnabled(CoreContainer core) {
		boolean toReturn = false;
		Collection<String> coresNames = core.getAllCoreNames();
		Iterator<String> iterator = coresNames.iterator();
		while(iterator.hasNext()){
			if("heading".equals(iterator.next())){
				toReturn=true;
				break;
			}
		}
		
		return toReturn;
	}
	
	private final void disableWal(final DataSource datasource) 
	{
		Connection connection = null;
		PreparedStatement statement1 = null;
		PreparedStatement statement2 = null;
		PreparedStatement statement3 = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement1 = connection.prepareStatement("SET FILES LOG FALSE");
			statement2 = connection.prepareStatement("CHECKPOINT");
			statement3 = connection.prepareStatement("SET FILES WRITE DELAY 10000 MILLIS");
			statement1.executeUpdate();
			statement2.executeUpdate();
			statement3.executeUpdate();			
			LOGGER.info("WAL has been disabled on embedded database");
		} catch (SQLException exception) 
		{
			LOGGER.error("Cannot disable WAL on embedded database.", exception);
		} finally 
		{
			try { if (statement1 != null) statement1.close();} catch (Exception e) { }
			try { if (statement2 != null) statement2.close();} catch (Exception e) { }
			try { if (statement3 != null) statement3.close();} catch (Exception e) { }
			try { if (connection != null) connection.close();} catch (Exception e) { }
		}		
	}

	private void enableWal() 
	{
		Connection connection = null;
		PreparedStatement statement1 = null;
		PreparedStatement statement2 = null;
		
		try 
		{
			Context namingContext = new InitialContext();
			DataSource datasource = (DataSource) namingContext.lookup(DEFAULT_BROWSER_DATASOURCE_NAME);
			
			connection = datasource.getConnection();
			statement1 = connection.prepareStatement("SET FILES LOG TRUE");
			statement2 = connection.prepareStatement("CHECKPOINT DEFRAG");
			statement1.executeUpdate();
			statement2.executeUpdate();
			
			LOGGER.info("WAL has been reenabled on embedded database");
		} catch (Exception exception) 
		{
			LOGGER.error("Cannot enable WAL on embedded database.", exception);
		} finally 
		{
			try { if (statement1 != null) statement1.close();} catch (Exception e) { }
			try { if (statement2 != null) statement2.close();} catch (Exception e) { }
			try { if (connection != null) connection.close();} catch (Exception e) { }
		}		
	}
	/**
	 * Creates a table in internal database associated with the given (browsing) field name.
	 * The table will have the same name of the attribute.
	 * 
	 * @param fieldName the browsing field name.
	 * @param datasource the datasource.
	 */
	private void createTableForAttributeIfDoesntExist(
			final String attributeName,
			final DataSource datasource) throws Exception
	{
		Connection connection = null;
		Statement statement = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE " + attributeName + " (SORT_KEY VARCHAR(1000) PRIMARY KEY NOT NULL, LOGICAL_VIEW VARCHAR(5))");
		} catch (SQLException exception) 
		{
			if (exception.getErrorCode() != - 5504)
			{
				throw exception;
			}
		} finally 
		{
			try { if (statement != null) statement.close();} catch (Exception e) { }
			try { if (connection != null) connection.close();} catch (Exception e) { }
		}
	}

	private Map<String, IHeadingFilter> headingFiltersCache = new HashMap<String, IHeadingFilter>();
	
	/**
	 * Returns a heading filter according with given configuration settings.
	 * In case no or invalid value is found for that parameter the dummy (default)
	 * implementation is returned.
	 *  
	 * @param attributeSettings the settings of a given attribute.
	 * @param core the SOLR core.
	 * @return a heading filter according with given configuration settings.
	 */
	IHeadingFilter getHeadingFilter(final NamedList<Object> attributeSettings, final SolrCore core)
	{
		String clazzName = (String) attributeSettings.get("heading-filter");	
		if (clazzName == null || clazzName.trim().length() == 0)
		{
			return dummyHeadingFilter;
		}
		
		try 
		{
			IHeadingFilter filter = headingFiltersCache.get(clazzName);
			if (filter == null)
			{
				filter = (IHeadingFilter) Class.forName(clazzName).newInstance();
				filter.init(core);
				headingFiltersCache.put(clazzName, filter);
			}
			return filter;
		} catch (Exception exception)
		{
			LOGGER.error(
					"Unable to initialise the given heading filter: " + clazzName, 
					exception);			
			return dummyHeadingFilter;
		}
	}
	
	/**
	 * Returns a sort key strategy according with given configuration settings.
	 * In case no or invalid value is found for that parameter the default
	 * implementation is returned.
	 *  
	 * @param attributeSettings the settings of a given attribute.
	 * @return a sort key strategy with given configuration settings.
	 */
	ISortKeyStrategy getSortKeyStrategy(final NamedList<Object> attributeSettings)
	{
		String clazzName = (String) attributeSettings.get("sort-key-strategy");	
		if (clazzName == null || clazzName.trim().length() == 0)
		{
			return defaultSortKeyStrategy;
		}
		
		try 
		{
			return (ISortKeyStrategy) Class.forName(clazzName).newInstance();
		} catch (Exception exception)
		{
			LOGGER.error(
					"Unable to initialise the given sort key strategy: " + clazzName, 
					exception);			
			return defaultSortKeyStrategy;
		}
	}
}
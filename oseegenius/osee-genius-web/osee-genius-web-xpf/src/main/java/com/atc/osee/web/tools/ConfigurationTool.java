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
package com.atc.osee.web.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.SkipSetters;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.folio.FolioConstants;

/**
 * OseeGenius -W- instance configuration.
 * 
 * @author agazzarini
 * @since 1.0
 */
@DefaultKey("configuration")
@SkipSetters
@ValidScope(Scope.APPLICATION)
public class ConfigurationTool  extends SafeConfig
{
	private static final String BROWSABLE_SIMPLE_INDEXES_PARAMETER_NAME = "browsable-simple-indexes";
	private static final String BROWSABLE_AUTHORITY_INDEXES_PARAMETER_NAME = "browsable-authority-indexes";
	private static final String ALLOWED_PAGE_SIZES_PARAMETER_NAME = "allowed-page-sizes";
	private static final String DEFAULT_PAGE_SIZE_PARAMETER_NAME = "default-page-size";
	private static final String SEARCHABLE_FIELD_GROUPS_PARAMETER_NAME = "searchable-meta-attributes";
	private static final String ADVANCED_SEARCHABLE_FIELD_GROUPS_PARAMETER_NAME = "advanced-searchable-meta-attributes";
	private static final String QUOTED_META_ATTRIBUTES = "quoted-meta-attributes";
	private static final String ORDER_BY_CRITERIAS_PARAMETER_NAME = "order-by-meta-attributes";
	private static final String DEFAULT_ORDER_BY_CRITERIA_PARAMETER_NAME = "default-order-by-criteria";
	private static final String PESPECTIVES_ENABLED_PARAMETER_NAME = "view-perspectives-enabled";
	private static final String SENDER_ADDRESS_PARAMETER_NAME = "email-sender-address";
	private static final String RECEIVER_ADDRESS_PARAMETER_NAME = "email-receiver-address";
	private static final String DESIDERATA_ADDRESS_PARAMETER_NAME = "desiderata-email-address";
	private static final String FACET_LIMITS_PARAMETER_NAME = "facet-limits";
	private static final String CUSTOMER_CSS_PARAMETER_NAME = "customer-css";
	private static final String COMMUNITY_DATA_SHARED = "community-data-sharing-mode";
	private static final String DUMMY_ACCOUNT_ID_PARAMETER_NAME = "dummy-account-id";	
	private static final String LOAN_NOT_ALLOWED_CODES = "loan-not-allowed-codes";	
	private static final String IN_VM_HOSTNAME = "in-vm-hostname";
	private static final String IN_VM_PORT = "in-vm-port";
	private static final String OPENDATA_FILE_PATH = "opendata-file-path";
	private static final String AUTHORITY_ENABLED = "authority-enabled";
	private static final String BRANCHES_LOCATIONS = "branches-location";
	private static final String LIBRARY_DOMAIN_URL = "library-domain-url";
	private static final String DELETE_PDF_PARAMETER = "delete-pdf-enabled";
	
	//bug 5399
	private static final String PERMANENT_FILTER = "permanent-filter";
	
	private static final int [] EMPTY_INT_ARRAY = new int [0];
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int [] DEFAULT_PAGE_SIZES = {5, 10, 20};
	
	private static final String GOOGLE_RECAPTCHA_KEY = "goolgle-recaptcha-key";
		
	private static final String UPLOAD_FOLDER = "upload-folder";
	
	private static final String HOLDING_SBN_ENABLED = "holdings-sbn-enabled";
	private static final String NAVIGATION_DEWEY_ENABLED = "navigation-dewey-enabled";
	private static final String LIST_FILTER_DEWEY_EDITIONS ="list-filter-dewey-editions";
	private static final String SERVICE_POINT_PARAMETER_NAME = "printer-ip";
	private static final String APLUS_WEBSERVICE_URL = "aplus-service-url";
	private static final String APLUS_WEB_PRINT_PAGE = "aplus-print-card-page";
	private static final String SEBINA_SERVICE_URL = "sebina-service-url";
	private static final String LOAN_SERVICE_CLOSE = "loan-service-close";
	private static final String LIBRARY_CLOSE = "library-close";
	private static final String SAVE_PERMANENTLY_HISTORY_ENABLED = "save-permanently-history-enabled";
	private static final String SCHEMA_TO_CREATE = "create-schema";
	private static final String CUSTOM_FIELDS = "custom-fields";
	
	
	private Map<String, String []> searchableMetaAttributes = new LinkedHashMap<String, String[]>();
	private Map<String, String []> advancedSearchableMetaAttributes = new LinkedHashMap<String, String[]>();
	private String [] quotedMetaAttributes;
	
	private List<OrderByCriteria> orderByMetaAttributes = new ArrayList<OrderByCriteria>(5);
	
	private Map<String, String> orderByMap = new LinkedHashMap<String, String>();	
	private List<String> thGeniusOrderByMetaAttributesList = new ArrayList<String>();	
	
	private int [] allowedPageSizes;
	private int initialNumberOfAdvancedSearchFields;
	private String [] defaultWhereValuesForAdvancedFields;
	
	private int defaultPageSize;
	private String defaultOrderByCriteria;
	
	private List<Supportedi18nLanguage> supportedLanguages = new ArrayList<Supportedi18nLanguage>();
	private List<ExternalSearchProvider> searchProviders = new ArrayList<ExternalSearchProvider>();
	private List<ExternalSearchProvider> thGeniusTargetProviders = new ArrayList<ExternalSearchProvider>();

	private String [] browsableSimpleIndexes;
	private String [] browsableAuthorityIndexes;
	
	private boolean viewPerspectivesEnabled;
	
	private String senderAddress;
	private String receiverAddress;
	private String desiderataAddress;
	
	private boolean useLabelInsteadFlags;
	
	private String css;
	
	private boolean communityDataPrivate;
	
	private int dummyAccountId;
	private String [] loanNotAllowedCodes;
	
	private String inVmHost;
	private String inVmPort;
	
	private int [] disabledBranches;
	private int [] disabledMainLibraries;
	
	private String opendataFilePath;
	
	private String defaultLogicalView;
    private String [] logicalView;

	private boolean authEnabled;
	//bug 5399
	private boolean permanentFilter;
	
	private Map<Integer, List<Integer>> branchesLocation = new HashMap<Integer, List<Integer>>();
	
	private String googleRecaptchaKey;
	private String uploadFolder;
	
	private boolean holdingSBNEnabled;
	private boolean navigationDeweyEnabled;
	private String [] filterDeweyEd;
	private static final String [] DEFAULT_DEWEY_EDS = {"x", "23", "22"};
	private String aplusServiceUrl;
	private String aplusPrintCardPage;
	private String sebinaServiceUrl;
	private String libraryDomainUrl;
	
	private FolioConfigurationTool folioConfig;
	private String printFolder;
	private String printCommand;
	private int loanLimit;
	private boolean loanCountEnabled;
	private boolean deletePdfEnabled;
	private String loanServiceClose;
	private String libraryClose;
	private boolean saveHistory;
	private boolean schemaToCreate;
	private boolean useCustomFields;
	
	@Override
	protected void configure(final ValueParser values)
	{
		super.configure(values);
	
		configureBrowsing(values);
		configureAllowedPageSizes(values);
		configureSearchableFieldGroups(values);
		configureOrderBy(values);
		configureAdvancedSearch(values);
		configureExternalSearchProvider(values);
		configureThGenius(values);
		configureI18n(values);
		configureFacetLimits(values);
		configureBranchesLocation(values);	
		configureServicePoint(values);		
		
		useCustomFields=values.getBoolean(CUSTOM_FIELDS, false);
		schemaToCreate=values.getBoolean(SCHEMA_TO_CREATE, false);
		saveHistory=values.getBoolean(SAVE_PERMANENTLY_HISTORY_ENABLED, false);
		viewPerspectivesEnabled = values.getBoolean(PESPECTIVES_ENABLED_PARAMETER_NAME, false); 
		senderAddress = values.getString(SENDER_ADDRESS_PARAMETER_NAME, null);
		receiverAddress = values.getString(RECEIVER_ADDRESS_PARAMETER_NAME, null);
		desiderataAddress = values.getString(DESIDERATA_ADDRESS_PARAMETER_NAME, null);
		
		css = values.getString(CUSTOMER_CSS_PARAMETER_NAME, null);
		
		communityDataPrivate = values.getBoolean(COMMUNITY_DATA_SHARED, true);
		
		dummyAccountId = values.getInt(DUMMY_ACCOUNT_ID_PARAMETER_NAME, -1);
		
		loanNotAllowedCodes = getCommaSeparatedValues(LOAN_NOT_ALLOWED_CODES, values);
		loanNotAllowedCodes = (loanNotAllowedCodes != null) ? loanNotAllowedCodes : new String [0];
		
		inVmHost = values.getString(IN_VM_HOSTNAME, "127.0.0.1");
		inVmPort = values.getString(IN_VM_PORT, null);
		
		disabledBranches = getCommaSeparatedIntegerValues("disabled-branches", values);
		disabledMainLibraries = getCommaSeparatedIntegerValues("disabled-main-libraries", values);
		
		opendataFilePath = values.getString(OPENDATA_FILE_PATH, null); 
		defaultLogicalView = values.getString("default-logical-view", null);
		logicalView = getCommaSeparatedValues("logical-view", values);
		authEnabled=values.getBoolean(AUTHORITY_ENABLED, false);
		//bug 5399
		permanentFilter = values.getBoolean(PERMANENT_FILTER, false);
						
		googleRecaptchaKey = values.getString(GOOGLE_RECAPTCHA_KEY);
		
		holdingSBNEnabled = values.getBoolean(HOLDING_SBN_ENABLED, false);
		navigationDeweyEnabled = values.getBoolean(NAVIGATION_DEWEY_ENABLED, false);
		configureDeweryFilterEd(navigationDeweyEnabled, values);
		uploadFolder = values.getString(UPLOAD_FOLDER);
		
		//aplus
		aplusServiceUrl = values.getString(APLUS_WEBSERVICE_URL);
		aplusPrintCardPage = values.getString(APLUS_WEB_PRINT_PAGE);
		
		//sebina
		sebinaServiceUrl = values.getString(SEBINA_SERVICE_URL);
		
		libraryDomainUrl = values.getString(LIBRARY_DOMAIN_URL);
		
		folioConfig = new FolioConfigurationTool();
		
		printFolder = values.getString("print-folder");
		printCommand = values.getString("print-command");
		
		loanLimit = values.getInt("loan-limit", 2);
		loanCountEnabled = values.getBoolean("loan-count-enabled", false);
		deletePdfEnabled = values.getBoolean(DELETE_PDF_PARAMETER, true);
		loanServiceClose = values.getString(LOAN_SERVICE_CLOSE);
		libraryClose = values.getString(LIBRARY_CLOSE);
		
	}
	
	public String getGoogleRecaptchaKey() {
		return googleRecaptchaKey;
	}
	
	public String getUploadFolder() {
		return uploadFolder;
	}

	public String [] getLogicalView() {
	    return logicalView;
    }

	/**
	 * Returns the css file to use.
	 * 
	 * @return the css file to use.
	 */
	public String getCss() 
	{
		return css;
	}
	
	public int [] getDisabledBranches()
	{
		return disabledBranches != null ? disabledBranches : EMPTY_INT_ARRAY;
	}
	
	public int [] getDisabledMainLibraries()
	{
		return disabledMainLibraries != null ? disabledMainLibraries : EMPTY_INT_ARRAY;
	}
	
	public int getCurrentYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}
	
	/**
	 * Returns the dummy account identifier.
	 * 
	 * @return the dummy account identifier.
	 */
	public int getDummyAccountId()
	{
		return dummyAccountId;
	}
	
	/**
	 * Returns true if the community data won't be shared among users.
	 * 
	 * @return true if the community data won't be shared among users.
	 */
	public boolean isCommunityDataPrivate()
	{
		return communityDataPrivate;
	}
	//bug 5399
	public boolean isPermamentFilterEnabled(){
		return permanentFilter;
	}

	/**
	 * Builds the meta attribute list that can be used for ordering ThGenius results.
	 * 
	 * @param configurationParameter the value of this attribute as definied in configuration.
	 */
	private void buildThGeniusOrderByMetaAttributeList(final String configurationParameter) 
	{	  
		if (configurationParameter != null)
		{
			String [] groups = configurationParameter.split("\\],\\[");
			if (groups != null)
			{
				for (int index = 0; index < groups.length; index++)
				{ 
					String [] attributeNameAndFields = groups[index].replace("[","").replace("]","").split("=");
					orderByMap.put(attributeNameAndFields[0], attributeNameAndFields[1]);
					thGeniusOrderByMetaAttributesList.add(attributeNameAndFields[0]);
				}
			}
		}
	}
	
	/**
	 * 
	 * @return front end url of folio from folio_config.properties
	 */
	public String getFolioUrlFrontend() {
		return folioConfig.getString(FolioConstants.FOLIO_URL_FRONTEND);
	}

	/**
	 * Returns true if labels (e.g. Italian, English) should be used instead of flag icons.
	 * 
	 * @return true if labels (e.g. Italian, English) should be used instead of flag icons.
	 */
	public boolean isUseLabelInsteadFlags()
	{
		return useLabelInsteadFlags;
	}
	
	/**
	 * Returns true if the given attribute name corresponds to an attribute that needs to be quoted.
	 * 
	 * @param attributeName the attribute name.
	 * @return true if the given attribute name corresponds to an attribute that needs to be quoted.
	 */
	public boolean isQuotedMetaAttribute(final String attributeName)
	{
		for (String quotedMetaAttribute : quotedMetaAttributes)
		{
			if (quotedMetaAttribute.equals(attributeName))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a map / list containing all searchable attributes.
	 * 
	 * @return a map / list containing all searchable attributes.
	 */
	public Map<String, String []> getSearchableMetaAttributes()
	{
		return searchableMetaAttributes;
	}
	
	/**
	 * Returns a map / list containing all advanced searchable attributes.
	 * 
	 * @return a map / list containing all advanced searchable attributes.
	 */
	public Map<String, String []> getAdvancedSearchableMetaAttributes()
	{
		return advancedSearchableMetaAttributes;
	}	
	
	/**
	 * Returns the precondigured email address that will be used to send emails.
	 * 
	 * @return the precondigured email address that will be used to send emails.
	 */
	public String getEmailSenderAddress()
	{
		return senderAddress;
	}
	
	/**
	 * Returns the precondigured email address that will be used to receive emails.
	 * 
	 * @return the precondigured email address that will be used to receive emails.
	 */
	public String getEmailReceiverAddress()
	{
		return receiverAddress;
	}
	
	/**
	 * Returns the precondigured email address that will be used for desiderata.
	 * 
	 * @return the precondigured email address that will be used for desiderata.
	 */
	public String getDesiderataAddress()
	{
		return desiderataAddress;
	}
	
	/**
	 * Returns the list of "orderable" attributes.
	 * 
	 * @return the list of "orderable" attributes.
	 */
	public List<OrderByCriteria> getOrderByMetaAttributes()
	{
		return orderByMetaAttributes;
	}	
	
	/**
	 * Returns the list of allowed page sizes.
	 * 
	 * @return the list of allowed page sizes.
	 */
	public int [] getAllowedPageSizes()
	{
		return allowedPageSizes;
	}
	
	/**
	 * Returns the initial number of advanced search fields.
	 * 
	 * @return the initial number of advanced search fields.
	 */
	public int getInitialNumberOfAdvancedSearchFields()
	{
		return initialNumberOfAdvancedSearchFields;
	}

	/**
	 * Returns the list of default "where" values for advanced search fields.
	 * 
	 * @return the list of default "where" values for advanced search fields.
	 */
	public String[] getDefaultWhereValuesForAdvancedFields()
	{
		return defaultWhereValuesForAdvancedFields;
	}

	/**
	 * Returns the default page size.
	 * 
	 * @return the default page size.
	 */
	public int getDefaultPageSize() 
	{
		return defaultPageSize;
	}

	/**
	 * Returns the default order by criteria.
	 * 
	 * @return the default order by criteria.
	 */
	public String getDefaultOrderByCriteria() 
	{
		return defaultOrderByCriteria;
	}
	
	/**
	 * Returns the list of search providers configured on this OseeGenius -W- instance.
	 * 
	 * @return the list of search providers configured on this OseeGenius -W- instance.
	 */
	public List<ExternalSearchProvider> getSearchProviders()
	{
		return searchProviders;
	}
	
	/**
	 * Returns true if the given code is a not loan allowed code.
	 * 
	 * @param code the given code.
	 * @return true if the given code is a not loan allowed code.
	 */
	public boolean isLoanNotAllowedCode(final String code)
	{
		for (String lnaCode : loanNotAllowedCodes)
		{
			if (lnaCode.equals(code))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the list of ThGenius search providers configured on this OseeGenius -W- instance.
	 * 
	 * @return the list of ThGenius search providers configured on this OseeGenius -W- instance.
	 */
	public List<ExternalSearchProvider> getThGeniusSearchProviders()
	{
		return thGeniusTargetProviders;
	}
	
	/**
	 * Returns the list of supported languages.
	 * 
	 * @return the list of supported languages.
	 */
	public List<Supportedi18nLanguage> getSupportedLanguages()
	{ 
		return supportedLanguages;
	}
	
	/**
	 * Returns the map containing ThGenius ordering attributes.
	 * 
	 * @return the map containing ThGenius ordering attributes.
	 */
	public List<String> getThGeniusOrderByMetaAttributes() 
	{
		return thGeniusOrderByMetaAttributesList;
	}
	
	/**
	 * Starting from a given "logic" attribute name, returns the corresponding order-by fields.
	 * 
	 * @param metaAttributeName the logic sort by attribute name. 
	 * @return the corresponding order-by fields.
	 */
	public String getOrderByFields(final String metaAttributeName)
	{
		return orderByMap.get(metaAttributeName);
	}
	
	/**
	 *Return the array of editons for Dewey classification.
	 *
	 * @return the array of string that list dewey editions on which we do a filter
	 */
	public String [] getFilterDeweyEd()
	{
		return filterDeweyEd;
	}
	
	/**
	 * Builds the list of supported languages.
	 * 
	 * @param values the OseeGenius -W- configuration parameters.
	 */
	private void configureI18n(final ValueParser values) 
	{
		String configurationParameterValue = values.getString("i18n-support");
		if (configurationParameterValue != null)
		{
			String [] couples = configurationParameterValue.trim().split(",");
			if (couples != null)
			{
				for (String couple : couples)
				{
					String [] codeAndIcon = couple.trim().split("=");
					useLabelInsteadFlags |= codeAndIcon[1].indexOf(".") != -1;
					supportedLanguages.add(new Supportedi18nLanguage(codeAndIcon[0].trim(), codeAndIcon[1].trim()));
				}
			}
		}
	}
	
	/**
	 * Configures the allowed page sizes and the default value for page size..
	 * If page sizes are not defined then a default values of 5,10,20 is assumed.
	 * If default page size is not defined a default value of 10 is assumed.
	 * 
	 * @param values the OseeGenius -W- configuration.
	 */
	private void configureAllowedPageSizes(final ValueParser values)
	{
		allowedPageSizes = getCommaSeparatedIntegerValues(ALLOWED_PAGE_SIZES_PARAMETER_NAME, values);
		allowedPageSizes = (allowedPageSizes != null && allowedPageSizes.length > 0) ? allowedPageSizes : DEFAULT_PAGE_SIZES;
		
		defaultPageSize = values.getInt(DEFAULT_PAGE_SIZE_PARAMETER_NAME, DEFAULT_PAGE_SIZE);
	}
	
	/**
	 * Configures the filter for dewey navigation. Do configuration only if Navdewey = true
	 * If list not defined, we use the list of DEFAULT_DEWEY_EDS.
	 * 
	 * @param Navdewey the boolean values of navigationDeweyEnabled
	 * @param values the OseeGenius -W- configuration.
	 */
	private void configureDeweryFilterEd(boolean Navdewey, final ValueParser values) {
		if (Navdewey) {
			filterDeweyEd =  getCommaSeparatedValues( LIST_FILTER_DEWEY_EDITIONS, values);
			filterDeweyEd = (filterDeweyEd != null && filterDeweyEd.length > 0) ? filterDeweyEd : DEFAULT_DEWEY_EDS;
		}
	}
	
	
	/**
	 * Configures the order by criteria.
	 * If order has not been defined the a "score".
	 * If default page size is not defined a default value of 10 is assumed.
	 * 
	 * @param values the OseeGenius -W- configuration.
	 */
	private void configureOrderBy(final ValueParser values)
	{
		String orderByAttributes = values.getString(ORDER_BY_CRITERIAS_PARAMETER_NAME);

		String [] pairs = orderByAttributes.split(",");
		if (pairs != null && pairs.length > 0)
		{
			for (String pair: pairs)
			{
				String [] valueAndName = pair.split("/");
				if (valueAndName != null && valueAndName.length == 2)
				{
					orderByMetaAttributes.add(new OrderByCriteria(valueAndName[1], valueAndName[0]));
				}
			}
		}
		
		defaultOrderByCriteria = values.getString(DEFAULT_ORDER_BY_CRITERIA_PARAMETER_NAME, "score");
	}

	private final static Logger LOGGER = LoggerFactory.getLogger(ConfigurationTool.class);
	private Map<String, Integer> facetsAndLimits = new HashMap<String, Integer>();
	
	public Integer getFacetLimit(String attributeName)
	{
		return facetsAndLimits.get(attributeName);
	}
	
	/**
	 * Configures the order by criteria.
	 * If order has not beetributen defined the a "score".
	 * If default page size is not defined a default value of 10 is assumed.
	 * 
	 * @param values the OseeGenius -W- configuration.
	 */
	private void configureFacetLimits(final ValueParser values)
	{
		String facetLimits = values.getString(FACET_LIMITS_PARAMETER_NAME);
		if (facetLimits != null)
		{
			String [] pairs = facetLimits.split(",");
			if (pairs != null && pairs.length > 0)
			{
				for (String pair : pairs)
				{
					String [] facetAndLimit = pair.split("/");
					if (facetAndLimit != null && facetAndLimit.length == 2)
					{
						try 
						{
							facetsAndLimits.put(facetAndLimit[0], Integer.parseInt(facetAndLimit[1]));
						} catch (Exception exception)
						{
							LOGGER.error("Unable to properly configure the facet limit ("+ pair + "). As consequence of that it will be skept.");
						}
					}
				}
			}
		}
	}
	
	private Map<String, String> servicePoints = new HashMap<String, String>();	
	public String getPrinterIP(String attributeName)
	{
		return servicePoints.get(attributeName);
	}
	
	/**
	 * Configures the service point printers.
	 * 
	 * @param values the OseeGenius -W- configuration.
	 */
	private void configureServicePoint(final ValueParser values)
	{
		String facetLimits = values.getString(SERVICE_POINT_PARAMETER_NAME);
		if (facetLimits != null)
		{
			String [] pairs = facetLimits.split(",");
			if (pairs != null && pairs.length > 0)
			{
				for (String pair : pairs)
				{
					String [] servicePoint = pair.split("/");
					if (servicePoint != null && servicePoint.length == 2)
					{
						try 
						{
							servicePoints.put(servicePoint[0], servicePoint[1]);
						} catch (Exception exception)
						{
							LOGGER.error("Unable to properly configure the service point ("+ pair + "). As consequence of that it will be skept.");
						}
					}
				}
			}
		}
	}
	
	/**
	 * Configures the branches location to show in advanced search page.
	 * 
	 * @param values the OseeGenius -W- configuration.
	 */
	private void configureBranchesLocation(final ValueParser values)
	{
		String branchesLoc = values.getString(BRANCHES_LOCATIONS);
		if (branchesLoc != null)
		{
			String [] pairs = branchesLoc.split("/");
			if (pairs != null && pairs.length > 0)
			{
				for (String pair : pairs)
				{
					String [] branchLocation = pair.split(":");
					if (branchLocation != null && branchLocation.length == 2)
					{
						try 
						{
						   List<Integer> listLoc = null;
						   if (branchLocation[1].indexOf(",")==-1){
							   listLoc = new ArrayList<Integer>();
							   listLoc.add(Integer.parseInt(branchLocation[1]));
						   }else{
							   String [] locs = branchLocation[1].split(",");
							   listLoc = new ArrayList<Integer>();
							   for(int i=0; i < locs.length; i++){
								   listLoc.add(Integer.parseInt(locs[i]));
							   }
						   }
						   if(listLoc != null){
							   branchesLocation.put(Integer.parseInt(branchLocation[0]), listLoc);
						   }
						} catch (Exception exception)
						{
							LOGGER.error("Unable to properly configure the branch location ("+ pair + "). As consequence of that it will be skept.");
						}
					}
				}
			}
		}
	}
	
	public Map<Integer, List<Integer>> getBranchesLocation(){
		return branchesLocation;
	}

	
	/**
	 * Loads the configured searchable field groups.
	 * Note that fields are mainly classfied in 2 categories simple and advanced searchable fields.
	 * If advanced fields don't have a configured value the OseeGenius will use the simple searchable 
	 * fieds in advanced search too.
	 * 
	 * @param values the OseeGenius -W- configuration.
	 */
	private void configureSearchableFieldGroups(final ValueParser values)
	{
		String [] searchableFieldGroups = values.getString(SEARCHABLE_FIELD_GROUPS_PARAMETER_NAME).split("\\],\\[");
		for (int index = 0; index < searchableFieldGroups.length; index++)
		{ 
			String [] nameAndFields = searchableFieldGroups[index].replace("[", "").replace("]", "").split(":");
			String [] fields = nameAndFields[1].split(",");
			searchableMetaAttributes.put(nameAndFields[0], fields);
		}
		
		String advancedSearchableFieldGroupsValue = values.getString(ADVANCED_SEARCHABLE_FIELD_GROUPS_PARAMETER_NAME, null);
		if (advancedSearchableFieldGroupsValue != null)
		{
			String [] advancedSearchableFieldGroups = advancedSearchableFieldGroupsValue.split("\\],\\[");
			for (int index = 0; index < advancedSearchableFieldGroups.length; index++)
			{ 
				String [] nameAndFields = advancedSearchableFieldGroups[index].replace("[", "").replace("]", "").split(":");
				String [] fields = nameAndFields[1].split(",");
				advancedSearchableMetaAttributes.put(nameAndFields[0], fields);
			}
		} else 
		{
			advancedSearchableMetaAttributes = searchableMetaAttributes;
		}
	}
	
	/**
	 * Load configuration for advanced search components.
	 * 
	 * @param values the OseeGenius -W- configuration.
	 */
	private void configureAdvancedSearch(final ValueParser values)
	{
		initialNumberOfAdvancedSearchFields = values.getInteger("initial-number-of-advanced-search-fields");
		defaultWhereValuesForAdvancedFields = values.getString("default-where-values-for-advanced-fields","").split(",");
		quotedMetaAttributes = values.getString(QUOTED_META_ATTRIBUTES, "").split(",");		
	}
	
	/**
	 * Configures the browsable indexes.
	 * At the end, browsing (scanning) will be enabled only if there 
	 * is at least one available index.
	 * 
	 * @param values the OseeGenius -W- configuration.
	 */
	private void configureBrowsing(final ValueParser values)
	{
		browsableSimpleIndexes = getCommaSeparatedValues(BROWSABLE_SIMPLE_INDEXES_PARAMETER_NAME, values);
		browsableAuthorityIndexes = getCommaSeparatedValues(BROWSABLE_AUTHORITY_INDEXES_PARAMETER_NAME, values);
		
		browsableSimpleIndexes = (browsableSimpleIndexes != null && browsableSimpleIndexes.length != 0) ? browsableSimpleIndexes : null;
		browsableAuthorityIndexes = (browsableAuthorityIndexes != null && browsableAuthorityIndexes.length != 0) ? browsableAuthorityIndexes : null;
	}

	/**
	 * Returns true if browsing has been enabled on this OseeGenius -W- instance.
	 * Remember, browing is enabled if there's at least one browsable attribute
	 * (either simple or authority).
	 * 
	 * @return true if browsing has been enabled on this OseeGenius -W- instance.
	 */
	public boolean isBrowsingEnabled()
	{
		return browsableSimpleIndexes != null || browsableAuthorityIndexes != null;
	}
	
	/**
	 * Returns the browsable simple indexes.
	 * 
	 * @return the browsable simple indexes.
	 */
	public String[] getBrowsableSimpleIndexes()
	{
		return browsableSimpleIndexes;
	}

	/**
	 * Returns the browsable authority indexes.
	 * 
	 * @return the browsable authority indexes.
	 */
	public String [] getBrowsableAuthorityIndexes()
	{
		return browsableAuthorityIndexes;
	}

	/**
	 * Returns true if view perspectives are enabled.
	 * 
	 * @return true if view perspectives are enabled.
	 */
	public boolean isViewPerspectivesEnabled()
	{
		return viewPerspectivesEnabled;
	}
	
	/**
	 * returns true if you need to save the search in the db
	 * 
	 * @return returns true if you need to save the search in the db.
	 */
	public boolean searchToSave()
	{
		return saveHistory;
	}
	
	public boolean schemaToCreate() {
		
		return schemaToCreate;		
		
	}
	
	
	/**
	 * returns true if custom fields are to be used
	 * false if note are to be used
	 */
	public boolean isCustomFieldActive() {
			
		return useCustomFields;		
			
	}
		
	
	
	/**
	 * Splits the value associated with the given parameter in a string array.
	 * 
	 * @param parameterName the parameter name.
	 * @param values the OseeGenius -W- configuration.
	 * @return a string array with configured values for the requested parameter, or null.
	 */
	private String [] getCommaSeparatedValues(final String parameterName, final ValueParser values)
	{
		String compound = values.getString(parameterName);
		return compound != null ? compound.split(",") : null;
	}	

	/**
	 * Splits the value associated with the given parameter in an int array.
	 * 
	 * @param parameterName the parameter name.
	 * @param values the OseeGenius -W- configuration.
	 * @return an int array with configured values for the requested parameter, or null.
	 */
	private int [] getCommaSeparatedIntegerValues(final String parameterName, final ValueParser values)
	{
		return values.getInts(parameterName);
	}	
	
	/**
	 * Configures ThGenius parameters.
	 * 
	 * @param values the ThGenius parameters.
	 */
	private void configureThGenius(final ValueParser values)
	{
		String [] thGeniusExternalSearchProviderUrls = values.getString("th-genius-search-target-url", "").split(",");
		String [] thGeniusExternalSearchProviderLabels = values.getString("th-genius-search-target-label", "").split(",");
		
		if (thGeniusExternalSearchProviderUrls != null && thGeniusExternalSearchProviderUrls.length != 0)
		{
			for (int index = 0; index < thGeniusExternalSearchProviderUrls.length; index++)
			{
				thGeniusTargetProviders.add(
						new ExternalSearchProvider(
								null, // No icon 
								thGeniusExternalSearchProviderLabels[index],
								thGeniusExternalSearchProviderUrls[index]));
			}
		}

		buildThGeniusOrderByMetaAttributeList(values.getString("th-genius-sort-attributes"));
	}
	
	/**
	 * Returns true if internationalization support has been enabled on this OseeGenius -W- instance.
	 * There's no a direct attribute to configure that: removing all values from "i18n-support"
	 * configuration parameter basically disables the i18n support.
	 * 
	 * @return true if internationalization support has been enabled on this OseeGenius -W- instance.
	 */
	public boolean isI18nEnabled()
	{
		return !supportedLanguages.isEmpty();
	}
	
	public String getInVmHost() {
		return inVmHost;
	}

	public String getInVmPort() {
		return inVmPort;
	}
	

	/**
	 * @return the opendataFilePath
	 */
	public String getOpendataFilePath() {
		return opendataFilePath;
	}

	/**
	 * @param opendataFilePath the opendataFilePath to set
	 */
	public void setOpendataFilePath(String opendataFilePath) {
		this.opendataFilePath = opendataFilePath;
	}
	
	

	/**
	 * @return the authEnabled
	 */
	public boolean isAuthEnabled() {
		return authEnabled;
	}

	/**
	 * @param authEnabled the authEnabled to set
	 */
	public void setAuthEnabled(boolean authEnabled) {
		this.authEnabled = authEnabled;
	}

	/**
	 * Reads from configuration declared external providers.
	 * Note that has nothing to do with federated search: external providers are web search
	 * engine that offers a (HTTP) GUI that can be controlled by HTTP (GET) requests by appending
	 * the query string.
	 * 
	 * @param values the configuration of this OseeGenius -W- instance.
	 */
	private void configureExternalSearchProvider(final ValueParser values)
	{
		String [] externalSearchProviderIcons = values.getString("external-search-providers-icon", "").split(",");
		if (externalSearchProviderIcons != null && externalSearchProviderIcons.length != 0)
		{
			String [] externalSearchProviderUrl = values.getString("external-search-providers-url", "").split(",");
			for (int index = 0; index < externalSearchProviderIcons.length; index++)
			{
				if (externalSearchProviderIcons[index].trim().length() != 0 && externalSearchProviderUrl[index].trim().length() != 0)
				searchProviders.add(new ExternalSearchProvider(externalSearchProviderIcons[index], externalSearchProviderUrl[index]));
			}
		}
	}

	public String getDefaultLogicalView() {
		return defaultLogicalView;
	}	
	
	public boolean getHoldingSbnEnable() {
		return holdingSBNEnabled;
	}
	public boolean getNavigationDeweyEnabled() {
		return navigationDeweyEnabled;
	}
	
	public String getAplusServiceUrl() {
		return aplusServiceUrl;
	}
	public void setAplusServiceUrl(String aplusServiceUrl) {
		this.aplusServiceUrl = aplusServiceUrl;
	}
	public String getAplusPrintCardPage() {
		return aplusPrintCardPage;
	}
	public void setAplusPrintCardPage(String aplusPrintCardPage) {
		this.aplusPrintCardPage = aplusPrintCardPage;
	}
	public String getSebinaServiceUrl() {
		return sebinaServiceUrl;
	}
	public void setSebinaServiceUrl(String sebinaServiceUrl) {
		this.sebinaServiceUrl = sebinaServiceUrl;
	}
	public String getLibraryDomainUrl() {
		return libraryDomainUrl;
	}	
	public void setPrintFolder(String printFolder) {
		this.printFolder = printFolder;
	}
	public String getPrintFolder() {
		return printFolder;
	}
	public void setPrintCommand(String printCommand) {
		this.printCommand = printCommand;		
	}
	public String getPrintCommand() {
		return printCommand;
	}
	public int getLoanLimit() {
		return loanLimit;
	}
	public void setLoanLimit(int loanLimit) {
		this.loanLimit = loanLimit;
	}
	public boolean isLoanCountEnabled() {
		return loanCountEnabled;
	}
	public void setLoanCountEnabled(boolean loanCountEnabled) {
		this.loanCountEnabled = loanCountEnabled;
	}
	public boolean isDeletePdfEnabled() {
		return deletePdfEnabled;
	}
	public void setDeletePdfEnabled(boolean deletePdfEnabled) {
		this.deletePdfEnabled = deletePdfEnabled;
	}
	
	public String getLoanServiceClose() {
		return loanServiceClose;
	}
	public void setLoanServiceClose(String loanServiceClose) {
		this.loanServiceClose = loanServiceClose;
	}
	
	public String getLibraryClose() {
		return libraryClose;
	}
	public void setLibraryClose(String libraryClose) {
		this.libraryClose = libraryClose;
	}
	
	
}
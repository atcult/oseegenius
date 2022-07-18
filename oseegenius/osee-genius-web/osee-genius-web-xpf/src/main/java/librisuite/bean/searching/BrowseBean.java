/*
 * (c) LibriCore
 * 
 * Created on Jul 20, 2004
 * 
 * BrowseBean.java
 */
package librisuite.bean.searching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;

import librisuite.business.authority.AuthorityNote;
import librisuite.business.codetable.DAOCodeTable;
import librisuite.business.codetable.IndexListElement;
import librisuite.business.common.DAOBibliographicCorrelation;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.common.View;
import librisuite.business.crossreference.CrossReferenceSummaryElement;
import librisuite.business.descriptor.DAODescriptor;
import librisuite.business.descriptor.Descriptor;
import librisuite.business.searching.BrowseManager;
import librisuite.business.searching.DAOIndexList;
import librisuite.business.searching.InvalidBrowseIndexException;
import librisuite.business.searching.SolrManager;
import librisuite.hibernate.CLSTN;
import librisuite.hibernate.CorrelationKey;
import librisuite.hibernate.REF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.libricore.librisuite.common.StringText;


/**
 * Manages presentation output for the browse frame
 * 
 * @author paulm
 * @version %I%, %G%
 * @since 1.0
 */
public class BrowseBean extends SearchBean {
	
	
	private static final int CATEGORY_NAME_DEFAULT = 17;
	private static short DEFAULT_CATEGORY = 17;
	public static final int NO_SHELF_MAIN_LIBRARY = 0;
	private static final int CATEGORY_NT_LIBRICAT = 11;
	private static final int CATEGORY_NT_AMICUS = 12;
	public static final String BROWSE_METHOD_EDIT_HDG = "editHdg";
	private static final Log logger = LogFactory.getLog(BrowseBean.class);

	private List browseIndexList;
	private List editorBrowseIndexList;
	private Stack browseLinkMethod = new Stack(); 		
	private Stack browseIndexHistory = new Stack();		
	private Stack selectedIndexHistory = new Stack();	
	private boolean showResults;
	private String selectedIndex;
	private String lastBrowseTerm;
	private BrowseManager browseManager;
	private List<Integer> xrefCountList;
	private List<Integer> docCountList;
	private List<Integer> noteThesaurusCountList;
    private List<Integer> authCountList;
	private Descriptor lastSelectedDescriptor;
	private int termsToDisplay=Defaults.getInteger("browse.termsPerPage");
	private static final DAOBibliographicCorrelation daoCorrelation = new DAOBibliographicCorrelation();
	private short selectedCategory = DEFAULT_CATEGORY;
	private String browseFieldName;
	private Locale currentLocale;
	private List<Integer> madesCountList;
	private List ntCountList;
	private boolean nameTitle;
	private String browseMethod;
	private String storedMethod;
	private List<Integer> publishersWithShortCodeList;
	private int collectionCode;
	private String cataloguingView;
	private SolrManager solrManager;
	private boolean supportsNameTitle=false;
    private static boolean authorityEnabled;
	private Map<String, List<AuthorityNote>> itemNotes;
    private String nextTerm;
    private String lastTerm;
    public String getLastTerm() {
		return lastTerm;
	}

	public void setLastTerm(String lastTerm) {
		this.lastTerm = lastTerm;
	}

	
    public String getNextTerm() {
		return nextTerm;
	}

	public void setNextTerm(String nextTerm) {
		this.nextTerm = nextTerm;
	}

	
	public Map<String, List<AuthorityNote>> getItemNotes() {
		return itemNotes;
	}

	public void setItemNotes(Map<String, List<AuthorityNote>> itemNotes) {
		this.itemNotes = itemNotes;
	}
	

	public boolean isAuthorityEnabled() {
		
		return authorityEnabled;
	}

	public void setAuthorityEnabled(boolean authorityEnabled) {
		this.authorityEnabled = authorityEnabled;
	}


	public boolean isSupportsNameTitle() {
		return supportsNameTitle;
	}

	public void setSupportsNameTitle(boolean supportsNameTitle) {
		this.supportsNameTitle = supportsNameTitle;
	}

	public SolrManager getSolrManager() {
		return solrManager;
	}

	public void setSolrManager(SolrManager solrManager) {
		this.solrManager = solrManager;
	}

	public String getCataloguingView() {
		return cataloguingView;
	}

	public void setCataloguingView(String cataloguingView) {
		this.cataloguingView = cataloguingView;
	}

	public int getCollectionCode() {
		return collectionCode;
	}

	public void setCollectionCode(int collectionCode) {
		this.collectionCode = collectionCode;
	}
	 //to remove
    /*
	public String getArchiveBibView() {
		return archiveBibView;
	}
	
	public void setArchiveBibView(String archiveBibView) {
		this.archiveBibView = archiveBibView;
	}
	*/

	public String getStoredMethod() {
		return storedMethod;
	}

	public void setStoredMethod(String storedMethod) {
		this.storedMethod = storedMethod;
	}

	public String getBrowseMethod() {
		return browseMethod;
	}

	public void setBrowseMethod(String browseMethod) {
		this.browseMethod = browseMethod;
	}

	public List getNtCountList() {
		return ntCountList;
	}

	public void setNtCountList(List ntCountList) {
		this.ntCountList = ntCountList;
	}

	public List getMadesCountList() {
		return madesCountList;
	}

	public void setMadesCountList(List madesCountList) {
		this.madesCountList = madesCountList;
	}

	public List getNoteThesaurusCountList() {
		return noteThesaurusCountList;
	}

	public void setNoteThesaurusCountList(List noteThesaurusCountList) {
		this.noteThesaurusCountList = noteThesaurusCountList;
	}
	
	public int getNoteThesaurusCount(int i) {
		return ((Integer) getNoteThesaurusCountList().get(i)).intValue();
	}

	/**
	 * false: do not load the decoratedBrowseList and
	 *        do not show Level column in cBrowse 
	 */
	public static final boolean verificationLevelVisible = Defaults.getBoolean("customer.casalini.hdg.verification.level.coulumn.visible");

	/**
	 * default. Not Decorated List
	 */
	private List/*<Descriptor*/ browseList;

	/**
	 * Decorated List
	 * loaded in setBrowseList if verificationLevelVisible = true
	 */
	private List/*<DescriptorDecorator>*/ decoratedBrowseList;

	public String getBrowseFieldName() {
		return browseFieldName;
	}

	public void setBrowseFieldName(String browseFieldName) {
		this.browseFieldName = browseFieldName;
	}


	private String lastBrowseTermSkip;
	public String getLastBrowseTermSkip() {
		return lastBrowseTermSkip;
	}


	public void setLastBrowseTermSkip(String lastBrowseTermSkips) {
		if (lastBrowseTermSkips.trim().length() > BrowseManager.MAX_BROWSE_TERM_LENGTH)
			lastBrowseTermSkip = lastBrowseTermSkips.substring(0, BrowseManager.MAX_BROWSE_TERM_LENGTH
	);
		else
			lastBrowseTermSkip = lastBrowseTermSkips;
	}
	
	/*modifica barbara 15/05/2007 inserimento skip in filling nella catalogazione*/
	private int skipInFiling;
	public int getSkipInFiling() {
		return skipInFiling;
	}

	public void setSkipInFiling(int skipInFiling) {
		this.skipInFiling = skipInFiling;
	}


	/**
	 * static member to get a single instance of the bean from the session and if
	 * not present to create a new one and save it in the session
	 *
	 */
	public static BrowseBean getInstance(HttpServletRequest request) {
		BrowseBean bean =
			(BrowseBean) BrowseBean.getSessionAttribute(
				request,
				BrowseBean.class);
		if (bean == null) {
			bean = new BrowseBean();
			bean.setSessionAttribute(request, BrowseBean.class);
			bean.setSolrManager(SolrManager.getInstance());
			//Only for CBT
			if("true".equals(System.getProperty("authorityEnabled")))
				authorityEnabled=true;
			else
				authorityEnabled=false;
			bean.setAuthorityEnabled(authorityEnabled);
	}
		bean.setSessionAttribute(request, SearchBean.class);
		SearchBean.getInstance(request);
		return bean;
	}

	/*
	 * gets the "browse" entries from IDX_LIST 
	 */
	private void initBrowseIndexList(Locale l) throws DataAccessException 
	{
		DAOIndexList dao = new DAOIndexList();
		setBrowseIndexList(dao.getBrowseIndex(l));
		setEditorBrowseIndexList(dao.getEditorBrowseIndex(l));
	    boolean isNT = getNtIndex();
		setSupportsNameTitle(isNT);
   }

	public short getMarcCategory() throws DataAccessException{
		if (getBrowseList() != null && getBrowseList().size() > 0) {
			return ((Descriptor) getBrowseList().get(0)).getCategory();
		} else {
			CorrelationKey cor=daoCorrelation.getMarcTagCodeBySelectedIndex(this.getSelectedIndex());
			if(cor==null)
				return CATEGORY_NAME_DEFAULT;
		   else {
				short category= cor.getMarcTagCategoryCode();
				if(category==CATEGORY_NT_AMICUS)
				  return CATEGORY_NT_LIBRICAT; 
				else return category; 
			}
		}
	}

	/**
	 * Finds the language independent key from browseIndexList based on the
	 * users selection
	 */
	public String getSelectedIndexKey() {
		String result = null;
		List l = getBrowseIndexList();

		logger.debug(
			"Looking for key of browse index '" + getSelectedIndex() + "'");
		Iterator iter = l.iterator();
		IndexListElement anElem;
		while (iter.hasNext()) {
			anElem = (IndexListElement) iter.next();
			if (anElem.getValue().toUpperCase().trim().compareTo(getSelectedIndex().toUpperCase().trim()) == 0) {
				return anElem.getKey();
			}
		}
		return result;
	}

	/**
	 * Finds the language dependent value from browseIndexList based on the
	 * given language independent representation (key)
	 */
	public String getLocalisedIndex(String browseIndexKey) {
		String result = null;
		List l = getBrowseIndexList();
		Iterator iter = l.iterator();
		IndexListElement anElem;
		while (iter.hasNext()) {
			anElem = (IndexListElement) iter.next();
			if (anElem.getKey().equals(browseIndexKey)) {
				return anElem.getValue();
			}
		}
		return result;
	}

	/**
	 * Initialises the Browse bean for operations
	 * @throws SolrServerException 
	 * 
	 */
	public void init(Locale locale) throws DataAccessException, SolrServerException {
		currentLocale = locale; 
		initBrowseIndexList(locale);
		setBrowseManager(new BrowseManager());
		setShowResults(false);
		storeSelectedIndex(getSelectedIndex());
		setSelectedIndex("NA       ");
		logger.debug("Selected index set to '" + getSelectedIndex() + "'");
		setBrowseMethod("browse");
	}

	/**
	 * Initialises the Browse bean for a nested operations
	 * 
	 */
	public void initForNestedOperation(Locale locale) throws DataAccessException {
		currentLocale = locale; 
		storeBrowseIndex(getBrowseManager().getBrowseIndex());
		setShowResults(false);
		storeSelectedIndex(getSelectedIndex());
		setSelectedIndex(
			((IndexListElement) (getBrowseIndexList().get(0))).getValue());
		logger.debug("Selected index set to '" + getSelectedIndex() + "'");
	}

	/**
	 * Initialises the Browse bean for a nested operations
	 * @throws InvalidBrowseIndexException 
	 * 
	 */
	public void closeNestedOperation(int mainLibraryForShelfList) throws DataAccessException, InvalidBrowseIndexException {
		setSelectedIndex(retrieveSelectedIndex());
		getBrowseManager().setBrowseIndex(retrieveBrowseIndex(), mainLibraryForShelfList);
		popBrowseLinkMethod();
	}
	
	private void storeBrowseIndex(String browseIndex) {
		browseIndexHistory.push(browseIndex);
	}
	
	private String retrieveBrowseIndex() {
		return (String)browseIndexHistory.pop();
	}
	
	public Stack getBrowseIndexHistory() {
		return browseIndexHistory;
	}
	private void storeSelectedIndex(String browseIndex) {
		selectedIndexHistory.push(browseIndex);
	}
	
	private String retrieveSelectedIndex() {
		return (String)selectedIndexHistory.pop();
	}
	
	public Stack getSelectedIndexHistory() {
		return selectedIndexHistory;
	}

	
	/**
	 * Redoes the browse to refresh the entries and the xref counts 
	 * 
	 * @param term
	 * @param termSkip
	 * @param cataloguingView
	 * @param mainLibrary
	 * @param localey
	 * @throws DataAccessException
	 * @throws InvalidBrowseIndexException
	 */
	public void refresh(String term, String termSkip, int cataloguingView, int mainLibrary, Locale locale)
		throws DataAccessException, InvalidBrowseIndexException {

		getBrowseManager().setBrowseIndex(getSelectedIndexKey(), mainLibrary);

		List l = null;

		logger.debug("Doing refresh with index: " + getSelectedIndexKey());
		l =
			getBrowseManager().getFirstPage(
				termSkip,
				cataloguingView,
				getTermsToDisplay(),
				getCollectionCode()
				);
		updateCounts(l, cataloguingView);

		setLastBrowseTerm(term);
		setLastBrowseTermSkip(termSkip);		
		setBrowseList(l);
		setShowResults(true);
	}
	
	public void refresh(String term, int cataloguingView, int mainLibrary, Locale locale) throws DataAccessException, InvalidBrowseIndexException 
	{
		
		getBrowseManager().setBrowseIndex(getSelectedIndexKey(), mainLibrary);
		List l = null;
		logger.debug("Doing refresh with index: " + getSelectedIndexKey());
		l = getBrowseManager().getFirstPage(term, cataloguingView, getTermsToDisplay(),getCollectionCode());
		updateCounts(l, cataloguingView);
	    publishersWithShortCodeList = new ArrayList();
		setLastBrowseTerm(term);
		setBrowseList(l);
		setShowResults(true);
	}

	private void updateCounts(List list, int cataloguingView)
		throws DataAccessException {
		List x;
		List y;
		List z;
		List j;
		List m;
		List n;
		
		if (getBrowseManager().isSupportsCrossReferences()) {
			x = getBrowseManager().getXrefCounts(list, cataloguingView);
			setXrefCountList(x);
		}
			
		y = getBrowseManager().getDocCounts(list, cataloguingView, getCollectionCode());
		setDocCountList(y);
		
			
		if (getBrowseManager().isSupportsAuthorities()) {
			if(!isAuthorityEnabled()){
				z = getBrowseManager().getSimpleAuthCounts(list);
				setAuthCountList(z);
			}
			else{
				z = getBrowseManager().getAuthCounts(list);
			 	setAuthCountList(z);
			}
		}
		
			
		if (getBrowseManager().isSupportsMades()) {
			m = getBrowseManager().getMadesCounts(list, -2);
			setMadesCountList(m);
		}
		
				
		if(getSelectedIndexKey().equals("7P0")||getSelectedIndexKey().equals("2P0")){
			n = getBrowseManager().getDocCountNT(list, cataloguingView);
			setNtCountList(n);
		}
				
	}

	public boolean isNameTitle(){
		if(getSelectedIndexKey().equals("7P0")||getSelectedIndexKey().equals("2P0"))
			return nameTitle=true;
		else
			return nameTitle=false;
	}
	/**
	 * gets the next page of entries 
	 * 
	 */
	public void next(String nextTerm, int cataloguingView, Locale locale) throws DataAccessException {
		
			
		BrowseManager b = getBrowseManager();
		
		List l = null;

		if(getBrowseList().size()>1){
			l = b.getNextPage(nextTerm, cataloguingView,getCollectionCode());
		}
		else{
			l =getBrowseManager().getFirstPage("",
					cataloguingView,
					getTermsToDisplay(),getCollectionCode());
		}
		updateCounts(l, cataloguingView);
		setBrowseList(l);
		
	}

	
	/**
	 * Gets the previous page of browse entries
	 * 
	 * @param cataloguingView
	 * @throws DataAccessException
	 */
	public void previous(String lastSortForm, int cataloguingView, Locale locale) throws DataAccessException {
		
				
		BrowseManager b = getBrowseManager();
		List l = null;

		if(getBrowseList().size()>0){
			l = b.getPreviousPage(lastSortForm, cataloguingView, getCollectionCode());
		}
		else{
			l =getBrowseManager().getFirstPage("",
					cataloguingView,
					getTermsToDisplay(),getCollectionCode());
		}
		updateCounts(l, cataloguingView);
		setBrowseList(l);
		
	}

	public List getBrowseIndexList() {
		return browseIndexList;
	}

	public String getBrowseLinkMethod() {
		if(browseLinkMethod.isEmpty()) {
			return BROWSE_METHOD_EDIT_HDG; 
		}
		return (String)browseLinkMethod.peek();
	}

	
	
	public List getBrowseList() {
		return browseList;
	}
	
	
	public List getDecoratedBrowseList() {
		return decoratedBrowseList;
	}

	public boolean isShowResults() {
		return showResults;
	}

	public void setBrowseIndexList(List list) {
		browseIndexList = list;
	}

	public void setBrowseLinkMethod(String string) {
		browseLinkMethod.push(string);
	}

	public void popBrowseLinkMethod() {
		browseLinkMethod.pop();
	}
	
	public void setBrowseList(List list) 
	{
		browseList = list;
		if(!isVerificationLevelVisible()) {
			decoratedBrowseList = list; 
		} else {
			decoratedBrowseList = decorate(list);
		}
	}

	private List decorate(List list) 
	{		
		if (list==null || list.isEmpty()){ 
			return list;
		}		
		
		Iterator it = list.iterator();
		List newList = new ArrayList();
		while (it.hasNext()) {
			Descriptor aDescriptor = (Descriptor) it.next();
			DescriptorDecorator helper = new DescriptorDecorator(aDescriptor);
			helper.setCurrentLocale(currentLocale);
			helper.setIndexingLanguage((String)decodeIndexingLanguageCode(helper.getIndexingLanguageCode()));
			helper.setAccessPointLanguage((String)decodeLanguageAccessPointCode(helper.getAccessPointLanguageCode(),aDescriptor));
			
			helper.setCrossReferenceList(getSummary(getCrossReferences(aDescriptor),View.toIntView(aDescriptor.getUserViewString())));
			newList.add(helper);
		}
		return newList;
	}

	
	private Object decodeIndexingLanguageCode(short indexingLanguageCode) {
		if(indexingLanguageCode==0) return "";
		try {
			DAOCodeTable dao = new DAOCodeTable();
			return dao.getLanguageOfIndexing(indexingLanguageCode);
		} catch(DataAccessException e){
			return "";
		}
		
	}

	private Object decodeLanguageAccessPointCode(short accessPointLanguageCode, Descriptor aDescriptor) {
		if(accessPointLanguageCode==0) return "";
		try {
			DAOCodeTable dao = new DAOCodeTable();
			return dao.getAccessPointLanguage(accessPointLanguageCode,aDescriptor);
		} catch(DataAccessException e){
			return "";
		}
		
	}
	
	private List getCrossReferences(Descriptor aDescriptor) {
		try {
			DAODescriptor daoDescriptor = (DAODescriptor) aDescriptor.getDAO();
			boolean supportoCross = daoDescriptor.supportsCrossReferences();
			if(supportoCross) {			
				return ((DAODescriptor) aDescriptor.getDAO()).getCrossReferences(aDescriptor, View.toIntView(aDescriptor.getUserViewString()));
			}
			else {
				return new ArrayList();
			}
		} catch(DataAccessException e){
			return new ArrayList();
		}
		
	}

	public void setShowResults(boolean b) {
		showResults = b;
	}

	public String getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(String s) {
		logger.debug("setting selectedIndex to '" + s + "'");
		selectedIndex = s;
	}

	public String getLastBrowseTerm() {
		return lastBrowseTerm;
	}
	
	public void setLastBrowseTerm(String string) {
		if (string.length() > 250)
			lastBrowseTerm = string.substring(0, 250);
		else
			lastBrowseTerm = string;
	}

	public BrowseManager getBrowseManager() {
		return browseManager;
	}

	public void setBrowseManager(BrowseManager manager) {
		browseManager = manager;
	}

	public List getXrefCountList() {
		return xrefCountList;
	}

	public void setXrefCountList(List list) {
		xrefCountList = list;
	}

	public int getXrefCount(int i) {
		return ((Integer) getXrefCountList().get(i)).intValue();
	}

	public void setXrefCount(int i, int val) {
		List l = getXrefCountList();
		Integer entry = (Integer) l.get(i);
		l.set(i, new Integer(val));
	}

	public int getDocCount(int i) {
		return ((Integer) getDocCountList().get(i)).intValue();
	}

	public void setDocCount(int i, int val) {
		List l = getDocCountList();
		Integer entry = (Integer) l.get(i);
		l.set(i, new Integer(val));
	}

	/**
	 * 
	 */
	public Descriptor getLastSelectedDescriptor() {
		return lastSelectedDescriptor;
	}

	/**
	 * 
	 */
	public void setLastSelectedDescriptor(Descriptor descriptor) {
		lastSelectedDescriptor = descriptor;
	}

	/**
	 * 
	 */
	public int getTermsToDisplay() {
		return termsToDisplay;
	}

	/**
	 * 
	 */
	public void setTermsToDisplay(int i) {
		termsToDisplay = i;
	}

	/**
	 * 
	 */
	public List getDocCountList() {
		return docCountList;
	}

	/**
	 * 
	 */
	public void setDocCountList(List list) {
		docCountList = list;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public boolean isSupportsCrossReferences() {
		return browseManager.isSupportsCrossReferences();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public boolean isSupportsAuthorities() {
		return browseManager.isSupportsAuthorities();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public List getAuthCountList() {
		return authCountList;
	}

	public int getAuthCount(int index) {
		return ((Integer) getAuthCountList().get(index)).intValue();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setAuthCountList(List list) {
		authCountList = list;
	}

	/* (non-Javadoc)
	 * @see librisuite.bean.searching.SearchBean#getSearchType()
	 */
	public String getSearchType() {
		return "browseSearch"; // not really used at present
	}
	
	public String getSkippedTerm(){
		if(lastBrowseTerm!=null && skipInFiling>0 && lastBrowseTerm.length()>skipInFiling){
			return lastBrowseTerm.substring(0, skipInFiling);
		}
		return "";
	}
	/**
	 * @return Returns the selectedCategory.
	 */
	public short getSelectedCategory() {
		return selectedCategory;
	}

	/**
	 * @param selectedCategory The selectedCategory to set.
	 */
	public void setSelectedCategory(short defaultCategory) {
		this.selectedCategory = defaultCategory;
	}
	
	public String toString() {
		return super.toString()
		+ "\r\n Link Method History: "+ browseLinkMethod
		+ "\r\n Selected Indexes History: "+ selectedIndexHistory
		+ "\r\n Browse Index History: "+ browseIndexHistory;
	}

	public void closeOperation() {
		try {
			popInfo();
		} catch (Exception e) {
			logger.error("Closing Browse operation failed: ignore this error",e);
		} 
	}

	private void popInfo() {
		if(!browseIndexHistory.isEmpty()) {
			retrieveBrowseIndex();
		}
		if(!selectedIndexHistory.isEmpty()) {
			retrieveSelectedIndex();
		}
	}
 
	public boolean isDewey(){
		if(this.getSelectedIndexKey().equals("24P5"))
			return true;
		else 
			return false;
	}
	
	public Short getEditionNbr(int i){
		if(getBrowseList().size()>0){
			Descriptor d = (Descriptor) getBrowseList().get(i);
		 return ((CLSTN)d).getDeweyEditionNumber();
		}
		return null;
	}
	
	/**
	 * used in cBrowse too
	 */
	public boolean isVerificationLevelVisible() {
		return verificationLevelVisible;
	}

	public List getEditorBrowseIndexList() {
		return editorBrowseIndexList;
	}

	public void setEditorBrowseIndexList(List editorBrowseIndexList) {
		this.editorBrowseIndexList = editorBrowseIndexList;
	}
	
	public boolean isThesaurus(){
		if(selectedIndex.equals("TH       "))
			return true;
		else 
			return false;
	}
	
	public boolean isTitle(){
		if(selectedIndex.equals("TI"))
			return true;
		else 
			return false;
	}
	
	public boolean isSupportsMades() {
		return browseManager.isSupportsMades();
	}
	
	public int getMadesCount(int i) {
		return ((Integer) getMadesCountList().get(i)).intValue();
	}
	
	public int getNtCount(int i) {
		return ((Integer) getNtCountList().get(i)).intValue();
	}
	
	
	public boolean isPublisher()
	{
		if ((("PU       ").equalsIgnoreCase(selectedIndex)) || (("PP       ").equalsIgnoreCase(selectedIndex)) )
			return true;
		else 
			return false;
	}
	
	/**
	 * return true if is present the index NTT or NTN
	 * @return
	 */
	public boolean getNtIndex(){
		
		List l = getBrowseIndexList();
		Iterator iter = l.iterator();
		IndexListElement anElem;
		boolean isNT = false;
		while (iter.hasNext()) {
			anElem = (IndexListElement) iter.next();
			if (anElem.getValue().toUpperCase().trim().compareTo("NTT") == 0 || 
				anElem.getValue().toUpperCase().trim().compareTo("NTN") == 0) {
					return isNT = true;
			}
		}
	
		return isNT;
	}
	
	/**
	 * Builds a List of CrossReferenceSummaryElements suitable for use
	 * in the presentation of cross references
	 * For category 17 of the names there is a cross reference list of type 1 (See) and 2 (See Also)
	 * For category 18 of the subjects there is a cross reference list of type 1 (See)
	 * 
	 * @return the list of CrossReferenceSummaryElement
	 */
	public List<CrossReferenceSummaryElement> getSummary(List<REF> xRefs, int cataloguingView){
		List<CrossReferenceSummaryElement> result = new ArrayList<CrossReferenceSummaryElement>();
		Iterator<REF> iter = xRefs.iterator();
		while (iter.hasNext()) {
			REF ref = (REF) iter.next();
			Descriptor hdg;
			try {
				hdg = (Descriptor) ref.getTargetDAO().load(ref.getTarget(), cataloguingView);
				
				
				boolean showAllCrossRef = true;
				try {
					showAllCrossRef = Defaults.getBoolean("show_all_crossreference");
				}
				catch (MissingResourceException e) {
					e.printStackTrace();
				}				
				
				if (!showAllCrossRef) {				
					if(hdg.getCategory()==17 && (ref.getKey().getType()==1 || ref.getKey().getType()==3)){
					    getCrossReferenceSummaryElement(result, ref, hdg, cataloguingView);
			        }
				    else if (hdg.getCategory()==18 && ref.getKey().getType()==1){
					   getCrossReferenceSummaryElement(result, ref, hdg, cataloguingView);
				   }
				}				
				else {	
					getCrossReferenceSummaryElement(result, ref, hdg, cataloguingView);
				}
			} catch (DataAccessException e) {
				return result;
			}
		}
		return result;
	}

	/**
	 * Return a CrossReferenceSummaryElement with the target and the cross reference information  
	 * 
	 * @param result
	 * @param ref
	 * @param hdg
	 * @throws DataAccessException 
	 */
	private void getCrossReferenceSummaryElement(List<CrossReferenceSummaryElement> result, REF ref,
			Descriptor hdg, int cataloguingView) throws DataAccessException {
		CrossReferenceSummaryElement aSummaryElement = new CrossReferenceSummaryElement();
		if (hdg.getStringText() != null)
			aSummaryElement.setTarget(new StringText(hdg.getStringText()).toDisplayString());
		aSummaryElement.setXRef(ref);
		aSummaryElement.setHeadingType(hdg.getHeadingType());
		aSummaryElement.setDocTargetCounts(getDocCounts(hdg,cataloguingView));
		result.add(aSummaryElement);
	}
	
	private int getDocCounts(Descriptor aDescriptor, int cataloguingView)
			throws DataAccessException {
			int count = 0;
			int headingNumber = aDescriptor.getHeadingNumber();				
			String headingtype = aDescriptor.getHeadingType();
			SolrQuery query = new SolrQuery( "*:*" );				
				
			query.setFilterQueries( "authority_group_" + headingtype + ":"+ headingNumber);
			Integer collection_code = getCollectionCode();
			if(collection_code != null && collection_code != 0){
				query.addFilterQuery( "collection_code" + ":"+ collection_code);
			}
			query.setRequestHandler("def");
			query.setRows(0);
				
			QueryResponse res;
			try {
				res = getSolrManager().getSolr().query(query);				
				count = (int) res.getResults().getNumFound();					
			} catch (Exception e) {
				e.printStackTrace();
				logger.debug("An error occurred while trying to retrieve additional information from the Solr server");
			}
			
			return count;
		}
	
	
}
/*
 * (c) LibriCore
 * 
 * Created on Jul 19, 2004
 * 
 * BrowseManager.java
 */
package librisuite.business.searching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import librisuite.business.common.BrowseFailedException;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.descriptor.DAOClassificationDescriptor;
import librisuite.business.descriptor.DAOControlNumberDescriptor;
import librisuite.business.descriptor.DAODescriptor;
import librisuite.business.descriptor.DAONameDescriptor;
import librisuite.business.descriptor.DAONameTitleNameDescriptor;
import librisuite.business.descriptor.DAONameTitleTitleDescriptor;
import librisuite.business.descriptor.DAOPublisherNameDescriptor;
import librisuite.business.descriptor.DAOPublisherPlaceDescriptor;
import librisuite.business.descriptor.DAOShelfList;
import librisuite.business.descriptor.DAOSubjectDescriptor;
import librisuite.business.descriptor.DAOTitleDescriptor;
import librisuite.business.descriptor.Descriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Responsible for the management of a browse session 
 * @author paulm
 * @since 1.0
 */
public class BrowseManager {
	private static final Log logger = LogFactory.getLog(BrowseManager.class);
	
	public static final int MAX_BROWSE_TERM_LENGTH = Defaults.getInteger("browse.max.term.lenght");
	public static final int MAX_SORTFORM_LENGTH = MAX_BROWSE_TERM_LENGTH;
	public static final int SORTFORM_LENGTH = 1080;
	private int pageSize = 10;
	private String browseIndex;
	private static Map<String, Object> daoMap = new HashMap<String, Object>();
	private static Map<String, String> filterMap = new HashMap<String, String>();
	private DAODescriptor dao;
	private String filter;
	private boolean supportsCrossReferences;
	private boolean supportsAuthorities;
	private boolean supportsMades;
		
	public BrowseManager() {
	 				
		daoMap.put("2P0", DAONameDescriptor.class);
		filterMap.put("2P0", "");
		
	    daoMap.put("3P10", DAONameDescriptor.class);
		filterMap.put("3P10", " and hdg.typeCode = 2 ");

		daoMap.put("4P10", DAONameDescriptor.class);
		filterMap.put("4P10", " and hdg.typeCode = 3 ");

		daoMap.put("5P10", DAONameDescriptor.class);
		filterMap.put("5P10", " and hdg.typeCode = 4 ");

		daoMap.put("7P0", DAOTitleDescriptor.class);
		filterMap.put("7P0", "");

		daoMap.put("9P0", DAOSubjectDescriptor.class);
		filterMap.put("9P0", "");
		
		daoMap.put("373P0", DAOSubjectDescriptor.class);
		filterMap.put("373P0", " and hdg.sourceCode = 4 ");
		
		daoMap.put("230P", DAOPublisherNameDescriptor.class);
		filterMap.put("230P", "");

		daoMap.put("243P", DAOPublisherPlaceDescriptor.class);
		filterMap.put("243P", "");

		daoMap.put("250S", DAONameTitleNameDescriptor.class);
		filterMap.put("250S", "");

		daoMap.put("251S", DAONameTitleTitleDescriptor.class);
		filterMap.put("251S", "");

		daoMap.put("16P30", DAOControlNumberDescriptor.class);
		filterMap.put("16P30", "");

		daoMap.put("18P2", DAOControlNumberDescriptor.class);
		filterMap.put("18P2", " and hdg.typeCode = 9 ");

		daoMap.put("19P2", DAOControlNumberDescriptor.class);
		filterMap.put("19P2", " and hdg.typeCode = 10 ");

		daoMap.put("20P3", DAOControlNumberDescriptor.class);
		filterMap.put("20P3", " and hdg.typeCode = 3 ");

		daoMap.put("21P2", DAOControlNumberDescriptor.class);
		filterMap.put("21P2", " and hdg.typeCode = 2 ");

		daoMap.put("22P10", DAOControlNumberDescriptor.class);
		filterMap.put("22P10", " and hdg.typeCode = 93 ");

		daoMap.put("29P20", DAOControlNumberDescriptor.class);
		filterMap.put("29P20", " and hdg.typeCode = 71 ");

		daoMap.put("30P4", DAOControlNumberDescriptor.class);
		filterMap.put("30P4", "");

		daoMap.put("31P3", DAOControlNumberDescriptor.class);
		filterMap.put("31P3", " and hdg.typeCode = 84 ");

		daoMap.put("32P3", DAOControlNumberDescriptor.class);
		filterMap.put("32P3", " and hdg.typeCode = 88 ");

		daoMap.put("33P3", DAOControlNumberDescriptor.class);
		filterMap.put("33P3", " and hdg.typeCode = 90 ");

		daoMap.put("34P20", DAOControlNumberDescriptor.class);
		filterMap.put("34P20", "");

		daoMap.put("35P20", DAOControlNumberDescriptor.class);
		filterMap.put("35P20", "");

		daoMap.put("36P20", DAOControlNumberDescriptor.class);
		filterMap.put("36P20", " and hdg.typeCode = 52 ");

		daoMap.put("51P3", DAOControlNumberDescriptor.class);
		filterMap.put("51P3", " and hdg.typeCode = 89 ");

		daoMap.put("52P3", DAOControlNumberDescriptor.class);
		filterMap.put("52P3", " and hdg.typeCode = 83 ");

		daoMap.put("53P3", DAOControlNumberDescriptor.class);
		filterMap.put("53P3", " and hdg.typeCode = 91 ");

		daoMap.put("54P3", DAOControlNumberDescriptor.class);
		filterMap.put("54P3", " and hdg.typeCode = 97 ");

		daoMap.put("55P3", DAOControlNumberDescriptor.class);
		filterMap.put("55P3", " and hdg.typeCode = 98 ");

	
		daoMap.put("47P40", DAOClassificationDescriptor.class);
		filterMap.put("47P40", " and hdg.typeCode = 21");

		daoMap.put("24P5", DAOClassificationDescriptor.class);
		filterMap.put("24P5", " and hdg.typeCode = 12");

		daoMap.put("25P5", DAOClassificationDescriptor.class);
		filterMap.put("25P5", " and hdg.typeCode = 1");

		daoMap.put("27P5", DAOClassificationDescriptor.class);
		filterMap.put("27P5", " and hdg.typeCode = 6");
		
		daoMap.put("23P5", DAOClassificationDescriptor.class);
		filterMap.put("23P5", " and hdg.typeCode not in (1,6,10,11,12,14,15,29) ");

		daoMap.put("48P3", DAOClassificationDescriptor.class);
		filterMap.put("48P3", " and hdg.typeCode = 10");

		daoMap.put("46P40", DAOClassificationDescriptor.class);
		filterMap.put("46P40", " and hdg.typeCode = 11");

		daoMap.put("50P3", DAOClassificationDescriptor.class);
		filterMap.put("50P3", " and hdg.typeCode = 14");

		daoMap.put("49P3", DAOClassificationDescriptor.class);
		filterMap.put("49P3", " and hdg.typeCode = 15");
		
		daoMap.put("326P1", DAOClassificationDescriptor.class);
		filterMap.put("326P1", " and hdg.typeCode = 29");
		
		daoMap.put("28P30", DAOShelfList.class);
		filterMap.put("28P30", " and hdg.typeCode = '@'");

		daoMap.put("244P30", DAOShelfList.class);
		filterMap.put("244P30", " and hdg.typeCode = 'N'");

		daoMap.put("47P30", DAOShelfList.class);
		filterMap.put("47P30", " and hdg.typeCode = 'M'");

		daoMap.put("37P30", DAOShelfList.class);
		filterMap.put("37P30", " and hdg.typeCode = '2'");

		daoMap.put("38P30", DAOShelfList.class);
		filterMap.put("38P30", " and hdg.typeCode = '3'");

		daoMap.put("39P30", DAOShelfList.class);
		filterMap.put("39P30", " and hdg.typeCode = '4'");

		daoMap.put("41P30", DAOShelfList.class);
		filterMap.put("41P30", " and hdg.typeCode = '6'");

		daoMap.put("42P30", DAOShelfList.class);
		filterMap.put("42P30", " and hdg.typeCode = 'A'");

		daoMap.put("43P30", DAOShelfList.class);
		filterMap.put("43P30", " and hdg.typeCode = 'C'");

		daoMap.put("44P30", DAOShelfList.class);
		filterMap.put("44P30", " and hdg.typeCode = 'E'");

		daoMap.put("45P30", DAOShelfList.class);
		filterMap.put("45P30", " and hdg.typeCode = 'F'");

		daoMap.put("46P30", DAOShelfList.class);
	    filterMap.put("46P30", " and hdg.typeCode = 'G'");
	
		daoMap.put("303P3", DAOClassificationDescriptor.class);
		filterMap.put("303P3", " and hdg.typeCode = 13");
	
		daoMap.put("353P1", DAOClassificationDescriptor.class);
		filterMap.put("353P1", " and hdg.typeCode = 80");
	}
	
	
	//TODO verificare non usato
	public List<Integer> getXrefCounts(List descriptors, int cataloguingView)
		throws DataAccessException {
		List<Integer> result = new ArrayList<Integer>();
		Iterator iter = descriptors.iterator();
		Descriptor aDescriptor = null;
		while (iter.hasNext()) {
			aDescriptor = (Descriptor) iter.next();
			result.add(
				new Integer(
					((DAODescriptor) aDescriptor.getDAO()).getXrefCount(
						aDescriptor,
						cataloguingView)));
		}
		
		return result;
	}
	
	//TODO verificare non usato
	public List<Integer> getXAtrCounts(List descriptors, int cataloguingView)
	throws DataAccessException {
	List<Integer> result = new ArrayList<Integer>();

	Iterator iter = descriptors.iterator();
	Descriptor aDescriptor = null;
	while (iter.hasNext()) {
		aDescriptor = (Descriptor) iter.next();
		
		result.add(
			new Integer(
				((DAODescriptor) aDescriptor.getDAO()).getXAtrCount(
					aDescriptor,
					cataloguingView)));
		}

	return result;
 }
	//TODO verificare non usato
	public List<Integer> getThesaurusNoteCounts(List descriptors, int cataloguingView)
	throws DataAccessException {
	List<Integer> result = new ArrayList<Integer>();

	Iterator iter = descriptors.iterator();
	Descriptor aDescriptor = null;
	while (iter.hasNext()) {
		aDescriptor = (Descriptor) iter.next();
		result.add(
			new Integer(
				((DAODescriptor) aDescriptor.getDAO()).getThesaurusNotesCount(
					aDescriptor,
					cataloguingView)));
	}

	return result;
}


	//TODO verificare non usato
	public List<Integer> getDocCounts(List descriptors, int cataloguingView, Integer collection_code)	
			throws DataAccessException {
		    List<Integer> result = new ArrayList<Integer>();
			Iterator iter = descriptors.iterator();
			Descriptor aDescriptor = null;
			while (iter.hasNext()) {				
				aDescriptor = (Descriptor) iter.next();				
				result.add(0);
			}

			return result;
		}
	
	
	//TODO verificare non usato
	public List<Integer> getDocCountNT(List descriptors, int cataloguingView)
	throws DataAccessException {
	
	List<Integer> result = new ArrayList<Integer>();
	Iterator iter = descriptors.iterator();
	Descriptor aDescriptor = null;

	while (iter.hasNext()) {
		aDescriptor = (Descriptor) iter.next();

		result.add(
			new Integer(
				((DAODescriptor) aDescriptor.getDAO()).getDocCountNT(
					aDescriptor,
					cataloguingView)));
	}
	
	return result;
}

	//TODO verificare non usato	
	public List<Integer> getAuthCounts(List descriptors)
			throws DataAccessException {
			List<Integer> result = new ArrayList<Integer>();
			Iterator iter = descriptors.iterator();
			Descriptor aDescriptor = null;

			while (iter.hasNext()) {
				aDescriptor = (Descriptor) iter.next();
				result.add(0);
				
			}
			return result;
		}
	//TODO verificare non usato	
	public List<Integer> getSimpleAuthCounts(List descriptors) throws DataAccessException {
		
		List<Integer> result = new ArrayList<Integer>();

		Iterator iter = descriptors.iterator();
		Descriptor aDescriptor = null;

		while (iter.hasNext()) {
			aDescriptor = (Descriptor) iter.next();

			result.add(
				new Integer(
					((DAODescriptor) aDescriptor.getDAO()).getAuthCount(
						aDescriptor)));
		}
		
		return result;
	}

	/**
	 * Get the first element of a browse
	 *
	 * @param term
	 * @param cataloguingView
	 * @param termsToDisplay
	 * @param collectionCode
	 * @return
	 * @throws DataAccessException
	*/
	 
	public List getFirstPage(String term, int cataloguingView, int termsToDisplay, int collectionCode) throws DataAccessException 
	{
		//in base alla proprietà SUPPORT_SUBJECT_LOCAL==true istanzia un dao oppure l'altro
	  
		DAODescriptor dao = getDao();	
		setPageSize(termsToDisplay);
		String searchTerm = dao.calculateSearchTerm(term, getBrowseIndex());
		List l = null;

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("looking for a heading < " + searchTerm);
			}
			//se il dao è instanceof di DAOSubjectDescriptor richiamare il suo metodo
			//solo se la proprietà è valorizzata nel defaultValues (support.subject.local=true)
			// altrimenti utilizzare  il super
			
			l = dao.getHeadingsBySortform(
					"<",
					"desc",
					searchTerm,
					getFilter(),
					cataloguingView,
					1, collectionCode);
			
			
			if(!((dao instanceof DAOPublisherNameDescriptor) && !(dao instanceof DAOPublisherPlaceDescriptor))) {
				if (l.size() > 0){
	 				searchTerm=dao.getBrowsingSortForm((Descriptor) l.get(0));
					l.clear();
     			 } 
			}
		
			if (logger.isDebugEnabled()) {
				logger.debug("looking for headings >= " + searchTerm);
			}
			
			l.addAll(
				dao.getHeadingsBySortform(
					">=",
					"",
					searchTerm,
					getFilter(),
					cataloguingView,
					getPageSize(),
					collectionCode));
			
			
		} catch (Exception e) {
			throw new BrowseFailedException();
		}
		
		return l;
	}

	/**
	 * Get the first element of a browse
	 *
	 * @param term
	 * @param cataloguingView
	 * @param termsToDisplay
	 * @param collectionCode
	 * @return
	 * @throws DataAccessException
	*/
	/*public List getFirstElement(String term, int cataloguingView, int termsToDisplay, int collectionCode) throws DataAccessException 
	{
		DAODescriptor dao = getDao();	
		setPageSize(termsToDisplay);
		
		String searchTerm = dao.calculateSearchTerm(term, getBrowseIndex());
		
		List l = null;

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("looking for a heading = " + searchTerm);
			}
		
				l=dao.getHeadingsBySortform(
					"=",
					"",
					searchTerm,
					getFilter(),
					cataloguingView,
					getPageSize(),
					collectionCode);
			
		} catch (Exception e) {
			throw new BrowseFailedException();
		}
		return l;
	}*/


	/**
	 * Set the DAO class that uses the index
	 * Set support cross reference
	 * Set support authority
	 * Set support mades
	 * 
	 * @param key
	 * @param mainLibrary
	 * @throws InvalidBrowseIndexException
	 */
	public void setBrowseIndex(String key, int mainLibrary)
		throws InvalidBrowseIndexException {
		browseIndex = key;

		try {
			Class c =  (Class)daoMap.get(key);
			if (c == null) {
				throw new InvalidBrowseIndexException(key);
			}
			setDao((DAODescriptor) c.newInstance());
		} catch (InstantiationException e) {
			throw new InvalidBrowseIndexException(key);
		} catch (IllegalAccessException e) {
			throw new InvalidBrowseIndexException(key);
		}
		setSupportsCrossReferences(getDao().supportsCrossReferences());
		setSupportsAuthorities(getDao().supportsAuthorities());
		setSupportsMades(getDao().supportsMades());
		setFilter((String)filterMap.get(key));
		if (getDao() instanceof DAOShelfList) {
			setFilter(getFilter() + " and hdg.mainLibraryNumber = " + mainLibrary);
		}
	}

	/**
	 * Get the following page of browse terms
	 * 
	 * @param d the descriptor displayed at the bottom of the current page
	 * @param cataloguingView the view being displayed
	 * @since 1.0
	 */
	public List getNextPage(String nextTerm/*Descriptor d*/, int cataloguingView, int collectionCode)
		throws DataAccessException {
		
		DAODescriptor dao = getDao();

		List l = null;
		String operator = ">";
		
		
		if( (dao instanceof DAOPublisherNameDescriptor || 
				dao instanceof DAOPublisherPlaceDescriptor) && /*dao.getBrowsingSortForm(d)*/nextTerm.indexOf(":")>0){
			operator = ">=";
		}

		try {
			
			l =
				dao.getHeadingsBySortform(
					operator,
					"",
					nextTerm
					/*dao.getBrowsingSortForm(d)*/,
					getFilter(),
					cataloguingView,
					getPageSize(),
					collectionCode);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BrowseFailedException();
		}
		
		return l;

	}
	
	/**
	 * Get the preceding page of browse terms
	 * 
	 * @param d the descriptor displayed at the bottom of the current page
	 * @param cataloguingView the view being displayed
	 * @since 1.0
	 */
	public List getPreviousPage(String lastSortForm/*Descriptor d*/, int cataloguingView, int collectionCode)
		throws DataAccessException {
		
		DAODescriptor dao = getDao();

		List l = null;
		List result = new ArrayList();
		String operator = "<";
		
	
		if( (dao instanceof DAOPublisherNameDescriptor || 
				dao instanceof DAOPublisherPlaceDescriptor) && /*dao.getBrowsingSortForm(d)*/ lastSortForm.indexOf(":")>0){
			operator = "<=";
		}

		
		try {
			
			l =
				dao.getHeadingsBySortform(
					operator,
					"desc",
					lastSortForm,
					getFilter(),
					cataloguingView,
					getPageSize(),
					collectionCode);
			
			for (int i = l.size() - 1; i >= 0; i--) {
				result.add(l.get(i));
			}

		} catch (Exception e) {
			throw new BrowseFailedException();
		}
		
		return result;

	}

	public DAODescriptor getDao() {
		return dao;
	}

	public void setDao(DAODescriptor descriptor) {
		dao = descriptor;
	}

	private int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int i) {
		pageSize = i;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String string) {
		filter = string;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public boolean isSupportsCrossReferences() {
		return supportsCrossReferences;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setSupportsCrossReferences(boolean b) {
		supportsCrossReferences = b;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public boolean isSupportsAuthorities() {
		return supportsAuthorities;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setSupportsAuthorities(boolean b) {
		supportsAuthorities = b;
	}
	
	public void setSupportsMades(boolean b) {
		supportsMades = b;
	}
	public boolean isSupportsMades() {
		return supportsMades;
	}



	/**
	 * 
	 * @since 1.0
	 */
	public String getBrowseIndex() {
		return browseIndex;
	}
	
	/**
	 * TODO passare il count da Solr
	 * @param descriptors
	 * @return
	 * @throws DataAccessException
	 */
	public List<Integer> getMadesCounts(List descriptors, int cataloguingView) throws DataAccessException {
		List<Integer> result = new ArrayList<Integer>();
		result.add(0);
		return result;
	}



	
}

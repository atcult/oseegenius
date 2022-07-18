/*
 * (c) LibriCore
 * 
 * Created on Jun 21, 2004
 * 
 * DAOPublisherDescriptor.java
 */
package librisuite.business.descriptor;

import java.util.List;

import librisuite.business.common.DataAccessException;
import librisuite.business.common.SortFormException;
import librisuite.hibernate.PUBL_HDG;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.type.Type;

/**
 * Manages headings in the PUBL_HDG table
 * 
 * @author paulm
 */
public class DAOPublisherDescriptor extends DAODescriptor {
	static protected Class persistentClass = PUBL_HDG.class;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.libricore.librisuite.business.Descriptor#getPersistentClass()
	 */
	public Class getPersistentClass() {
		return DAOPublisherDescriptor.persistentClass;
	}

	public Descriptor getMatchingHeading(Descriptor d) 
			throws DataAccessException, SortFormException {
		PUBL_HDG dp = (PUBL_HDG) d;
		try {
			List l = currentSession().find("from " 
					+ getPersistentClass().getName() 
					+ " as c " 
					+ " where c.nameStringText = ?" 
					+ " and c.placeStringText = ? "
					+ " and c.indexingLanguage = ? "
					+ " and c.accessPointLanguage = ?" 
					+ " and c.key.userViewString = ? ",
					new Object[] { 
						dp.getNameStringText(), 
						dp.getPlaceStringText(), 
						new Integer(dp.getIndexingLanguage()),
						new Integer(dp.getAccessPointLanguage()),
						dp.getUserViewString()}, 
					new Type[] { 
						Hibernate.STRING, 
						Hibernate.STRING, 
						Hibernate.INTEGER,
						Hibernate.INTEGER,
						Hibernate.STRING });
			if (l.size() == 1) {
				return (Descriptor) l.get(0);
			} else {
				return null;
			}
		} catch (HibernateException e) {
			logAndWrap(e);
			return null;
		}
	}

	public boolean isMatchingAnotherHeading(Descriptor d) {
		PUBL_HDG dp = (PUBL_HDG) d;
		try {
			List l = currentSession().find(
						"select count(*) from " 
						+ getPersistentClass().getName() 
						+ " as c "
						+ " where c.nameStringText = ?" 
						+ " and c.placeStringText = ? "
						+ " and c.indexingLanguage = ? "
						+ " and c.accessPointLanguage = ?" 
						+ " and c.key.userViewString = ? "
						+ " and c.key.headingNumber <> ?",
					new Object[] { 	dp.getNameStringText(), 
							dp.getPlaceStringText(), 
							new Integer(dp.getIndexingLanguage()),
							new Integer(dp.getAccessPointLanguage()),
							dp.getUserViewString(),
							new Integer(d.getKey().getHeadingNumber()) }, 
				
					new Type[] { Hibernate.STRING, 
							Hibernate.STRING, 
							Hibernate.INTEGER,
							Hibernate.INTEGER,
							Hibernate.STRING, 
							Hibernate.INTEGER
						});
			return ((Integer) l.get(0)).intValue() > 0;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * @see
	 * @link DAODescriptor#calculateSortForm(Descriptor)
	 */
	public String calculateSplittedSortForm(String stringText, SortFormParameters parms) 
			throws DataAccessException, SortFormException {
		if ("".equals(stringText)) {
			return " ";
		}
		//return calculateSortForm(stringText, parms);
		if(SWITCH_DATABASE.equals("oracle"))
			return calculateSortFormForOracle(stringText, parms);
		else
			return calculateSortFormForOracle(stringText, parms);
	}

	public String calculateNameSortForm(PUBL_HDG d) 
			throws DataAccessException, SortFormException {
		return calculateSplittedSortForm(d.getNameStringText(), getNameSortFormParameters());
	}

	public String calculatePlaceSortForm(PUBL_HDG d) 
			throws DataAccessException, SortFormException {
		return calculateSplittedSortForm(d.getPlaceStringText(), getPlaceSortFormParameters());
	}

	private SortFormParameters getNameSortFormParameters() {
		return new SortFormParameters(100, 104, 1, 0, 0);
	}

	private SortFormParameters getPlaceSortFormParameters() {
		return new SortFormParameters(100, 104, 2, 0, 0);
	}

	/**
	 * MIKE: moved here from BroweManager
	 */
	public String calculateSearchTerm(String term, String browseIndex)
			throws DataAccessException {
		// calculate the sortform of the search term
		String searchTerm = super.calculateSearchTerm(term, browseIndex);
		
		if(term.indexOf(":")>0){
			String[] parsedTerm = term.split(":");
			//searchTerm = term.toUpperCase();
			String place = parsedTerm[0].trim();
			String name = parsedTerm[1].trim();
			searchTerm = calculateSearchTerm(place, browseIndex) +" : "+calculateSearchTerm(name, browseIndex);
		}
		return searchTerm;
	}

}

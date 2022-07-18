/*
 * (c) LibriCore
 * 
 * Created on Dec 1, 2004
 * 
 * DAOClassificationDescriptor.java
 */
package librisuite.business.descriptor;

import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.SortFormException;
import librisuite.hibernate.CLSTN;

/**
 * @author paulm
 * @version $Revision: 1.3 $, $Date: 2005/12/12 12:54:37 $
 * @since 1.0
 */
public class DAOClassificationDescriptor extends DAODescriptor {
	private static final Class persistentClass = CLSTN.class;

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#getPersistentClass()
	 */
	public Class getPersistentClass() {
		return persistentClass;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#supportsCrossReferences()
	 */
	public boolean supportsCrossReferences() {
		return false;
	}
	
	public Descriptor getMatchingHeading(Descriptor desc)
		throws DataAccessException, SortFormException {
	CLSTN d = (CLSTN) desc;
	List l =null;
	try {
		if(d.getTypeCode()==12){
		 l =
			currentSession().find(
				"from "
					+ getPersistentClass().getName()
					+ " as c "
					+ " where c.stringText = ?" 
					+ " and c.typeCode = ?" // difference from standard
					+ " and c.deweyEditionNumber =?"
					+ " and c.key.userViewString = ? ",
					
				new Object[] {
					d.getStringText(),
					new Integer(d.getTypeCode()), // difference from standard
					d.getDeweyEditionNumber(),
					d.getUserViewString()},
				new Type[] {
					Hibernate.STRING,
					Hibernate.INTEGER,
					Hibernate.SHORT,
					Hibernate.STRING});
		}else{
			 l =
					currentSession().find(
						"from "
							+ getPersistentClass().getName()
							+ " as c "
							+ " where c.stringText = ?" 
							+ " and c.typeCode = ?" // difference from standard
							+ " and c.key.userViewString = ? ",
						new Object[] {
							d.getStringText(),
							new Integer(d.getTypeCode()), // difference from standard
							d.getUserViewString()},
						new Type[] {
							Hibernate.STRING,
							Hibernate.INTEGER,
							Hibernate.STRING});
			
		}
		if (l!=null && l.size() == 1) {
			return (Descriptor) l.get(0);
		} else {
			return null;
		}
		
	} catch (HibernateException e) {
		logAndWrap(e);
		return null;
	}
}
	/**
	 * Notice:
	 * In the past some sortforms included "extra" information to help in identifying 
	 * uniqueness (some headings added a code for "language of access point" 
	 * so that otherwise identical headings could be differentiated if they had 
	 * different languages).  
     * Now, the only example I can think of is the Dewey Decimal classification 
     * where the sortform includes the Dewey Edition Number so that identical 
     * numbers from different editions will have different sortforms.
     * 
	 * @param d
	 * @return
	 * @throws DataAccessException
	 * @throws SortFormException
	 */
	public boolean isMatchingAnotherHeading(Descriptor desc) {
		CLSTN d = (CLSTN) desc;
		List l =null;
		try {
		if(d.getTypeCode()==12){
			 l =
				currentSession().find(
					"select count(*) from "
						+ getPersistentClass().getName()
						+ " as c "
						+ " where c.stringText = ?" 
						+ " and c.typeCode = ?" // difference from standard
						+ " and c.deweyEditionNumber =?"
						+ " and c.key.userViewString = ? "
						+ " and c.key.headingNumber <> ?",
					new Object[] {
							d.getStringText(),
							new Integer(d.getTypeCode()), // difference from standard
							d.getDeweyEditionNumber(),
							d.getUserViewString(),
							new Integer(d.getKey().getHeadingNumber()) },
					new Type[] {
							Hibernate.STRING,
							Hibernate.INTEGER,
							Hibernate.SHORT,
							Hibernate.STRING, 
							Hibernate.INTEGER });
		}else{
			 l =
					currentSession().find(
						"select count(*) from "
							+ getPersistentClass().getName()
							+ " as c "
							+ " where c.stringText = ?" 
							+ " and c.typeCode = ?" // difference from standard
							+ " and c.key.userViewString = ? "
							+ " and c.key.headingNumber <> ?",
						new Object[] {
								d.getStringText(),
								new Integer(d.getTypeCode()), // difference from standard
								d.getUserViewString(),
								new Integer(d.getKey().getHeadingNumber()) },
						new Type[] {
								Hibernate.STRING,
								Hibernate.INTEGER,
								Hibernate.STRING, 
								Hibernate.INTEGER });
		}
			return ((Integer) l.get(0)).intValue() > 0;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Returns the first n rows having sortform > term
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * 
	 */
	public List getHeadingsBySortform(String operator, String direction,
			String term, String filter, int headingView, int count, int collectionCode)
			throws DataAccessException, InstantiationException, IllegalAccessException {
		Session s = currentSession();

		List l = null;
		Query q = null;
		try {
			if (collectionCode != 0) {
				l = super.getHeadingsByCollectionId(operator, direction, term, filter,
						headingView, count, collectionCode, s);
			} 
           else {
				if (filter.indexOf("80") != -1) {
					l = getSuggestHeading(operator, direction, term,
							headingView, count, s);
				}

				else {
					q = s.createQuery("from "
							+ getPersistentClass().getName()
							+ " as hdg where hdg.sortForm "
							+ operator
							+ " :term  and "
							+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' "
							+ filter + " order by hdg.sortForm " + direction);
					q.setString("term", term);
					q.setInteger("view", headingView);
					q.setMaxResults(count);

					l = q.list();
				}
			}
			if (l.size() > 0)
				l = isolateViewForList(l, headingView);

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		
		return l;
	}

	private List getSuggestHeading(String operator, String direction,
			String term, int headingView, int count, Session s)
			throws HibernateException, DataAccessException {
		String filter;
		List l;
		Query q;
		filter = " and hdg.typeCode = 29";
		q = s
		.createQuery("select distinct hdg from "
				+ getPersistentClass().getName()
				+ " as hdg  where hdg.sortForm "
				+ operator
				+ " :term  and "
				+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' "
				+ filter
				+ " and (hdg.key.headingNumber in (select distinct acs.headingNumber from ClassificationAccessPoint as acs where "
				+ "  acs.functionCode=81) "
				+ " OR hdg.key.headingNumber in (select distinct acs2.headingNumber from MadesClassificationAccessPoint as acs2 where "
				+ " acs2.functionCode=81)) "
				+ " order by hdg.sortForm " + direction);
		q.setString("term", term);
		q.setInteger("view", headingView);
		q.setMaxResults(count);

		l = q.list();
		l = isolateViewForList(l, headingView);
		//Per i clienti che non hanno Mades
		if (l.size() == 0) {
			q = s
					.createQuery("select distinct hdg from "
							+ getPersistentClass().getName()
							+ " as hdg , ClassificationAccessPoint as acs2 where hdg.sortForm "
							+ operator
							+ " :term  and "
							+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' "
							+ filter
							+ " and hdg.key.headingNumber=acs2.headingNumber"
							+ " and acs2.functionCode=81 "
							+ " order by hdg.sortForm " + direction);
			q.setString("term", term);
			q.setInteger("view", headingView);
			q.setMaxResults(count);

			l = q.list();
			l = isolateViewForList(l, headingView);

		}
		return l;
	}
}

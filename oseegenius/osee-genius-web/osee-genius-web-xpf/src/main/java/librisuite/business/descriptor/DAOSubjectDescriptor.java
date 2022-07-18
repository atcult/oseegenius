/*
 * Created on Sep 15, 2004
 *
 */
package librisuite.business.descriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.hibernate.NME_HDG;
import librisuite.hibernate.SBJCT_HDG;

/**
 * Class comment
 * @author janick
 */
public class DAOSubjectDescriptor extends DAODescriptor {
	public final boolean SUPPORT_SUBJECT_ACCESS_POINT_LANGUAGE = Defaults.getBoolean("support.subject.access.point.language");

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#getPersistentClass()
	 */
	public Class getPersistentClass() {
		return SBJCT_HDG.class;
	}


	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#supportsAuthorities()
	 */
	public boolean supportsAuthorities() {
		return true;
	}
	public boolean isMatchingAnotherHeading(Descriptor desc) {
		SBJCT_HDG d = (SBJCT_HDG) desc;
		try {
			List l = currentSession().find(
					"select count(*) from "
							+ getPersistentClass().getName()
							+ " as c "
							+ " where c.stringText= ? "
							+ " and c.accessPointLanguage = ?" 
							+ " and c.typeCode =? "
							+ " and c.sourceCode =? "
							+ " and c.key.userViewString = ?"
							+ " and c.key.headingNumber <> ?",
					new Object[] { 
							d.getStringText(),
							new Integer(d.getAccessPointLanguage()),
							new Integer(d.getTypeCode()),
							new Integer(d.getSourceCode()),
							d.getUserViewString(),
							new Integer(d.getKey().getHeadingNumber()) },
					new Type[] { Hibernate.STRING,
							Hibernate.INTEGER,
						    Hibernate.INTEGER, 
							Hibernate.INTEGER,
							Hibernate.STRING,
							Hibernate.INTEGER});
			return ((Integer) l.get(0)).intValue() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns the first n rows having sortform > term
	 * 
	 * @param operator
	 * @param direction
	 * @param term
	 * @param filter
	 * @param cataloguingView
	 * @param count
	 * @param collectionCode
	 * @return the first n rows having sortform > term
	 * @throws DataAccessException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public List getHeadingsBySortform(String operator, String direction,
			String term, String filter, int cataloguingView, int count,
			int collectionCode) throws DataAccessException,
			InstantiationException, IllegalAccessException {
		
		Session s = currentSession();
		List<Descriptor> l = null;
		String accessPointLanguage = "";
				
		try {
			if(SUPPORT_SUBJECT_ACCESS_POINT_LANGUAGE)
				accessPointLanguage = " hdg.accessPointLanguage = 255 and ";
			
			if (collectionCode != 0) {
				l = getHeadingsByCollectionId(operator, direction, term, filter,
						cataloguingView, count, collectionCode, s);
				
			} else {
					Query q = s.createQuery("from "
						+ getPersistentClass().getName()
						+ " as hdg where hdg.sortForm " + operator
						+ " :term  and "
						+ accessPointLanguage
						+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' "						
						+ filter + " order by hdg.sortForm " + direction);
				q.setString("term", term);
				q.setInteger("view", cataloguingView);
				q.setMaxResults(count);				
				l = q.list();
				
			}
			if (l.size() > 0)
				l = isolateViewForList(l, cataloguingView);
			
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return l;
	}
	
	/**
	 * Return a list of descriptor 
	 * added filter for collection
	 * l return a list of descriptor tied to a record
	 * l2 return a list of descriptor tied to a cross-reference
	 * 
	 * @param operator
	 * @param direction
	 * @param term
	 * @param filter
	 * @param cataloguingView
	 * @param count
	 * @param collectionCode
	 * @param s
	 * @return  a list of descriptor 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws HibernateException
	 */
	public List<Descriptor> getHeadingsByCollectionId(String operator,
			String direction, String term, String filter, int cataloguingView,
			int count, int collectionCode, Session s)
			throws InstantiationException, IllegalAccessException,
			HibernateException {
		
		List <Descriptor> l;
		List<Descriptor> l2;
		Descriptor descriptor = null;
		String accessPointLanguage = "";
		descriptor = (Descriptor) getPersistentClass().newInstance();
		if(SUPPORT_SUBJECT_ACCESS_POINT_LANGUAGE)
			accessPointLanguage = " and hdg.accessPointLanguage = 255 ";
	
		Query q =
				s.createQuery(
					"select distinct hdg  from "
						+ getPersistentClass().getName()
						+ " as hdg ,"
						+ descriptor.getAccessPointClass().getName()
						+ " as apf, "
						+ " CLCTN_ACS_PNT "
						+ " as c "
						+ " where hdg.key.headingNumber = apf.headingNumber "
						+ " and apf.bibItemNumber=c.bibItemNumber "
						+ " and c.collectionNumber ="
						+ collectionCode
						+  " and hdg.sortForm "
						+ operator
						+ " :term  and "
						+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' "
						+ accessPointLanguage
						+ filter
						+ " order by hdg.sortForm "
						+ direction);
			q.setString("term", term);
			q.setInteger("view", cataloguingView);
			q.setMaxResults(count);
			l = q.list();
		
	 if(supportsCrossReferences()){
			Query q2 =
					s.createQuery(
						"select distinct hdg from "
							+ getPersistentClass().getName()
							+ " as hdg ,"
							+ descriptor.getAccessPointClass().getName()
							+ " as apf, "
							+ descriptor.getReferenceClass(descriptor.getClass()).getName()
							+ " as ref, "
							+ " CLCTN_ACS_PNT "
							+ " as c "
							+ " where "
							+ " NOT EXISTS "
							+ "(select acs.headingNumber from "
							+  descriptor.getAccessPointClass().getName()
							+ " as acs where acs.headingNumber= hdg.key.headingNumber ) " 
							+ " and hdg.key.headingNumber =  ref.key.source "
							+ " and apf.headingNumber =  ref.key.target "
							+ " and apf.bibItemNumber = c.bibItemNumber "
							+ " and c.collectionNumber ="
							+ collectionCode
							+  " and hdg.sortForm "
							+ operator
							+ " :term  and "
							+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' "
							+ accessPointLanguage
							+ filter
							+ " order by hdg.sortForm "
							+ direction);
				q2.setString("term", term);
				q2.setInteger("view", cataloguingView);
				q2.setMaxResults(count);
				l2 = q2.list();
				l.addAll(l2);
			 }
				sortList(operator, l);
				removeItems(count, l);
		return l;
	}
	
	/**
	 * Remove items exceeding count
	 * 
	 * @param count
	 * @param l
	 */
	private void removeItems(int count, List<Descriptor> l) {
		if(l.size()>count){
			List<Descriptor> l3 = new ArrayList<Descriptor>();
			Iterator<Descriptor> ite = l.iterator();
			int i = 1;
			while(ite.hasNext()){
				l3.add(ite.next());
				if((count==i))
					break;
					i++;
		    }
			l.removeAll(l);
			l.addAll(l3);
		}
	}

	
	/**
	 * Sort a list of Descriptor(Heading)
	 * 
	 * @param operator
	 * @param l
	 */
	private void sortList(String operator, List<Descriptor> l) {
		if(operator.indexOf(">")!=-1){
			Collections.sort(l, new Comparator<Descriptor>() {
				 @Override
				 public int compare(final Descriptor o1, final Descriptor o2) {
				       return o1.getSortForm().compareTo(o2.getSortForm());
				   }

		    });
		}
		else{
			Collections.sort(l, new Comparator<Descriptor>() {
				 @Override
				 public int compare(final Descriptor o1, final Descriptor o2) {
				       return o2.getSortForm().compareTo(o1.getSortForm());
				   }

		    });
			
		}
	}





}

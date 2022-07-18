/*
 * (c) LibriCore
 * 
 * Created on Jun 21, 2004
 * 
 * Descriptor.java
 */
package librisuite.business.descriptor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import librisuite.business.cataloguing.common.AccessPoint;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Persistence;
import librisuite.business.common.ReferentialIntegrityException;
import librisuite.business.common.SortFormException;
import librisuite.business.common.View;
import librisuite.business.searching.BrowseManager;
import librisuite.business.searching.DAOIndexList;
import librisuite.hibernate.DescriptorKey;
import librisuite.hibernate.NME_HDG;
import librisuite.hibernate.NME_TTL_HDG;
import librisuite.hibernate.PUBL_HDG;
import librisuite.hibernate.REF;
import librisuite.hibernate.ReferenceType;
import librisuite.hibernate.SBJCT_HDG;
import librisuite.hibernate.TTL_HDG;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;

import com.libricore.librisuite.common.HibernateUtil;
import com.libricore.librisuite.common.TransactionalHibernateOperation;

/**
 * An abstract class representing separately indexed tables that access bibliographic
 * items.  (e.g. Name, Title, Subject, ...)
 * 
 * Descriptor objects contain members to identify unique keys for "headings" and also
 * contain a reference to the associated persistence class.
 * 
 * @author paulm
 */
public abstract class DAODescriptor extends HibernateUtil {


	public static final String BLANK_SORTFORM = " ";

	protected static Log logger = LogFactory.getLog(DAODescriptor.class);
	
	String SWITCH_DATABASE = System.getProperty("switch.database");
	/**
	 * Using delegation to mades descriptor
	 */
	private static DAOMadesDescriptor madesDescriptor = new DAOMadesDescriptor(); 
	
	/**
	 * gets the name of the associated Persistent class
	 * 
	 * @return the name
	 */
	public abstract Class getPersistentClass();

	/**
	 * default implemenation indicating whether the Descriptor class may
	 * have cross-references (default true)
	 * 
	 * @since 1.0
	 */
	public boolean supportsCrossReferences() {
		return true;
	}

	/**
	 * default implemenation indicating whether the Descriptor class may
	 * have authorities (default false)
	 * 
	 * @since 1.0
	 */
	public boolean supportsAuthorities() {
		return false;
	}
	
	/**
	 * default implemenation indicating whether the Descriptor class may
	 * have mades (default false)
	 * 
	 * @since 1.0
	 */
	public boolean supportsMades() {
		return DAOMadesDescriptor.madesSupported;
	} 

	/**
	 * Calculates the sort form of a string (search term) when the type of descriptor
	 * is not known, or not available.  Note that this method is overloaded.  The 
	 * (Descriptor) version should be used for calculating sortforms when a Descriptor
	 * is available
	 * 
	 * @since 1.0
	 */
	public String calculateSortForm(String in) throws DataAccessException {
		String result = "";
		int bufSize = 300;

		if ("".equals(in)) {
			return BLANK_SORTFORM;
		}

		Session s = currentSession();
		CallableStatement proc = null;
		try {
			Connection connection = s.connection();
			proc = connection.prepareCall(
				"{ call PACK_SORTFORM.SF_NORMALIZE(?, ?, ?, ?) }");
		
			proc.setString(1, in);
			proc.setInt(2, bufSize);
			proc.registerOutParameter(3, Types.INTEGER);
			proc.registerOutParameter(4, Types.VARCHAR);
			proc.execute();
			result = proc.getString(4);
		} catch (HibernateException e) {
			logAndWrap(e);
		} catch (SQLException e) {
			logAndWrap(e);
		}
		finally{
			try {
				if(proc!=null) proc.close();
			} catch (SQLException e) {
				// do nothing
				e.printStackTrace();
			}
		
		}
		return result;
	}

	/**
	 * Calculates the sortform of a descriptor.  The method is overloaded.  The (String) 
	 * version can be used to normalize a string when no Descriptor is available.
	 * 
	 * @since 1.0
	 */
	public String calculateSortForm(Descriptor d)
		throws DataAccessException, SortFormException {

		if ("".equals(d.getStringText())) {
			return BLANK_SORTFORM;
		}

		SortFormParameters parms = d.getSortFormParameters();
		//return calculateSortForm(d.getStringText(), parms);
		if(SWITCH_DATABASE!=null && SWITCH_DATABASE.equals("postgres"))
			return calculateSortFormForPostgres(d.getStringText(), parms);
		else
			return calculateSortFormForOracle(d.getStringText(), parms);
	}

	/**
	 * Calculates the sortform of a String with known search index.  
	 * The method is overloaded.  (String) and (Descriptor) versions exist.
	 * 
	 * @since 1.0
	 */
	public String calculateSortForm(String s, String browseIndexKey)
		throws DataAccessException, SortFormException {
		if ("".equals(s)) {
			return " ";
		}

		SortFormParameters parms =
			new DAOIndexList().getSortFormParametersByKey(browseIndexKey);
		if(SWITCH_DATABASE!=null && SWITCH_DATABASE.equals("postgres"))
			return calculateSortFormForPostgres(s, parms);
			
		else
			return calculateSortFormForOracle(s, parms);
			
	}

	/*Per postgress*/
	public String calculateSortFormForPostgres(String text, SortFormParameters parms)
		throws DataAccessException, SortFormException {
		String result = "";
		int bufSize = 300;

		Session s = currentSession();
		CallableStatement proc = null;
		Connection connection = null;
		int rc;
		try {
			connection = s.connection();
			proc =
				connection.prepareCall(
					"{ call PACK_SORTFORM.SF_PREPROCESS(?, ?, ?, ?, ?, ?, ?, ?, ?) }");
			proc.setString(1, text);
			proc.setInt(2, bufSize);
			proc.setInt(3, parms.getSortFormMainType());
			proc.setInt(4, parms.getSortFormSubType());
			proc.setInt(5, parms.getNameTitleOrSubjectType());
			proc.setInt(6, parms.getNameSubtype());
			proc.setInt(7, parms.getSkipInFiling());
			proc.registerOutParameter(8, Types.INTEGER);
			proc.registerOutParameter(9, Types.VARCHAR);
			proc.execute();

			rc = proc.getInt(8);

			if (rc != 0) {
				throw new SortFormException(String.valueOf(rc));
			}
			result = proc.getString(9);

		
			proc.close();
			
			proc =
				connection.prepareCall(
					"{ call PACK_SORTFORM.SF_BUILDSRTFRM(?, ?, ?, ?, ?, ?, ?, ?, ?) }");
			proc.setString(1, result);
			proc.setInt(2, bufSize);
			proc.setInt(3, parms.getSortFormMainType());
			proc.setInt(4, parms.getSortFormSubType());
			proc.setInt(5, parms.getNameTitleOrSubjectType());
			proc.setInt(6, parms.getNameSubtype());
			proc.setInt(7, parms.getSkipInFiling());
			proc.registerOutParameter(8, Types.INTEGER);
			proc.registerOutParameter(9, Types.VARCHAR);
			proc.execute();

			rc = proc.getInt(8);

			if (rc != 0) {
				throw new SortFormException(String.valueOf(rc));
			}
			result = proc.getString(9);
		} catch (HibernateException e) {
			logAndWrap(e);
		} catch (SQLException e) {
			logAndWrap(e);
		} finally {
			try {
				if(proc!=null) {
					proc.close();
				}
			} catch (SQLException e) {
				// do nothing
				e.printStackTrace();
			}
		}

		return result;
	}
	
	
	/**
	 * Calculates the sort form of the string text of the heading
	 * 
	 * @param text
	 * @param parms
	 * @returnt the sort form of the string text in capital letters and without diacritical
	 * @throws DataAccessException
	 * @throws SortFormException
	 */
	public String calculateSortFormForOracle(String text, SortFormParameters parms)
			throws DataAccessException, SortFormException {
		String result = "";
		int bufSize = 300;

		Session s = currentSession();
		CallableStatement proc = null;
		Connection connection = null;
		int rc;
		try {
			connection = s.connection();
			proc = connection
					.prepareCall("{ ? = call AMICUS.PACK_SORTFORM.SF_PREPROCESS(?, ?, ?, ?, ?, ?, ?, ?) }");
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, text);
			proc.registerOutParameter(3, Types.VARCHAR);
			proc.setInt(4, bufSize);
			proc.setInt(5, parms.getSortFormMainType());
			proc.setInt(6, parms.getSortFormSubType());
			proc.setInt(7, parms.getNameTitleOrSubjectType());
			proc.setInt(8, parms.getNameSubtype());
			proc.setInt(9, parms.getSkipInFiling());
			proc.execute();

			rc = proc.getInt(1);

			if (rc != 0) {
				throw new SortFormException(String.valueOf(rc));
			}
			result = proc.getString(3);

			proc.close();

			proc = connection
					.prepareCall("{ ? = call AMICUS.PACK_SORTFORM.SF_BUILDSRTFRM(?, ?, ?, ?, ?, ?, ?, ?) }");
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, result);
			proc.registerOutParameter(3, Types.VARCHAR);
			proc.setInt(4, bufSize);
			proc.setInt(5, parms.getSortFormMainType());
			proc.setInt(6, parms.getSortFormSubType());
			proc.setInt(7, parms.getNameTitleOrSubjectType());
			proc.setInt(8, parms.getNameSubtype());
			proc.setInt(9, parms.getSkipInFiling());
			proc.execute();

			rc = proc.getInt(1);

			if (rc != 0) {
				throw new SortFormException(String.valueOf(rc));
			}
			result = proc.getString(3);
		} catch (HibernateException e) {
			logAndWrap(e);
		} catch (SQLException e) {
			logAndWrap(e);
		} finally {
			try {
				if (proc != null) {
					proc.close();
				}
			} catch (SQLException e) {
				// do nothing
				e.printStackTrace();
			}
		}
		return result;
	}

	
	/**
	 * Loads the heading member from the database based on the settings of the key values
	 * 
	 * @param headingNumber
	 * @param cataloguingView
	 * @return the heading from the database
	 * @throws DataAccessException
	 */
	public Descriptor load(int headingNumber, int cataloguingView)
		throws DataAccessException {
		return load(headingNumber, cataloguingView, getPersistentClass());
	}

	/**
	 * Loads the heading member from the database based on the settings of the key values
	 * 
	 * @param headingNumber
	 * @param cataloguingView
	 * @param persistentClass
	 * @return the heading from the database
	 * @throws DataAccessException
	 */
	public Descriptor load(
		int headingNumber,
		int cataloguingView,
		Class persistentClass)
		throws DataAccessException {

		List l =
			find(
				"from "
					+ persistentClass.getName()
					+ " as hdg where hdg.key.headingNumber = ? "
					+ " AND substr(hdg.key.userViewString, ?, 1) = '1'",
				new Object[] {
					new Integer(headingNumber),
					new Integer(cataloguingView)},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });

		Descriptor result = null;
		if (!l.isEmpty()) {
			result = (Descriptor) l.get(0);
			result = (Descriptor) isolateView(result, cataloguingView);
		} else {
			logger.warn(
				"No descriptor of type '"
					+ persistentClass.getName()
					+ "' found for heading number '"
					+ headingNumber
					+ "' and user view '"
					+ cataloguingView
					+ "'");
		}

		return result;
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
		
		try {
			if (collectionCode != 0) {
				l = getHeadingsByCollectionId(operator, direction, term, filter,
						cataloguingView, count, collectionCode, s);
				
			} else {
				Query q = s.createQuery("from "
						+ getPersistentClass().getName()
						+ " as hdg where hdg.sortForm " + operator
						+ " :term  and "
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
		descriptor = (Descriptor) getPersistentClass().newInstance();
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

	
	/**
	 * This method was created to manage the absence of sortform.
     * The problem occurs when n headers have the same sortform
     * and you want to list less than n elements (maxResults <n, count <n)
     * the elements between maxResults and n would never be visualized.
     * As this event occurs especially when searching for searchTerm = ""
     * ad hoc research was carried out. The searchTerm = "" is created in the save
     * a heading whenever the sortform creation fails (see triggers on
     * tables AMICUS.XXX_HDG 
	 * 
	 * @param operator
	 * @param direction
	 * @param term
	 * @param filter
	 * @param cataloguingView
	 * @param count
	 * @return a list of Descriptor(Heading)
	 * @throws DataAccessException
	 */
	public List getHeadingsByBlankSortform(
			String operator,
			String direction,
			String term,
			String filter,
			int cataloguingView,
			int count
		)
		throws DataAccessException {
		Session s = currentSession();

		List l = null;
		try {
			Query q =
				s.createQuery(
					"from "
						+ getPersistentClass().getName()
						+ " as hdg where hdg.sortForm = "
						+ " :term  and "
						+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' "
						+ filter
						+ " order by hdg.sortForm, hdg.stringText "
						+ direction);
			q.setString("term", " ");
			q.setInteger("view", cataloguingView);

			l = q.list();
			l = isolateViewForList(l, cataloguingView);
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return l;
	}

	

	public List getDocList(Descriptor d, int cataloguingView)
		throws DataAccessException {

		List l =
			find(
				" select apf.bibItemNumber from "
					+ d.getAccessPointClass().getName()
					+ " as apf "
					+ " where apf.headingNumber = ? and "
					+ " substr(apf.userViewString, ?, 1) = '1'",
				new Object[] {
					new Integer(d.getKey().getHeadingNumber()),
					new Integer(cataloguingView)},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });

		return l;
	}

	/**
	 * @deprecated Use {@link librisuite.business.descriptor.DAOMadesDescriptor#getMadesCount(TTL_HDG_HRCY,int)} instead
	 */
	public int getMadesCount(Descriptor d, int cataloguingView)
		throws DataAccessException, UnsupportedHeadingException {
			return madesDescriptor.getMadesCount(d, cataloguingView);
	}

//	public int getAuthCount(Descriptor d) throws DataAccessException {
		/*
		 * Currently, descriptors that support Authorities also contain a column containing
		 * the count of authorities.  This column is redundant and was added for performance
		 * considerations.  It is believed that the AMICUS client did not maintain this
		 * column correctly.  Nevertheless, we will report the value from this column rather
		 * than re-calculating from the database.
		 * 
		 * This method is implemented in the DAO because the other Browse counts are here and 
		 * also because future implementations may wish to access the db to obtain this count.
		 */

//		return d.getAuthorityCount();
//	}
	
	public int getAuthCount(Descriptor d) throws DataAccessException {
		/*
		 * Currently, descriptors that support Authorities also contain a column
		 * containing the count of authorities. This column is redundant and was
		 * added for performance considerations. It is believed that the AMICUS
		 * client did not maintain this column correctly. Nevertheless, we will
		 * report the value from this column rather than re-calculating from the
		 * database.
		 * 
		 * This method is implemented in the DAO because the other Browse counts
		 * are here and also because future implementations may wish to access
		 * the db to obtain this count.
		 */

		// paulm aut implement db based count
		if (supportsAuthorities()) {
			logger.debug("looking for aut count of hdg:"
					+ autTypeByDescriptorType.get(d
							.getCategory()) + " " + d.getHeadingNumber());
			List l = find("select count(*) from AUT as aut "
					+ " where aut.headingNumber = ? and "
					+ " aut.headingType = ?", new Object[] {
					new Integer(d.getHeadingNumber()),
					autTypeByDescriptorType.get(d.getCategory()) },
					new Type[] { Hibernate.INTEGER, Hibernate.STRING });
			logger.debug("result is: " + ((Integer) l.get(0)).intValue());
			return ((Integer) l.get(0)).intValue();
		} else {
			return 0;
		}
	}

	
	private static Map autTypeByDescriptorType = new HashMap();
	static {
		autTypeByDescriptorType.put(new Short((short) 2), "NH");
		autTypeByDescriptorType.put(new Short((short) 3), "TH");
		autTypeByDescriptorType.put(new Short((short) 4), "SH");
		autTypeByDescriptorType.put(new Short((short) 17), "NH");
		autTypeByDescriptorType.put(new Short((short) 22), "TH");
		autTypeByDescriptorType.put(new Short((short) 18), "SH");
		autTypeByDescriptorType.put(new Short((short) 11), "MH");
	}



	/**
	 * Allows individual dao's (especially publisher) to override the descriptor
	 * handling of sortform
	 * 
	 * @since 1.0
	 */
	public String getBrowsingSortForm(Descriptor d) {
		return d.getSortForm();
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
	public boolean isMatchingAnotherHeading(Descriptor d) {
		try {
			String sortForm = calculateSortForm(d);
			List l =
				currentSession().find(
					"select count(*) from "
						+ getPersistentClass().getName()
						+ " as c "
						+ " where c.sortForm = ? and c.stringText = ? "
						+ " and c.key.userViewString = ?"
						+ " and c.key.headingNumber <> ?",
					new Object[] {
						sortForm,
						d.getStringText(),
						d.getUserViewString(),
						new Integer(d.getKey().getHeadingNumber())},
					new Type[] {
						Hibernate.STRING,
						Hibernate.STRING,
						Hibernate.STRING,
						Hibernate.INTEGER});
			return ((Integer) l.get(0)).intValue() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public Descriptor getMatchingHeading(Descriptor d)
		throws DataAccessException, SortFormException {
		// regenerate sort form

		d.setSortForm(calculateSortForm(d));
		try {
			List l =
				currentSession().find(
					"from "
						+ getPersistentClass().getName()
						+ " as c "
						+ " where c.sortForm = ? and c.stringText = ? "
						+ " and c.key.userViewString = ? ",
					new Object[] {
						d.getSortForm(),
						d.getStringText(),
						d.getUserViewString()},
					new Type[] {
						Hibernate.STRING,
						Hibernate.STRING,
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

	

	/**
	 * returns the number of cross references for the given descriptor in the given view
	 * 
	 * @param source -- The descriptor whose xref count is to be calculated
	 * @param cataloguingView -- the view to use for counting
	 * @return the count of cross references
	 */
	public int getXrefCount(Descriptor source, int cataloguingView)
		throws DataAccessException {
		List l =
			find(
				"select count(*) from "
					+ source.getReferenceClass(source.getClass()).getName()
					+ " as ref where ref.key.source = ? and "
					+ " substr(ref.key.userViewString, ?, 1) = '1' ",
				new Object[] {
					new Integer(source.getKey().getHeadingNumber()),
					new Integer(cataloguingView)},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });
		if (l != null) {
			return ((Integer) l.get(0)).intValue();
		} else {
			return 0;
		}
	}

	/**
	 * gets the cross references for the given source and view.  
	 * 
	 * @param source - the descriptor whose references are to be retrieved
	 * @param cataloguingView - the view to be used
	 * @return a list of cross references.  
	 */
	public List getCrossReferences(Descriptor source, int cataloguingView)
		throws DataAccessException {

		return find(
			"from "
				+ source.getReferenceClass(source.getClass()).getName()
				+ " as ref "
				+ " where ref.key.source = ? "
				+ " AND substr(ref.key.userViewString, ?, 1) = '1' "
				+ " order by ref.key.target, ref.key.type",
			new Object[] {
				new Integer(source.getKey().getHeadingNumber()),
				new Integer(cataloguingView)},
			new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });

	}

	
	public REF loadReference(
		Descriptor source,
		Descriptor target,
		short referenceType,
		int cataloguingView)
		throws DataAccessException {

		REF result = null;

		if (source.getClass() == target.getClass()) {
			List l =
				find(
					"from "
						+ source.getReferenceClass(target.getClass()).getName()
						+ " as ref "
						+ " where ref.key.source = ? AND "
						+ " ref.key.target = ? AND "
						+ " substr(ref.key.userViewString, ?, 1) = '1' AND "
						+ " ref.key.type = ?",
					new Object[] {
						new Integer(source.getKey().getHeadingNumber()),
						new Integer(target.getKey().getHeadingNumber()),
						new Integer(cataloguingView),
						new Short(referenceType)},
					new Type[] {
						Hibernate.INTEGER,
						Hibernate.INTEGER,
						Hibernate.INTEGER,
						Hibernate.SHORT });
			if (l.size() == 1) {
				result = (REF) l.get(0);
				result = (REF) isolateView(result, cataloguingView);
			}
		}

		return result;
	}

	public Descriptor getSeeReference(Descriptor d, int cataloguingView)
		throws DataAccessException {

		if (supportsCrossReferences()) {

			List xRefs = getCrossReferences(d, cataloguingView);

			Iterator iter = xRefs.iterator();
			while (iter.hasNext()) {
				REF ref = (REF) iter.next();
				if (ReferenceType.isSee(ref.getType())) {
					return ref.getTargetDAO().load(
						ref.getTarget(),
						cataloguingView);
				}
			}
		}
		return d;
	}

	public String calculateSearchTerm(String term, String browseIndex)
			throws DataAccessException {
		String searchTerm;
		try {
			searchTerm = calculateSortForm(term, browseIndex);
		} catch (SortFormException e) {
			int lterm = term.getBytes().length;
			String newTerm = lterm>BrowseManager.SORTFORM_LENGTH?term.substring(0,BrowseManager.MAX_SORTFORM_LENGTH):term;
			try {
				searchTerm = calculateSortForm(newTerm, browseIndex);
			} catch (SortFormException e1) {
				searchTerm = " ";
			}
		}
		return searchTerm;
	}
	public REF getCrossReferencesWithLanguage(Descriptor source, int cataloguingView, short indexingLanguage)
	
	throws DataAccessException {
	
	REF result = null;
	List l = null;
    //int sourceHeadingNumber=0;
	//RICERCA PRIMA DEL SOURCE NEL TARGET
	//sourceHeadingNumber = getSourceHeadingNumberByTarget(source, cataloguingView);
   if(source instanceof SBJCT_HDG){
			 l = find(
					"select ref from "
						+ source.getReferenceClass(source.getClass()).getName()
						+ " as ref, "+ source.getClass().getName() + " as hdg "
						+ " where ref.key.source = ? "
						+ " AND substr(ref.key.userViewString, ?, 1) = '1' "
						+ " AND ref.key.type=5 "
						+ " AND ref.key.target=hdg.key.headingNumber "
						+ " AND hdg.accessPointLanguage=? ",
					new Object[] {
						new Integer(source.getKey().getHeadingNumber()),
						new Integer(cataloguingView),
						new Short(indexingLanguage)},
					new Type[] { Hibernate.INTEGER, Hibernate.INTEGER, Hibernate.SHORT });
		 
	}
	else if(source instanceof NME_TTL_HDG){
		  l = find(
					"select ref from "
						+ source.getReferenceClass(source.getClass()).getName()
						+ " as ref, " +
						""+ source.getClass().getName() + " as hdg, "
						+ " NME_HDG as nme, "
						+ " TTL_HDG as ttl "
						+ " where hdg.nameHeadingNumber = nme.key.headingNumber "
						+ " and hdg.titleHeadingNumber = ttl.key.headingNumber "
						+ " and ref.key.source = ? "
						+ " AND substr(ref.key.userViewString, ?, 1) = '1' "
						+ " AND ref.key.type=5 "
						+ " AND ref.key.target=hdg.key.headingNumber "
						+ " AND nme.indexingLanguage=? "
						+ " AND ttl.indexingLanguage=? ",
					new Object[] {
						new Integer(source.getKey().getHeadingNumber()),
						new Integer(cataloguingView),
						new Short(indexingLanguage),
						new Short(indexingLanguage)},
					new Type[] { Hibernate.INTEGER, Hibernate.INTEGER, Hibernate.SHORT, Hibernate.SHORT});
	}
	else {
	    l = find(
		"select ref from "
			+ source.getReferenceClass(source.getClass()).getName()
			+ " as ref, "+ source.getClass().getName() + " as hdg "
			+ " where ref.key.source = ? "
			+ " AND substr(ref.key.userViewString, ?, 1) = '1' "
			+ " AND ref.key.type=5 "
			+ " AND ref.key.target=hdg.key.headingNumber "
			+ " AND hdg.accessPointLanguage=? ",
		new Object[] {
			new Integer(source.getKey().getHeadingNumber()),
			new Integer(cataloguingView),
			new Short(indexingLanguage)},
		new Type[] { Hibernate.INTEGER, Hibernate.INTEGER, Hibernate.SHORT });
	   
	   
	   	}
	if (l.size() == 1) {
		result = (REF) l.get(0);
		result = (REF) isolateView(result, cataloguingView);
	}
	else{
		result = getSourceHeadingNumberByTarget( source, cataloguingView, indexingLanguage);
	}
	return result;

}
	
	private REF getSourceHeadingNumberByTarget(Descriptor source, int cataloguingView, short indexingLanguage) throws DataAccessException {
		List l=null;
		List l2=null;
		REF result = null;
		int targetHeadingNumber=0;
		
		l2 = find(
				"select ref from "
					+ source.getReferenceClass(source.getClass()).getName()
					+ " as ref, "+ source.getClass().getName() + " as hdg "
					+ " where ref.key.source = ? "
					+ " AND substr(ref.key.userViewString, ?, 1) = '1' "
					+ " AND ref.key.target=hdg.key.headingNumber "
					+ " AND ref.key.type=5 ",
				new Object[] {
					new Integer(source.getKey().getHeadingNumber()),
					new Integer(cataloguingView)},
		   	new Type[] { Hibernate.INTEGER, Hibernate.INTEGER});
	
	 if(l2.size()==1){
	    targetHeadingNumber = ((REF) l2.get(0)).getTarget();
		l = find(
					"select ref from "
						+ source.getReferenceClass(source.getClass()).getName()
						+ " as ref, "+ source.getClass().getName() + " as hdg "
						+ " where ref.key.source = ? "
						+ " AND substr(ref.key.userViewString, ?, 1) = '1' "
						+ " AND ref.key.type=5 "
						+ " AND ref.key.target=hdg.key.headingNumber "
						+ " AND hdg.accessPointLanguage=? ",
						
						new Object[] {
							new Integer(targetHeadingNumber),
							new Integer(cataloguingView),
							new Short(indexingLanguage)},
						new Type[] { Hibernate.INTEGER, Hibernate.INTEGER, Hibernate.SHORT });
		if (l.size() == 1) {
			result = (REF) l.get(0);
			result = (REF) isolateView(result, cataloguingView);
		}
	 }
		return result;

	}
	
	/**
	 * gets the cross references for attribute the given source and view.  
	 * 
	 * @param source - the descriptor whose references are to be retrieved
	 * @param cataloguingView - the view to be used
	 * @return a list of cross references.  
	 */
	public List getCrossReferencesAttrib(Descriptor source, int cataloguingView)
		throws DataAccessException {

		return find(
			"from THS_ATRIB "
				+ " as ref "
				+ " where ref.key.source = ? "
				+ " AND substr(ref.key.userViewString, ?, 1) = '1' "
				+ " order by ref.key.target, ref.key.type",
			new Object[] {
				new Integer(source.getKey().getHeadingNumber()),
				new Integer(cataloguingView)},
			new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });

	}

	public int getXAtrCount(Descriptor source, int cataloguingView)
	throws DataAccessException {
	List l =
		find(
			"select count(*) from THS_ATRIB"
				
				+ " as ref where ref.key.source = ? and "
				+ " substr(ref.key.userViewString, ?, 1) = '1' ",
			new Object[] {
				new Integer(source.getKey().getHeadingNumber()),
				new Integer(cataloguingView)},
			new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });
	if (l != null) {
		return ((Integer) l.get(0)).intValue();
	} else {
		return 0;
	}
}
	/**
	 * @param headingNumber
	 * @return the count of the notes from this headingNumber
	 * access to THS_NTE table
	 * @since 1.0
	 */

	public int getThesaurusNotesCount(Descriptor source,int cataloguingView)
	throws DataAccessException {
		List l =
			find(
				"select count(*) from THS_NTE"
					
					+ " as ref where ref.key.headingNumber = ? ",
					
				new Object[] {
					new Integer(source.getKey().getHeadingNumber())},
				new Type[] { Hibernate.INTEGER});
		if (l != null) {
			return ((Integer) l.get(0)).intValue();
		} else {
			return 0;
		}
		
	}
		
	public int getDocCountNT(Descriptor d, int cataloguingView)
	throws DataAccessException {
	
	int result=0;
	List l =null;
	//discriminare count a seconda della heading se è per nomi o per titoli
	if(d instanceof NME_HDG){
	l =
		find(
			" select count(*) from  NME_TTL_HDG as hdg"
				+ " where hdg.nameHeadingNumber = ? and "
				+ " substr(hdg.key.userViewString, ?, 1) = '1'",
				//+" and apf.headingNumber = hdg.key.headingNumber",
			new Object[] {
				new Integer(d.getKey().getHeadingNumber()),
				new Integer(cataloguingView)},
			new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });
	}else if(d instanceof TTL_HDG){
		l =
			find(
				" select count(*)from NME_TTL_HDG as hdg"
					+ " where hdg.titleHeadingNumber = ? and "
					+ " substr(hdg.key.userViewString, ?, 1) = '1'",
					//+" and apf.headingNumber = hdg.key.headingNumber",
				new Object[] {
					new Integer(d.getKey().getHeadingNumber()),
					new Integer(cataloguingView)},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });
	}
	if (l.size() > 0) {
		result = ((Integer) l.get(0)).intValue();
	}

    //}
	return result;
}

	
	
	 public int getCountTitle(Descriptor d) throws DataAccessException {
		Session s = currentSession();
		int count = 0;
		Connection connection;
		PreparedStatement stmt = null;
		ResultSet js = null;

		try {
			connection = s.connection();
			String sql = "select count(*) from (select distinct ttl_hdg_nbr, bib_itm_nbr from TTL_ACS_PNT where TTL_HDG_NBR="
					+ d.getHeadingNumber() + ") a";
						
			stmt = connection.prepareStatement(sql);
			js = stmt.executeQuery();

			while (js.next()) {
				count = js.getInt(1);
			}

		} catch (SQLException exception) {
			logAndWrap(exception);

		} catch (HibernateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
    		try {
				js.close();
				stmt.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return count;
	}

	 
	 public int getCountName(Descriptor d) throws DataAccessException {
			Session s = currentSession();
			int count = 0;
			Connection connection;
			PreparedStatement stmt = null;
			ResultSet js = null;

			try {
				connection = s.connection();
				String sql = "select count(*) from (select distinct nme_hdg_nbr, bib_itm_nbr from NME_ACS_PNT where NME_HDG_NBR="
						+ d.getHeadingNumber() + ") a";
				stmt = connection.prepareStatement(sql);
				js = stmt.executeQuery();

				while (js.next()) {
					count = js.getInt(1);
				}

			} catch (SQLException exception) {
				logAndWrap(exception);

			} catch (HibernateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
	    		try {
					js.close();
					stmt.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return count;
		}

	
}

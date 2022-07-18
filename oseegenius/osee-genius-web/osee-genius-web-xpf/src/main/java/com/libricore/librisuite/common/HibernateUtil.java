/*
 * (c) LibriCore
 * 
 * Created on Apr 16, 2004
 */
package com.libricore.librisuite.common;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.event.NamingExceptionEvent;
import javax.sql.DataSource;

import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.common.LibrisuiteUtils;
import librisuite.business.common.Persistence;
import librisuite.business.common.PersistentObjectWithView;
import librisuite.business.common.View;
import librisuite.business.exception.RecordInUseException;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a base class of support utilities
 * for DAO objects
 * 
 * @author wimc
 *
 */
@SuppressWarnings("rawtypes")
public class HibernateUtil {

	private static Log logger = LogFactory.getLog(HibernateUtil.class);
	private static ThreadLocal/*<Connection>*/ lockingSession = new ThreadLocal/*<Connection>*/();
	private static ThreadLocal/*<String> */lockingSessionId = new ThreadLocal/*<String>*/();

	private static Connection getLockingSession() {
		return (Connection) lockingSession.get();
	}

	private static void setLockingSession(Connection con) {
		lockingSession.set(con);
	}

	private static String getLockingSessionId() {
		return (String) lockingSessionId.get();
	}

	private static void setLockingSessionId(String s) {
		lockingSessionId.set(s);
	}

	public Session currentSession() throws DataAccessException {
		try {
			return HibernateSessionProvider.getInstance().currentSession();
		} catch (HibernateException e) {
			logAndWrap(e);
			return null;
		}
	}

	public void closeSession() throws DataAccessException {
		try {
			HibernateSessionProvider.getInstance().closeSession();
		} catch (HibernateException e) {
			logAndWrap(e);
		}
	}

	/**
	 * Logs the Hibernate Exception and wraps it as a DataAccessException
	 * 
	 * 
	 * @param e -- the exception
	 */
	public void logAndWrap(Throwable e) throws DataAccessException 
	{
		logger.warn("HibernateException " + e.getMessage());
		throw new DataAccessException(e);
	}

	/**
	 * Creates a new usr_vw_ind string from the input string by
	 * setting the position specified in arg2 to '0'.  The resultant
	 * view string is useful in saving a persistant object after the
	 * current cataloguing view of the record is deleted (or modified)
	 * 
	 * 
	 * @param viewString -- the original view String
	 * @param cataloguingView -- the position to be set to '0' (1 indexing)
	 */
	public String maskOutViewString(String viewString, int cataloguingView) 
	{
		return View.maskOutViewString(viewString, cataloguingView);
	}

	/**
	 * Creates a new usr_vw_ind string from the input string by
	 * setting the position specified in arg2 to '1'.  The resultant
	 * view string is useful in saving a persistant object after the
	 * current cataloguing view of the record is added (based on a copy from
	 * existing views);
	 * 
	 * 
	 * @param viewString -- the original view String
	 * @param cataloguingView -- the position to be set to '1' (1 indexing)
	 */

	public String maskOnViewString(String viewString, int cataloguingView) {
		return View.maskOnViewString(viewString, cataloguingView);
	}
	/**
	 * Creates a new usr_vw_ind string by
	 * setting all positions to '0' except the position specified in arg1.  
	 * The resultant view string is useful in saving a persistant object after the
	 * current cataloguing view of the record is saved or updated;
	 * 
	 * 
	 * @param cataloguingView -- the position to be set to '1' (1 indexing)
	 */

	public String makeSingleViewString(int cataloguingView) {
		return View.makeSingleViewString(cataloguingView);
	}

	/**
	 * Invokes hibernate methods save, update depending on the objects 
	 * updateStatus.  If the status is "deleted" then the corresponding DAO.delete()
	 * method is invoked.
	 */
	public void persistByStatus(Persistence po) throws DataAccessException {
		Session s = currentSession();

		if (po.isNew()) {
			if (logger.isDebugEnabled()) {
				logger.debug("inserting " + po);
			}
			po.getDAO().save(po);
		} else if (po.isChanged()) {
			if (logger.isDebugEnabled()) {
				logger.debug("updating " + po);
			}
			po.getDAO().update(po);
		} else if (po.isDeleted()) {
			if (logger.isDebugEnabled()) {
				logger.debug("deleting " + po);
			}
			po.getDAO().delete(po);
		}
	}
	/**
	 * performs isolateView on a List
	 */
	@SuppressWarnings("unchecked")
	public List isolateViewForList(List multiView, int userView) throws DataAccessException 
	{
		if(userView<View.AUTHORITY) {
			logger.error("NO ISOLATION FOR MADES");
			return multiView;
		}
		List singleView = new ArrayList();
		Iterator iter = multiView.iterator();
		while (iter.hasNext()) {
			singleView.add(
				isolateView((PersistentObjectWithView) iter.next(), userView));
		}
		return singleView;
	}

	/**
	 * Ensures that the returned PersistentObject is a "single" view row version of
	 * the passed argument
	 *
	 */
	public PersistentObjectWithView isolateView(final PersistentObjectWithView p, final int userView) throws DataAccessException 
	{
		if(userView<View.AUTHORITY) {
			logger.error("NO ISOLATION FOR MADES");
			return p;
		}
		final String myView = makeSingleViewString(userView);
		final PersistentObjectWithView p3 =
			(PersistentObjectWithView) LibrisuiteUtils.deepCopy(p);

		if (p.getUserViewString().compareTo(myView) != 0) {
			new TransactionalHibernateOperation() {
				public void doInHibernateTransaction(Session s)
					throws HibernateException {
					s.delete(p);
					PersistentObjectWithView p2 =
						(PersistentObjectWithView) LibrisuiteUtils.deepCopy(p);
					p2.setUserViewString(
						maskOutViewString(p.getUserViewString(), userView));
					s.save(p2);
					p3.setUserViewString(myView);
					s.save(p3);
				}
			}
			.execute();
			return p3;
		} else {
			return p;
		}
	}

	/**
	 * Convenience method for currentSession().load(Class clazz, Serializable id).
	 * If the load method of the Hibernate Session throws a HibernateException, 
	 * it wraps it in a DataAccessException
	 * @param clazz a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @return the persistent instance 
	 * @throws DataAccessException
	 */
	public Object load(Class clazz, Serializable id) throws DataAccessException 
	{
		try {
			return currentSession().load(clazz, id);
		} catch (HibernateException e) {
			logAndWrap(e);
			return null;
		}
	}

	/**
	 * Convenience method for currentSession().get(Class clazz, Serializable id)
	 * If the get method of the Hibernate Session throws a HibernateException, 
	 * it wraps it in a DataAccessException
	 * @param clazz a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @return the persistent instance or null
	 * @throws DataAccessException
	 */
	public Object get(Class clazz, Serializable id)	throws DataAccessException 
	{
		try {
			return currentSession().get(clazz, id);
		} catch (HibernateException e) {
			logAndWrap(e);
			return null;
		}
	}

	/**
	 * Convenience method for currentSession().get(Class clazz, Serializable id, LockMode l)
	 * If the get method of the Hibernate Session throws a HibernateException, 
	 * it wraps it in a DataAccessException
	 * @param clazz a persistent class
	 * @param id a valid identifier of an existing persistent instance of the class
	 * @return the persistent instance or null
	 * @throws DataAccessException
	 */
	public Object get(Class clazz, Serializable id, LockMode l)	throws DataAccessException 
	{
		try {
			return currentSession().get(clazz, id, l);
		} catch (HibernateException e) {
			logAndWrap(e);
			return null;
		}
	}

	/**
	 * Convenience method for currentSession().find(String query, Object[] values, Type[] types)
	 * If the find method of the Hibernate Session throws a HibernateException, 
	 * it wraps it in a DataAccessException
	 * @param query the query string
	 * @param values an array of values to be bound to the "?" placeholders (JDBC IN parameters).
	 * @param types an array of Hibernate types of the values 
	 * @return a distinct list of instances 
	 * @throws DataAccessException
	 */
	public List find(String query, Object[] values, Type[] types) throws DataAccessException 
	{
		try {
			return currentSession().find(query, values, types);
		} catch (HibernateException e) {
			logAndWrap(e);
			return null;
		}
	}

	/**
	 * Convenience method for currentSession().find(String query)
	 * If the find method of the Hibernate Session throws a HibernateException, 
	 * it wraps it in a DataAccessException
	 * @param query the query string
	 * @return a distinct list of instances 
	 * @throws DataAccessException
	 */
	public List find(String query) throws DataAccessException 
	{
		try {
			return currentSession().find(query);
		} catch (HibernateException e) {
			logAndWrap(e);
			return null;
		}
	}

	/**
	 * Default implementation for save (with no extra requirements)
	 * @since 1.0
	 */
	public void save(final Persistence p) throws DataAccessException 
	{
		new TransactionalHibernateOperation() {
			public void doInHibernateTransaction(Session s)
				throws HibernateException {
				s.save(p);
			}
		}
		.execute();
	}

	/**
	 * Default implementation for update (with no extra requirements)
	 * @since 1.0
	 */
	public void update(final Persistence p) throws DataAccessException 
	{
		new TransactionalHibernateOperation() {
			public void doInHibernateTransaction(Session s)
				throws HibernateException {
				s.update(p);
			}
		}
		.execute();
	}

	/**
	 * Default implementation for delete with no cascade affects
	 * @since 1.0
	 */
	public void delete(final Persistence p) throws DataAccessException 
	{
		new TransactionalHibernateOperation() {
			public void doInHibernateTransaction(Session s)
				throws HibernateException {
				s.delete(p);
			}
		}
		.execute();
	}
	


	private boolean isSessionAlive(String sessionId) 
	{
		try {
			Connection con = currentSession().connection();
			ResultSet rs = con.createStatement().executeQuery(
					"SELECT count(*) from v$session where audsid = "
							+ sessionId);
			if (rs.next()) {
				return rs.getInt(1) > 0;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isUserPresent(String user) 
	{
		try {
			Connection con = currentSession().connection();
			ResultSet rs = con.createStatement().executeQuery(
					"SELECT count(*) from S_LCK_TBL where USR_NME = '"
							+ user+"'");
			if (rs.next()) {
				return rs.getInt(1) > 0;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	private Connection createNewDBSession() throws SQLException {
		Connection con = null;
		try {
		   con = HibernateSessionProvider.getInstance().currentSession().connection();

		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}



	private String getSessionID(Connection con) throws SQLException 
	{
		ResultSet rs = con.createStatement().executeQuery("SELECT audsid from v$session where audsid = userenv('sessionid') ");
		if (rs.next()) {
			return rs.getString(1);
		} else {
			return null;
		}
	}
	
	/* BUG 3762 Paul: inizio */
	/* * Ensures that a client DB session exists and returns the Session ID.  Used for unique
	 * identifications of search clients in the search engine.
	 * @return
	 */
	public String getUniqueSessionId() 
	{
		try {
			if (getLockingSession() == null) {
				setLockingSession(createNewDBSession());
				setLockingSessionId(getSessionID(getLockingSession()));
			}
			return getLockingSessionId();
		} catch (Exception e) {
			return null;
		}
	}
	


}
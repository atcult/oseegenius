/*
 * Created on Oct 15, 2004
 *
 */
package com.libricore.librisuite.common;

import java.util.Iterator;

import javax.naming.NameNotFoundException;

import librisuite.business.common.Defaults;
import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.connection.DatasourceConnectionProvider;
import net.sf.hibernate.mapping.PersistentClass;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class comment
 * @author janick
 */
public class HibernateSessionProvider {
	private static String dialect = Defaults.getString("hibernate.dialect");
	private static String datasource = Defaults.getString("hibernate.connection.datasource");
	private static String showSql = Defaults.getString("hibernate.show_sql");

	private static Log logger = LogFactory.getLog(HibernateSessionProvider.class);
	private static final ThreadLocal sessionHolder = new ThreadLocal();
	private SessionFactory sessionFactory = null;	
	
	private Configuration configuration = null;
	
	private static HibernateSessionProvider uniqueInstance = null;
	private static HibernateException except = null;
	
	static {
		try {
			uniqueInstance = new HibernateSessionProvider();
		} catch (HibernateException e) {
			except = e;
		}
	}
	
	/**
	 * 
	 */
	private HibernateSessionProvider() throws HibernateException {
		super();
	   configuration = new Configuration();
		configuration.configure();
		configuration.setProperty("hibernate.connection.datasource", datasource);
		configuration.setProperty("hibernate.dialect", dialect);
	    configuration.setProperty("dialect", dialect);
		configuration.setProperty("hibernate.show_sql", showSql);
		configuration.setProperty("show_sql", showSql);
		logger.debug("Hibernate Configuration parameters set");
		sessionFactory = configuration.buildSessionFactory();
		logger.debug("Hibernate SessionFactory created from Configuration");		
	}
	
	public static HibernateSessionProvider getInstance() throws HibernateException {
		if (except != null) throw except;
		return uniqueInstance;
	}
	
	public void closeSession() throws HibernateException {
		Session session = (Session) sessionHolder.get();
		if (session != null) {
			sessionHolder.set(null);
			session.close();
			logger.info("Hibernate Session Closed");
		} else {
			logger.info("No Hibernate Session to close");
		}
	}

	public Session currentSession() throws HibernateException {
		Session session = (Session) sessionHolder.get();
		// Open a new Session, if this Thread has none yet
		if (session == null) {
			session = sessionFactory.openSession();
			/*
			 * PAULM -- set this flushmode to avoid flushes before queries
			 * Not sure if it will work -- the theory is that we always do a commit
			 * when we save so there should not be any "dirty" data issues
			 */
			session.setFlushMode(FlushMode.COMMIT);
			sessionHolder.set(session);
			logger.info("New Hibernate Session Opened");
		}
		return session;
	}	
	
	public Class getHibernateClassName(String tableName){
		Iterator  iterator = configuration.getClassMappings();
		PersistentClass pc = null;
		
		while (iterator.hasNext())
		     {
		        pc = (PersistentClass) iterator.next();		         
		        if (pc.getTable().getName().equals(tableName)){		        
					return pc.getMappedClass();
		        }
		     }
		     return null;
	}

}
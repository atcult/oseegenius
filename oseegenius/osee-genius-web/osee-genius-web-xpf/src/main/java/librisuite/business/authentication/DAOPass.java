/*
 * (c) LibriCore
 * 
 * Created on Jul 20, 2004
 * 
 * DAOIndexList.java
 */
package librisuite.business.authentication;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import librisuite.business.common.DataAccessException;
import librisuite.hibernate.UserTempData;
import librisuite.hibernate.UserTempPass;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import com.libricore.librisuite.common.HibernateUtil;
import com.libricore.librisuite.common.TransactionalHibernateOperation;

/** 
 * @author hansv
 * @version %I%, %G%
 * @since 1.0
 */
public class DAOPass extends HibernateUtil {
	private void saveToken(final UserTempPass utp, final Map/*<String, String>*/ parameters) throws DataAccessException {
		new TransactionalHibernateOperation() {
			public void doInHibernateTransaction(Session s)
				throws HibernateException {
				String pass = utp.getPass();
				s.save(utp);
				Iterator entries = parameters.entrySet().iterator();
				while (entries.hasNext()) {
					Map.Entry entry = (Map.Entry) entries.next();
					UserTempData utd = new UserTempData(
										pass, 
										(String) entry.getKey(), 
										(String) entry.getValue());
					s.save(utd);
				}
			}
		}
		.execute();
	}
	
	private List loadTokenParams(String pass) throws HibernateException, DataAccessException{
		Query q = currentSession()
			.createQuery("from UserTempData t where t.pass= :pass")
			.setString("pass", pass);
		List l = q.list();
		return l;
	}
	
	public Hashtable loadParameters(String pass) throws HibernateException, DataAccessException{
		List l = loadTokenParams(pass);
		Iterator it = l.iterator();
		Hashtable ht = new Hashtable();
		while (it.hasNext()) {
			UserTempData param = (UserTempData) it.next();
			String dataValue = param.getDataValue();
			if(dataValue!=null) {// skip null values 
				//TODO _MIKE: do no insert nulls in table instead
				ht.put(param.getDataKey(), dataValue);
			}
		}
		return ht;
	}
	
	public UserTempPass loadSavedToken(String pass) throws DataAccessException, AuthenticationException{
		try {
			Query q = currentSession()
				.createQuery("from UserTempPass t where t.pass = :pass")
				.setString("pass", pass);
			List l = q.list();
			if(l.size()==0 || l.size()>1) throw new AuthenticationException();
			return (UserTempPass) l.get(0);
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return null;
	}
	
	public boolean isTokenValid(String pass) throws DataAccessException, AuthenticationException{
		try {
			List l = currentSession().find("select count(*) from UserTempPass t where t.pass= ?", pass, Hibernate.STRING);
			if(((Integer)l.get(0)).intValue()==1) return true;
			else return false;
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return false;
	}

	public void savePassToken(String ssid, String userName, String startingApp, Map params) throws DataAccessException {
		UserTempPass token = new UserTempPass(ssid, userName, startingApp);
		saveToken(token, params);
	}

	public void removeToken(final String pass) throws DataAccessException {
		new TransactionalHibernateOperation() {
			public void doInHibernateTransaction(Session s)
				throws HibernateException {
				s.delete("from UserTempData t where t.pass= ?", pass, Hibernate.STRING);
				s.delete("from UserTempPass p where p.pass= ?", pass, Hibernate.STRING);
			}
		}
		.execute();
	}

}

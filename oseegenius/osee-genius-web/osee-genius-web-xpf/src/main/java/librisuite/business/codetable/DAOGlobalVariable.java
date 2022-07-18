/*
 * (c) LibriCore
 * 
 * Created on Jul 2, 2004
 * 
 * DAOGlobalVariable.java
 */
package librisuite.business.codetable;

import librisuite.business.common.DataAccessException;
import librisuite.hibernate.S_SYS_GLBL_VRBL;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.libricore.librisuite.common.HibernateUtil;
import com.libricore.librisuite.common.TransactionalHibernateOperation;

/**
 * Provides access to S_SYS_GLBL_VRBL
 * @author paulm
 * @version %I%, %G%
 * @since 1.0
 */
public class DAOGlobalVariable extends HibernateUtil {
    
    private Log logger = LogFactory.getLog(DAOGlobalVariable.class);
	//TODO null exception if variable doesn't exist
	public String getValueByName(String name) throws DataAccessException {
		return ((S_SYS_GLBL_VRBL) get(S_SYS_GLBL_VRBL.class, name)).getValue();
	}
	
	  public void edit(final S_SYS_GLBL_VRBL globalVrbl) throws DataAccessException {
			new TransactionalHibernateOperation() {
				public void doInHibernateTransaction(Session s) throws HibernateException {
					s.update(globalVrbl);
				}
			}.execute();
		}
	
	public void setValueByName(String name,String value) throws DataAccessException {
		  
	        S_SYS_GLBL_VRBL sysGlobal = (S_SYS_GLBL_VRBL) get(S_SYS_GLBL_VRBL.class, name);
		    
		    sysGlobal.setValue(value);
		    
		   edit(sysGlobal);
	    
	}
	
}

package librisuite.business.searching;

import java.util.List;

import librisuite.business.common.DataAccessException;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import com.libricore.librisuite.common.HibernateUtil;

public class DAOPublisher extends HibernateUtil {

	public DAOPublisher(){ 
		super();		
	}
	
	public List getPublisherList() throws DataAccessException
	{
		List result = null;
		try {
			Session s = currentSession();			
			result = s.find("from CasSapPubl as a ", new Object[] {},new Type[] {});				
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	
	
	
	public List loadHdg(String hdg) throws DataAccessException
	{
		List result = null;
		try {	
			
			Session s = currentSession();
			result = s.find("from PublCdeHdg as a where a.hdrNumber = '" + hdg + "'");		

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	


	public List getResultFromPublisher(String publisherCode) throws DataAccessException 
	{
		List result = null;
		try {			
			Session s = currentSession();
			Query q = s.createQuery("select distinct ta"
				+ " from TitleAccessPoint as ta, "
				+ " PublCdeHdg as pu "
				+ " where pu.publisherCode = ?"				
				+ " and pu.hdrNumber = ta.headingNumber");
			q.setString(0, publisherCode);
			q.setMaxResults(100);		
			result = q.list();

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	
	public List loadHeadingFromPublisher(String publisherCode) throws DataAccessException 
	{
	   List result = null;
		try {			
			Session s = currentSession();
			Query q = s.createQuery("select distinct ta"
				+ " from PUBL_HDG as ta, "
				+ " PublCdeHdg as pu "
				+ " where pu.publisherCode = ?"				
				+ " and pu.hdrNumber = ta.key.headingNumber"
				+ " order by ta.nameSortForm");
			q.setString(0, publisherCode);
	
			result = q.list();
			
			

		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	
	public List loadTotalHeading() throws DataAccessException 
	{
		List result = null;
		Query q = null;
		try {			
			Session s = currentSession();
			q = s.createQuery("select pu, ta"
				+ " from PUBL_HDG as ta, "
				+ " PublCdeHdg as pu "			
				+ " where pu.hdrNumber = ta.key.headingNumber"
				+ " order by ta.nameSortForm");
			result = q.list();
			
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
}

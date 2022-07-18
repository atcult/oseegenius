/*
 * (c) LibriCore
 * 
 * Created on Dec 17, 2004
 * 
 * DAOPublisherNameDescriptor.java
 */
package librisuite.business.descriptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import librisuite.business.common.DataAccessException;
import librisuite.hibernate.DescriptorKey;
import librisuite.hibernate.PUBL_HDG;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

/**
 * This class implements the browse specific methods
 * special to publishers when Publisher name is being browsed
 * @author paulm
 * @version $Revision: 1.2 $, $Date: 2005/12/21 08:30:33 $
 * @since 1.0
 */
public class DAOPublisherNameDescriptor extends DAOPublisherDescriptor {
	
	public Class getPersistentClass() {
		return DAOPublisherNameDescriptor.persistentClass;
	}

	

public List getHeadingsBySortform(String operator, String direction, String term, String filter, int cataloguingView, int count,int collectionCode) throws DataAccessException{
   	String query="";
	Connection connection = null;
    PreparedStatement stmt =null;
    java.sql.ResultSet rs =null;
    Session s = currentSession();
	String[] parsedTerm = term.split(" : ");
	List l = null;
	if (parsedTerm.length < 2) {
	   return getSortformByOneSearchTerm(operator, direction, term, filter, cataloguingView, count, s, l);
	}
	else{
		return getSortformByTwoSearchTerms(operator, direction, filter, cataloguingView, count, query, connection, stmt, rs, s, parsedTerm, l);
	}
		
	}



/**
 * @param operator
 * @param direction
 * @param filter
 * @param cataloguingView
 * @param count
 * @param query
 * @param connection
 * @param stmt
 * @param rs
 * @param s
 * @param parsedTerm
 * @param l
 * @return
 * @throws DataAccessException
 */
private List getSortformByTwoSearchTerms(String operator, String direction, String filter, int cataloguingView, int count, String query, Connection connection, PreparedStatement stmt, java.sql.ResultSet rs, Session s, String[] parsedTerm, List l) throws DataAccessException {
	String name;
	String place;
	String operator2;
	/*
	 * Query 1* elemento della First page
	 * Se operatore = "<" : eseguo la query per Nome Editore <= 
	 *  e Luogo Pubbl. < 
	 */	
    String rownum = "";
    if(SWITCH_DATABASE !=null && SWITCH_DATABASE.equals("postgres"))
    	rownum = " limit ";
     else
    	rownum = " where rownum <= ";
    	
     place = parsedTerm[0].trim();
     name = parsedTerm[1].trim();
     if(operator.equals("<")){

	  try {
		Query q =
			s.createQuery(
				"from "
					+ getPersistentClass().getName()
					+ " as hdg where hdg.nameSortForm "
					+ (operator.equals("<")?"<=":operator)
					+ " :name  and "
					+" hdg.placeSortForm "
					+ operator
					+ " :place  and "
					+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' "
					+ filter
					+ " order by hdg.nameSortForm "
					+ direction
					+ ", hdg.placeSortForm "
					+ direction);
		q.setString("place", place);
		q.setString("name", name);
		q.setInteger("view", cataloguingView);
		q.setMaxResults(count);

		l = q.list();
		l = isolateViewForList(l, cataloguingView);
	} catch (HibernateException e) {
		logAndWrap(e);
	}
	return l;

  }else{
	/*
	 * Query per elementi successivi e precedenti
	 */	
  if(operator.indexOf(">=")!=-1 ||operator.indexOf("<=")!=-1){
   if(operator.indexOf(">=")!=-1)
	 operator2=">";
  else if(operator.indexOf("<=")!=-1)
	 operator2="<";
  else 
  operator2=operator;

  try {
	connection = s.connection();
	query="Select * from (SELECT * FROM "+
	  "(select * "+
	  "from publ_hdg "+
	  "where PUBL_HDG_SRT_FRM_NME = '" 
	  +name+
	  "' AND PUBL_HDG_SRT_FRM_PLCE  "
	  +operator
	  +"'"
	  +place+
	  //aggiunto
	  "' and substr(usr_vw_ind,"+cataloguingView+", 1) = '1' "
	  +" UNION "+
	  "select * "+
	  "from publ_hdg "+
	  "where PUBL_HDG_SRT_FRM_NME "
	  +operator2
	  +"'"
	  +name+
      //aggiunto
	  "' and substr(usr_vw_ind,"+cataloguingView+", 1) = '1' "
	  +") a ORDER BY PUBL_HDG_SRT_FRM_NME "
	  + direction+
	  ", PUBL_HDG_SRT_FRM_PLCE "
	  + direction
	  +") " + rownum + count;
	  stmt = connection.prepareStatement(query);
	  rs = stmt.executeQuery();
	  l= new ArrayList();
	  while (rs.next()) {
		 PUBL_HDG pu = new PUBL_HDG();
		 pu.setIndexingLanguage(rs.getShort("LANG_OF_IDXG_CDE"));
		 pu.setNameSortForm(rs.getString("PUBL_HDG_SRT_FRM_NME"));
		 pu.setPlaceSortForm(rs.getString("PUBL_HDG_SRT_FRM_PLCE"));
		 pu.setNameStringText(rs.getString("PUBL_HDG_STRNG_TXT_NME"));
		 pu.setPlaceStringText(rs.getString("PUBL_HDG_STRNG_TXT_PLCE"));
		 pu.setKey(new DescriptorKey(rs.getInt("PUBL_HDG_NBR"),rs.getString("USR_VW_IND")));
		 pu.markUnchanged();
		 l.add(pu);
	  }
	  l = isolateViewForList(l, cataloguingView);
  
	} catch (HibernateException e) {
			logAndWrap(e);
			
	} catch (SQLException e1) {
			logAndWrap(e1);
	} 
	finally{
	    try {
	     if(stmt!=null)stmt.close();
	     if(rs!=null)rs.close();
	    
	     } catch (SQLException e) {
	     	   logAndWrap(e);
	     }
	   }
   }
}
	return l;
}



/**
 * @param operator
 * @param direction
 * @param term
 * @param filter
 * @param cataloguingView
 * @param count
 * @param s
 * @param l
 * @return
 * @throws DataAccessException
 */
private List getSortformByOneSearchTerm(String operator, String direction, String term, String filter, int cataloguingView, int count, Session s, List l) throws DataAccessException {
	try {
			Query q =
				s.createQuery(
					"from "
						+ getPersistentClass().getName()
						+ " as hdg where hdg.nameSortForm "
						+ operator
						+ " :term  and "
						+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' "
						+ filter
						+ " order by hdg.nameSortForm "
						+ direction
						+ ", hdg.placeSortForm "
						+ direction);
			q.setString("term", term);
			q.setInteger("view", cataloguingView);
			q.setMaxResults(count);
	
			l = q.list();
			l = isolateViewForList(l, cataloguingView);
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return l;
}

	

	public String getBrowsingSortForm(Descriptor d) {
		if (!(d instanceof PUBL_HDG)) {
			logger.warn("I can only handle PUBL_HDG descriptors");
			throw new IllegalArgumentException();
		}
		PUBL_HDG pub = (PUBL_HDG)d;
		return pub.getPlaceSortForm()+" : "+pub.getNameSortForm();
	}
	
	
}

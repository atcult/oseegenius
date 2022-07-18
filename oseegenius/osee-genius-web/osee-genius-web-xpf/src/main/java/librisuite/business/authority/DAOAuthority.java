package librisuite.business.authority;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import librisuite.business.common.DataAccessException;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.libricore.librisuite.common.HibernateUtil;

public class DAOAuthority  extends HibernateUtil {
	
	private final static Map<String, String> AUTH_QUERIES = new HashMap<String, String>();
	private final static Map<String, List<AuthorityNote>> EMPTY_AUTHORITY = new HashMap<String, List<AuthorityNote>>(0);
	
	static 
	{
		AUTH_QUERIES.put(
				"TH", 		
				"SELECT B.AUT_NTE_TYP_CDE, (UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'a')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'b')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'u')) AS NOTES " 
				+"FROM AUT A, AUT_NTE B, TTL_HDG C "
				+"WHERE A.AUT_NBR=B.AUT_NBR AND "
				+"C.TTL_HDG_NBR=? AND "
				+"C.TTL_HDG_NBR= A.HDG_NBR AND AND substr(C.USR_VW_IND,?,1)='1'"/*+ VISTA1*/);
		
			
		
		AUTH_QUERIES.put(
				"NH", 		
				"SELECT B.AUT_NTE_TYP_CDE, (UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'a')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'b')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'c')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'd')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'f')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'g')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 's')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 't')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'u') ||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, '2')) AS NOTES " 
				+"FROM AUT A, AUT_NTE B, NME_HDG C "
				+"WHERE A.AUT_NBR=B.AUT_NBR AND "
				+"C.NME_HDG_NBR=? AND "
				+"C.NME_HDG_NBR= A.HDG_NBR AND substr(C.USR_VW_IND,?,1)= '1'"/*+ VISTA1*/);
		
		AUTH_QUERIES.put(
				"SH", 		
				"SELECT B.AUT_NTE_TYP_CDE, (UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'a')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'b')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'u')) AS NOTES " 
				+"FROM AUT A, AUT_NTE B, SBJCT_HDG C "
				+"WHERE A.AUT_NBR=B.AUT_NBR AND "
				+"C.SBJCT_HDG_NBR=? AND "
				+"C.SBJCT_HDG_NBR= A.HDG_NBR AND substr(C.USR_VW_IND,?,1)= '1'"/*+ VISTA1*/);
		
		
	}
	

	public Map<String, List<AuthorityNote>> getAuthorityNotesList(final String indexName, final int headingNbr, final int cataloguingView) throws DataAccessException
	
	{
		String query = AUTH_QUERIES.get(indexName);
		if (query == null)
		{
			return EMPTY_AUTHORITY;
		}
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
			
		Map<String, List<AuthorityNote>> notes = null;
		Session s = currentSession();
		try 
		{
	        connection = s.connection();
			statement = connection.prepareStatement(query);
			statement.setInt(1, headingNbr);
			statement.setInt(2, cataloguingView);
			rs = statement.executeQuery();
			while (rs.next())
			{
				if (notes == null)
				{
					notes = new TreeMap<String, List<AuthorityNote>>();
				}
					
				String codNote = rs.getString("AUT_NTE_TYP_CDE");
				List<AuthorityNote> notesList = null;
			
				if (notes.containsKey(codNote))
				{
					notesList = (List<AuthorityNote>) notes.get(codNote);
					AuthorityNote note = new AuthorityNote();
					note.setNote(rs.getString("NOTES"));
					notesList.add(note);
					
				}
				else
				{
					notesList = new ArrayList<AuthorityNote>();
					AuthorityNote note = new AuthorityNote();
					note.setNote(rs.getString("NOTES"));
					notesList.add(note);
					notes.put(codNote, notesList);
				}	
			}
		
		} catch (HibernateException e) {
			logAndWrap(e);
		} catch (SQLException e) {
			logAndWrap(e);
		} finally
		{
			if (rs != null) try { connection.close();} catch (Exception ignore) {}
			if (statement != null) try { statement.close();} catch (Exception ignore) {}
		}
		return notes;	
	}

}

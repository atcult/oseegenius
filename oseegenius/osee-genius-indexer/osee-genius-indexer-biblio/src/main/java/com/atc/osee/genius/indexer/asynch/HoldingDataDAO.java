package com.atc.osee.genius.indexer.asynch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

public class HoldingDataDAO {
	
	private final DataSource datasource;
	private final MarcFactory factory;
	
	static String SELECT_COPIES = 
			"SELECT "+
			"CPY_ID.BRCDE_NBR, CPY_RMRK_NTE, CPY_STMT_TXT,LCTN_NME_CDE, LOAN_PRD_CDE, ML.LIB_SMBL_CDE ML_SIMBOL, BRANCH.LIB_SMBL_CDE BRANCH_SIMBOL, "+  
			"CPY_ID.CPY_ID_NBR, SHLF_LIST.SHLF_LIST_STRNG_TEXT,CPY_ID.ORG_NBR, CPY_ID.BRNCH_ORG_NBR,  "+
			"T_TRLTN.LBL_STR_TXT,ORG_ENG_SRT_FORM "+   
			"FROM CPY_ID, SHLF_LIST , LCTN L, T_TRLTN, LIB ML, LIB BRANCH,ORG_NME "+
			"WHERE      "+
			"CPY_ID.BIB_ITM_NBR=? AND "+  
			"CPY_ID.SHLF_LIST_KEY_NBR = SHLF_LIST.SHLF_LIST_KEY_NBR (+) AND "+   
			"CPY_ID.LCTN_NME_CDE=LCTN_NBR (+) AND "+    
			"CPY_ID.BRNCH_ORG_NBR=L.ORG_NBR(+) AND "+    
			"(T_TRLTN.LNG_CDE IS NULL OR T_TRLTN.LNG_CDE=5) AND "+  
			"L.STR_NBR=T_TRLTN.STR_NBR (+) AND "+  
			"CPY_ID.ORG_NBR = ORG_NME.ORG_NBR AND   "+
			"CPY_ID.BRNCH_ORG_NBR=BRANCH.ORG_NBR (+) AND "+   
			"CPY_ID.ORG_NBR=ML.ORG_NBR (+) AND "+  
			"CPY_ID.BRNCH_ORG_NBR=BRANCH.ORG_NBR (+) "+ 	
			"GROUP BY ORG_ENG_SRT_FORM,CPY_ID.BRCDE_NBR, CPY_RMRK_NTE, CPY_STMT_TXT,LCTN_NME_CDE,LOAN_PRD_CDE, ML.LIB_SMBL_CDE, BRANCH.LIB_SMBL_CDE, CPY_ID.CPY_ID_NBR, SHLF_LIST.SHLF_LIST_STRNG_TEXT, CPY_ID.ORG_NBR, CPY_ID.BRNCH_ORG_NBR, T_TRLTN.LBL_STR_TXT "+  
			"ORDER BY ORG_ENG_SRT_FORM, BRANCH_SIMBOL";
		
	HoldingDataDAO(final DataSource datasource, final MarcFactory factory) {
		this.datasource = datasource;
		this.factory = factory;
	}
	
	private void addSubField(final DataField field, final char code, final String value) {
		if (value != null) {
			final Subfield s = factory.newSubfield(code, value);
			field.addSubfield(s);	
		}
	}
	
	public void addholdingData(final Record record, final int amicusNumber) throws SQLException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null; 		
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_COPIES);
			statement.setInt(1, amicusNumber);
			rs = statement.executeQuery();
			final List<String> mls = new ArrayList<String>();
			
			while (rs.next()) {
				final String mlSimbol = rs.getString("ML_SIMBOL");
				if (!mls.contains(mlSimbol)) {
					mls.add(mlSimbol);
					final DataField _850 = factory.newDataField("850", ' ', ' ');
					addSubField(_850, 'a', mlSimbol);
					record.addVariableField(_850);
				}
				
				final DataField _852 = factory.newDataField("852", ' ', ' ');
				addSubField(_852, 'a', mlSimbol);
				addSubField(_852, 'b', clean(rs.getString("CPY_STMT_TXT")));
				
				addSubField(_852, 'B', rs.getString("CPY_ID_NBR"));
				addSubField(_852, 'C', rs.getString("BRANCH_SIMBOL"));
				addSubField(_852, 'F', rs.getString("BRCDE_NBR"));
				
				addSubField(_852, 'x', clean(rs.getString("LBL_STR_TXT")));
				
				String shelf = rs.getString("SHLF_LIST_STRNG_TEXT");
				shelf = (shelf != null && shelf.length() > 2) ? clean(shelf.substring(2)) : null;
				addSubField(_852, 'm', shelf);
				
				String remarkNote = rs.getString("CPY_RMRK_NTE");
				remarkNote = (remarkNote != null && remarkNote.length() > 2) ? clean(remarkNote.substring(2)) : null;
				
				addSubField(_852, 'q', remarkNote);
				
				addSubField(_852, 'y', clean(rs.getString("LCTN_NME_CDE")));
				addSubField(_852, '8', rs.getString("LOAN_PRD_CDE"));
				
				record.addVariableField(_852);
			}
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLException(e);
		} finally {
			if (rs != null) { try { rs.close();} catch (Exception ignore) {}}
			if (statement != null) { try { statement.close();} catch (Exception ignore) {}}
			if (connection != null) { try { connection.close();} catch (Exception ignore) {}}
		}
	}
	
	/**
	 * Heading labels in AMICUS are stored together with MARC subfield delimiters.
	 * This method removes those delimiters by substituting them with a space.
	 * 
	 * @param stringTxt the AMICUS heading as we found in database.
	 * @return the heading without MARC delimiters.
	 */
	private static String clean(final String stringTxt)
	{
		if (stringTxt == null) {
			return "";
		}
		
		StringBuilder builder = new StringBuilder(stringTxt);
		for (int index = 0; index < builder.length(); index++)
		{
			char aChar = builder.charAt(index);
			if (aChar == 31)
			{
				if (index == 0)
				{
					builder.delete(index, index + 2);
					index = -1;
					continue;
				} else
				{
					builder.replace(index, index + 2, " ");	
					continue;
				}
			} 
		}
		return builder.toString().trim();
	}	
	
//	/**
//	 * Loads the holding notes for copy associated with the given copy identifier.
//	 * 
//	 * @param copyId the copy identifier.
//	 * @param copy the object that holds the copy data.
//	 * @param statement the JDBC statement used for querying the database.
//	 * @throws SQLException in case of database failure.
//	 */
//	private void loadAndStoreHoldingNotes(
//			final int copyId, 
//			final NamedList<Object> copy, 
//			final PreparedStatement statement) throws SQLException 
//	{		
//		statement.setLong(1, copyId);
//	
//		NamedList<List<String>> notesByType = new SimpleOrderedMap<List<String>>();
//		
//		ResultSet rs = null;	
//		try 
//		{
//			rs = statement.executeQuery();
//			while (rs.next())
//			{
//				String type = rs.getString("NTE_TPE");
//				String note = rs.getString("NTE_TXT");
//				
//				List<String> container = getNodeTipology(type, notesByType);
//				container.add(note);
//			}
//			
//			if (notesByType.size() != 0)
//			{
//				copy.add("notes", notesByType);
//			}
//		} finally
//		{
//			try 
//			{
//				rs.close();
//			} catch (Exception exception) 
//			{
//				// Nothing
//			}
//		}
//	}	
}

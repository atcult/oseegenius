package com.atc.osee.plugin.AMICUS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.atc.osee.web.logic.integration.AbstractDAO;
import com.atc.osee.web.logic.integration.NoSuchRecordException;
import com.atc.osee.web.model.Account;

/**
 * Account Data Access Object.
 * 
 * @author Giorgio Gazzarini
 * @since 1.0
 */
public class AccountDAO extends AbstractDAO 
{	
	private final static String SELECT_ACCOUNT ="SELECT PRSN.PRSN_NBR, PRSN_1ST_NME, PRSN_SRNME_NME " +
	"FROM PRSN,LIBRIVISION.LV_USER " +
	"WHERE LOGIN=? AND PRSN.PRSN_NBR=LIBRIVISION.LV_USER.PRSN_NBR";
	//delete comment for OG proto on It-NET
//	"FROM WLN.PRSN,WLN.LV_USER " +
//	"WHERE LOGIN=? AND PRSN.PRSN_NBR=WLN.LV_USER.PRSN_NBR"; 

	private final static String SELECT_ACCOUNT_ADDRESS = "SELECT PRSN_PSTL_ADR_TYP_CDE,PSTL_ADR_PSTL_CDE,PSTL_ADR_CTY_NME,PSTL_ADR_ST_NME,PSTL_ADR_ST_NBR,PSTL_ADR_ST_TYP_CDE,PSTL_ADR_RM_NBR,PSTL_ADR_FLR_NBR,PSTL_ADR_ST_DRCTN_CDE,ADR_PROV_TRTRY_STATE_CDE,PSTL_ADR_CNTRY_CDE,PSTL_ADR_RGN_CDE,PSTL_ADR_GEOG_CDE FROM PRSN_PSTL_ADR WHERE PRSN_PSTL_ADR_TYP_CDE IN (7) AND PRSN_NBR =?";
	private final static String SELECT_ACCOUNT_BARCODE = "SELECT PRSN_CIRTN_BRCDE_NBR FROM PRSN_CIRTN WHERE PRSN_NBR =?";

	/**
	 * Builds a new DAO with the given datasource.
	 * 
	 * @param datasource the datasource.
	 */
	public AccountDAO(DataSource datasource) 
	{
		super(datasource);
	}
	
	/**
	 * Returns the account barcode associated with the given person number.
	 * @param user number.
	 * @return the barcode associated with the given username.
	 * @throws SQLException in case of database failure.
	 * @throws NoSuchRecordException in case no account can be found.
	 */
	private void injectBarcode(Account account, Connection connection) throws SQLException, NoSuchRecordException {
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			statement = connection.prepareStatement(SELECT_ACCOUNT_BARCODE);
			statement.setInt(1, account.getId());
			rs = statement.executeQuery();
			if (rs.next())
			{
				String barcode = rs.getString("PRSN_CIRTN_BRCDE_NBR");
				account.setBarcode(barcode);
			}
		
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception)
		{
			throw new SQLException(exception);
		} finally 
		{
			try {  rs.close(); } catch (Exception exception) { }
			try {  statement.close(); } catch (Exception exception) { }
		}
	}
	

	
	/**
	 * Returns the LV borrower account associated with the given username.
	 * 
	 * @param username the borrower username.
	 * @return the LV borrower account associated with the given username.
	 * @throws SQLException in case of database failure.
	 * @throws NoSuchRecordException in case no account can be found.
	 */
	private void injectAddress(Account account, Connection connection) throws SQLException, NoSuchRecordException
	{
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try
		{
			statement = connection.prepareStatement(SELECT_ACCOUNT_ADDRESS);
			statement.setInt(1, account.getId());
			rs = statement.executeQuery();
			if (rs.next())
			{
				String streetName = rs.getString("PSTL_ADR_ST_NME");
				String streetNumber = rs.getString("PSTL_ADR_ST_NBR");
				streetName += (streetNumber != null) ? " "+streetNumber : "";
				account.setStreet(streetName);
				account.setZipCode(rs.getString("PSTL_ADR_PSTL_CDE"));
				account.setCity(rs.getString("PSTL_ADR_CTY_NME"));
				account.setState(rs.getString("ADR_PROV_TRTRY_STATE_CDE"));
			}
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception)
		{
			throw new SQLException(exception);
		} finally 
		{
			try {  rs.close(); } catch (Exception exception) { }
			try {  statement.close(); } catch (Exception exception) { }
		}
	}
	
	/**
	 * Returns the LV borrower account associated with the given username.
	 * 
	 * @param username the borrower username.
	 * @return the LV borrower account associated with the given username.
	 * @throws SQLException in case of database failure.
	 * @throws NoSuchRecordException in case no account can be found.
	 */
	public Account getAccount(String username) throws SQLException, NoSuchRecordException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_ACCOUNT);
			statement.setString(1, username);
			rs = statement.executeQuery();
			if (rs.next())
			{
				Account account = new Account(
						rs.getInt("PRSN_NBR"),
						username, 
						rs.getString("PRSN_1ST_NME"),
						rs.getString("PRSN_SRNME_NME"));
				injectAddress(account, connection);
				injectBarcode(account, connection);
				return account;
			}
			throw new NoSuchRecordException();
		} catch (NoSuchRecordException exception) 
		{
			throw exception;			
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception)
		{
			throw new SQLException(exception);
		} finally 
		{
			try {  rs.close(); } catch (Exception exception) { }
			try {  statement.close(); } catch (Exception exception) { }
			try {  connection.close(); } catch (Exception exception) { }
		}
	}			
}
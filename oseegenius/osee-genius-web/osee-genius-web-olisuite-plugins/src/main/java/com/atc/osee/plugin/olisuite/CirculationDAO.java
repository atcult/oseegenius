package com.atc.osee.plugin.olisuite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.atc.osee.web.logic.integration.AbstractDAO;
import com.atc.osee.web.model.Order;

/**
 * Circulation DAO helper.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class CirculationDAO extends AbstractDAO 
{
	private static final String SELECT_ORDERS = 
			"SELECT ORDR_ITM_QTY,ORDR_ITM_RCV_QTY,ORDR_DTE,STATUS_TYP_CDE,LIB_ORG_NBR "+ 
			"FROM ORDR_ITM,ORDR_ITM_BIB_ITM,ORDR, ORG_NME "+
			"WHERE "+
			"BIB_ITM_NBR=? AND "+
			"ORDR.ORG_NBR = ORG_NME.ORG_NBR AND "+
			"LIB_ORG_NBR = ORG_NME.ORG_NBR AND "+
			"ORDR.ORDR_NBR=ORDR_ITM_BIB_ITM.ORDR_NBR AND "+   
			"ORDR_ITM.ORDR_NBR=ORDR_ITM_BIB_ITM.ORDR_NBR AND "+ 
			"ORDR_ITM.ORDR_NBR=ORDR_ITM_BIB_ITM.ORDR_NBR AND  "+
			"ORDR_ITM_QTY > ORDR_ITM_RCV_QTY AND "+ 
			"STATUS_TYP_CDE IN (3,4) "+
			"ORDER BY ORG_FR_SRT_FORM ASC ";
	
	/**
	 * Builds a new {@link CirculationDAO} with the given datasource.
	 * @param datasource
	 */
	public CirculationDAO(final DataSource datasource) 
	{
		super(datasource);
	}
	
	/**
	 * Returns the orders associated with the given bibliographic record and library.
	 * 
	 * @param amicusNumber the record identifier.
	 * @param mainLibraryId the main library identifier.
	 * @return the orders associated with the given bibliographic record and library.
	 * @throws SQLException in case of database failure.
	 */
	public List<Order> getOrders(int amicusNumber) throws SQLException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Order> orders = new ArrayList<Order>(3);
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_ORDERS);
			statement.setInt(1, amicusNumber);
			rs = statement.executeQuery();
			while (rs.next())
			{
				orders.add(new Order(
						rs.getInt("LIB_ORG_NBR"),
						rs.getDate("ORDR_DTE"),
						rs.getInt("ORDR_ITM_QTY"),
						rs.getInt("ORDR_ITM_RCV_QTY"),
						rs.getInt("STATUS_TYP_CDE")
				));
			}
			return orders;
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception) 
		{
			throw new SQLException(exception);
		} finally
		{
			if (rs != null) try { connection.close();} catch (Exception ignore) {}
			if (statement != null) try { statement.close();} catch (Exception ignore) {}
			if (connection != null) try { connection.close();} catch (Exception ignore) {}
		}
	}
}

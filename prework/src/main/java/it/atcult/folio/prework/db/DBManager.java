package it.atcult.folio.prework.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;

import it.atcult.folio.prework.Configuration;

public class DBManager 
{
	
	private static Logger logger = Logger.getLogger(DBManager.class);
	
	public static Connection getConnection() throws SQLException
	{
	    Connection connection = null;
	    try
	    {
	    	Configuration configuration = new Configuration();
	    	Class.forName("org.postgresql.Driver");
	    	String url = configuration.getConnectionUrl();
	    	String user = configuration.getConnectionUser();
	    	String password = configuration.getConnectionPassword();
	    	connection = DriverManager.getConnection(url, user, password);
	    	connection.setAutoCommit(false);
	      
	    }
	    catch (Exception ex)
	    {
	      connection.rollback();
	      logger.fatal("Errore durante apertura connessione Pg:" + ex.getMessage());
	    }
	    
	    return connection;

	}

	public static void releaseConnection(Connection connection) {
		try 
		{
			if (connection != null)
				connection.close();
		}
		catch (SQLException sqle) 
		{
			logger.error("Errore nella chiusura connessione: " + sqle.getMessage());
		}
	}
}

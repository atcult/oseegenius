package com.atc.osee.web.plugin.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.Subscription;
import com.atc.osee.web.plugin.AccountPlugin;
import com.atc.osee.web.plugin.BadPasswordException;
import com.atc.osee.web.plugin.NoSuchAccountException;
import com.atc.osee.web.plugin.PasswordMismatchException;
import com.atc.osee.web.plugin.WrongEmailException;

/**
 * OseeGenius default (RDBMS based) account plugin implementation.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AccountPluginImpl implements AccountPlugin 
{
	private final static Logger LOGGER = LoggerFactory.getLogger(AccountPluginImpl.class);
	private DataSource datasource;
	
	@Override
	public Account getAccount(final String username) throws SystemInternalFailureException, NoSuchAccountException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement("SELECT OSEEGENIUS_USER.USERNAME, MY_ACCOUNT.* FROM OSEEGENIUS_USER,MY_ACCOUNT WHERE USERNAME=? AND OSEEGENIUS_USER.ID=MY_ACCOUNT.ID");
			statement.setString(1, username);
			
			rs =statement.executeQuery();
			if (rs.next())
			{
				return new Account(
						rs.getInt("ID"),
						rs.getString("USERNAME"),
						rs.getString("NAME"),
						rs.getString("SURNAME"),
						rs.getString("GENDER"),
						rs.getString("ZIP_CODE"),
						rs.getString("STREET"),
						rs.getString("CITY"),
						rs.getString("STATE"),
						rs.getString("INTRO"));
			}
			throw new NoSuchAccountException();
		} catch(NoSuchAccountException exception)
		{
			throw exception;
		} catch (Exception exception) 
		{
			LOGGER.error("Data access failure while loading account data for "+username, exception);
			throw new SystemInternalFailureException();
		} finally 
		{
			try 
			{
				rs.close();
			} catch (Exception exception)
			{
				// Nothing
			}
			
			try 
			{
				statement.close();
			} catch (Exception exception)
			{
				// Nothing				
			}
			
			try 
			{
				connection.close();
			} catch (Exception exception)
			{
				// Nothing								
			}
		}
	}

	@Override
	public void init(final DataSource datasource) 
	{
		this.datasource = datasource;
	}

	@Override
	public void init(final ValueParser configuration) 
	{
		// Nothing
	}

	@Override
	public void changePassword(int accountId, String oldPassword,
			String newPassword) throws
			PasswordMismatchException, BadPasswordException,
			SystemInternalFailureException
	{
		// NON E' QUESTO IL PLUGIN DOVE VA IMPLEMENTATA QUESTA FUNZIONE!
		throw new SystemInternalFailureException();
	}

	@Override
	public void changeContacts(int personNumber, String address, String cap,
			String city, String prov, String email, String telephone,
			String mobile) throws SystemInternalFailureException,
			WrongEmailException 
	{
		// NON E' QUESTO IL PLUGIN DOVE VA IMPLEMENTATA QUESTA FUNZIONE!
		throw new SystemInternalFailureException();		
	}

	@Override
	public void sendPassword(int id) throws SystemInternalFailureException,
			NoSuchAccountException 
	{
		// NON E' QUESTO IL PLUGIN DOVE VA IMPLEMENTATA QUESTA FUNZIONE!
		throw new SystemInternalFailureException();			
	}

	@Override
	public List<Subscription> getAccountSubscriptions(int accountId) throws SystemInternalFailureException, NoSuchAccountException 
	{
		// NON E' QUESTO IL PLUGIN DOVE VA IMPLEMENTATA QUESTA FUNZIONE!
		throw new SystemInternalFailureException();			
	}

	@Override
	public boolean isChangePasswordNeeded(String username)
			throws SystemInternalFailureException {
		// TODO Auto-generated method stub
		return false;
	}	
}
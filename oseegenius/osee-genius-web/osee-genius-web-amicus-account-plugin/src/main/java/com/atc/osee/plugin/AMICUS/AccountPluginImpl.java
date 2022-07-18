package com.atc.osee.plugin.AMICUS;

import java.util.List;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.logic.integration.NoSuchRecordException;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.Subscription;
import com.atc.osee.web.plugin.AccountPlugin;
import com.atc.osee.web.plugin.BadPasswordException;
import com.atc.osee.web.plugin.NoSuchAccountException;
import com.atc.osee.web.plugin.PasswordMismatchException;
import com.atc.osee.web.plugin.WrongEmailException;

/**
 * Account plugin implementation for AMICUS LMS.
 * 
 * @author ggazzarini
 * @since 1.0
 */
public class AccountPluginImpl implements AccountPlugin
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountPluginImpl.class.getName());

	private AccountDAO accountDAO;

	@Override
	public void init(final DataSource datasource)
	{
		accountDAO = new AccountDAO(datasource);
	}

	@Override
	public void init(final ValueParser configuration) 
	{
		// Nothing to be done here...
	}
	
	@Override
	public Account getAccount(final String username) throws NoSuchAccountException, SystemInternalFailureException
	{
		try 
		{
			Account account = accountDAO.getAccount(username);
			return account;
		} catch(NoSuchRecordException exception)
		{
			throw new NoSuchAccountException();
		} catch(Exception exception)
		{
			LOGGER.error("Data access failure.", exception);
			throw new SystemInternalFailureException();
		}
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
	public List<Subscription> getAccountSubscriptions(int accountId)
			throws SystemInternalFailureException, NoSuchAccountException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isChangePasswordNeeded(String username)
			throws SystemInternalFailureException {
		// TODO Auto-generated method stub
		return false;
	}	
}
/*
 	Copyright (c) 2010 @Cult s.r.l. All rights reserved.
	 
	@Cult s.r.l. makes no representations or warranties about the 
	suitability of the software, either express or implied, including
	but not limited to the implied warranties of merchantability, fitness
	for a particular purpose, or non-infringement. 
	
	@Cult s.r.l.not be liable for any damage suffered by 
	licensee as a result of using, modifying or distributing this software 
	or its derivates.
	
	This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.plugin.olisuite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.Subscription;
import com.atc.osee.web.plugin.AccountPlugin;
import com.atc.osee.web.plugin.BadPasswordException;
import com.atc.osee.web.plugin.NoSuchAccountException;
import com.atc.osee.web.plugin.PasswordMismatchException;
import com.atc.osee.web.plugin.UserDoesntHaveEmailException;
import com.atc.osee.web.plugin.WrongEmailException;
import com.atc.weloan.logic.core.model.Person;
import com.atc.weloan.logic.core.services.OnlineRemoteService;
import com.atc.weloan.shared.IncorrectOldPasswordException;
import com.atc.weloan.shared.InvalidEmailFormatException;
import com.atc.weloan.shared.WrongNewPasswordException;
import com.atc.weloan.shared.core.integration.NoSuchPersonException;

/**
 * Olisuite 1.1 implementation for OseeGenius Account Plugin.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AccountPluginImpl implements AccountPlugin
{
	private OnlineRemoteService service;
	
	private DataSource datasource;
	
	@Override
	public void init(final DataSource datasource) 
	{
		this.datasource = datasource;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void init(ValueParser configuration) 
	{
		Context olisuiteNamingContext = null;

		String olisuiteInitialNamingFactory = configuration.getString(OLISUITE_NAMING_CONTEXT_FACTORY_PROPERTY_NAME, null);
		String olisuiteProviderURL = configuration.getString(OLISUITE_PROVIDER_URL_PROPERTY_NAME);

		if (olisuiteProviderURL != null && olisuiteInitialNamingFactory != null)
		{
			Hashtable olisuiteEnvironment = new Hashtable();
			olisuiteEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, olisuiteInitialNamingFactory);
			olisuiteEnvironment.put(Context.PROVIDER_URL, olisuiteProviderURL);
			try 
			{ 
				olisuiteNamingContext = new InitialContext(olisuiteEnvironment);
				service = (OnlineRemoteService) olisuiteNamingContext.lookup(configuration.getString(OLISUITE_ONLINE_SERVICE_NAME));
			} catch (Exception exception)
			{
				Log.error("Unable to lookup the OliSuite Borrower management service .", exception);
			}
		}		
	}

	// TODO : integrare alla luce dei nuovi servizi OG
	@Override
	public Account getAccount(final String username) throws SystemInternalFailureException, NoSuchAccountException 
	{
		try 
		{
			Person person = service.getBorrower(username);
			Account account = new Account(
					person.getPersonNumber(), 
					username, 
					person.getName(), 
					person.getSurname(), 
					null, //					person.getGender(), 
					person.getAddress().getPostalCode(),
					person.getAddress().getStreetName(),
					person.getAddress().getCity(),
					person.getAddress().getRegion(),
					null);
			account.setBirthDate(person.getBirthDate());
			account.setCodiceFiscale(person.getCodiceFiscale());
			account.setMobile(person.getMobile());
			account.setTelephone(person.getTelephone());
			account.setEmail(person.getMail());
			account.setStreetNumber(person.getAddress().getStreetNumber());
			account.setState(person.getAddress().getProvinceState());
			
			account.setSubscriptions(getAccountSubscriptions(account.getId()));
			
			return account;
		} catch (com.atc.weloan.shared.SystemInternalFailureException exception) 
		{
			Log.error("Internal exception occured on Olisuite server.", exception);
			throw new SystemInternalFailureException(exception);
		} catch (NoSuchPersonException exception) 
		{
			throw new NoSuchAccountException();
		}
	}

	@Override
	public void changePassword(
			final int accountId, 
			final String oldPassword,
			final String newPassword) 
					throws PasswordMismatchException, 
						BadPasswordException, SystemInternalFailureException 
	{
		
		try {
			
			service.changePassword(accountId, oldPassword, newPassword);
		
		} catch (NoSuchPersonException exception) {
			
			Log.error("The account " + accountId +  "cannot be found.", exception);
			throw new SystemInternalFailureException();
			
		} catch (WrongNewPasswordException exception) {
			
			throw new BadPasswordException();
			
		} catch (com.atc.weloan.shared.SystemInternalFailureException e) {
			
			throw new SystemInternalFailureException(e);
			
		} catch (IncorrectOldPasswordException e) {
			
			throw new  PasswordMismatchException();
		}
	}

	@Override
	public void changeContacts(
			int accountId, 
			String address, 
			String cap,
			String city, 
			String prov, 
			String email, 
			String telephone,
			String mobile) throws SystemInternalFailureException, WrongEmailException 
	{
		StringBuilder communicationModeBuilder = new StringBuilder();
		communicationModeBuilder.append(mobile != null && mobile.trim().length() != 0 ? "1" : "0");
		communicationModeBuilder.append("1");
		communicationModeBuilder.append(address != null && address.trim().length() != 0 ? "1" : "0");
		
		try 
		{
			service.changeContacts(
					accountId, 
					address, 
					cap, city, prov, email, telephone, mobile, 
					communicationModeBuilder.toString());
		} catch (NoSuchPersonException exception) 
		{
			Log.error("The account " + accountId +  "cannot be found.", exception);
			throw new SystemInternalFailureException();
		} catch (InvalidEmailFormatException exception) 
		{
			throw new WrongEmailException();
		} catch (com.atc.weloan.shared.SystemInternalFailureException exception) 
		{
			throw new SystemInternalFailureException(exception);
		}
	}

	@Override
	public void sendPassword(final int accountId) throws SystemInternalFailureException, NoSuchAccountException, UserDoesntHaveEmailException 
	{
		try 
		{
			service.sendPassword(accountId);
		} catch (NoSuchPersonException exception) 
		{
			throw new NoSuchAccountException();
		} catch (com.atc.weloan.shared.SystemInternalFailureException exception) 
		{
			throw new SystemInternalFailureException(exception);
		} catch (InvalidEmailFormatException exception)
		{
			throw new UserDoesntHaveEmailException();
		}
	}

	@Override
	public List<Subscription> getAccountSubscriptions(int accountId) throws SystemInternalFailureException, NoSuchAccountException 
	{
		try 
		{
			Connection connection = null;
			PreparedStatement statement = null;
			ResultSet rs = null;
			
			connection = datasource.getConnection();
			statement = connection.prepareStatement(
					"SELECT PRSN_CIRTN.ORG_NBR,PRSN_CIRTN_TYP_CDE,CIR_DTE, CIR_END_DATE, PRSN_CIRTN_BLCKL_DTE, PRSN_CIRTN_TRP_IND, PRSN_CIRTN_NOT_BRW_IND, SRVC_ACT, PRSN_CIRTN.PRSN_CIRTN_BRCDE_NBR  "+
					"FROM PRSN_CIRTN, CUSTOM.ONL_SRVC_ACT, ORG_NME WHERE PRSN_NBR=? AND PRSN_CIRTN.ORG_NBR=ORG_NME.ORG_NBR AND PRSN_CIRTN.ORG_NBR=CUSTOM.ONL_SRVC_ACT.ORG_NBR(+) ORDER BY ORG_ENG_NME ASC");

			statement.setInt(1, accountId);
			rs = statement.executeQuery();
			
			List<Subscription> subscriptions = new ArrayList<Subscription>();
			while (rs.next())
			{
				subscriptions.add(new Subscription(
						rs.getInt("ORG_NBR"),
						rs.getDate("CIR_DTE"),
						rs.getDate("CIR_END_DATE"),
						rs.getDate("PRSN_CIRTN_BLCKL_DTE"),
						!rs.getBoolean("PRSN_CIRTN_NOT_BRW_IND"),
						rs.getBoolean("PRSN_CIRTN_TRP_IND"),
						rs.getInt("PRSN_CIRTN_TYP_CDE"),
						rs.getObject("SRVC_ACT") != null,
						rs.getString("PRSN_CIRTN_BRCDE_NBR")));
				
			}
			return subscriptions;
		} catch (Exception exception) 
		{
			throw new SystemInternalFailureException(exception);
		} 
	}

	@Override
	public boolean isChangePasswordNeeded(final String username) throws SystemInternalFailureException 
	{
		try 
		{
			Connection connection = null;
			PreparedStatement statement = null;
			ResultSet rs = null;
			
			connection = datasource.getConnection();
			statement = connection.prepareStatement("SELECT PASSWORD FROM LV_USER WHERE LOGIN=?");

			statement.setString(1, username);
			rs = statement.executeQuery();
			
			if (rs.next())
			{
				try 
				{
					Integer.parseInt(rs.getString(1));
					return true;
				} catch (Exception exception) 
				{
					return false;
				}
			}
			throw new SystemInternalFailureException();
		} catch (SystemInternalFailureException exception)
		{
			throw exception;
		} catch (Exception exception) 
		{
			throw new SystemInternalFailureException(exception);
		} 
	}
}
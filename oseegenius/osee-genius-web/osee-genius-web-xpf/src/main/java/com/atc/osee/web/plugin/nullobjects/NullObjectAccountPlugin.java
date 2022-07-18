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
package com.atc.osee.web.plugin.nullobjects;
 
import java.util.List;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.Subscription;
import com.atc.osee.web.plugin.AccountPlugin;
import com.atc.osee.web.plugin.BadPasswordException;
import com.atc.osee.web.plugin.NoSuchAccountException;
import com.atc.osee.web.plugin.PasswordMismatchException;
import com.atc.osee.web.plugin.UserDoesntHaveEmailException;
import com.atc.osee.web.plugin.WrongEmailException;

/**
 * Null Object plugin implementation for account plugin.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class NullObjectAccountPlugin implements AccountPlugin 
{	
	@Override
	public Account getAccount(final String username) throws NoSuchAccountException
	{
		return new Account(-1, username, username, username);
	}

	@Override
	public void init(final DataSource datasource) 
	{
		// Nothing to be done here...
	}

	@Override
	public void init(ValueParser configuration) 
	{
		// Nothing to be done here...
	}

	@Override
	public void sendPassword(int id) throws SystemInternalFailureException,
			NoSuchAccountException, UserDoesntHaveEmailException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changePassword(int accountId, String oldPassword,
			String newPassword) throws PasswordMismatchException,
			BadPasswordException, SystemInternalFailureException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeContacts(int personNumber, String address, String cap,
			String city, String prov, String email, String telephone,
			String mobile) throws SystemInternalFailureException,
			WrongEmailException {
		// TODO Auto-generated method stub
		
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
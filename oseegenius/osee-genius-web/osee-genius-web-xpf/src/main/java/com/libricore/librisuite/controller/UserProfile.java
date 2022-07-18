/*
 * (c) LibriCore
 * 
 * Created on Jun 18, 2004
 * 
 * UserProfile.java
 */
package com.libricore.librisuite.controller;

import librisuite.business.authentication.DAOUserAccount;
import librisuite.business.authorisation.AmicusAuthority;
import librisuite.business.authorisation.AuthorisationAgent;
import librisuite.business.authorisation.AuthorisationException;
import librisuite.business.authorisation.Permission;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.hibernate.T_ITM_DSPLY;
import librisuite.hibernate.USR_ACNT;

/**
 * User information affecting application behaviour
 * @author paulm
 */
public class UserProfile {
	private int cataloguingView;
	private T_ITM_DSPLY defaultRecordDisplay;
	private int mainLibrary;
	private int branchLibrary;
	private AuthorisationAgent authorisationAgent;
	private String name;
	private int maxRecordCount;
	String startApp = Defaults.getString("starting.application");

	public UserProfile(String name) throws DataAccessException {
		USR_ACNT aUserAccount = new DAOUserAccount().load(name);
		if(aUserAccount==null) {
			throw new NullPointerException("User "+name+" not found in the database");
		}
		//MAURA
		/*ORG_HRCHY anOrgHierarchy =
			new DAOOrganisationHierarchy().load(
				aUserAccount.getBranchLibrary());*/
		int realView = getRealUserView(aUserAccount);
		setCataloguingView(realView);
		setDefaultRecordDisplay(aUserAccount.getDefaultRecordDisplay());
		setBranchLibrary(aUserAccount.getBranchLibrary());
		//MAURA
		//setMainLibrary(anOrgHierarchy.getPARNT_ORG_NBR());
		this.name = aUserAccount.getName();
		this.maxRecordCount = aUserAccount.getMaxRecordCount();
		setAuthorisationAgent(new AmicusAuthority(name));
	}

	/**
	 * It depends upon the starting application "mad"
	 * TODO _MIKE: Both mades and LibriCat cataloguing view can be active simultaneously
	 * @param aUserAccount
	 * @return
	 */
	private int getRealUserView(USR_ACNT aUserAccount) {
		int uaView = aUserAccount.getCataloguingView();
		int realView = uaView;
		if("mad".equalsIgnoreCase(startApp)){
			realView = convert(uaView);
		}
		return realView;
	}

	/**
	 * Converte la vista catalografica dell'utente nella vista interna Mades
	 * secondo la seguente tabella:
	 * <pre>
	 * userView  | madesView
	 * ----------+-----------
	 *    1      |    -2
	 *    2      |    -3
	 *    3      |    -4
	 * ----------^-----------
	 * </pre>
	 * @param userView
	 * @return
	 */
	private int convert(int userView) {
		
		return -(userView + 1);
	}

	/**
	 * Getter for cataloguingView
	 * 
	 * @return cataloguingView
	 */
	public int getCataloguingView() {
		return cataloguingView;
	}

	/**
	 * Setter for cataloguingView
	 * 
	 * @param s cataloguingView
	 */
	public void setCataloguingView(int s) {
		cataloguingView = s;
	}

	public int getBranchLibrary() {
		return branchLibrary;
	}

	public void setBranchLibrary(int i) {
		branchLibrary = i;
	}

	public int getMainLibrary() {
		return mainLibrary;
	}

	public void setMainLibrary(int i) {
		mainLibrary = i;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public AuthorisationAgent getAuthorisationAgent() {
		return authorisationAgent;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setAuthorisationAgent(AuthorisationAgent agent) {
		authorisationAgent = agent;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void checkPermission(Permission aPermission)
		throws AuthorisationException {
		authorisationAgent.checkPermission(aPermission);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void checkPermission(Permission[] somePermissions)
		throws AuthorisationException {
		authorisationAgent.checkPermission(somePermissions);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void checkPermission(String permissionName)
		throws AuthorisationException {
		authorisationAgent.checkPermission(permissionName);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void checkPermission(String[] someNames)
		throws AuthorisationException {
		authorisationAgent.checkPermission(someNames);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public T_ITM_DSPLY getDefaultRecordDisplay() {
		return defaultRecordDisplay;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setDefaultRecordDisplay(T_ITM_DSPLY t_itm_dsply) {
		defaultRecordDisplay = t_itm_dsply;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public int getMaxRecordCount() {
		return maxRecordCount;
	}

	
}

package com.atc.osee.web.model;

import java.io.Serializable;
import java.util.Date;

/**
 * A subscription that a given user has with a specific (main) library.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class Subscription implements Serializable 
{
	private static final long serialVersionUID = -1082947697535840377L;

	private final int libraryId;
	private final Date activationDate;
	private final Date deactivationDate;

	private final Date blacklistDate;
	private final boolean loanEnabled;
	private final boolean trapSet;

	private final int userType;

	private final boolean onlineServicesEnabled;
	
	private String barcode;
	/**
	 * Builds a new subscription with the given data.
	 * 
	 * @param libraryName the library name.
	 * @param libraryId the library identifier.
	 * @param activationDate the subscription activation date.
	 * @param deactivationDate the subscription expiry date.
	 * @param blacklisted if the subscription has been blacklisted.
	 * @param loanEnabled if the subscription is allowed for loans.
	 * @param trapSet if the subscription has been trapset.
	 * @param userType the user type associated with subscription.
	 */
	public Subscription(
			final int libraryId, 
			final Date activationDate,
			final Date deactivationDate, 
			final Date blacklistDate, 
			final boolean loanEnabled,
			final boolean trapSet, 
			final int userType,
			final boolean onlineServicesEnabled,
			final String barcode) 
	{
		this.libraryId = libraryId;
		this.activationDate = activationDate;
		this.deactivationDate = deactivationDate;
		this.blacklistDate = blacklistDate;
		this.loanEnabled = loanEnabled;
		this.trapSet = trapSet;
		this.userType = userType;
		this.onlineServicesEnabled = onlineServicesEnabled;
		this.barcode = barcode;
	}

	/**
	 * Returns the library identifier.
	 * 
	 * @return the library identifier.
	 */
	public int getLibraryId() 
	{
		return libraryId;
	}

	/**
	 * Returns the activation date.
	 * 
	 * @return the activation date.
	 */
	public Date getActivationDate() 
	{
		return activationDate;
	}

	/**
	 * Returns the expiry date.
	 * 
	 * @return the expiry date.
	 */
	public Date getDeactivationDate() 
	{
		return deactivationDate;
	}

	/**
	 * Returns true if this subscription has been blacklisted.
	 * 
	 * @return true if this subscription has been blacklisted.
	 */
	public Date getBlacklistDate() 
	{
		return blacklistDate;
	}

	/**
	 * Returns true if this subscription has been enabled for loans.
	 * 
	 * @return true if this subscription has been enabled for loans.
	 */
	public boolean isLoanEnabled() 
	{
		return loanEnabled;
	}

	/**
	 * Returns true if this subscription has been trapset.
	 * 
	 * @return true if this subscription has been trapset.
	 */
	public boolean isTrapSet() 
	{
		return trapSet;
	}

	/**
	 * Returns the user type of this subscription.
	 * 
	 * @return the user type of this subscription.
	 */
	public int getUserType() 
	{
		return userType;
	}

	public boolean isOnlineServicesEnabled() {
		return onlineServicesEnabled;
	}

	/**
	 * @return the barcode
	 */
	public String getBarcode() {
		return barcode;
	}

	/**
	 * @param barcode the barcode to set
	 */
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	
}
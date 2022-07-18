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
package com.atc.osee.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OseeGEnius -W- account.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Account implements Serializable
{
	private static final long serialVersionUID = -9064431338987789580L;
	
	private String authToken;
	private final int id;

	private final String username;
	
	private Date birthDate;
	
	private final String name;
	private final String surname;
	
	private String street;
	private String streetNumber;
	

	private String zipCode;
	private String city;
	private String state;
	private String country;
	
	private String gender;;
	private String intro;
	
	private String codiceFiscale;
	
	private String mobile;
	private String telephone;
	private String email;
	
	private String barcode;
	
	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	private Map<String, Object> customAttributes = new HashMap<String, Object>();
	private List<Subscription> subscriptions = new ArrayList<Subscription>();
	private List<Integer> subscriptionsId = new ArrayList<Integer>();
	
	
	
	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	/**
	 * Builds an account with the given data.
	 * 
	 * @param id the account identifier.
	 * @param username the username.
	 * @param name the account name.
	 * @param surname the account surname.
	 */
	public Account(final int id, final String username, final String name, final String surname)
	{
		this.username = username;
		this.name = name;
		this.surname = surname;
		this.id = id;		
	}
	
	/**
	 * Builds a new account with the given data.
	 * 
	 * @param id the account identifier.
	 * @param username the username.
	 * @param name the account name.
	 * @param surname the account surname.
	 * @param gender the gender.
	 * @param zipCode the zip code.
	 * @param street the street (address).
	 * @param city the city.
	 * @param state the state / province.
	 * @param intro an summary / introduction.
	 */
	public Account(
			final int id, 
			final String username, 
			final String name, 
			final String surname, 
			final String gender, 
			final String zipCode, 
			final String street, 
			final String city, 
			final String state, 
			final String intro) 
	{
		this.username = username;
		this.name = name;
		this.surname = surname;
		this.id = id;
		this.gender = gender;
		this.zipCode = zipCode;
		this.street = street;
		this.city = city;
		this.state = state;
		this.intro = intro;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	/**
	 * Returns the value of a custom attribute name.
	 * 
	 * @param attributeName the attribute name.
	 * @return the value of a custom attribute name.
	 */
	public Object getAttribute(final String attributeName)
	{
		return customAttributes.get(attributeName);
	}
	
	/**
	 * Sets a custom attribute name.
	 * 
	 * @param attributeName the attribute name.
	 * @return the value of a custom attribute name.
	 */
	public void setAttribute(final String name, final Object value)
	{
		customAttributes.put(name, value);
	}	
	
	/**
	 * Returns the user summary.
	 * 
	 * @return the user summary-
	 */
	public String getIntro()
	{
		return intro;
	}
	
	/**
	 * Returns the username.
	 * 
	 * @return the username.
	 */
	public String getUsername() 
	{
		return username;
	}

	/**
	 * Returns the user name.
	 * 
	 * @return the user name.
	 */
	public String getName() 
	{
		return name;
	}
	
	/**
	 * Returns the user surname.
	 * 
	 * @return the user surname.
	 */
	public String getSurname() 
	{
		return surname;
	}

	/**
	 * Returns the account identifier.
	 * 
	 * @return the account identifier.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Returns the street address.
	 * 
	 * @return the street address.
	 */
	public String getStreet()
	{
		return street;
	}

	/**
	 * Sets the street address.
	 * 
	 * @param address the street address.
	 */
	public void setStreet(final String address)
	{
		this.street = address;
	}

	/**
	 * Returns the zip code.
	 * 
	 * @return the zip code.
	 */
	public String getZipCode()
	{
		return zipCode;
	}

	/**
	 * Sets the zip code.
	 * 
	 * @param zipCode the zip code.
	 */
	public void setZipCode(final String zipCode)
	{
		this.zipCode = zipCode;
	}

	/**
	 * Returns the account city.
	 * 
	 * @return the account city.
	 */
	public String getCity()
	{
		return city;
	}

	/**
	 * Sets the account city.
	 * 
	 * @param city the account city.
	 */
	public void setCity(final String city)
	{
		this.city = city;
	}

	/**
	 * Returns the user state / province.
	 * 
	 * @return the user state / province.
	 */
	public String getState()
	{
		return state;
	}

	/**
	 * Sets the account state / province.
	 * 
	 * @param state the account state / province.
	 */
	public void setState(final String state)
	{
		this.state = state;
	}

	/**
	 * Returns the user country.
	 * 
	 * @return the user country.
	 */
	public String getCountry()
	{
		return country;
	}

	/**
	 * Sets the user country.
	 * 
	 * @param country the user country.
	 */
	public void setCountry(final String country)
	{
		this.country = country;
	}

	/**
	 * Returns the user gender.
	 * 
	 * @return the user gender.
	 */
	public String getGender()
	{
		return gender;
	}		
	
	/**
	 * Returns the codice fiscale.
	 * 
	 * @return the codice fiscale.
	 */
	public String getCodiceFiscale()
	{
		return codiceFiscale;
	}
	
	/**
	 * Returns the codice fiscale.
	 * 
	 * @param codiceFiscale the codice fiscale.
	 */
	public void setCodiceFiscale(final String codiceFiscale)
	{
		this.codiceFiscale = codiceFiscale;
	}

	public String getMobile() 
	{
		return mobile;
	}

	public void setMobile(String mobile) 
	{
		this.mobile = mobile;
	}

	public String getTelephone() 
	{
		return telephone;
	}

	public void setTelephone(String telephone) 
	{
		this.telephone = telephone;
	}

	public String getEmail() 
	{
		return email;
	}

	public void setEmail(String email) 
	{
		this.email = email;
	}
	
	/**
	 * @return the streetNumber
	 */
	public String getStreetNumber() {
		return streetNumber;
	}

	/**
	 * @param streetNumber the streetNumber to set
	 */
	public void setStreetNumber(String streetNumber) {
		this.streetNumber = streetNumber;
	}

	public List<Subscription> getSubscriptions() 
	{
		return subscriptions;
	}

	public void setSubscriptions(List<Subscription> subscriptions) 
	{
		this.subscriptions = subscriptions;
		for (Subscription subscription: subscriptions)
		{
			subscriptionsId.add(subscription.getLibraryId());
		}
	}
	
	public boolean isSubscribed(int libraryId)
	{
		return subscriptionsId.contains(libraryId);
	}
	
	private boolean changePasswordCheckNeverDone = true;

	public boolean changePasswordCheckNeverDone() 
	{
		return changePasswordCheckNeverDone;
	}
	
	public void unsetChangePasswordCheck()
	{
		changePasswordCheckNeverDone = false;
	}
}
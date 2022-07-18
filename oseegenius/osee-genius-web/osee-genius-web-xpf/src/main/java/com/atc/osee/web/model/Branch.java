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
import java.util.Date;
import java.util.List;

import com.atc.osee.web.Utility;

/**
 * A library branch.
 * Inherithing this structure from AMICUS LMS, OseeGenius organizes libraries in
 * Main libraries (the underlying organization) and branches.
 *  
 * @author agazzarini
 * @since 1.0
 */
public class Branch implements Serializable , Comparable<Branch>
{
	private static final long serialVersionUID = 1028858319778415949L;

	private final int id;
	private final int parentId;
	private final String name;
	private String street;
	private String zipCode;
	private String city;
	private String telephone;
	private String fax;
	private String email;
	private String province;

	private String symbol;
	
	private List<DailyOpeningHours> openingHours;
	
	private String latLon;
	
	private String type_bib;
	private String contact;
	private String onlineEmail;
	
	private String webSite;
	private List<Date> closingDays;
	

	/**
	 * Builds a new branch with the given data.
	 * 
	 * @param id the branch identifier.
	 * @param parentId the parent organization identifier (the main library).
	 * @param name the name of the branch.
	 * @param symbol the library symbol.
	 */
	public Branch(final int id, final int parentId, final String name, final String symbol)
	{
		this.id = id;
		this.parentId = parentId;
		this.name = name;
		this.symbol = symbol;
	}
	
	/**
	 * Builds a new branch with the given data.
	 * 
	 * @param id the branch identifier.
	 * @param parentId the parent organization identifier (the main library).
	 * @param name the name of the branch.
	 * @param symbol the library symbol.
	 * @param latLon the google lat/lon.
	 */
	public Branch(final int id, final int parentId, final String name, final String symbol, final String latLon)
	{
		this.id = id;
		this.parentId = parentId;
		this.name = Utility.getTitleCase(name);
		this.symbol = symbol;
		this.latLon = latLon;
	}
	
	/**
	 * Returns the branch identifier.
	 * 
	 * @return the branch identifier.
	 */
	public int getId() 
	{
		return id;
	}

	/**
	 * Returns the branch name.
	 * 
	 * @return the branch name.
	 */
	public String getName() 
	{
		return name;
	}
	
	/**
	 * Returns the parent identifier.
	 * 
	 * @return the parent identifier.
	 */
	public int getParentId() 
	{
		return parentId;
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
	 * Sets the zip code of the branch.
	 * 
	 * @param zipCode the zip code of the branch.
	 */
	public void setZipCode(final String zipCode) 
	{
		this.zipCode = zipCode;
	}

	/**
	 * Returns the branch email address.
	 * 
	 * @return the branch email address.
	 */
	public String getEmail() 
	{
		return email;
	}

	/**
	 * Sets the branch email address.
	 * 
	 * @param email the branch email address.
	 */
	public void setEmail(final String email) 
	{
		this.email = email;
	}

	/**
	 * Returns the branch telephone number.
	 * 
	 * @return the branch telephone number.
	 */
	public String getTelephone() 
	{
		return telephone;
	}

	/**
	 * Sets the branch telephone number.
	 * 
	 * @param telephone the branch telephone number.
	 */
	public void setTelephone(final String telephone) 
	{
		this.telephone = telephone;
	}

	/**
	 * Returns the branch fax number.
	 * 
	 * @return the branch fax number.
	 */
	public String getFax() 
	{
		return fax;
	}

	/**
	 * Sets the branch fax number.
	 * 
	 * @param fax the branch fax number.
	 */
	public void setFax(final String fax) 
	{
		this.fax = fax;
	}

	/**
	 * Returns the branch city.
	 * 
	 * @return the branch city.
	 */
	public String getCity() 
	{
		return city;
	}

	/**
	 * Sets the branch city.
	 * 
	 * @param city the branch city.
	 */
	public void setCity(final String city) 
	{
		this.city = city;
	}

	@Override
	public boolean equals(final Object obj) 
	{
		try 
		{
			Branch branch = (Branch) obj;
			return branch.id == id;
		} catch (Exception exception) 
		{
			return false;
		}
	}
	
	/**
	 * Returns the opening and closing hours of the library.
	 * 
	 * @return the opening and closing hours of the library.
	 */
	public List<DailyOpeningHours> getOpeningHours() 
	{
		return openingHours;
	}

	/**
	 * Sets the opening and closing hours of the library.
	 * 
	 * @param openingHours the opening and closing hours of the library.
	 */	
	public void setOpeningHours(final List<DailyOpeningHours> openingHours) 
	{
		this.openingHours = openingHours;
	}

	/**
	 * Returns the street address of the branch.
	 * 
	 * @return the street address of the branch.
	 */
	public String getStreet() 
	{
		return street;
	}

	/**
	 * Sets the street address of the branch.
	 * 
	 * @param street the street address of the branch.
	 */
	public void setStreet(final String street) 
	{
		this.street = street;
	}

	/**
	 * Returns the state / province of the branch.
	 * 
	 * @return the state / province of the branch.
	 */
	public String getProvince()
	{
		return province;
	}

	/**
	 * Sets the state / province of the branch.
	 * 
	 * @param province the state / province of the branch.
	 */
	public void setProvince(final String province)
	{
		this.province = province;
	}

	@Override
	public int hashCode() 
	{
		return id;
	}

	@Override
	public int compareTo(final Branch anotherBranch) 
	{
		try 
		{
			return name.compareTo(anotherBranch.name);
		} catch (Exception exception) 
		{
			return 0;
		}
	}
		
	/**
	 * Returns the symbol of this library.
	 * 
	 * @return the symbol of this library.
	 */
	public String getSymbol()
	{
		return symbol;
	}	
	
	/**
	 * @param symbol the symbol to set
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * @return the latLon
	 */
	public String getLatLon() {
		return latLon;
	}

	/**
	 * @param latLon the latLon to set
	 */
	public void setLatLon(String latLon) {
		this.latLon = latLon;
	}

	/**
	 * @return the type_bib
	 */
	public String getType_bib() {
		return type_bib;
	}

	/**
	 * @param type_bib the type_bib to set
	 */
	public void setType_bib(String type_bib) {
		this.type_bib = type_bib;
	}

	/**
	 * @return the contact
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * @param contact the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}
	/**
	 * 
	 * @return the online mail
	 */
	public String getOnlineEmail() {
		return onlineEmail;
	}
    /**
     * 
     * @param onlineEmail the onlineEmail to set
     */
	public void setOnlineEmail(String onlineEmail) {
		this.onlineEmail = onlineEmail;
	}

	/**
	 * @return the webSite
	 */
	public String getWebSite() {
		return webSite;
	}

	/**
	 * @param webSite the webSite to set
	 */
	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}

	/**
	 * @return the closingDays
	 */
	public List<Date> getClosingDays() {
		return closingDays;
	}

	/**
	 * @param closingDays the closingDays to set
	 */
	public void setClosingDays(List<Date> closingDays) {
		this.closingDays = closingDays;
	}
	
}
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
import java.util.Set;
import java.util.TreeSet;

import com.atc.osee.web.Utility;

/**
 * A library institution.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Library implements Serializable , Comparable<Library>
{
	private static final long serialVersionUID = 1063909717797181916L;

	private int id;  
	private String name;  
	private String symbol;  
	private String city;

	private Set<Branch> branches = new TreeSet<Branch>();

	/**
	 * Builds a new library with the given identifier.
	 * 
	 * @param id the library identifier.
	 */
	public Library(final int id) 
	{
		this.id = id;
	}	
	
	/**
	 * Builds a new library with the given data.
	 * 
	 * @param id the library identifier.
	 * @param name the library name.
	 * @param symbol the library symbol.
	 */
	public Library(final int id, final String name, final String symbol) 
	{
		this.id = id;
		setName(name);
		setSymbol(symbol);
	}

	public String toString() {
		return String.valueOf(id);
	}
	
	/**
	 * Builds a new library with no data.
	 */
	public Library() 
	{
		// is it useful??
	}

	/**
	 * Returns the library identifier.
	 * 
	 * @return the library identifier.
	 */
	public int getId() 
	{
		return id;
	}

	/**
	 * Sets the library identifier.
	 * 
	 * @param id the library identifier.
	 */
	public void setId(final int id) 
	{
		this.id = id;
	}

	/**
	 * Returns the library name.
	 *  
	 * @return the library name.
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * Sets the library name.
	 * 
	 * @param name the library name.
	 */
	public void setName(final String name) 
	{
		this.name = Utility.getTitleCase(name);
	}

	/**
	 * Returns the library symbol.
	 * 
	 * @return the library symbol.
	 */
	public String getSymbol() 
	{
		return symbol;
	}

	/**
	 * Sets the library symbol.
	 * 
	 * @param symbol the library symbol.
	 */
	public void setSymbol(final String symbol) 
	{
		this.symbol = symbol;
	}

	/**
	 * Returns the library branches.
	 * 
	 * @return the library branches.
	 */
	public Set<Branch> getBranches() 
	{
		return branches;
	}
	
	/**
	 * Adds a new branch to this library.
	 * 
	 * @param branch the new "child" branch.
	 */
	public void addBranch(final Branch branch)
	{
		branches.add(branch);
	}

	/**
	 * Returns the city of this library.
	 * 
	 * @return the city of this library.
	 */
	public String getCity() 
	{
		return city;
	}

	/**
	 * Sets the city of this library.
	 * 
	 * @param city the city of this library.
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
			Library library = (Library) obj;
			return library.id == id;
		} catch (Exception exception) 
		{
			return false;
		}
	}
	
	@Override
	public int hashCode() 
	{
		return id;
	}

	@Override
	public int compareTo(final Library anotherLibrary) 
	{
		try 
		{
			return name.compareTo(anotherLibrary.name);
		} catch (Exception exception) 
		{
			return 0;
		}
	}
}

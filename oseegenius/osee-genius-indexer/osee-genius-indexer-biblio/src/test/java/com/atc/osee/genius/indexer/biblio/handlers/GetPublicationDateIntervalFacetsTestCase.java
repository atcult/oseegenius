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
package com.atc.osee.genius.indexer.biblio.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for OBML publication date facets function.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetPublicationDateIntervalFacetsTestCase 
{
	private GetPublicationDateIntervalFacets handler;
	private String notAvailable = "N.A.";
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		handler = new GetPublicationDateIntervalFacets();
	}
	
	/**
	 * Tests the execution of the function when the publication date is this year.
	 * 
	 * <br/>precondition: the publication date corresponds to this year.
	 * <br/>postcondition: 5 interval facets have been returned as result.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void thisYear()
	{
		String year = String.valueOf(handler.getCurrentYear());
		
		Collection<String> result = (Collection<String>) handler.decorate(notAvailable, year);
		
		assertEquals(5, result.size());
		
		assertTrue(result.contains(GetPublicationDateIntervalFacets.THIS_YEAR));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_2_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_5_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_10_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_50_YEARS));

		assertFalse(result.contains(GetPublicationDateIntervalFacets.MORE_THAN_50_YEARS_AGO));
	}
	
	/**
	 * Tests the execution of the function when the publication date is two years ago.
	 * 
	 * <br/>precondition: the publication date corresponds to two year ago.
	 * <br/>postcondition: 4 interval facets have been returned as result.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void twoYearsAgo()
	{
		String year = String.valueOf(handler.getCurrentYear() - 1);
		
		Collection<String> result = (Collection<String>) handler.decorate(notAvailable, year);
		
		assertEquals(4, result.size());
		
		assertFalse(result.contains(GetPublicationDateIntervalFacets.THIS_YEAR));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_2_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_5_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_10_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_50_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.MORE_THAN_50_YEARS_AGO));
	}
	
	/**
	 * Tests the execution of the function when the publication date is five years ago.
	 * 
	 * <br/>precondition: the publication date corresponds to five year ago.
	 * <br/>postcondition: 3 interval facets have been returned as result.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void fiveYearsAgo()
	{
		String year = String.valueOf(handler.getCurrentYear() - 4);
		
		Collection<String> result = (Collection<String>) handler.decorate(notAvailable, year);
		
		assertEquals(3, result.size());
		
		assertFalse(result.contains(GetPublicationDateIntervalFacets.THIS_YEAR));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_2_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_5_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_10_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_50_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.MORE_THAN_50_YEARS_AGO));
	}	
	
	/**
	 * Tests the execution of the function when the publication date is ten years ago.
	 * 
	 * <br/>precondition: the publication date corresponds to ten year ago.
	 * <br/>postcondition: 2 interval facets have been returned as result.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void tenYearsAgo()
	{
		String year = String.valueOf(handler.getCurrentYear() - 9);
		
		Collection<String> result = (Collection<String>) handler.decorate(notAvailable, year);
		
		assertEquals(2, result.size());
		
		assertFalse(result.contains(GetPublicationDateIntervalFacets.THIS_YEAR));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_2_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_5_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_10_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_50_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.MORE_THAN_50_YEARS_AGO));
	}	
	
	/**
	 * Tests the execution of the function when the publication date is 40 years ago.
	 * 
	 * <br/>precondition: the publication date corresponds to 40 year ago.
	 * <br/>postcondition: 1 interval facet has been returned as result.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void fourtyYearsAgo()
	{
		String year = String.valueOf(handler.getCurrentYear() - 40);
		
		Collection<String> result = (Collection<String>) handler.decorate(notAvailable, year);
		
		assertEquals(1, result.size());
		
		assertFalse(result.contains(GetPublicationDateIntervalFacets.THIS_YEAR));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_2_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_5_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_10_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.LAST_50_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.MORE_THAN_50_YEARS_AGO));
	}		
	
	/**
	 * Tests the execution of the function when the publication date is more than 50 years ago.
	 * 
	 * <br/>precondition: the publication date is more than 50 years ago.
	 * <br/>postcondition: 1 interval facet has been returned as result.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void moreThan50YearsAgo()
	{
		String year = String.valueOf(handler.getCurrentYear() - 127);
		
		Collection<String> result = (Collection<String>) handler.decorate(notAvailable, year);
		
		assertEquals(1, result.size());
		
		assertFalse(result.contains(GetPublicationDateIntervalFacets.THIS_YEAR));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_2_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_5_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_10_YEARS));
		assertFalse(result.contains(GetPublicationDateIntervalFacets.LAST_50_YEARS));
		assertTrue(result.contains(GetPublicationDateIntervalFacets.MORE_THAN_50_YEARS_AGO));
	}		
	
	/**
	 * Tests the execution of the function when the publication date is not a number (null or alphanumeric).
	 * 
	 * <br/>precondition: the publication date is not a number
	 * <br/>postcondition: the function returns null.
	 */
	@Test
	public void invalidYear()
	{
		assertNull(handler.decorate(notAvailable, "ABCDEF192"));
		assertNull(handler.decorate(notAvailable, ""));
		assertNull(handler.decorate(null, notAvailable, (Object)null));
	}		
}
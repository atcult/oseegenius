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
package com.atc.osee.genius.indexer.biblio.browsing.filter.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for RemoveLeadingStopwords heading filter.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class RemoveLeadingStopwordsFilterTestCase 
{
	private RemoveLeadingStopwordsFilter cut;
	
	private static final String L = "l'";
	private static final String LA = "la ";
	private static final String DELL = "dell'";
	private static final String DELLA = "della ";
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		cut = new RemoveLeadingStopwordsFilter();
		cut.leadingStopwords = Arrays.asList(L, LA, DELLA, DELL);
	}
	
	/**
	 * Test the behaviour of the filter in case no changes are required.
	 * 
	 * <br/>
	 * <br/>precondition: the given heading doesn't start with a stopword.
	 * <br/>postcondition: the filter returns the unmodified heading.
	 */
	@Test
	public void unmodifiedHeading() 
	{
		String headingWithoutStopwords = "Storia delle storie del mondo";
		String filteredHeading = cut.doFilter(headingWithoutStopwords);
		
		assertEquals(headingWithoutStopwords, filteredHeading);
	}

	/**
	 * Test the behaviour of the filter in case the heading starts with a stopword.
	 * 
	 * <br/>
	 * <br/>precondition: the given heading starts with a stopword (elision stopword).
	 * <br/>postcondition: the filter returns the heading without the stopword.
	 */
	@Test
	public void leadingStopWordWithElision()
	{
		String truncatedHeading = "umanesimo";
		String heading = L + truncatedHeading;
		String filteredHeading = cut.doFilter(heading);
		assertEquals(truncatedHeading, filteredHeading);
		
		heading = DELL + truncatedHeading;
		filteredHeading = cut.doFilter(heading);
		assertEquals(truncatedHeading, filteredHeading);
	}
	
	/**
	 * Test the behaviour of the filter in case the heading starts with a stopword.
	 * 
	 * <br/>
	 * <br/>precondition: the given heading starts with a stopword (elision stopword).
	 * <br/>postcondition: the filter returns the heading without the stopword.
	 */
	@Test
	public void leadingStopWordWithoutElision()
	{
		String truncatedHeading = "storia delle storie del mondo";
		String heading = LA + truncatedHeading;
		String filteredHeading = cut.doFilter(heading);
		assertEquals(truncatedHeading, filteredHeading);
		
		heading = DELLA + truncatedHeading;
		filteredHeading = cut.doFilter(heading);
		assertEquals(truncatedHeading, filteredHeading);
	}

	/**
	 * Test the behaviour of the filter in case the heading starts with a dummy stopword.
	 * For example, if "la" is supposed to be a stopword and the heading is "lalalalal" (no spaces)
	 * the filter mustn't remove the "la" token.
	 * 
	 * <br/>
	 * <br/>precondition: the given heading starts with a dummy stopword.
	 * <br/>postcondition: the filter returns the unmodified heading .
	 */
	@Test
	public void leadingGhostStopWord()
	{
		String heading = "lalalalalala";
		
		String filteredHeading = cut.doFilter(heading);
		assertEquals(heading, filteredHeading);
	}	
}
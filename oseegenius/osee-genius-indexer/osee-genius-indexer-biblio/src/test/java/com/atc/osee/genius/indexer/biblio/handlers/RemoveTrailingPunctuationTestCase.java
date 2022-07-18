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

import org.junit.Before;
import org.junit.Test;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * RemoveTrailingPunctuation OBML tag handler test case.
 *  
 * @author agazzarini
 * @since 1.0
 */
public class RemoveTrailingPunctuationTestCase 
{
	private RemoveTrailingPunctuationTagHandler sut; 
	private TagHandler getValue;
	
//	private String aValue = "Summerland /";
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		sut = new RemoveTrailingPunctuationTagHandler();
		getValue = new GetValue();
	}
	
	/**
	 * Tests the OBML GetValue function with a filter enabled.
	 * 
	 * <br/>precondition: the incoming expression contains a tag mapping and a filter.
	 * <br/>postcondition: the subfield value that matches the filter is filtered out.
	 */
	@Test
	public void noPunctuation()
	{
		String aValue = "a value with no punctuation";
		
		String result = (String) sut.decorate("", aValue);
		assertEquals(aValue, result);		
	}	
	
	@Test
	public void oneCharacterToRemove()
	{
		String aValue = "a value with no punctuation";
		
		String result = (String) sut.decorate("", aValue + ", ");
		assertEquals(aValue, result);		
	}	
}
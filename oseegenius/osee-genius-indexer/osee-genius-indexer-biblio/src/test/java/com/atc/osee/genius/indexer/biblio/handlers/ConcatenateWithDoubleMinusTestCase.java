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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

/**
 * Test case for OBML concatenate with double minus function. 
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ConcatenateWithDoubleMinusTestCase 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield;
	
	private ConcatenateWithDoubleMinus cut;
		
	private String italia = "Italia";
	private String lazio = "Lazio";
	private String vignanello = "Vignanello";
	private String piemonte = "Piemonte";
	private String torino = "Torino";	
	private String viaRoma = "viaRoma";	
	private String doubleMinus = "--";
	
	private String sixHundredFifty = "650";
	private String emptySpace = " ";
	private String aTag = sixHundredFifty + "abcdefghilmnopqrstuvxyz";
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();

		datafield = factory.newDataField();
		datafield.setTag(sixHundredFifty);
		datafield.addSubfield(factory.newSubfield('a', italia));
		datafield.addSubfield(factory.newSubfield('b', italia));
		datafield.addSubfield(factory.newSubfield('c', italia));
		datafield.addSubfield(factory.newSubfield('d', italia));
		
		datafield.addSubfield(factory.newSubfield('v', lazio));
		datafield.addSubfield(factory.newSubfield('x', vignanello));

		cut = new ConcatenateWithDoubleMinus();
	}
	
	/**
	 * Positive test for this OBML function.
	 * 
	 * <br/>precondition: a given MARC record contains two requested subfields
	 * <br/>postcondition: the result includes subfields values separated by --
	 */
	@Test
	public void concatenateSubjects()
	{
		record.addVariableField(datafield);

		String expectedResult = 
				italia 
				+ emptySpace 
				+ italia 
				+ emptySpace 
				+ italia 
				+ emptySpace 
				+ italia 
				+ emptySpace 
				+ doubleMinus 
				+ emptySpace 
				+ lazio 
				+ emptySpace 
				+ doubleMinus 
				+ emptySpace 
				+ vignanello;
		Object result = cut.getValue(
				aTag, 
				record, 
				null, 
				null, 
				null);
		assertEquals(expectedResult, result);
	}
	
	/**
	 * Positive test for this OBML function when there's just one value to concatenate.
	 * 
	 * <br/>precondition: only one of the target subfield is valorized.
	 * <br/>postcondition: the result includes just the subfield value.
	 */
	@Test
	public void dummyConcatenationWithOneSingleSubject()
	{
		record.removeVariableField(datafield);
		
		datafield = factory.newDataField();
		datafield.setTag(sixHundredFifty);
		datafield.addSubfield(factory.newSubfield('a', italia));
		
		record.addVariableField(datafield);

		String expectedResult = italia;
		Object result = cut.getValue(
				aTag, 
				record, 
				null, 
				null, 
				null);
		
		assertEquals(expectedResult, result);
	}
	
	/**
	 * Positive test for this OBML function when there's just one value to concatenate.
	 * 
	 * <br/>precondition: only one of the target subfield is valorized.
	 * <br/>postcondition: the result includes just the subfield value.
	 */
	@Test
	public void concatenationWithMultipleSubjects()
	{
		record.addVariableField(datafield);
		
		datafield = factory.newDataField();
		datafield.setTag(sixHundredFifty);
		datafield.addSubfield(factory.newSubfield('a', italia));
		datafield.addSubfield(factory.newSubfield('v', piemonte));
		datafield.addSubfield(factory.newSubfield('x', torino));
		datafield.addSubfield(factory.newSubfield('x', viaRoma));
		record.addVariableField(datafield);

		Set<String> expectedResult = new LinkedHashSet<String>(Arrays.asList(
				italia + emptySpace +italia + emptySpace +italia + emptySpace +italia + emptySpace + doubleMinus + emptySpace + lazio + emptySpace + doubleMinus + emptySpace + vignanello,
				italia + emptySpace + doubleMinus + emptySpace + piemonte + emptySpace + doubleMinus + emptySpace + torino 
				+ emptySpace + doubleMinus + emptySpace + viaRoma));
		Object result = cut.getValue(
				aTag, 
				record, 
				null, 
				null, 
				null);
		
		assertEquals(expectedResult, result);
	}	
}
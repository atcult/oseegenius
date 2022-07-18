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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

/**
 * GetValue OBML tag handler test case.
 *  
 * @author agazzarini
 * @since 1.0
 */
public class TitleBrowseHeadingTestCase 
{
	private MarcFactory factory;
	private Record record;
	private TitleBrowseHeading sut; 
	
	private String aValueSkipPart = "LA ";
	private String aValueNoSkipPart = "DIVINA COMMEDIA";
	
	private String bValue = "B";
	private String cValue = "C";
	private String dValue = "D";
	private String eValue = "3";
	private String fValue = "F";
	private String gValue = "G";
	private String hValue = "H";
	private String iValue = "I";
	private String lValue = "L";
	private String mValue = "M";
	private String nValue = "N";
	private String oValue = "O";
	private String pValue = "P";
	private String qValue = "Q";
	private String rValue = "R";
	private String sValue = "S";
	
	private String tValueSkipPart = "IL ";
	private String tValueNoSkipPart = "MISTERO";
	
	private String uValue = "U";
	private String vValue = "V";
	private String zValue = "Z";
	
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		
		sut = new TitleBrowseHeading();
		
		Properties tags2SkipIndicator = new Properties();
		tags2SkipIndicator.setProperty("130","2");
		tags2SkipIndicator.setProperty("630","2");
		tags2SkipIndicator.setProperty("730","2");
		tags2SkipIndicator.setProperty("740","2");
		tags2SkipIndicator.setProperty("210","2");
		tags2SkipIndicator.setProperty("222","2");
		tags2SkipIndicator.setProperty("240","2");
		tags2SkipIndicator.setProperty("242","2");
		tags2SkipIndicator.setProperty("243","2");
		tags2SkipIndicator.setProperty("245","2");
		tags2SkipIndicator.setProperty("247","2");
		tags2SkipIndicator.setProperty("830","2");
		tags2SkipIndicator.setProperty("440","2");
		
		sut.tags2SkipIndicator = tags2SkipIndicator;
	}
	
	@Test
	public void test245()
	{
		record.addVariableField(createDataField("245", '3'));
		
		final Object result = sut.getValue("245", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
				aValueSkipPart + aValueNoSkipPart,
				bValue);
		
		String expectedValue = buildExpected(
				aValueNoSkipPart,
				bValue);		
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);
	}
	
	@Test
	public void test246()
	{
		record.addVariableField(createDataField("246", '3'));
		
		final Object result = sut.getValue("246", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
			aValueSkipPart + aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue,
			vValue,
			zValue);

		String expectedValue = buildExpected(
			aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue,
			vValue,
			zValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}
	
	@Test
	public void test130()
	{
		record.addVariableField(createDataField("130", '3'));
		
		final Object result = sut.getValue("130", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
			aValueSkipPart + aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue,
			vValue,
			zValue);

		String expectedValue = buildExpected(
			aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue,
			vValue,
			zValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}
	
	@Test
	public void test730()
	{
		record.addVariableField(createDataField("730", '3'));
		
		final Object result = sut.getValue("730", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
			aValueSkipPart + aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue,
			vValue,
			zValue);

		String expectedValue = buildExpected(
			aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue,
			vValue,
			zValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}	
	
	@Test
	public void test740()
	{
		record.addVariableField(createDataField("740", '3'));
		
		final Object result = sut.getValue("740", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
			aValueSkipPart + aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue,
			vValue,
			zValue);

		String expectedValue = buildExpected(
			aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue,
			vValue,
			zValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}		
	
	@Test
	public void test440()
	{
		record.addVariableField(createDataField("440", '3'));
		
		final Object result = sut.getValue("440", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
			aValueSkipPart + aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue);

		String expectedValue = buildExpected(
			aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}
	
	@Test
	public void test830()
	{
		record.addVariableField(createDataField("830", '3'));
		
		final Object result = sut.getValue("830", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
			aValueSkipPart + aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue);

		String expectedValue = buildExpected(
			aValueNoSkipPart,
			bValue,
			cValue,
			dValue,
			eValue,
			fValue,
			fValue,
			gValue,
			hValue,
			iValue,
			lValue,
			mValue,
			nValue,
			oValue,
			pValue,
			fValue,
			qValue,
			rValue,
			sValue,
			tValueSkipPart + tValueNoSkipPart,
			uValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}	
	
	@Test
	public void test800()
	{
		record.addVariableField(createDataField("800", '3'));
		
		final Object result = sut.getValue("800", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
			tValueSkipPart + tValueNoSkipPart,
			uValue);

		String expectedValue = buildExpected(
			tValueNoSkipPart,
			uValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}		
	
	@Test
	public void test810()
	{
		record.addVariableField(createDataField("810", '3'));
		
		final Object result = sut.getValue("810", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
			tValueSkipPart + tValueNoSkipPart,
			uValue);

		String expectedValue = buildExpected(
			tValueNoSkipPart,
			uValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}		

	@Test
	public void test811()
	{
		record.addVariableField(createDataField("811", '3'));
		
		final Object result = sut.getValue("811", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof String);
		final String labelAndValue = (String) result;
		
		String expectedLabel = buildExpected(
			tValueSkipPart + tValueNoSkipPart,
			uValue);

		String expectedValue = buildExpected(
			tValueNoSkipPart,
			uValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}		
	
	private DataField createDataField(final String tag, final char howManySkipChars)
	{
		DataField datafield = factory.newDataField(tag, '1', howManySkipChars);
		datafield.addSubfield(factory.newSubfield('a', aValueSkipPart + aValueNoSkipPart));
		datafield.addSubfield(factory.newSubfield('b', bValue));
		datafield.addSubfield(factory.newSubfield('c', cValue));
		datafield.addSubfield(factory.newSubfield('d', dValue));
		datafield.addSubfield(factory.newSubfield('e', eValue));
		datafield.addSubfield(factory.newSubfield('f', fValue));
		datafield.addSubfield(factory.newSubfield('f', fValue));
		datafield.addSubfield(factory.newSubfield('g', gValue));
		datafield.addSubfield(factory.newSubfield('h', hValue));
		datafield.addSubfield(factory.newSubfield('i', iValue));
		datafield.addSubfield(factory.newSubfield('l', lValue));
		datafield.addSubfield(factory.newSubfield('m', mValue));
		datafield.addSubfield(factory.newSubfield('n', nValue));
		datafield.addSubfield(factory.newSubfield('o', oValue));
		datafield.addSubfield(factory.newSubfield('p', pValue));
		datafield.addSubfield(factory.newSubfield('f', fValue));
		datafield.addSubfield(factory.newSubfield('q', qValue));
		datafield.addSubfield(factory.newSubfield('r', rValue));
		datafield.addSubfield(factory.newSubfield('s', sValue));
		datafield.addSubfield(factory.newSubfield('t', tValueSkipPart + tValueNoSkipPart));
		datafield.addSubfield(factory.newSubfield('u', uValue));
		datafield.addSubfield(factory.newSubfield('v', vValue));
		datafield.addSubfield(factory.newSubfield('z', zValue));
		
		return datafield;
	}
	
	private String buildExpected(String ...strings ) 
	{
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < strings.length; i++)
		{
			if (i > 0)
			{
				builder.append(" ");
			}
			builder.append(strings[i]);
		}
		
		return builder.toString();
	}
}
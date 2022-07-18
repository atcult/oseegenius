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

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

/**
 * N/T Title OBML tag handler test case.
 *  
 * @author agazzarini
 * @since 1.0
 */
@Ignore
public class NTTitleBrowseHeadingTestCase 
{
	private MarcFactory factory;
	private Record record;
	private NTTitleBrowseHeading sut; 
	
	private String aValueSkipPart = "DANTE ";
	private String aValueNoSkipPart = "ALIGHIERI";
	
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
	
	private String tValueSkipPart = "LA ";
	private String tValueNoSkipPart = "DIVINA COMMEDIA";
	
	private String uValue = "U";
	private String vValue = "V";
	private String zValue = "Z";
	
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		
		sut = new NTTitleBrowseHeading();
	}

	@Test
	public void test700()
	{
		record.addVariableField(createDataField("700", '0'));
		record.addVariableField(createDataField("700", '0'));
		
		final Object result = sut.getValue("700", record, null, null, null);
		
		assertNotNull(result);
		
		assertTrue(result.getClass().getName(), result instanceof Set<?>);
		
		@SuppressWarnings("unchecked")
		Set<String> headings = (Set<String>) result;
		assertEquals(2, headings.size());
		
		for (String heading : headings)
		{
			final String labelAndValue = (String) heading;
			
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
				tValueNoSkipPart,
				uValue);			
			
			final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
			
			assertEquals(expected, labelAndValue);		
		}
	}		
	
	
	@Test
	public void test800()
	{
		record.addVariableField(createDataField("800", '0'));
		
		final Object result = sut.getValue("800", record, null, null, null);
		
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
			tValueNoSkipPart,
			uValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}		
	
	@Test
	public void test810()
	{
		record.addVariableField(createDataField("811", '0'));
		
		final Object result = sut.getValue("811", record, null, null, null);
		
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
			tValueNoSkipPart,
			uValue);			
		
		final String expected = expectedLabel + TitleBrowseHeading.SEPARATOR + expectedValue;
		
		assertEquals(expected, labelAndValue);		
	}			
	
	@Test
	public void test811()
	{
		record.addVariableField(createDataField("811", '0'));
		
		final Object result = sut.getValue("811", record, null, null, null);
		
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
	
	private String buildExpected(final String ...strings ) 
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
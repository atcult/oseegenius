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
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

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
public class GetCompoundValueTestCase 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield;
	private GetCompoundValue sut; 
	
	private String twoHundredSixty = "260";
	private char a = 'a';
	private char b = 'b';
	private char c = 'c';
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		
		sut = new GetCompoundValue();
	}
	
	/**
	 * Tests the OBML GetValue function with a filter enabled.
	 * 
	 * <br/>precondition: the incoming expression contains a tag mapping and a filter.
	 * <br/>postcondition: the subfield value that matches the filter is filtered out.
	 */
	@Test
	public void onePublisher()
	{
		datafield = factory.newDataField(twoHundredSixty, '1', '0');
		datafield.addSubfield(factory.newSubfield(a, "Milano"));
		datafield.addSubfield(factory.newSubfield(b, "Mondadori"));
		datafield.addSubfield(factory.newSubfield(c, "2011"));
		
		record.addVariableField(datafield);

		Object result = sut.getValue("260a,260ab", record, null, null, null);

		assertTrue(result instanceof String);
		assertEquals(
				"Milano" + GetCompoundValue.SEPARATOR + "Milano Mondadori",
				result);
	}	
	
	@Test
	public void onePublisherWithInvertedLabelAndValue()
	{
		datafield = factory.newDataField(twoHundredSixty, '1', '0');
		datafield.addSubfield(factory.newSubfield(a, "Milano"));
		datafield.addSubfield(factory.newSubfield(b, "Mondadori"));
//		datafield.addSubfield(factory.newSubfield(c, "2011"));
		
		record.addVariableField(datafield);

		Object result = sut.getValue("260ab,260a", record, null, null, null);

		assertTrue(result instanceof String);
		assertEquals(
				"Milano Mondadori" + GetCompoundValue.SEPARATOR + "Milano",
				result);
	}	

	/**
	 * Tests the OBML GetValue function with a filter enabled.
	 * 
	 * <br/>precondition: the incoming expression contains a tag mapping and a filter.
	 * <br/>postcondition: the subfield value that matches the filter is filtered out.
	 */
	@Test
	public void threePublisher()
	{
		datafield = factory.newDataField(twoHundredSixty, '1', '0');
		datafield.addSubfield(factory.newSubfield(a, "Milano"));
		datafield.addSubfield(factory.newSubfield(b, "Mondadori"));
		datafield.addSubfield(factory.newSubfield(c, "2011"));

		record.addVariableField(datafield);

		datafield = factory.newDataField(twoHundredSixty, '1', '0');
		datafield.addSubfield(factory.newSubfield(a, "Roma"));
		datafield.addSubfield(factory.newSubfield(b, "Bulzoni"));
		datafield.addSubfield(factory.newSubfield(c, "2009"));

		record.addVariableField(datafield);

		datafield = factory.newDataField(twoHundredSixty, '1', '0');
		datafield.addSubfield(factory.newSubfield(a, "Firenze"));
		datafield.addSubfield(factory.newSubfield(b, "Cadmo"));
		datafield.addSubfield(factory.newSubfield(c, "2008"));
		
		record.addVariableField(datafield);

		Object result = sut.getValue("260a,260ab", record, null, null, null);

		assertTrue(result instanceof Collection<?>);
		
		@SuppressWarnings("unchecked")
		List<String> results = (List<String>) result;
		
		assertEquals(3, results.size());
		
		assertEquals(
				"Milano" + GetCompoundValue.SEPARATOR + "Milano Mondadori",
				results.get(0));
		
		assertEquals(
				"Roma" + GetCompoundValue.SEPARATOR + "Roma Bulzoni",
				results.get(1));

		assertEquals(
				"Firenze" + GetCompoundValue.SEPARATOR + "Firenze Cadmo",
				results.get(2));
	}	

	/**
	 * Tests the OBML GetValue function with a filter enabled.
	 * 
	 * <br/>precondition: the incoming expression contains a tag mapping and a filter.
	 * <br/>postcondition: the subfield value that matches the filter is filtered out.
	 */
	@Test
	public void threePublisherWithTwoValid()
	{
		datafield = factory.newDataField(twoHundredSixty, '1', '0');
		datafield.addSubfield(factory.newSubfield(a, "Milano"));
		datafield.addSubfield(factory.newSubfield(b, "Mondadori"));
		datafield.addSubfield(factory.newSubfield(c, "2011"));

		record.addVariableField(datafield);

		datafield = factory.newDataField(twoHundredSixty, '1', '0');
		datafield.addSubfield(factory.newSubfield(a, "Roma"));

		record.addVariableField(datafield);

		datafield = factory.newDataField(twoHundredSixty, '1', '0');
		datafield.addSubfield(factory.newSubfield(a, "Firenze"));
		datafield.addSubfield(factory.newSubfield(b, "Cadmo"));
		datafield.addSubfield(factory.newSubfield(c, "2008"));
		
		record.addVariableField(datafield);

		Object result = sut.getValue("260a,260ab", record, null, null, null);

		assertTrue(result instanceof Collection<?>);
		
		@SuppressWarnings("unchecked")
		List<String> results = (List<String>) result;
		System.out.println(results);
//		assertEquals(3, results.size());
//		
//		assertEquals(
//				"Milano" + GetCompoundValue.SEPARATOR + "Milano Mondadori",
//				results.get(0));
//		
//		assertEquals(
//				"Roma" + GetCompoundValue.SEPARATOR + "Roma Bulzoni",
//				results.get(1));
//
//		assertEquals(
//				"Firenze" + GetCompoundValue.SEPARATOR + "Firenze Cadmo",
//				results.get(2));
	}	

	
//	/**
//	 * Tests the OBML GetValue function in case a record contains empty subfields.
//	 * 
//	 * <br/>precondition: the incoming expression matches agains an empty subfield.
//	 * <br/>postcondition: the empty value is not considered.
//	 */
//	@Test
//	public void emptySubfields()
//	{
//		String cValue = "ABC";
//		datafield.addSubfield(factory.newSubfield(b, aSpace.trim()));
//		datafield.addSubfield(factory.newSubfield(b, aSpace));
//		datafield.addSubfield(factory.newSubfield(c, cValue));
//		
//		record.addVariableField(datafield);
//		
//		String result = (String) sut.getValue(twoHundredFortyFiveAbc, record, null, null, null);
//		assertEquals(aValue + aSpace + cValue, result);
//	}	
//	
//	/**
//	 * Tests the OBML GetValue function in case a record doesn't contain the target subfields.
//	 * 
//	 * <br/>precondition: the incoming expression matches a non existent subfield.
//	 * <br/>postcondition: the expression is ignored.
//	 */
//	@Test
//	public void noSubfields()
//	{
//		datafield.addSubfield(factory.newSubfield('c', michaelChabon));
//		
//		record.addVariableField(datafield);
//		
//		String result = (String) sut.getValue(twoHundredFortyFive + "xyz", record, null, null, null);
//		assertNull(result);
//	}
//	
//	/**
//	 * Tests the OBML GetValue function in case the expression matches a monovalued subfields.
//	 * 
//	 * <br/>precondition: the incoming expression matches a monovalued subfield.
//	 * <br/>postcondition: the value of the subfield is included in result.
//	 */
//	@Test
//	public void monovaluedSubfields() 
//	{
//		String cValue = michaelChabon;
//		datafield.addSubfield(factory.newSubfield('c', cValue));
//		
//		record.addVariableField(datafield);
//		String result = (String) sut.getValue(twoHundredFortyFiveAbc, record, null, null, null);
//		
//		assertEquals(aValue + aSpace + cValue, result);
//	} 
//
//	/**
//	 * Tests the OBML GetValue function in case the expression matches a multivalued subfield.
//	 * 
//	 * <br/>precondition: the incoming expression matches a multivalued subfield.
//	 * <br/>postcondition: all the values of the subfield is included in result.
//	 */
//	@Test
//	public void multivaluedSubfields() 
//	{
//		datafield.addSubfield(factory.newSubfield('b', multivalue1));
//		datafield.addSubfield(factory.newSubfield('b', multivalue2));
//		datafield.addSubfield(factory.newSubfield('c', michaelChabon));
//		
//		record.addVariableField(datafield);
//
//		String result = (String) sut.getValue(twoHundredFortyFiveAbc, record, null, null, null);
//		assertEquals(aValue + aSpace + multivalue1 + aSpace + multivalue2 + aSpace + michaelChabon, result);
//	} 
}
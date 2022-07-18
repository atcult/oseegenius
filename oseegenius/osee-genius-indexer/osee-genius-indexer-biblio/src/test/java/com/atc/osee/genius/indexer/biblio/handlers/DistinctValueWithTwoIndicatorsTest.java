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
import static org.junit.Assert.assertNull;

import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;

import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;


/**
 * GetValue OBML tag handler test case.
 *  
 * @author ztajoli
 * @since 1.2
 */
public class DistinctValueWithTwoIndicatorsTest 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield;
	private DistinctValueWithTwoIndicators sut; 
	
	private String aValue = "Summerland /";
	private String zValue = "BVE0053170";
	private String aSpace = " ";
	private String michaelChabon = "Michael Chabon.";
	private String multivalue1 =  "Multivalued value #1/";
	private String multivalue2 =  "Multivalued value #2/";
	private String twoHundredFortyFive = "245";
	private char a = 'a';
	private char b = 'b';
	private char c = 'c';
	private String twoHundredFortyFiveAbc = twoHundredFortyFive + a + b + c;
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		datafield = factory.newDataField(twoHundredFortyFive, '1', '0');
		datafield.addSubfield(factory.newSubfield(a, aValue));
		
		sut = new DistinctValueWithTwoIndicators();
	}
	
	
	@Test
	public void withZero() {
		datafield.addSubfield(factory.newSubfield('0', zValue));
		record.addVariableField(datafield);
		String result = (String) sut.getValue("2450,*,*", record, null, null, null);
		assertEquals(zValue, result);
	}
		
	/**
	 * Tests the OBML GetValue function in case a record contains empty subfields.
	 * 
	 * <br/>precondition: the incoming expression matches agains an empty subfield.
	 * <br/>postcondition: the empty value is not considered.
	 */
	@Test
	public void emptySubfields()
	{
		String cValue = "ABC";
		LinkedHashSet<String> chk_result = new LinkedHashSet<String>();
		chk_result.add("Summerland /");
		chk_result.add(cValue);
		
		
		datafield.addSubfield(factory.newSubfield(b, aSpace.trim()));
		datafield.addSubfield(factory.newSubfield(b, aSpace));
		datafield.addSubfield(factory.newSubfield(c, cValue));
		record.addVariableField(datafield);
		
		Object result = sut.getValue(twoHundredFortyFiveAbc+",1,0", record, null, null, null);
		assertNotNull(result);
		assertEquals(chk_result , result);
	}	
	
	/**
	 * Tests the OBML GetValue function in case a record doesn't contain the target subfields.
	 * 
	 * <br/>precondition: the incoming expression matches a non existent subfield.
	 * <br/>postcondition: the expression is ignored.
	 */
	@Test
	public void noSubfields()
	{
		datafield.addSubfield(factory.newSubfield('c', michaelChabon));
		record.addVariableField(datafield);
		
		String result = (String) sut.getValue(twoHundredFortyFive + "xyz,1,0", record, null, null, null);
		assertNull(result);
	}
	
	/**
	 * Tests the OBML GetValue function in case the expression matches a monovalued subfields.
	 * 
	 * <br/>precondition: the incoming expression matches a monovalued subfield.
	 * <br/>postcondition: the value of the subfield is included in result.
	 */
	@Test
	public void monovaluedSubfields() 
	{
		String cValue = michaelChabon;
		datafield.addSubfield(factory.newSubfield('c', cValue));
		record.addVariableField(datafield);
		
		String result = (String) sut.getValue(twoHundredFortyFive+"c,1,0", record, null, null, null);
		assertNotNull(result);
		assertEquals(cValue, result);
		
		String result2 = (String) sut.getValue(twoHundredFortyFive+"c,*,0", record, null, null, null);
		assertNotNull(result2);
		assertEquals(cValue, result2);
		
		String result3 = (String) sut.getValue(twoHundredFortyFive+"c,1,#", record, null, null, null);
		assertNull(result3);
	} 

	/**
	 * Tests the OBML GetValue function in case the expression matches a multivalued subfield.
	 * 
	 * <br/>precondition: the incoming expression matches a multivalued subfield.
	 * <br/>postcondition: all the values of the subfield is included in result.
	 */
	@Test
	public void multivaluedSubfields() 
	{
		LinkedHashSet<String> chk_result = new LinkedHashSet<String>();
		chk_result.add(aValue);
		chk_result.add(multivalue1);
		chk_result.add(multivalue2 );
		chk_result.add(michaelChabon );
		
		datafield.addSubfield(factory.newSubfield('b', multivalue1));
		datafield.addSubfield(factory.newSubfield('b', multivalue2));
		datafield.addSubfield(factory.newSubfield('c', michaelChabon));
		record.addVariableField(datafield);

		Object result = sut.getValue(twoHundredFortyFive+"abc,1,0", record, null, null, null);
		assertNotNull(result);
		assertEquals(chk_result , result);
	} 
	
	/**
	 * Tests the OBML GetValue function in case the expression matches a multivalued  2° indicator.
	 * 
	 * <br/>precondition: the incoming expression matches a multivalued subfield.
	 * <br/>precondition: the incoming expression matches a in a list of multivalued as 2° indicator
	 * <br/>postcondition: all the values of the subfield is included in result.
	 */
	@Test
	public void multivaluedSecondIndicator() 
	{
		LinkedHashSet<String> chk_result = new LinkedHashSet<String>();
		chk_result.add(multivalue1);
		chk_result.add(multivalue2 );
		
		datafield.addSubfield(factory.newSubfield('b', multivalue1));
		datafield.addSubfield(factory.newSubfield('b', multivalue2));
		datafield.addSubfield(factory.newSubfield('c', michaelChabon));
		record.addVariableField(datafield);

		Object result = sut.getValue(twoHundredFortyFive+"b,1,320", record, null, null, null);
		assertNotNull(result);
		assertEquals(chk_result , result);
		
		Object result2 = sut.getValue(twoHundredFortyFive+"b,*,320", record, null, null, null);
		assertNotNull(result2);
		assertEquals(chk_result , result2);
		
		
	}
	
	/**
	 * Tests the OBML GetValue function in case the expression matches a multivalued  1° indicator
	 * and a * as second.
	 * 
	 * <br/>precondition: the incoming expression matches a multivalued subfield.
	 * <br/>precondition: the incoming expression matches a in a list of multivalued as 1° indicator
	 * <br/>postcondition: all the values of the subfield is included in result.
	 */
	@Test
	public void multivaluedFirstIndicator() 
	{
		LinkedHashSet<String> chk_result = new LinkedHashSet<String>();
		chk_result.add(multivalue1);
		chk_result.add(multivalue2 );
		
		datafield.addSubfield(factory.newSubfield('b', multivalue1));
		datafield.addSubfield(factory.newSubfield('b', multivalue2));
		datafield.addSubfield(factory.newSubfield('c', michaelChabon));
		record.addVariableField(datafield);

		Object result = sut.getValue(twoHundredFortyFive+"b,#12,*", record, null, null, null);
		assertNotNull(result);
		assertEquals(chk_result , result);
		
		Object result2 = sut.getValue(twoHundredFortyFive+"b,#42,*", record, null, null, null);
		assertNull(result2);
		
		Object result3 = sut.getValue(twoHundredFortyFive+"b,#42,0", record, null, null, null);
		assertNull(result3);
		
	}

}
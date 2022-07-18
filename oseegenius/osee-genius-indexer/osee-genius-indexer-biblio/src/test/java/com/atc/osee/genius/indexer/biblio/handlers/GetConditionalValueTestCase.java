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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.xml.sax.InputSource;

/**
 * GetConditionalValue OBML tag handler test case.
 *  
 * @author aguercio
 * @since 1.0
 */
public class GetConditionalValueTestCase 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield;
	private GetConditionalValue sut; 
	private String tagName = "370";
	private String valueA = "value $a";
	private String valueB = "value $b";
	private String valueV = "value $v";
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		datafield = factory.newDataField(tagName, '1', '0');
		datafield.addSubfield(factory.newSubfield('v', valueV));
		datafield.addSubfield(factory.newSubfield('b', valueB));
		
		sut = new GetConditionalValue();
	}
	
	@Test
	public void testMandatoryNotPresent()
	{		
		datafield.addSubfield(factory.newSubfield('a', valueA));
		record.addVariableField(datafield);
		String result = (String) sut.getValue(tagName + "f;fvb", record, null, null, null);		
		assertNull(result);
		
	}
	
	@Test
	public void testMandatoryPresent()
	{		
		datafield.addSubfield(factory.newSubfield('a', valueA));
		record.addVariableField(datafield);
		String result = (String) sut.getValue(tagName + "a;avb", record, null, null, null);
		System.out.println(result);
		assertEquals(valueA + " [Fonte dell'informazione] " + valueV + " " + valueB , result);		
		
	}
	
	@Test
	public void testMultipleMandatoryPresent()
	{		
		datafield.addSubfield(factory.newSubfield('a', valueA));
		record.addVariableField(datafield);
		String result = (String) sut.getValue(tagName + "vb;avb", record, null, null, null);
		System.out.println(result);
		assertEquals(valueA + " [Fonte dell'informazione] " + valueV + " " + valueB , result);		
		
	}
	
	
		
}
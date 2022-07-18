/*
 	Copyright (c) 2010-2020 @Cult s.r.l. All rights reserved.
	
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
 * DistinctValue OBML tag handler test case.
 *  
 * @author ztajoli
 * @since 1.2
 */
public class DistinctValueTest 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield264_21;
	private DistinctValue sut; 
	
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		datafield264_21 = factory.newDataField("264", '2', '1');
		
		sut = new DistinctValue();
	}

	/**
	 * Tests the OBML function with 264.
	 * 
	 * <br/>precondition: the record contains 264, 1Ind=2, 2Ind=1.
	 * <br/>precondition: filter "264b,>21"
	 * <br/>postcondition: the returned result is a string with publisher, 'ACADEMIC PRESS FRIBURG ;'
	 */
	@Test
	public void filterIndToPass()
	{
		
		record.addVariableField(datafield264_21);
		
		datafield264_21.addSubfield(factory.newSubfield('a', "FREIBURG :"));
		datafield264_21.addSubfield(factory.newSubfield('b', "ACADEMIC PRESS FRIBURG ;"));
		datafield264_21.addSubfield(factory.newSubfield('c', "2002"));
		
		String result = (String) sut.getValue("264b,>21", record, null, null, null);

		assertNotNull(result);
		assertEquals("ACADEMIC PRESS FRIBURG ;", result);
	}	
	
	/**
	 * Tests the OBML function with 264.
	 * 
	 * <br/>precondition: the record contains 264, 1Ind=2, 2Ind=1.
	 * <br/>precondition: filter "264b,>#1"
	 * <br/>postcondition: the returned result is null
	 */
	@Test
	public void filterIndNotToPass()
	{
		
		record.addVariableField(datafield264_21);
		
		datafield264_21.addSubfield(factory.newSubfield('a', "FREIBURG :"));
		datafield264_21.addSubfield(factory.newSubfield('b', "ACADEMIC PRESS FRIBURG ;"));
		datafield264_21.addSubfield(factory.newSubfield('c', "2002"));
		
		String result = (String) sut.getValue("264b,>#1", record, null, null, null);

		assertNull(result);

	}
	
	
	/**
	 * Tests the OBML function with 264.
	 * 
	 * <br/>precondition: the record contains 264 with two $b
	 * <br/>precondition: filter "264b"
	 * <br/>postcondition: the returned result is LinkedHashSet<String> with the two publisher names
	 */
	@Test
	public void filter2Subs()
	{
		LinkedHashSet<String> chk_result = new LinkedHashSet<String>();
		chk_result.add("ACADEMIC PRESS FRIBURG ;");
		chk_result.add("OLSCHKY ;");
		
		record.addVariableField(datafield264_21);
		datafield264_21.addSubfield(factory.newSubfield('a', "FREIBURG :"));
		datafield264_21.addSubfield(factory.newSubfield('b', "ACADEMIC PRESS FRIBURG ;"));
		datafield264_21.addSubfield(factory.newSubfield('a', "BERLIN :"));
		datafield264_21.addSubfield(factory.newSubfield('b', "OLSCHKY ;"));
		datafield264_21.addSubfield(factory.newSubfield('c', "2002"));
		
		Object result = sut.getValue("264b", record, null, null, null);
		
		assertNotNull(result);
		assertEquals(chk_result , result);

	}
	
	
	

}
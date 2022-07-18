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

import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

/**
 * GetPublishers OBML tag handler test case.
 *  
 * @author agazzarini
 * @author ztajoli
 * @since 1.2
 */
public class GetPublishersTestCase 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield;
	private DataField datafield264;
	private GetPublishers sut; 
	
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
		datafield = factory.newDataField(twoHundredSixty, '1', '0');
		datafield264 = factory.newDataField("264", '1', '0');
		
		sut = new GetPublishers();
	}

	/**
	 * Tests the OBML function with one publisher.
	 * That means this function will not break any heading.
	 * 
	 * <br/>precondition: the record contains 1 publishers .
	 * <br/>postcondition: the returned result is a string containing the publisher.
	 */
	@Test
	public void justOnePublisher()
	{
		record.addVariableField(datafield);
		
		datafield.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG ;"));
		datafield.addSubfield(factory.newSubfield(c, "2011"));
		
		String result = (String) sut.getValue("260abc", record, null, null, null);

		assertNotNull(result);
		
		assertEquals("FREIBURG : " +  "ACADEMIC PRESS FRIBURG ;" + " 2011", result);
	}	
	
	/**
	 * Tests the OBML GetValue function with two publishers.
	 * Rif. Bz #1105 (PUG).
	 * 
	 * <br/>precondition: the record contains 2 publishers on the same tag.
	 * <br/>postcondition: the returned set contains 2 publishers..
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void twoPublishers()
	{
		record.addVariableField(datafield);
		
		datafield.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG ;"));
		datafield.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield.addSubfield(factory.newSubfield(a, "WIEN :"));
		datafield.addSubfield(factory.newSubfield(b, "HERDER"));
		
		Set<String> result = (Set<String>) sut.getValue("260ab", record, null, null, null);

		assertEquals(2, result.size());
		
		Iterator<String> iterator = result.iterator();
		assertEquals("FREIBURG : " +  "ACADEMIC PRESS FRIBURG ;", iterator.next());
		assertEquals("FREIBURG : " +  "WIEN :" + " HERDER", iterator.next());
	}
	
	/**
	 * Tests the OBML GetValue function with two publishers.
	 * Rif. Bz #1105 (PUG).
	 * 
	 * <br/>precondition: the record contains 2 publishers on the same tag.
	 * <br/>postcondition: the returned set contains 2 publishers..
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void threeMixedPublishers()
	{
		record.addVariableField(datafield);
		
		datafield.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG ;"));
		datafield.addSubfield(factory.newSubfield(c, "2011"));
		datafield.addSubfield(factory.newSubfield(a, "FREIBURG1 :"));
		datafield.addSubfield(factory.newSubfield(a, "WIEN1 :"));
		datafield.addSubfield(factory.newSubfield(a, "WIEN2 :"));
		datafield.addSubfield(factory.newSubfield(b, "HERDER"));
		datafield.addSubfield(factory.newSubfield(a, "FREIBURG2 :"));
		datafield.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG2 ;"));
		datafield.addSubfield(factory.newSubfield(c, "2011 -"));
		datafield.addSubfield(factory.newSubfield(c, "2014"));
		
		Set<String> result = (Set<String>) sut.getValue("260abc", record, null, null, null);

		assertEquals(3, result.size());
		
		Iterator<String> iterator = result.iterator();
		assertEquals("FREIBURG : " +  "ACADEMIC PRESS FRIBURG ;" + " 2011", iterator.next());
		assertEquals("FREIBURG1 : " +  "WIEN1 :" +  " WIEN2 :" + " HERDER", iterator.next());
		assertEquals("FREIBURG2 : " +  "ACADEMIC PRESS FRIBURG2 ;" + " 2011 -" + " 2014", iterator.next());
	}
	
	/**
	 * Tests the OBML GetValue function with two publishers on 264.
	 * Rif. Bz #7158 (Teologica).
	 * 
	 * <br/>precondition: the record contains 2 publishers on the same tag.
	 * <br/>postcondition: the returned set contains 2 publishers..
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void twoPublishers264()
	{
		record.addVariableField(datafield264);
		
		datafield264.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield264.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG ;"));
		datafield264.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield264.addSubfield(factory.newSubfield(a, "WIEN :"));
		datafield264.addSubfield(factory.newSubfield(b, "HERDER"));
		
		Set<String> result = (Set<String>) sut.getValue("264ab", record, null, null, null);

		assertEquals(2, result.size());
		
		Iterator<String> iterator = result.iterator();
		assertEquals("FREIBURG : " +  "ACADEMIC PRESS FRIBURG ;", iterator.next());
		assertEquals("FREIBURG : " +  "WIEN :" + " HERDER", iterator.next());
	}
	
	/**
	 * Tests the OBML GetValue function with two publishers on 264.
	 * Rif. Bz #7158 (Teologica).
	 * 
	 * <br/>precondition: the record contains 2 publishers on the same tag.
	 * <br/>postcondition: the returned set contains 2 publishers..
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void threeMixedPublishers264()
	{
		record.addVariableField(datafield264);
		
		datafield264.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield264.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG ;"));
		datafield264.addSubfield(factory.newSubfield(c, "2011"));
		datafield264.addSubfield(factory.newSubfield(a, "FREIBURG1 :"));
		datafield264.addSubfield(factory.newSubfield(a, "WIEN1 :"));
		datafield264.addSubfield(factory.newSubfield(a, "WIEN2 :"));
		datafield264.addSubfield(factory.newSubfield(b, "HERDER"));
		datafield264.addSubfield(factory.newSubfield(a, "FREIBURG2 :"));
		datafield264.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG2 ;"));
		datafield264.addSubfield(factory.newSubfield(c, "2011 -"));
		datafield264.addSubfield(factory.newSubfield(c, "2014"));
		
		Set<String> result = (Set<String>) sut.getValue("264abc", record, null, null, null);

		assertEquals(3, result.size());
		
		Iterator<String> iterator = result.iterator();
		assertEquals("FREIBURG : " +  "ACADEMIC PRESS FRIBURG ;" + " 2011", iterator.next());
		assertEquals("FREIBURG1 : " +  "WIEN1 :" +  " WIEN2 :" + " HERDER", iterator.next());
		assertEquals("FREIBURG2 : " +  "ACADEMIC PRESS FRIBURG2 ;" + " 2011 -" + " 2014", iterator.next());
	}
	
}
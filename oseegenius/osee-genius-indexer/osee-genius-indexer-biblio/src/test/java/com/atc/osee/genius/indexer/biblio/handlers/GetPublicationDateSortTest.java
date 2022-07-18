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


import org.junit.Before;
import org.junit.Test;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

/**
 * GetPublicationDateSort OBML tag handler test case.
 *  
 * @author ztajoli
 * @since 1.2
 */
public class GetPublicationDateSortTest 
{
	private MarcFactory factory;
	private Record record;
	private ControlField f008_2099;
	private ControlField f008_2002;
	private DataField datafield;
	private DataField datafield264;
	private GetPublicationDateSort sut; 
	
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
		f008_2099 = factory.newControlField("008", "020805s2099####at############000#0#eng#d");
		f008_2002 = factory.newControlField("008", "020805s2002####at############000#0#eng#d");
		datafield = factory.newDataField("260", '1', '0');
		datafield264 = factory.newDataField("264", '1', '0');
		
		sut = new GetPublicationDateSort();
	}

	/**
	 * Tests the OBML function with 008 and 260.
	 * 
	 * <br/>precondition: the record contains 008 and 1 260 $abc.
	 * <br/>postcondition: the returned result is a string the date, '2002'
	 */
	@Test
	public void dateIn008And260()
	{
		record.addVariableField(f008_2002);
		record.addVariableField(datafield);
		
		datafield.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG ;"));
		datafield.addSubfield(factory.newSubfield(c, "2002"));
		
		String result = (String) sut.getValue("260abc", record, null, null, null);

		assertNotNull(result);
		
		assertEquals("2002", result);
	}	
	
	/**
	 * Tests the OBML function with 260 and 264 only
	 * 
	 * <br/>precondition: the record contains only 260 $abc and 264 $abc.
	 * <br/>postcondition: the returned result is a NULL string'
	 */
	@Test
	public void dateWithout008()
	{
		
		record.addVariableField(datafield);
		
		datafield.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG ;"));
		datafield.addSubfield(factory.newSubfield(c, "2002"));
		
		datafield264.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield264.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG ;"));
		datafield264.addSubfield(factory.newSubfield(c, "2002"));
		
		
		String result = (String) sut.getValue("260abc:264abc", record, null, null, null);

		assertNull(result);
	}
	
	
	/**
	 * Tests the OBML function with 264 and 008 too wide in the future (2099)
	 * 
	 * <br/>precondition: the record contains 008 and 264 $abc.
	 * <br/>postcondition: the returned result is a NULL string'
	 */
	@Test
	public void date008And264()
	{
		record.addVariableField(f008_2099);
		record.addVariableField(datafield264);
		
		datafield264.addSubfield(factory.newSubfield(a, "FREIBURG :"));
		datafield264.addSubfield(factory.newSubfield(b, "ACADEMIC PRESS FRIBURG ;"));
		datafield264.addSubfield(factory.newSubfield(c, "2002"));
		
		
		String result = (String) sut.getValue("264abc", record, null, null, null);

		assertNull(result);
	}

}
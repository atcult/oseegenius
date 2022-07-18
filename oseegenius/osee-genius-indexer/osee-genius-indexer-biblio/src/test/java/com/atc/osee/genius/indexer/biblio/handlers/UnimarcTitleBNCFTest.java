package com.atc.osee.genius.indexer.biblio.handlers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.LeaderImpl;


public class UnimarcTitleBNCFTest {

	private String nameField = "200";
	private MarcFactory factory;
	private Record record;
	private DataField datafield2;
	private UnimarcTitleBNCF sut; 
	
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		LeaderImpl leaderImpl = new LeaderImpl("00977nam2a22002773i 450 ");
		record.setLeader(leaderImpl);
		
		datafield2 = factory.newDataField("461", ' ', ' ');
		datafield2.addSubfield(factory.newSubfield('t', "Tragedie"));
		record.addVariableField(datafield2);

		sut = new UnimarcTitleBNCF();
	}
	
	
	
	@Test
	public void testCheckShort() {
		
		DataField datafield = factory.newDataField(nameField, ' ', ' ');
		datafield.addSubfield(factory.newSubfield('a', "a1"));
		record.addVariableField(datafield);
		Object result = sut.getValue(null, record , null, null, null);
		
		String expectedResult = new String();
		expectedResult = "[Tragedie]. a1";
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testCheckLong() {
		DataField datafield = factory.newDataField(nameField, ' ', ' ');
		datafield.addSubfield(factory.newSubfield('a', "Bootstrap"));
		record.addVariableField(datafield);
		Object result = sut.getValue(null, record , null, null, null);
		
		String expectedResult = new String();
		expectedResult = "Bootstrap";
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testCheckLimit() {
		DataField datafield = factory.newDataField(nameField, ' ', ' ');
		datafield.addSubfield(factory.newSubfield('a', "<<Il >>lav"));
		record.addVariableField(datafield);
		Object result = sut.getValue(null, record , null, null, null);
		
		String expectedResult = new String();
		expectedResult = "[Tragedie]. <<Il >>lav";
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testCheckOutside() {
		DataField datafield = factory.newDataField(nameField, ' ', ' ');
		datafield.addSubfield(factory.newSubfield('a', "<<Le >>Cori"));
		record.addVariableField(datafield);
		Object result = sut.getValue(null, record , null, null, null);
		
		String expectedResult = new String();
		expectedResult = "<<Le >>Cori";
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testCheckMaxLen() {
		DataField datafield = factory.newDataField(nameField, ' ', ' ');
		datafield.addSubfield(factory.newSubfield('a', "Corite"));
		record.addVariableField(datafield);
		Object result = sut.getValue(null, record , null, null, null);
		
		String expectedResult = new String();
		expectedResult = "Corite";
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	
}

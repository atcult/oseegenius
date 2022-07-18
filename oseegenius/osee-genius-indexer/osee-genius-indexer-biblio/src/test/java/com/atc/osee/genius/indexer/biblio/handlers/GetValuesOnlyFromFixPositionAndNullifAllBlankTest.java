package com.atc.osee.genius.indexer.biblio.handlers;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;


public class GetValuesOnlyFromFixPositionAndNullifAllBlankTest {

	private String nameField = "960";
	private String nameField2 = "961";
	private MarcFactory factory;
	private Record record;
	private DataField datafield;
	private DataField datafield2;
	private DataField datafield3;
	private GetValuesOnlyFromFixPositionAndNullifAllBlank sut; 
	
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		
		datafield = factory.newDataField(nameField, ' ', ' ');
		datafield.addSubfield(factory.newSubfield('d', "0123456  9X"));
		record.addVariableField(datafield);
		
		datafield2 = factory.newDataField(nameField, ' ', ' ');
		datafield2.addSubfield(factory.newSubfield('d', "0123456  9X"));
		record.addVariableField(datafield2);
		
		datafield3 = factory.newDataField(nameField2, ' ', ' ');
		datafield3.addSubfield(factory.newSubfield('d', "CF 456 abcde"));
		record.addVariableField(datafield3);
		
		sut = new GetValuesOnlyFromFixPositionAndNullifAllBlank();
	}
	
	
	
	@Test
	public void testMantainBlacks() {
		@SuppressWarnings("unchecked")
		LinkedList<String> result = (LinkedList<String>) sut.getValue("960dY[7-8]", record , null, null, null);
		
		LinkedList<String> expectedResult = new LinkedList<String>();
		expectedResult.add("  ");
		expectedResult.add("  ");
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testDeleteAllBlacks() {
		Object result = sut.getValue("960dN[7-8]", record , null, null, null);
		assertNull(result);
	}
	
	@Test
	public void testFromXtoTheEnd() {
		@SuppressWarnings("unchecked")
		LinkedList<String> result = (LinkedList<String>) sut.getValue("960dY[3-]", record , null, null, null);
		
		LinkedList<String> expectedResult = new LinkedList<String>();
		expectedResult.add("3456  9X");
		expectedResult.add("3456  9X");
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testOnlyOneChar() {
		@SuppressWarnings("unchecked")
		LinkedList<String> result = (LinkedList<String>) sut.getValue("960dN[6-6]", record , null, null, null);
		
		LinkedList<String> expectedResult = new LinkedList<String>();
		expectedResult.add("6");
		expectedResult.add("6");
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	
	@Test
	public void testOutputNullIfAbsent() {
		Object result = sut.getValue("970dY[0-2]", record , null, null, null);
		assertNull(result);
	}
	
	@Test
	public void testMoreOfOneField() {
		@SuppressWarnings("unchecked")
		LinkedList<String> result = (LinkedList<String>) sut.getValue("960dY[0-2]:961dY[0-2]", record , null, null, null);
		
		LinkedList<String> expectedResult = new LinkedList<String>();
		expectedResult.add("012");
		expectedResult.add("012");
		expectedResult.add("CF ");
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testOneYesForAll() {
		@SuppressWarnings("unchecked")
		LinkedList<String> result = (LinkedList<String>) sut.getValue("960dN[7-8]:960dY[7-8]", record , null, null, null);
		
		LinkedList<String> expectedResult = new LinkedList<String>();
		expectedResult.add("  ");
		expectedResult.add("  ");
		expectedResult.add("  ");
		expectedResult.add("  ");
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	

}

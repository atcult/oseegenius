package com.atc.osee.genius.indexer.biblio.handlers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.LeaderImpl;

/**
 * Test of biblio handler GetInventoryBNCFT
 * 
 * @author ztajoli
 * @since 1.2
 */

public class GetInventoryBNCFTestCase {

	private MarcFactory factory;
	private Record record;
	private GetInventoryBNCF sut; 
	
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		LeaderImpl leaderImpl = new LeaderImpl("00977nam2a22002773i 450 ");
		record.setLeader(leaderImpl);
		
		sut = new GetInventoryBNCF();
	}
	
	@Test
	public void testCheckInventoryCF() {
		
		DataField datafield = factory.newDataField("960", ' ', '0');
		datafield.addSubfield(factory.newSubfield('e', "CF   000337636                                C VMB   1 v."));
		record.addVariableField(datafield);
		Object result = sut.getValue(null, record , null, null, null);
		
		String expectedResult = "CF000337636";
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testCheckInventoryNull() {
		
		DataField datafield = factory.newDataField("960", ' ', '0');
		datafield.addSubfield(factory.newSubfield('d', " CFMAGL.     21.9.493"));
		record.addVariableField(datafield);
		Object result = sut.getValue(null, record , null, null, null);
				
		assertNull(result);
	}
	
	@Test
	public void testCheckInventoryCFtesi() {
		
		DataField datafield = factory.newDataField("960", ' ', '0');
		datafield.addSubfield(factory.newSubfield('e', "CFTVD988700001                                L       "));
		record.addVariableField(datafield);
		Object result = sut.getValue(null, record , null, null, null);
		
		String expectedResult = "CFTVD988700001";
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testCheckInventoryIG() {
		
		DataField datafield = factory.newDataField("960", ' ', '0');
		datafield.addSubfield(factory.newSubfield('e', "IGE  000039626 B VM #inv: 23319/1 v. 1"));
		record.addVariableField(datafield);
		Object result = sut.getValue(null, record , null, null, null);
		
		String expectedResult = "IGE000039626";
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}
	
	
	
}

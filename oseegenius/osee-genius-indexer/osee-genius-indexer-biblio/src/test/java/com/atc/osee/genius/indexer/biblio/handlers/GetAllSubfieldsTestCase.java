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
 * GetValue OBML tag handler test case.
 *  
 * @author agazzarini
 * @since 1.0
 */
public class GetAllSubfieldsTestCase 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield;
	private GetValue sut; 
	
	private String aValue = "Summerland /";
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
		
		sut = new GetValue();
	}
	
	@Test
	public void testRepeatedSubfields()
	{
		GetAlphabeticalSubfields gas= new GetAlphabeticalSubfields();
		datafield.addSubfield(factory.newSubfield('a', "seconda a"));
		datafield.addSubfield(factory.newSubfield('b', "primo b"));
		datafield.addSubfield(factory.newSubfield('b', "secondo b"));
		datafield.addSubfield(factory.newSubfield('a', "terza a"));
		datafield.addSubfield(factory.newSubfield('b', "secondo valore"));
		record.addVariableField(datafield);
		System.out.println(gas.getValue("245", record, null, null, null));
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
		datafield.addSubfield(factory.newSubfield(b, aSpace.trim()));
		datafield.addSubfield(factory.newSubfield(b, aSpace));
		datafield.addSubfield(factory.newSubfield(c, cValue));
		
		record.addVariableField(datafield);
		
		String result = (String) sut.getValue(twoHundredFortyFiveAbc, record, null, null, null);
		assertEquals(aValue + aSpace + cValue, result);
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
		
		String result = (String) sut.getValue(twoHundredFortyFive + "xyz", record, null, null, null);
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
		String result = (String) sut.getValue(twoHundredFortyFiveAbc, record, null, null, null);
		
		assertEquals(aValue + aSpace + cValue, result);
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
		datafield.addSubfield(factory.newSubfield('b', multivalue1));
		datafield.addSubfield(factory.newSubfield('b', multivalue2));
		datafield.addSubfield(factory.newSubfield('c', michaelChabon));
		
		record.addVariableField(datafield);

		String result = (String) sut.getValue(twoHundredFortyFiveAbc, record, null, null, null);
		assertEquals(aValue + aSpace + multivalue1 + aSpace + multivalue2 + aSpace + michaelChabon, result);
	} 
	
	@Test
	public void ceretiG()
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\"><record><leader>00756nab a2200217 i 4500</leader><controlfield tag=\"001\">000000018904</controlfield><controlfield tag=\"003\">CaOOAMICUS</controlfield><controlfield tag=\"005\">20040930125930.0</controlfield><controlfield tag=\"008\">950101s1994    xx           u0   u ita c</controlfield><datafield tag=\"040\" ind1=\" \" ind2=\" \"><subfield code=\"a\">DIA</subfield><subfield code=\"b\">ita</subfield></datafield><datafield tag=\"084\" ind1=\" \" ind2=\" \"><subfield code=\"a\">WCRP: (1994) 6th World Conference on Religion and Peace</subfield></datafield><datafield tag=\"090\" ind1=\" \" ind2=\" \"><subfield code=\"a\">1</subfield><subfield code=\"b\">19</subfield></datafield><datafield tag=\"100\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Cereti, G.</subfield></datafield><datafield tag=\"245\" ind1=\"1\" ind2=\"3\"><subfield code=\"a\">La sesta assemblea mondiale delle religioni per la pace a Riva del Garda :</subfield><subfield code=\"b\">\"Sanare il mondo, le religioni per la pace\".</subfield></datafield><datafield tag=\"650\" ind1=\" \" ind2=\"4\"><subfield code=\"a\">Ecumenismo</subfield><subfield code=\"x\">Italia.</subfield><subfield code=\"2\">//ita</subfield></datafield><datafield tag=\"650\" ind1=\" \" ind2=\"4\"><subfield code=\"a\">World Conference on Religion and Peace (WCRP)</subfield><subfield code=\"2\">//ita</subfield></datafield><datafield tag=\"650\" ind1=\" \" ind2=\"4\"><subfield code=\"a\">Religioni</subfield><subfield code=\"x\">incontri WCRP VI.</subfield><subfield code=\"2\">//ita</subfield></datafield><datafield tag=\"655\" ind1=\" \" ind2=\"7\"><subfield code=\"a\">INFORMATION.</subfield><subfield code=\"2\">local//eng</subfield></datafield><datafield tag=\"740\" ind1=\"0\" ind2=\" \"><subfield code=\"a\">Religioni per la pace.</subfield></datafield><datafield tag=\"773\" ind1=\"1\" ind2=\" \"><subfield code=\"t\">Religioni per la pace </subfield><subfield code=\"g\">52 (1994) 1-3</subfield></datafield><datafield tag=\"941\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">19</subfield></datafield></record></collection>";
		MarcReader reader = new MarcXmlReader(new InputSource(new StringReader(xml)));
		
		if (reader.hasNext())
		{
			Record record = reader.next();
			
			System.out.println(">>" + sut.getValue("100aqbcd", record, null, null, null) + "<<");
		}
		
	}
}
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
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcReader;
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
public class GetValueTestCase 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield;
	private DataField datafield710;
	private DataField datafield710m;
	private DataField datafieldean;
	private GetValue sut; 
	
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
		
		datafield710 = factory.newDataField("710", '0', '1');
		datafield710m = factory.newDataField("710", '1', '2');
		datafieldean = factory.newDataField("024", '3', ' ');
		
		sut = new GetValue();
	}
	
	
	@Test
	public void withZero() {
		datafield.addSubfield(factory.newSubfield('0', zValue));
		record.addVariableField(datafield);
		String result = (String) sut.getValue("2450", record, null, null, null);
		System.out.println(result+"\n");
		assertEquals(zValue, result);
	}
	
	
	/**
	 * Tests the OBML GetValue function with a filter enabled.
	 * 
	 * <br/>precondition: the incoming expression contains a tag mapping and a filter.
	 * <br/>postcondition: the subfield value that matches the filter is filtered out.
	 */
	@Test
	public void withFilter()
	{
		String filter = aValue;
		record.addVariableField(datafield);
		
		String result = (String) sut.getValue(twoHundredFortyFive + a + "," + filter, record, null, null, null);
		assertNull(result);		
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
	public void authorCorporate() 
	{
		datafield710.addSubfield(factory.newSubfield(a, "Fondazione ternana opera educatrice"));
		datafield710.addSubfield(factory.newSubfield('3', "CFIV319125"));
		record.addVariableField(datafield710);
		
		String result = (String) sut.getValue("710abcdfghp,>0.", record, null, null, null);
		assertEquals("Fondazione ternana opera educatrice", result);
	}
	
	@Test
	public void authorMeeting() 
	{
		datafield710m.addSubfield(factory.newSubfield(a, "Riunione annuale CERN"));
		datafield710m.addSubfield(factory.newSubfield('e', "<Genevra>"));
		datafield710m.addSubfield(factory.newSubfield('3', "CFIV319125"));
		record.addVariableField(datafield710m);
		
		String result = (String) sut.getValue("710abcdefghp,>1.", record, null, null, null);
		assertEquals("Riunione annuale CERN <Genevra>", result);
	}
	
	@Test
	public void eanCheck() 
	{
		datafieldean.addSubfield(factory.newSubfield(a, "005-4577bis"));
		datafieldean.addSubfield(factory.newSubfield('5', "CFI77788"));
		record.addVariableField(datafieldean);
		
		String result = (String) sut.getValue("024a,>3#", record, null, null, null);
		assertEquals("005-4577bis", result);
		
		String result2 = (String) sut.getValue("024a,>2.", record, null, null, null);
		assertNull(result2);
		
		String result3 = (String) sut.getValue("024a,>.2", record, null, null, null);
		assertNull(result3);
		
		String result4= (String) sut.getValue("024a,>3.", record, null, null, null);
		assertEquals("005-4577bis", result4);
		
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
	
	@Test
	public void blabla()
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\"><record><leader>00756nab a2200217 i 4500</leader><controlfield tag=\"001\">000000018904</controlfield><controlfield tag=\"003\">CaOOAMICUS</controlfield><controlfield tag=\"005\">20040930125930.0</controlfield><controlfield tag=\"008\">950101s1994    xx           u0   u ita c</controlfield><datafield tag=\"040\" ind1=\" \" ind2=\" \"><subfield code=\"a\">DIA</subfield><subfield code=\"b\">ita</subfield></datafield><datafield tag=\"084\" ind1=\" \" ind2=\" \"><subfield code=\"a\">WCRP: (1994) 6th World Conference on Religion and Peace</subfield></datafield><datafield tag=\"090\" ind1=\" \" ind2=\" \"><subfield code=\"a\">1</subfield><subfield code=\"b\">19</subfield></datafield><datafield tag=\"100\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Cereti, G.</subfield></datafield><datafield tag=\"245\" ind1=\"1\" ind2=\"3\"><subfield code=\"a\">La sesta assemblea mondiale delle religioni per la pace a Riva del Garda :</subfield><subfield code=\"b\">\"Sanare il mondo, le religioni per la pace\".</subfield></datafield><datafield tag=\"650\" ind1=\" \" ind2=\"4\"><subfield code=\"a\">Ecumenismo</subfield><subfield code=\"x\">Italia.</subfield><subfield code=\"2\">//ita</subfield></datafield><datafield tag=\"650\" ind1=\" \" ind2=\"4\"><subfield code=\"a\">World Conference on Religion and Peace (WCRP)</subfield><subfield code=\"2\">//ita</subfield></datafield><datafield tag=\"650\" ind1=\" \" ind2=\"4\"><subfield code=\"a\">Religioni</subfield><subfield code=\"x\">incontri WCRP VI.</subfield><subfield code=\"2\">//ita</subfield></datafield><datafield tag=\"655\" ind1=\" \" ind2=\"7\"><subfield code=\"a\">INFORMATION.</subfield><subfield code=\"2\">local//eng</subfield></datafield><datafield tag=\"740\" ind1=\"0\" ind2=\" \"><subfield code=\"a\">Religioni per la pace.</subfield></datafield><datafield tag=\"773\" ind1=\"1\" ind2=\" \"><subfield code=\"t\">Religioni per la pace </subfield><subfield code=\"g\">52 (1994) 1-3</subfield></datafield><datafield tag=\"941\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">19</subfield></datafield><datafield tag=\"774\" ind1=\"1\" ind2=\" \"><subfield code=\"w\">445551</subfield></datafield></record></collection>";
		MarcReader reader = new MarcXmlReader(new InputSource(new StringReader(xml)));
		
		if (reader.hasNext())
		{
			Record record = reader.next();
			Object value = sut.getValue("774w", record, null, null, null);
			NumericString2 numericString = new NumericString2();
			BibDecorator bibDecorator = new BibDecorator();
			Object firstDecorator = numericString.decorate("774", value.toString());
			Object secondDecorator = bibDecorator.decorate("774", firstDecorator.toString());
			System.out.println(">>" + secondDecorator.toString() + "<<");
		}		
	}
	
	
}
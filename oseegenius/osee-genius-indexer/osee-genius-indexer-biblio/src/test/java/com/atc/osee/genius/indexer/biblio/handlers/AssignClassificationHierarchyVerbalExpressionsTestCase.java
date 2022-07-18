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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.solr.core.SolrCore;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

/**
 * Test case for OBML function dewey hierarchy assignator.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AssignClassificationHierarchyVerbalExpressionsTestCase 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield;
	
	private AssignClassificationHierarchyVerbalExpressions cut;
	
	private static final String DECIMAL_CLASSIFICATION_PROPERTY_FILENAME = "dewey_en.properties";
	private static Properties decimalClassification;
	
	/**
	 * Loads once the dewey decimal classification.
	 * 
	 * @throws IOException in case a load failure occurs.
	 */
	@BeforeClass
	public static void loadDecimalClassification() throws IOException
	{
		decimalClassification = new Properties();
		decimalClassification.setProperty("318", "Social sciences");
		decimalClassification.setProperty("310", "General statistics");
		decimalClassification.setProperty("300", "General statistics Of South America");
	}
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		
		datafield = factory.newDataField();
		datafield.setTag("082");
		datafield.addSubfield(factory.newSubfield('a', "318.23"));
		record.addVariableField(datafield);
		
		cut = new AssignClassificationHierarchyVerbalExpressions()
		{
			@Override
			Properties getFileContent(final String name, final SolrCore core) throws IOException 
			{
				return decimalClassification;
			}
		};
	}
	
	/**
	 * Positive test for this OBML function.
	 * 
	 * <br/>precondition: a given MARC record contains a valid 082a CDD code.
	 * <br/>postcondition: the whole corresponding hierarchy is included as part of the result.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void positive()
	{
		String threeOneEigth = "318";
		String threeOneZero = "310";
		String threeZeroZero = "300";
		
		String verbalExpression318 = decimalClassification.getProperty(threeOneEigth);
		String verbalExpression310 = decimalClassification.getProperty(threeOneZero);
		String verbalExpression300 = decimalClassification.getProperty(threeZeroZero);
		
		Set<String> expectedResult = new LinkedHashSet(
				Arrays.asList(
						verbalExpression318, 
						verbalExpression310, 
						verbalExpression300));
		
		Object result = cut.getValue(
				DECIMAL_CLASSIFICATION_PROPERTY_FILENAME + ",082a,3", 
				record, 
				null, 
				null, 
				null);
		
		assertEquals(expectedResult, result);
	}
	
	/**
	 * An expression has been given but no value is found in the MARC record..
	 * 
	 * <br/>precondition: the expression matches a non existent tag.
	 * <br/>postcondition: the result of this function is null.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void noValueForGivenExpression()
	{				
		Collection<String> result = (Collection<String>) cut.getValue(
				DECIMAL_CLASSIFICATION_PROPERTY_FILENAME + ",098z,3", 
				record, 
				null, 
				null, 
				null);
		
		assertTrue(result.isEmpty());
	}	
}
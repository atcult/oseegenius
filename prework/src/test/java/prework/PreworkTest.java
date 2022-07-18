package prework;

import static org.junit.Assert.assertFalse;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.atcult.folio.prework.Prework;

public class PreworkTest {
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	

	@Test
	 public final void isNotLoanable_OldYear() {
		//Tag 100
			String field = "$a20170525e1949    ||||0itac50      ba";
			boolean result = Prework.isNotLoanable(field, 0);
			assertTrue(result);
				
		}
	
	@Test
	 public final void isNotLoanable_NewYear() {
		//Tag 100
			String field = "$a20170525e2019    ||||0itac50      ba";
			boolean result = Prework.isNotLoanable(field, 0);
			assertTrue(result);
				
		}
	
	@Test
	 public final void isNotLoanable_DateTypeF() {
		//Tag 100
			String field = "$a20160514f20132019||||0itac50      ba";
			boolean result = Prework.isNotLoanable(field, 0);
			assertTrue(result);
				
		}
	
	@Test
	 public final void isNotLoanable_DateTypeI() {
		//Tag 100
			String field = "$a20160514i20132019||||0itac50      ba";
			boolean result = Prework.isNotLoanable(field, 0);
			assertTrue(result);
				
		}
	

	@Test
	 public final void isDateNonNumeric() {
		//Tag 100
			String field = "$a20160514f2013aaaa||||0itac50      ba";
			boolean result = Prework.isNotLoanable(field, 0);
			assertFalse(result);
				
		}
	
	@Test
	 public final void isNotLoanable_failed() {
		//Tag 100
			String field = "$a20170525d2011    ||||0itac50      ba";
			boolean result = Prework.isNotLoanable(field, 0);
			assertFalse(result);
				
		}
	
	 
	@Test
	  public final void replaceAvailabilityCode() {
		String recordText = "00391nam0a22000853i 450 001001100000005001700011100004100028200005800069960017800127AGR001594620190415093701.0  a20180623d2018    ||||0itac50      ba1 aOmaggio al poeta-scrittore siciliano Ignazio Buttitta 0aBibl. Nazionale Centrale Di Firenzed CFGEN       B 45                    8581eCF   006731163                                A VMB   1 v.gGEN B45 08581h20180623i20180623";
		MarcReader reader = new MarcStreamReader(new ByteArrayInputStream(recordText.getBytes()));
		Record rec = reader.next();
		String tag = "$a20180623d2018    ||||0itac50      ba";
		boolean result = Prework.isNotLoanable(tag, 0);
		assertTrue(result);
		String [] tags = {"960"};
		ArrayList<VariableField> items = Prework.insertItemForteBel(rec, tags);
		items = Prework.replaceAvailabilityCode(rec, items, "BID", null);
		VariableField field = items.get(0);
		String subfield = extractedSubfield(field, 'e');
		assertTrue(subfield.substring(2, 4).equals("CF"));
		assertTrue(subfield.substring(48, 49).equals("A"));
		assertTrue(subfield.substring(46, 48).equals("P "));
	 }
	
	@Test
	  public final void replaceAvailabilityCode_failed() {
		String recordText = "00383nam0a22000853i 450 001001100000005001700011100004100028200007000069960015800139ANA000028320190415093702.0  a20041201d1884    ||||0itac50      ba1 aPergolesi e Spontiniesaggio biografico-criticofFrancesco Colini 0aBibl. Nazionale Centrale Di Firenzeb1 v.c1 v.d CFMAGL.     9.9.368eCF   000454947                                C VMB   1 v.gMAGL.9.9.368h20041201";
		MarcReader reader = new MarcStreamReader(new ByteArrayInputStream(recordText.getBytes()));
		Record rec = reader.next();
		String tag = "$a20041201d1884    ||||0itac50      ba";
		boolean result = Prework.isNotLoanable(tag, 0);
		assertTrue(result);
	    String [] tags = {"960"};
		ArrayList<VariableField> items = Prework.insertItemForteBel(rec, tags);
		items = Prework.replaceAvailabilityCode(rec, items, "BID", null);
		VariableField field = items.get(0);
		String subfield = extractedSubfield(field, 'e');
		assertTrue(subfield.substring(2, 4).equals("CF"));
		assertTrue(subfield.substring(48, 49).equals("C"));
		assertTrue(subfield.substring(46, 48).equals("  "));
		assertFalse(subfield.substring(46, 48).equals("P  "));
	 }
	
	private String extractedSubfield(VariableField field, char code) {
		DataField dataField = (DataField) field;
		return dataField.getSubfield('e').toString();
	}
	
}

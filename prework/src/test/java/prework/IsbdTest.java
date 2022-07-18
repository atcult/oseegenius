package prework;

import static org.junit.Assert.*;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.atcult.folio.prework.Isbd;

public class IsbdTest {
	private String delim = "$";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void ExtractSub_Inside() {
		String field = "$aBiblioteca Nazionale$dCOLL. 7888$eCF   000000004     AVt$zNOFORTE";
		Object result = Isbd.extractSubfield(field, "$e", delim);
		
		assertTrue(result instanceof String);
		String str_result = (String) result;
		assertTrue(str_result.equals("$eCF   000000004     AVt"));
	}
	
	@Test
	public final void ExtractSub_Limit() {
		String field = "$aBiblioteca Nazionale$dCOLL. 7888$eCF   000000004     AVt";
		Object result = Isbd.extractSubfield(field, "$e", delim);
		
		assertTrue(result instanceof String);
		String str_result = (String) result;
		assertTrue(str_result.equals("$eCF   000000004     AVt"));
	}
	
	@Test
	public final void ExtractSub_Only() {
		String field = "$eCF   000000004     AVt";
		Object result = Isbd.extractSubfield(field, "$e", delim);
		
		assertTrue(result instanceof String);
		String str_result = (String) result;
		assertTrue(str_result.equals("$eCF   000000004     AVt"));
	}
	
	@Test
	public final void ExtractSub_NULL() {
		String field = "$aBiblioteca Nazionale$dCOLL. 7888";
		Object result = Isbd.extractSubfield(field, "$e", delim);
		assertNull(result);
		
		field = "";
		result = Isbd.extractSubfield(field, "$e", delim);
		assertNull(result);
	}
	
	
	 
}

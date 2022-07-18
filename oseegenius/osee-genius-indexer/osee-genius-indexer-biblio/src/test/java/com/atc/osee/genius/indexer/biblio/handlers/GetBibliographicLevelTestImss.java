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

import org.junit.Test;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.LeaderImpl;

/**
 * TEst case for GetBibliographicLevel OBML function.
 * @author aliceg
 *
 */
public class GetBibliographicLevelTestImss {
	
	@Test
	public void values() {
		final MarcFactory factory = MarcFactory.newInstance();
		final Record record = factory.newRecord();
		VariableField _008 = factory.newControlField("007", "950727s2005 xx eng u");
		LeaderImpl leaderImpl = new LeaderImpl("01538nap a2200421 i 4500");
		record.setLeader(leaderImpl);
		record.addVariableField(_008);
		
		ImssGetBibliographicLevel hadler = new ImssGetBibliographicLevel();
		Object result = hadler.getValue("", record, null, null, null);
		System.out.println(result);
		assertEquals("b", result);				
	}
	
}

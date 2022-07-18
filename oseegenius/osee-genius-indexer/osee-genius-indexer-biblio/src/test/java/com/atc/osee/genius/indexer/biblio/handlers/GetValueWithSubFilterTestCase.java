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

import java.io.StringReader;
import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.LeaderImpl;
import org.xml.sax.InputSource;

/**
 * GetValueWithSubFilter OBML tag handler test case.
 *  
 * @author ztajoli
 * @since 1.2
 */
public class GetValueWithSubFilterTestCase 
{
	private MarcFactory factory;
	private Record record;
	private DataField datafield1;
	private DataField datafield2;
	private DataField datafield3;
	private DataField datafield4;
	private DataField datafield5;
	private GetValueWithSubFilter sut; 
	
	
	/**
	 * Setup fixture for this test case.
	 */
	@Before
	public void setUp()
	{
		factory = MarcFactory.newInstance();
		record = factory.newRecord();
		LeaderImpl leaderImpl = new LeaderImpl("00977nam2a22002773i 450 ");
		record.setLeader(leaderImpl);
		
		datafield1 = factory.newDataField("245", '1', '0');
		datafield1.addSubfield(factory.newSubfield('a', "CSS£ for all"));
		record.addVariableField(datafield1);
		
		sut = new GetValueWithSubFilter();
	}
	
	
	@Test
	public void withFilter() {
		datafield2 = factory.newDataField("702", ' ', ' ');
		datafield2.addSubfield(factory.newSubfield('a', "Rossi, Antonio"));
		datafield2.addSubfield(factory.newSubfield('4', "390"));
		record.addVariableField(datafield2);
		
		datafield5 = factory.newDataField("702", ' ', ' ');
		datafield5.addSubfield(factory.newSubfield('a', "Ramus, Sergej"));
		datafield5.addSubfield(factory.newSubfield('4', "390"));
		record.addVariableField(datafield5);
				
		datafield3 = factory.newDataField("712", ' ', ' ');
		datafield3.addSubfield(factory.newSubfield('a', "Verdi, Andrea"));
		datafield3.addSubfield(factory.newSubfield('4', "320"));
		record.addVariableField(datafield3);
		
		datafield4 = factory.newDataField("712", ' ', ' ');
		datafield4.addSubfield(factory.newSubfield('a', "Gaston, Peter"));
		record.addVariableField(datafield4);
		
		LinkedHashSet<String> chk = new LinkedHashSet<String>();
		chk.add("Rossi, Antonio");
		chk.add("Ramus, Sergej");
		
		Object result = sut.getValue("702abcdfgp:712abcdfghp,4,390", record, null, null, null);
		assertEquals(chk, result);
		
		Object result2 = sut.getValue("702abcdfgp:712abcdfghp,4,320", record, null, null, null);
		assertEquals("Verdi, Andrea", result2);
		
		Object result3 = sut.getValue("700abcdfgp:710abcdfghp,4,320", record, null, null, null);
		assertNull(result3);
	}
	
	@Test
	public void chk_record()
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\"><record><leader>05201nam0a2200601 i 450 </leader><controlfield tag=\"001\">CFIE000183</controlfield><controlfield tag=\"005\">20191016155344.0</controlfield><datafield tag=\"012\" ind1=\" \" ind2=\" \"><subfield code=\"a\">o.lo dehe a.ar maor (3) 1568 (R)</subfield><subfield code=\"2\">fei</subfield></datafield><datafield tag=\"049\" ind1=\" \" ind2=\" \"><subfield code=\"a\">SBN</subfield></datafield><datafield tag=\"100\" ind1=\" \" ind2=\" \"><subfield code=\"a\">20000101d1568    ||||0itac50      ba</subfield></datafield><datafield tag=\"101\" ind1=\"|\" ind2=\" \"><subfield code=\"a\">ita</subfield></datafield><datafield tag=\"102\" ind1=\" \" ind2=\" \"><subfield code=\"a\">it</subfield></datafield><datafield tag=\"181\" ind1=\" \" ind2=\"1\"><subfield code=\"6\">z01</subfield><subfield code=\"a\">i </subfield><subfield code=\"b\">xxxe  </subfield></datafield><datafield tag=\"182\" ind1=\" \" ind2=\"1\"><subfield code=\"6\">z01</subfield><subfield code=\"a\">n</subfield></datafield><datafield tag=\"200\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Comento sopra la canzone di Guido Caualcanti. Di f. Paolo del Rosso caualiere de la religione di S. Gio. Battista &amp; accademico fiorentino</subfield></datafield><datafield tag=\"210\" ind1=\" \" ind2=\" \"><subfield code=\"a\">In Fiorenza</subfield><subfield code=\"c\">appresso Bartolomeo Sermartelli</subfield><subfield code=\"d\">1568</subfield><subfield code=\"e\">In Fiorenza</subfield><subfield code=\"g\">appresso Bartolomeo Sermartelli</subfield><subfield code=\"h\">1568</subfield></datafield><datafield tag=\"215\" ind1=\" \" ind2=\" \"><subfield code=\"a\">167, [1] p.</subfield><subfield code=\"d\">8º</subfield></datafield><datafield tag=\"300\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Con il testo della canzone di Cavalcanti</subfield></datafield><datafield tag=\"300\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Marca (Z1151) sul front</subfield></datafield><datafield tag=\"300\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Cors. ; rom</subfield></datafield><datafield tag=\"300\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Segn.: A-I⁸ K⁴ L⁸</subfield></datafield><datafield tag=\"300\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Iniziali xil.</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Tutta pelle con riquadri a secco. Sulla c. di guardia post. timbro a olio della Legatoria Vangelisti; sul front. timbro a olio BNCF.</subfield><subfield code=\"5\">IT-FI0098          , V.BAN     B.19.2.285</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">1/4 di pelle e carta marmorizz. Sul risguardo del piatto ant. ex-libris Nencini,dat. 1874.</subfield><subfield code=\"5\">IT-FI0098          , V.BAN     B.19.2.285</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Tutta perg., con tracce di lacci. Sul front. timbro a olio BNF, dat. 1872, e timbro a secco della Bibl. Palatina; ivi, inoltre tracce di nota ms. di possesso.</subfield><subfield code=\"5\">IT-FI0098          , V.BAN     B.19.2.285</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Leg. in tutta carta. Sul recto della prima c. di guardia ant. coll. Targioni \"VII CAVAL. c.Com.Ross. Sul front. timbro a olio della Bibl. Magl.</subfield><subfield code=\"5\">IT-FI0098          , V.BAN     B.19.2.285</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">1 esemplare</subfield><subfield code=\"5\">IT-FI0098          , V.BAN     B.19.2.285</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Tutta pelle con riquadri a secco. Sulla c. di guardia post. timbro a olio della Legatoria Vangelisti; sul front. timbro a olio BNCF.</subfield><subfield code=\"5\">IT-FI0098          , V.BAN     B.19.2.285</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">1/4 di pelle e carta marmorizz. Sul risguardo del piatto ant. ex-libris Nencini,dat. 1874.</subfield><subfield code=\"5\">IT-FI0098          , NENC.     1.3.2.3</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Tutta perg., con tracce di lacci. Sul front. timbro a olio BNF, dat. 1872, e timbro a secco della Bibl. Palatina; ivi, inoltre tracce di nota ms. di possesso.</subfield><subfield code=\"5\">IT-FI0098          , PALAT     12.B.A.3.               1.54</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Leg. in tutta carta. Sul recto della prima c. di guardia ant. coll. Targioni \"VII CAVAL. c.Com.Ross. Sul front. timbro a olio della Bibl. Magl.</subfield><subfield code=\"5\">IT-FI0098          , MAGL.     3.5.225</subfield></datafield><datafield tag=\"316\" ind1=\" \" ind2=\" \"><subfield code=\"a\">1 esemplare</subfield><subfield code=\"5\">IT-FI0098          , NENC.     1.5.5.6</subfield></datafield><datafield tag=\"317\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Provenienza: Nencini, Giovanni &lt;1803-1878&gt;. Sul piatto ant. int. ex-libris a stampa Giovanni Nencini dat. 1874.</subfield><subfield code=\"5\">IT-FI0098           NENC. 1.3.2.3</subfield></datafield><datafield tag=\"317\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Provenienza: Nencini, Giovanni &lt;1803-1878&gt;. Sul piatto ant. int. ex-libris a stampa Giovanni Nencini dat. 1874.</subfield><subfield code=\"5\">IT-FI0098           NENC. 1.3.2.3</subfield></datafield><datafield tag=\"620\" ind1=\" \" ind2=\" \"><subfield code=\"a\">IT</subfield><subfield code=\"d\">Firenze</subfield><subfield code=\"3\">RMSL001067</subfield></datafield><datafield tag=\"700\" ind1=\" \" ind2=\"1\"><subfield code=\"a\">Del Rosso</subfield><subfield code=\"b\">, Paolo</subfield><subfield code=\"3\">SBLV086210</subfield></datafield><datafield tag=\"702\" ind1=\" \" ind2=\"1\"><subfield code=\"a\">Cavalcanti</subfield><subfield code=\"b\">, Guido</subfield><subfield code=\"f\"> &lt;ca. 1255-1300&gt;</subfield><subfield code=\"3\">CFIV038084</subfield></datafield><datafield tag=\"702\" ind1=\" \" ind2=\"1\"><subfield code=\"a\">Nencini</subfield><subfield code=\"b\">, Giovanni</subfield><subfield code=\"f\"> &lt;1803-1878&gt;</subfield><subfield code=\"3\">CFIT000005</subfield><subfield code=\"4\">320</subfield><subfield code=\"5\">IT-FI0098           NENC. 1.3.2.3</subfield></datafield><datafield tag=\"702\" ind1=\" \" ind2=\"1\"><subfield code=\"a\">Nencini</subfield><subfield code=\"b\">, Giovanni</subfield><subfield code=\"f\"> &lt;1803-1878&gt;</subfield><subfield code=\"3\">CFIT000005</subfield><subfield code=\"4\">320</subfield><subfield code=\"5\">IT-FI0098           NENC. 1.3.2.3</subfield></datafield><datafield tag=\"712\" ind1=\"0\" ind2=\"2\"><subfield code=\"a\">Sermartelli, Bartolomeo</subfield><subfield code=\"d\"> &lt;1.</subfield><subfield code=\"f\"> ; 1553-1591&gt;</subfield><subfield code=\"3\">BVEV017540</subfield><subfield code=\"4\">650</subfield></datafield><datafield tag=\"790\" ind1=\" \" ind2=\"1\"><subfield code=\"a\">Dal Rosso</subfield><subfield code=\"b\">, Paolo</subfield><subfield code=\"3\">BVEV056275</subfield><subfield code=\"z\">Del Rosso, Paolo</subfield></datafield><datafield tag=\"790\" ind1=\" \" ind2=\"1\"><subfield code=\"a\">Rosso</subfield><subfield code=\"b\">, Paolo : del</subfield><subfield code=\"3\">SBNV014138</subfield><subfield code=\"z\">Del Rosso, Paolo</subfield></datafield><datafield tag=\"791\" ind1=\"0\" ind2=\"2\"><subfield code=\"a\">Sermartelli, Bartholomeo</subfield><subfield code=\"3\">CFIV280176</subfield><subfield code=\"z\">Sermartelli, Bartolomeo &lt;1. ; 1553-1591&gt;</subfield></datafield><datafield tag=\"801\" ind1=\" \" ind2=\"3\"><subfield code=\"a\">IT</subfield><subfield code=\"b\">IT-FI0098</subfield><subfield code=\"c\">20000101</subfield></datafield><datafield tag=\"850\" ind1=\" \" ind2=\" \"><subfield code=\"a\">IT-FI0098          </subfield></datafield><datafield tag=\"921\" ind1=\" \" ind2=\" \"><subfield code=\"a\">BVEM001022</subfield><subfield code=\"b\">In cornice: tartaruga porta sul dorso una vela gonfiata dal vento, sulla quale è il giglio fiorentino. Motto: Festina lente.</subfield><subfield code=\"c\">Z1151</subfield><subfield code=\"d\">Sul front.</subfield></datafield><datafield tag=\"960\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Bibl. Nazionale Centrale Di Firenze</subfield><subfield code=\"c\">1 v.</subfield><subfield code=\"d\"> CFV.BAN     B.19.2.285</subfield><subfield code=\"e\">CF   000957058                                C VMB   Tutta pelle con riquadri a secco. Sulla c. di guardia post. timbro a olio della Legatoria Vangelisti; sul front. timbro a olio BNCF.</subfield><subfield code=\"g\">B.19.2.285</subfield><subfield code=\"h\">19970606</subfield><subfield code=\"i\">20130617</subfield><subfield code=\"s\">NOFORTE</subfield></datafield><datafield tag=\"960\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Bibl. Nazionale Centrale Di Firenze</subfield><subfield code=\"c\">1 v.</subfield><subfield code=\"d\"> CFNENC.     1.3.2.3</subfield><subfield code=\"e\">CF   005261537                                C VMB   1/4 di pelle e carta marmorizz. Sul risguardo del piatto ant. ex-libris Nencini,dat. 1874.</subfield><subfield code=\"g\">NENC.1.3.2.3</subfield><subfield code=\"h\">19970606</subfield><subfield code=\"s\">NOFORTE</subfield></datafield><datafield tag=\"960\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Bibl. Nazionale Centrale Di Firenze</subfield><subfield code=\"c\">1 v.</subfield><subfield code=\"d\"> CFPALAT     12.B.A.3.               1.54</subfield><subfield code=\"e\">CF   005261536                                C VMB   Tutta perg., con tracce di lacci. Sul front. timbro a olio BNF, dat. 1872, e timbro a secco della Bibl. Palatina; ivi, inoltre tracce di nota ms. di possesso.</subfield><subfield code=\"g\">PALAT.12.B.A.3.1.54</subfield><subfield code=\"h\">19970606</subfield><subfield code=\"s\">NOFORTE</subfield></datafield><datafield tag=\"960\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Bibl. Nazionale Centrale Di Firenze</subfield><subfield code=\"c\">1 v.</subfield><subfield code=\"d\"> CFMAGL.     3.5.225</subfield><subfield code=\"e\">CF   005261535                                C VMB   Leg. in tutta carta. Sul recto della prima c. di guardia ant. coll. Targioni \"VII CAVAL. c.Com.Ross. Sul front. timbro a olio della Bibl. Magl.</subfield><subfield code=\"g\">MAGL.3.5.225</subfield><subfield code=\"h\">19970606</subfield><subfield code=\"s\">NOFORTE</subfield></datafield><datafield tag=\"960\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Bibl. Nazionale Centrale Di Firenze</subfield><subfield code=\"b\">1 esemplare</subfield><subfield code=\"c\">1 esemplare</subfield><subfield code=\"d\"> CFNENC.     1.5.5.6</subfield><subfield code=\"e\">CF   005811034                                C VM    1 esemplare</subfield><subfield code=\"g\">NENC.1.5.5.6</subfield><subfield code=\"h\">20130422</subfield><subfield code=\"i\">20130422</subfield><subfield code=\"s\">NOFORTE</subfield></datafield><datafield tag=\"977\" ind1=\" \" ind2=\" \"><subfield code=\"a\"> CF</subfield></datafield><datafield tag=\"FMT\" ind1=\" \" ind2=\" \"><subfield code=\"a\">BK</subfield></datafield></record></collection>"; 
		MarcReader reader = new MarcXmlReader(new InputSource(new StringReader(xml)));
		
		if (reader.hasNext())
		{
			Record record = reader.next();
			Object result = sut.getValue("702abcdfgp:712abcdfghp,4,320", record, null, null, null);
			System.out.println(result);
		}
		
	}

	
}
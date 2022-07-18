/*
 * (c) LibriCore
 * 
 * Created on Aug 6, 2004
 * 
 * $Author: Paulm $
 * $Date: 2007/02/13 09:17:59 $
 * $Locker:  $
 * $Name:  $
 * $Revision: 1.12 $
 * $Source: /source/LibriSuite/src/librisuite/business/common/DAOBibliographicCorrelation.java,v $
 * $State: Exp $
 */
package librisuite.business.common;

import java.util.Iterator;
import java.util.List;

import librisuite.business.searching.DAOIndexList;
import librisuite.hibernate.Correlation;
import librisuite.hibernate.CorrelationKey;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages access to table S_BIB_MARC_IND_DB_CRLTN -- the correlation between AMICUS
 * database encoding and MARC21 encoding
 * @author paulm
 * @version $Revision: 1.12 $, $Date: 2007/02/13 09:17:59 $
 */
public class DAOBibliographicCorrelation extends DAOCorrelation {
	private static final Log logger =
		LogFactory.getLog(DAOBibliographicCorrelation.class);

	/**
	 * Returns the BibliographicCorrelation from BibliographicCorrelationKey
	 * @param bibliographicCorrelationKey -- the database bibliographicCorrelationKey
	 * @return a BibliographicCorrelation object containing or null when none found
	 *
	 */
	public Correlation getBibliographicCorrelation(CorrelationKey bibliographicCorrelationKey)
		throws DataAccessException {
		return getBibliographicCorrelation(
			bibliographicCorrelationKey.getMarcTag(),
			bibliographicCorrelationKey.getMarcFirstIndicator(),
			bibliographicCorrelationKey.getMarcSecondIndicator(),
			bibliographicCorrelationKey.getMarcTagCategoryCode());
	}

	/**
	 * Returns the BibliographicCorrelation based on MARC encoding and category code
	 * @param tag -- marc tag
	 * @param firstIndicator -- marc first indicator
	 * @param secondIndicator -- marc second indicator
	 * @param categoryCode -- category code
	 * @return a BibliographicCorrelation object or null when none found
	 *
	 */
	public Correlation getBibliographicCorrelation(
		String tag,
		char firstIndicator,
		char secondIndicator,
		short categoryCode)
		throws DataAccessException {

		List l =
			find(
				"from BibliographicCorrelation as bc "
					+ "where bc.key.marcTag = ? and "
					+ "(bc.key.marcFirstIndicator = ? or bc.key.marcFirstIndicator='S' )and "
					//Natascia 13/06/2007: scommentate chiocciole
					+ "bc.key.marcFirstIndicator <> '@' and "
					+ "(bc.key.marcSecondIndicator = ? or bc.key.marcSecondIndicator='S')and "
					+ "bc.key.marcSecondIndicator <> '@' and "
					+ "bc.key.marcTagCategoryCode = ?",
				new Object[] {
					new String(tag),
					new Character(firstIndicator),
					new Character(secondIndicator),
					new Short(categoryCode)},
				new Type[] {
					Hibernate.STRING,
					Hibernate.CHARACTER,
					Hibernate.CHARACTER,
					Hibernate.SHORT });
		
		if (l.size() == 1) {
			return (Correlation) l.get(0);
		} 
		else
			return null;
	}

	/**
	 * Returns the MARC encoding based on the input database encodings
	 * @param category -- the database category (1-name, etc...)
	 * @param value1 -- the first database code
	 * @param value2 -- the second database code
	 * @param value3 -- the third database code
	 * @return a BibliographicCorrelationKey object containing 
	 * the MARC encoding (tag and indicators) or null when none found
	 *
	 */
	public CorrelationKey getMarcEncoding(
		short category,
		short value1,
		short value2,
		short value3)
		throws DataAccessException {

		List l =
			find(
				"from BibliographicCorrelation as bc "
					+ "where bc.key.marcTagCategoryCode = ? and "
					+ "bc.databaseFirstValue = ? and "
					+ "bc.databaseSecondValue = ? and "
					+ "bc.databaseThirdValue = ?",
				new Object[] {
					new Short(category),
					new Short(value1),
					new Short(value2),
					new Short(value3)},
				new Type[] {
					Hibernate.SHORT,
					Hibernate.SHORT,
					Hibernate.SHORT,
					Hibernate.SHORT });

		if (l.size() == 1) {
			return ((Correlation) l.get(0)).getKey();
		} else {
			return null;
		}
	}

	public List getSecondCorrelationList(
		short category,
		short value1,
		Class codeTable)
		throws DataAccessException {

		return find(
				" select distinct ct from "
					+ codeTable.getName()
					+ " as ct, BibliographicCorrelation as bc "
					+ " where bc.key.marcTagCategoryCode = ? and "
					+ " bc.key.marcFirstIndicator <> '@' and "
					+ " bc.key.marcSecondIndicator <> '@' and "
					+ " bc.databaseFirstValue = ? and "
					+ " bc.databaseSecondValue = ct.code and  "
					+ "ct.obsoleteIndicator = 0  order by ct.sequence ",
				new Object[] { new Short(category), new Short(value1)},
				new Type[] { Hibernate.SHORT, Hibernate.SHORT});
	}

	
	public List getThirdCorrelationList(
			short category,
			short value1,
			short value2,
			Class codeTable)
			throws DataAccessException {
			
			return find(
				" select distinct ct from "
					+ codeTable.getName()
					+ " as ct, BibliographicCorrelation as bc "
					+ " where bc.key.marcTagCategoryCode = ? and "
					+ " bc.key.marcFirstIndicator <> '@' and "
					+ " bc.key.marcSecondIndicator <> '@' and "
					+ " bc.databaseFirstValue = ? and "
					+ " bc.databaseSecondValue = ? and "					
					+ " bc.databaseThirdValue = ct.code and "
					+ " ct.obsoleteIndicator = 0  order by ct.sequence ",
				new Object[] {
					new Short(category),
					new Short(value1),
					new Short(value2)},
				new Type[] { Hibernate.SHORT, Hibernate.SHORT, Hibernate.SHORT });
		}

	public short getFirstAllowedValue2(
			short category,
			short value1,
			short value3)
			throws DataAccessException {
			List l = find(
				" from BibliographicCorrelation as bc "
					+ " where bc.key.marcTagCategoryCode = ? and "
					+ " bc.key.marcFirstIndicator <> '@' and "
					+ " bc.key.marcSecondIndicator <> '@' and "
					+ " bc.databaseFirstValue = ? and "
					+ " bc.databaseThirdValue = ? ",
				new Object[] {
					new Short(category),
					new Short(value1),
					new Short(value3)},
				new Type[] { Hibernate.SHORT, Hibernate.SHORT, Hibernate.SHORT });
			
			if (l.size() > 0) {
				//MAURA
				//return ((BibliographicCorrelation)l.get(0)).getDatabaseSecondValue();
				return -1;
			}
			else {
				return -1;
			}
		}

	public String getClassificationIndexByShelfType(short shelfType)
		throws DataAccessException {
		List l =
			find(
				"from BibliographicCorrelation as bc "
					+ " where bc.key.marcTagCategoryCode = 13 and "
					+ " bc.databaseFirstValue = ? ",
				new Object[] { new Short(shelfType)},
				new Type[] { Hibernate.SHORT });
		if (l.size() == 1) {
			String s = ((Correlation) l.get(0)).getSearchIndexTypeCode();
			return new DAOIndexList().getIndexByEnglishAbreviation(s);
		} else {
			return null;
		}
	}

	/*modifica barbara 13/04/2007 PRN 127 - nuova intestazione su lista vuota default maschera inserimento intestazione nome*/
	public CorrelationKey getMarcTagCodeBySelectedIndex(String selectedIndex)
	throws DataAccessException {
	List l =
		find(
			"from BibliographicCorrelation as bc "
				+ " where bc.searchIndexTypeCode = ?" 
				+" or bc.searchIndexTypeCode = ?" ,
	new Object[] { new String(selectedIndex.substring(0, 2)),
				new String(selectedIndex.substring(0, 2).toLowerCase())},
	new Type[] { Hibernate.STRING,  Hibernate.STRING});
	
	if(l.size()>0)
		return ((Correlation) l.get(0)).getKey();
	else	
		return null;
	}






}

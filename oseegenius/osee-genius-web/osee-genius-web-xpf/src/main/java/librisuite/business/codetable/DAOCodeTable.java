/*
 * Created on Oct 12, 2004
 *
 */
package librisuite.business.codetable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.descriptor.Descriptor;
import librisuite.hibernate.CLSTN;
import librisuite.hibernate.CodeTable;
import librisuite.hibernate.SBJCT_HDG;
import librisuite.hibernate.T_LANG;
import librisuite.hibernate.T_LANG_OF_ACS_PNT;
import librisuite.hibernate.T_LANG_OF_ACS_PNT_SBJCT;
import librisuite.hibernate.T_LANG_OF_IDXG;
import librisuite.hibernate.T_LANG_OF_IDXG_LANG;
import librisuite.hibernate.T_SINGLE;
import librisuite.hibernate.T_SINGLE_CHAR;
import librisuite.hibernate.T_SINGLE_LONGCHAR;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.libricore.librisuite.common.HibernateUtil;
import com.libricore.librisuite.common.TransactionalHibernateOperation;
import com.neovisionaries.i18n.LanguageAlpha3Code;

/**
 * Class comment
 * @author janick
 */
public class DAOCodeTable extends HibernateUtil {
	/**
	 * Standard Amicus increment for CodeTables sequence column
	 */
	public static final int STEP = 10;
	
	private static final String ALPHABETICAL_ORDER = " order by ct.longText ";
	private static final String SEQUENCE_ORDER = " order by ct.sequence ";
	private String defaultListOrder = Defaults.getBoolean("labels.alphabetical.order", true)?ALPHABETICAL_ORDER:SEQUENCE_ORDER;

	private static Log logger = LogFactory.getLog(DAOCodeTable.class);
	
	
	public List getList(Class c) throws DataAccessException {
		List listCodeTable = null;

		try {
			Session s = currentSession();
			listCodeTable =
				s.find(
					"from "
						+ c.getName()
						+ " as ct "
						+ " order by ct.sequence ");
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		logger.debug("Got codetable for " + c.getName());
		return listCodeTable;
	}
 
	public List getList(Class c, boolean alphabeticOrder) throws DataAccessException {
		List listCodeTable = null;
		
		String order = SEQUENCE_ORDER;
		if (alphabeticOrder)
			order = ALPHABETICAL_ORDER;
		try {
			Session s = currentSession();
			listCodeTable =
				s.find(
					"from "
						+ c.getName()
						+ " as ct "
						/*barbara modifica 27/02/2007 non visualizzo nella lista gli obsoleti
						 * PRN 041*/
						+ " where ct.obsoleteIndicator = 0"
						+ order);
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		logger.debug("Got codetable for " + c.getName());
		return listCodeTable;
	}
	
	public List getCorrelatedList(Class c, boolean alphabeticOrder, String filtro) throws DataAccessException {
		List listCodeTable = null;
		
		String order = SEQUENCE_ORDER;
		if (alphabeticOrder)
			order = ALPHABETICAL_ORDER;
		try {
			Session s = currentSession();
			listCodeTable =
				s.find("select distinct ct from "
						+ c.getName()
						+ " as ct, BibliographicCorrelation as bc "
						/*barbara modifica 27/02/2007 non visualizzo nella lista gli obsoleti
						 * PRN 041*/
						+ " where ct.obsoleteIndicator = 0"
						+ filtro
						+ order);
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		logger.debug("Got codetable for " + c.getName());
		return listCodeTable;
	}
	
	public List getListCntrl(Class c, boolean alphabeticOrder) throws DataAccessException {
		List listCodeTable = null;
		
		String order = SEQUENCE_ORDER;
		if (alphabeticOrder)
			order = ALPHABETICAL_ORDER;
		
		try {
			Session s = currentSession();
			listCodeTable =
				s.find(
					"from "
						+ c.getName()
						+ " as ct "
						+ " where ct.obsoleteIndicator = 0"
						+" and (ct.code<>94 and ct.code<>102 and ct.code<>103)"
						+ order);
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		logger.debug("Got codetable for " + c.getName());
		return listCodeTable;
	}
	
	public List getListFromTag008(Class c,String tag, boolean alphabeticOrder) throws DataAccessException {
		List listCodeTable = null;
		
		String order = SEQUENCE_ORDER;
		if (alphabeticOrder)
			order = ALPHABETICAL_ORDER;
		
		try {
			Session s = currentSession();
			listCodeTable =
				s.find(
					"from "
						+ c.getName()
						+ " as ct "
						+ " where ct.obsoleteIndicator = 0"
						+ " and ct.shortText like '%008%'"
						+ order);
			     /*new Object[] {
					new String(tag)},
				  new Type[] {
					Hibernate.STRING});*/

		} catch (HibernateException e) {
			logAndWrap(e);
		}
	
		return listCodeTable;
	}
	
	/*T_BIB_HDR*/
	public List getListFromWithoutTag008(Class c,String tag, boolean alphabeticOrder) throws DataAccessException {
		List listCodeTable = null;
		
		String order = SEQUENCE_ORDER;
		if (alphabeticOrder)
			order = ALPHABETICAL_ORDER;
		
		try {
			Session s = currentSession();
			listCodeTable =
				s.find(
					"from "
						+ c.getName()
						+ " as ct "
						+ " where ct.obsoleteIndicator = 0"
						+ " and ct.code <> '31' "
						+ " and ct.code <> '32' "
						+ " and ct.code <> '33'"
						+ " and ct.code <> '34' "
						+ " and ct.code <> '35' "
						+ " and ct.code <> '36' "
						+ " and ct.code <> '37' "
						+ " and ct.code <> '15' "
						+ " and ct.code <> '39' "
						+ " and ct.code <> '41' "
						+ order);
			     /*new Object[] {
					new String(tag)},
				  new Type[] {
					Hibernate.STRING});*/

		} catch (HibernateException e) {
			logAndWrap(e);
		}
	
		return listCodeTable;
	}



	public List getList(Class c, Locale locale) throws DataAccessException {
		List listCodeTable = null;

		try {
			Session s = currentSession();
			listCodeTable =
				s.find(
					"from "
						+ c.getName()
						+ " as ct "
						+ " where ct.language = ?"
						+" and ct.obsoleteIndicator = '0'"
						+ defaultListOrder,
					new Object[] { LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString()},
					new Type[] { Hibernate.STRING });
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		logger.debug("Got codetable for " + c.getName());
		return listCodeTable;
	}

	public List getListOrderAlphab(Class c, Locale locale)
		throws DataAccessException {
		List listCodeTable = null;
		try {
			Session s = currentSession();
			listCodeTable =
				s.find(
					"from "
						+ c.getName()
						+ " as ct "
						+ ALPHABETICAL_ORDER);

		} catch (HibernateException e) {
			logAndWrap(e);
		}

		logger.debug("Got codetable for " + c.getName());
		return listCodeTable;
	}

	public static List asOptionList(List raw, Locale locale) {
		if (raw == null) {
			return null;
		}

		List result = new ArrayList();
		Iterator iterator = raw.iterator();

		while (iterator.hasNext()) {
			CodeTable element = (CodeTable) iterator.next();
			if (element.getLanguage().equals(LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString())) {
				result.add(
					new ValueLabelElement(
						element.getCodeString(),
						element.getLongText()));
			}
		}
		return result;
	}
	
	public static List asShortOptionList(List raw, Locale locale) {
		if (raw == null) {
			return null;
		}

		List result = new ArrayList();
		Iterator iterator = raw.iterator();

		while (iterator.hasNext()) {
			CodeTable element = (CodeTable) iterator.next();
			if (element.getLanguage().equals(LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString())) {
				result.add(
					new ValueLabelElement(
						element.getCodeString(),
						element.getShortText()));
			}
		}
		return result;
	}

	/**
	** Return a list with the fields I need to fill the newCopy.jsp�s checkboxes
	** the parameter class is the name of the code table I need	
	 * @since 1.0
	 */
	public List getOptionList(Class c, Locale locale)
		throws DataAccessException {
		return asOptionList(getList(c, locale), locale);
	}
	/**
	** Return a list with the fields I need to fill the newCopy.jsp�s checkboxes
	** the parameter class is the name of the code table I need	
	 * @since 1.0
	 */
  public List getShortOptionList(Class c, Locale locale)
		throws DataAccessException {
		return asShortOptionList(getList(c, locale), locale);
	}
	public List getOptionListOrderAlphab(Class c, Locale locale)
		throws DataAccessException {
		return asOptionList(getListOrderAlphab(c, locale), locale);
	}

	public static T_SINGLE getSelectedCodeTable(List raw, Locale locale, short code) {
		if (raw == null) {
			return null;
		}

		Iterator iterator = raw.iterator();

		while (iterator.hasNext()) {
			T_SINGLE element = (T_SINGLE) iterator.next();
			if (element.getLanguage().equals(LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString())
					&& element.getCode() == code) {
				return element;
			}
		}
		return null;
	}

	public T_SINGLE_CHAR load(Class c, char code, Locale locale)
		throws DataAccessException {
		T_SINGLE_CHAR key;
		try {
			key = (T_SINGLE_CHAR) c.newInstance();
			key.setCode(code);
			key.setLanguage(LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString());
		} catch (Exception e) {
			throw new RuntimeException("unable to create code table object");
		}
		return (T_SINGLE_CHAR) loadCodeTableEntry(c, key);
	}

	public T_SINGLE load(Class c, short code, Locale locale)
		throws DataAccessException {
		T_SINGLE key;
		try {
			key = (T_SINGLE) c.newInstance();
			key.setCode(code);
			key.setLanguage(LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString());
		} catch (Exception e) {
			throw new RuntimeException("unable to create code table object");
		}
		return (T_SINGLE) loadCodeTableEntry(c, key);
	}

	private CodeTable loadCodeTableEntry(Class c, Serializable ser)
		throws DataAccessException {

		CodeTable result = (CodeTable) get(c, ser);
		
		try {
			logger.debug(
				"Got codetable entry for "
					+ c.getName()
					+ " ( value='"
					+ result.getCodeString()
					+ "', text='"
					+ result.getLongText()
					+ "')");
		}
		catch (Exception e){ //do nothing
			
		}
		return result;
	}
	public String getLongText(char code, Class c, Locale locale)
		throws DataAccessException {
		String result = new String("");
		CodeTable ct = (CodeTable) load(c, code, locale);
		result = ct.getLongText();
		return result;
	}
	public String getLongText(short code, Class c, Locale locale)
	throws DataAccessException {
	String result = new String("");
	CodeTable ct = (CodeTable) load(c, code, locale);
	result = ct.getLongText();
	return result;
}
	public String getLongText(String code, Class c, Locale locale)
	throws DataAccessException {
	String result = new String("");
	CodeTable ct = (CodeTable) load(c, code, locale);
	result = ct.getLongText();
	return result;
}
	public String getTranslationString(long translationKey, Locale locale)
		throws DataAccessException {
		List l =
			find(
				"select t.text "
					+ " from T_TRLTN as t, T_LANG as l "
					+ " where t.stringNumber = ? and t.languageNumber = l.sequence / 10 "
					+ " and l.code = ? ",
				new Object[] {
					new Long(translationKey),
					LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString()},
				new Type[] { Hibernate.LONG, Hibernate.STRING });
		if (l.size() > 0) {
			return (String) l.get(0);
		} else {
			return null;
		}
	}

	/*
	 * This doesn't seem to be doing anything very useful
	 * and it is not currently referenced.  At one time
	 * it was so leave it for now
	 */
	public String getLanguageOfIndexing(int code) throws DataAccessException {
		String result = "und";
		List scriptingLanguage = null;
		int valueCode = 0;
		try {
			Session s = currentSession();
			scriptingLanguage =
				s.find(
					"from T_LANG_OF_IDXG as t where t.code = '" + code + "'");
			Iterator iter = scriptingLanguage.iterator();
			while (iter.hasNext()) {

				T_LANG_OF_IDXG rawElmt = (T_LANG_OF_IDXG) iter.next();
				valueCode = rawElmt.getSequence();
			}
			scriptingLanguage =
				s.find(
					"from T_LANG_OF_IDXG_LANG as t where t.languageIndexing = "
						+ valueCode);
			iter = scriptingLanguage.iterator();
			while (iter.hasNext()) {

				T_LANG_OF_IDXG_LANG rawElmt = (T_LANG_OF_IDXG_LANG) iter.next();
				valueCode = rawElmt.getLanguage();
			}
			scriptingLanguage =
				s.find("from T_LANG as t where t.sequence = " + valueCode);
			iter = scriptingLanguage.iterator();
			while (iter.hasNext()) {

				T_LANG rawElmt = (T_LANG) iter.next();
				result = rawElmt.getCode();
			}
		} catch (HibernateException e) {
			logAndWrap(e);
		}

		return result;
	}
	
	public String getAccessPointLanguage(int code, Descriptor aDescriptor)
			throws DataAccessException {
		String result = "und";
		List scriptingLanguage = null;
		int valueCode = 0;
		Iterator iter = null;
		try {
			Session s = currentSession();
			if (aDescriptor instanceof SBJCT_HDG) {
				scriptingLanguage = s
						.find("from T_LANG_OF_ACS_PNT_SBJCT as t where t.code = '"
								+ code + "'");
				iter = scriptingLanguage.iterator();
				while (iter.hasNext()) {

					T_LANG_OF_ACS_PNT_SBJCT rawElmt = (T_LANG_OF_ACS_PNT_SBJCT) iter
							.next();
					valueCode = rawElmt.getSequence();
				}
			} else {
				scriptingLanguage = s
						.find("from T_LANG_OF_ACS_PNT as t where t.code = '"
								+ code + "'");

				iter = scriptingLanguage.iterator();
				while (iter.hasNext()) {

					T_LANG_OF_ACS_PNT rawElmt = (T_LANG_OF_ACS_PNT) iter.next();
					valueCode = rawElmt.getSequence();
				}
			}
			scriptingLanguage = s
					.find("from T_LANG_OF_IDXG_LANG as t where t.languageIndexing = "
							+ valueCode);
			iter = scriptingLanguage.iterator();
			while (iter.hasNext()) {

				T_LANG_OF_IDXG_LANG rawElmt = (T_LANG_OF_IDXG_LANG) iter.next();
				valueCode = rawElmt.getLanguage();
			}
			scriptingLanguage = s.find("from T_LANG as t where t.sequence = "
					+ valueCode);
			iter = scriptingLanguage.iterator();
			while (iter.hasNext()) {

				T_LANG rawElmt = (T_LANG) iter.next();
				result = rawElmt.getCode();
			}
		} catch (HibernateException e) {
			logAndWrap(e);
		}

		return result;
	}
	
	public List getEntries(Class c, Object code, boolean alphabeticOrder) throws DataAccessException, InstantiationException, IllegalAccessException {
		List listCodeTable = null;
		
		String order = SEQUENCE_ORDER;
		if (alphabeticOrder)
			order = ALPHABETICAL_ORDER;
		try {
			Session s = currentSession();
			listCodeTable =
				s.find(
					"from "
						+ c.getName()
						+ " as ct "
						+ " where ct.obsoleteIndicator = 0"
						+ " and ct.code = "+getCorrectValue(c,code) + " "
						+ order);
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		logger.debug("Got codetable for " + c.getName());
		return listCodeTable;
	}

	private String getCorrectValue(Class type, Object code) throws InstantiationException, IllegalAccessException {
		Object obj = type.newInstance();
		if (obj instanceof T_SINGLE_CHAR || obj instanceof T_SINGLE_LONGCHAR) {
			return "'" + code  + "'";
		} else return "" + code;
	}

	/**
	 * Save a group of rows in the same code table. If it is an insertion then the
	 * sequence is calculated the same for all items present in the list
	 * Be carefull to do not mixed CodeTables in items list
	 * @param items
	 * @throws DataAccessException
	 */
	public void save(final List/*<CodeTable>*/ items) throws DataAccessException {
		new TransactionalHibernateOperation() {
			public void doInHibernateTransaction(Session s)
				throws HibernateException, DataAccessException {
					if(items==null || items.isEmpty()) {
						return;
					}
					int sequence = suggestNewSequence((CodeTable)items.get(0));
					Iterator it = items.iterator();
					while (it.hasNext()) {
						CodeTable nextCodeTable = (CodeTable)it.next();
						if(nextCodeTable.isNew()){
							nextCodeTable.setSequence(sequence);
						}
						persistByStatus(nextCodeTable);
					}
			}
		}.execute();
	}

	/**
	 * Symply returns the calculated new sequence (MAX sequence + 1)
	 * @param codeTable
	 * @return
	 * @throws DataAccessException
	 */
	public synchronized int suggestSequence(CodeTable codeTable) throws DataAccessException {
		try {
			Session s = currentSession();
			Query q = s.createQuery("select ct.sequence from "
									+ codeTable.getClass().getName()
									+ " as ct order by ct.sequence desc");
			q.setMaxResults(1);
			List results = q.list();
			int newSequence = STEP; // default for empty table
			if(results.size()>0) {
				newSequence = ((Integer)results.get(0)).intValue() + STEP ;
			}
			return newSequence;	
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		/* pratically unreachable because logAndWrap throws an exception everytime */
		return STEP;
	}
	
	public synchronized int suggestNewSequence(CodeTable codeTable) throws DataAccessException {
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct.sequence from "
									+ codeTable.getClass().getName()
									+ " as ct order by ct.sequence desc");
			q.setMaxResults(1);
			List results = q.list();
			int newSequence = STEP; // default for empty table
			if(results.size()>0) {
				newSequence = ((Integer)results.get(0)).intValue() + STEP ;
			}
			return newSequence;	
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		/* pratically unreachable because logAndWrap throws an exception everytime */
		return STEP;
	}
	public synchronized int suggestNewCode(CodeTable codeTable) throws DataAccessException {
		int newCode = 0; 
		try {
			Session s = currentSession();
			Query q = s.createQuery("select ct.code from "
									+ codeTable.getClass().getName()
									+ " as ct order by ct.code desc");
			q.setMaxResults(1);
			List results = q.list();
			// default for empty table
			if(results.size()>0) {
				newCode = ((Short)results.get(0)).intValue() + 1 ;
			}
		
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		/* pratically unreachable because logAndWrap throws an exception everytime */
		return newCode;
	}
	
	public List getList(Class c, Locale locale, boolean alphabeticOrder) throws DataAccessException {
		List listCodeTable = null;
		String order = SEQUENCE_ORDER;
		if (alphabeticOrder)
			order = ALPHABETICAL_ORDER;

		try {
			Session s = currentSession();
			listCodeTable =
				s.find(
					"from "
						+ c.getName()
						+ " as ct "
						+ " where ct.language = ?"
						/*Carmen modifica 01/02/2008 non devo visualizzare nella lista gli obsoleti*/
						+" and ct.obsoleteIndicator = 0"
						+ order,
					new Object[] { LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString()},
					new Type[] { Hibernate.STRING });
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		logger.debug("Got codetable for " + c.getName());
		return listCodeTable;
	}

	
	public boolean findHeading(String code)
	throws DataAccessException {
		
	try {
		Session s = currentSession();
		List l = s.createCriteria(CLSTN.class)
		.add( Expression.like("stringText", "%"+code+"%") )
        .add( Expression.eq("typeCode", new Short((short)29)))
		.list();

		if (l.size() >= 1) {
			return true;
		} else {
			return false;
		}
	} catch (HibernateException e) {
		logAndWrap(e);
		return false;
	}
}

	

public void deleteCode(Class c,String code) throws DataAccessException {
	Session s = currentSession();
	 Transaction tx = null;
	try{	
		 tx = s.beginTransaction();
			s.delete( 
					 "from " + 
					  c.getName()
					+ " as ct "
					+ " where ct.code = ?",
					new Object[] {code},
					new Type[] {Hibernate.STRING });
			 tx.commit();
    	
	} catch (HibernateException e) {
		 if (tx != null) {

             try {
                 tx.rollback();
                
             } catch (HibernateException e1) {
            	 logAndWrap(e);
             }
         }
		
	}

}
public void deleteNoteSubCode(String code) throws DataAccessException {
	Session s = currentSession();
	 Transaction tx = null;
	try{	
		 tx = s.beginTransaction();
			s.delete( 
					 "from T_CAS_STND_NTE_SUB_TYP "  
					+ " as ct "
					+ " where ct.sequence = ?",
					new Object[] {code},
					new Type[] {Hibernate.STRING });
			 tx.commit();
    	
	} catch (HibernateException e) {
		 if (tx != null) {

             try {
                 tx.rollback();
                
             } catch (HibernateException e1) {
            	 logAndWrap(e);
             }
         }
		
	}

}
public  int getCodeNote(int code) throws DataAccessException {
	int newCode = 0;
	try {
		Session s = currentSession();
		Query q = s.createQuery("select distinct ct.code "
				                +" from T_CAS_STND_NTE_SUB_TYP as ct "
								+ " where ct.sequence ="+code);
		q.setMaxResults(1);
		List results = q.list();
		if(results.size()>0) {
			newCode = ((Integer)results.get(0)).intValue();
	    }
		
	} catch (HibernateException e) {
		logAndWrap(e);
	}
	
	return newCode;
}

public  String getCodeCache(String code) throws DataAccessException {
	String newCode = "";
	try {
		Session s = currentSession();
		Query q = s.createQuery("select distinct ct.levelCard "
				                +" from CasCache as ct  "
								+ " where ct.levelCard =:code");
		q.setString("code", code);
		q.setMaxResults(1);
		List results = q.list();
		if(results.size()>0) {
			newCode = (String)results.get(0);
	    }
		
	} catch (HibernateException e) {
		logAndWrap(e);
	}
	
	return newCode;
}
public List getCodeNoteList(int code) throws DataAccessException {
	List result=null;
	try {
		Session s = currentSession();
		Query q = s.createQuery("select distinct ct"
				                +" from T_CAS_STND_NTE_SUB_TYP as ct "
								+ " where ct.sequence ="+code);
		q.setMaxResults(1);
		result = q.list();
	
	} catch (HibernateException e) {
		logAndWrap(e);
	}
	return result;
}
public List getMasterClient(int code) throws DataAccessException {
	List result=null;
	try {
		Session s = currentSession();
		Query q = s.createQuery("select distinct ct"
				                +" from CLCTN_MST_CSTMR as ct "
								+ " where ct.collectionCode ="+code);
		q.setMaxResults(1);
		result = q.list();
	
	} catch (HibernateException e) {
		logAndWrap(e);
	}
	return result;
}

public  List getFirstCorrelationListFilter(Class c, boolean alphabeticOrder,short rangTag) throws DataAccessException {
	List listCodeTable = null;
	String rangeTagFrom="";
	short rangeTagTo=0;
	//Only Tag 0XX
	if(rangTag==10)
	  rangeTagFrom="010";
	else
	 rangeTagFrom=""+rangTag;
	//Delta of 99 tag
	if(rangTag!=0)
		 rangeTagTo = (short) (rangTag+99);
	//Filters
	String filtro=" and bc.key.marcSecondIndicator <> '@' and bc.databaseFirstValue = ct.code ";
	String filterTag=" and bc.key.marcTag between '" +rangeTagFrom+ "' and '"+rangeTagTo+"'";
	if(rangTag==0)
		filterTag="";
	String order = SEQUENCE_ORDER;
	if (alphabeticOrder)
		order = ALPHABETICAL_ORDER;
	try {
		Session s = currentSession();
		listCodeTable =
			s.find("select distinct ct from "
					+ c.getName()
					+ " as ct, BibliographicCorrelation as bc "
					+ " where ct.obsoleteIndicator = 0 and bc.key.marcTagCategoryCode= 7"
					+ filterTag
					+ filtro
					+ order);
	} catch (HibernateException e) {
		logAndWrap(e);
	}
	logger.debug("Got codetable for " + c.getName());
	return listCodeTable;
}

	public  List getDiacritici() throws DataAccessException 
	{
//		String campi = " idCarattere, setCarattere, font, carattere, nomeCarattere, codiceUnicode, codiceUtf8 ";
		List listCodeTable = null;
		
		try {
			Session s = currentSession();
//			listCodeTable = s.find("select" + campi + "from Diacritici order by id_carattere");
//			listCodeTable = s.find("from Diacritici as a where a.idCarattere < 444 or a.idCarattere > 526 order by 1");
//			listCodeTable = s.find("from Diacritici as a where a.idCarattere > 443 order by 1");
			listCodeTable = s.find("from Diacritici as a order by 1");
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		logger.debug("Got codetable for Diacritici");
		
		return listCodeTable;
	}
	
	public List getNoteTranslation(String code2, String language) throws DataAccessException {
		List result=null;
		int code = new Integer(code2).intValue();
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct"
					                +" from T_TRSLTN_NTE_TYP as ct "
									+ " where ct.code = "+ code+" and ct.language="+"'"+language+"'");
			q.setMaxResults(1);
			result = q.list();
		
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	
	public List getNoteTranslationLanguage(String language) throws DataAccessException {
		List result=null;
		
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct"
					                +" from T_TRSLTN_NTE_TYP as ct "
									+ " where ct.language="+"'"+language+"'");
			q.setMaxResults(1);
			result = q.list();
		
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	
	
	public List getDefaultNoteTranslation(String code2, String language) throws DataAccessException {
		List result=null;
		int code = new Integer(code2).intValue();
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct"
					                +" from T_DFLT_TRSLTN_NTE as ct "
									+ " where ct.code = "+code+" and ct.language="+"'"+language+"'");
			q.setMaxResults(1);
			result = q.list();
		
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	public List getDefaultNoteTranslationLanguage(String language) throws DataAccessException {
		List result=null;
		
		try {
			Session s = currentSession();
			Query q = s.createQuery("select distinct ct"
					                +" from T_DFLT_TRSLTN_NTE as ct "
									+ " where  ct.language="+"'"+language+"'");
			q.setMaxResults(1);
			result = q.list();
		
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return result;
	}
	public T_SINGLE_LONGCHAR load(Class c, String code, Locale locale) throws DataAccessException {
		T_SINGLE_LONGCHAR key;
		try {
			key = (T_SINGLE_LONGCHAR) c.newInstance();
			key.setCode(code);
			key.setLanguage(LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString());
		} catch (Exception e) {
			throw new RuntimeException("unable to create code table object");
		}
		return (T_SINGLE_LONGCHAR) loadCodeTableEntry(c, key);
	}
	public List getCorrelationListFromSuggest(
			short category,
			String marcTag,
			Class codeTable)
			throws DataAccessException {

			return find(
				" select distinct ct from "
					+ codeTable.getName()
					+ " as ct, BibliographicCorrelation as bc "
					+ " where bc.key.marcTagCategoryCode = ? and "
//					Natascia 13/06/2007: scommentate chiocciole
					+ " bc.key.marcFirstIndicator <> '@' and "
					+ " bc.key.marcSecondIndicator <> '@' and "
					+ " bc.key.marcTag = ? and "
					+ " bc.databaseFirstValue = ct.code and  "
					/*barbara modifica 27/02/2007 non visualizzo nella lista gli obsoleti
					 * PRN 041*/
					+ "ct.obsoleteIndicator = 0  order by ct.sequence ",
				new Object[] { new Short(category), new String(marcTag)},
				new Type[] { Hibernate.SHORT, Hibernate.STRING });
	}
	/**
	 * Metodo che mi serve per leggere lo short text della tabella T_CAS_DIG_TYP 
	 * in cui trovo la directory di riferimento per il repository digitale 
	 */	
	public String getShortText(String code, Class c, Locale locale) throws DataAccessException 
	{
		String result = new String("");
		CodeTable ct = (CodeTable) load(c, code, locale);
		result = ct.getShortText();
		return result;
	}
	public String getShortText(short code, Class c, Locale locale) throws DataAccessException 
	{
		String result = new String("");
		CodeTable ct = (CodeTable) load(c, code, locale);
		result = ct.getShortText();
		return result;
	}
}

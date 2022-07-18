/*
 * (c) LibriCore
 * 
 * Created on Jul 20, 2004
 * 
 * DAOIndexList.java
 */
package librisuite.business.searching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import librisuite.business.codetable.IndexListElement;
import librisuite.business.common.DataAccessException;
import librisuite.business.descriptor.SortFormParameters;
import librisuite.hibernate.IndexList;
import librisuite.hibernate.IndexListKey;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.libricore.librisuite.common.HibernateUtil;
import com.neovisionaries.i18n.LanguageAlpha3Code;

/**
 * Provides data access to IDX_LIST table
 * @author paulm
 * @version %I%, %G%
 * @since 1.0
 */
public class DAOIndexList extends HibernateUtil {
	private static final Log logger = LogFactory.getLog(DAOIndexList.class);

	public List getBrowseIndex(Locale locale) throws DataAccessException {

		List result = new ArrayList();
		String language =  LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString();
		
		String query =
			"from IndexList as a "
				+ "where SUBSTR(a.browseCode, 1, 1) = 'B' "
				+ "and a.key.language = '"
				+ language
				//+ locale.getISO3Language()
				/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
				+ "' and a.codeLibriCatMades = 'LC'" 
				+ " order by a.languageDescription";

		return getIndexByQuery(query);
	}

	public List getEditorBrowseIndex(Locale locale) throws DataAccessException 
	{
//--> La tendina deve visualizzare solo NOME EDITORE e LUOGO PUBBLICAZIONE
		List result = new ArrayList();

		String query =
			"from IndexList as a "
				+ "where SUBSTR(a.browseCode, 0, 1) = 'B' "
				+ "and a.key.language = '"
				+ LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString()
				/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
				+ "' and a.codeLibriCatMades = 'LC'"
				+ "  and a.key.keyNumber in (230, 243)"
				+ " order by a.languageDescription";

		return getIndexByQuery(query);
	}
	
	public List getBrowseIndexPublisher(Locale locale) throws DataAccessException {

		List result = new ArrayList();

		String query =
			"from IndexList as a "
				+ "where SUBSTR(a.browseCode, 0, 1) = 'B' "
				+ " and (a.languageCode = 'PP'" 
				+ " or a.languageCode = 'PU')" 
				+ "and a.key.language = '"
				+ LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString()
				/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
				+ "' and a.codeLibriCatMades = 'LC'" 
				+ " order by a.languageDescription";

		return getIndexByQuery(query);
	}
	public List getPrimaryIndex(Locale locale) throws DataAccessException {

		List result = new ArrayList();

		String query =
			"from IndexList as a "
				+ "where SUBSTR(a.key.typeCode, 0, 1) = 'P' "
				+ "and a.key.language = '"
				+ LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString()
				/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
				+ "' and a.codeLibriCatMades = 'LC'";
		return getIndexByQuery(query);
	}

	public List getSecondaryIndex(Locale locale) throws DataAccessException {

		List result = new ArrayList();

		String query =
			"from IndexList as a "
				+ "where SUBSTR(a.key.typeCode, 0, 1) = 'S' "
				+ " and a.key.language = '"
				+ LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString()
				/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
				+ "' and a.codeLibriCatMades = 'LC'";

		return getIndexByQuery(query);
	}

	public String getIndexBySortFormType(short mainType, short subType)
		throws DataAccessException {

		String query =
			"from IndexList as a "
				+ "where a.sortFormMainTypeCode = "
				+ mainType
				+ " and a.sortFormSubTypeCode = "
				+ subType
				+ " and a.key.language = '"
				+ LanguageAlpha3Code.getByCode(Locale.ENGLISH.getLanguage()).getAlpha3B().toString()
				/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
				+ "' and a.codeLibriCatMades = 'LC'";

		List l = getIndexByQuery(query);
		if (l.size() > 0) {
			return ((IndexListElement) l.get(0)).getKey();
		} else {
			return null;
		}
	}

	public String getIndexByEnglishAbreviation(String s)
		throws DataAccessException {

		String query =
			"from IndexList as a "
				+ "where a.languageCode = "
				+ s
				+ " and a.key.language = '"
				+ LanguageAlpha3Code.getByCode(Locale.ENGLISH.getLanguage()).getAlpha3B().toString()
				/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
				+ "' and a.codeLibriCatMades = 'LC'";

		List l = getIndexByQuery(query);
		if (l.size() > 0) {
			return ((IndexListElement) l.get(0)).getKey();
		} else {
			return null;
		}
	}

	public String getLocalizedIndexByKey(String key, Locale locale)
		throws DataAccessException {
		IndexListKey ilk = new IndexListKey(key);
		String query =
			"from IndexList as a "
				+ "where a.key.keyNumber = "
				+ ilk.getKeyNumber()
				+ " and a.key.typeCode = "
				+ "'" + ilk.getTypeCode() + "'"
				+ " and a.key.language = "
				+ "'" + LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString() + "'"
				/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
				+ " and a.codeLibriCatMades = 'LC'";

		List l = getIndexByQuery(query);
		if (l.size() > 0) {
			return ((IndexListElement) l.get(0)).getValue();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * Get the IndexElementList for a expecific query
	 * 
	 * @param query
	 * @param locale
	 * @throws DataAccessException
	 * @since 1.0
	 */
	public List getIndexByQuery(String query) throws DataAccessException {
		List l = null;
		List result = new ArrayList();
		Session s = currentSession();

		if (logger.isDebugEnabled()) {
			logger.debug("Doing query: " + query);
		}
		try {
			l = s.find(query);
		} catch (HibernateException e) {
			logAndWrap(e);
		}

		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			IndexList aRow = (IndexList) iter.next();

			result.add(
				new IndexListElement(
					aRow.getLanguageCode(),
					aRow.getLanguageDescription(),
					""
						+ aRow.getKey().getKeyNumber()
						+ aRow.getKey().getTypeCode().trim()));

		}
		return result;
	}

	public String getCodeTable(String key, Locale locale)
		throws DataAccessException {
		List codeTableList = new ArrayList();
		String codeTable = new String("");
		String query = new String();

		query =
			"select a.codeTableName from IndexList as a "
				+ "where a.languageCode = '"
				+ key
				+ "'"
				+ " and a.key.language = '"
				+ LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString()
				/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
				+ "' and a.codeLibriCatMades = 'LC'";

		try {
			codeTableList = currentSession().find(query);

			if (codeTableList.size() > 0) {
				if (codeTableList.get(0) != null) {
					codeTable = codeTableList.get(0).toString();
				}
			}

		} catch (HibernateException e) {
			logAndWrap(e);
		} catch (DataAccessException ae) {
			logAndWrap(ae);
		}

		return codeTable;
	}

	public String getCodeTable(String key) throws DataAccessException {
	List codeTableList = new ArrayList();
	String codeTable = new String("");
	String query = new String();

	query =
		"select a.codeTableName from IndexList as a "
			+ "where a.languageCode = '"
			+ key
			/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
			+ "' and a.codeLibriCatMades = 'LC'";

	try {
		codeTableList = currentSession().find(query);

		if (codeTableList.size() > 0) {
			if (codeTableList.get(0) != null) {
				codeTable = codeTableList.get(0).toString();
			}
		}

	} catch (HibernateException e) {
		logAndWrap(e);
	} catch (DataAccessException ae) {
		logAndWrap(ae);
	}

	return codeTable;
}

	
	public SortFormParameters getSortFormParametersByKey(String indexKey)
		throws DataAccessException {
		SortFormParameters result = null;
		IndexListKey ilk = new IndexListKey(indexKey);
		List l =
			find(
				"from IndexList as t where t.key.keyNumber = ? "
					+ " and trim(t.key.typeCode) = ? "
					/*modifica Barbara 26/04/2007 - nella lista degli indici solo indici LC*/
					+ " and t.codeLibriCatMades = 'LC'"
					+ " and t.key.language = ? ",
				new Object[] {
					new Integer(ilk.getKeyNumber()),
					ilk.getTypeCode(),
					ilk.getLanguage()},
				new Type[] {
					Hibernate.INTEGER,
					Hibernate.STRING,
					Hibernate.STRING });
		if (l.size() > 0) {
			IndexList i = (IndexList) l.get(0);
			result =
				new SortFormParameters(
					i.getSortFormMainTypeCode(),
					i.getSortFormSubTypeCode(),
					i.getSortFormTypeCode(),
					i.getSortFormFunctionCode(),
					i.getSortFormSkipInFiling());
		}
		return result;
	}
}
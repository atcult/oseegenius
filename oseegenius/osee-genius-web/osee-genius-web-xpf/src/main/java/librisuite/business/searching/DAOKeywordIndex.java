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

import librisuite.business.codetable.ValueLabelElement;
import librisuite.business.common.DataAccessException;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import com.libricore.librisuite.common.HibernateUtil;
import com.neovisionaries.i18n.LanguageAlpha3Code;

/**
 * Provides data access to T_SRCH_KYWRD_IDX table
 * @author hansv
 * @version %I%, %G%
 * @since 1.0
 */
public class DAOKeywordIndex extends HibernateUtil {
	public List getKeywordIndex(Locale locale) throws DataAccessException {
		List l = null;
		List result = new ArrayList();
		Session s = currentSession();

		try {
			l =
				s.find(
					"from T_SRCH_KYWRD_IDX as a "
						+ "where a.keywordIndexValueCode < 0 and a.keywordIndexValueCode > -7"
						+ " and a.language = ?",
						new Object[] {
							LanguageAlpha3Code.getByCode(locale.getLanguage()).getAlpha3B().toString()},
						new Type[] { Hibernate.STRING});
		} catch (HibernateException e) {
			logAndWrap(e);
		}

		Iterator iter = l.iterator();
		//MAURA
//		while (iter.hasNext()) {
//			T_SRCH_KYWRD_IDX aRow = (T_SRCH_KYWRD_IDX) iter.next();
//				result.add(
//					new ValueLabelElement(
//						aRow.getKeywordIndexShortText(),
//						aRow.getKeywordIndexText()));
//		}
		return result;
	}

}

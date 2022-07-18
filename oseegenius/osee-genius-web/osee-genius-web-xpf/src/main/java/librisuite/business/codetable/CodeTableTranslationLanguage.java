/*
 * (c) LibriCore
 * 
 * Created on Jun 23, 2004
 * 
 * CodeTableTranslationLanguage.java
 */
package librisuite.business.codetable;

import java.util.List;
import java.util.Locale;

import librisuite.business.common.DataAccessException;
import librisuite.hibernate.T_TRLTN_LANG_CDE;

/**
 * @author paulm
 * @version %I%, %G%
 * @since 1.0
 */
public class CodeTableTranslationLanguage{
	private short code;

	/**
	 * Class constructor
	 *
	 * @since 1.0
	 */
	public CodeTableTranslationLanguage(short code) {
		setCode(code);
	}

	/* (non-Javadoc)
	 * @see com.libricore.librisuite.business.CodeTable#decode(java.util.Locale)
	 */
	public String decode(Locale locale) throws DataAccessException{
		short language = 0; /* default to english */

		if (locale.getLanguage().compareTo("en") == 0) {
			language = 0;
		} 
		else if (locale.getLanguage().compareTo("it") == 0) {
			language = 5;
		}
		else if (locale.getLanguage().compareTo("es") == 0) {
			language = 3;
		}
		else if (locale.getLanguage().compareTo("hu") == 0) {
			language = 2;
		}
		else if (locale.getLanguage().compareTo("ar") == 0) {
			language = 4;
		}
		else if (locale.getLanguage().compareTo("fr") == 0) {
			language = 1;
		}
		
		T_TRLTN_LANG_CDE c;

			c =
				(T_TRLTN_LANG_CDE) new DAOTranslationLanguage().load(
					(language * 100000) + getCode());
		
		return c.getText();
	}

	public short getCode() {
		return code;
	}

	public void setCode(short s) {
		code = s;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.codetable.CodeTable#getList(java.util.Locale)
	 */
	public List getList(Locale locale) throws DataAccessException {
		// Lists not normally required from T_TRLTN_LANG_CDE
		return null;
	}

}

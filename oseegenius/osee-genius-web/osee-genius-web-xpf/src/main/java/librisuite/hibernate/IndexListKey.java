/*
 * (c) LibriCore
 * 
 * Created on Jul 20, 2004
 * 
 * IndexListKey.java
 */
package librisuite.hibernate;

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.neovisionaries.i18n.LanguageAlpha3Code;

/**
 * Hibernate key class for IDX_LIST table
 * @author paulm
 * @version %I%, %G%
 * @since 1.0
 */
public class IndexListKey implements Serializable {
	private static final Pattern p = Pattern.compile("(\\d+)(.+)");  //digits then the rest
	private int keyNumber;
	private String typeCode;
	private String language;

	/**
	 * Class constructor
	 *
	 * 
	 * @since 1.0
	 */
	public IndexListKey() {
	}

	/**
	 * Uses regexp to isolate the digits (group 1) from the rest (group 2)
	 * Class constructor
	 *
	 * @param key a concatenation of keyNumber and typeCode as used in BrowseManager's 
	 * Maps
	 * @since 1.0
	 */
	public IndexListKey(String key) {
		Matcher m = p.matcher(key);
		m.find();
		setKeyNumber(Integer.parseInt(m.group(1)));
		setTypeCode(m.group(2));
		setLanguage(LanguageAlpha3Code.getByCode(Locale.ENGLISH.getLanguage()).getAlpha3B().toString());
	}
	
	public int getKeyNumber() {
		return keyNumber;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setKeyNumber(int i) {
		keyNumber = i;
	}

	public void setTypeCode(String string) {
		typeCode = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object anObject) {
		IndexListKey aKey = (IndexListKey)anObject;
		if (this.getKeyNumber() == aKey.getKeyNumber()) {
			if (this.getTypeCode().compareTo(aKey.getTypeCode()) == 0){
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return this.getKeyNumber();
	}

	/**
	 * 
	 * 
	 * @return
	 * @exception
	 * @see
	 * @since 1.0
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * 
	 * 
	 * @param string
	 * @exception
	 * @see
	 * @since 1.0
	 */
	public void setLanguage(String string) {
		language = string;
	}

}

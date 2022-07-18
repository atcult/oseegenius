/*
 * (c) LibriCore
 * 
 * Created on Aug 13, 2004
 * 
 * Defaults.java
 */
package librisuite.business.common;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * provides access to default values established in property pages
 * @author paulm
 * @version $Revision: 1.3 $, $Date: 2006/07/11 08:01:06 $
 * @since 1.0
 */
public class BrowseValues {
	
	static public String getString(String key){
		ResourceBundle defaults =
			ResourceBundle.getBundle("resources/browseValues");
			return defaults.getString(key);
	}
	
	public static String getString(String key, String ifNotPresentValue) {
		try {
			return getString(key);
		} catch (MissingResourceException e) {
			return ifNotPresentValue;
		}
	}
	
	static public short getShort(String key) {
		String result = getString(key);
		return (short)Integer.parseInt(result);
	}

	static public int getInteger(String key) {
		String result = getString(key);
		return Integer.parseInt(result);
	}

	static public char getChar(String key) {
		String result = getString(key);
		return result.charAt(0);
	}

	static public Character getCharacter(String key) {
		String result = getString(key);
		return new Character(result.charAt(0));
	}
	
	static public boolean getBoolean(String key) {
		String result = getString(key);
		return Boolean.valueOf(result).booleanValue();
	}
	
	static public boolean getBoolean(String key, boolean ifNotPresentValue) {
		try {
			return getBoolean(key);
		} catch (MissingResourceException e) {
			return ifNotPresentValue;
		}
	}
	
	static public Class getClazz(String key) {
		String result = getString(key);
		try {
			return Class.forName(result);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public static List/*<String>*/ getAllKeys(){
		File file = null;
		try {
			String path = Defaults.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			path = path.substring(0, path.lastIndexOf("/lib/osee"));
			file = new File(path);			
		} catch (Exception e) {
			e.printStackTrace();
		}				
		ClassLoader loader = null;
		try {
		URL[] urls = {file.toURI().toURL()};
		loader = new URLClassLoader(urls);
		} catch (Exception e)  {
			e.printStackTrace();
		}
		ResourceBundle defaults =
			ResourceBundle.getBundle("defaultValues", Locale.getDefault(), loader);
		List/*<String>*/ elencoChiavi = new ArrayList/*<String>*/();
		Enumeration/*<String>*/ enm = defaults.getKeys();
		while (enm.hasMoreElements()) {
			String element = (String) enm.nextElement();
			elencoChiavi.add(element);
		}
		return elencoChiavi;
	}

}

/*
 * (c) LibriCore
 * 
 * $Author: Paulm $
 * $Date: 2006/02/01 14:07:37 $
 * $Locker:  $
 * $Name:  $
 * $Revision: 1.3 $
 * $Source: /source/LibriSuite/src/librisuite/bean/LocalisedBean.java,v $
 * $State: Exp $
 */
package librisuite.bean;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

/**
 * @author Wim Crols
 * @version $Revision: 1.3 $, $Date: 2006/02/01 14:07:37 $
 * @since 1.0
 */
public abstract class LocalisedBean extends LibrisuiteBean {

	private static Hashtable/*<Class, Hashtable>*/ tables = new Hashtable();
	private Class clazz = null;
	
	public LocalisedBean() {
		super();
	}
	
	public LocalisedBean(Class clazz) {
		super();
		this.clazz = clazz;
	}


	private Hashtable getUniqueTable(){
		Class key = clazz;
		if(!tables.containsKey(key)) {
			tables.put(key, new Hashtable());
		}
		return (Hashtable) tables.get(key);
	}
	
	private boolean containsLocale(Locale locale) {
		return getUniqueTable().containsKey(locale);
	}

	public Object getObject(Locale locale) {
		
		Hashtable hashtable = getUniqueTable();
		if (containsLocale(locale)) {
			return hashtable.get(locale);
		} else {
			Object object = loadObject(locale);
			return setObject(locale, object);
		}
	}

	private Object setObject(Locale locale, Object object) {
		getUniqueTable().put(locale, object);
		return object;
	}

	public abstract Object loadObject(Locale locale);
	
	/**
	 * This method is called when the table is changed at runtime to inform
	 * all EditBeans to discard statics preloaded values  
	 * @param locale
	 * @author michelem
	 * @return
	 */
	public void onChange(Class changedClass){
		if(!tables.containsKey(changedClass)) {
			return;
		}
		Hashtable ht = (Hashtable) tables.get(changedClass);
		Enumeration locals = ht.keys();
		tables.remove(changedClass);
		while (locals.hasMoreElements()) {
			Locale locale = (Locale) locals.nextElement();
			loadObject(locale);
		}
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}
	
	public Class getClazz() {
		return clazz;
	}


}

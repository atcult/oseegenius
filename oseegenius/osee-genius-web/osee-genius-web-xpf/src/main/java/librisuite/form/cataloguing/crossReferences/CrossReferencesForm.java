/*
 * (c) LibriCore
 * 
 * Created on Jul 5, 2004
 * 
 * ShowCrossReferences.java
 */
package librisuite.form.cataloguing.crossReferences;

import librisuite.form.LibrisuiteForm;

/**
 * @author paulm
 * @version %I%, %G%
 * @since 1.0
 */
public class CrossReferencesForm extends LibrisuiteForm {

	private int index;
	private String method;
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int i) {
		index = i;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setMethod(String string) {
		method = string;
	}

}

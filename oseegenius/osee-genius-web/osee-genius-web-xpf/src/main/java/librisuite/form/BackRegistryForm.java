/*
 * (c) LibriCore
 * 
 * Created on Jul 6, 2005
 * 
 * BackRegistryForm.java
 */
package librisuite.form;

/**
 * @author paulm
 * @version $Revision: 1.1 $, $Date: 2005/07/08 16:24:22 $
 * @since 1.0
 */
public class BackRegistryForm extends LibrisuiteForm {

	public String forward;
	
	/**
	 * 
	 * @since 1.0
	 */
	public String getForward() {
		return forward;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setForward(String string) {
		forward = string;
	}

}

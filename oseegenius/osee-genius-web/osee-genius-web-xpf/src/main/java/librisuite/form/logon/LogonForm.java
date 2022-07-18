/*
 * (c) LibriCore
 * 
 * Created on 28-jun-2004
 * 
 * LogonForm.java
 */
package librisuite.form.logon;

import javax.servlet.http.HttpServletRequest;

import librisuite.form.LibrisuiteForm;

import org.apache.struts.action.ActionMapping;

/**
 * @author maite
 * @author elena
 * @author wimc
 * @version $Revision: 1.9 $, $Date: 2005/03/03 14:11:56 $
 * @since 1.0
 */
public class LogonForm extends LibrisuiteForm {

	private String locale = null;

	private String changeLocaleButton = null;

	private String changeCssButton = null;

	private String name = null;

	private String password = null;

	private String changePassword = "";

	private String enterLibriSuiteButton = null;

	public void reset(ActionMapping actionMapping,
			HttpServletRequest httpServletRequest) {
		super.reset(actionMapping, httpServletRequest);
		this.locale = null;
		this.changeLocaleButton = null;
		this.changeCssButton = null;
		this.name = null;
		this.password = null;
		this.changePassword = "";
		this.enterLibriSuiteButton = null;
	}

	/**
	 * Getter for changePassword
	 * 
	 * @return changePassword
	 * @since 1.0
	 */
	public String getChangePassword() {
		return changePassword;
	}

	/**
	 * Getter for name
	 * 
	 * @return name
	 * @since 1.0
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for password
	 * 
	 * @return password
	 * @since 1.0
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Setter for changePassword
	 * 
	 * @param b
	 *            changePassword
	 * @since 1.0
	 */
	public void setChangePassword(String s) {
		changePassword = s;
	}

	/**
	 * Setter for Name
	 * 
	 * @param string
	 *            Name
	 * @since 1.0
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * Setter for password
	 * 
	 * @param string
	 *            password
	 * @since 1.0
	 */
	public void setPassword(String string) {
		password = string;
	}

	/**
	 * Getter for enterLibriSuiteButton
	 * 
	 * @return Returns the enterLibriSuiteButton.
	 */
	public String getEnterLibriSuiteButton() {
		return enterLibriSuiteButton;
	}

	/**
	 * Setter for enterLibriSuiteButton
	 * 
	 * @param enterLibriSuiteButton
	 *            The enterLibriSuite to set.
	 */
	public void setEnterLibriSuiteButton(String enterLibriSuiteButton) {
		this.enterLibriSuiteButton = enterLibriSuiteButton;
	}

	/**
	 * @return Returns the locale.
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale
	 *            The locale to set.
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return Returns the changeLocaleButton.
	 */
	public String getChangeLocaleButton() {
		return changeLocaleButton;
	}

	/**
	 * @param changeLocaleButton
	 *            The changeLocaleButton to set.
	 */
	public void setChangeLocaleButton(String changeLocaleButton) {
		this.changeLocaleButton = changeLocaleButton;
	}

	/**
	 * @return Returns the changeCssButton.
	 */
	public String getChangeCssButton() {
		return changeCssButton;
	}

	/**
	 * @param changeCssButton
	 *            The changeCssButton to set.
	 */
	public void setChangeCssButton(String changeCssButton) {
		this.changeCssButton = changeCssButton;
	}

}
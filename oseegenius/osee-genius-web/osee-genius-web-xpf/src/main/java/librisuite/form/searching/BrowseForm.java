/*
 * (c) LibriCore
 * 
 * Created on Jul 6, 2004
 * 
 * TempBrowseForm.java
 */
package librisuite.form.searching;

import javax.servlet.http.HttpServletRequest;

import librisuite.business.common.Defaults;
import librisuite.form.LibrisuiteForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionMapping;

/**
 * @author paulm
 * @version %I%, %G%
 * @since 1.0
 */
public class BrowseForm extends LibrisuiteForm {
	private static Log logger = LogFactory.getLog(BrowseForm.class);
	private int entryNumber;
	private String selectedIndex;
	private String searchTerm;
	private boolean showResults;
	private int termsToDisplay=Defaults.getInteger("browse.termsPerPage");
	private String nameText;
	private String operation;
	private int headingNumber;
	private String language;
	private int collectionCode;
	private String term;
	//private String lastTerm;


	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	
	public int getCollectionCode() {
		return collectionCode;
	}

	public void setCollectionCode(int collectionCode) {
		this.collectionCode = collectionCode;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getHeadingNumber() {
		return headingNumber;
	}

	public void setHeadingNumber(int headingNumber) {
		this.headingNumber = headingNumber;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getNameText() {
		return nameText;
	}

	public void setNameText(String nameText) {
		this.nameText = nameText;
	}

	public int getEntryNumber() {
		return entryNumber;
	}

	public void setEntryNumber(int i) {
		entryNumber = i;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public String getSelectedIndex() {
		return selectedIndex;
	}

	public boolean isShowResults() {
		return showResults;
	}

	public void setSearchTerm(String string) {
		searchTerm = string;
	}

	public void setSelectedIndex(String s) {
		logger.debug("setting selectedIndex to '" + s + "'");
		selectedIndex = s;
	}

	public void setShowResults(boolean b) {
		showResults = b;
	}

	/**
	 * 
	 */
	public int getTermsToDisplay() {
		return termsToDisplay;
	}

	/**
	 * 
	 */
	public void setTermsToDisplay(int i) {
		termsToDisplay = i;
	}
	
	public void reset(ActionMapping actionMapping, HttpServletRequest httpServletRequest) 
	{
		setOperation(null);
		this.termsToDisplay=Defaults.getInteger("browse.termsPerPage");
	}



}

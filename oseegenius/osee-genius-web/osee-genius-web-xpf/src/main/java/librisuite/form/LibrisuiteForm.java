/*
 * (c) LibriCore
 * 
 * Created on Jul 1, 2004
 * 
 * LibrisuiteForm.java
 */
package librisuite.form;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * This abstract class can be used as a super class for all ActionForms.
 * All methods common or related to ActionForm can be in this class.
 * 
 * @author Wim Crols
 * @version $Revision: 1.2 $, $Date: 2004/08/30 14:26:28 $
 * @since 1.0
 */
public abstract class LibrisuiteForm extends ActionForm {

	private static Log logger = LogFactory.getLog(LibrisuiteForm.class);

	/**
	 * If this boolean is set then an error should be displayed.
	 */
	private boolean error = false;

	/**
	 * If error is set to true errorId should be set to the property key
	 * for displaying the correct error message.
	 */
	private String errorId = new String("");

	/* (non-Javadoc)
	 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	public void reset(
		ActionMapping actionMapping,
		HttpServletRequest httpServletRequest) {
		try {
			httpServletRequest.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			logger.error(unsupportedEncodingException);
		}
		super.reset(actionMapping, httpServletRequest);
	}

	/**
	 * Getter for error
	 * Error is a boolean which is set if
	 * an error message should be displayed on screen.
	 * 
	 * @return error
	 * @since 1.0
	 */
	public boolean isError() {
		return this.error;
	}

	/**
	 * Getter for errorId
	 * ErrorId is the name which is used as a property key for displaying
	 * the error in the choosen language.
	 * 
	 * @return errorId
	 * @since 1.0
	 */
	public String getErrorId() {
		return this.errorId;
	}

	/**
	 * Setter for error
	 * 
	 * @param error error to be set
	 * @since 1.0
	 */
	public void setError(boolean error) {
		this.error = error;
	}

	/**
	 * Setter for errorId
	 * 
	 * @param errorId errorId to be set
	 * @since 1.0
	 */
	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}

}

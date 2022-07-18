/*
 * (c) LibriCore
 * 
 * Created on Mar 8, 2006
 * 
 * MarcRecordServiceAction.java
 */
package librisuite.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import librisuite.action.logon.SessionChecker;
import librisuite.business.common.Defaults;
import librisuite.form.LibrisuiteForm;

/**
 * This action provides an web service called from AMICUS
 * 
 */
public class AmicusServiceAction extends LibrisuiteAction implements SessionChecker {
	private static final Log logger = LogFactory.getLog(AmicusServiceAction.class);
	private static final String EQUIVALENT_RECORD_SERVICE = "eqrec";
	private static final String QUALITY_CONTROL_SERVICE = "verify";
	private static final String NOT_AVAILABLE = "notAvailable";
	private static final String FAIL = "fail";
	private static final boolean isTestMode = Defaults.getBoolean("amicus.web.service.test.mode", false);

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.action.LibrisuiteAction#librisuiteExecute(org.apache.struts.action.ActionMapping,
	 *      librisuite.form.LibrisuiteForm,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward librisuiteExecute(ActionMapping mapping, LibrisuiteForm form, HttpServletRequest request, HttpServletResponse response) {

		return null;
	}


}

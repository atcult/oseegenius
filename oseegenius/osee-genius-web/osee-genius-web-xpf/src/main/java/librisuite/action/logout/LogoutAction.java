/*
 * (c) LibriCore
 *
 * Created on 28-jun-2004
 *
 * LogonAction.java
 */
package librisuite.action.logout;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import librisuite.action.LibrisuiteAction;
import librisuite.action.logon.LogonAction;
import librisuite.action.logon.SessionChecker;
import librisuite.business.common.DataAccessException;
import librisuite.form.LibrisuiteForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * @author primesource
 * @author barbara
 * @version $Revision: 1.32 $, $Date: 2007/01/31 08:01:06 $
 * @since 1.0
 */

public class LogoutAction extends LibrisuiteAction implements SessionChecker {
	
	private Log logger = LogFactory.getLog(LogonAction.class);

	public ActionForward librisuiteExecute(
		ActionMapping actionMapping,
		LibrisuiteForm librisuiteForm,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		logger.debug("Entering logoutAction");
		ActionErrors errors = new ActionErrors();


		httpServletRequest.getSession().invalidate();
		return actionMapping.findForward("success");
	}
}
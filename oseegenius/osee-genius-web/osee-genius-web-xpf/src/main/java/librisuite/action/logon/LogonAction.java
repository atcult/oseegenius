/*
 * (c) LibriCore
 * 
 * Created on 28-jun-2004
 * 
 * LogonAction.java
 */
package librisuite.action.logon;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import librisuite.action.LibrisuiteAction;
import librisuite.bean.css.CssBean;
import librisuite.bean.locale.LocaleBean;
import librisuite.bean.logon.LogonBean;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.form.LibrisuiteForm;
import librisuite.form.logon.LogonForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.libricore.librisuite.controller.SessionUtils;
import com.libricore.librisuite.controller.UserProfile;

/**
 * @author maite
 * @author elena
 * @author Wim Crols
 * @version $Revision: 1.32 $, $Date: 2006/07/11 08:01:06 $
 * @since 1.0
 */
public class LogonAction extends LibrisuiteAction implements SessionChecker {

	public static final String LAST_LOGON_MESSAGE = "LAST_LOGON_MESSAGE";
	private Log logger = LogFactory.getLog(LogonAction.class);

	public ActionForward librisuiteExecute(ActionMapping actionMapping,
			LibrisuiteForm librisuiteForm,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		
		String result = "welcome";
		httpServletRequest.getSession().removeAttribute(LAST_LOGON_MESSAGE);
		LogonForm logonForm = (LogonForm) librisuiteForm;
		//logonForm.setName(USER_NAME);
		logonForm.setEnterLibriSuiteButton("EnterLibriSuiteButton");
	
		LogonBean logonBean = LogonBean.getInstance(httpServletRequest);
		CssBean cssBean = CssBean.getInstance(httpServletRequest);
		LocaleBean localeBean = LocaleBean.getInstance(httpServletRequest);
		localeBean.setLocale(this.getLocale(httpServletRequest));
		initSession(httpServletRequest, cssBean, localeBean, logonBean);
		setUserProfile(httpServletRequest, logonForm);
			
	    return actionMapping.findForward(result);
	}

	
	
	private void setUserProfile(
		HttpServletRequest request,
		LogonForm logonForm) {
		try {
			SessionUtils.setUserProfile(
				request.getSession(false),
				new UserProfile(logonForm.getName()));
			
		} catch (DataAccessException e) {
			throw new RuntimeException(e);
		}

	}
	private void initSession(HttpServletRequest httpServletRequest, CssBean cssBean, LocaleBean localeBean, LogonBean logonBean) {
		logger.info("destroy old session...");
		HttpSession session_old = httpServletRequest.getSession(false);
		session_old.invalidate();
		HttpSession session = httpServletRequest.getSession(true);
		session.setAttribute(cssBean.getClass().getName(), cssBean);
		session.setAttribute(localeBean.getClass().getName(), localeBean);
		SessionUtils.setCurrentLocale(session, localeBean.getLocale());
		session.setAttribute(logonBean.getClass().getName(), logonBean);
	}
	
	

}
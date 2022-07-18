/*
 * (c) LibriCore
 * 
 * Created on Mar 8, 2006
 * 
 * MarcRecordServiceAction.java
 */
package librisuite.action;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import librisuite.action.logon.DirectLogonAction;
import librisuite.action.logon.InitialApplicationAction;
import librisuite.action.logon.SessionChecker;
import librisuite.bean.css.CssBean;
import librisuite.business.authentication.AuthenticationException;
import librisuite.business.authentication.DAOPass;
import librisuite.business.authorisation.AuthorisationException;
import librisuite.business.authorisation.MadesAuthority;
import librisuite.business.common.DataAccessException;
import librisuite.form.DispatchServiceForm;
import librisuite.form.LibrisuiteForm;
import librisuite.hibernate.UserTempPass;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.libricore.librisuite.controller.SessionUtils;
import com.libricore.librisuite.controller.UserProfile;

/**
 * This action provides a web service to deliver a Marc record based on the
 * contents of the request.
 * 
 * @author paulm
 * @version $Revision: 1.1 $, $Date: 2006/11/23 15:01:47 $
 * @since 1.0
 */
public class DispatchServiceAction extends LibrisuiteAction implements SessionChecker {
	public static final String OPACA_SERVICE = "opaca";

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.action.LibrisuiteAction#librisuiteExecute(org.apache.struts.action.ActionMapping,
	 *      librisuite.form.LibrisuiteForm,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward librisuiteExecute(ActionMapping mapping, LibrisuiteForm form, HttpServletRequest request, HttpServletResponse response) {
		ActionErrors errors = new ActionErrors();
		String result = "fail";
		DispatchServiceForm dsForm = (DispatchServiceForm) form;
		request.getSession(false).invalidate();
		request.getSession(true);
		DirectLogonAction logonAction = new DirectLogonAction();
		try {
			String pass = dsForm.getSsid();
			DAOPass dao = new DAOPass();
			UserTempPass utp = dao.loadSavedToken(pass);
			Hashtable params = dao.loadParameters(pass);
			
			logonAction.logon(request, dsForm, params);
			InitialApplicationAction initApp = new InitialApplicationAction();
			initApp.initSelectedApplication(request, utp.getAppCode());
			String calledService = dsForm.getService();
			
			// TODO _MIKE: create a factory to manage these services
			if(OPACA_SERVICE.equals(calledService)){
				if(!"mad".equals(params.get("appName"))) {
					throw new AuthenticationException("incorrect.caller.application");
				}
				CssBean cssBean = CssBean.getInstance(request);
				cssBean.setMadesCssUrl(cssBean.getOpacaCssUrl());
				selectOpacaPermissions(request);
			}
			dao.removeToken(pass);
			
			result = calledService;
		} catch (AuthenticationException e) {
			addError(request, errors, e, "error.authentication.failed");
			result = "fail";
		} catch (AuthorisationException e) {
			addError(request, errors, e, "error.authorisation");
			return mapping.findForward("fail");
		} catch (DataAccessException e) {
			addError(request, errors, e, "error.librisuite.business.common.DataAccessException");
			result = "fail";
		} catch (Throwable e) {
			addError(request, errors, e, "error.java.lang.Throwable");
			result = "fail";
		} 

		return mapping.findForward(result);
	}

	private void selectOpacaPermissions(HttpServletRequest request) {
		UserProfile pf = SessionUtils.getUserProfile(request);
		((MadesAuthority)pf.getAuthorisationAgent()).opacaMode();
	}

}

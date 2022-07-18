/*
 * (c) LibriCore
 * 
 * Created on 28-jun-2004
 * 
 * LogonAction.java
 */
package librisuite.action.logon;

import java.util.Hashtable;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import librisuite.action.LibrisuiteAction;
import librisuite.bean.css.CssBean;
import librisuite.bean.locale.LocaleBean;
import librisuite.business.authentication.AuthenticationException;
import librisuite.business.authentication.DAOPass;
import librisuite.business.common.DataAccessException;
import librisuite.form.DispatchServiceForm;
import librisuite.form.LibrisuiteForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.libricore.librisuite.controller.SessionUtils;
import com.libricore.librisuite.controller.UserProfile;

/**
 * @author michelem
 * @since 1.0
 */
public class DirectLogonAction extends LibrisuiteAction {

	private Log logger = LogFactory.getLog(DirectLogonAction.class);

	public ActionForward librisuiteExecute(ActionMapping actionMapping, LibrisuiteForm librisuiteForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		ActionErrors errors = new ActionErrors();

		DispatchServiceForm dsForm = (DispatchServiceForm) librisuiteForm;

		String result = "authorised";

		try {
			logon(httpServletRequest, dsForm);
		} catch (AuthenticationException e) {
			logger.info("AuthenticationException in authenticateUser", e);
			addError(httpServletRequest, errors, e, "error.authentication.failed");
			result = "fail";
		} catch (DataAccessException e) {
			logger.error("error.librisuite.business.common.DataAccessException");
			result = "fail";
		}

		return actionMapping.findForward(result);
	}

	public void logon(HttpServletRequest request, DispatchServiceForm dsForm) throws AuthenticationException, DataAccessException {

		String ssid = dsForm.getSsid();
		String lcl = dsForm.getLcl();
		String lcc = dsForm.getLcc();
		String unme = dsForm.getUnme();

		// TODO _MIKE: create a new authorisation broker for this pourpose
		checkUser(ssid);

		logon(request, unme, lcl, lcc);
	}
	
	private final String getParam(Hashtable ht, String s){
		if(ht.containsKey(s)){
			return ((String) ht.get(s)).trim();
		}
		return "";
	}
	
	public void logon(HttpServletRequest request, DispatchServiceForm dsForm, Hashtable ht) throws AuthenticationException, DataAccessException {

		String ssid = dsForm.getSsid();
		String lcl 	= getParam(ht, "lcl");
		String lcc 	= getParam(ht, "lcc");
		String unme = getParam(ht, "unme").trim();

		// TODO _MIKE: create a new authorisation broker for this pourpose
		checkUser(ssid);

		logon(request, unme, lcl, lcc);
	}

	public void logon(HttpServletRequest request, String unme, String lcl, String lcc) {
		// updating locale
		Locale locale = new Locale(lcl, lcc);
		this.setLocale(request, locale);

		LocaleBean localeBean = LocaleBean.getInstance(request);
		localeBean.setLocale(locale);

		CssBean.getInstance(request);

		String userName = unme;

		setUserProfile(request, userName);
		//MainMenuBean.getInstance(request);
	}
	

	private void checkUser(String ssid) throws DataAccessException, AuthenticationException {
		DAOPass dao = new DAOPass();
		boolean valid = dao.isTokenValid(ssid);
		if (!valid) {
			throw new AuthenticationException("access.denied.for.invalid.token");
		}
	}

	private void setUserProfile(HttpServletRequest request, String userName) {
		try {
			SessionUtils.setUserProfile(request.getSession(false), new UserProfile(userName));
		} catch (DataAccessException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
/*
 * (c) LibriCore
 * 
 * Created on Aug 11, 2004
 * 
 * LibrisuiteLookupDispatchAction.java
 */
package librisuite.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import librisuite.bean.authority.AuthorityBean;
import librisuite.bean.crossreference.CrossReferenceBean;
import librisuite.bean.css.CssBean;
import librisuite.bean.locale.LocaleBean;
import librisuite.bean.logon.LogonBean;
import librisuite.business.authorisation.AuthorisationException;
import librisuite.business.common.DataAccessException;
import librisuite.business.exception.LibrisuiteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;

import com.libricore.librisuite.controller.SessionUtils;
import com.libricore.librisuite.controller.UserProfile;

/**
 * @author paulm
 * @version $Revision: 1.3 $, $Date: 2005/10/21 13:33:25 $
 * @since 1.0
 */
public abstract class LibrisuiteDispatchAction extends DispatchAction {

	private static Log logger =
		LogFactory.getLog(LibrisuiteDispatchAction.class);

	public void addError(
		HttpServletRequest request,
		ActionErrors errors,
		String error) {
			// MIKE: for logging purpose
			logger.error("adding error "+error);
		errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(error));
		saveErrors(request, errors);
	}

	public void addError(
			HttpServletRequest request,
			ActionErrors errors,
			Throwable cause,
			String error) {
			if(cause!=null){
				Object arg0 = cause.getLocalizedMessage();
				if(arg0==null) arg0 = cause.getClass().getName();
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionError(error, arg0 ));
				// MIKE: for logging purpose
				logger.error("adding error "+error);
				LibrisuiteAction.traceException(cause);
				saveErrors(request, errors);
			} else addError(request, errors, error);
		}
	
	


	public void initSession(HttpServletRequest httpServletRequest, CssBean cssBean, LocaleBean localeBean, LogonBean logonBean,CrossReferenceBean crossBean
		) {
		logger.info("destroy old session...");
		HttpSession session_old = httpServletRequest.getSession(false);
		session_old.invalidate();
		HttpSession session = httpServletRequest.getSession(true);
		session.setAttribute(cssBean.getClass().getName(), cssBean);
		session.setAttribute(localeBean.getClass().getName(), localeBean);
		SessionUtils.setCurrentLocale(session, localeBean.getLocale());
		session.setAttribute(logonBean.getClass().getName(), logonBean);
		session.setAttribute(crossBean.getClass().getName(), crossBean);
		
	}
	
	public void initSelectedApplication(HttpServletRequest request, String startingApp) throws AuthorisationException, LibrisuiteException, DataAccessException {
		  logger.info("**** "+startingApp +"****");
								
		  UserProfile userProfile = SessionUtils.getUserProfile(request.getSession(false));
								
		 if (startingApp.equals("cat")) {
			 userProfile.checkPermission("basicCataloguing");
		 } 
	  }
	
}

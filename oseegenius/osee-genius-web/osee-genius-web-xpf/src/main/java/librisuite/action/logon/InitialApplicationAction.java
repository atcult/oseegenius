/*
 * (c) LibriCore
 * 
 * Created on Jan 16, 2006
 * 
 * InitialApplicationAction.java
 */
package librisuite.action.logon;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import librisuite.action.LibrisuiteAction;
import librisuite.bean.css.CssBean;
import librisuite.bean.locale.LocaleBean;
import librisuite.bean.logon.LogonBean;
import librisuite.bean.searching.BrowseBean;
import librisuite.bean.searching.SearchBean;
import librisuite.business.authorisation.AuthorisationException;
import librisuite.business.common.DataAccessException;
import librisuite.business.exception.LibrisuiteException;
import librisuite.form.LibrisuiteForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.libricore.librisuite.controller.SessionUtils;
import com.libricore.librisuite.controller.UserProfile;

/**
 * @author paulm
 * @version $Revision: 1.1 $, $Date: 2006/01/19 12:31:26 $
 * @since 1.0
 */
public class InitialApplicationAction extends LibrisuiteAction {
	
	private static final Log logger = LogFactory.getLog(InitialApplicationAction.class);

	/* (non-Javadoc)
	 * @see librisuite.action.LibrisuiteAction#librisuiteExecute(org.apache.struts.action.ActionMapping, librisuite.form.LibrisuiteForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward librisuiteExecute(
		ActionMapping mapping,
		LibrisuiteForm librisuiteForm,
		HttpServletRequest request,
		HttpServletResponse httpServletResponse) {
		String startingApp = "cat";
		try {
			initSelectedApplication(request, startingApp);
		} catch (AuthorisationException e) {
			logger.info("User not authorised for "+startingApp+" application");
			request.setAttribute(LogonAction.LAST_LOGON_MESSAGE, e.getMessage());
			return mapping.findForward("fail");
		} 
		catch (Throwable e) {
			logger.error("Librisuite exception initializing application",e);
			request.setAttribute(LogonAction.LAST_LOGON_MESSAGE, e.getMessage());
			return mapping.findForward("fail");
		}
		return mapping.findForward(startingApp);
	}

	/**
	 * @param request
	 * @param startingApp
	 * @throws AuthorisationException
	 * @throws LibrisuiteException
	 * @throws DataAccessException
	 */
	public void initSelectedApplication(HttpServletRequest request, String startingApp) throws AuthorisationException, LibrisuiteException, DataAccessException {
		logger.info("**** "+startingApp +"****");
		
   	    UserProfile userProfile = SessionUtils.getUserProfile(request.getSession(false));
		
		if (startingApp.equals("cat")) {
			userProfile.checkPermission("basicCataloguing");
			
		} 
		
	}
	
	


}

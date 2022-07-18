/*
 * (c) LibriCore
 * 
 * Created on Jul 1, 2004
 * 
 * LibrisuiteAction.java
 */
package librisuite.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import librisuite.action.logon.SessionChecker;
import librisuite.bean.LibrisuiteBean;
import librisuite.bean.searching.SearchBean;
import librisuite.business.codetable.DAOCodeTable;
import librisuite.business.exception.LibrisuiteException;
import librisuite.form.LibrisuiteForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import com.libricore.librisuite.controller.SessionUtils;

/**
 * @author Wim Crols
 * @version $Revision: 1.21 $, $Date: 2006/02/03 16:35:32 $
 * @since 1.0
 */
public abstract class LibrisuiteAction extends Action {

	private static Log logger = LogFactory.getLog(LibrisuiteAction.class);
	private static DAOCodeTable daoCodeTable = new DAOCodeTable();

	public ActionForward execute(
		ActionMapping actionMapping,
		ActionForm actionForm,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		LibrisuiteForm librisuiteForm = (LibrisuiteForm) actionForm;
		logger.info("ACTION: ["+actionMapping.getPath()
				+"] TYPE=["+this.getClass().getName()
				+"] NAME=["+actionMapping.getName()
				+"] METHOD=["+httpServletRequest.getParameter("method")
				+"] PARAMETER=["+actionMapping.getParameter()+"]");

		isLVSessionTimeOut(httpServletRequest);
		if (isSessionTimeOut(httpServletRequest)) {
			logger.info("HTTP session has timed out");
			ActionForward sessionTimeOutActionForward = new ActionForward();
			sessionTimeOutActionForward.setName("sessionTimeOut");
			sessionTimeOutActionForward.setPath("/");
			return sessionTimeOutActionForward;
		}

		try {
			return this.librisuiteExecute(
				actionMapping,
				librisuiteForm,
				httpServletRequest,
				httpServletResponse);
		} catch (Exception librisuiteException) {
			logger.warn(
				"Got uncaught LibrisuiteException",
				librisuiteException);
			librisuiteForm.setError(true);
			librisuiteForm.setErrorId(
				this.getClass().getName()
					+ "::"
					+ librisuiteException.getClass().getName());
			throw new RuntimeException(librisuiteException);
		}

	}

//TODO remove the throws clause once Circulation has been coordinated
	public abstract ActionForward librisuiteExecute(
		ActionMapping mapping,
		LibrisuiteForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws LibrisuiteException;

	public void addError(
		HttpServletRequest request,
		ActionErrors errors,
		String error) {
		errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionError(error));
		saveErrors(request, errors);
	}
	public void addError(
			HttpServletRequest request,
			ActionErrors errors,
			Throwable cause,
			String error) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionError(error, cause.getLocalizedMessage() ));
			// MIKE: for logging purpose
			logger.error("adding error "+error);
			traceException(cause);
			saveErrors(request, errors);
		}

	public static void traceException(Throwable cause) {
		if(cause!=null){
			logger.error("caused by", cause);
			if(!cause.equals(cause.getCause())){
				traceException(cause.getCause());
			}
		}
	}

	protected static boolean isLVSessionTimeOut(HttpServletRequest httpServletRequest) {
	
		return false;
	}

	protected boolean isSessionTimeOut(HttpServletRequest httpServletRequest) {
		if (this instanceof SessionChecker) {
			return false;
		}
		else if (SessionUtils.getUserProfile(httpServletRequest.getSession()) == null) {
			return true;
		}
		else {
			return false;
		}
	}

	public static DAOCodeTable getDaoCodeTable() {
		return daoCodeTable;
	}

}

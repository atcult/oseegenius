/*
 * Created on 15-jul-2004
 *
 */
package librisuite.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import librisuite.form.LibrisuiteForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Class comment
 * @author janick
 */
public class FindForwardAction extends LibrisuiteAction {
	private static Log logger = LogFactory.getLog(FindForwardAction.class);

	/**
	 * 
	 */
	public FindForwardAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward librisuiteExecute(
		ActionMapping arg0,
		LibrisuiteForm arg1,
		HttpServletRequest arg2,
		HttpServletResponse arg3) {
		
		String param = arg0.getParameter();
		if ((param != null) && (!("".equals(param)))) {
			logger.debug("action has parameter " + param);
			return arg0.findForward(arg2.getParameter(param));
		} else {
			logger.debug("action has no parameter -- looking through forwards");
			String forwards[] = arg0.findForwards();

			for (int i = 0; i < forwards.length; i++) {
				if (arg2.getParameter(forwards[i]) != null) {
					logger.debug(
						"forward " + forwards[i] + " found in request");
					return arg0.findForward(forwards[i]);
				}
			}

		}
		//the request did not contain any specific forwarding parameter
		return arg0.findForward("success");
	}

}

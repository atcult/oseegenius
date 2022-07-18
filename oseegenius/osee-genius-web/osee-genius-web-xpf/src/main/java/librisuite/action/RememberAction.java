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

import com.libricore.librisuite.common.BackUtils;

/**
 * 
 * @author Michele
 */
public class RememberAction extends LibrisuiteAction {
	public static Log logger = LogFactory.getLog(RememberAction.class);

	/**
	 * 
	 */
	public RememberAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward librisuiteExecute(
		ActionMapping mapping,
		LibrisuiteForm form,
		HttpServletRequest request,
		HttpServletResponse response) {

		return BackUtils.rememberAction(mapping, request);
	}

}

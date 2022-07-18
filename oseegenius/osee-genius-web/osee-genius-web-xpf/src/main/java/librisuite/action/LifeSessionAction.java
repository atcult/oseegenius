
package librisuite.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import librisuite.action.logon.SessionChecker;
import librisuite.bean.css.CssBean;
import librisuite.bean.locale.LocaleBean;
import librisuite.bean.logon.LogonBean;
import librisuite.business.common.DataAccessException;
import librisuite.form.LibrisuiteForm;
import librisuite.form.logon.LogonForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.libricore.librisuite.controller.SessionUtils;
import com.libricore.librisuite.controller.UserProfile;



public class LifeSessionAction extends LibrisuiteAction implements SessionChecker {
	private Log logger = LogFactory.getLog(LifeSessionAction.class);

	public ActionForward librisuiteExecute(ActionMapping actionMapping,
			LibrisuiteForm librisuiteForm,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		
		String result = "lifeSession";
					
	    return actionMapping.findForward(result);
	}

	
	
	

}
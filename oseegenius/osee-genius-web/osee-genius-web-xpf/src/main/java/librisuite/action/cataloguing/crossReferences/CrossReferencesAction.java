/*
 * (c) LibriCore
 * 
 * Created on Jul 5, 2004
 * 
 * ShowCrossReferences.java
 */
package librisuite.action.cataloguing.crossReferences;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import librisuite.action.LibrisuiteAction;
import librisuite.bean.crossreference.CrossReferenceBean;
import librisuite.bean.searching.BrowseBean;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.DeleteCrossReferenceException;
import librisuite.business.exception.IncompatibleCrossReferenceHeadingsException;
import librisuite.business.searching.InvalidBrowseIndexException;
import librisuite.form.LibrisuiteForm;
import librisuite.form.cataloguing.crossReferences.CrossReferencesForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.libricore.librisuite.controller.SessionUtils;

/**
 * @author paulm
 * @version %I%, %G%
 * @since 1.0
 */
public class CrossReferencesAction extends LibrisuiteAction {
	private Log logger = LogFactory.getLog(CrossReferencesAction.class);



	/* (non-Javadoc)
	 * @see librisuite.action.LibrisuiteAction#librisuiteExecute(org.apache.struts.action.ActionMapping, librisuite.form.LibrisuiteForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward librisuiteExecute(
		ActionMapping mapping,
		LibrisuiteForm librisuiteForm,
		HttpServletRequest request,
		HttpServletResponse httpServletResponse) {

		ActionErrors errors = new ActionErrors();
		CrossReferencesForm form = (CrossReferencesForm)librisuiteForm;
		CrossReferenceBean bean = CrossReferenceBean.getInstance(request);
		
		if (form.getMethod().equals("back")) {
			BrowseBean browseBean;
			browseBean = (BrowseBean) BrowseBean.getInstance(request);
			try {
				browseBean.setLastBrowseTerm(bean.getSourceDescriptor().getDisplayText());
				browseBean.setLastBrowseTermSkip(bean.getSourceDescriptor().getDisplayText());

				browseBean.refresh(browseBean.getLastBrowseTerm(), SessionUtils
						.getCataloguingView(), SessionUtils
						.getMainLibrary(),
						SessionUtils.getCurrentLocale(request));

			} catch (DataAccessException e1) {
				addError(request, errors, "error.data.browse");
				return mapping.findForward("fail");
			} catch (InvalidBrowseIndexException e2) {
				addError(request, errors, "error.browse.index");
				return mapping.findForward("fail");
			}
			return mapping.findForward("close");
		}
		else if (form.getMethod().equals("addNew")) {
			try {
				//default
				bean.setReferenceType((short)0);
				if(bean.getTargetDescriptor()!=null)
				bean.setTargetDescriptor(null);
			} catch (IncompatibleCrossReferenceHeadingsException e) {
				addError(request, errors, "error.data.reference.incompatible");
				return mapping.findForward("fail");
			}
			return mapping.findForward("new");
		}
		else if (form.getMethod().equals("deleteByIndex")) {
			try {
				bean.deleteByIndex(form.getIndex());
			} catch (DeleteCrossReferenceException e) {
				addError(request, errors, "error.data.reference.delete");
				return mapping.findForward("fail");
			}
			return mapping.findForward("deleted");
		}
		else {
			return mapping.findForward("fail");
		}
	}

}

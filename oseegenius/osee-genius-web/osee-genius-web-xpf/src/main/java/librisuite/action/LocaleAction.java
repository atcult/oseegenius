/*
 * (c) LibriCore
 * 
 * Created on Jul 6, 2004
 * 
 * TempBrowseAction.java
 */
package librisuite.action;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import librisuite.bean.crossreference.CrossReferenceBean;
import librisuite.bean.locale.LocaleBean;
import librisuite.form.searching.BrowseForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.model.Visit;
import com.libricore.librisuite.controller.SessionUtils;


public class LocaleAction extends LibrisuiteDispatchAction {
	
	private Log logger = LogFactory.getLog(LocaleAction.class);
	
    //TODO Bisogna gestire il ritorno sulla apgine del browse oppure delle cross reference
	public ActionForward refreshLanguage(
			ActionMapping mapping,
			ActionForm actionForm,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
	    HttpSession session = httpServletRequest.getSession();
	    BrowseForm form = (BrowseForm) actionForm;
		LocaleBean localeBean = LocaleBean.getInstance(httpServletRequest);
		CrossReferenceBean crossReferenceBean = CrossReferenceBean.getInstance(httpServletRequest);
		Visit visit = (Visit) httpServletRequest.getSession().getAttribute(HttpAttribute.VISIT);
		Locale locale = new Locale(form.getLanguage());
		visit.setPreferredLocale(locale);
		localeBean.setLocale(visit.getPreferredLocale());
		session.setAttribute(localeBean.getClass().getName(), localeBean);
		SessionUtils.setCurrentLocale(session, localeBean.getLocale());
		String result="";
		if(crossReferenceBean.isCrossReference()==true)
			result="crossReference";
		else
			 result="browse";
		return mapping.findForward(result);
		
}
		
			

  
	
  
	
}
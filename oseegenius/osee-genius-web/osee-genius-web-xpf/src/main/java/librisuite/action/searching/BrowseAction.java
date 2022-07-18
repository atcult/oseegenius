/*
 * (c) LibriCore
 * 
 * Created on Jul 6, 2004
 * 
 * TempBrowseAction.java
 */
package librisuite.action.searching;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import librisuite.action.LibrisuiteDispatchAction;
import librisuite.bean.authority.AuthorityBean;
import librisuite.bean.crossreference.CrossReferenceBean;
import librisuite.bean.css.CssBean;
import librisuite.bean.locale.LocaleBean;
import librisuite.bean.logon.LogonBean;
import librisuite.bean.searching.BrowseBean;
import librisuite.business.authorisation.AuthorisationException;
import librisuite.business.authority.AuthorityNote;
import librisuite.business.common.BrowseValues;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.descriptor.Descriptor;
import librisuite.business.exception.IncompatibleCrossReferenceHeadingsException;
import librisuite.business.exception.LibrisuiteException;
import librisuite.business.searching.InvalidBrowseIndexException;
import librisuite.business.searching.NoResultsFoundException;
import librisuite.form.searching.BrowseForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDateTime;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.model.Visit;
import com.libricore.librisuite.controller.SessionUtils;
import com.libricore.librisuite.controller.UserProfile;

/**
 * @author paulm
 * @version %I%, %G%
 * @since 1.0
 */
public class BrowseAction extends LibrisuiteDispatchAction {
	private Log logger = LogFactory.getLog(BrowseAction.class);
	public static final String HEADING_BEAN_SESSION_NAME = "HeadingBean";
	public static final int madesView = Defaults.getInteger("default.mades.view.search");
	public static final int MAIN_LIBRARY = Defaults.getInteger("main.library");

	/* (non-Javadoc)
	 * @see librisuite.action.LibrisuiteAction#librisuiteExecute(org.apache.struts.action.ActionMapping, librisuite.form.LibrisuiteForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward showXrefs(
		ActionMapping mapping,
		ActionForm actionForm,
		HttpServletRequest request,
		HttpServletResponse httpServletResponse) {
		
		System.out.println(LocalDateTime.now() + " BrowseAction: showXrefs (point 1)");
		logger.debug("showXrefs");
		ActionErrors errors = new ActionErrors();
		BrowseForm form = (BrowseForm) actionForm;
		BrowseBean bean = (BrowseBean) BrowseBean.getInstance(request);
		List l = bean.getBrowseList();
		Descriptor d = (Descriptor) l.get(form.getEntryNumber());
		CrossReferenceBean crb = CrossReferenceBean.getInstance(request);
		
		try {
			crb.setSourceDescriptor(d, false);
			crb.setAttribute(false);
		} catch (DataAccessException e1) {
			addError(request, errors, "error.data.reference.read");
			return mapping.findForward("fail");
		}
		
		return mapping.findForward("showXrefs");
	}


	public ActionForward refresh(
			ActionMapping mapping,
			ActionForm actionForm,
			HttpServletRequest request,
			HttpServletResponse httpServletResponse) {

		    System.out.println(LocalDateTime.now() + " BrowseAction: refresh (point 1)");
			logger.debug("refresh");
			ActionErrors errors = new ActionErrors();
			String result = "refresh";
			
			try {
				BrowseBean bean = (BrowseBean) BrowseBean.getInstance(request);
				CrossReferenceBean crb = CrossReferenceBean.getInstance(request);
				bean.setLastBrowseTermSkip(bean.getLastBrowseTerm().substring(bean.getSkipInFiling()));
				bean.setSkipInFiling(bean.getSkipInFiling());
				crb.setCrossReferenceList(new ArrayList());
			
				bean.refresh(
					bean.getLastBrowseTerm(),
					bean.getLastBrowseTermSkip(),
					SessionUtils.getCataloguingView(),
					SessionUtils.getMainLibrary(),
					SessionUtils.getCurrentLocale(request));
				;
			} catch (DataAccessException e) {
				addError(request, errors, "error.data.browse");
				result = "fail";
			} catch (LibrisuiteException e) {
				addError(request, errors, "error.librisuite.notInSession");
				result = "fail";
			}
			
			return mapping.findForward(result);
		}
	
	
	public ActionForward refreshFromForm(
		ActionMapping mapping,
		ActionForm actionForm,
		HttpServletRequest request,
		HttpServletResponse httpServletResponse) {
		
		logger.debug("refreshFromForm");
		
		saveBibArchiveSession(request);
		
		ActionErrors errors = new ActionErrors();
		String result = "refresh";
		BrowseForm form = (BrowseForm) actionForm;
		try {
			BrowseBean bean = (BrowseBean) BrowseBean.getInstance(request);
			bean.init(SessionUtils.getCurrentLocale(request));
			copyParametersToBean(request,bean,form);
			if (form.getSelectedIndex() != null) {
				bean.setSelectedIndex(form.getSelectedIndex());
				bean.setTermsToDisplay((form.getTermsToDisplay()));
				if (form.getSearchTerm()!=null && !form.getSearchTerm().isEmpty()){
					bean.setLastBrowseTerm(form.getSearchTerm());
				    bean.setLastBrowseTermSkip(form.getSearchTerm());
				}
				bean.setSkipInFiling(0);
				
			if((bean.getLastBrowseTerm()!=null && !bean.getLastBrowseTerm().isEmpty())){
					bean.refresh(
						bean.getLastBrowseTerm(),
						bean.getLastBrowseTermSkip(),
						SessionUtils.getCataloguingView(),
						SessionUtils.getMainLibrary(),
						SessionUtils.getCurrentLocale(request));
			 }
			String nextSortForm = getNextSortForm(bean);
			bean.setNextTerm(nextSortForm.trim());
			String lastSortForm = getLastSortForm(bean);
			bean.setLastTerm(lastSortForm.trim());
				
			}
			
			
		} catch (DataAccessException e) {
			addError(request, errors, "error.data.browse");
			result = "fail";
		} catch (LibrisuiteException e) {
			addError(request, errors, "error.librisuite.notInSession");
			result = "fail";
		} catch (Exception e) {
				addError(request, errors, "error.data.browse");
				result = "fail";
		}
		
		return mapping.findForward(result);
	}

	public ActionForward next(
		ActionMapping mapping,
		ActionForm actionForm,
		HttpServletRequest request,
		HttpServletResponse httpServletResponse) {
		
		logger.debug("next");
		ActionErrors errors = new ActionErrors();
		BrowseForm form = (BrowseForm) actionForm;
		BrowseBean bean = (BrowseBean) BrowseBean.getInstance(request);
		
		try {
			bean.setTermsToDisplay((form.getTermsToDisplay()));
			bean.next(form.getTerm(),SessionUtils.getCataloguingView(),SessionUtils.getCurrentLocale(request));
			String nextSortForm;
			try {
				nextSortForm = getNextSortForm(bean);
			} catch (ArrayIndexOutOfBoundsException e) {
				logger.error("next action error: nextSortForm is empty. Next arrived at the end of browse");
				nextSortForm = form.getTerm();
			}
			bean.setNextTerm(nextSortForm.trim());
			String lastSortForm;
			try {
				lastSortForm = getLastSortForm(bean);
			} catch (IndexOutOfBoundsException e) {
				logger.error("next action error: lastSortForm is empty. Next arrived at the end of browse");
				lastSortForm = form.getTerm();
			}
			bean.setLastTerm(lastSortForm.trim());
			
		} catch (DataAccessException e1) {
			addError(request, errors, "error.data.browse");
			return mapping.findForward("fail");
		}
		
		return mapping.findForward("next");
	}


	private String getNextSortForm(BrowseBean bean) {
		int lastIndex = bean.getBrowseList().size() - 1;
		Descriptor d = (Descriptor)bean.getBrowseList().get(lastIndex);
		String sortForm = d.getSortForm();
		return sortForm;
	}
	

	public ActionForward previous(
		ActionMapping mapping,
		ActionForm actionForm,
		HttpServletRequest request,
		HttpServletResponse httpServletResponse) {

		logger.debug("previous");
		ActionErrors errors = new ActionErrors();
		BrowseForm form = (BrowseForm) actionForm;
		BrowseBean bean = (BrowseBean) BrowseBean.getInstance(request);

		try {
			bean.setTermsToDisplay((form.getTermsToDisplay()));
			bean.previous(form.getTerm(), SessionUtils.getCataloguingView(), SessionUtils.getCurrentLocale(request));
			String lastSortForm = getLastSortForm(bean);
			bean.setLastTerm(lastSortForm.trim());
			String nextSortForm = getNextSortForm(bean);
			bean.setNextTerm(nextSortForm.trim());
		} catch (DataAccessException e1) {
			addError(request, errors, "error.data.browse");
			return mapping.findForward("fail");
		}
		
		return mapping.findForward("previous");
	}

	private String getLastSortForm(BrowseBean bean) {
		Descriptor d = (Descriptor) bean.getBrowseList().get(0);
		String sortForm = d.getSortForm();
		return sortForm;
	}


	public ActionForward initBean(
		ActionMapping mapping,
		ActionForm actionForm,
		HttpServletRequest request,
		HttpServletResponse httpServletResponse) {
			
		ActionErrors errors = new ActionErrors();
		HttpSession session = request.getSession();
		BrowseForm form = (BrowseForm) actionForm;		
		Visit visit = (Visit) request.getSession().getAttribute(HttpAttribute.VISIT);
		LocaleBean localeBean = LocaleBean.getInstance(request);
		localeBean.setLocale(visit.getPreferredLocale());
		if (form.getLanguage() != null ) {			
			Locale locale = new Locale(form.getLanguage());
			localeBean.setLocale(locale);
			session.setAttribute(localeBean.getClass().getName(), localeBean);
			SessionUtils.setCurrentLocale(session, localeBean.getLocale());	
		}
		
		try {
			initialApplication(mapping, request, httpServletResponse, localeBean);			
			BrowseBean bean = (BrowseBean) BrowseBean.getInstance(request);			
			CrossReferenceBean crb = CrossReferenceBean.getInstance(request);			
			crb.setCrossReferenceList(new ArrayList());
			copyParametersToBean(request,bean,form);			
			bean.init(SessionUtils.getCurrentLocale(request));
					
		} catch (DataAccessException e1) {
			addError(request, errors, "error.data.browse");
	    } catch (Exception e1) {
		   addError(request, errors, "error.data.browse");
	    }
		
		return mapping.findForward("initBean");
	}
	
	
	
	
	public ActionForward browseNt(ActionMapping mapping, ActionForm actionForm,
			HttpServletRequest request, HttpServletResponse httpServletResponse) 
	throws DataAccessException {

		
		logger.debug("cancel");
		String result;
		BrowseBean bean = (BrowseBean) BrowseBean.getInstance(request);
		BrowseForm form = (BrowseForm) actionForm;
		String searchTerm = form.getSearchTerm();
	
        if(bean.getSelectedIndexKey().equals("7P0")){
		  bean.setSelectedIndex(bean.getLocalisedIndex("251S"));
		  bean.setBrowseMethod("browseTitle");
        }
     
        else if(bean.getSelectedIndexKey().equals("2P0")){
           bean.setSelectedIndex(bean.getLocalisedIndex("250S"));
           bean.setBrowseMethod("browseName");
        }
	
		try {
			bean.refresh(searchTerm, SessionUtils
					.getCataloguingView(), SessionUtils
					.getMainLibrary(),
					 SessionUtils.getCurrentLocale(request));
		} catch (InvalidBrowseIndexException e) {
			e.printStackTrace();
		}

		result = "refresh";

		bean.popBrowseLinkMethod();
		
		return mapping.findForward(result);
	}
	
	private void copyParametersToBean(HttpServletRequest httpServletRequest, BrowseBean bean, BrowseForm form  ) {
	
		String view = "1";
		String scanView = httpServletRequest.getParameter("lv");
		String scanIndex = httpServletRequest.getParameter("index");
		String clctnCde  = httpServletRequest.getParameter("collection_code");		
		String term = httpServletRequest.getParameter("term");
		HttpSession session = httpServletRequest.getSession();
		session.setAttribute("collection", clctnCde);
		saveBibArchiveSession(httpServletRequest);
		try {
			if(scanView!=null && !scanView.isEmpty())
				view = BrowseValues.getString(scanView);
			
		} catch (MissingResourceException e) {
			view = "1";

		}
		if(scanView!=null && !scanView.isEmpty())
			SessionUtils.setCataloguingView(new Integer(view));
		SessionUtils.setMainLibrary(MAIN_LIBRARY);
        if(scanIndex!=null && scanIndex.length()>0){
            scanIndex =  padding(scanIndex); 
            bean.setSelectedIndex(scanIndex);
        }
        if(term!=null){
        	form.setSearchTerm("");;
            bean.setLastBrowseTerm(term);
	        bean.setLastBrowseTermSkip(term);
        }
        else{
        	bean.setLastBrowseTerm("");
 	        bean.setLastBrowseTermSkip("");
        }
        	
        if (clctnCde!=null){
        	bean.setCollectionCode(new Integer(clctnCde));
        }else{
        	bean.setCollectionCode(0);
        }
	 
	}
	
	
	private String padding(String browseIndex) {
		return browseIndex.concat("            ").substring(0, 9);
	}

	
	private void initialApplication(ActionMapping mapping,
				HttpServletRequest httpServletRequest,
				HttpServletResponse httpServletResponse,
				LocaleBean localeBean){
	
		    HttpSession session = httpServletRequest.getSession();
		    LogonBean logonBean = LogonBean.getInstance(httpServletRequest);
			CssBean cssBean = CssBean.getInstance(httpServletRequest);			
			CrossReferenceBean crossReferenceBean = CrossReferenceBean.getInstance(httpServletRequest);
			SessionUtils.setCurrentLocale(session, localeBean.getLocale());
			initSession(httpServletRequest, cssBean, localeBean, logonBean,crossReferenceBean);
		
	}
		

	
	public ActionForward refreshCollection(
			ActionMapping mapping,
			ActionForm actionForm,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		
	    HttpSession session = httpServletRequest.getSession();
	    BrowseForm form = (BrowseForm) actionForm;
		BrowseBean bean = (BrowseBean) BrowseBean.getInstance(httpServletRequest);
        bean.setCollectionCode(new Integer(form.getCollectionCode()));
 		String result="refresh"; 				
		session.setAttribute(HttpAttribute.COLLECTION, String.valueOf(form.getCollectionCode()));
		return mapping.findForward(result);
	}
		
		public ActionForward refreshFromXref(
				ActionMapping mapping,
				ActionForm actionForm,
				HttpServletRequest request,
				HttpServletResponse httpServletResponse) {
	

				logger.debug("refresh");
				ActionErrors errors = new ActionErrors();
				String result = "refresh";
				
				try {
					BrowseBean bean = (BrowseBean) BrowseBean.getInstance(request);
					CrossReferenceBean crb = CrossReferenceBean.getInstance(request);
					bean.setLastBrowseTermSkip(bean.getLastBrowseTerm().substring(bean.getSkipInFiling()));
					bean.setSkipInFiling(bean.getSkipInFiling());
					crb.setCrossReferenceList(new ArrayList());
					bean.refresh(
						bean.getLastBrowseTerm(),
						bean.getLastBrowseTermSkip(),
						SessionUtils.getCataloguingView(),
						SessionUtils.getMainLibrary(),
						SessionUtils.getCurrentLocale(request));
					;
				} catch (DataAccessException e) {
					addError(request, errors, "error.data.browse");
					result = "fail";
				} catch (LibrisuiteException e) {
					addError(request, errors, "error.librisuite.notInSession");
					result = "fail";
				}
			
				return mapping.findForward(result);
			}
		
		public ActionForward showAuths(
				ActionMapping mapping,
				ActionForm actionForm,
				HttpServletRequest request,
				HttpServletResponse response) {
			
				logger.debug("showAuths");
				ActionErrors errors = new ActionErrors();
				BrowseForm form = (BrowseForm) actionForm;
				BrowseBean bean = (BrowseBean) BrowseBean.getInstance(request);
				AuthorityBean autBean = AuthorityBean.getInstance(request);

				try {
					List l = bean.getBrowseList();
					Descriptor d = (Descriptor) l.get(form.getEntryNumber());
					String query = d.getLockingEntityType(); ;
					logger.debug("About to search on: '" + query + "'");
					Map<String, List<AuthorityNote>> autNotes;
					autNotes = autBean.getAuthorityNotesList(query, d.getHeadingNumber(), SessionUtils.getCataloguingView());
					autBean.setItemNotes(autNotes);
					autBean.setDescriptor(d);
				 } catch (DataAccessException e) {
						addError(request, errors, "error.librivision.searchFailed");
						return mapping.findForward("showAuths");
				  }
			
				return mapping.findForward("showAuths");
			}

		
			
		protected void saveBibArchiveSession(HttpServletRequest request) {
			String archiveBibView  = request.getParameter("archiveBibView");	
			HttpSession session = request.getSession();
			if (archiveBibView == null) {
				session.setAttribute("archiveBibView", "");	
			}
			else {
				session.setAttribute("archiveBibView", archiveBibView);	
			}
		}
  
	
}
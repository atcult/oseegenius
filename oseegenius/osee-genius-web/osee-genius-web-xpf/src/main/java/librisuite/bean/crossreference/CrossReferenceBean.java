/*
 * (c) LibriCore
 * 
 * Created on Jul 7, 2004
 * 
 * CrossReferenceManager.java
 */
package librisuite.bean.crossreference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.record.formula.functions.IfFunc;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import librisuite.bean.LibrisuiteBean;
import librisuite.bean.searching.BrowseBean;
import librisuite.business.codetable.CodeTableTranslationLanguage;
import librisuite.business.codetable.DAOCodeTable;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.DeleteCrossReferenceException;
import librisuite.business.common.View;
import librisuite.business.crossreference.CrossReferenceSummaryElement;
import librisuite.business.descriptor.DAODescriptor;
import librisuite.business.descriptor.Descriptor;
import librisuite.business.exception.IncompatibleCrossReferenceHeadingsException;
import librisuite.business.exception.NoHeadingSetException;
import librisuite.business.searching.BrowseManager;
import librisuite.hibernate.REF;
import librisuite.hibernate.ReferenceType;
import librisuite.hibernate.T_REF_PRNT_CNSTN;
import librisuite.hibernate.T_SINGLE_CHAR;

import com.libricore.librisuite.common.StringText;
import com.libricore.librisuite.controller.SessionUtils;

/**
 * Supports UI for Cross References
 * 
 * @author paulm
 * @since 1.0
 */
public class CrossReferenceBean extends LibrisuiteBean {

	private Locale locale;
	private Descriptor sourceDescriptor;
	private Descriptor targetDescriptor;
	private short referenceType;
	private short dualReference;
	private List crossReferenceList = new ArrayList();
	private boolean isThesaurus=false;
	private boolean attribute=false;
	private BrowseBean browseBean;
	private static final Log logger = LogFactory.getLog(CrossReferenceBean.class);
	

	public boolean isAttribute() {
		return attribute;
	}

	public void setAttribute(boolean attribute) {
		this.attribute = attribute;
	}

	public void setThesaurus(boolean isThesaurus) {
		this.isThesaurus = isThesaurus;
	}

	public static CrossReferenceBean getInstance(HttpServletRequest request) {
		CrossReferenceBean bean =
			(CrossReferenceBean) getSessionAttribute(request,
				CrossReferenceBean.class);
		if (bean == null) {
			bean = new CrossReferenceBean();
			bean.setSessionAttribute(request, bean.getClass());
		}
		bean.browseBean = BrowseBean.getInstance(request);
			
		bean.setLocale(SessionUtils.getCurrentLocale(request));
		return bean;
	}
    
	public List getSummary(Descriptor d, int cataloguingView, Locale locale, boolean isAttribute)
		throws DataAccessException {

		List xRefs = null;
		if(isAttribute)
			xRefs=((DAODescriptor) d.getDAO()).getCrossReferencesAttrib(d, cataloguingView);
		else
			xRefs=((DAODescriptor) d.getDAO()).getCrossReferences(d, cataloguingView);
			return getSummary(xRefs, cataloguingView, locale);
	}

	public Descriptor getSeeReference(Descriptor d, int cataloguingView)
		throws DataAccessException {

		return ((DAODescriptor) d.getDAO()).getSeeReference(d, cataloguingView);
	}

	/**
	 * Builds a List of CrossReferenceSummaryElements suitable for use
	 * in the presentation of cross references
	 * 
	 * @param locale - the current local (for decoding codes)
	 * @return the list
	 */
	public List getSummary(List xRefs, int cataloguingView, Locale locale)
		throws DataAccessException {
		List result = new ArrayList();
		DAOCodeTable cts = new DAOCodeTable();
		setThesaurus(false);
		Iterator iter = xRefs.iterator();
		while (iter.hasNext()) {
			REF ref = (REF) iter.next();
			Descriptor hdg =
				(Descriptor) ref.getTargetDAO().load(
					ref.getTarget(),
					cataloguingView);
			CrossReferenceSummaryElement aSummaryElement =
				new CrossReferenceSummaryElement();
			if(ref.getType() != null)
				aSummaryElement.setDecodedType(
				cts
					.load(ReferenceType.class, ref.getType(), locale)
					.getLongText());
			if(ref.getPrintConstant() != null) {
				T_SINGLE_CHAR s = cts.load(T_REF_PRNT_CNSTN.class,ref.getPrintConstant(),locale);
				if (s != null)	{
					aSummaryElement.setDecodedPrintConstant(
							s.getShortText());
				}
			}
			if(hdg.getStringText() != null)
				aSummaryElement.setTarget(
				new StringText(hdg.getStringText()).toDisplayString());
			aSummaryElement.setXRef(ref);
			aSummaryElement.setHeadingType(hdg.getHeadingType());
			aSummaryElement.setDocTargetCounts(getDocCounts(hdg,cataloguingView));
			result.add(aSummaryElement);
		}
		compressDualReferences(result, locale);

		return result;
	}
	/**
	 * Builds a List of CrossReferenceSummaryElements suitable for use
	 * in the presentation of cross references from Thesaurus
	 * 
	 * @param locale - the current local (for decoding codes)
	 * @return the list
	 */
	public List getSummaryFromThesaurus(List xRefs, int cataloguingView, Locale locale)
	throws DataAccessException {
		List result = new ArrayList();
		return result;
   }
	
	
	/**
	 * Converts adjacent see also/see also from references to the same heading
	 * into a single "Dual" reference (for display purposes only)
	 * 
	 * @param xRefs - a List of CrossReferenceSummaryElements
	 */
	private void compressDualReferences(List xRefs, Locale locale)
		throws DataAccessException {
		CrossReferenceSummaryElement prevRef = null;
		Iterator iter = xRefs.iterator();

		while (iter.hasNext()) {
			CrossReferenceSummaryElement aRef =
				(CrossReferenceSummaryElement) iter.next();
			if (prevRef != null) {
				if (prevRef.getXRef().getTarget()
					== aRef.getXRef().getTarget()) {
					prevRef.setDecodedType(
						new CodeTableTranslationLanguage((short) 672).decode(
							locale));
					iter.remove();
				}
			}
			prevRef = aRef;
		}
	}

	public List add(
		Descriptor source,
		Descriptor target,
		short referenceType,
		short dualReference,
		int cataloguingView,boolean isAttribute)
		throws DataAccessException {

		List l = new ArrayList();
		return l;
	}

	public void delete(REF ref) throws DeleteCrossReferenceException {

		try {
			ref.getDAO().delete(ref);
		} catch (DataAccessException e) {
			throw new DeleteCrossReferenceException();
		}
	}

	public void deleteByIndex(int index) throws DeleteCrossReferenceException {
		CrossReferenceSummaryElement anElem =
			(CrossReferenceSummaryElement) getCrossReferenceList().get(index);
		getCrossReferenceList().remove(index);
		delete(anElem.getXRef());
	}

	/**
	 * 
	 * @since 1.0
	 */
	public List getCrossReferenceList() {
		return crossReferenceList;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public Descriptor getSourceDescriptor() {
		return sourceDescriptor;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setCrossReferenceList(List list) {
		crossReferenceList = list;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setSourceDescriptor(Descriptor descriptor,boolean isAttribute)
		throws DataAccessException {
		sourceDescriptor = descriptor;
		setCrossReferenceList(
			getSummary(
				getSourceDescriptor(),
				View.toIntView(getSourceDescriptor().getUserViewString()),
				getLocale(),isAttribute));
	}

	/**
	 * 
	 * @since 1.0
	 */
	public Descriptor getTargetDescriptor() {
		return targetDescriptor;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setTargetDescriptor(Descriptor descriptor)
		throws IncompatibleCrossReferenceHeadingsException {
		targetDescriptor = descriptor;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public short getReferenceType() {
		return referenceType;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setReferenceType(short s) {
		referenceType = s;
	}
   //TODO non sono sicura di questa modifica da chiedea
	public boolean isDual() {
		if(isThesaurus || isAttribute())
			return false;
		else
		return ReferenceType.isSeeAlso(getReferenceType())
			|| ReferenceType.isSeeAlsoFrom(getReferenceType());
	}

	public void saveNewReference(int cataloguingView, boolean isAttribute)
		throws DataAccessException, NoHeadingSetException {
		if(getTargetDescriptor()==null)
			throw new NoHeadingSetException();
		add(
			getSourceDescriptor(),
			getTargetDescriptor(),
			getReferenceType(),
			getDualReference(),
			cataloguingView,isAttribute);
			setCrossReferenceList(
			getSummary(getSourceDescriptor(), cataloguingView, getLocale(),isAttribute));
	}
	/**
	 * 
	 * @since 1.0
	 */
	public short getDualReference() {
		return dualReference;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setDualReference(short s) {
		dualReference = s;
	}


	public int getNumberOfReferences() {
		return getCrossReferenceList().size();
	}
	
	public boolean isCanEditHeadings(){
		return getAuthorisationAgent().isPermitted("editHeadings");
	}
	

	
	private int getDocCounts(Descriptor aDescriptor, int cataloguingView)
			throws DataAccessException {
			int count = 0;
			int headingNumber = aDescriptor.getHeadingNumber();				
			String headingtype = aDescriptor.getHeadingType();
			SolrQuery query = new SolrQuery( "*:*" );				
				
			query.setFilterQueries( "authority_group_" + headingtype + ":"+ headingNumber);
			Integer collection_code = browseBean.getCollectionCode();
			if(collection_code != null && collection_code != 0){
				query.addFilterQuery( "collection_code" + ":"+ collection_code);
			}
			query.setRequestHandler("def");
			query.setRows(0);
				
			QueryResponse res;
			try {
				res = browseBean.getSolrManager().getSolr().query(query);				
				count = (int) res.getResults().getNumFound();					
			} catch (Exception e) {
				e.printStackTrace();
				logger.debug("An error occurred while trying to retrieve additional information from the Solr server");
			}
			
			return count;
		}
	
	public boolean isCrossReference(){
		if(getNumberOfReferences()>0)
			return true;
		else 
			return false;
		
	}
}

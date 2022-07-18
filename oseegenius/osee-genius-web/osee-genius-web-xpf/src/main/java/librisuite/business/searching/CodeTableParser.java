package librisuite.business.searching;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;


import librisuite.business.cataloguing.bibliographic.BIB_ITM;
import librisuite.business.codetable.ValueLabelElement;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.common.RecordNotFoundException;

import com.libricore.librisuite.controller.SessionUtils;

/**
 * This class is invoked from the ResultSummary xslt in order to get the list of
 * values in T_CAS_SDIV_GEOG_TYP,T_CAS_SDIV_PRTC_TYP,T_CAS_SDIV_TMPL_TYP
 * 
 * @author Carmen Trazza
 * @version $Revision: 1.1 $, $Date: 2008/05/28 14.57.00 $
 * @since 1.0
 */
public class CodeTableParser {
	private static final boolean isCasalini = Defaults.getBoolean(
			"customer.casalini.parser.enabled", false);

	public static String getStringText(String tbl_vlu_cde, String code,
			String lingua) {
		List subdivisionType = null;
		/* Se non è Casalini non eeseguire alcuna trasformazione */
		if (!isCasalini)
			return tbl_vlu_cde;
		/*MAURA
		if (code.equals("b"))
			subdivisionType = getTimeSubdivisionType(getLocale(lingua));
		else if (code.equals("c"))
			subdivisionType = getGeographicSubdivisionType(getLocale(lingua));
		else if (code.equals("d"))
			subdivisionType = getSpecialSubdivisionType(getLocale(lingua));
		if (subdivisionType != null) {
			Iterator iter = subdivisionType.iterator();
			while (iter.hasNext()) {
				ValueLabelElement elemento = (ValueLabelElement) iter.next();
				if (elemento.getValue().equals("" + tbl_vlu_cde))
					return elemento.getLabel();
			}
		}*/

		return tbl_vlu_cde;
	}

	public static String getStringVerificationLevel(int bibNumber,
			String lingua) {
		List encodingLevel = null;
		/* Fare la select sul DB per amicusNumber */
		/*MAURA
		DAOBibItem dao = new DAOBibItem();
		BIB_ITM bib = null;
		char verification;
		String tbl_vlu_cde ="";
		try {
			bib = dao.load(bibNumber, 1);
		} catch (RecordNotFoundException e) {
			tbl_vlu_cde ="";
		} catch (DataAccessException e) {
			tbl_vlu_cde ="";
		}
		if(bib!=null){
		 verification = bib.getVerificationLevel();
		 tbl_vlu_cde = "" +verification;
		 //encodingLevel = getVerificationLevel(getLocale(lingua));
		 if (encodingLevel != null) {
			Iterator iter = encodingLevel.iterator();
			while (iter.hasNext()) {
				ValueLabelElement elemento = (ValueLabelElement) iter.next();
				if (elemento.getValue().equals("" + tbl_vlu_cde))
					return elemento.getLabel();
			}
		}
		}
		return tbl_vlu_cde;
		*/
		return "";
	}

	public static String getStringManagerialLevelType(int bibNumber,
			String lingua) {
		List encodingLevel = null;
		//MAURA
		//DAOCasCache dao = new DAOCasCache();
		List casCache = null;
		String tbl_vlu_cde ="";
		/*MAURA
		 * try {
			casCache = dao.loadCasCache(bibNumber);
		
		} catch (DataAccessException e) {
			tbl_vlu_cde ="";
		}*/
		if(casCache!=null && casCache.size()>0){
		 //CasCache cas = (CasCache)casCache.get(0);
		 //tbl_vlu_cde = cas.getLevelCard();
		 encodingLevel = getManagerialLevelType(getLocale(lingua));
		 if (encodingLevel != null) {
			Iterator iter = encodingLevel.iterator();
			while (iter.hasNext()) {
				ValueLabelElement elemento = (ValueLabelElement) iter.next();
				if (elemento.getValue().equals("" + tbl_vlu_cde))
					return elemento.getLabel();
			}
		}
		}
		return tbl_vlu_cde;
	}

	public static String getStringEncodingLevel(String tbl_vlu_cde,
			String lingua) {
		List itemRecordType = null;
		//itemRecordType = getEncodingLevel(getLocale(lingua));
		
		Iterator iter = itemRecordType.iterator();
		while (iter.hasNext()) {
			ValueLabelElement elemento = (ValueLabelElement) iter.next();
			if (elemento.getValue().equals("" + tbl_vlu_cde)){
			  return elemento.getLabel();
			}
		}
		return tbl_vlu_cde;
	}
	public static String getStringItemBibliographicLevel(String tbl_vlu_cde,
			String lingua) {
		List itemRecordType = null;
		//itemRecordType = getItemBibliographicLevel(getLocale(lingua));
		
		
		Iterator iter = itemRecordType.iterator();
		while (iter.hasNext()) {
			ValueLabelElement elemento = (ValueLabelElement) iter.next();
		    if (elemento.getValue().equals("" + tbl_vlu_cde)){
		      return elemento.getLabel();
			}
		}
		return tbl_vlu_cde;
	}



	/**
	 * LIBRISUITE.T_CAS_SDIV_GEOG_TYP, {@link T_CAS_SDIV_GEOG_TYP},
	 * {@link GeographicSubdivisionType}
	 */
	/*
	public static List getGeographicSubdivisionType(Locale locale) {
		return CasaliniCodeListsBean.getGeographicSubdivisionType()
				.getCodeList(locale);
	}*/

	/**
	 * LIBRISUITE.T_CAS_SDIV_PRTC_TYP, {@link T_CAS_SDIV_PRTC_TYP},
	 * {@link SpecialSubdivisionType}
	 */
	/*
	public static List getSpecialSubdivisionType(Locale locale) {
		return CasaliniCodeListsBean.getSpecialSubdivisionType().getCodeList(
				locale);
	}*/

	/**
	 * LIBRISUITE.T_CAS_SDIV_TMPL_TYP, {@link T_CAS_SDIV_TMPL_TYP},
	 * {@link TimeSubdivisionType}
	 */
	/*
	public static List getTimeSubdivisionType(Locale locale) {
		return CasaliniCodeListsBean.getTimeSubdivisionType().getCodeList(
				locale);
	}*/

//	public static List getItemBibliographicLevel(Locale locale) {
//		return CodeListsBean.getItemBibliographicLevel().getCodeList(locale);
//	}
//
//	public static List getVerificationLevel(Locale locale) {
//		return CodeListsBean.getVerificationLevel().getCodeList(locale);
//
//	}
//
//	public static List getEncodingLevel(Locale locale) {
//		return CodeListsBean.getEncodingLevel().getCodeList(locale);
//	}
	
	public static List getManagerialLevelType(Locale locale) {
		//
		return null;
		//return CasaliniCodeListsBean.getManagerialLevelType().getCodeList(locale);
	}

	private static Locale getLocale(String lingua) {
		if (lingua.equalsIgnoreCase("eng"))
			return Locale.ENGLISH;
		return Locale.ITALIAN;
	}
}

/*
 * Created on 29-jul-2004
 *
 */
package librisuite.business.cataloguing.bibliographic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import librisuite.business.cataloguing.common.AccessPoint;
import librisuite.business.common.CorrelationValues;
import librisuite.business.common.DAOBibliographicCorrelation;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.common.LibrisuiteUtils;
import librisuite.business.descriptor.DAODescriptor;
import librisuite.business.descriptor.DAOSubjectDescriptor;
import librisuite.business.descriptor.Descriptor;
import librisuite.hibernate.REF;
import librisuite.hibernate.SBJCT_HDG;
import librisuite.hibernate.SubjectFunction;
import librisuite.hibernate.SubjectSource;
import librisuite.hibernate.SubjectType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.libricore.librisuite.common.StringText;

/**
 * @author paulm
 * @version $Revision: 1.19 $, $Date: 2007/02/13 09:17:59 $
 * @since 1.0
 */
/**
 * Class comment
 * 
 * @author janick
 */
public class SubjectAccessPoint extends BibliographicAccessPoint implements
		/*OrderedTag,*/ Equivalent {

	private static final Log logger = LogFactory
			.getLog(SubjectAccessPoint.class);

	private static final String VARIANT_CODES = "34eu";

	private short functionCode = -1;

	private String workRelatorCode;

	private String workRelatorStringtext;

	private int sequenceNumber;

	private SBJCT_HDG descriptor = new SBJCT_HDG();

	/**
	 * 
	 */
	public SubjectAccessPoint() {
		super();
		setFunctionCode(Defaults.getShort("subjectAccessPoint.functionCode"));
	}

	/**
	 * @param itemNbr
	 */
	public SubjectAccessPoint(int itemNbr) {
		super(itemNbr);
		setFunctionCode(Defaults.getShort("subjectAccessPoint.functionCode"));
	}

	/**
	 * 
	 */
	public short getFunctionCode() {
		return functionCode;
	}

	/**
	 * 
	 */
	public Integer getSequenceNumber() {
		return new Integer(sequenceNumber);
	}

	/**
	 * 
	 */
	public String getWorkRelatorCode() {
		return workRelatorCode;
	}

	/**
	 * 
	 */
	public String getWorkRelatorStringtext() {
		return workRelatorStringtext;
	}

	/**
	 * @param i
	 */
	public void setFunctionCode(short i) {
		functionCode = i;
	}

	/**
	 * @param integer
	 */
	public void setSequenceNumber(Integer integer) {
		sequenceNumber = 0;
		if (integer != null) {
			sequenceNumber = integer.intValue();
		}
	}

	/**
	 * @param string
	 */
	public void setWorkRelatorCode(String string) {
		workRelatorCode = string;
	}

	/**
	 * @param string
	 */
	public void setWorkRelatorStringtext(String string) {
		workRelatorStringtext = string;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.Tag#setStringText(com.libricore.librisuite.common.StringText)
	 */
	// public void setStringText(StringText stringText) {
	// TODO separate descriptor subfields from apf subfields
	// TODO flag the descriptor as "changed"?
	// descriptor.setStringText(stringText.toString());
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof SubjectAccessPoint))
			return false;
		SubjectAccessPoint other = (SubjectAccessPoint) obj;
		return super.equals(obj) && (other.functionCode == this.functionCode);
		// TODO don't know if this is right
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode(); // TODO this is bad, should be changed
	}

	/**
	 * 
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * 
	 */
	public void setDescriptor(Descriptor sbjct_hdg) {
		descriptor = (SBJCT_HDG) sbjct_hdg;
		try {
			getMarcEncoding();
		} catch (MarcCorrelationException e) {
			try {
				CorrelationValues v = getCorrelationValues();
				short v2 = new DAOBibliographicCorrelation()
				.getFirstAllowedValue2(getCategory(), v.getValue(1), v
						.getValue(3));
				setFunctionCode(v2);
			} catch (DataAccessException e1) {
				setFunctionCode((short)-1);
			}
		} catch (DataAccessException e) {
			// ignore
		} 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getFirstCorrelationList(java.util.Locale)
	 */
	public List getFirstCorrelationList() throws DataAccessException {
		return getDaoCodeTable().getList(SubjectType.class,true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getSecondCorrelationList(short,
	 *      java.util.Locale)
	 */
	public List getSecondCorrelationList(short value1)
			throws DataAccessException {
		DAOBibliographicCorrelation dao = new DAOBibliographicCorrelation();
		return dao.getSecondCorrelationList(getCategory(), value1,
				SubjectFunction.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getThirdCorrelationList(short,
	 *      short, java.util.Locale)
	 */
	public List getThirdCorrelationList(short value1, short value2)
			throws DataAccessException {
		DAOBibliographicCorrelation dao = new DAOBibliographicCorrelation();
		return dao.getThirdCorrelationList(getCategory(), value1, value2,
				SubjectSource.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.Tag#correlationChangeAffectsKey(librisuite.business.common.CorrelationValues)
	 */
	public boolean correlationChangeAffectsKey(CorrelationValues v) {
		return (v.isValueDefined(2)) && (v.getValue(2) != getFunctionCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getRequiredEditPermission()
	 */
	public String getRequiredEditPermission() {
		return "editSubject";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCorrelationValues()
	 */
	public CorrelationValues getCorrelationValues() {
		return getDescriptor().getCorrelationValues().change(2,
				getFunctionCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.Tag#setCorrelationValues(librisuite.business.common.CorrelationValues)
	 */
	public void setCorrelationValues(CorrelationValues v) {
		setFunctionCode(v.getValue(2));
		getDescriptor().setCorrelationValues(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCategory()
	 */
	public short getCategory() {
		return 4;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#getAccessPointStringText()
	 */
	public StringText getAccessPointStringText() {
		StringText text = new StringText(workRelatorStringtext);
		text.parse(workRelatorCode);
		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#setAccesspointStringText(com.libricore.librisuite.common.StringText)
	 */
	public void setAccessPointStringText(StringText stringText) {
		// TODO _JANICK externalize codes
		workRelatorCode = stringText.getSubfieldsWithCodes("4").toString();
		workRelatorStringtext = stringText.getSubfieldsWithCodes("eu")
				.toString();
	}

	public void setDescriptorStringText(StringText stringText) {
		getDescriptor().setStringText(
				stringText.getSubfieldsWithoutCodes(VARIANT_CODES).toString());
	}
	public String getVariantCodes() {
		return VARIANT_CODES;
	}
	public List replaceEquivalentDescriptor(short indexingLanguage, int cataloguingView)
	throws DataAccessException {
	DAODescriptor dao = new DAOSubjectDescriptor();
	List newTags = new ArrayList();
	Descriptor d = getDescriptor();
	REF ref = dao.getCrossReferencesWithLanguage(d, cataloguingView, indexingLanguage);
    if (ref!=null) {
		//REF aRef = (REF)refs.get(0);
		/*Deve fare il replace del descrittore, non un nuovo tag altrimenti rimuovere il tag corrente*/
		AccessPoint aTag =	(AccessPoint) (LibrisuiteUtils.deepCopy(this));
		aTag.markNew();
		aTag.setDescriptor(dao.load(ref.getTarget(),cataloguingView));
		aTag.setHeadingNumber(new Integer(aTag.getDescriptor()
							 .getKey()
							 .getHeadingNumber()));
		 newTags.add(aTag);
	}
	return newTags;
}

}

package librisuite.hibernate;

import java.io.Serializable;

import librisuite.business.cataloguing.bibliographic.SubjectAccessPoint;
import librisuite.business.common.CorrelationValues;
import librisuite.business.common.Defaults;
import librisuite.business.descriptor.DAOSubjectDescriptor;
import librisuite.business.descriptor.Descriptor;
import librisuite.business.descriptor.SkipInFiling;
import librisuite.business.descriptor.SortFormParameters;

import com.libricore.librisuite.common.HibernateUtil;
import com.libricore.librisuite.common.StringText;

/**
 * Hibernate class for table SBJCT_HDG_BRWS_EN_VW
 * @author carmen
 * @since 1.0
 */
public class SBJCT_HDG_BRWS_EN_VW extends Descriptor implements Serializable, SkipInFiling {
	private static final long serialVersionUID = 1L;
	private String copyFromHeadingType;
	private Integer copyFromHeadingNumber;
	private short skipInFiling;
	private short typeCode;
	private short sourceCode;
	private String secondarySourceCode;
	private String exclude = "bcd";
	private String separator = " -- ";

	/**
	 * 
	 * Class constructor - establishes default values for new subjects
	 *
	 * 
	 * @since 1.0
	 */
	public SBJCT_HDG_BRWS_EN_VW() {
		super();
		setAccessPointLanguage(Defaults.getShort("subject.accessPointLanguage"));
		setScriptingLanguage(Defaults.getString("subject.scriptingLanguage"));
		setTypeCode(Defaults.getShort("subject.typeCode"));
		setSourceCode(Defaults.getShort("subject.sourceCode"));
		setVerificationLevel(Defaults.getChar("subject.verificationLevel"));

	}

	/**
	 * Getter for typeCode
	 * 
	 * @return typeCode
	 */
	public short getTypeCode() {
		return typeCode;
	}

	/**
	 * Setter for typeCode
	 * 
	 * @param s typeCode
	 */
	public void setTypeCode(short s) {
		typeCode = s;
	}

	/* (non-Javadoc)
	 * @see com.libricore.librisuite.business.rdms.Descriptor#getReferenceClass()
	 */
	public Class getReferenceClass(Class targetClazz) {
		if (targetClazz == this.getClass()) {
			return SBJCT_REF.class;
		}
		else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getDefaultBrowseKey()
	 */
	public String getDefaultBrowseKey() {
		return "9P0";
	}

	public String getNextNumberKeyFieldCode() {
		return "SH";
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getDAO()
	 */
	public HibernateUtil getDAO() {
		return new DAOSubjectDescriptor();
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getAccessPointClass()
	 */
	public Class getAccessPointClass() {
		return SubjectAccessPoint.class;
	}

	/**
	 * 
	 */
	public Integer getCopyFromHeadingNumber() {
		return copyFromHeadingNumber;
	}

	/**
	 * 
	 */
	public String getCopyFromHeadingType() {
		return copyFromHeadingType;
	}

	/**
	 * 
	 */
	public String getSecondarySourceCode() {
		return secondarySourceCode;
	}

	/**
	 * 
	 */
	public short getSkipInFiling() {
		return skipInFiling;
	}

	/**
	 * 
	 */
	public short getSourceCode() {
		return sourceCode;
	}

	/**
	 * 
	 */
	public void setCopyFromHeadingNumber(Integer integer) {
		copyFromHeadingNumber = integer;
	}

	/**
	 * 
	 */
	public void setCopyFromHeadingType(String string) {
		copyFromHeadingType = string;
	}

	/**
	 * 
	 */
	public void setSecondarySourceCode(String string) {
		if (SubjectSource.isOtherSource(getSourceCode())) {
			secondarySourceCode = string;
		}
		else {
			secondarySourceCode = null;
		}
	}

	/**
	 * 
	 */
	public void setSkipInFiling(short s) {
		skipInFiling = s;
	}

	/**
	 * 
	 */
	public void setSourceCode(short s) {
		sourceCode = s;
		if (!SubjectSource.isOtherSource(s)) {
			setSecondarySourceCode(null);
		}
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getCategory()
	 */
	public short getCategory() {
		return 18;
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getCorrelationValues()
	 */
	public CorrelationValues getCorrelationValues() {
		return new CorrelationValues(typeCode, CorrelationValues.UNDEFINED, sourceCode);
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#setCorrelationValues(librisuite.business.common.CorrelationValues)
	 */
	public void setCorrelationValues(CorrelationValues v) {
		typeCode = v.getValue(1);
		sourceCode = v.getValue(3);
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getSortFormParameters()
	 */
	public SortFormParameters getSortFormParameters() {
		return new SortFormParameters(100, 103, getTypeCode(), 0, getSkipInFiling());
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.Descriptor#getHeadingNumberSearchIndex()
	 */
	public String getHeadingNumberSearchIndexKey() {
		return "229P";
	}
	public String getLockingEntityType() {
		return "SH";
	}
	
	public String getHeadingType() {
		return "subjects";
	}
	
	/**
	 * Helper method to format stringText for display
	 * 
	 * 
	 */
	public String getDisplayText() {
		return new StringText(getStringText()).toDisplayStringWithSeparator(exclude,separator);
	}
}

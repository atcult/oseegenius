/*
 * (c) LibriCore
 * 
 * Created on Dec 1, 2004
 * 
 * ControlNumberAccessPoint.java
 */
package librisuite.business.cataloguing.bibliographic;

import java.util.ArrayList;
import java.util.List;

import librisuite.business.common.CorrelationValues;
import librisuite.business.common.DAOBibliographicCorrelation;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.descriptor.Descriptor;
import librisuite.hibernate.CNTL_NBR;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.libricore.librisuite.common.StringText;

/**
 * @author paulm
 * @version $Revision: 1.8 $, $Date: 2005/12/21 13:33:35 $
 * @since 1.0
 */
public class ControlNumberAccessPoint extends BibliographicAccessPoint  {
	private static final short CONTROL_NUMBER_TAG_CATEGORY = 5;

	private CNTL_NBR descriptor = new CNTL_NBR();
	private char validationCode = 'a';
	 //private int sequenceNumber;
	private Integer sequenceNumber;
	/**
	 * Class constructor
	 *
	 * 
	 * @since 1.0
	 */
	public ControlNumberAccessPoint() {
		super();
		setFunctionCode(
			Defaults.getShort("controlNumberAccessPoint.functionCode"));
	}

	/**
	 * Class constructor
	 *
	 * @param itemNbr
	 * @since 1.0
	 */
	public ControlNumberAccessPoint(int itemNbr) {
		super(itemNbr);
		setFunctionCode(
			Defaults.getShort("controlNumberAccessPoint.functionCode"));
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#getDescriptor()
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#setDescriptor(librisuite.hibernate.Descriptor)
	 */
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = (CNTL_NBR) descriptor;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.VariableField#getStringText()
	 */
	public StringText getStringText() {
		StringText s = super.getStringText();
		if (getValidationCode() != 'a') {
			s.getSubfield(0).setCode(String.valueOf(getValidationCode()));
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.VariableField#setStringText(com.libricore.librisuite.common.StringText)
	 */
	public void setDescriptorStringText(StringText stringText) {
		descriptor.setStringText(stringText.toString());
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#getAccessPointStringText()
	 */
	public StringText getAccessPointStringText() {
		return new StringText();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#setAccesspointStringText(com.libricore.librisuite.common.StringText)
	 */
	public void setAccessPointStringText(StringText stringText) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCategory()
	 */
	public short getCategory() {
		return CONTROL_NUMBER_TAG_CATEGORY;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCorrelationValues()
	 */
	public CorrelationValues getCorrelationValues() {
		return getDescriptor().getCorrelationValues().change(
			2,
			getFunctionCode());
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getFirstCorrelationList()
	 */
	public List getFirstCorrelationList() throws DataAccessException {
		//Maura
		//return getDaoCodeTable().getListCntrl(ControlNumberType.class,true);
		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getSecondCorrelationList(short)
	 */
	/*Maura
	public List getSecondCorrelationList(short value1)
		throws DataAccessException {
		DAOBibliographicCorrelation dao = new DAOBibliographicCorrelation();
		return dao.getSecondCorrelationList(
			getCategory(),
			value1,
			ControlNumberFunction.class);
	}*/

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#setCorrelationValues(librisuite.business.common.CorrelationValues)
	 */
	public void setCorrelationValues(CorrelationValues v) {
		setFunctionCode(v.getValue(2));
		getDescriptor().setCorrelationValues(v);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#correlationChangeAffectsKey(librisuite.business.common.CorrelationValues)
	 */
	public boolean correlationChangeAffectsKey(CorrelationValues v) {
		return (v.isValueDefined(2)) && (v.getValue(2) != getFunctionCode());
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getRequiredEditPermission()
	 */
	public String getRequiredEditPermission() {
		return "editControlNumber";
	}

	/**
	 * 
	 * @since 1.0
	 */
	public char getValidationCode() {
		return validationCode;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setValidationCode(char c) {
		validationCode = c;
	}

	public Element generateModelXmlElementContent(Document xmlDocument) {
		Element content = null;
		if (xmlDocument != null) {
			content = getStringText().generateModelXmlElementContent(xmlDocument);
		}
		return content;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.Tag#getThirdCorrelationList(short, short)
	 */
	public List getThirdCorrelationList(short value1, short value2) throws DataAccessException {
		return null;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ControlNumberAccessPoint))
			return false;
		ControlNumberAccessPoint other = (ControlNumberAccessPoint) obj;
		return super.equals(obj) && (other.functionCode == this.functionCode)&& (other.descriptor.getKey().getHeadingNumber() == this.descriptor.getKey().getHeadingNumber());
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
	/*public Integer getSequenceNumber() {
		return new Integer(sequenceNumber);
	}

	public void setSequenceNumber(Integer integer) {
		sequenceNumber = 0;
		if (integer != null) {
			sequenceNumber = integer.intValue();
		}
	}*/
	public void setSequenceNumber(Integer integer) {
		sequenceNumber = integer;
	}
	/**
	 * 
	 */
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
}

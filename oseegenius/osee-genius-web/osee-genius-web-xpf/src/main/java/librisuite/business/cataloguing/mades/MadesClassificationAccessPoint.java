/*
 * (c) LibriCore
 * 
 * Created on Dec 1, 2004
 * 
 * MadesClassificationAccessPoint.java
 */
package librisuite.business.cataloguing.mades;

import java.util.List;

import librisuite.business.common.CorrelationValues;
import librisuite.business.common.DAOMadesCorrelation;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.descriptor.Descriptor;
import librisuite.hibernate.CLSTN;
import librisuite.hibernate.ClassificationFunction;
import librisuite.hibernate.ClassificationType;

import com.libricore.librisuite.common.StringText;

/**
 * @author paulm
 * @version $Revision: 1.7 $, $Date: 2005/12/21 08:30:32 $
 * @since 1.0
 */
public class MadesClassificationAccessPoint extends MadesAccessPoint {
	/**
	 * Correlation key position for function code 
	 */
	private static final int 	CLASSIFICATION_FUNCTION_CODE = 2;
	
	private static final short 	CLASSIFICATION_TAG_CATEGORY = 6;

	private CLSTN descriptor = new CLSTN();

	/**
	 * Class constructor
	 *
	 * 
	 * @since 1.0
	 */
	public MadesClassificationAccessPoint() {
		super();
		setFunctionCode(
			Defaults.getShort("classificationAccessPoint.functionCode"));
	}

	/**
	 * Class constructor
	 *
	 * @param itemNbr
	 * @since 1.0
	 */
	public MadesClassificationAccessPoint(int itemNbr) {
		super(itemNbr);
		setFunctionCode(
			Defaults.getShort("classificationAccessPoint.functionCode"));
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
		this.descriptor = (CLSTN) descriptor;
	}


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
		// nothing to do -- no apf text
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCategory()
	 */
	public short getCategory() {
		return CLASSIFICATION_TAG_CATEGORY;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCorrelationValues()
	 */
	public CorrelationValues getCorrelationValues() {
		return getDescriptor().getCorrelationValues().change(
			CLASSIFICATION_FUNCTION_CODE,
			getFunctionCode());
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getFirstCorrelationList()
	 */
	public List getFirstCorrelationList() throws DataAccessException {
		return getDaoCodeTable().getList(ClassificationType.class,false);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getSecondCorrelationList(short)
	 */
	public List getSecondCorrelationList(short value1)
		throws DataAccessException {
		DAOMadesCorrelation dao = new DAOMadesCorrelation();
		return dao.getSecondCorrelationList(
			getCategory(),
			value1,
			ClassificationFunction.class);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#setCorrelationValues(librisuite.business.common.CorrelationValues)
	 */
	public void setCorrelationValues(CorrelationValues v) {
		setFunctionCode(v.getValue(CLASSIFICATION_FUNCTION_CODE));
		getDescriptor().setCorrelationValues(v);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#correlationChangeAffectsKey(librisuite.business.common.CorrelationValues)
	 */
	public boolean correlationChangeAffectsKey(CorrelationValues v) {
		return (v.isValueDefined(CLASSIFICATION_FUNCTION_CODE)) && (v.getValue(CLASSIFICATION_FUNCTION_CODE) != getFunctionCode());
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getRequiredEditPermission()
	 */
	public String getRequiredEditPermission() {
		return "editClassNumber";
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.Tag#getThirdCorrelationList(short, short)
	 */
	public List getThirdCorrelationList(short value1, short value2) throws DataAccessException {
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "hash: "+hashCode()
			+", CORR:" 
			+ getCorrelationValues() + ", "
			+getStringText()+ ", descriptor is new: " 
			+ getDescriptor().isNew()
			+ " hdg: " + getHeadingNumber()
			+ "'"+getStringText()+"'";
	}

}

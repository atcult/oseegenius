/*
 * (c) LibriCore
 * 
 * Created on Dec 1, 2004
 * 
 * MadesControlNumberAccessPoint.java
 */
package librisuite.business.cataloguing.mades;

import java.util.ArrayList;
import java.util.List;

import librisuite.business.common.CorrelationValues;
import librisuite.business.common.DAOMadesCorrelation;
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
 * TODO _MIKE: creare un tag apposito per il tag 013 e riportare le operazioni
 * del TagHelper
 */
public class MadesControlNumberAccessPoint extends MadesAccessPoint {
	/* @deprecated  */
	// private static final String LEVEL_DESC_KEY = "livelloDesc";

	private static final short CONTROL_NUMBER_TAG_CATEGORY = 5;

	/* @deprecated  */
	// private static CorrelationValues LEVEL_DESC_CORRELATIONS = MadesTDAProvider.getInstance().getInfoFor(LEVEL_DESC_KEY).getCorrelations();
	
	private CNTL_NBR descriptor = new CNTL_NBR();
	private char validationCode = 'a';

	/**
	 * Class constructor
	 *
	 * 
	 * @since 1.0
	 */
	public MadesControlNumberAccessPoint() {
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
	public MadesControlNumberAccessPoint(int itemNbr) {
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
		//return getDaoCodeTable().getList(ControlNumberType.class,false);
		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getSecondCorrelationList(short)
	 */
	/*Maura
	public List getSecondCorrelationList(short value1)
		throws DataAccessException {
		DAOMadesCorrelation dao = new DAOMadesCorrelation();
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
	
	/** @deprecated because unused */
	/*
	public boolean isLevelDescription(){
		return getCorrelationValues().equals(LEVEL_DESC_CORRELATIONS);
	}
	*/
	
	/**
	 * TODO _MIKE: non viene controllato che esista già un descrittore perchè
	 * verrebbe calcolata la sortform e per alcuni tag va in errore la
	 * preprocess_sortform (vedi tag 013 ERR -5012 UnsupportedSubType)
	 */
	public void generateNewKey() throws DataAccessException {
		super.generateNewKey();
		if (getDescriptor().isNew()) {
			getDescriptor().getKey().setUserViewString(IntegrationConstants.CATALOGUING_VIEW_STRING);
		}
		// bibliografici:
		// TODO revisit the matching done here
		/*
		 * 1. Is this the right place to do heading matching? 2. The matching
		 * heading's view may not be single here - possible problem
		 * 
		 * if (getDescriptor().isNew()) { 
		 * 		Descriptor d = ((DAODescriptor) getDescriptor().getDAO()).getMatchingHeading( getDescriptor());
		 * 		 if (d == null) { getDescriptor().generateNewKey(); } 
		 * 			else { setDescriptor(d); } }
		 * setHeadingNumber( new Integer(getDescriptor().getKey().getHeadingNumber()));
		 */
	}

}

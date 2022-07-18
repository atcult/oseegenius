/*
 * Created on 29-jul-2004
 *
 */
package librisuite.business.cataloguing.mades;

import java.util.List;

import librisuite.business.common.CorrelationValues;
import librisuite.business.common.DAOMadesCorrelation;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.descriptor.Descriptor;
import librisuite.hibernate.NME_HDG;
import librisuite.hibernate.NameFunction;
import librisuite.hibernate.NameSubType;
import librisuite.hibernate.NameType;

import com.libricore.librisuite.common.StringText;

/**
 * @author paulm
 * @version $Revision: 1.26 $, $Date: 2006/05/03 12:33:06 $
 * @since 1.0
 */
public class MadesNameAccessPoint extends MadesNameTitleComponent {

	private static final String VARIANT_CODES = "3euox45";
	private String workRelatorCode;
	private String institution;
	private String workRelatorStringtext;
	private String otherSubfields;
	private Integer sequenceNumber;
	private NME_HDG descriptor = new NME_HDG();

	/**
	 * 
	 */
	public MadesNameAccessPoint() {
		super();
		// TODO _MIKE check defaults for nameAccessPoint.functionCode
		setFunctionCode(Defaults.getShort("nameAccessPoint.functionCode"));
	}

	/**
	 * @param itemNbr
	 */
	public MadesNameAccessPoint(int itemNbr) {
		super(itemNbr);
	}

	/**
	 * 
	 */
	public String getInstitution() {
		return institution;
	}

	/**
	 * 
	 */
	public String getOtherSubfields() {
		return otherSubfields;
	}

	/**
	 * 
	 */
	public Integer getSequenceNumber() {
		return sequenceNumber;
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
	 * @param string
	 */
	public void setInstitution(String string) {
		institution = string;
	}

	/**
	 * @param string
	 */
	public void setOtherSubfields(String string) {
		otherSubfields = string;
	}

	/**
	 * @param integer
	 */
	public void setSequenceNumber(Integer integer) {
		sequenceNumber = integer;
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

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#setStringText(com.libricore.librisuite.common.StringText)
	 */
	//	public void setStringText(StringText stringText) {
	//TODO separate descriptor subfields from apf subfields
	//TODO flag the descriptor as "changed"?
	//		descriptor.setStringText(stringText.toString());

	//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof MadesNameAccessPoint))
			return false;
		MadesNameAccessPoint other = (MadesNameAccessPoint) obj;
		return super.equals(obj)
			&& (other.getFunctionCode() == this.getFunctionCode())
			&& (other.nameTitleHeadingNumber == this.nameTitleHeadingNumber);
		//TODO don't know if this is right
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return this.getItemNumber() + this.getNameTitleHeadingNumber();
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
	public void setDescriptor(Descriptor nme_hdg) {
		descriptor = (NME_HDG) nme_hdg;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getFirstCorrelationList(java.util.Locale)
	 */
	public List getFirstCorrelationList() throws DataAccessException {
		return getDaoCodeTable().getList(NameType.class,false);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getSecondCorrelationList(short, java.util.Locale)
	 */
	public List getSecondCorrelationList(short value1)
		throws DataAccessException {
		DAOMadesCorrelation dao = new DAOMadesCorrelation();
		return dao.getSecondCorrelationList(
			getCategory(),
			value1,
			NameSubType.class);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getThirdCorrelationList(short, short, java.util.Locale)
	 */
	public List getThirdCorrelationList(short value1, short value2)
		throws DataAccessException {
		return getDaoCodeTable().getList(NameFunction.class,false);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#correlationChangeAffectsKey(librisuite.business.common.CorrelationValues)
	 */
	public boolean correlationChangeAffectsKey(CorrelationValues v) {
		return (v.isValueDefined(3) && (v.getValue(3) != getFunctionCode()));
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getRequiredEditPermission()
	 */
	public String getRequiredEditPermission() {
		return "editName";
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCorrelationValues()
	 */
	public CorrelationValues getCorrelationValues() {
		return getDescriptor().getCorrelationValues().change(
			3,
			getFunctionCode());
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#setCorrelationValues(librisuite.business.common.CorrelationValues)
	 */
	public void setCorrelationValues(CorrelationValues v) {
		setFunctionCode(v.getValue(3));
		getDescriptor().setCorrelationValues(v);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCategory()
	 */
	public short getCategory() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#getAccessPointStringText()
	 */
	public StringText getAccessPointStringText() {
		StringText text = new StringText(workRelatorStringtext);
		text.parse(otherSubfields);
		text.parse(workRelatorCode);
		text.parse(institution);
		return text;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#setAccesspointStringText(com.libricore.librisuite.common.StringText)
	 */
	public void setAccessPointStringText(StringText stringText) {
		//TODO _JANICK externalize codes
		workRelatorStringtext =
			stringText.getSubfieldsWithCodes("eu").toString();
		otherSubfields = stringText.getSubfieldsWithCodes("ox").toString();
		workRelatorCode = stringText.getSubfieldsWithCodes("4").toString();
		institution = stringText.getSubfieldsWithCodes("5").toString();
	}

	public void setDescriptorStringText(StringText stringText) {
		getDescriptor().setStringText(
			stringText.getSubfieldsWithoutCodes(VARIANT_CODES).toString());
	}
	public String getVariantCodes() {
		return VARIANT_CODES;
	}

}

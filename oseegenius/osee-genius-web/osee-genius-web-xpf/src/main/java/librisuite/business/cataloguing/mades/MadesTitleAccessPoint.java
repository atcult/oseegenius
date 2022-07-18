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
import librisuite.hibernate.TTL_HDG;
import librisuite.hibernate.TitleFunction;
import librisuite.hibernate.TitleSecondaryFunction;

import com.libricore.librisuite.common.StringText;

/**
 * @author paulm
 * @version $Revision: 1.15 $, $Date: 2006/03/28 12:44:16 $
 * @since 1.0
 */
public class MadesTitleAccessPoint extends MadesNameTitleComponent {

	private static final String VARIANT_CODES = "3civ5";
	private String institution;
	private Integer SeriesIssnHeadingNumber;
	private short secondaryFunctionCode;
	private String volumeNumberDescription;
	private String variantTitle;
	private TTL_HDG descriptor = new TTL_HDG();

	/**
	 * 
	 */
	public MadesTitleAccessPoint() {
		super();
		setFunctionCode(Defaults.getShort("titleAccessPoint.functionCode"));
		setSecondaryFunctionCode(
			Defaults.getShort("titleAccessPoint.secondaryFunctionCode"));
	}

	/**
	 * @param itemNbr
	 */
	public MadesTitleAccessPoint(int itemNbr) {
		super(itemNbr);
	}

	/**
	 * 
	 */
	public String getInstitution() {
		return institution;
	}

	/**
	 * @param string
	 */
	public void setInstitution(String string) {
		institution = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + functionCode;
		result = PRIME * result + nameTitleHeadingNumber;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MadesTitleAccessPoint other = (MadesTitleAccessPoint) obj;
		if (functionCode != other.functionCode)
			return false;
		if (nameTitleHeadingNumber != other.nameTitleHeadingNumber)
			return false;
		return true;
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
	public void setDescriptor(Descriptor ttl_hdg) {
		descriptor = (TTL_HDG) ttl_hdg;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getFirstCorrelationList(java.util.Locale)
	 */
	public List getFirstCorrelationList() throws DataAccessException {
		return getDaoCodeTable().getList(TitleFunction.class,false);
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
			TitleSecondaryFunction.class);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getThirdCorrelationList(short, short, java.util.Locale)
	 */
	public List getThirdCorrelationList(short value1, short value2)
		throws DataAccessException {
		return null;
	}

	/**
	 * 
	 */
	public short getSecondaryFunctionCode() {
		return secondaryFunctionCode;
	}

	/**
	 * 
	 */
	public Integer getSeriesIssnHeadingNumber() {
		return SeriesIssnHeadingNumber;
	}

	/**
	 * 
	 */
	public String getVariantTitle() {
		return variantTitle;
	}

	/**
	 * 
	 */
	public String getVolumeNumberDescription() {
		return volumeNumberDescription;
	}

	/**
	 * 
	 */
	public void setSecondaryFunctionCode(short s) {
		secondaryFunctionCode = s;
	}

	/**
	 * 
	 */
	public void setSeriesIssnHeadingNumber(Integer integer) {
		SeriesIssnHeadingNumber = integer;
	}

	/**
	 * 
	 */
	public void setVariantTitle(String string) {
		variantTitle = string;
	}

	/**
	 * 
	 */
	public void setVolumeNumberDescription(String string) {
		volumeNumberDescription = string;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getRequiredEditPermission()
	 */
	public String getRequiredEditPermission() {
		return "editTitle";
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#correlationChangeAffectsKey(librisuite.business.common.CorrelationValues)
	 */
	public boolean correlationChangeAffectsKey(CorrelationValues v) {
		return (v.isValueDefined(1) && (v.getValue(1) != getFunctionCode()));
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCorrelationValues()
	 */
	public CorrelationValues getCorrelationValues() {
		return getDescriptor()
			.getCorrelationValues()
			.change(1, getFunctionCode())
			.change(2, getSecondaryFunctionCode());
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#setCorrelationValues(librisuite.business.common.CorrelationValues)
	 */
	public void setCorrelationValues(CorrelationValues v) {
		setFunctionCode(v.getValue(1));
		setSecondaryFunctionCode(v.getValue(2));
		getDescriptor().setCorrelationValues(v);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#getAccessPointStringText()
	 */
	public StringText getAccessPointStringText() {
		//TODO _JANICK to the Dark Side, this issn number leads
		StringText text = new StringText(variantTitle);
		text.parse(volumeNumberDescription);
		text.parse(institution);
		return text;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#setAccesspointStringText(com.libricore.librisuite.common.StringText)
	 */
	public void setAccessPointStringText(StringText stringText) {
		//		TODO _JANICK externalize codes
		variantTitle = stringText.getSubfieldsWithCodes("ci").toString();
		volumeNumberDescription =
			stringText.getSubfieldsWithCodes("v").toString();
		institution = stringText.getSubfieldsWithCodes("5").toString();
	}

	public void setDescriptorStringText(StringText stringText) {
		getDescriptor().setStringText(
			stringText.getSubfieldsWithoutCodes(VARIANT_CODES).toString());
	}
	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#getCategory()
	 */
	public short getCategory() {
		return 3;
	}
	public String getVariantCodes() {
		return VARIANT_CODES;
	}

}

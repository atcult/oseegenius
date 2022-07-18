/*
 * (c) LibriCore
 * 
 * Created on Dec 12, 2005
 * 
 * NameTitleAccessPoint.java
 */
package librisuite.business.cataloguing.mades;

import java.util.ArrayList;
import java.util.List;

import librisuite.business.common.CorrelationValues;
import librisuite.business.common.DAOMadesCorrelation;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.descriptor.Descriptor;
import librisuite.hibernate.NME_TTL_HDG;
import librisuite.hibernate.NameSubType;
import librisuite.hibernate.NameType;

import com.libricore.librisuite.common.HibernateUtil;
import com.libricore.librisuite.common.StringText;

/**
 * @author paulm
 * @version $Revision: 1.2 $, $Date: 2006/01/05 13:25:58 $
 * @since 1.0
 */
public class MadesNameTitleAccessPoint extends MadesNameTitleComponent {

	private static final DAOMadesNameTitleAccessPoint daoNameTitleAccessPoint =
		new DAOMadesNameTitleAccessPoint();
	private static final String VARIANT_CODES = "3v5";
	private NME_TTL_HDG descriptor = new NME_TTL_HDG();
	private String institution;
	private Short secondaryFunctionCode;
	/*
	 * Note that the Issn in name/title was not converted when the one in titles was.
	 * This was an oversight, but for now, the data here is still a string.  We are 
	 * not correcting it now, because it is also being considered to re-convert all 
	 * ttl issn's back to a string (rather than a foreign key to CNRL_NBR)
	 */
	private String seriesIssnHeadingNumber;
	private String volumeNumberDescription;

	/**
	 * Class constructor
	 *
	 * 
	 * @since 1.0
	 */
	public MadesNameTitleAccessPoint() {
		super();
		// TODO _MIKE check defaults for nameTitleAccessPoint.functionCode
		setFunctionCode(Defaults.getShort("nameTitleAccessPoint.functionCode"));
		// TODO _MIKE check defaults for nameTitleAccessPoint.secondaryFunctionCode
		setSecondaryFunctionCode(
			new Short(
				Defaults.getShort(
					"nameTitleAccessPoint.secondaryFunctionCode")));
	}

	/**
	 * Class constructor
	 *
	 * @param itemNbr
	 * @since 1.0
	 */
	public MadesNameTitleAccessPoint(int itemNbr) {
		super(itemNbr);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#getAccessPointStringText()
	 */
	public StringText getAccessPointStringText() {
		//TODO _JANICK to the Dark Side, this issn number leads
		StringText text = new StringText(volumeNumberDescription);
		text.parse(institution);
		return text;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#getCorrelationValues()
	 */
	public CorrelationValues getCorrelationValues() {
		/*
		 * NameTitleAccessPoints in bib have function code first in the correlation
		 * so we move down the heading correlation values.  c.f. Authority tags where
		 * the NT heading correlation has the descriptor correlation values first.
		 */
		CorrelationValues v = getDescriptor().getCorrelationValues();
		v = v.change(3, v.getValue(2));
		v = v.change(2, v.getValue(1));
		v = v.change(1, getFunctionCode());
		return v;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.Browsable#getDescriptor()
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#getFirstCorrelationList()
	 */
	public List getFirstCorrelationList() throws DataAccessException {
		//MAURA
		//return getDaoCodeTable().getList(T_NME_TTL_FNCTN.class,false);
		return new ArrayList();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public String getInstitution() {
		return institution;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public Short getSecondaryFunctionCode() {
		return secondaryFunctionCode;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#getSecondCorrelationList(short)
	 */
	public List getSecondCorrelationList(short value1)
		throws DataAccessException {
		DAOMadesCorrelation dao = new DAOMadesCorrelation();
		return dao.getSecondCorrelationList(
			getCategory(),
			value1,
			NameType.class);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public String getSeriesIssnHeadingNumber() {
		return seriesIssnHeadingNumber;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#getThirdCorrelationList(short, short)
	 */
	public List getThirdCorrelationList(short value1, short value2)
		throws DataAccessException {
		DAOMadesCorrelation dao = new DAOMadesCorrelation();
		return dao.getThirdCorrelationList(
			getCategory(),
			value1,
			value2,
			NameSubType.class);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public String getVolumeNumberDescription() {
		return volumeNumberDescription;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#setAccesspointStringText(com.libricore.librisuite.common.StringText)
	 */
	public void setAccessPointStringText(StringText stringText) {
		//		TODO _JANICK externalize codes
		// TODO _MIKE check volumeNumberDescription and institution codes
		volumeNumberDescription =
			stringText.getSubfieldsWithCodes("v").toString();
		institution = stringText.getSubfieldsWithCodes("5").toString();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#setCorrelationValues(librisuite.business.common.CorrelationValues)
	 */
	public void setCorrelationValues(CorrelationValues v) {
		setFunctionCode(v.getValue(1));
		v = v.change(1, v.getValue(2));
		v = v.change(2, v.getValue(3));
		v = v.change(3, CorrelationValues.UNDEFINED);
		getDescriptor().setCorrelationValues(v);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.Browsable#setDescriptor(librisuite.business.descriptor.Descriptor)
	 */
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = (NME_TTL_HDG) descriptor;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setDescriptor(NME_TTL_HDG nme_ttl_hdg) {
		descriptor = nme_ttl_hdg;
	}

	public void setDescriptorStringText(StringText stringText) {
		// TODO _MIKE check descriptor code
		getDescriptor().setStringText(
			stringText.getSubfieldsWithoutCodes(VARIANT_CODES).toString());
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setInstitution(String string) {
		institution = string;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setSecondaryFunctionCode(Short s) {
		secondaryFunctionCode = s;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setSeriesIssnHeadingNumber(String s) {
		seriesIssnHeadingNumber = s;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setVolumeNumberDescription(String string) {
		volumeNumberDescription = string;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.Tag#getDAO()
	 */
	public HibernateUtil getDAO() {
		return daoNameTitleAccessPoint;
	}
	public String getVariantCodes() {
		return VARIANT_CODES;
	}

}

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
import librisuite.business.descriptor.DAOTitleDescriptor;
import librisuite.business.descriptor.Descriptor;
import librisuite.hibernate.REF;
import librisuite.hibernate.TTL_HDG;
import librisuite.hibernate.TitleFunction;
import librisuite.hibernate.TitleSecondaryFunction;

import com.libricore.librisuite.common.StringText;
import com.libricore.librisuite.common.Subfield;

/**
 * MIKE: 20071218: now it implements MarcHelperTag
 * @author paulm
 * @version $Revision: 1.15 $, $Date: 2006/03/28 12:44:16 $
 * @since 1.0
 */
public class TitleAccessPoint extends NameTitleComponent implements /*MarcHelperTag,OrderedTag,*/Equivalent {

	private String institution;
	private Integer seriesIssnHeadingNumber;
	private short secondaryFunctionCode;
	private String volumeNumberDescription;
	private String variantTitle;
	private TTL_HDG descriptor = new TTL_HDG();
	private static final String VARIANT_CODES = "3civ5";
	//private int sequenceNumber;
	private Integer sequenceNumber;


	/**
	 * 
	 */
	public TitleAccessPoint() {
		super();
		setFunctionCode(Defaults.getShort("titleAccessPoint.functionCode"));
		setSecondaryFunctionCode(
			Defaults.getShort("titleAccessPoint.secondaryFunctionCode"));
	}

	/**
	 * @param itemNbr
	 */
	public TitleAccessPoint(int itemNbr) {
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof TitleAccessPoint))
			return false;
		TitleAccessPoint other = (TitleAccessPoint) obj;
		return super.equals(obj)
			&& (other.getFunctionCode() == this.getFunctionCode())
			&& (other.nameTitleHeadingNumber == this.nameTitleHeadingNumber);
		//TODO don't know if this is right
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode(); //TODO this is bad, should be changed
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
		return getDaoCodeTable().getList(TitleFunction.class,true);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getSecondCorrelationList(short, java.util.Locale)
	 */
	public List getSecondCorrelationList(short value1)
		throws DataAccessException {
		DAOBibliographicCorrelation dao = new DAOBibliographicCorrelation();
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
		return seriesIssnHeadingNumber;
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
		seriesIssnHeadingNumber = integer;
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
		text.parse(institution);
		if(this.getSeriesIssnHeadingNumber()!=null)
			text.addSubfield(new Subfield("x",""+getISSNText()));
		text.parse(volumeNumberDescription);
		return text;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.AccessPoint#setAccesspointStringText(com.libricore.librisuite.common.StringText)
	 */
	public void setAccessPointStringText(StringText stringText) {
		//		TODO _JANICK externalize codes
		variantTitle = stringText.getSubfieldsWithCodes("ci").toString();
		institution = stringText.getSubfieldsWithCodes("5").toString();
		if(!stringText.getSubfieldsWithCodes("x").isEmpty() && this.getSeriesIssnHeadingNumber()!=null)
		  	seriesIssnHeadingNumber=new Integer(this.getSeriesIssnHeadingNumber().intValue());
		else
		  	seriesIssnHeadingNumber=null;
		volumeNumberDescription =
			stringText.getSubfieldsWithCodes("v").toString();

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

	public String getKey() throws DataAccessException, MarcCorrelationException {
		return getMarcEncoding().getMarcTag();
	}
	
	public String getISSNText(){
		DAOTitleDescriptor daoTitle= new DAOTitleDescriptor();
		return daoTitle.getISSNString(this.getSeriesIssnHeadingNumber().intValue());
	
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
	public List  replaceEquivalentDescriptor(short indexingLanguage, int cataloguingView)
	throws DataAccessException {
	DAODescriptor dao = new DAOTitleDescriptor();
	List newTags = new ArrayList();
	Descriptor d = getDescriptor();
	REF ref = dao.getCrossReferencesWithLanguage(d, cataloguingView, indexingLanguage);
    if (ref!=null) {
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

/*
 * (c) LibriCore
 * 
 * Created on Aug 3, 2004
 * 
 * AccessPoint.java
 */
package librisuite.business.cataloguing.bibliographic;

import librisuite.business.cataloguing.common.AccessPoint;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.PersistentObjectWithView;
import librisuite.business.common.UserViewHelper;

import com.libricore.librisuite.common.StringText;

/**
 * @author paulm
 * @version %I%, %G%
 */
public abstract class BibliographicAccessPoint extends AccessPoint
	implements PersistentObjectWithView {

	private String materialSpecified;
	private UserViewHelper userViewHelper = new UserViewHelper();

	public void setStringText(StringText stringText) {
		materialSpecified = stringText.getSubfieldsWithCodes("3").toString();
		super.setStringText(stringText);
	}

	public StringText getStringText() {
		StringText result = super.getStringText();
		result.parse(materialSpecified);
		return result;
	}

	/**
	 * Class constructor
	 *
	 * 
	 */
	public BibliographicAccessPoint() {
		super();
	}

	/**
	 * Class constructor
	 *
	 * @param itemNbr
	 */
	public BibliographicAccessPoint(int itemNumber) {
		super(itemNumber);
	}


	public void generateNewKey() throws DataAccessException {
		super.generateNewKey();
		if (getDescriptor().isNew()) {
			getDescriptor().getKey().setUserViewString(getUserViewString());
		}
		setUserViewString(getDescriptor().getKey().getUserViewString());
	}

	/**
	 * 
	 */
	public String getMaterialSpecified() {
		return materialSpecified;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public String getUserViewString() {
		return userViewHelper.getUserViewString();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setUserViewString(String s) {
		userViewHelper.setUserViewString(s);
	}

	public int getBibItemNumber() {
		return getItemNumber();
	}

	public void setBibItemNumber(int itemNumber) {
		setItemNumber(itemNumber);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setMaterialSpecified(String string) {
		materialSpecified = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			BibliographicAccessPoint anApf = (BibliographicAccessPoint)obj;
			return this.getUserViewString().equals(anApf.getUserViewString());
		}
		else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() + getUserViewString().hashCode();
	}

}

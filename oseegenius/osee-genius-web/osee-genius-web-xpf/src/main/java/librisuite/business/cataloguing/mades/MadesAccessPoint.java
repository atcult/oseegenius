package librisuite.business.cataloguing.mades;

import librisuite.business.cataloguing.common.AccessPoint;

import com.libricore.librisuite.common.StringText;

/**
 * @author michelem
 *
 */
public abstract class MadesAccessPoint extends AccessPoint implements POWithMadesView {

	private String materialSpecified;

	/** Mades User View */
	private int madUserView;

	public void setStringText(StringText stringText) {
		// TODO _MIKE materialSpecified: subfieldsWithCodes = 3?
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
	public MadesAccessPoint() {
		super();
	}

	/**
	 * Class constructor
	 * 
	 * @param itemNbr
	 */
	public MadesAccessPoint(int itemNumber) {
		super(itemNumber);
	}

	/**
	 * 
	 */
	public String getMaterialSpecified() {
		return materialSpecified;
	}

	public int getMadItemNumber() {
		return getItemNumber();
	}

	public void setMadItemNumber(int itemNumber) {
		setItemNumber(itemNumber);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setMaterialSpecified(String string) {
		materialSpecified = string;
	}

	/**
	 * @return Returns the madUserView.
	 */
	public int getMadUserView() {
		return madUserView;
	}

	/**
	 * @param madUserView The madUserView to set.
	 */
	public void setMadUserView(int madUserView) {
		this.madUserView = madUserView;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + madUserView;
		return result;
	}

	/* TODO _MIKE verificare, potrebbe essere troppo limitativo
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MadesAccessPoint other = (MadesAccessPoint) obj;
		if (madUserView != other.madUserView)
			return false;
		return true;
	}
}

/*
 * (c) LibriCore
 *
 * Created on 21-jun-2004
 *
 * MAD_SHLF_LIST_ACS_PNT.java
 */
package librisuite.business.cataloguing.mades;

import java.io.Serializable;

/**
 * @author Usuario
 * @version $Revision: 1.3 $, $Date: 2004/12/02 17:20:52 $
 * @since 1.0
 */
public class MAD_SHLF_LIST_ACS_PNT implements Serializable {
    //KEY
	private int shelfListKeyNumber;
	private int madItemNumber;
	
	private int mainLibraryNumber;

	/**
	 * Class constructor
	 *
	 *
	 * @since 1.0
	 */
	public MAD_SHLF_LIST_ACS_PNT() {
		super();
	}

	public MAD_SHLF_LIST_ACS_PNT(
		int madItemNumber,
		int mainLibraryNumber,
		int shelfListKeyNumber
		) {
		setMadItemNumber(madItemNumber);
		setMainLibraryNumber(mainLibraryNumber);
		setShelfListKeyNumber(shelfListKeyNumber);
	}

	/**
	 * @return madItemNumber
	 */
	public int getMadItemNumber() {
		return madItemNumber;
	}

	/**
	 * @return mainLibraryNumber
	 */
	public int getMainLibraryNumber() {
		return mainLibraryNumber;
	}

	/**
	 * @return shelfListKeyNumber
	 */
	public int getShelfListKeyNumber() {
		return shelfListKeyNumber;
	}
	/**
	 * @return shelfListKeyNumber
	 */
	public int getHeadingNumber() {
		return shelfListKeyNumber;
	}

	/**
	 * @param i madItemNumber
	 */
	public void setMadItemNumber(int i) {
		madItemNumber = i;
	}

	/**
	 * @param i mainLibraryNumber
	 */
	public void setMainLibraryNumber(int i) {
		mainLibraryNumber = i;
	}

	/**
	 * @param i shelfListKeyNumber
	 */
	public void setShelfListKeyNumber(int i) {
		shelfListKeyNumber = i;
	}	
	
	/**
	 * @param i shelfListKeyNumber
	 */
	public void setHeadingNumber(int i) {
		shelfListKeyNumber = i;
	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + madItemNumber;
		result = PRIME * result + shelfListKeyNumber;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MAD_SHLF_LIST_ACS_PNT other = (MAD_SHLF_LIST_ACS_PNT) obj;
		if (madItemNumber != other.madItemNumber)
			return false;
		if (shelfListKeyNumber != other.shelfListKeyNumber)
			return false;
		return true;
	}

}

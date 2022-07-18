/*
 * (c) LibriCore
 * 
 * Created on Nov 18, 2005
 * 
 * BibliographicHeaderFieldHelper.java
 */
package librisuite.business.cataloguing.bibliographic;

import librisuite.business.cataloguing.common.HeaderFieldHelper;


/**
 * @author paulm
 * @version $Revision: 1.2 $, $Date: 2005/12/01 13:50:04 $
 * @since 1.0
 */
public class BibliographicHeaderFieldHelper extends HeaderFieldHelper {

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.HeaderField#getCategory()
	 */
	public short getCategory() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.HeaderFieldHelper#getHeaderListClass()
	 */
	public Class getHeaderListClass() {
		//Maura
		//return T_BIB_HDR.class;
		return null;
	}

}

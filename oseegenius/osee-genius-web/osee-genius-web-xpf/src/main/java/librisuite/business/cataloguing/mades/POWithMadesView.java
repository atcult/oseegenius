/*
 * (c) Primesource
 * 
 * Created on Mar 05, 2007
 * 
 * POWithMadesView.java
 */
package librisuite.business.cataloguing.mades;

import librisuite.business.common.Persistence;

/**
 * @author michelem
 * @version $Revision: 1.2 $, $Date: 2005/12/12 12:54:37 $
 * @since 1.0
 */
public interface POWithMadesView extends Persistence {

	abstract public int getMadUserView();
	
	abstract public void setMadUserView(int madesUserView);
	
}

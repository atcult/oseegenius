/**
 * Class for folio costants.
 */
package com.atc.osee.web.tools;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jfree.util.Log;

import com.atc.osee.web.IConstants;

/**
 * @author natascia
 *
 */
public class FolioConfigurationTool implements Serializable {

	private static final long serialVersionUID = 3169263177917506245L;
	
	ResourceBundle bundle = null;
	
	public FolioConfigurationTool() {
		try {
			if (System.getProperty("folio.env") != null && System.getProperty("folio.env").equalsIgnoreCase("prod"))
				bundle = ResourceBundle.getBundle(IConstants.FOLIO_BUNDLE_NAME, Locale.getDefault());					
			else
				bundle = ResourceBundle.getBundle(IConstants.FOLIO_TEST_BUNDLE_NAME, Locale.getDefault());
		} catch (Exception e) {
			Log.info("Folio Module disabled");
		}
	}
	
	/**
     * Accepts objects and uses their string value as the key.
     * If the key is null or no corresponding value will be found 
     * this method will return null.
     * 
     * @see Key#get(String)
     * @param objectKey the key as an object.
     * @return the Key associated with the given key.
     */
    public String getString(final String key)
    {        
    	if (key == null) return null;
        return bundle.getString(key);
    }
}

/*
 * (c) LibriCore
 * 
 * Created on Jul 17, 2004
 * 
 * View.java
 */
package librisuite.business.common;


/**
 * A collection of static methods to manage user views
 * @author paulm
 * MIKE: added view for mades
 * @version %I%, %G%
 * @since 1.0
 */
public class View {

	public static final int AUTHORITY = -1;
	public static final int DEFAULT_BIBLIOGRAPHIC_VIEW = 1;
	
	/**
	 * Creates a new usr_vw_ind string from the input string by
	 * setting the position specified in arg2 to '0'.  The resultant
	 * view string is useful in saving a persistant object after the
	 * current cataloguing view of the record is deleted (or modified)
	 * 
	 * 
	 * @param viewString -- the original view String
	 * @param cataloguingView -- the position to be set to '0' (1 indexing)
	 */
		static public String maskOutViewString(String viewString, int cataloguingView) {
			if(cataloguingView<0) throw new IllegalArgumentException("view "+cataloguingView+" cannot be converted in string");
			char[] newArray = viewString.toCharArray();
			newArray[cataloguingView - 1] = '0';
			return new String(newArray);
		}

		/**
		 * Creates a new usr_vw_ind string from the input string by
		 * setting the position specified in arg2 to '1'.  The resultant
		 * view string is useful in saving a persistant object after the
		 * current cataloguing view of the record is added (based on a copy from
		 * existing views);
		 * 
		 * 
		 * @param viewString -- the original view String
		 * @param cataloguingView -- the position to be set to '1' (1 indexing)
		 */

		static public String maskOnViewString(String viewString, int cataloguingView) {
			if(cataloguingView<0) throw new IllegalArgumentException("view "+cataloguingView+" cannot be converted in string");
			char[] newArray = viewString.toCharArray();
			newArray[cataloguingView - 1] = '1';
			return new String(newArray);
		}
	/**
	 * Creates a new usr_vw_ind string by
	 * setting all positions to '0' except the position specified in arg1.  
	 * The resultant view string is useful in saving a persistant object after the
	 * current cataloguing view of the record is saved or updated;
	 * 
	 * 
	 * @param cataloguingView -- the position to be set to '1' (1 indexing)
	 */

	static public String makeSingleViewString(int cataloguingView) {
		
		return maskOnViewString("0000000000000000", cataloguingView);
	}

	/**
	 * Determines the equivalent integer value view from the (single) view string
	 * 
	 * @param cataloguingView -- the position to be set to '1' (1 indexing)
	 */

	static public short toIntView(String userViewString) {
		return (short)(1 + userViewString.indexOf("1"));
	}

}

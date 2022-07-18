/*
 * (c) LibriCore
 * 
 * Created on Jun 17, 2004
 * 
 * ReferenceType.java
 */
package librisuite.hibernate;

import librisuite.business.codetable.DAOCodeTable;
import librisuite.business.common.DataAccessException;

/**
 * Superclass for single table Codetables (short code)
 * @author paulm
 * @version $Revision: 1.7 $, $Date: 2006/01/11 13:36:22 $
 * @since 1.0
 */
public abstract class T_SINGLE extends CodeTable {
	private short code;

	/**
	 * Getter for code
	 * 
	 * @return code
	 */
	public short getCode() {
		return code;
	}

	/**
	 * Setter for code
	 * 
	 * @param s code
	 */
	public void setCode(short s) {
		code = s;
	}

	/* (non-Javadoc)
	 * @see librisuite.hibernate.CodeTable#getCodeString()
	 */
	public String getCodeString() {
		return String.valueOf(code);
	}

	public boolean equals(Object obj) {
				if (!(obj instanceof T_SINGLE))
					return false;
				return (((T_SINGLE) obj).getCode() == getCode())
					&& (((T_SINGLE) obj).getLanguage().equals(getLanguage()));			
			}

			/* (non-Javadoc)
			 * @see java.lang.Object#hashCode()
			 */
	public int hashCode() {
				return getCode() + getLanguage().hashCode();
			}

	public void setExternalCode(Object extCode) {
		if(extCode instanceof String){
//			20100923 inizio: va in NumberFormatException se la tabella e' vuota e quindu extCode e' spazio
			if (((String) extCode).trim().length()>0){
				code = Short.parseShort((String)extCode);
			}else{
				code = 1;
			}
//			code = Short.parseShort((String)extCode);
//			20100923 fine
			
		} else if(extCode instanceof Short){
			code = ((Short)extCode).shortValue();
		}
	}
	
	public int getNextNumber() throws DataAccessException{
		return new DAOCodeTable().suggestNewCode(this);
	}
}

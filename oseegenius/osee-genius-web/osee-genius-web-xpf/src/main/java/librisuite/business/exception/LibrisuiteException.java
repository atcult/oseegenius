/*
 * (c) LibriCore
 * 
 * Created on Jul 1, 2004
 * 
 * LibrisuiteException.java
 */
package librisuite.business.exception;

/**
 * @author Wim Crols
 * @version $Revision: 1.3 $, $Date: 2004/07/12 10:15:16 $
 * @since 1.0
 */
public class LibrisuiteException extends Exception {

	/**
	 * @see java.lang.Exception#Exception()
	 */
	public LibrisuiteException() {
		super();
	}

	/**
	 * @see java.lang.Exception#Exception(String)
	 */
	public LibrisuiteException(String message) {
		super(message);
	}

	/**
	 * @see java.lang.Exception#Exception(String, Throwable)
	 */
	public LibrisuiteException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see java.lang.Exception#Exception(Throwable)
	 */
	public LibrisuiteException(Throwable cause) {
		super(cause);
	}

}

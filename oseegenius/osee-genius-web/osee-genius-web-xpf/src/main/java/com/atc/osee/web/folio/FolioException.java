package com.atc.osee.web.folio;

/**
 * Thrown to capture error message in folio API
 * 
 * @author aguercio
 * @since 2.0
 */
public class FolioException extends Exception {
	
	private static final long serialVersionUID = -62444076542753L;
	
	/**
	 * Builds a new exception with a given cause.
	 * 
	 * @param cause the exception cause.
	 */
	
	public FolioException(final String message) 
	{
		super(message);
	}
}

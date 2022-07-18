package com.atc.osee.web.plugin;

/**
 * A hold cannot be created for a given user.
 * The exception holds an internal error code that is coming from Olisuite and
 * will be translated in an errore message here on OseeGeniusSide
 * 
 * @author agazzarini
 * @since 1.0
 */
public class HoldCannotBeCreatedException extends Exception 
{
	private static final long serialVersionUID = -1321448067045661834L;

	private final int reasonCode;
	
	/**
	 * Builds a new {@link HoldCannotBeCreatedException} with the given reason code.
	 * 
	 * @param reasonCode the reason code.
	 */
	public HoldCannotBeCreatedException(final int reasonCode)
	{
		this.reasonCode = reasonCode;
	}
	
	/**
	 * Returns the reason code of this exception.
	 * 
	 * @return the reason code of this exception.
	 */
	public int getReasonCode()
	{
		return reasonCode;
	}
}

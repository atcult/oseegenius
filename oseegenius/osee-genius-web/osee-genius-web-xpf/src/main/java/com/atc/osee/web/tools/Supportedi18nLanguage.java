package com.atc.osee.web.tools;

/**
 * A simple value object encapsulating a language code and (relative) icon path.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class Supportedi18nLanguage 
{
	private final String languageCode;
	private final String icon;
	
	/**
	 * Builds a new supported language with the given data.
	 * 
	 * @param languageCode the language code.
	 * @param icon the icon.
	 */
	Supportedi18nLanguage(final String languageCode, final String icon) 
	{
		this.languageCode = languageCode;
		this.icon = icon;
	}

	/**
	 * Returns the language code associated with this value object.
	 * 
	 * @return the language code associated with this value object.
	 */
	public String getLanguageCode() 
	{
		return languageCode;
	}

	/**
	 * Returns the icon associated with this value object.
	 * 
	 * @return the icon associated with this value object.
	 */
	public String getIcon() 
	{
		return icon;
	}
}
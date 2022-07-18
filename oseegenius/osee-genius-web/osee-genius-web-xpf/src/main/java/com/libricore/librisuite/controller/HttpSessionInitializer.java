/*
 * Created on 14-mei-2004
 *
 */
package com.libricore.librisuite.controller;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ensures that a new session is correctly initialized. A new
 * PresentationStack and default locale have to be bound as attributes to the
 * new session
 * 
 * @author janick
 */
public class HttpSessionInitializer implements HttpSessionListener {

	private static final Log logger = LogFactory
			.getLog(HttpSessionInitializer.class);


	/**
	 * default constructor (called by servlet container)
	 */
	public HttpSessionInitializer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent event) {
		if (logger.isInfoEnabled())
			logger.info("New HttpSession created");

		HttpSession session = event.getSession();
		//TODO check if the configuration file is present, if it is then use the locale defined in there othwerwhise don't set the locale
		//SessionUtils.setCurrentLocale(session, getDefaultLocale());
		session.setAttribute("newSession", new Boolean(true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent event) {
		if (logger.isInfoEnabled())
			logger.info("HttpSession destroyed");
		HttpSession session = event.getSession();
		logger.info("calling LVMessage.LVExit()");
		//MAURA 
		//LVMessage.LVExit(LVMessage.getLVSessionId());
		logger.info("done");
	}

	private Locale getDefaultLocale() {
		if (logger.isDebugEnabled())
			logger.debug("Get default locale");
		ResourceBundle aResourceBundle = ResourceBundle
				.getBundle("com/libricore/librisuite/controller/Controller");
		String defaultLocaleLanguage = aResourceBundle
				.getString("defaultLocaleLanguage");
		String defaultLocaleCountry = aResourceBundle
				.getString("defaultLocaleCountry");
		String defaultLocaleVariant = aResourceBundle
				.getString("defaultLocaleVariant");
		Locale defaultLocale = new Locale(defaultLocaleLanguage,
				defaultLocaleCountry, defaultLocaleVariant);
		return defaultLocale;
	}


}
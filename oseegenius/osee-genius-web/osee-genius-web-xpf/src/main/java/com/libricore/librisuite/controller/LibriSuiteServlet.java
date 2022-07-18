/**
 * 
 */
package com.libricore.librisuite.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.MissingResourceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import librisuite.business.common.Defaults;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionServlet;

/**
 * @author michelem
 *
 */
public class LibriSuiteServlet extends ActionServlet {
	private static final Log logger = LogFactory.getLog(LibriSuiteServlet.class);
	private static final String SEARCHER_URLS_PARAMETER="ir.urls";

	public void init() throws ServletException {
		super.init();
		// After Struts and Hibernate configuration...
		// initialize singletons:
		initObject("init.SolrManager");
		logger.info("Singletons succesfully initialized");
	}
	
	/**
	 * Init a manager calling the method public static void initInstance();
	 * @param defaultClassKey
	 */
	public void initObject(String defaultClassKey){
		try {
			Class clz = Defaults.getClazz(defaultClassKey);
			if(clz==null) return;
		    String url = getSearchersString();
		    try {
			clz.getDeclaredMethod("initInstance", String.class).invoke(null,url);
		    } catch (InvocationTargetException e) {

		        // Answer:
		        e.getCause().printStackTrace();
		    }
		} 
		catch(MissingResourceException e){
			logger.warn("Missing or unnecessary resource '" + defaultClassKey+"' in defaultValues.properties");
		}
		catch (Exception e) {
			logger.error("Configured '" + defaultClassKey+"' not instantiated: ",e);
		}
	}
	
	private String getSearchersString() {
		String[] urlsArray = new String[0];
		String urls = System.getProperty(SEARCHER_URLS_PARAMETER);
		if (urls == null) {
			urls = this.getServletContext()
					.getInitParameter(SEARCHER_URLS_PARAMETER);
		}
		if (urls != null)
			urlsArray = urls.split(",");
		urls = urlsArray[0];
		return urls;
	}

	
}

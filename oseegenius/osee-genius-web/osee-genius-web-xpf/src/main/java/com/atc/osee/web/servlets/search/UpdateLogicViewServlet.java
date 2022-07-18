/* 
 	Copyright (c) 2010 @Cult s.r.l. All rights reserved.
	
	@Cult s.r.l. makes no representations or warranties about the 
	suitability of the software, either express or implied, including
	but not limited to the implied warranties of merchantability, fitness
	for a particular purpose, or non-infringement. 
	
	@Cult s.r.l.not be liable for any damage suffered by 
	licensee as a result of using, modifying or distributing this software 
	or its derivates.
	
	This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.web.servlets.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.print.attribute.standard.PrinterLocation;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.velocity.tools.Toolbox;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.w3c.dom.Document;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.XsltUtility;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.servlets.RememberLastVisitedResourceServlet;
import com.atc.osee.web.servlets.writers.AbstractOutputWriter;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * OseeGenius -W- save logicView selected elsewhere (i.e. in wecat browsing)
 * 
 * @author Alice Guercio
 * @since 1.0
 */
public class UpdateLogicViewServlet extends RememberLastVisitedResourceServlet 
{
	private static final long serialVersionUID = -760895403089280L;
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{	
		final String defaultValue = "BOTH";
		final String escapedCommercialE = "%26"; 
		
		request.setCharacterEncoding("UTF-8");	
		 try {
			HttpSession session = request.getSession(true);
			if (session == null) {
				setSessionAttribute(request, HttpAttribute.OG_CONTEXT, null);		
			}		
			 
			String lv = request.getParameter("lv");
			String redirect = request.getParameter("to");
			if(defaultValue.equals(lv) || lv == null || "".equals(lv)){
				session.removeAttribute(HttpAttribute.LOGICAL_VIEW);
			}						
			else {
				session.setAttribute(HttpAttribute.LOGICAL_VIEW, lv);
			}
			if (redirect != null && !"".equals(redirect)) {
				response.sendRedirect(redirect.replaceAll(escapedCommercialE, "&"));
			}
			
			String language = request.getParameter(HttpParameter.LANGUAGE).replaceAll("[.:,;()><&|^#*/=~'\"-+]", "");
			if (language != null)
			{
				Visit visit = (Visit) request.getSession(true).getAttribute(HttpAttribute.VISIT);
				Locale locale = new Locale(language);
				visit.setPreferredLocale(locale);
			} 
			
			
		 }			 
		 
		 
		catch (Exception exception) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
		}		
		
		
		
	}
	
	
}
package com.atc.osee.web.servlet.authority;

import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.solr.common.SolrDocument;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.view.ServletUtils;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.servlets.DownloadIMSSServlet;
import com.atc.osee.web.servlets.DownloadServlet;
import com.atc.osee.web.tools.ResourceTool;
import com.atc.osee.web.tools.StringManipulationTool;

public class AuthorityDownloadIMSSServlet extends DownloadIMSSServlet {
	
	private static final long serialVersionUID = 2013305875339071431L;

	protected ISearchEngine getSearchEngine(final HttpServletRequest request)
	{
		return (ISearchEngine) getServletContext().getAttribute(IConstants.SEARCH_ENGINE_AUTH_ATTRIBUTE_NAME);
	}
	
	protected String htmlTransform(
			final HttpServletRequest request,
			Locale locale, 			
			SolrDocument document,
			String templateType) {		
	
		String templateName = "";
		if(IConstants.HTML_FULL.equals(templateType)) {
			if(locale.ITALIAN.equals(locale)) {
				templateName = "recordXslt_ita";
			}
			//needed because text class (resourceTool) passed to velocity context load italian text always
			else {
				templateName = "recordXslt_eng";
			}
		}		
		else { // IConstants.HTML_SHORT.equals(templateType)
			templateName = "/components/hits/auth_list_view_mode";
		}
		
		try {
			VelocityEngine ve = new VelocityEngine();			
			ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, getServletContext().getRealPath("/authority/"));				
			ve.init();
			Template t = ve.getTemplate(templateName + ".vm" );
			VelocityContext context = new VelocityContext();
			context.put("resource", document);
			context.put("document", document);
			//context.put("text", ServletUtils.findTool("text", request));
			context.put("text", ServletUtils.findTool("text", getServletContext()));
			context.put("string", new StringManipulationTool());	
			context.put("export", true);
			StringWriter writer = new StringWriter();
		    t.merge( context, writer );
		    return writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}

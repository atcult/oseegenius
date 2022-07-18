package com.atc.osee.web.servlets.writers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.solr.common.SolrDocument;
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.view.ServletUtils;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Supertype layer for all export manager that use Jasper Report.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class JasperReportOutputWriter extends AbstractOutputWriter 
{
	protected JasperReport report; 
	protected Map<String, Object> params = new HashMap<String, Object>();
	
	@Override
	public void write(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final List<SolrDocument> documents) throws IOException 
	{
		response.reset();
		response.setContentType(getContentType());
		response.setHeader("Content-disposition", 
				"attachment; filename=\"" 
				+ attachmentName(request) 
				+ "." 
				+ getFilenameSuffix() 
				+ "\"");

		HttpSession session = request.getSession();
		Visit visit = (Visit)request.getSession().getAttribute(HttpAttribute.VISIT);
		ConfigurationTool configuration = (ConfigurationTool) ServletUtils.findTool(
				IConstants.CONFIGURATION_KEY, session.getServletContext());	
		
		try 
		{
			if (report == null)
			{
				String relativePath = "/export/" +  getTemplateName() + ".jrxml";
				String stylesheet = request.getSession().getServletContext().getRealPath(relativePath);
				File file = new File(stylesheet);	
				JasperDesign design = JRXmlLoader.load(new FileInputStream(file));
				
				report = JasperCompileManager.compileReport(design);
			}
			
			params.put(JRParameter.REPORT_LOCALE,visit.getPreferredLocale());
			
			//bug 3476 - lavorazione campo collocazione per Trento
			Toolbox toolbox = (org.apache.velocity.tools.Toolbox) request.getSession().getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
			String prova = (String) toolbox.get("institution");
			if("Catalogo Bibliografico Trentino".equals(prova)){
				elaborateCollocation(documents,request);
			}
			
			
			params.put("ITEMS", new JRBeanCollectionDataSource(documents)); // per tamplate excel della gregoriana
			JasperPrint print = JasperFillManager.fillReport(report, params,new JRBeanCollectionDataSource(documents));
			doExport(print, response.getOutputStream());
		} catch (Exception exception) 
		{
exception.printStackTrace();			
			Log.error("Error while generating report.", exception);
		}		
	}
	
	private void elaborateCollocation(List<SolrDocument> documents, HttpServletRequest request){
		
		Map<String, String> allLibrariesBySymbol = (Map<String, String>) request.getSession().getServletContext().getAttribute(IConstants.ALL_LIBRARIES_BY_SIMBOL);
		if(allLibrariesBySymbol!=null && !allLibrariesBySymbol.isEmpty()){
			for(SolrDocument doc: documents){
				List<String> collocationList = (List<String>)doc.getFieldValue("collocation_for_report");
				List<String> collocationListNew = null;
				if(collocationList!=null && !collocationList.isEmpty()){
					collocationListNew = new ArrayList<String>();
					String libraryLabel = null;
					String collocationWithoutLibrary = null;
					String collocationNew = null;
					for(String collocation: collocationList){
						int ind = collocation.indexOf(" ");
						if(ind!=-1){
							libraryLabel = collocation.substring(0, ind);
							libraryLabel = libraryLabel.trim();
							collocationWithoutLibrary = collocation.substring(ind);
							collocationNew = allLibrariesBySymbol.get(libraryLabel).replaceAll("\\r", "").replaceAll("\\n", "") + collocationWithoutLibrary;
							collocationListNew.add(collocationNew);
						}	
					}
				}
				if(collocationListNew!=null && !collocationListNew.isEmpty()){
					doc.setField("collocation_for_report", collocationListNew);		
				}
			}
		}
	}
	
	protected String getTemplateName()
	{
		return "template";
	}
	
	/**
	 * Returns the content type managed by this output writer.
	 * 
	 * @return the content type managed by this output writer.
	 */
	protected abstract String getContentType();
	
	/**
	 * Returns the suffix of the file that will be downloaded.
	 * 
	 * @return the suffix of the file that will be downloaded.
	 */	
	protected abstract String getFilenameSuffix();
	
	/**
	 * Executes the export.
	 * 
	 * @param print the jasper print object.
	 * @param output the output stream.
	 * @throws IOException in case of I/O failure.
	 * @throws ServletException in case of I/O exception.
	 * @throws JRException in case of Jasper framework failure.
	 */
	protected abstract void doExport(final JasperPrint print, final OutputStream output) throws IOException, JRException ;
}
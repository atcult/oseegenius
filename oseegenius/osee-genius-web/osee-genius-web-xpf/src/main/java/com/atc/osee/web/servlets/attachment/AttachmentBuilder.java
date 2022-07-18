package com.atc.osee.web.servlets.attachment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.solr.common.SolrDocument;
import org.apache.velocity.tools.view.ServletUtils;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Supertype layer for all attachment builders.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class AttachmentBuilder
{
	protected static JasperReport REPORT; 
	protected Map<String, Object> params = new HashMap<String, Object>();
	
	/**
	 * Returns the byte array streams that constitutes the attachment.
	 * 
	 * @param request the HTTP request.
	 * @param response the SOLR response.
	 * @return the byte array streams that constitutes the attachment.
	 * @throws JRException in case Jasper Report framework fails during the report creation.
	 * @throws IOException in case of I/O failure.
	 */
	@SuppressWarnings("unchecked")
	public byte [] getOutputByteStream(final HttpServletRequest request, final List<SolrDocument> documents) throws JRException, IOException
	{
		HttpSession session = request.getSession();
		Visit visit = (Visit)request.getSession().getAttribute(HttpAttribute.VISIT);
		ConfigurationTool configuration = (ConfigurationTool) ServletUtils.findTool(
				IConstants.CONFIGURATION_KEY, session.getServletContext());	
		
		if (REPORT == null)
		{
			String relativePath = "/export/" +  getTemplateName() + ".jrxml";
			String stylesheet = request.getSession().getServletContext().getRealPath(relativePath);
			File file = new File(stylesheet);	
			JasperDesign design = JRXmlLoader.load(new FileInputStream(file));
			
			REPORT = JasperCompileManager.compileReport(design);
		}
		
		params.put(JRParameter.REPORT_LOCALE,visit.getPreferredLocale());
		
		for (SolrDocument document: documents)
		{
			
			List<String> otherAuthorPerson = (List<String>) document.get(ISolrConstants.OTHER_AUTHOR_PERSON_FIELD_NAME);
			List<String> otherAuthorCorporate = (List<String>) document.get(ISolrConstants.OTHER_AUTHOR_CORPORATION_FIELD_NAME);
			List<String> otherAuthorConference = (List<String>) document.get(ISolrConstants.OTHER_AUTHOR_MEETING_FIELD_NAME);
			
			List<String> otherAuthor = new ArrayList<String>();
			if (otherAuthorPerson != null)
			{
				otherAuthor.addAll(otherAuthorPerson);
			}

			if (otherAuthorCorporate!= null)
			{
				otherAuthor.addAll(otherAuthorCorporate);
			}

			if (otherAuthorConference!= null)
			{
				otherAuthor.addAll(otherAuthorConference);
			}
			
			if (!otherAuthor.isEmpty())
			{
				document.addField("other_author", otherAuthor);
			}
		}
		
		params.put("ITEMS", new JRBeanCollectionDataSource(documents)); // per tamplate excel della gregoriana
		
		JasperPrint print = JasperFillManager.fillReport(REPORT, params,new JRBeanCollectionDataSource(documents));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		doExport(print, output, documents);
		return output.toByteArray();
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
	public abstract String getContentType();
	
	/**
	 * Returns the suffix of the file that will be downloaded.
	 * 
	 * @return the suffix of the file that will be downloaded.
	 */	
	public abstract String getFilenameSuffix();
	
	
	
	/**
	 * Executes the export.
	 * 
	 * @param print the jasper print object.
	 * @param output the output stream.
	 * @param response the SOLR response.
	 * @throws IOException in case of I/O failure.
	 * @throws JRException in case of Jasper framework failure.
	 */
	protected abstract void doExport(final JasperPrint print, final OutputStream output, final List<SolrDocument> documents) throws IOException, JRException ;	
}
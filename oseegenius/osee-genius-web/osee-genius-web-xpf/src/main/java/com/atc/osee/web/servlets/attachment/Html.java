package com.atc.osee.web.servlets.attachment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;

import org.apache.solr.common.SolrDocument;

import com.atc.osee.web.IConstants;

/**
 * HTML attachment builder.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Html extends AttachmentBuilder 
{
	@Override
	public String getContentType() 
	{
		return "text/html; charset=" + IConstants.UTF_8;
	}

	@Override
	public String getFilenameSuffix() 
	{
		return "html";
	}

	@Override
	protected void doExport(
			final JasperPrint print, 
			final OutputStream output, 
			final List<SolrDocument> documents) throws IOException, JRException
	{
		JRHtmlExporter exporter = new JRHtmlExporter();
		
		exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output);
		exporter.setParameter(JRHtmlExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
		exporter.setParameter(JRHtmlExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.TRUE);
		exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.FALSE);
		exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "image?image=");
		
		exporter.exportReport();
	}
}
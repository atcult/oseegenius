package com.atc.osee.web.servlets.writers;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRRtfExporter;

/**
 * HTML output writer.
 * Allows download of a set of selected records in HTML format.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class RtfOutputWriter extends JasperReportOutputWriter
{

	@Override
	protected String getContentType() 
	{
		return "application/rtf";
	}

	@Override
	protected String getFilenameSuffix() 
	{
		return "doc";
	}

	@Override
	protected void doExport(
			final JasperPrint print, 
			final OutputStream output) throws IOException, JRException 
	{
		JRRtfExporter exporter = new JRRtfExporter ();
//		exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
//		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
//		exporter.setParameter(JRHtmlExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
//		exporter.setParameter(JRHtmlExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.TRUE);
//		exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.FALSE);
//		exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "image?image=");
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output);
		
		exporter.exportReport();
	}
}
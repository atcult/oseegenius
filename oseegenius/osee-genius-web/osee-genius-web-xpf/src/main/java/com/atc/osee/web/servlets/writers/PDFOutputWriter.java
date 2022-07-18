package com.atc.osee.web.servlets.writers;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * PDF output writer.
 * Allows download of a set of selected records in PDF format.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class PDFOutputWriter extends JasperReportOutputWriter 
{		
	@Override
	protected String getContentType() 
	{
		return "application/pdf";
	}

	@Override
	protected String getFilenameSuffix() 
	{
		return "pdf";
	}

	@Override
	public void doExport(final JasperPrint print, final OutputStream output) throws IOException, JRException 
	{
		JasperExportManager.exportReportToPdfStream(print, output);
	}
}
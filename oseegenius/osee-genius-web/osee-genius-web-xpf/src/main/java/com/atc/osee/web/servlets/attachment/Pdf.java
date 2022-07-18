package com.atc.osee.web.servlets.attachment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.solr.common.SolrDocument;

/**
 * PDF attachment builder.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Pdf extends AttachmentBuilder 
{
	@Override
	public String getContentType() 
	{
		return "application/pdf";
	}

	@Override
	public String getFilenameSuffix() 
	{
		return "pdf";
	}

	@Override
	protected void doExport(
			final JasperPrint print, 
			final OutputStream output, 
			final List<SolrDocument> documents) throws IOException, JRException
	{
		JasperExportManager.exportReportToPdfStream(print, output);
	}
}
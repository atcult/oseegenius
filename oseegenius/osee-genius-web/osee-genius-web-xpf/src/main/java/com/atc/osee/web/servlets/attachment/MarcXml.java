package com.atc.osee.web.servlets.attachment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.solr.common.SolrDocument;
import org.marc4j.MarcReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;

/**
 * MARC XML attachment builder.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class MarcXml extends AttachmentBuilder 
{
	@Override
	public String getContentType() 
	{
		return "text/xml; charset=" + IConstants.UTF_8;
	}

	@Override
	public String getFilenameSuffix() 
	{
		return "xml";
	}

	@Override
	protected void doExport(
			final JasperPrint print, 
			final OutputStream output, 
			final List<SolrDocument> documents) throws IOException, JRException
	{
		MarcWriter writer = null;
		MarcReader reader = null;
		
		 try 
		 {
			 writer = new MarcXmlWriter(output, "UTF-8");
			 for (SolrDocument document : documents)
			 {
				 reader = new MarcXmlReader(new ByteArrayInputStream( ((String)document.get(ISolrConstants.MARC_21_FIELD_NAME)).getBytes("UTF-8")));
				 if (reader.hasNext())
				 {
					Record record = reader.next();
					writer.write(record);
				}
			 }
		 } catch(UnsupportedEncodingException exception)
		 {
			 // TODO LOG
		 } finally 
		 {
			 writer.close();
		 }

	}
}

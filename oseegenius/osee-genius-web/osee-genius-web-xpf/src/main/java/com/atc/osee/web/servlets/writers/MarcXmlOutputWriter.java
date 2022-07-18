package com.atc.osee.web.servlets.writers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.common.SolrDocument;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

import com.atc.osee.web.ISolrConstants;

public class MarcXmlOutputWriter extends AbstractOutputWriter
{
	public void write(HttpServletRequest request, HttpServletResponse response, final List<SolrDocument> documents) throws IOException
	{
		 response.reset(); 
		 response.setContentType("text/xml"); 
		 response.setHeader("Content-disposition", "attachment; filename=\""+attachmentName(request)+".xml\""); 
		 
		 MarcWriter writer = null;
		 MarcReader reader = null;
		 
		 try 
		 {
			 writer = new MarcXmlWriter(response.getOutputStream());
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
package com.atc.osee.web.servlets.writers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.common.SolrDocument;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;

import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;

/**
 * Tagged MARC output writer.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class TaggedMarc21OutputWriter extends AbstractOutputWriter
{
	@Override
	public void write(final HttpServletRequest request, final HttpServletResponse response, final List<SolrDocument> documents) throws IOException
	{
		 response.reset(); 
		 response.setContentType("text/plain"); 
		 response.setHeader("Content-disposition", "attachment; filename=\"" + attachmentName(request) + ".txt\""); 
		 
		 PrintWriter writer = null;
		 MarcReader reader = null;
		 
		 try 
		 {
			 writer = response.getWriter();
			 for (SolrDocument document : documents)
			 {
				 reader = new MarcStreamReader(new ByteArrayInputStream(((String)document.get(ISolrConstants.MARC_21_FIELD_NAME)).getBytes(IConstants.UTF_8)));
				 if (reader.hasNext())
				 {
					Record record = reader.next();
					writer.println(record.toString());
				}
			 }
		 } catch (UnsupportedEncodingException exception)
		 {
			 // Nothing to be done here..
		 } finally 
		 {
			 writer.close();
		 }
	}
}
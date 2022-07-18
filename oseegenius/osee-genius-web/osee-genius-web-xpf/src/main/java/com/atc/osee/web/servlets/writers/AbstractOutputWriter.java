package com.atc.osee.web.servlets.writers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.common.SolrDocument;

import com.atc.osee.web.HttpParameter;

public abstract class AbstractOutputWriter
{
	public abstract void write(HttpServletRequest request, HttpServletResponse response, final List<SolrDocument> documents) throws IOException;
	
	protected String attachmentName(HttpServletRequest request)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy-HH:mm");
		String result = request.getParameter(HttpParameter.URI);
		if (result == null)
		{
			result = request.getParameter(HttpParameter.QUERY);
		}
		return (result != null ? result : "export_")+formatter.format(new Date());
	}	
}
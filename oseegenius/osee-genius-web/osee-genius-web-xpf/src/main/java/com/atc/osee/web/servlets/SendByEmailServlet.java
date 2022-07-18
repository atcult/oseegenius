/* 
 	Copyright (c) 2010 @Cult s.r.l. All rights reserved.
	
	@Cult s.r.l. makes no representations or warranties about the 
	suitability of the software, either express or implied, including
	but not limited to the implied warranties of merchantability, fitness
	for a particular purpose, or non-infringement. 
	
	@Cult s.r.l.not be liable for any damage suffered by 
	licensee as a result of using, modifying or distributing this software 
	or its derivates.
	
	This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.web.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.Layout;
import com.atc.osee.web.model.FederatedItemSelection;
import com.atc.osee.web.model.ItemSelection;
import com.atc.osee.web.model.SBNFederatedItemSelection;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.sbn.Document;
import com.atc.osee.web.servlets.attachment.AttachmentBuilder;
import com.atc.osee.web.servlets.attachment.Excel;
import com.atc.osee.web.servlets.attachment.Html;
import com.atc.osee.web.servlets.attachment.Marc21;
import com.atc.osee.web.servlets.attachment.MarcXml;
import com.atc.osee.web.servlets.attachment.Pdf;
import com.atc.osee.web.servlets.attachment.TaggedMarc;
import com.atc.osee.web.util.Utils;

/**
 * OseeGenius -W- Web controller that manages email communications.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class SendByEmailServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 1L;

	private AttachmentBuilder defaultAttachmentBuilder = new Pdf();
	
	private Map<String, AttachmentBuilder> attachmentBuilders = new HashMap<String, AttachmentBuilder>();
	{
		attachmentBuilders.put("pdf", defaultAttachmentBuilder);
		attachmentBuilders.put("xls", new Excel());
		attachmentBuilders.put("html", new Html());
		attachmentBuilders.put("mxml", new MarcXml());
		attachmentBuilders.put("m21", new Marc21());
		attachmentBuilders.put("tmarc", new TaggedMarc());
	}
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String resourceId = request.getParameter(HttpParameter.RESOURCE_ID);
		if (resourceId != null && resourceId.trim().length() != 0)
		{
			request.setAttribute(HttpParameter.RESOURCE_ID, resourceId);
		}
		
		String context = request.getParameter(HttpParameter.SELECTION_CONTEXT);
		if (isNotNullOrEmptyString(context))
		{
			request.setAttribute(HttpParameter.SELECTION_CONTEXT, context);			
		}
		
		if (isNotNullOrEmptyString(request.getParameter("from")))
		{
			request.setAttribute("from", "sbn");		
		}
		
		forwardTo(request, response, "email.vm", Layout.DUMMY_LAYOUT);
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String resourceId = request.getParameter(HttpParameter.RESOURCE_ID);
		if (resourceId != null && resourceId.trim().length() != 0)
		{
			request.setAttribute(HttpParameter.RESOURCE_ID, resourceId);
		}
		
		ISearchEngine searchEngine = getSearchEngine(request);
		ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT))
				? getSearchExperience(request).getCurrentTab()
				: getWorkspaceSelection(request);		
				
		if (isNotNullOrEmptyString(request.getParameter(HttpParameter.FOLDER_SELECTION_CONTEXT)))
		{
			selection = getSearchExperience(request);
		}				
				
		String senderName = getConfiguration(request).getEmailSenderAddress();
		
		AttachmentBuilder attachmentBuilder = getAttachmentBuilder(request);
		
		String recipientsParameter = request.getParameter(HttpParameter.RECIPIENTS);
		String subject = request.getParameter(HttpParameter.SUBJECT);
		//String message = request.getParameter(HttpParameter.MESSAGE);
		String message = getMessage(request,HttpParameter.MESSAGE);
		
		try
		{
			List<SolrDocument> documents = null;
			
			if (selection instanceof SearchTab && ((SearchTab)(selection)).isExternalSearchTab())
			{
				if(((SearchTab)(selection)).isExternalSbnSearchTab()){
					SearchTab selectedTab = (SearchTab) selection;
					Map<String, SBNFederatedItemSelection> selectedSBNFederatedRecords = selectedTab.getSelectedSBNItemsForExportOrDownload();
					documents = new ArrayList<SolrDocument>(selectedSBNFederatedRecords.size());
					try 
					{
						Source xslt = new StreamSource(getClass().getResourceAsStream("/sbn/unimarc.xsl"));
						Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
						
						for (Entry<String, SBNFederatedItemSelection> entry : selectedSBNFederatedRecords.entrySet()){
							
								SBNFederatedItemSelection item = entry.getValue();
								StringWriter w = new StringWriter();
								engine.transform(new StreamSource(new ByteArrayInputStream(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"))), new StreamResult(w));
								Document sbnDoc = new Document(w.toString());
								String unimarcDoc = new String(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"), "UTF-8");
								Utils.convertSBNToSolrDocuments(documents, sbnDoc.getMetaData(),unimarcDoc, item.id);
							
							} 
						}catch (Exception exception)
						{
							// Nothing to be done here...
						}
					
				}else{
					SearchTab tab = (SearchTab) selection;
					Map<String, FederatedItemSelection> selectedFederatedRecords = tab.getSelectedFederatedItemsForExportOrDownload();
					documents  = new ArrayList<SolrDocument>(selectedFederatedRecords.size());
					for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet())
					{
						try 
						{
							FederatedItemSelection item = entry.getValue();
							Utils.convertToSolrDocuments(documents, tab.getPazpar2().record(item.recid, null), item.recid, item.id);
						} catch (Exception exception)
						{
							// Nothing to be done here...
						}
					}
				}	
			} else if (selection instanceof SearchExperience)
			{
				documents  = ((SearchExperience)selection).getSelectedDocuments();
			} else 
			{			
				QueryResponse queryResponse = (resourceId != null && resourceId.trim().length() != 0) 
					? searchEngine.findDocumentByURI(resourceId) 
					: searchEngine.documents(selection.getSelectedItemsForEmail());
					
					documents = queryResponse.getResults();
			} 
			
			setRequestAttribute(request, "selectedItems", documents);
			
			Message email = new MimeMessage(getMailSession());
			email.setFrom(new InternetAddress(senderName));
			
			String [] recipients = recipientsParameter.split(",");
			int addressNumber;
	        if (recipients.length > 10){
	        	
	        	addressNumber= 10;
	        }
	        else{
	        	
	        	addressNumber = recipients.length;
	        }
			InternetAddress [] to = new InternetAddress[addressNumber];
			for (int i = 0; i < addressNumber; i++)
			{
				to[i] = new InternetAddress(recipients[i]);
			}
			
			email.setRecipients(Message.RecipientType.TO, to);
			
			email.setSubject(subject);
			
			Multipart multipart = new MimeMultipart();
			
			MimeBodyPart attachment = new MimeBodyPart();
			DataSource datasource = new ByteArrayDataSource(
					attachmentBuilder.getOutputByteStream(request, documents), 
					attachmentBuilder.getContentType());
		    
			attachment.setDataHandler(new DataHandler(datasource));
			attachment.setFileName("attachment." + attachmentBuilder.getFilenameSuffix());
			
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent((createEmailText(request, message, documents)),  "text/html;charset=UTF-8");
			
			multipart.addBodyPart(attachment);
			multipart.addBodyPart(mimeBodyPart);
			email.setSentDate(new Date());
			
			email.setContent(multipart);
			Transport.send(email);			
			
			
			if (isNotNullOrEmptyString(request.getParameter(HttpParameter.PREVIOUS_QUERY_DONE)))
			{
				response.sendRedirect(request.getParameter(HttpParameter.PREVIOUS_QUERY_DONE));
			} else {
				forwardTo(request, response, "/email_ok.vm", Layout.DUMMY_LAYOUT);
			}
			
			
			
		} catch (Exception exception)
		{			
			exception.printStackTrace();
			
			request.setAttribute("inError", true);
			request.setAttribute(HttpParameter.RECIPIENTS, recipientsParameter);
			request.setAttribute(HttpParameter.SUBJECT, subject);
			request.setAttribute(HttpParameter.MESSAGE, message);
			
			if (isNotNullOrEmptyString(request.getParameter(HttpParameter.PREVIOUS_QUERY_DONE)))
			{
				forwardTo(request, response, "/email_notok.vm", "dummy_layout.vm");
			} else {
				forwardTo(request, response, "/email.vm", "dummy_layout.vm");
			}
						
		}		
	}
	
	/**
	 * Creates the text of the outgoing email.
	 * 
	 * @param request the HTTP request.
	 * @param message the message body.
	 * @param queryResponse the SOLR query response.
	 * @return the text that will be inserted as email body.
	 */
	protected String createEmailText(final HttpServletRequest request, final String message, final List<SolrDocument> documents)
	{
		Locale locale = getVisit(request).getPreferredLocale();
		ResourceBundle messages = ResourceBundle.getBundle("resources", locale);
		
		String permalinkPrefix = 
			request.getScheme()
			+ "://"
			+ request.getServerName()
			+ ":"
			+ request.getServerPort()
			+ request.getContextPath()
			+ "/resource?uri=";
		
		StringBuilder builder = new StringBuilder("<p>").append(message.replace("\n", "<br/>")).append("</p>");
		
		builder.append("<table>");
		for (SolrDocument document : documents)
		{
			builder.append("<tr><td>");
			builder
				.append("<a href='")
				.append(permalinkPrefix)
				.append(document.get(ISolrConstants.ID_FIELD_NAME))
				.append("'><b>");

			if (document.getFieldValue(ISolrConstants.TITLE_FIELD_NAME) != null)
			{
				builder.append(document.getFieldValue("title"));
			} else 
			{
				builder
					.append(IConstants.OPEN_PARENTHESIS)
					.append(messages.getString("title_na"))
					.append(IConstants.CLOSE_PARENTHESIS);		
			}
				
			builder.append("</b></a><br/>");				
			builder.append("</td></tr><tr><td style='font-size:0.8em;'><i>");
			if (document.get(ISolrConstants.AUTHOR_PERSON_FIELD_NAME) != null)
			{
				builder.append(document.get(ISolrConstants.AUTHOR_PERSON_FIELD_NAME));
			} else if (document.get(ISolrConstants.AUTHOR_CORPORATION_FIELD_NAME) != null)
			{
				builder.append(document.get(ISolrConstants.AUTHOR_CORPORATION_FIELD_NAME));
			} else if (document.get(ISolrConstants.AUTHOR_MEETING_FIELD_NAME) != null)
			{
				builder.append(document.get(ISolrConstants.AUTHOR_MEETING_FIELD_NAME));
			} else 
			{
				builder
				.append(IConstants.OPEN_PARENTHESIS)
				.append(messages.getString("author_na"))
				.append(IConstants.CLOSE_PARENTHESIS);			
			}
			builder.append("</i>");
			builder.append("</td></tr>");			
		}
		builder.append("</table>");
		return builder.toString();
	}
	
	/**
	 * Returns the attachment builder associated with the current request.
	 * 
	 * @param request the HTTP request.
	 * @return the attachment builder associated with the current request.
	 */
	private AttachmentBuilder getAttachmentBuilder(final HttpServletRequest request)
	{
		String attachmentType = request.getParameter(HttpParameter.ATTACHMENT_TYPE);
		AttachmentBuilder attachmentBuilder = attachmentBuilders.get(attachmentType);
		return attachmentBuilder != null ? attachmentBuilder : defaultAttachmentBuilder;
	}
	
	/**
	 * Returns the OseeGenius -W- Mail Session.
	 * 
	 * @return the OseeGenius -W- Mail Session.
	 * @throws NamingException in case of naming service failure.
	 */
	private Session getMailSession() throws NamingException
	{
		Context namingContext = new InitialContext();
		return (Session) namingContext.lookup("java:comp/env/mail/oseegenius");
	}
	
	protected String getMessage(final HttpServletRequest request, final String input)
	{
		return request.getParameter(input);
	}
}
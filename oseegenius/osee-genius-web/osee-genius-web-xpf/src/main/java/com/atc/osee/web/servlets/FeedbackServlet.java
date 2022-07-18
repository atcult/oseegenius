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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.velocity.tools.view.ServletUtils;
import org.jfree.chart.title.Title;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.Layout;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.servlets.attachment.AttachmentBuilder;
import com.atc.osee.web.servlets.attachment.Pdf;
import com.atc.osee.web.servlets.writers.PDFOutputWriter;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Feedback sender servlet.
 * 
 * @author aguercio
 * @since 1.2
 */
public class FeedbackServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 13463346457L;
	
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		Account account = getVisit(request).getAccount();
		request.setAttribute("account", account);
		
		String resourceId = request.getParameter(HttpParameter.RESOURCE_ID);
		if (isNotNullOrEmptyString(resourceId)) {
			resourceId=resourceId.replaceAll("[.:,;()><&|^#*/=~'\"-+]", "");
			request.setAttribute(HttpParameter.RESOURCE_ID, resourceId);		
		}
		String resourceTitle = request.getParameter(HttpParameter.RESOURCE_TITLE);
		if (isNotNullOrEmptyString(resourceTitle)) {
			request.setAttribute(HttpParameter.RESOURCE_TITLE, resourceTitle);		
		}
		String recipientParamenter = request.getParameter(HttpParameter.USER_EMAIL);
		if (isNotNullOrEmptyString(recipientParamenter)) {
			request.setAttribute(HttpParameter.USER_EMAIL, recipientParamenter);		
		}
		String serviceType = request.getParameter(HttpParameter.SERVICE_TYPE);
		if (isNotNullOrEmptyString(serviceType)) {
			request.setAttribute(HttpParameter.SERVICE_TYPE, serviceType);		
		}
		String feedback = request.getParameter(HttpParameter.MESSAGE);
		if (isNotNullOrEmptyString(feedback)) {
			request.setAttribute(HttpParameter.MESSAGE, feedback);		
		}
		String user_name = request.getParameter(HttpParameter.NAME);		
		if (isNotNullOrEmptyString(user_name)) {
			request.setAttribute(HttpParameter.NAME, user_name);		
		}
		String name = request.getParameter(HttpParameter.NAME);		
		if (isNotNullOrEmptyString(user_name)) {
			request.setAttribute(HttpParameter.NAME, name);		
		}
		String surname = request.getParameter("surname");		
		if (isNotNullOrEmptyString(surname)) {
			request.setAttribute("surname", surname);		
		}
		String user_phone = request.getParameter(HttpParameter.PHONE);
		if (isNotNullOrEmptyString(user_phone)) {
			request.setAttribute(HttpParameter.PHONE, user_phone);		
		}
		String user_address = request.getParameter(HttpParameter.ADDRESS);
		if (isNotNullOrEmptyString(user_address)) {
			request.setAttribute(HttpParameter.ADDRESS, user_address);		
		}	
		String card_number = request.getParameter(HttpParameter.CARD_NUMBER);
		if (isNotNullOrEmptyString(card_number)) {
			request.setAttribute(HttpParameter.CARD_NUMBER, card_number);		
		}	
		String publisher = request.getParameter(HttpParameter.PUBLISHER);
		if (isNotNullOrEmptyString(publisher)) {
			request.setAttribute(HttpParameter.PUBLISHER, publisher);		
		}
		String year = request.getParameter(HttpParameter.YEAR);
		if (isNotNullOrEmptyString(year)) {
			request.setAttribute(HttpParameter.YEAR, year);		
		}
		String author = request.getParameter(HttpParameter.AUTHOR);
		if (isNotNullOrEmptyString(author)) {
			request.setAttribute(HttpParameter.AUTHOR, author);		
		}
		String collocation = request.getParameter(HttpParameter.COLLOCATION);
		if (isNotNullOrEmptyString(collocation)) {
			request.setAttribute(HttpParameter.COLLOCATION, collocation);
		}
		String bibliographicLevel = request.getParameter(HttpParameter.BIBLIOGRAPHIC_LEVEL);
		if (isNotNullOrEmptyString(bibliographicLevel)) {
			request.setAttribute(HttpParameter.BIBLIOGRAPHIC_LEVEL, bibliographicLevel);
		}
		String recordBarcode = request.getParameter("recordBarcode");
		if (isNotNullOrEmptyString(recordBarcode)) {
			request.setAttribute("recordBarcode", recordBarcode);
		}
		
		request.setAttribute("currentDate", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		
		String forwardToPage = chooseForward(serviceType);				
		forwardTo(request, response, forwardToPage, Layout.DUMMY_LAYOUT);
	}
	
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException{
		//this settings is very important to preserve diacritics in email, variable under post ect.
		request.setCharacterEncoding("UTF-8");
		
		String resourceId = request.getParameter(HttpParameter.RESOURCE_ID);
		if (resourceId != null && resourceId.trim().length() != 0)	{
			resourceId=resourceId.replaceAll("[.:,;()><&|^#*/=~'\"-+]", "");

			request.setAttribute(HttpParameter.RESOURCE_ID, resourceId);
		}		
		String senderName = getConfiguration(request).getEmailSenderAddress();
		String userEmailParameter = request.getParameter(HttpParameter.USER_EMAIL);
		String serviceType = request.getParameter(HttpParameter.SERVICE_TYPE);
		String feedback = request.getParameter(HttpParameter.MESSAGE);
		String user_name = request.getParameter(HttpParameter.NAME);		
		String surname = request.getParameter("surname");		
		String name = request.getParameter(HttpParameter.NAME);
		String user_phone = request.getParameter(HttpParameter.PHONE);
		String user_address = request.getParameter(HttpParameter.ADDRESS);
		String publisher = request.getParameter(HttpParameter.PUBLISHER);
		String resourceTitle = request.getParameter(HttpParameter.RESOURCE_TITLE);
		String year = request.getParameter(HttpParameter.YEAR);
		String author = request.getParameter(HttpParameter.AUTHOR);
		String date = request.getParameter(HttpParameter.DATE);
		String purpose = request.getParameter(HttpParameter.PURPOSE);
		String cardNumber = request.getParameter(HttpParameter.CARD_NUMBER);
		String volume = request.getParameter(HttpParameter.VOLUME);
		String fascicolo = request.getParameter(HttpParameter.FASCICOLO);
		String pages = request.getParameter(HttpParameter.PAGES);
		String collocation = request.getParameter(HttpParameter.COLLOCATION);
		String bibliographicLevel = request.getParameter(HttpParameter.BIBLIOGRAPHIC_LEVEL);
		String subject = createEmailSubject(request, serviceType);
		String recordBarcode = request.getParameter("recordBarcode");
		
		//required field check
		boolean nameMailEmpty = isNullOrEmptyString(user_name) || isNullOrEmptyString(userEmailParameter);
		boolean othersEmpty = isNullOrEmptyString(user_phone) || isNullOrEmptyString(resourceTitle) || isNullOrEmptyString(date);
		
		
		String message = createEmailMessage(request, serviceType, user_name, user_phone, feedback, resourceId, 
				userEmailParameter, user_address, publisher, resourceTitle, year, author, date, purpose, cardNumber);
					
		try {
			
			Message email = new MimeMessage(getMailSession());
			email.setFrom(new InternetAddress(senderName));
			//In questo caso il sistema deve mandare una mail alla biblioteca stessa, per informarlo della notifica
			//per cui sender e reciever sono la stessa persona
			String recipientEmail = getConfiguration(request).getEmailSenderAddress();
			String [] recipients = recipientEmail.split(",");
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
			//email.setText(message);
			email.setSubject(subject);		
			email.setSentDate(new Date());				
			email.setContent(message, "text/html;charset=UTF-8");
			
			
			
			// bug  teologica 7870
			Account account = getVisit(request).getAccount();
			if (account != null) {
				surname = surname.toUpperCase();
				name = name.toUpperCase();
			}
			else {
				if (surname!=null)
					surname = surname.toLowerCase();
				name = name.toLowerCase();
			}
			
			
			
			
			// for facolta Teologica
			if(IConstants.PRINT_BOOKING.equals(serviceType)) {
				printBooking(
						request, 
						resourceId, 
						surname,
						name,
						userEmailParameter,
						user_phone,
						user_address,
						publisher,
						resourceTitle,
						year,
						author,
						date,
						purpose,
						cardNumber,
						volume,
						fascicolo,
						pages,
						collocation,
						bibliographicLevel, 
						email,
						recordBarcode);				

			}
			
			Transport.send(email);	
			
			String forwardToPage = chooseForward(serviceType);	
			forwardTo(request, response, "email_ok.vm", "dummy_layout.vm");		
			
		}catch (Exception exception) {			
			exception.printStackTrace();			
			request.setAttribute("inError", true);
			request.setAttribute(HttpParameter.USER_EMAIL, userEmailParameter);
			request.setAttribute(HttpParameter.SUBJECT, subject);
			request.setAttribute(HttpParameter.MESSAGE, message);			
			request.setAttribute(HttpParameter.SERVICE_TYPE, serviceType);
			request.setAttribute(HttpParameter.NAME, user_name);
			request.setAttribute(HttpParameter.PHONE, user_phone);
			request.setAttribute(HttpParameter.ADDRESS, user_address);
			request.setAttribute(HttpParameter.PUBLISHER, publisher);
			request.setAttribute(HttpParameter.RESOURCE_TITLE, resourceTitle);
			request.setAttribute(HttpParameter.YEAR, year);
			request.setAttribute(HttpParameter.AUTHOR, author);
			request.setAttribute(HttpParameter.DATE, date);
			request.setAttribute(HttpParameter.PURPOSE, purpose);
			request.setAttribute(HttpParameter.CARD_NUMBER, cardNumber);
			request.setAttribute(HttpParameter.VOLUME, volume);
			request.setAttribute(HttpParameter.FASCICOLO, fascicolo);
			request.setAttribute(HttpParameter.PAGES, pages);
			request.setAttribute("recordBarcode", recordBarcode);
			
			String forwardToPage = chooseForward(serviceType);
			forwardTo(request, response, forwardToPage, "dummy_layout.vm");			
		}		
		
	}

	/**
	 * Create a PDF attachment for email with data
	 * @param request
	 * @param resourceId
	 * @param bibliographicLevel
	 * @param email
	 * @throws MalformedURLException
	 * @throws JRException
	 * @throws IOException
	 * @throws MessagingException
	 */
	private void printBooking(
			final HttpServletRequest request,
			final String resourceId, 
			final String surname,
			final String name,
			final String userEmailParameter,
			final String user_phone,
			final String user_address,
			final String publisher,
			final String resourceTitle,
			final String year,
			final String author,
			final String date,
			final String purpose,
			final String cardNumber,
			final String volume,
			final String fascicolo,
			final String pages,
			final String collocation,
			final String bibliographicLevel, 
			final Message email,
			final String recordBarcode)
			throws MalformedURLException, JRException, IOException,
			MessagingException {
		
		Multipart multipart = new MimeMultipart();
		MimeBodyPart attachment = new MimeBodyPart();
		AttachmentBuilder attachmentBuilder = new Pdf();
		
		ConfigurationTool configuration = (ConfigurationTool) ServletUtils.findTool(
				IConstants.CONFIGURATION_KEY, request.getSession().getServletContext());	
//		URL templateURL = new URL(
//				"http://"+ configuration.getInVmHost() + ":" + (configuration.getInVmPort() != null ? configuration.getInVmPort() : request.getServerPort()) + request.getContextPath() + "/export/" +  getPdfModuleName(bibliographicLevel) + ".jrxml");
//		
//		JasperDesign design = JRXmlLoader.load(templateURL.openStream());		
		String relativePath = "/export/" +  getPdfModuleName(bibliographicLevel) + ".jrxml";
		//System.out.println(relativePath);
		String stylesheet = request.getSession().getServletContext().getRealPath(relativePath);
		File file = new File(stylesheet);
		JasperDesign design = JRXmlLoader.load(new FileInputStream(file));		
		
		JasperReport REPORT = JasperCompileManager.compileReport(design);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("resourceId", resourceId);
		params.put("surname", surname);
		params.put("name", name);
		params.put("userEmailParameter", userEmailParameter);
		params.put("user_phone", user_phone);
		params.put("user_address", user_address);
		params.put("publisher", publisher);
		params.put("resourceTitle", resourceTitle);
		params.put("year", year);
		params.put("author", author);
		params.put("date", date);
		params.put("purpose", purpose);
		params.put("cardNumber", cardNumber);
		params.put("volume", volume);
		params.put("fascicolo", fascicolo);
		params.put("pages", pages);
		params.put("collocation", collocation);
		params.put("recordBarcode", recordBarcode);
		params.put("date", new SimpleDateFormat("dd/MM/yyy").format(new Date()));
		params.put("hour", new SimpleDateFormat("HH:mm:ss a").format(new Date()));
		JasperPrint print = JasperFillManager.fillReport(REPORT, params, new JREmptyDataSource());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		JasperExportManager.exportReportToPdfStream(print, output);
		DataSource datasource = new ByteArrayDataSource(output.toByteArray(), 
				attachmentBuilder.getContentType());
		attachment.setDataHandler(new DataHandler(datasource));
		attachment.setFileName("attachment.pdf");
		
		multipart.addBodyPart(attachment);
		email.setContent(multipart);
	}
		
	protected String createEmailMessage (final HttpServletRequest request, String serviceType, 
			String name, String phone, String feedback, String resourceId, String email, String address, 
			String publisher, String resourceTitle, String year, String author, String date, String purpose, String cardNumber){
		if (IConstants.PRINT_BOOKING.equals(serviceType)) {
			return "Nuova prenotazione";
		}
		StringBuilder message = new StringBuilder("<p>");
		
		String permalinkPrefix = 
				request.getScheme()
				+ "://"
				+ request.getServerName()
				+ ":"
				+ request.getServerPort()
				+ request.getContextPath();
				
				if (IConstants.AUTH_FEEDBACK.equals(serviceType)) {
					permalinkPrefix = permalinkPrefix + "/authResource";
				}
				else {
					permalinkPrefix = permalinkPrefix + "/resource";
				}
					
				permalinkPrefix = permalinkPrefix 
				+"?uri="
				+ resourceId;			
				
		message.append("L'utente ").append(name).append("(" + email);
				if(phone != null){
					message.append(" "  + phone);
				}
				if(address != null){
					message.append(" " + address);
				}
				if(isNotNullOrEmptyString(cardNumber)){
					message.append(" Tessera n. " + cardNumber);
				}
				message.append(") ");
				
		StringBuilder bibliographic_data = new StringBuilder();
		bibliographic_data.append("  <a href='" + permalinkPrefix + "'>" + resourceTitle + "</a> ");
		bibliographic_data.append(" (Record nr." + resourceId + ")");
		if(isNotNullOrEmptyString(author)){
			bibliographic_data.append(" di " + author);				
		}
		if (publisher != null){
			bibliographic_data.append(", " + publisher);				
		}
		if(year != null){
			bibliographic_data.append(" del " + year);
		}
		
		if(IConstants.FEEDBACK.equals(serviceType) || IConstants.AUTH_FEEDBACK.equals(serviceType)){			
			message.append("ha espresso un commento sulla risorsa ");			
			message.append(bibliographic_data);
			message.append("</br>");
			message.append("\"");
			message.append(feedback);	
			message.append("\"");
		}
		if(IConstants.INFO.equals(serviceType)){
			message.append("ha richiesto informazioni sulla risorsa ");			
			message.append(bibliographic_data);
			message.append("</br>");
			message.append("\"");
			message.append(feedback);	
			message.append("\"");
		}
		if(IConstants.BOOKING.equals(serviceType)){
			message.append("ha richiesto la prenotazione della risorsa ");
			message.append(bibliographic_data);
			message.append(" per la data " + date);
			message.append(" con motivazione: " + purpose);
		}
		message.append("</p>");
		return message.toString();
	}
	
	protected String createEmailSubject(final HttpServletRequest request, String serviceType){
		String object = "";
		if(IConstants.FEEDBACK.equals(serviceType) || IConstants.AUTH_FEEDBACK.equals(serviceType) ){
			return "Nuovo feedback utente"; 
		}
		if(IConstants.INFO.equals(serviceType)){
			return "Nuova richiesta di informazioni";
		}
		if(IConstants.BOOKING.equals(serviceType)){
			return "Nuova prenotazione";
		}
		if(IConstants.PRINT_BOOKING.equals(serviceType)){
			return "Nuova prenotazione";
		}
		return object;				
	}
	
	
	protected String createEmailText(final HttpServletRequest request, final String message, final String email) {
							
			StringBuilder builder = new StringBuilder("<p>").
					append("Nuovo messaggio da ").
					append(email.replace("\n", "<br/>")).
					append("</p>");
			
			builder.append("<p>").append(message.replace("\n", "<br/>")).append("</p>");				
						
			return builder.toString();
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
	
	/**
	 * calculate the correct name of page redirect to
	 * @param serviceType
	 * @return
	 */

	protected String chooseForward(String serviceType) {
		String result = "/leave_feedback.vm";
		if(IConstants.FEEDBACK.equals(serviceType)){
			return "/leave_feedback.vm";
		}
		if(IConstants.INFO.equals(serviceType)){
			return "/ask_info.vm";
		}
		if(IConstants.BOOKING.equals(serviceType) || IConstants.PRINT_BOOKING.equals(serviceType)){
			return "/booking.vm";
		}
		if(IConstants.AUTH_FEEDBACK.equals(serviceType)){
			return "/auth_leave_feedback.vm";
		}
		return result;
	}
	
	/**
	 * for Teologica that send via email different pdf according to biblographicLevel
	 * @param bibliographicLevel
	 * @return
	 */
	protected String getPdfModuleName(String bibliographicLevel) {
		if ("m".equals(bibliographicLevel)) {
			return "monograph";
		}
		else {
			return "serial";
		}
	}

}
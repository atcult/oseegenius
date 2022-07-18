package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 * loan in FOLIO
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioLoanServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 12467696843L;	
	
	protected Map<String, Map<String, String>> materialTypeLimits;
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null || !isNotNullOrEmptyString(getVisit(request).getFolioAccount().getId())) {
			response.sendRedirect("signIn?redirectId=" + request.getParameter("idResource"));
		}
		else {	
			final String barcode = request.getParameter("barcode").replaceAll("__", " ");
			final String idResource =  request.getParameter("idResource");		
			final String servicePoint = request.getParameter("servicePoint");
			final String collocation = request.getParameter("collocation");
			final String title = request.getParameter("title");
			final String author = request.getParameter("author");
			final String publisher = request.getParameter("publisher");	
			final String note = request.getParameter("note");
			final String edition = request.getParameter("edition");
			final String holding = request.getParameter("holding");
			//test 
			try {
				String test = servicePoint.split("___")[0];
			} catch (Exception e) {
				Log.error("Errore nella richiesta--- barcode:  " + barcode + " --- servicePoint: " + servicePoint + " --- title: " + title + " --- user: " + loggedUser.getBarcode());
			}
			
			
			final String servicePointId = servicePoint.split("___")[0];
			final String servicePointLabel = servicePoint.split("___")[1];		
								
			try {							
				final String accessToken = folioAPI.loginFolioAdministrator();					
				
				FolioItemModel item = folioAPI.getCopyBybarcode(barcode, accessToken);
				if (item == null) {
					request.setAttribute("inError", true);		
					request.setAttribute("errorMessage", "not_in_folio");
					forwardTo(request, response, "/components/userPanel/request_result.vm", "workspace_layout.vm");	
					return;
				}
				FolioRecord record = new FolioRecord();
				record.setCollocation(collocation);
				record.setTitle(title);
				record.setAuthorName(author);
				record.setPublisher(publisher);
				record.setNote(note);
				record.setEdition(edition);
				record.setHolding(holding);
				makeRequestOrLoan(request, 
								response, 
								loggedUser, 
								record,
								servicePointId, 
								servicePointLabel, 
								accessToken, 
								item,
								null);
				forwardTo(request, response, "/components/userPanel/request_result.vm", "workspace_layout.vm");
				return;
				
			}
			catch (FolioException e) { 			
				request.setAttribute("inError", true);		
				request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()).replaceAll(" ", "_").toLowerCase());
				forwardTo(request, response, "/components/userPanel/request_result.vm", "workspace_layout.vm");	
				return;
			}			
		}				
	}

	protected boolean tooManyRequestOrLoan(FolioUserModel loggedUser,
											final String accessToken,
											final String servicePointId,
											final FolioItemModel item,
											final HttpServletRequest request) throws FolioException {
		if (loggedUser.isStaff()) {
			return false;
		}
		else {
			if (isLoanRequest(servicePointId)) {
				return tooManyLoan(accessToken, loggedUser, servicePointId, request);			
			}
			else {  //it's a request not a loan
				return tooManyRequest(accessToken, item, loggedUser, servicePointId);
			}			
		}				
	}
	
	protected boolean tooManyLoan(final String accessToken, FolioUserModel loggedUser, final String servicePointId, final HttpServletRequest request) throws FolioException {
		ConfigurationTool configuration = getConfiguration(request);
		final int maxLoan = configuration.getLoanLimit();
		boolean countLoanLoanToo = configuration.isLoanCountEnabled();
		
		int openLoanRequests = getLoanRequestCount(accessToken, loggedUser, servicePointId);
		int openLoanLoans = getLoanLoanCount(accessToken, loggedUser);
		
		if (countLoanLoanToo) {
			return openLoanRequests + openLoanLoans >= maxLoan;
		}
		else {
			return openLoanRequests >= maxLoan;
		}		
	}
	
	protected boolean tooManyRequest (final String accessToken, FolioItemModel item , FolioUserModel loggedUser, final String servicePointId) throws FolioException {
		String materialTypeCode = item.getMaterialType();	
		if (materialTypeCode == null) {
			Log.error("ERRORE in materialTypeCode per l'item: " + item.getBarcode());
		}		
		int requestCount = getRequestNotLoanCount(accessToken, loggedUser, materialTypeCode);
		int loanConsultCount = getLoanConsultCount(accessToken, loggedUser, materialTypeCode);
		int howManyEqualMaterialType = requestCount + loanConsultCount;
		return doesUserExceed(loggedUser, materialTypeCode, howManyEqualMaterialType, materialTypeLimits);
	}
	
	protected int getRequestNotLoanCount(final String accessToken, FolioUserModel loggedUser, String materialTypeCode) throws FolioException {
		int resultCount = 0;
		List<FolioLoan> requests = folioAPI.getUserRequestOpen(accessToken, loggedUser.getId());
		//check how many request open are not loan request and of the same material type
		for(FolioLoan currentRequest : requests) {
			if (!isLoanRequest(currentRequest.getServicePointId())) {
				FolioItemModel currentItem = folioAPI.getCopyById(currentRequest.getItemId(), accessToken);
				String currentMaterialType = currentItem.getMaterialType();
				if(materialTypeCode.equals(currentMaterialType) 
						|| getOtherMaterialType(loggedUser, materialTypeCode, materialTypeLimits).contains(currentMaterialType)) {
					resultCount ++;
				}
			}
		}
		return resultCount;
	}

	protected void makeRequestOrLoan(final HttpServletRequest request, 
									final HttpServletResponse response,
									final FolioUserModel loggedUser, 
									final FolioRecord record, 
									final String servicePointId, 
									final String servicePointLabel,
									final String accessToken, 
									final FolioItemModel item,
									final String printingPoint) throws FolioException, ServletException, IOException, UnknownHostException {
							
		if (!tooManyRequestOrLoan(loggedUser, accessToken, servicePointId, item, request)) {
			
			final String barcode = item.getBarcode();
			final String userId = loggedUser.getId();
			String requestType = chooseRequestType(item);
			boolean canChangeLoanType = false;
			if (folioAPI.isCopyAvailable(item.getFolioJson().toString())) {
				canChangeLoanType = true;
			}
			FolioResponseModel folioResponse = folioAPI.sendCopyRequest(record.getNote(), barcode, item.getId(), userId, servicePointId, requestType, accessToken);			
			boolean isFolioRequestSent = Integer.toString(folioResponse.getResponseCode()).startsWith("2");
			if (isFolioRequestSent) {										
				String updatedItem = folioAPI.getFirstCopy(folioAPI.getCopyInfo(barcode, accessToken));		
				if (canChangeLoanType) {
					changeLoanType(accessToken, updatedItem, servicePointId, folioAPI);	
				}
				//FolioOpenings servicePointCalendar = folioAPI.getOpeningDays(folioAPI.getServicePointCalendar(accessToken, servicePointId));
				// print request to the right service point					
				try {
					JasperPrint jasperPrint = createJasperPrint(servicePointLabel, item, record, loggedUser, requestType, request);
					request.getSession().setAttribute("pdf", jasperPrint);
					byte[] pdf = createDataToPrint(jasperPrint);
					sendDirectToPrinter(pdf, printingPoint, item, loggedUser, record, request);
					//sendToCupsPrinter(pdf, printingPoint, servicePointLabel, item, record, loggedUser, requestType, request, response);
				} 
				catch (Exception e) {		
					Log.error("Error while printing request", e);
					request.setAttribute("inError", true);		
					request.setAttribute("errorMessage", "error during print");					
					return;
				}
											
				request.setAttribute("message", "request_success");					
				return;
			}
			else {
				request.setAttribute("inError", true);		
				request.setAttribute("errorMessage", "something go wrong");					
				return;
			}
		}
		else {
			request.setAttribute("inError", true);		
			request.setAttribute("errorMessage", "too_many_request_loan");				
			return;
		}
	}
	
	
	protected boolean isLoanRequest(final String servicePointId) {
		return folioConfig.getString(FolioConstants.LOAN_ROOM).equals(servicePointId);
	}
	
	
	protected int getLoanRequestCount(String accessToken, FolioUserModel user, final String servicePointId) throws FolioException {
		return folioAPI.getUserLoanRequestOpenCount(accessToken, servicePointId, user.getId());
	}
	
	protected int getLoanLoanCount(String accessToken, FolioUserModel user) throws FolioException {
		int resultCount = 0;
		List<FolioLoan> loans = folioAPI.getLoanList(folioAPI.getUserOpenLoans(accessToken, user.getId()));
		for (FolioLoan currentloan : loans) {
			FolioItemModel currentItem = folioAPI.getCopyById(currentloan.getItemId(), accessToken);
			if (isLoanType(currentItem.getPermanentLoanTypeContent())) {
				resultCount ++;
			}
		}
		return resultCount;
	}
	
	protected int getLoanConsultCount (String accessToken, FolioUserModel user, final String materialTypeCode) throws FolioException {
		int resultCount = 0;
		List<FolioLoan> loans = folioAPI.getLoanList(folioAPI.getUserOpenLoans(accessToken, user.getId()));
		for (FolioLoan currentloan : loans) {
			FolioItemModel currentItem = folioAPI.getCopyById(currentloan.getItemId(), accessToken);
			boolean isConsult = isConsultType(currentItem.getPermanentLoanTypeContent());
			String currentMaterialType = currentItem.getMaterialType();
			
			if (isConsult && 
					(currentMaterialType.equals(materialTypeCode) || getOtherMaterialType(user, materialTypeCode, materialTypeLimits).contains(currentMaterialType))) {
				resultCount ++;
			}
		}
		return resultCount;
	}
	
	protected boolean isLoanType (final String loanType) {
		return loanType.equals(folioConfig.getString(FolioConstants.LOAN_TYPE_LOAN_DESCR));
	}
	
	protected boolean isConsultType (final String loanType) {
		return loanType.equals(folioConfig.getString(FolioConstants.LOAN_TYPE_CONS_DESCR));
	}
	
	
	/**
	 * give the right folio requestType according to availability of the item
	 * @param item
	 * @return requestType
	 */

	protected String chooseRequestType (FolioItemModel item) {
		if (item.isCopyAvailable()) {
			return folioConfig.getString(FolioConstants.PAGE_REQUEST);
		}
		else {
			return folioConfig.getString(FolioConstants.HOLD_REQUEST);
		}
	}
	
	/**
	 * Get printer name
	 * @param printingPoint
	 * @param collocation
	 * @return
	 */
	
	protected String choosePrintingPointName (final String printingPoint,
												final String collocation) {
		if (printingPoint != null) {
			return printingPoint;			
		}
		else {
			return getPrinterNameByCollocation(collocation);
		}
	}
	
	protected String findPrinterIPbyName (final String printerName, ConfigurationTool configuration) {
		return configuration.getPrinterIP(printerName);
	}
	
	/**
	 * Get printer IP
	 * If printingPoint is defined -> used it, then choose it according to the collocation
	 * @param printingPoint
	 * @param collocation
	 * @param configuration
	 * @return
	 */
	protected String choosePrintingPoint(final String printingPoint,
								final String collocation,
								ConfigurationTool configuration) {
		if (printingPoint != null) {
			return configuration.getPrinterIP(printingPoint);			
		}
		else {
			return getPrinterByCollocation(collocation, configuration);
		}
	}
	
	protected JasperPrint createJasperPrint (String servicePoint, 
										FolioItemModel item, 
										FolioRecord record, 
										FolioUserModel user, 
										String requestType,
										final HttpServletRequest request) throws JRException, IOException {
		
		ByteArrayOutputStream output = null;
		String relativePath = "/reports/bncf_request.jrxml";		
		String reportName = request.getSession().getServletContext().getRealPath(relativePath);
		JasperDesign design = JRXmlLoader.load(new FileInputStream(reportName));				
		JasperReport report = JasperCompileManager.compileReport(design);
		Map<String, Object> params = new HashMap<String, Object>();	
		params.put("service_point", servicePoint);
		params.put("user_category", user.getPatronGroupName());
		params.put("username", user.getLastName() +" "+ user.getFirstName() + " (" + user.getBarcode() + ")");
		params.put("user_barcode", user.getBarcode());
		params.put("shelflist", record.getCollocation());
		params.put("author", record.getAuthorName());
		params.put("title", record.getTitle());
		params.put("barcode", item.getBarcode());
		params.put("publisher",  record.getPublisher());
		params.put("enumeration", record.getEnumeration());
		params.put("new_collocation", record.getNewCollocation());
		params.put("note", record.getNote());
		params.put("request_type", requestTypeLabel(requestType));
		params.put("edition", record.getEdition());
		params.put("holding", record.getHolding());
		
		String dateRequest = new SimpleDateFormat("dd/MM/yyyy").format(new Date()) +" "+	new SimpleDateFormat("HH:mm:ss a").format(new Date());
		params.put("date_request", dateRequest);
		JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, new JREmptyDataSource());
		return jasperPrint;
		
	}
	
	protected byte[] createDataToPrint(JasperPrint jasperPrint) throws JRException {
		byte[] pdf = null;
		ByteArrayOutputStream output = null;
		output = new ByteArrayOutputStream();
		JasperExportManager.exportReportToPdfStream(jasperPrint, output);
		pdf = output.toByteArray();
		return pdf;
	}
		
	protected void sendDirectToPrinter (byte[]pdf, String printingPoint, FolioItemModel item, FolioUserModel user, FolioRecord record, final HttpServletRequest request) {
		ConfigurationTool configuration = getConfiguration(request);
		String printerName = choosePrintingPointName (printingPoint, record.getCollocation());
		String printerIp = findPrinterIPbyName(printerName, configuration);
		try {
			printPDF(pdf, printerIp);
		}
		catch (Exception e) {
			Log.error("Stampa fallita");
			Log.error(e.getMessage());			
			String fileName = savePDFtoFile (pdf, item, user, printerName, request);
			Log.info("Salvataggio del file " + fileName + "per la ristampa successiva");
		}
	}
	
	protected String savePDFtoFile (byte[] pdf,
									FolioItemModel item, 
									FolioUserModel user,
									String printerName,
									final HttpServletRequest request) {
		ConfigurationTool configuration = getConfiguration(request);
		String filePath = configuration.getPrintFolder();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String fileName = timestamp.toString() + "_" + item.getBarcode() + "_" + user.getBarcode() + "_" + printerName;
		fileName = fileName.replaceAll(" ", "_").replaceAll(":", "_").replace('.', '_');
		String fileNameAndPath = filePath + fileName;
		/*
		Log.info("salvataggio pdf in corso per la richiesta:");
		Log.info("-service point: " + servicePoint);
		Log.info("-print point: " + printerName);
		Log.info("-user: " + user.getLastName() +" "+ user.getFirstName() + " (" + user.getBarcode() + ")");
		Log.info("-title: " + record.getTitle());
		Log.info("-barcode copy: " + item.getBarcode());
		*/

		savePdfPs(pdf, fileNameAndPath);
		return fileNameAndPath;
	}
	
	protected void sendToCupsPrinter(byte[] pdf,
								String printingPoint,
								String servicePoint, 
								FolioItemModel item, 
								FolioRecord record, 
								FolioUserModel user, 
								String requestType,
								final HttpServletRequest request,
								final HttpServletResponse response) throws JRException, UnknownHostException, IOException {
		
		String printerName = null;
		
		try {		
			ConfigurationTool configuration = getConfiguration(request);
			printerName = choosePrintingPointName (printingPoint, record.getCollocation());
					
						
			if (printerName == null) {
				Log.error("Nessun punto di stampa per item " + item.getBarcode() );
				return;
			}
						
			boolean deletePdfEnabled = configuration.isDeletePdfEnabled();
			String fileNameAndPath = savePDFtoFile(pdf, item, user, printerName, request);			
			printPs(fileNameAndPath, printerName, configuration.getPrintCommand(), deletePdfEnabled);		
						
		} catch (Exception e) {
			//sendPrintEmail(request, pdf, printingPoint, record.getCollocation());
			Log.error("error printing " + printerName + " collocation: " + record.getCollocation());
			e.printStackTrace();
		}
	
	}
	
	private void printPs(String fileName, String printerPointName, String printCommand, boolean deletePdfEnabled) {
		try {
			String printCommandToSend = (printCommand != null || !"".equals(printCommand)) ? printCommand : "lpr"; 
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command("bash", "-c", printCommandToSend + " -P " + printerPointName + "  " + fileName + ".pdf");			
			Log.info("executing command " + processBuilder.command().toString());
			Process process = processBuilder.start();
			
			StringBuilder commandOutput = new StringBuilder();
	
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			
			BufferedReader error =
	                new BufferedReader(new InputStreamReader(process.getErrorStream()));
	
			String line;
			while ((line = reader.readLine()) != null) {
				commandOutput.append(line + "\n");
			}
	
			int exitVal = process.waitFor();
			if (exitVal == 0) {
				Log.info(commandOutput.toString() + " stampa inviata");
				if (deletePdfEnabled) {
					File fileToDelete = new File (fileName + ".pdf");
					boolean isDeleted = fileToDelete.delete();
					if (!isDeleted) {
						Log.error("file " + fileName + ".pdf non cancellato correttamente");
					}
				}
			} else {
				Log.info(commandOutput.toString() + " anomalia nella stampa");
				Log.error("exit " + exitVal);
				while ((line = error.readLine()) != null) {
					Log.error(line);
			    }
			}
			
		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private void savePdfPs(byte[] pdf, String fileNameAndPath) {
		try {
			File file = new File(fileNameAndPath + ".pdf");
			OutputStream os = new FileOutputStream(file);
			os.write(pdf);
			os.close();
			
			//TO-DO: enable only if customer can't print pdf
			/*
			ProcessBuilder processBuilder = new ProcessBuilder();
			
			String osName = System.getProperty("os.name");
			if (osName != null && osName.startsWith("Windows")) {
				processBuilder.command("cmd", "/c", "pdf2ps", fileNameAndPath + ".pdf ", fileNameAndPath + ".ps");
			}
			else {
				processBuilder.command("bash", "-c", "pdf2ps " + fileNameAndPath + ".pdf " +  fileNameAndPath + ".ps");
			}
			Process process = processBuilder.start();
	
			StringBuilder commandOutput = new StringBuilder();
	
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			
			BufferedReader error =
		                new BufferedReader(new InputStreamReader(process.getErrorStream()));
	
			String line;
			while ((line = reader.readLine()) != null) {
				commandOutput.append(line + "\n");
			}
	
			int exitVal = process.waitFor();
			if (exitVal == 0) {				
				Log.info(commandOutput.toString() + " file ps generato");
			} else {
				Log.error(commandOutput.toString() + " anomalia nella generazione del file ps");
				Log.error("exit " + exitVal);
				while ((line = error.readLine()) != null) {
					Log.error(line);
			    }
			}
			*/
		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	/*
	protected void sendPrintEmail(final HttpServletRequest request, byte[] pdf, String printinPoint, String collocation) {
		String senderName = getConfiguration(request).getEmailSenderAddress();
		String subject = "Nuova richiesta";
		try {
			Message email = new MimeMessage(getMailSession());
		
			email.setFrom(new InternetAddress(senderName));		
			InternetAddress [] to = { chooseReciever(printinPoint, collocation) };
			email.setRecipients(Message.RecipientType.TO, to);
			email.setSubject(subject);
			email.setSentDate(new Date());
			
			Multipart multipart = new MimeMultipart();			
			MimeBodyPart attachment = new MimeBodyPart();
			DataSource datasource = new ByteArrayDataSource(pdf, "application/octet-stream");
			attachment.setDataHandler(new DataHandler(datasource));
			attachment.setFileName("attachment.html");
			
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
									
			multipart.addBodyPart(attachment);
			multipart.addBodyPart(mimeBodyPart);
			email.setContent(multipart);
			
			Transport.send(email);
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
		
	}
	
	protected InternetAddress [] chooseReciever(String printingPoint, String collocation) {
		InternetAddress [] to = { new InternetAddress(emailAddress) };
		return to;
	}
	*/
	
	
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
	
	
	protected String requestTypeLabel(final String requestType) {
		final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
		if (folioConfig.getString(FolioConstants.PAGE_REQUEST).equals(requestType)) {
			return "Richiesta";
		}
		if (folioConfig.getString(FolioConstants.HOLD_REQUEST).equals(requestType)) {
			return "Prenotazione";
		}
		return null;
	}
	
	protected String getPrinterNameByCollocation (final String collocation) {
		String acronim = null;
		Map<String, String> mapPrinterPoint = getCollocationMap();
		for (String coll : mapPrinterPoint.keySet()) {
			if (collocation.startsWith(coll)) {
				acronim = mapPrinterPoint.get(coll);
				break;
			}				
		}
		if (acronim != null) {
			return acronim;
		}
		else {
			Log.error("Nessun punto di stampa per la collocazione " + collocation);
			return FolioConstants.PRINTING_POINT_SLETTURA;
		}
	}

	protected String getPrinterByCollocation(final String collocation, ConfigurationTool configuration) {
		String acronim = null; 
		Map<String, String> mapPrinterPoint = getCollocationMap();
		for (String coll : mapPrinterPoint.keySet()) {
			if (collocation.startsWith(coll)) {
				acronim = mapPrinterPoint.get(coll);
				break;
			}				
		}
		
		return (acronim != null) ?configuration.getPrinterIP(acronim) :configuration.getPrinterIP(FolioConstants.PRINTING_POINT_SLETTURA); 				
	}


	protected Map<String, String> getCollocationMap(){
		Map<String, String> mapPrinterPoint = new HashMap<String, String>();
		mapPrinterPoint.put("ACSER", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("ATL", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("BONAM", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("BONANNI", FolioConstants.PRINTING_POINT_SLETTURA); 
		mapPrinterPoint.put("C.MUS", FolioConstants.PRINTING_POINT_SMUSICA);
		mapPrinterPoint.put("CAPR.", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("CART", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("CAS.S", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("CASS", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("CF.M", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("CF.S", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("CINA", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("CONS", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("CONT", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("CPREG", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("DE GU", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("F.A.", FolioConstants.PRINTING_POINT_SMANOSCRITTI);
		mapPrinterPoint.put("F.A.O.", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("F.D.", FolioConstants.PRINTING_POINT_SMANOSCRITTI);
		mapPrinterPoint.put("FM.", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("FM.C", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("FILIP", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("FOTO", FolioConstants.PRINTING_POINT_SMANOSCRITTI);
		mapPrinterPoint.put("FV", FolioConstants.PRINTING_POINT_SMUSICA);
		mapPrinterPoint.put("GEN", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("GIGL", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("GIOR", FolioConstants.PRINTING_POINT_SPERIODICI);
		mapPrinterPoint.put("GRUPPI", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("LANZA", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("LELJ", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("LUSIT.", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("MAGL.", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("MF.S", FolioConstants.PRINTING_POINT_SPERIODICI);
		mapPrinterPoint.put("MILA", FolioConstants.PRINTING_POINT_SMUSICA);
		mapPrinterPoint.put("MUS", FolioConstants.PRINTING_POINT_SMUSICA);
		mapPrinterPoint.put("N.A.1475", FolioConstants.PRINTING_POINT_SMANOSCRITTI);
		mapPrinterPoint.put("N.MUS", FolioConstants.PRINTING_POINT_SMUSICA);
		mapPrinterPoint.put("NENC.", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("OAF", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("OLSCH", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("P.GIO", FolioConstants.PRINTING_POINT_SPERIODICI);
		mapPrinterPoint.put("P.PER", FolioConstants.PRINTING_POINT_SPERIODICI);
		mapPrinterPoint.put("P.PU", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("P.RIV", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("PALAT", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("PASS.", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("PRARI", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("PREG", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("RARI", FolioConstants.PRINTING_POINT_SMANOSCRITTI);
		mapPrinterPoint.put("RIN.", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("RIV", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("RO.CA", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("S.M.", FolioConstants.PRINTING_POINT_SMUSICA);
		mapPrinterPoint.put("TARG.", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("TDR", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("TH.", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("V.ACC.", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("V.BAN", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("V.COL", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("V.CON", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("V.GR", FolioConstants.PRINTING_POINT_SCONSULTAZIONE);
		mapPrinterPoint.put("V.MIS", FolioConstants.PRINTING_POINT_SLETTURA);
		mapPrinterPoint.put("ZAG", FolioConstants.PRINTING_POINT_SLETTURA);

		return mapPrinterPoint;
	}
	
	
	
	protected void printPDF(byte[] pdf, String ipPrinter) throws UnknownHostException, IOException {				
		
		if (ipPrinter == null) {
			Log.error("Nessuna ip trovato per la stampa");
			return;
		}
		
		String ipAddress = ipPrinter.substring(0, ipPrinter.indexOf(":"));
		int port = Integer.parseInt(ipPrinter.substring(ipPrinter.indexOf(":")+1));
		
		OutputStream output= null;		
		
	    Socket socket = new Socket(ipAddress, port);
	    output = socket.getOutputStream();		    		  
	    
	    output.write(pdf);
        output.flush();
        socket.shutdownOutput();            
	     
	    output.close();
        socket.close();
        Log.info("Stampa inviata a " + ipAddress + " " + port);
        
	}
	
	/**
	 * The user request can be "Prestito" or "Consultazione" and change the PermanentLoanType in Folio if it's the first request.
	 * @param configuration
	 * @param folioToken
	 * @param updatedItem
	 * @param servicePoint
	 * @param folioApi
	 */
	
	protected void changeLoanType(final String folioToken, final String updatedItem, final String servicePoint, final FolioRestApi folioApi) {
		//if (folioApi.isCopyPaged(updatedItem)) {
			folioApi.changeLoanTypeAsServicePoint (folioToken, updatedItem, servicePoint);			
		//}
	}
	
	
	
	protected boolean doesUserExceed(final FolioUserModel user, final String itemMaterialType, int currentRequestCount,  Map<String, Map<String, String>> materialLimits) {
		if (user.isStaff()) {
			return false;
		}		
		else {			
			int max = getMaxLimit(user, itemMaterialType, materialLimits);
			return max <= currentRequestCount;
		}
	}
	
	private String getOtherMaterialType (FolioUserModel user, String itemMaterialType, Map<String, Map<String, String>> materialLimits) {
		String userType = user.getPatronGroupCode();
		Map<String, String> limits =  materialLimits.get(userType);
		return getMaterialTypesLimit(limits.get(itemMaterialType));
	}
	
	private int getMaxLimit (FolioUserModel user, String itemMaterialType, Map<String, Map<String, String>> materialLimits) {
		String userType = user.getPatronGroupCode();
		Map<String, String> limits =  materialLimits.get(userType);
		String materialTypeLimit = limits.get(itemMaterialType);
		if (materialTypeLimit == null) {
			Log.error("ERRORE: Nessun limite per il materiale " + itemMaterialType + " con patrongroup: " + userType);
		}
		return getLimitNumber(materialTypeLimit);
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		materialTypeLimits = getMaterialTypeLimits();
	}
	
	protected Map<String, Map<String, String>> getMaterialTypeLimits() {
		
		Map<String, Map<String, String>> materialTypeLimits = new HashMap<String, Map<String, String>>();
		
		//utente preregistrato
		// 9 monografie: tra materiale che ha codice di fruibilità A-C-F-L-R
		Map<String, String> userPreregister = new HashMap<String, String>();
		userPreregister.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID), "9;A-B-C-F-L-R");
		userPreregister.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_ID), "9;A-B-C-F-L-R");
		userPreregister.put(folioConfig.getString(FolioConstants.MUSIC_ID), "9;A-B-C-F-L-R");
		userPreregister.put(folioConfig.getString(FolioConstants.THESIS_ID), "9;A-B-C-F-L-R");
		userPreregister.put(folioConfig.getString(FolioConstants.ELETRONIC_ID), "9;A-B-C-F-L-R");
		// 3 E
		userPreregister.put(folioConfig.getString(FolioConstants.SERIAL_AN_ID), "3");
		// 3 G
		userPreregister.put(folioConfig.getString(FolioConstants.JOURNAL_ID), "3");
		// 3 M
		userPreregister.put(folioConfig.getString(FolioConstants.MICROFILM_ID), "3");
		// 3 D
		userPreregister.put(folioConfig.getString(FolioConstants.SERIAL_MOD_ID), "3");
		// 0 manoscritti
		userPreregister.put(folioConfig.getString(FolioConstants.HANDWRITE_ID), "0");
		materialTypeLimits.put(folioConfig.getString(FolioConstants.USER_PREREG), userPreregister);
		
		//utente base
		// 9 monografie: tra materiale che ha codice di fruibilità A-L-R	
		Map<String, String> userBase = new HashMap<String, String>();	
		userBase =  new HashMap<String, String>();
		userBase.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID), "9;A-B-L-R");		
		userBase.put(folioConfig.getString(FolioConstants.THESIS_ID), "9;A-B-L-R");
		userBase.put(folioConfig.getString(FolioConstants.ELETRONIC_ID), "9;A-B-L-R");
		// 4 D
		userBase.put(folioConfig.getString(FolioConstants.SERIAL_MOD_ID), "4");
		// 4 G
		userBase.put(folioConfig.getString(FolioConstants.JOURNAL_ID), "4");
		// 4 M
		userBase.put(folioConfig.getString(FolioConstants.MICROFILM_ID), "4");
		// 0 manoscritti
		userBase.put(folioConfig.getString(FolioConstants.HANDWRITE_ID), "0");
		materialTypeLimits.put(folioConfig.getString(FolioConstants.USER_BASE), userBase);
		
		//utente base + prestito
		//9 monografie: tra materiale che ha codice di fruibilità A-L-R		
		Map<String, String> userBaseLoan = new HashMap<String, String>();	
		userBaseLoan =  new HashMap<String, String>();
		userBaseLoan.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID), "9;A-B-L-R");		
		userBaseLoan.put(folioConfig.getString(FolioConstants.THESIS_ID), "9;A-B-L-R");
		userBaseLoan.put(folioConfig.getString(FolioConstants.ELETRONIC_ID), "9;A-B-L-R");
		// 4 D
		userBaseLoan.put(folioConfig.getString(FolioConstants.SERIAL_MOD_ID), "4");
		// 4 G
		userBaseLoan.put(folioConfig.getString(FolioConstants.JOURNAL_ID), "4");
		// 4 M
		userBaseLoan.put(folioConfig.getString(FolioConstants.MICROFILM_ID), "4");
		// 0 manoscritti
		userBaseLoan.put(folioConfig.getString(FolioConstants.HANDWRITE_ID), "0");
		materialTypeLimits.put(folioConfig.getString(FolioConstants.USER_BASE_LOAN), userBaseLoan);
		
		//utente musica
		// 9 monografie: tra materiale che ha codice di fruibilità A-C-F-L-R
		Map<String, String> userMusic = new HashMap<String, String>();
		userMusic.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID), "9;A-B-C-F-L-R");
		userMusic.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_ID), "9;A-B-C-F-L-R");
		userMusic.put(folioConfig.getString(FolioConstants.MUSIC_ID), "9;A-B-C-F-L-R");
		userMusic.put(folioConfig.getString(FolioConstants.THESIS_ID), "9;A-B-C-F-L-R");
		userMusic.put(folioConfig.getString(FolioConstants.ELETRONIC_ID), "9;A-B-C-F-L-R");
		// 3 E
		userMusic.put(folioConfig.getString(FolioConstants.SERIAL_AN_ID), "3");
		// 3 G
		userMusic.put(folioConfig.getString(FolioConstants.JOURNAL_ID), "3");
		// 3 M
		userMusic.put(folioConfig.getString(FolioConstants.MICROFILM_ID), "3");
		// 3 D
		userMusic.put(folioConfig.getString(FolioConstants.SERIAL_MOD_ID), "3");
		// 0 manoscritti
		userMusic.put(folioConfig.getString(FolioConstants.HANDWRITE_ID), "0");
		materialTypeLimits.put(folioConfig.getString(FolioConstants.USER_MUSIC), userMusic);
		
		//utente musica prestito
		// 9 monografie: tra materiale che ha codice di fruibilità A-C-F-L-R
		Map<String, String> userMusicLoan = new HashMap<String, String>();
		userMusicLoan.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID), "9;A-B-C-F-L-R");
		userMusicLoan.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_ID), "9;A-B-C-F-L-R");
		userMusicLoan.put(folioConfig.getString(FolioConstants.MUSIC_ID), "9;A-B-C-F-L-R");
		userMusicLoan.put(folioConfig.getString(FolioConstants.THESIS_ID), "9;A-B-C-F-L-R");
		userMusicLoan.put(folioConfig.getString(FolioConstants.ELETRONIC_ID), "9;A-B-C-F-L-R");
		// 3 E
		userMusicLoan.put(folioConfig.getString(FolioConstants.SERIAL_AN_ID), "3");
		// 3 G
		userMusicLoan.put(folioConfig.getString(FolioConstants.JOURNAL_ID), "3");
		// 3 M
		userMusicLoan.put(folioConfig.getString(FolioConstants.MICROFILM_ID), "3");
		// 3 D
		userMusicLoan.put(folioConfig.getString(FolioConstants.SERIAL_MOD_ID), "3");
		// 0 manoscritti
		userMusicLoan.put(folioConfig.getString(FolioConstants.HANDWRITE_ID), "0");
		materialTypeLimits.put(folioConfig.getString(FolioConstants.USER_MUSIC_LOAN), userMusicLoan);
		
		//utente manoscritti
		// 9 monografie: tra materiale che ha codice di fruibilità A-C-F-L-R
		Map<String, String> userHandwrite = new HashMap<String, String>();
		userHandwrite.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID), "9;A-B-C-F-L-R");
		userHandwrite.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_ID), "9;A-B-C-F-L-R");
		userHandwrite.put(folioConfig.getString(FolioConstants.MUSIC_ID), "9;A-B-C-F-L-R");
		userHandwrite.put(folioConfig.getString(FolioConstants.THESIS_ID), "9;A-B-C-F-L-R");
		userHandwrite.put(folioConfig.getString(FolioConstants.ELETRONIC_ID), "9;A-B-C-F-L-R");
		// 3 E
		userHandwrite.put(folioConfig.getString(FolioConstants.SERIAL_AN_ID), "3");
		// 3 G
		userHandwrite.put(folioConfig.getString(FolioConstants.JOURNAL_ID), "3");
		// 3 M
		userHandwrite.put(folioConfig.getString(FolioConstants.MICROFILM_ID), "3");
		// 3 D
		userHandwrite.put(folioConfig.getString(FolioConstants.SERIAL_MOD_ID), "3");
		// 3 H manoscritti
		userHandwrite.put(folioConfig.getString(FolioConstants.HANDWRITE_ID), "3");
		materialTypeLimits.put(folioConfig.getString(FolioConstants.USER_HANDWRITE), userHandwrite);

		//utente manoscritti + prestito
		// 9 monografie: tra materiale che ha codice di fruibilità A-C-F-L-R
		Map<String, String> userHandwriteLoan = new HashMap<String, String>();
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID), "9;A-B-C-F-L-R");
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_ID), "9;A-B-C-F-L-R");
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.MUSIC_ID), "9;A-B-C-F-L-R");
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.THESIS_ID), "9;A-B-C-F-L-R");
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.ELETRONIC_ID), "9;A-B-C-F-L-R");
		// 3 E
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.SERIAL_AN_ID), "3");
		// 3 G
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.JOURNAL_ID), "3");
		// 3 M
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.MICROFILM_ID), "3");
		// 3 D
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.SERIAL_MOD_ID), "3");
		// 3 manoscritti
		userHandwriteLoan.put(folioConfig.getString(FolioConstants.HANDWRITE_ID), "3");
		materialTypeLimits.put(folioConfig.getString(FolioConstants.USER_HANDWRITE_LOAN), userHandwriteLoan);
		
		return materialTypeLimits;
	}
	
	private int getLimitNumber(final String limitText) {
		String [] limits = limitText.split(";");
		return Integer.parseInt(limits[0]);
	}
	
	private String getMaterialTypesLimit (final String limitText) {
		String [] limits = limitText.split(";");
		if (limits.length > 1) {
			String [] codes = limits[1].split("-");
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < codes.length; i ++) {
				result.append(getItemTypeMap().get(codes[i]) + ";");
			}
			return result.toString();
		}
		else return "";
	}
	
	protected Map<String, String> getHoldingSectionMap() {
		Map<String, String> map = new HashMap<String, String> ();
		map.put("A", folioConfig.getString(FolioConstants.SECTION_HOLDING_A));
		map.put("C", folioConfig.getString(FolioConstants.SECTION_HOLDING_C));
		map.put("D", folioConfig.getString(FolioConstants.SECTION_HOLDING_D));
		map.put("E", folioConfig.getString(FolioConstants.SECTION_HOLDING_E));
		map.put("F", folioConfig.getString(FolioConstants.SECTION_HOLDING_F));
		map.put("G", folioConfig.getString(FolioConstants.SECTION_HOLDING_G));
		map.put("H", folioConfig.getString(FolioConstants.SECTION_HOLDING_H));
		map.put("K", folioConfig.getString(FolioConstants.SECTION_HOLDING_K));
		map.put("L", folioConfig.getString(FolioConstants.SECTION_HOLDING_L));
		map.put("M", folioConfig.getString(FolioConstants.SECTION_HOLDING_M));
		map.put("R", folioConfig.getString(FolioConstants.SECTION_HOLDING_R));
		return map;
	}
	
	protected String getHoldingSection(final String fruitionCode) {
		Map<String, String> map = getHoldingSectionMap();
		return map.get(fruitionCode);
	}
	
	protected String getItemType(final String fruitionCode) {
		Map<String, String> map = getItemTypeMap();
		return map.get(fruitionCode);
	}
	
	protected Map<String, String> getItemTypeMap() {
		Map<String, String> map = new HashMap<String, String> ();
		map.put("A", folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID));
		map.put("C", folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_ID));
		map.put("D", folioConfig.getString(FolioConstants.SERIAL_MOD_ID));
		map.put("E", folioConfig.getString(FolioConstants.SERIAL_AN_ID));
		map.put("F", folioConfig.getString(FolioConstants.MUSIC_ID));
		map.put("G", folioConfig.getString(FolioConstants.JOURNAL_ID));
		map.put("H", folioConfig.getString(FolioConstants.HANDWRITE_ID));
		map.put("K", folioConfig.getString(FolioConstants.RESOURCE_NOT_AVAIL_ID));
		map.put("L", folioConfig.getString(FolioConstants.THESIS_ID));
		map.put("M", folioConfig.getString(FolioConstants.MICROFILM_ID));
		map.put("R", folioConfig.getString(FolioConstants.ELETRONIC_ID));
		return map;
	}
	
	protected Map<String, String> getRevertItemTypeMap() {
		Map<String, String> map = new HashMap<String, String> ();
		map.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID), "A");
		map.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_ID), "C");
		map.put(folioConfig.getString(FolioConstants.SERIAL_MOD_ID), "D");
		map.put(folioConfig.getString(FolioConstants.SERIAL_AN_ID), "E");
		map.put(folioConfig.getString(FolioConstants.MUSIC_ID), "F");
		map.put(folioConfig.getString(FolioConstants.JOURNAL_ID), "G");
		map.put(folioConfig.getString(FolioConstants.HANDWRITE_ID),"H");
		map.put(folioConfig.getString(FolioConstants.RESOURCE_NOT_AVAIL_ID), "K");
		map.put(folioConfig.getString(FolioConstants.THESIS_ID), "L");
		map.put(folioConfig.getString(FolioConstants.MICROFILM_ID), "M");
		map.put(folioConfig.getString(FolioConstants.ELETRONIC_ID), "R");
		return map;
	}
	
	protected String getText(ResourceBundle messages, ResourceBundle add_messages, final String key) {
		if (add_messages != null && add_messages.containsKey(key)) {
			return add_messages.getObject(key).toString();
		}
		else {
			if (messages != null && messages.containsKey(key)) {
				return messages.getObject(key).toString();
			}
			else {
				return ResourceBundle.getBundle("additional_resources", Locale.ITALIAN).getObject(key).toString();
			}				
		}			
	}
	
	/**
	 * On doGet we call the retry to print
	 */
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		ConfigurationTool configuration = getConfiguration(request);
		String filePath = configuration.getPrintFolder();
		File folder = new File(filePath);
		File [] folderFiles = folder.listFiles();
		Arrays.sort(folderFiles);
		//ripartisci i file per punto di stampa
		LinkedHashMap<String, List<File>> fileListForPrinter = new LinkedHashMap<String, List<File>>();
		for (final File fileToPrint : folderFiles) {			
			String fileName = fileToPrint.getName();
			String printingPoint = getPrintingPointFromFileName(fileName);
			if (fileListForPrinter.get(printingPoint) == null) {
				fileListForPrinter.put(printingPoint, new LinkedList<File>());
			}
			fileListForPrinter.get(printingPoint).add(fileToPrint);			
		}
		
		int MAX_FILE_PER_SESSION_PRINTING = 20;
		
		int countPrinted = 0;	
		
		for (final String currentPrinter : fileListForPrinter.keySet()) {
			List<File> currentFileList = fileListForPrinter.get(currentPrinter);
			int countPerPrinter = 0;
			for (final File fileToPrint : currentFileList) {			
				countPerPrinter ++;
				if (countPerPrinter <= MAX_FILE_PER_SESSION_PRINTING) {
					byte [] pdfToPrint = FileUtils.readFileToByteArray(fileToPrint);
					String fileName = fileToPrint.getName();
					String ipPrinter = choosePrintingPoint(currentPrinter, null, configuration);
					try {
						 Log.info("Tentativo di stampa per " + fileName);
						 printPDF(pdfToPrint, ipPrinter);
						 fileToPrint.delete();
						 countPrinted ++;
					 }
					 catch (Exception e) {
						 Log.error("Stampa fallita per il file " + fileName);
						 Log.error(e.getClass() + " " + e.getMessage());
					 }
				}
			}
		}		
		
		String message = "Stampati " +  countPrinted + " / " + folderFiles.length;
		
		response.setContentType("application/json");
		StringBuilder builderResponse = new StringBuilder();
		builderResponse.append("{");
		builderResponse.append("\"message\" : ").append("\"").append(message).append("\"");
		builderResponse.append("}");
		response.getWriter().println(builderResponse.toString());	
		
	}
	
	private String getPrintingPointFromFileName (final String fileName) {
		int indexUnderscore = fileName.lastIndexOf("_");
		int indexDot = fileName.lastIndexOf(".");
		return fileName.substring(indexUnderscore + 1, indexDot);
	}
	
}

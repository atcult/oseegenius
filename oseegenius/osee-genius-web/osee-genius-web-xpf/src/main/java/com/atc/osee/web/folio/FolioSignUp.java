package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

//new import
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioSignUp extends FolioUserServlet {
	private static final long serialVersionUID = 12575453L;
	protected int FIRST_STEP = 1;
	protected int SECOND_STEP = 2;
	protected int THIRD_STEP = 3;
	protected int FORTH_STEP = 4;
	
	
	
	protected static DataSource datasource;
	protected BncfDao dao;
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	protected String getCurrentLayout (FolioUserModel loggedUser) {
		if (loggedUser != null && loggedUser.isAdmin()) {
			return "/workspace_layout.vm";
		}
		else return  "/one_column.vm";	
	}

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		String layoutName = getCurrentLayout(loggedUser);
		
		request.setAttribute("step", FIRST_STEP);
		forwardTo(request, response, "/components/userPanel/sign_up.vm", layoutName);
	}
	
	protected void step1 (int currentStep, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		FolioUserModel user = createUserFromStep1(request);	
		request.setAttribute("user", user);
		if (mandatoryCheckFirstStep(request, user)) {
			request.getSession().setAttribute("user", user);
			request.setAttribute("step", currentStep + 1);
			try {
				request.setAttribute("cityList", dao.getCityList());				
			} catch (Exception exception) {
				exception.printStackTrace();
				Log.error(exception.getMessage());
				request.setAttribute("inError", true);	
				request.setAttribute("errorMessage", "something go wrong");
				forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
				return;
			}
			forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
			return;
		}
		else {
			request.setAttribute("step", currentStep);
			request.setAttribute("inError", true);	
			request.setAttribute("errorMessage", "compile_mandatory");
			forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
			return;
		}
	}
	
	protected void step2 (int currentStep, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		FolioUserModel user = (FolioUserModel) request.getSession().getAttribute("user");
		injectUserStep2(request, user);
		request.setAttribute("user", user);
		String propertiesFile="country_address_" + getVisit(request).getPreferredLocale()   +".properties";
		setCountryAddress(request, propertiesFile);
		if (mandatoryCheckSecondStep(request, user)) {
			request.getSession().setAttribute("user", user);
			request.setAttribute("step", currentStep + 1);
			forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
			return;
		}
		else {
			request.setAttribute("step", currentStep);
			request.setAttribute("inError", true);	
			request.setAttribute("errorMessage", "compile_mandatory");
			forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
			return;
		}

	}
	protected void step3 (int currentStep, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		FolioUserModel user = (FolioUserModel) request.getSession().getAttribute("user");
		injectUserStep3(request, user);
		request.setAttribute("user", user);
		
		if (mandatoryCheckThirdStep(request, user)) {
			request.getSession().setAttribute("user", user);
			request.setAttribute("step", currentStep + 1);
			forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
			return;
		}
		else {
			request.setAttribute("step", currentStep);
			request.setAttribute("inError", true);	
			request.setAttribute("errorMessage", "compile_mandatory");
			forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
			return;
		}
	} 
	
	protected void step4 (int currentStep, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		FolioUserModel user = (FolioUserModel) request.getSession().getAttribute("user");
		injectUserStep4(request, user);
		request.setAttribute("user", user);
		if (mandatoryCheckForthStep(request, user, loggedUser)) {
			try {
				if (userAlreadyRegistered(user)) {
					request.setAttribute("user", null);						
					forwardTo(request, response, "/components/userPanel/sign_up_already.vm", getCurrentLayout(loggedUser));	
					return;
				}
				else {
					newUserRegistration(request, response, user);
				}
			}
			catch (FolioException e) {			
				request.setAttribute("user", null);
				request.setAttribute("step", FIRST_STEP);
				request.setAttribute("inError", true);
				request.setAttribute("errorMessage", folioAPI.getFolioErrorMessage(e.getMessage()));
				forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
				return;
			}
		}
		else {
			request.setAttribute("step", currentStep);
			request.setAttribute("inError", true);	
			request.setAttribute("errorMessage", "compile_mandatory");
			forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
			return;
		}
		
	}
	
	protected boolean userAlreadyRegistered(FolioUserModel user) throws FolioException{
		final String accessToken = folioAPI.loginFolioAdministrator();
		FolioResponseModel folioResponse = folioAPI.searchUserByPersonalData(accessToken, user);	
		if (folioResponse != null) {
			return folioResponse.getTotalRecords() > 0;
		}
		return false;
	}
	
	protected void newUserRegistration (final HttpServletRequest request,  final HttpServletResponse response, FolioUserModel user) throws ServletException, IOException {
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		final UUID uuid = UUID.randomUUID();
		user.setId(uuid.toString());		
		request.setAttribute("user", user);
		final String password = generateCommonLangPassword();
		//add to database
		try {
			final String barcode = dao.insertBarcode(user.getId());
			user.setBarcode(barcode);
			user.setUsername(barcode);
		}
		catch (Exception exception) {
			request.getSession().setAttribute("user", null);
			request.setAttribute("user", null);
			request.setAttribute("step", FIRST_STEP);
			request.setAttribute("inError", true);	
			request.setAttribute("errorMessage", "something go wrong");
			forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
			return;
		}
		
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		
		if(isCustomFieldActive) {		
			//setto i flag per i consensi
			HashMap<String, String> customFields = user.getCustomFields();
			//SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd");
			//String now = date.format(new Date());
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_REPRODUCTION_RULES), "SI");
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_DATA_PROCESSING), "SI");
		}
		//login on Folio with administration credentials	
		try {
			final String accessToken = folioAPI.loginFolioAdministrator();
			folioAPI.createFolioUser(accessToken, 
										password, 
										user,
										isCustomFieldActive);
			
			boolean emailResponse = sendPassByEmail(request, user.getEmail(), user.getUsername(), password);
			if (emailResponse) {
				request.setAttribute("message", "register_success");						
				forwardTo(request, response, "/components/userPanel/sign_up_success.vm", getCurrentLayout(loggedUser));	
				return;
			}
			else {					
				request.setAttribute("user", null);
				request.setAttribute("step", FIRST_STEP);
				request.setAttribute("inError", true);
				request.setAttribute("errorMessage", "email_not_sent");
				forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
				return;
			}
		}
		catch (FolioException e) {			
			request.setAttribute("user", null);
			request.setAttribute("step", FIRST_STEP);
			request.setAttribute("inError", true);
			request.setAttribute("errorMessage", folioAPI.getFolioErrorMessage(e.getMessage()));
			forwardTo(request, response, "/components/userPanel/sign_up.vm", getCurrentLayout(loggedUser));
			return;
		}
		
	}
	
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		int currentStep = Integer.parseInt(request.getParameter("current_step"));		
		switch (currentStep) {
			case 1:						
				step1(currentStep, request, response);
				break;				
			case 2:	
				step2(currentStep, request, response);
				break;
			case 3:
				step3(currentStep, request, response);
				break;
			case 4:
				step4(currentStep, request, response);
			default:
				break;
		}		
	}
	
	
	
	private boolean sendPassByEmail(final HttpServletRequest request, final String emailAddress, final String username, final String password) {
		String senderName = getConfiguration(request).getEmailSenderAddress();
		
		ResourceBundle messages = ResourceBundle.getBundle("resources", getVisit(request).getPreferredLocale());		
		ResourceBundle add_messages = ResourceBundle.getBundle("additional_resources", getVisit(request).getPreferredLocale());
		String subject = getText(messages, add_messages, "register_success_email_object");
		String message = username
						+ " "
						+ getText(messages, add_messages, "welcome_message")
						+ " "
						+ password;
		try {
			Message email = new MimeMessage(getMailSession());
		
			email.setFrom(new InternetAddress(senderName));		
			InternetAddress [] to = { new InternetAddress(emailAddress) };
			email.setRecipients(Message.RecipientType.TO, to);
			email.setSubject(subject);
			email.setSentDate(new Date());
			email.setContent(message, "text/html;charset=UTF-8");
			Transport.send(email);
			return true;
		}
		catch (Exception e) {
			Log.error("Errore per la mail " + emailAddress);
			e.printStackTrace();
			return false;
		}		
	}
	
	
	private String getText(ResourceBundle messages, ResourceBundle add_messages, final String key) {
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
	
	
	private String generateCommonLangPassword() {
	    String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
	    String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
	    String numbers = RandomStringUtils.randomNumeric(2);
	    String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
	    String totalChars = RandomStringUtils.randomAlphanumeric(2);
	    String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
	      .concat(numbers)
	      .concat(specialChar)
	      .concat(totalChars);
	    return combinedChars;
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
	
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			final InitialContext cxt = new InitialContext();
			datasource = (DataSource) cxt.lookup("java:/comp/env/jdbc/pg");
			dao = new BncfDao(datasource);
		} catch (Exception ignore) {
			Log.error("", ignore);
		}
	}
	


	
	
	
	
	
	
}

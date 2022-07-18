package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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


import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.io.InputStream;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioUserServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 126757553L;
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	protected static DataSource datasource;
	protected BncfDao dao;
	//private String propertiesFile="country_address_en.properties";
	protected boolean mandatoryCheckFirstStep(final HttpServletRequest request, FolioUserModel user) {
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		if(isCustomFieldActive) {
			HashMap<String, String> customFields = new HashMap<String, String>();
			customFields=user.getCustomFields();
			return isNotNullOrEmptyString(user.getFirstName())
					&& isNotNullOrEmptyString(user.getLastName())
					&& isNotNullOrEmptyString(user.getEmail()) 
					&& isNotNullOrEmptyString(user.getDateOfBirth()) 
					&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_JOB)))
					&& (isNotNullOrEmptyString(user.getMobile()) || isNotNullOrEmptyString(user.getTelephone()))
					&& (!isUnderage(user.getDateOfBirth()) 
							|| isUnderage(user.getDateOfBirth()) 
								&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GUARDIAN)))
								&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT_GUARDIAN)))
								&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT_GUARDIAN)))
								&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY_GUARDIAN_DOCUMENT)))
								&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_GUARDIAN))));
		}
		else {
			IdentityDocumentModel tutor = new IdentityDocumentModel(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID)));
			return isNotNullOrEmptyString(user.getFirstName())
					&& isNotNullOrEmptyString(user.getLastName())
					&& isNotNullOrEmptyString(user.getEmail()) 
					&& isNotNullOrEmptyString(user.getDateOfBirth()) 
					&& isNotNullOrEmptyString(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_JOB_ID)))
					&& (isNotNullOrEmptyString(user.getMobile()) || isNotNullOrEmptyString(user.getTelephone()))
					&& (!isUnderage(user.getDateOfBirth()) 
							|| isUnderage(user.getDateOfBirth()) 
								&& tutor != null
								&& isNotNullOrEmptyString(tutor.getName())
								&& isNotNullOrEmptyString(tutor.getSurname())
								&& isNotNullOrEmptyString(tutor.getType())
								&& isNotNullOrEmptyString(tutor.getDocumentNumber())
								&& isNotNullOrEmptyString(tutor.getCity())
								&& isNotNullOrEmptyString(tutor.getExpirationDate()));
		}
	}
	
	protected boolean mandatoryCheckSecondStep(final HttpServletRequest request, FolioUserModel user) {		
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		if(isCustomFieldActive)
			return isNotNullOrEmptyString(user.getCustomFields().get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NATION)))
					&& isNationAndFiscalCode(user);
		else
			return isNotNullOrEmptyString(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_NATION_ID)))
					&& isNationAndFiscalCodeNotes(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_NATION_ID)), user.getFiscalCode(), user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID)));
	}
	
	protected boolean mandatoryCheckThirdStep(final HttpServletRequest request, FolioUserModel user) {
		return (user.getResidenceAddress() != null 
				&& isNotNullOrEmptyString(user.getResidenceAddress().getCountryId())
				&& isNotNullOrEmptyString(user.getResidenceAddress().getRegion())
				&& isNotNullOrEmptyString(user.getResidenceAddress().getCity())
				&& isNotNullOrEmptyString(user.getResidenceAddress().getPostalCode())
				&& isNotNullOrEmptyString(user.getResidenceAddress().getAddressLine()));	
	}
	
	protected boolean mandatoryCheckForthStep(
			final HttpServletRequest request, 
			FolioUserModel user, 
			FolioUserModel loggedUser) {
		
		final String gRecaptcha = request.getParameter("g-recaptcha-response");	
		final boolean gdpr = "1".equals(request.getParameter("gdpr"));
		final boolean reproduction = "1".equals(request.getParameter("reproduction"));
		
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		boolean dataField=false;
		if(isCustomFieldActive) {
			HashMap<String, String> customFields = new HashMap<String, String>();
			customFields=user.getCustomFields();
			dataField = isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT)))
					&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT)))
					&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY)))
					&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_DOCUMENT)))
					&& gdpr
					&& reproduction;
		}
		else {
			IdentityDocumentModel document = new IdentityDocumentModel(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID)));
			dataField = isNotNullOrEmptyString(document.getType())
					&& isNotNullOrEmptyString(document.getDocumentNumber())
					&& isNotNullOrEmptyString(document.getCity())
					&& isNotNullOrEmptyString(document.getExpirationDate())
					&& gdpr
					&& reproduction;
		}
		
		
		
		if (loggedUser != null && loggedUser.isAdmin()) {
			return dataField;
		}
		else {
			return dataField 
				&&	verifyRecaptcha(gRecaptcha, configuration);
		}

	}	
	
	protected boolean mandatoryConsent(final HttpServletRequest request) {
		final boolean gdpr = "1".equals(request.getParameter("gdpr"));
		final boolean reproduction = "1".equals(request.getParameter("reproduction"));
		return gdpr && reproduction;
	}
	
	protected boolean mandatoryFieldsCheck(
			final HttpServletRequest request,
			FolioUserModel user) {
		
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		
		
		if(isCustomFieldActive) {
			HashMap<String, String> customFields = new HashMap<String, String>();
			customFields=user.getCustomFields();
			
			return isNotNullOrEmptyString(user.getFirstName())
					&& isNotNullOrEmptyString(user.getLastName())
					&& isNotNullOrEmptyString(user.getEmail()) 
					&& isNotNullOrEmptyString(user.getDateOfBirth()) 
					&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_JOB)))
					&& (user.getResidenceAddress() != null 
						&& isNotNullOrEmptyString(user.getResidenceAddress().getRegion())
						&& isNotNullOrEmptyString(user.getResidenceAddress().getCity())
						&& isNotNullOrEmptyString(user.getResidenceAddress().getPostalCode())
						&& isNotNullOrEmptyString(user.getResidenceAddress().getAddressLine()))	
					&& (!isUnderage(user.getDateOfBirth()) 
						|| isUnderage(user.getDateOfBirth()) 
						&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GUARDIAN)))
						&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT_GUARDIAN)))
						&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT_GUARDIAN)))
						&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY_GUARDIAN_DOCUMENT)))
						&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_GUARDIAN))))
					&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NATION)))
					&& isNationAndFiscalCode(user) 
					&& (isNotNullOrEmptyString(user.getMobile()) || isNotNullOrEmptyString(user.getTelephone()))
					&&isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT)))
					&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT)))
					&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY)))
					&& isNotNullOrEmptyString(customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_DOCUMENT)))
					&& mandatoryConsent(request);		
			
		}
		else {	
			IdentityDocumentModel tutor = new IdentityDocumentModel(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID)));
			IdentityDocumentModel document = new IdentityDocumentModel(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID)));
			return isNotNullOrEmptyString(user.getFirstName())
					&& isNotNullOrEmptyString(user.getLastName())
					&& isNotNullOrEmptyString(user.getEmail()) 
					&& isNotNullOrEmptyString(user.getDateOfBirth()) 
					&& isNotNullOrEmptyString(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_JOB_ID)))
					&& (user.getResidenceAddress() != null 
						&& isNotNullOrEmptyString(user.getResidenceAddress().getRegion())
						&& isNotNullOrEmptyString(user.getResidenceAddress().getCity())
						&& isNotNullOrEmptyString(user.getResidenceAddress().getPostalCode())
						&& isNotNullOrEmptyString(user.getResidenceAddress().getAddressLine()))	
					&& (!isUnderage(user.getDateOfBirth()) 
						|| isUnderage(user.getDateOfBirth()) 
							&& tutor != null
							&& isNotNullOrEmptyString(tutor.getName())
							&& isNotNullOrEmptyString(tutor.getSurname())
							&& isNotNullOrEmptyString(tutor.getType())
							&& isNotNullOrEmptyString(tutor.getDocumentNumber())
							&& isNotNullOrEmptyString(tutor.getCity())
							&& isNotNullOrEmptyString(tutor.getExpirationDate()))
					&& isNotNullOrEmptyString(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_NATION_ID)))
					&& isNationAndFiscalCodeNotes(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_NATION_ID)), user.getFiscalCode(), user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID))) 
					&& (isNotNullOrEmptyString(user.getMobile()) || isNotNullOrEmptyString(user.getTelephone()))
					&& isNotNullOrEmptyString(document.getType())
					&& isNotNullOrEmptyString(document.getDocumentNumber())
					&& isNotNullOrEmptyString(document.getCity())
					&& isNotNullOrEmptyString(document.getExpirationDate())
					&& mandatoryConsent(request);	
		}
	}
	
	
	private boolean isUnderage(String dateOfBirth) {
		try {
			SimpleDateFormat date =  new SimpleDateFormat("yyyy");
			int currentYear = Integer.parseInt(date.format(new Date()));
			return currentYear - Integer.parseInt(dateOfBirth.substring(0, 4)) < 18;
		}
		catch (Exception e) {
			return false;
		}		
	}
	
	
	private boolean isNationAndFiscalCodeNotes(final String nation, final String fiscalCode, final String placeOfBirth) {
		if (nation.startsWith("Ital")) {
			return isNotNullOrEmptyString(fiscalCode);
		}
		else {
			return isNotNullOrEmptyString(placeOfBirth);
		}
	}
	
	
	private boolean isNationAndFiscalCode(FolioUserModel user) {
		String nation=user.getCustomFields().get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NATION));
		String birthNation=user.getCustomFields().get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_COUNTRY_OF_BIRTH));
		String birthplace=user.getCustomFields().get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_BIRTHPLACE)) ;
		if (nation.startsWith("Ital")) {
			return isNotNullOrEmptyString(user.getFiscalCode());
		}
		else if (isNotNullOrEmptyString(birthNation))  {
			if(birthNation.startsWith("Ital")) {
				String birthCounty=user.getCustomFields().get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_PROVINCE_OF_BIRTH));
				
				return isNotNullOrEmptyString(birthCounty)&&isNotNullOrEmptyString(birthplace) ;
			}
			else {
				return isNotNullOrEmptyString(birthplace);
				}
			}
		else return false;
	}
	
	private String formatFolioDate(String input) {
		return (isNotNullOrEmptyString(input)) ? input + "T00:00:00.000+0000": null;
	}
	
	protected FolioUserModel createUserFromStep1(HttpServletRequest request) throws UnsupportedEncodingException {
		FolioUserModel user = new FolioUserModel();
	

		user.setFirstName(new String(request.getParameter("name").getBytes(), "UTF-8"));
		user.setLastName(new String(request.getParameter("surname").getBytes(), "UTF-8"));
		user.setEmail(request.getParameter("email"));	
		user.setDateOfBirth(formatFolioDate(request.getParameter("dateOfBirth")));
		user.setTelephone(request.getParameter("phone"));
		user.setMobile(request.getParameter("mobile"));
			
		
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		
		if(isCustomFieldActive) {
			HashMap<String, String> customFields = new HashMap<String, String>();
			
			
			if (isNotNullOrEmptyString(request.getParameter("gender"))) {
				
				String gender=request.getParameter("gender");
				if (gender.equals("M"))
					customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GENDER),"M");
				else if (gender.equals("F"))
					customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GENDER),"F");
				else if (gender.equals("A"))
					customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GENDER),"A");
			}	
			if (isNotNullOrEmptyString(request.getParameter("job"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_JOB), request.getParameter("job"));
			}
					
			String guardianDocumentType;
			try {					
				guardianDocumentType = new String(request.getParameter("tutorDocumentType").getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {			
				guardianDocumentType = request.getParameter("tutorDocumentType");
			}		
			if (isNotNullOrEmptyString(request.getParameter("tutorName")) && isNotNullOrEmptyString(request.getParameter("tutorSurname")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentType")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentNumber")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentProvenance")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentExpire"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GUARDIAN),request.getParameter("tutorName")+ "-"+ request.getParameter("tutorSurname") );
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT_GUARDIAN),guardianDocumentType );
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT_GUARDIAN),request.getParameter("tutorDocumentNumber") );
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_GUARDIAN),request.getParameter("tutorDocumentExpire") );
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY_GUARDIAN_DOCUMENT),request.getParameter("tutorDocumentProvenance") );
			}	
			user.setCustomFields(customFields);
		}else {
			HashMap<String, String> notes = new HashMap<String, String>();
			if (isNotNullOrEmptyString(request.getParameter("gender"))) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_GENDER_ID), request.getParameter("gender"));
			}	
			if (isNotNullOrEmptyString(request.getParameter("job"))) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_JOB_ID), request.getParameter("job"));
			}
					
			String guardianDocumentType;
			try {					
				guardianDocumentType = new String(request.getParameter("tutorDocumentType").getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {			
				guardianDocumentType = request.getParameter("tutorDocumentType");
			}		
			if (isNotNullOrEmptyString(request.getParameter("tutorName")) && isNotNullOrEmptyString(request.getParameter("tutorSurname")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentType")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentNumber")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentProvenance")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentExpire"))) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID), "Nome: " + request.getParameter("tutorName") + "; Cognome: " + request.getParameter("tutorSurname") + "; Tipo documento: " + guardianDocumentType + "; Numero: " + request.getParameter("tutorDocumentNumber") + "; Rilasciato da: " + request.getParameter("tutorDocumentProvenance") + "; Scadenza: " + request.getParameter("tutorDocumentExpire") + ";");
			}	
			user.setCustomFieldFromNotes(notes);
		}		
		return user;
	}
	
	protected void injectUserStep2 (HttpServletRequest request, FolioUserModel user) throws UnsupportedEncodingException {
		user.setFiscalCode(request.getParameter("fiscalCode"));
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		
		if(isCustomFieldActive) {		
			HashMap<String, String> customFields = user.getCustomFields();
			if (isNotNullOrEmptyString(request.getParameter("nation"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NATION), request.getParameter("nation"));
			}
			if (isNotNullOrEmptyString(request.getParameter("birth-nation"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_COUNTRY_OF_BIRTH), request.getParameter("birth-nation"));
			}	
	
			if (isNotNullOrEmptyString(request.getParameter("birthLocationText"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_BIRTHPLACE), new String(request.getParameter("birthLocationText").getBytes(), "UTF-8"));						
			}
			else {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_PROVINCE_OF_BIRTH), new String(request.getParameter("cityLocationSelect").getBytes(), "UTF-8"));
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_BIRTHPLACE), new String(request.getParameter("municipLocationSelect").getBytes(), "UTF-8"));	
			}
			user.setCustomFields(customFields);
		}else {
			HashMap<String, String> notes = user.getNotesFromCustomFields();
			if (isNotNullOrEmptyString(request.getParameter("nation"))) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_NATION_ID), request.getParameter("nation"));
			}
			
			StringBuilder birthLocation = new StringBuilder(); 		
			
			if (isNotNullOrEmptyString(request.getParameter("birth-nation"))) {
				birthLocation.append(new String(request.getParameter("birth-nation").getBytes(), "UTF-8"));
			}	
			if (isNotNullOrEmptyString(request.getParameter("birthLocationText"))) {
				birthLocation.append("; ").append(new String(request.getParameter("birthLocationText").getBytes(), "UTF-8"));							
			}
			else {
				birthLocation.append("; ").append(new String(request.getParameter("cityLocationSelect").getBytes(), "UTF-8")).append("; ").append(new String(request.getParameter("municipLocationSelect").getBytes(), "UTF-8")); 			
			}
					
			notes.put(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID), birthLocation.toString());
			user.setCustomFieldFromNotes(notes);
		}
	}
	
	protected void injectUserStep3 (HttpServletRequest request, FolioUserModel user) throws UnsupportedEncodingException {
		
		List<FolioAddress> addressList = new ArrayList<FolioAddress>();
		FolioAddress addressObject1 = new FolioAddress();
		addressObject1.setCountryId(request.getParameter("countryId"));		
		addressObject1.setAddressLine(new String(request.getParameter("address").getBytes(), "UTF-8"));
		addressObject1.setAddressTypeId(folioConfig.getString(FolioConstants.RESIDENCE_ADDRESS));
		addressObject1.setCity(new String(request.getParameter("city").getBytes(), "UTF-8"));
		addressObject1.setPostalCode(request.getParameter("postalCode"));
		addressObject1.setRegion(new String(request.getParameter("province").getBytes(), "UTF-8"));	
						
		if ("0".equals(request.getParameter("mainAddress"))) {
			addressObject1.setMainAddress(true);
		}
		else {
			addressObject1.setMainAddress(false);
		}
		addressList.add(addressObject1);	
		
		if ( isNotNullOrEmptyString(request.getParameter("countryId2"))
				||isNotNullOrEmptyString(request.getParameter("address2")) 
				|| isNotNullOrEmptyString(request.getParameter("city2"))
				|| isNotNullOrEmptyString(request.getParameter("postalCode2"))
				|| isNotNullOrEmptyString(request.getParameter("province2"))) {
			
			FolioAddress addressObject2 = new FolioAddress();
			addressObject2.setCountryId(request.getParameter("countryId2"));	
			addressObject2.setAddressLine(new String(request.getParameter("address2").getBytes(), "UTF-8"));
			addressObject2.setAddressTypeId(folioConfig.getString(FolioConstants.DOMICILE_ADDRESS));
			addressObject2.setCity(new String(request.getParameter("city2").getBytes(), "UTF-8"));
			addressObject2.setPostalCode(request.getParameter("postalCode2"));
			addressObject2.setRegion(new String(request.getParameter("province2").getBytes(), "UTF-8"));
				
			if ("1".equals(request.getParameter("mainAddress"))) {
				addressObject2.setMainAddress(true);
			}
			else {
				addressObject2.setMainAddress(false);
			}
			
			addressList.add(addressObject2);				
		}				
		
		user.setAddress(addressList);		
	}
	
	protected void injectUserStep4 (HttpServletRequest request, FolioUserModel user) {
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		HashMap<String, String> customFields=null;
		HashMap<String, String> notes=null;
		if(isCustomFieldActive) 
			customFields = user.getCustomFields();
		else 
			notes = user.getNotesFromCustomFields();
		if (isNotNullOrEmptyString(request.getParameter("note"))) {
			HashMap<String, String> generalNotes = user.getNotes();
			generalNotes.put(folioConfig.getString(FolioConstants.NOTE_NOTE_ID), request.getParameter("note"));
			user.setNotes(generalNotes);
		}
		
		
		String resPermissionNumber = request.getParameter("resPermissionNumber");
		String resPermissionExpire = request.getParameter("resPermissionExpire");
		
		if (isNotNullOrEmptyString(resPermissionNumber) || isNotNullOrEmptyString(resPermissionExpire)) {
			if(isCustomFieldActive) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_RESIDENCE_PERMISSION),resPermissionExpire);
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RESIDENCE_PERMISSION_NUMBER),resPermissionNumber);
			}
			else
				notes.put(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID), "Numero permesso di soggiorno: " + resPermissionNumber + "; Data di scadenza del permesso di soggiorno: " + resPermissionExpire);
		}
		
		String documentType;
		try {
			documentType = new String(request.getParameter("documentType").getBytes(), "UTF-8");						
		} catch (UnsupportedEncodingException e) {
			documentType = request.getParameter("documentType");			
		}	
		if(isCustomFieldActive) {
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT),  documentType);
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT),request.getParameter("documentNumber"));
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY), request.getParameter("documentProvenance"));
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_DOCUMENT), request.getParameter("documentExpire"));
			user.setCustomFields(customFields);
		}
		else {
			notes.put(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID), "Tipo documento: " + documentType + "; Numero: " + request.getParameter("documentNumber") + "; Rilasciato da: " + request.getParameter("documentProvenance") + "; Scadenza: " + request.getParameter("documentExpire") + ";");
			user.setCustomFieldFromNotes(notes);
		}
	}
	
	
	protected FolioUserModel createUserFromRequest(HttpServletRequest request) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		
		FolioUserModel user = new FolioUserModel();
			
		user.setFirstName(request.getParameter("name"));
		user.setLastName(request.getParameter("surname"));
		user.setEmail(request.getParameter("email"));	
		user.setDateOfBirth(formatFolioDate(request.getParameter("dateOfBirth")));		
		user.setTelephone(request.getParameter("phone"));
		
		user.setMobile(request.getParameter("mobile"));
		user.setFiscalCode(request.getParameter("fiscalCode"));
		
		List<FolioAddress> addressList = new ArrayList<FolioAddress>();
		FolioAddress addressObject1 = new FolioAddress();
		addressObject1.setCountryId(request.getParameter("countryId"));
		addressObject1.setAddressLine(request.getParameter("address"));
		addressObject1.setAddressTypeId(folioConfig.getString(FolioConstants.RESIDENCE_ADDRESS));
		addressObject1.setCity(request.getParameter("city"));
		addressObject1.setPostalCode(request.getParameter("postalCode"));
		addressObject1.setRegion(request.getParameter("province"));		
		
						
		if ("0".equals(request.getParameter("mainAddress"))) {
			addressObject1.setMainAddress(true);
		}
		else {
			addressObject1.setMainAddress(false);
		}
		addressList.add(addressObject1);
		
		
		if (isNotNullOrEmptyString(request.getParameter("address2")) 
				|| isNotNullOrEmptyString(request.getParameter("city2"))
				|| isNotNullOrEmptyString(request.getParameter("postalCode2"))
				|| isNotNullOrEmptyString(request.getParameter("countryId2"))) {
			
			FolioAddress addressObject2 = new FolioAddress();
			addressObject2.setCountryId(request.getParameter("countryId2"));
			addressObject2.setAddressLine(request.getParameter("address2"));
			addressObject2.setAddressTypeId(folioConfig.getString(FolioConstants.DOMICILE_ADDRESS));
			addressObject2.setCity(request.getParameter("city2"));
			addressObject2.setPostalCode(request.getParameter("postalCode2"));
			addressObject2.setRegion(request.getParameter("province2"));
			
			
			if ("1".equals(request.getParameter("mainAddress"))) {
				addressObject2.setMainAddress(true);
			}
			else {
				addressObject2.setMainAddress(false);
			}
			
			addressList.add(addressObject2);				
		}				
		
		user.setAddress(addressList);	
		
		
		HashMap<String, String> generalNotes = new HashMap<String, String>();
		
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		
		if(isCustomFieldActive) {		
			HashMap<String, String> customFields = new HashMap<String, String>();
			
			if (isNotNullOrEmptyString(request.getParameter("gender"))) {
				String gender=request.getParameter("gender");
				if (gender.equals("M"))
					customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GENDER),"M");
				else if (gender.equals("F"))
					customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GENDER),"F");
				else if (gender.equals("A"))
					customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GENDER),"A");
			}	
			if (isNotNullOrEmptyString(request.getParameter("job"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_JOB), request.getParameter("job"));
			}
	
			if (isNotNullOrEmptyString(request.getParameter("nation"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NATION), request.getParameter("nation"));
			}
			
			
			if (isNotNullOrEmptyString(request.getParameter("birth-nation"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_COUNTRY_OF_BIRTH), request.getParameter("birth-nation"));
			}	
	
			if (isNotNullOrEmptyString(request.getParameter("birthLocationText"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_BIRTHPLACE), request.getParameter("birthLocationText"));
			}
			else {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_PROVINCE_OF_BIRTH), request.getParameter("cityLocationSelect"));
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_BIRTHPLACE), request.getParameter("municipLocationSelect"));
			}
			
			String resPermissionNumber = request.getParameter("resPermissionNumber");
			String resPermissionExpire = request.getParameter("resPermissionExpire");
			if (isNotNullOrEmptyString(resPermissionNumber) || isNotNullOrEmptyString(resPermissionExpire)) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_RESIDENCE_PERMISSION),resPermissionExpire);
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RESIDENCE_PERMISSION_NUMBER),resPermissionNumber);
			}
			
			if (isNotNullOrEmptyString(request.getParameter("note"))) {
				generalNotes.put(folioConfig.getString(FolioConstants.NOTE_NOTE_ID), request.getParameter("note"));
			}
			
			String documentType;
			String guardianDocumentType;
			try {
				documentType = new String(request.getParameter("documentType").getBytes(), "UTF-8");			
				guardianDocumentType = new String(request.getParameter("tutorDocumentType").getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				documentType = request.getParameter("documentType");
				guardianDocumentType = request.getParameter("tutorDocumentType");
			}		
			try {					
				guardianDocumentType = new String(request.getParameter("tutorDocumentType").getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {			
				guardianDocumentType = request.getParameter("tutorDocumentType");
			}		
			if (isNotNullOrEmptyString(request.getParameter("tutorName")) && isNotNullOrEmptyString(request.getParameter("tutorSurname")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentType")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentNumber")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentProvenance")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentExpire"))) {
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GUARDIAN),request.getParameter("tutorName")+ "-"+ request.getParameter("tutorSurname") );
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT_GUARDIAN),guardianDocumentType );
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT_GUARDIAN),request.getParameter("tutorDocumentNumber") );
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_GUARDIAN),request.getParameter("tutorDocumentExpire") );
				customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY_GUARDIAN_DOCUMENT),request.getParameter("tutorDocumentProvenance") );
				}	
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT),  documentType);
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT),request.getParameter("documentNumber"));
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY), request.getParameter("documentProvenance"));
			customFields.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_DOCUMENT), request.getParameter("documentExpire"));
			user.setNotes(generalNotes);
			user.setCustomFields(customFields);
		}else {
			HashMap<String, String> notes = new HashMap<String, String>();
			// birthLocation
			StringBuilder birthLocation = new StringBuilder(); 		
			
			if (isNotNullOrEmptyString(request.getParameter("birth-nation"))) {
				birthLocation.append(request.getParameter("birth-nation"));
			}	
	
			if (isNotNullOrEmptyString(request.getParameter("birthLocationText"))) {
				birthLocation.append("; ").append(request.getParameter("birthLocationText"));							
			}
			else {
				birthLocation.append("; ").append(request.getParameter("cityLocationSelect")).append("; ").append(request.getParameter("municipLocationSelect")); 			
			}
					
			notes.put(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID), birthLocation.toString());
			
			
			if (isNotNullOrEmptyString(request.getParameter("gender"))) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_GENDER_ID), request.getParameter("gender"));
			}			
			if (isNotNullOrEmptyString(request.getParameter("nation"))) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_NATION_ID), request.getParameter("nation"));
			}	
			if (isNotNullOrEmptyString(request.getParameter("job"))) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_JOB_ID), request.getParameter("job"));
			}
			String resPermissionNumber = request.getParameter("resPermissionNumber");
			String resPermissionExpire = request.getParameter("resPermissionExpire");
			
			if (isNotNullOrEmptyString(resPermissionNumber) || isNotNullOrEmptyString(resPermissionExpire)) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID), "Numero permesso di soggiorno: " + resPermissionNumber + "; Data di scadenza del permesso di soggiorno: " + resPermissionExpire);
			}
			if (isNotNullOrEmptyString(request.getParameter("note"))) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_NOTE_ID), request.getParameter("note"));
			}
			
			String documentType;
			String guardianDocumentType;
			try {
				documentType = new String(request.getParameter("documentType").getBytes(), "UTF-8");			
				guardianDocumentType = new String(request.getParameter("tutorDocumentType").getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				documentType = request.getParameter("documentType");
				guardianDocumentType = request.getParameter("tutorDocumentType");
			}		
	
			notes.put(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID), "Tipo documento: " + documentType + "; Numero: " + request.getParameter("documentNumber") + "; Rilasciato da: " + request.getParameter("documentProvenance") + "; Scadenza: " + request.getParameter("documentExpire") + ";");
			if (isNotNullOrEmptyString(request.getParameter("tutorName")) && isNotNullOrEmptyString(request.getParameter("tutorSurname")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentType")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentNumber")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentProvenance")) && isNotNullOrEmptyString(request.getParameter("tutorDocumentExpire"))) {
				notes.put(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID), "Nome: " + request.getParameter("tutorName") + "; Cognome: " + request.getParameter("tutorSurname") + "; Tipo documento: " + guardianDocumentType + "; Numero: " + request.getParameter("tutorDocumentNumber") + "; Rilasciato da: " + request.getParameter("tutorDocumentProvenance") + "; Scadenza: " + request.getParameter("tutorDocumentExpire") + ";");
			}	
			
			user.setNotes(notes);
		}
		return user;
	}
	
	protected void activateUser(final String userId,boolean isCustomFieldActive) {
		try {
			final String accessToken = folioAPI.loginFolioAdministrator();
			FolioUserModel user = folioAPI.searchSimpleFolioUserbyId(accessToken, userId, isCustomFieldActive);
			if (user.getExpireDate() == null || !user.isActive()) {
				String nextExpiration = getNextExpiration();
				user.setExpireDate(nextExpiration);
				user.setActive(true);
				setFolioExpiration(user, accessToken);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void setFolioExpiration(final FolioUserModel user, final String accessToken) throws FolioException {		
		folioAPI.updateExpirationUsere(accessToken, user.getFolioJson(), user);		
	}
	
	private String getNextExpiration() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 1);
		Date nextExpiration = calendar.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String result = formatter.format(nextExpiration).toString();
		return result;
	}
	
	private boolean verifyRecaptcha(final String recaptcha, ConfigurationTool configuration) {
		final String GOOGLE_RECAPTCHA_VERIFY = "https://www.google.com/recaptcha/api/siteverify";
		if (recaptcha == null || "".equals(recaptcha)) {
			return false;
		}		
		try {
			URL obj = new URL(GOOGLE_RECAPTCHA_VERIFY);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
	
			// add request header
			con.setRequestMethod("POST");
	
			String postParams = "secret=" + configuration.getGoogleRecaptchaKey() + "&response="
					+ recaptcha;
	
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();
	
			int responseCode = con.getResponseCode();
	
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	
			//parse JSON response and return 'success' value
			JSONObject jsonObject = new JSONObject (response.toString());
					
			return jsonObject.getBoolean("success");
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public void createUserIfNotExist(final String userId, final String barcode) {
		try {
			boolean userExist = dao.userExist(userId);
			if (!userExist) {
				dao.inserOldUser(userId, barcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	
	protected void setCountryAddress(final HttpServletRequest request, String propertiesFile) {
		Map<String, String> mapCountryAddress =new HashMap<String, String>();
		mapCountryAddress=	getCountryMap(propertiesFile);
		request.setAttribute("mapCountryAddress", mapCountryAddress);
		return;
	}

	
	private Map<String, String> getCountryMap (String propertiesFile) {	
		Map<String, String> map = new HashMap<String, String>();
		Properties properties = new Properties();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(propertiesFile);
		try {
			properties.load(in);	         
			for (String key : properties.stringPropertyNames()) {
				map.put(key, properties.getProperty(key));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sortMap(map);
    }
	
	
	
	private Map<String, String> sortMap (Map<String, String> map){
		List<Entry<String, String>> list = new LinkedList<Entry<String, String>>(map.entrySet());
		Collections.sort(list, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Comparable<String>) ((Map.Entry<String, String>) (o1)).getValue()).compareTo(((Map.Entry<String, String>) (o2)).getValue());
				}
			});
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (Iterator<Entry<String, String>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
		    result.put(entry.getKey(), entry.getValue());
			}		
		return result;
		
	}
	
		

}


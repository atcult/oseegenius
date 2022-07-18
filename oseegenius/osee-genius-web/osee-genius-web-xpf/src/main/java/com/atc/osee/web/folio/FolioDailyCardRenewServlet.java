package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioDailyCardRenewServlet extends FolioUserServlet {
	private static final long serialVersionUID = 1678934453L;
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		StringBuilder builderResponse = null;
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		try {			
			String tomorrow = getTomorrow();
			String aWeekAgo = getAWeekAgo();
			String today = getToday();
			final String query = "(expirationDate<" + tomorrow + " AND expirationDate>" + aWeekAgo + " NOT metadata.updatedDate=" + today + ")"; //") sortby expirationDate/sort.descending";
			final int limit = 500;
			int offset = 0;
			final String accessToken = folioAPI.loginFolioAdministrator();				
			List <FolioUserModel> userList = folioAPI.getUserList(folioAPI.searchFolioUserByQuery(accessToken, query, limit, offset),isCustomFieldActive);			
			while (!userList.isEmpty()) {
				// fix offset because I modify data every time I run the query, so I risk that offset != leaves some data				
				for (FolioUserModel user : userList) {
					setAppropriatePatroGroup(user);					
					String queryRequestByYear = buildRequestInYearQuery(user.getId());					
					FolioResponseModel folioResponse = folioAPI.searchRequestByQuery(accessToken, queryRequestByYear, 0, 0);
					if (folioResponse.getTotalRecords() > 0 || folioConfig.getString(FolioConstants.USER_STAFF).equals(user.getPatronGroupCode())) {
							String nextExpiration = getNextExpiration();
							user.setExpireDate(nextExpiration);
							user.setActive(true);
					}
					setFolioExpiration(user, accessToken);				
				}
				userList = folioAPI.getUserList(folioAPI.searchFolioUserByQuery(accessToken, query, limit, offset),isCustomFieldActive);
			}
			if (builderResponse == null) {
				builderResponse = new StringBuilder("{");
			}
			
		} catch (Exception exception) {
			Log.error(exception.getMessage());
			if (builderResponse == null) {
				builderResponse = new StringBuilder("{");
			}
			
			builderResponse.append("\"error\" : ").append("\"").append(exception.getMessage()).append("\"");
		}
		builderResponse.append("}");
		response.getWriter().println(builderResponse.toString());		
	}
	
	protected String buildRequestInYearQuery(final String userId) {
		String aYearAgo = getYearAgo();
		return "(requesterId==" + userId + " AND requestDate>" + aYearAgo + ")";
	}
	
	protected void setAppropriatePatroGroup(FolioUserModel user) {
		String userPatronCode = user.getPatronGroupCode();
		//TO-DO to remove after massive test		
		if (folioConfig.getString(FolioConstants.USER_ADMIN).equals(userPatronCode)) {
			System.out.println("sono un admin");
		}
		if (! (folioConfig.getString(FolioConstants.USER_ADMIN).equals(userPatronCode) || folioConfig.getString(FolioConstants.USER_STAFF).equals(userPatronCode))) {
			user.setPatronGroupCode(folioConfig.getString(FolioConstants.USER_BASE));
		}
	}
	
	protected void setFolioExpiration(final String userId, boolean isCustomFieldActive) {
		try {
			final String accessToken = folioAPI.loginFolioAdministrator();
			FolioUserModel user = folioAPI.searchSimpleFolioUserbyId(accessToken, userId, isCustomFieldActive);
			if (user.getExpireDate() == null /*|| inactive*/) {
				String nextExpiration = getNextExpiration();
				user.setExpireDate(nextExpiration);
			}
		} catch (Exception e) {
			
		}
	}
	
	private String getYearAgo() {
		Date now = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, -1);
		Date nextExpiration = calendar.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String result = formatter.format(nextExpiration).toString();
		return result;
	}
	
	private String getNextExpiration() {
		Date now = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 1);
		Date nextExpiration = calendar.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String result = formatter.format(nextExpiration).toString();
		return result;
	}
	
	
	public String getTomorrow () {
	    	// Create a calendar object with today date. Calendar is in java.util pakage.
		    Calendar calendar = Calendar.getInstance();
		    // Move calendar to yesterday
		    calendar.add(Calendar.DATE, 1);
		    // Get current date of calendar which point to the yesterday now
		    Date yesterday = calendar.getTime();
			SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd");
			return date.format(yesterday);
	}
	
	public String getAWeekAgo () {
    	// Create a calendar object with today date. Calendar is in java.util pakage.
	    Calendar calendar = Calendar.getInstance();
	    // Move calendar to yesterday
	    calendar.add(Calendar.DATE, -7);
	    // Get current date of calendar which point to the yesterday now
	    Date aWeekAgo = calendar.getTime();
		SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd");
		return date.format(aWeekAgo);
}
	
	public String getToday() {
		SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd");
		return date.format(new Date());
	}
}

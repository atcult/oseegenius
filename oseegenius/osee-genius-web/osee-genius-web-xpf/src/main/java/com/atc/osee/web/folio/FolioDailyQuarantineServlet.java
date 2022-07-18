package com.atc.osee.web.folio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

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
 * daily procedure for quarantine loan
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioDailyQuarantineServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 1246450843L;	
	

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		response.setContentType("application/json");
		StringBuilder builderResponse = new StringBuilder("{");
		final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
		String [] quarantineUsers = folioConfig.getString(FolioConstants.QUARANTINE).split(",");
		try {
			final String accessToken = folioAPI.loginFolioAdministrator();	
			Map<String, String> locationServicePoints = folioAPI.getLocations(accessToken);
			
			for (int i = 0; i < quarantineUsers.length; i ++) {
				String currentUserBarcode = quarantineUsers[i];
				String userId = getIdByBarcode(currentUserBarcode, accessToken,isCustomFieldActive);
				FolioResponseModel openLoanResponse = folioAPI.getUserOpenLoansBefore(accessToken, userId,
						"1000", null, getAWeekAgo());
				
				final List<FolioLoan> openLoans = folioAPI.getLoanList(openLoanResponse);
				for (FolioLoan currentLoan : openLoans) {	
					String servicePointId = calculateServicePoint(locationServicePoints, currentLoan.getLocation());
					folioAPI.closeLoan(accessToken, currentLoan.getItemBarcode(), servicePointId);
				}
			}
		}
		catch (Exception exception) {
			Log.error(exception.getMessage());			
			
			builderResponse.append("\"error\" : ").append("\"").append(exception.getMessage()).append("\"");
		}
		builderResponse.append("}");
		response.getWriter().println(builderResponse.toString());		
		
				
	}
	
	private String getIdByBarcode(final String barcode, final String accessToken, boolean isCustomFieldActive) {
		try {
			List<FolioUserModel> users = folioAPI.getUserList(folioAPI.searchFolioUserByBarcode(accessToken, barcode, "1", null),isCustomFieldActive);
			if (users.size() > 0) {
				return users.get(0).getId();
			}
			else {
				Log.error("ERROR in FolioDailyQuarantine: no user with barcode " + barcode);
				return null;
			}
		} catch (Exception e) {
			Log.error(e.getMessage());
			return null;
		}
	}
	
	private String calculateServicePoint(Map<String, String>  locationServicePoints, final String currentLocation) {
		return locationServicePoints.get(currentLocation);
	}
	
	
	public String getAWeekAgo() {
    	// Create a calendar object with today date. Calendar is in java.util pakage.
	    Calendar calendar = Calendar.getInstance();
	    // Move calendar to a week ago. I need to consider 6 insted of 7, because folio doesn't support <= operator 
	    calendar.add(Calendar.DATE, -6);
	    // Get current date of calendar which point to the yesterday now
	    Date aWeekAgo = calendar.getTime();
		SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd");
		return date.format(aWeekAgo);
	}
}

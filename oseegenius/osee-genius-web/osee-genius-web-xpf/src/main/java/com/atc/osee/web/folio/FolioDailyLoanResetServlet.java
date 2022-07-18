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
 * daily procedure for reset "Consultazione" permanentLoanType
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioDailyLoanResetServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 1256796843L;	

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		
		String statusAvailableQuery = "and status=\"Available\"";
		String statusNotPagedNorAvailableQuery = "not (status=\"Paged\" or status=\"Available\" or status=\"Checked out\")";
		StringBuilder builderResponse = null;
		
		try {
			final String accessToken = folioAPI.loginFolioAdministrator();	
			
			List<JSONObject> listAvailable = folioAPI.getItemWithLoanType(statusAvailableQuery, accessToken);
			for (JSONObject currentItem : listAvailable) {
				folioAPI.resetLoanType(accessToken, currentItem.toString());
			}
			
			List<JSONObject> listNotAvailableNorPagedNorCheckedOut = folioAPI.getItemWithLoanType(statusNotPagedNorAvailableQuery, accessToken);
			for (JSONObject currentItem : listNotAvailableNorPagedNorCheckedOut) {
				FolioResponseModel folioResponse = folioAPI.getNextRequest(accessToken, currentItem.getString("id"));
				JSONObject item = new JSONObject(folioResponse.getJsonResponse());
				JSONArray pendingRequests = item.getJSONArray("requests");
				if (pendingRequests.length() > 0) {
					JSONObject folioRequest = pendingRequests.getJSONObject(0);
					String requestServicePoint = folioRequest.getString("pickupServicePointId");
					changeLoanType(accessToken, currentItem.toString(), requestServicePoint, folioAPI);
				}
				else {
					folioAPI.resetLoanType(accessToken, currentItem.toString());
				}
			}			
			
			if (builderResponse == null) {
				builderResponse = new StringBuilder("{");
			}
		}
		catch (Exception exception) {
			Log.error(exception.getMessage());
			if (builderResponse == null) {
				builderResponse = new StringBuilder("{");
			}
			else {
				builderResponse.append(",");
			}
			builderResponse.append("\"error\" : ").append("\"").append(exception.getMessage()).append("\"");
		}
		builderResponse.append("}");
		response.getWriter().println(builderResponse.toString());				
	}

	
	private void changeLoanType(final String folioToken, final String updatedItem, final String servicePoint, final FolioRestApi folioApi) {
		if (folioApi.isCopyPaged(updatedItem)) {
			folioApi.changeLoanTypeAsServicePoint (folioToken, updatedItem, servicePoint);			
		}
	}	
	
}

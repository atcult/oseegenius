package com.atc.osee.web.folio;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;
import com.atc.osee.web.util.Utils;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioServicePointServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 124634853L;
	
	protected void doGet (final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");	
		final String codeAvail = request.getParameter("codeAvail");
		final String codeNotAvail = request.getParameter("codeNotAvail");
		
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();		
		json.put("servicePoints", array);
		
		try {
			ResourceBundle add_messages = ResourceBundle.getBundle("additional_resources", getVisit(request).getPreferredLocale());
			
			FolioUserModel loggedUser = getVisit(request).getFolioAccount();
			if (loggedUser == null) {
				
			}
			else {
				NameValuePair [] servicePoints = loggedUser.getServicePoints().get(codeAvail);					
				
				for (int i = 0; i < servicePoints.length; i ++) {
					NameValuePair currentServicePoint = servicePoints[i];
					if ("LOAN_ROOM".equals(currentServicePoint.getValue()) && (
							("P".equals(codeNotAvail) || "D".equals(codeNotAvail)) ||
							(Utils.isLoanServiceClose(getConfiguration(request)))
						)) {
						// don't add element
					}
					else {
						JSONObject o = new JSONObject();
						o.put("label", add_messages.getObject(currentServicePoint.getValue()).toString());	
						o.put("value", currentServicePoint.getName());
						array.put(o);
					}				
				}			
				response.getWriter().println(json.toString());
			}
		} catch (Exception exception) {
			Log.error(exception.getMessage());	
			StringBuilder builderResponse = new StringBuilder("{");
			builderResponse.append("\"error\" : ").append("\"").append(exception.getMessage()).append("\"");
			builderResponse.append("}");
			
			response.getWriter().println(builderResponse.toString());
		}		
	}

}

package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioLoanSerialServlet extends FolioLoanServlet {
	private static final long serialVersionUID = 777884453L;
	
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	protected static DataSource datasource;
	protected BncfDao dao;
	
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		final String resourceId = request.getParameter("idResource");
		
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null || !isNotNullOrEmptyString(getVisit(request).getFolioAccount().getId())) {
			response.sendRedirect("signIn?redirectId=" + resourceId);
		}
		else {					
			
			final String servicePoint = request.getParameter("servicePoint");
			final String collocation = request.getParameter("collocation");
			final String title = request.getParameter("title");		
			final String publisher = request.getParameter("publisher");	
			final String year = request.getParameter("year");
			final String month = request.getParameter("month");
			final String volume = request.getParameter("volume");
			final String issue = request.getParameter("issue");	
			final String day = request.getParameter("day");
			final String note = request.getParameter("note");	
			final String edition = request.getParameter("edition");
			final String holding = request.getParameter("holding");
		
			final String servicePointId = servicePoint.split("___")[0];
			final String servicePointLabel = servicePoint.split("___")[1];
			
			try {
				final String accessToken = folioAPI.loginFolioAdministrator();	
					
				
				JSONObject instance = folioAPI.getFirstResult(
							folioAPI.getInstanceByIdentifier(resourceId, accessToken, FolioConstants.BID_TYPE_SBNWEB),
							"instances");
				if (instance == null) {
					request.setAttribute("inError", true);		
					request.setAttribute("errorMessage", "La risorsa non è disponibile in Folio");
					forwardTo(request, response, "/components/userPanel/operation_result.vm", "/homepage.vm");	
					return;
				}
				FolioRecord record = new FolioRecord();
				record.setInstanceId(instance.getString("id"));
				record.setCollocation(collocation);
				record.setTitle(title);
				record.setPublisher(publisher);
				record.setIssue(issue);
				record.setYear(year);
				record.setVolume(volume);
				record.setMonth(month);
				record.setDay(day);
				record.setNote(note);
				record.setBarcode(dao.getBarcode(FolioConstants.SERIAL_OPAC_PREFIX));
				record.setHoldingType(folioConfig.getString(FolioConstants.TYPE_HOLDING_PERIODIC));				
				record.setHoldingSection(getHoldingSection(request.getParameter("section")));
				record.setItemType(getItemType(request.getParameter("section")));
				record.setLoanType(folioConfig.getString(FolioConstants.LOAN_TYPE_CONS_ID));
				record.setEdition(edition);
				record.setHolding(holding);
				
				FolioItemModel item = folioAPI.createItemWithHolding(accessToken, record);
				
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
				
			} catch (SQLException sqlException) {
				request.setAttribute("inError", true);	
				sqlException.printStackTrace();
				request.setAttribute("errorMessage", "Errore nella creazione del barcode");
				forwardTo(request, response, "/components/userPanel/operation_result.vm", "workspace_layout.vm");	
				return;				
			} catch (FolioException e) {
				e.printStackTrace();
				request.setAttribute("inError", true);		
				request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
				forwardTo(request, response, "/components/userPanel/operation_result.vm", "workspace_layout.vm");	
				return;
			}
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

}

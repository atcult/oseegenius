package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

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

public class FolioUserSearchServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 126858553L;
	private static final String DEFAULT_LIMIT = "30";
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	protected static DataSource datasource;
	protected BncfDao dao;

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String query = request.getParameter("userQuery");
		final String message = request.getParameter("message");
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		FolioUserModel account = getVisit(request).getFolioAccount();
		if (account == null) {
			response.sendRedirect("signIn");			
		}
		else {
			if (query != null) {
				final String offset = isNotNullOrEmptyString(request.getParameter("offset"))? request.getParameter("offset") : null;
				final String limit = isNotNullOrEmptyString(request.getParameter("limit"))? request.getParameter("limit") : DEFAULT_LIMIT;
				
				
				if (isNotNullOrEmptyString(query)) {			
					final String accessToken = account.getOkapiToken();		
					
					try {		
						FolioResponseModel searchResponse = folioAPI.searchFolioUser(accessToken, query, limit, offset);
						Map<String, String> patronGroups = folioAPI.getAllPatronGroup(accessToken);
						List<FolioUserModel> userList = folioAPI.getUserList(searchResponse,isCustomFieldActive);
						injectPatronName(userList, patronGroups);
						injectBlockExpireDate(userList);
						request.setAttribute("numFound", folioAPI.getNumFound(searchResponse));					
						request.setAttribute("userList", userList);
						request.setAttribute("defaultLimit", DEFAULT_LIMIT);
						request.setAttribute("folioFrontendUrl", folioConfig.getString(FolioConstants.FOLIO_URL_FRONTEND));
						if (message != null) {
							request.setAttribute("message", "success");
						}
						forwardTo(request, response, "/components/userPanel/user_search.vm", "workspace_layout.vm");				
					}
					catch (FolioException e) {
						request.setAttribute("inError", true);		
						request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
						forwardTo(request, response, "/components/userPanel/user_search.vm", "workspace_layout.vm");
					}
				}
				else {
					request.setAttribute("inError", true);	
					request.setAttribute("errorMessage", "compile mandatory");
					forwardTo(request, response, "/components/userPanel/user_search.vm", "workspace_layout.vm");
				}
			}
			else {
				forwardTo(request, response, "/components/userPanel/user_search.vm", "workspace_layout.vm");
			}
		}
	}
	
	private void injectPatronName(List<FolioUserModel> userList, Map<String, String> patronGroups) {
		for (FolioUserModel currentUser : userList) {
			currentUser.setPatronGroupName(patronGroups.get(currentUser.getPatronGroupCode()));
		}
	}
	
	private void injectBlockExpireDate(List<FolioUserModel> userList) {
		for (FolioUserModel currentUser : userList) {
			try {
				currentUser.setBlockExpireDate(dao.getUserBlockExpireDate(currentUser.getId()));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

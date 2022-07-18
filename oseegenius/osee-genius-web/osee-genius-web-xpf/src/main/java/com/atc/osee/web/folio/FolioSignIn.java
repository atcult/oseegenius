package com.atc.osee.web.folio;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioSignIn extends OseeGeniusServlet {
	private static final long serialVersionUID = 124634853L;
	
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		final String password = request.getParameter("password");
		final String username = request.getParameter("username");
		final String redirectId = request.getParameter("redirectId");
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		if (isNotNullOrEmptyString(username)
				&& isNotNullOrEmptyString(password)) {			
			try {
				final String accessToken = folioAPI.loginFolioAdministrator();
				
				final FolioUserModel folioUser = folioAPI.loginFolioUser(username, password, accessToken,isCustomFieldActive);
				if (folioUser != null) {
					
					/* only admin can retrieve user note */
					HashMap<String, String> userNotes = folioAPI.getFolioNoteByUserId(folioUser.getId(), accessToken);
					folioUser.setNotes(userNotes);
					
					folioUser.setDefaultServicePoint(folioAPI.getUserMainServicePoint(folioUser.getId(), accessToken));
					
					request.setAttribute("user", folioUser);
					request.setAttribute("userGroups", folioAPI.getAllPatronGroup(accessToken));
					getVisit(request).injectFolioAccount(folioUser);
				}
				if (isNotNullOrEmptyString(redirectId)) {
					response.sendRedirect("resource?uri=" + redirectId);
				}
				else {
					response.sendRedirect("folioWorkspace");
				}
			}
			catch (FolioException e) {
				request.setAttribute("specificServletTitle", "reserved_area");
				request.setAttribute("inError", true);		
				request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
				forwardTo(request, response, "/components/userPanel/login.vm");
			}
		}		
	}
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("specificServletTitle", "reserved_area");
		forwardTo(request, response, "/components/userPanel/login.vm");
	}
}

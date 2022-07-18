package com.atc.osee.web.folio;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioUserViewServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 12468565863L;
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		ConfigurationTool configuration = getConfiguration(request);
		
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null) {
			response.sendRedirect("signIn");
			return;
		}
		if (loggedUser != null && loggedUser.isAdmin()) {
			try {				
				final String accessToken = folioAPI.loginFolioAdministrator();
				final Map<String, String> userTypes = folioAPI.getAllUserTypes(accessToken);
				request.setAttribute("userTypes", userTypes);
			}
			catch (FolioException e) {
				
			}			
		}
		
		forwardTo(request, response, "/components/userPanel/folio_account.vm", "workspace_layout.vm");
		
	}
}

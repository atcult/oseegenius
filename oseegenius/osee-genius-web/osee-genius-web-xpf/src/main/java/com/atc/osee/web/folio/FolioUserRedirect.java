package com.atc.osee.web.folio;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

/**
 * redirect to Folio
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioUserRedirect extends OseeGeniusServlet {
	private static final long serialVersionUID = 12467696843L;
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
		
		String barcode = request.getParameter("barcode");
		if (isNotNullOrEmptyString(barcode)) {
						
			try {
				final String accessToken = folioAPI.loginFolioAdministrator();
					
				
				List<FolioUserModel> users = folioAPI.getUserList(folioAPI.searchFolioUserByBarcode(accessToken, barcode, "1", null), isCustomFieldActive);
				if (users.size() > 0) {
					final String userId = users.get(0).getId();
					response.sendRedirect(folioConfig.getString(FolioConstants.FOLIO_URL_FRONTEND) + "/users/view/" + userId + "?layer=edit&sort=name");
				}
				else {
					forwardTo(request, response, "/components/userPanel/user_not_found.vm");
				}
				
			}
			catch (FolioException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}	
		}
		else {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);	
		}
	}
}

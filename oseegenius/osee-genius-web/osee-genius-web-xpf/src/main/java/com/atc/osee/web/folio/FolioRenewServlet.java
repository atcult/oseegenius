package com.atc.osee.web.folio;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.util.Log;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioRenewServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 12457567463L;	
	
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String itemId = request.getParameter("itemId");
		
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		try {	
			final String accessToken = folioAPI.loginFolioAdministrator();
			boolean result = folioAPI.renewLoan(accessToken, itemId, loggedUser.getId());
			if (result) {
				response.sendRedirect("folioHistory");
				return;
			}
			else {
				request.setAttribute("inError", true);		
				request.setAttribute("errorMessage", "something go wrong");
				forwardTo(request, response, "/components/userPanel/folio_history.vm", "workspace_layout.vm");
			}
			
		} catch (FolioException e) {
			if (isJustWarning(e)) {
				response.sendRedirect("folioHistory");
				return;
			}
			request.setAttribute("inError", true);		
			request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()).replaceAll(" ", "_").toLowerCase());
			forwardTo(request, response, "/components/userPanel/folio_history.vm", "workspace_layout.vm");
		}
	}
	
	/**
	 * when message is "renewal would not change the due date" it's just a warning, not a real error
	 * @param e, folio Excpetion
	 * @return
	 */
	private boolean isJustWarning(FolioException e) {
		return "renewal would not change the due date".equals(folioAPI.getFolioErrorCode(e.getMessage()).toLowerCase());
	}
}

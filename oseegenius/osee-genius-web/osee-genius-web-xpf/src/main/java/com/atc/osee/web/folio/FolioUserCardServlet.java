package com.atc.osee.web.folio;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioUserCardServlet extends FolioUserServlet {
	private static final long serialVersionUID = 166675453L;
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String userName = request.getParameter("name");
		final String userSurname = request.getParameter("surname");
		final String cfu = request.getParameter("cfu");
		final String userId = request.getParameter("userId");
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		activateUser(userId,isCustomFieldActive);
		
		response.sendRedirect("printCard?name=" + userName + "&surname=" + userSurname + "&cfu=" + cfu);
		
	}
	
}

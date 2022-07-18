package com.atc.osee.web.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.model.Account;
import com.atc.osee.web.servlets.OseeGeniusServlet;

public class RestLogoutServlet extends OseeGeniusServlet {
	
	private static final long serialVersionUID = 8467307730533904009L;

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		final String authToken = request.getParameter("token");
		
		if (authToken == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		Map<String, Account> tokens = (Map<String, Account>) getServletContext().getAttribute("auth-tokens");
		if (tokens == null) {
			tokens = new HashMap<String, Account>();
		}
		
		tokens.remove(authToken);
	}		
}

package com.atc.osee.web.rest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestLoansServlet extends OseeGeniusServlet {
	
	private static final long serialVersionUID = 8467307730533904009L;

	private final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		final String authToken = request.getParameter("token");
	
		if (authToken == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		Map<String, Account> tokens = getAuthTokens(request);
		
		final Account account = tokens.get(authToken);
		if (account == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		try {
			mapper.writeValue(response.getWriter(), getLicense(request).getCirculationPlugin().findLoans(account.getId()));
		} catch (SystemInternalFailureException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}		
}

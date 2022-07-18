package com.atc.osee.web.rest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.servlets.circulation.RenewalNotAllowedException;

public class RestRenewServlet extends OseeGeniusServlet {
	
	private static final long serialVersionUID = 8467307730533904009L;

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		final String authToken = request.getParameter("token");
		final String barcode = request.getParameter("barcode");
		final String branchId_s = request.getParameter("branchId");
	
		if (authToken == null || barcode == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		int b = 0;
		try {
			b = Integer.parseInt(branchId_s);
		} catch (Exception e) {
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
			getLicense(request).getCirculationPlugin().renew(barcode, b);
		} catch (SystemInternalFailureException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (RenewalNotAllowedException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}		
}

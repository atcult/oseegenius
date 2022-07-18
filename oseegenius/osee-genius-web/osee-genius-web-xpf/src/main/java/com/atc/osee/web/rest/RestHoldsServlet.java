package com.atc.osee.web.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.Hold;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestHoldsServlet extends OseeGeniusServlet {
	
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
			mapper.writeValue(response.getWriter(), getLicense(request).getCirculationPlugin().findHolds(account.getId()));
		} catch (SystemInternalFailureException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}		
	
	public static void main(String[] args) throws Exception {
		Map<Library, List<Hold>> holdsByLibrary = new HashMap<Library, List<Hold>>();
	
		Library branch = new Library(2, "ROVERETO - BIBLIOTECA COMUNALE", "ROV_COM");
		
		List<Hold> holds = new ArrayList<Hold>();
		holds.add(new Hold(1213, "Nome Biblioteca dove deve essere ritirata la copia", 1, new Date(), "Questo è il titolo", "Paul Yopin", new Date(), new Date(System.currentTimeMillis() + 1012923)));
		holds.add(new Hold(223, "Nome Biblioteca dove deve essere ritirata la copia", 1, new Date(), "Pippo pluto e paperino", "Yestman, John", new Date(), new Date(System.currentTimeMillis() + 1012923)));
		
		holdsByLibrary.put(branch, holds);

		Library branch1 = new Library(3, "TRENTO - BIBLIOTECA COMUNALE", "TN_COM");
		
		List<Hold> holds1 = new ArrayList<Hold>();
		holds1.add(new Hold(2312, "Nome Biblioteca dove deve essere ritirata la copia", 1, new Date(), "Questo è il titolo 2", "Paul Pauline", new Date(), new Date(System.currentTimeMillis() + 1012923)));
		
		holdsByLibrary.put(branch1, holds1);
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(System.out, holdsByLibrary);
		
		
	}
	
}

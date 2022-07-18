package com.atc.osee.web.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestLibraryServlet extends OseeGeniusServlet {
	
	private static final long serialVersionUID = 8467307730533904009L;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String p = request.getParameter("branchId");
		int id = 0;
		try {
			id = Integer.parseInt(p);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		try {
			mapper.writeValue(response.getWriter(), getLicense(request).getLibraryPlugin().getBranchDetails(id));
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}		
}
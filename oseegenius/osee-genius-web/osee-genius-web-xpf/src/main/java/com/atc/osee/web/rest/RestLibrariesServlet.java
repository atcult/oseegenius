package com.atc.osee.web.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.IConstants;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestLibrariesServlet extends OseeGeniusServlet {
	
	private static final long serialVersionUID = 8467307730533904009L;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		final List<Library> mainLibraries = (List<Library>) getServletContext().getAttribute(IConstants.MAIN_LIBRARIES);
		mapper.writeValue(response.getWriter(), mainLibraries);
	}		
}
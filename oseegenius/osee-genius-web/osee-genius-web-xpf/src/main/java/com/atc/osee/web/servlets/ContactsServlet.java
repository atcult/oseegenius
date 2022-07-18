package com.atc.osee.web.servlets;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Osee Genius -W- contact us link
 * 
 * @author Maura Braddi
 * @since 1.0
 */
public class ContactsServlet extends OseeGeniusServlet {
	
	private static final long serialVersionUID = 9137191908374321026L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
	{
		forwardTo(req, res, "/contacts.vm", "/one_column.vm");
	}

}

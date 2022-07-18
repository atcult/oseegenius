package com.atc.osee.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Web controller that shows login page.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ShowLoginPageServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = -6145621619215254944L;
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
	{
		forwardTo(req, res, "/login.vm", "/login_layout.vm");
	}
}
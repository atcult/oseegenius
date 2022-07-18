package com.atc.osee.web.rest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.plugin.NoSuchAccountException;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestLoginServlet extends OseeGeniusServlet {
	
	private static final long serialVersionUID = 8467307730533904009L;

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		final String username = request.getParameter("u");
		final String password = request.getParameter("p");
		
		if (username == null || password == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			conn = getDatasource().getConnection();
			st = conn.prepareStatement("SELECT * FROM LIBRIVISION.LV_USER WHERE LOGIN=? AND PASSWORD=?");
			st.setString(1, username);
			st.setString(2, password);
			rs = st.executeQuery();
			if (!rs.next()) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		} catch (final Exception exception) {
			exception.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				st.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				conn.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		try {
			Account account = getLicense(request).getAccountPlugin().getAccount(username);
			String token = String.valueOf(UUID.randomUUID());
			
			Map<String, Account> tokens = (Map<String, Account>) getServletContext().getAttribute("auth-tokens");
			if (tokens == null) {
				tokens = new HashMap<String, Account>();
			}
			tokens.put(token, account);
			account.setAuthToken(token);
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(response.getWriter(), account);			
		} catch (SystemInternalFailureException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (NoSuchAccountException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}		
	
	protected DataSource getDatasource() throws NamingException {
		InitialContext ctx = new InitialContext();
		return (DataSource) ctx.lookup("java:comp/env/jdbc/amicus");
	}
}

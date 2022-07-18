package com.atc.osee.web.external.services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Servlet print card with aplus service.
 * 
 * @author Natascia Bianchini
 * @since 1.2
 *
 */
public class PrintCardServlet extends OseeGeniusServlet {

	private static final long serialVersionUID = -5075910988383041929L;

	enum AWErrorMessage {
		ERR_NME { @Override public String toString(){ return "aw_name_surname_wrong";}},
		ERR_CFU_MISSING { @Override public String toString(){ return "aw_cfu_missing";}},
		ERR_GENERIC { @Override public String toString(){ return "aw_generic_error";}},
		ERR_CONN { @Override public String toString(){ return "aw_connection_error";}},
		ERR_OP  { @Override public String toString(){ return "aw_username_or_pwd_wrong";}};
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		final String userName = request.getParameter("name");
		final String userSurname = request.getParameter("surname");
		final String cfu = request.getParameter("cfu");
		
		try {
			ConfigurationTool configuration = getConfiguration(request);
			final String wsEndPoint = configuration.getAplusServiceUrl();
			final String printCardPage = configuration.getAplusPrintCardPage();
			
			SoapClientAplus client = new SoapClientAplus();
			long result =  client.handleUserByCFU(request, wsEndPoint, userName, userSurname, cfu);
			if (result < 0) {				
				Log.error("Error during insert a new user in APROWEB: code %d - %s.", result, getErrorCode(result));
				request.setAttribute("inError", true);		
				request.setAttribute("errorMessage", getErrorCode(result));
				forwardTo(request, response, "/components/userPanel/operation_result.vm", "workspace_layout.vm");	
				return;
			}
			
			response.sendRedirect(printCardPage + cfu);
			
		} catch (Exception e) {
			Log.error("Error during call APLUS soap weservice.", e);
			request.setAttribute("inError", true);		
			request.setAttribute("errorMessage", "something go wrong");
			forwardTo(request, response, "/components/userPanel/operation_result.vm", "workspace_layout.vm");
			return;
		}
		
	}
	
	private String getErrorCode(final long result) {
		if (result == -1)
			return AWErrorMessage.ERR_NME.toString();
		else if (result == -2)
			return AWErrorMessage.ERR_CFU_MISSING.toString();
		else if (result == -3)
			return AWErrorMessage.ERR_GENERIC.toString();
		else if (result == -4)
			return AWErrorMessage.ERR_CONN.toString();
		else if (result == -5)
			return AWErrorMessage.ERR_OP.toString();
		
		return "";
	}
	

}

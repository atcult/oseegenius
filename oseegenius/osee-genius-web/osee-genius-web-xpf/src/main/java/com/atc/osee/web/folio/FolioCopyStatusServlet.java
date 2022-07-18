package com.atc.osee.web.folio;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * loan in FOLIO
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioCopyStatusServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 12467696843L;
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		String barcode = request.getParameter("barcode");
		
		JSONObject jsonResult = new JSONObject();
		try {
			final String accessToken = folioAPI.loginFolioAdministrator();
			FolioItemModel item = folioAPI.getCopyBybarcode(barcode, accessToken);			
			if (item != null) {			
				addLoanDate(item, accessToken);				
				jsonResult.put("item", item.getFolioJson());				
			}
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			
			response.getWriter().write(jsonResult.toString());
			
		}
		catch (FolioException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}			
	}
	
	private void addLoanDate(FolioItemModel item, String accessToken) throws FolioException {
		String loanDate = folioAPI.getLoanDate(folioAPI.getLastLoan(accessToken, item.getId(), "1", null).getJsonResponse());
		if (loanDate != null) {			
			JSONObject json = item.getFolioJson();
			json.put("dueDate", loanDate);
			item.setFolioJson(json);
		}	
		return;
	}
}

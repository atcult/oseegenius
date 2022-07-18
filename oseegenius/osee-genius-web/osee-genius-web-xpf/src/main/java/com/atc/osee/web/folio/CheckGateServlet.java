package com.atc.osee.web.folio;

/**
 * Servlet used from aplus gate to check if user can go-in/go-out.
 * 
 *  @author Natascia Bianchini
 * 
 */

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

public class CheckGateServlet extends OseeGeniusServlet {

	private static final long serialVersionUID = -9046742032253359109L;
	
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		final String barcode = request.getParameter("barcode");		
		final String inout = getServletConfig().getInitParameter("inout");		
		final Locale locale = (getVisit(request) != null) ?getVisit(request).getPreferredLocale() :Locale.ITALIAN;
		
		response.setContentType("application/json");	
		
		try {			
			if (isNotNullOrEmptyString(barcode)) {
				String outcome = "ok";
												
				final String accessToken = folioAPI.loginFolioAdministrator();
				final FolioUserModel user = folioAPI
					.getUserList(folioAPI
					.searchFolioUserByBarcode( accessToken, barcode, "1", null), isCustomFieldActive)
					.get(0);
				
				String userId = user.getId();
				boolean isActive = user.isActive();
				if (inout.equalsIgnoreCase("1")) {
					if (isActive) {
						response.setStatus(HttpServletResponse.SC_OK);						
					} else {
						response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
						outcome = "ko";
					}					
					response.getWriter().println(getJsonResponse(inout, 0, locale, outcome));
				}
				
				if (inout.equalsIgnoreCase("2")) {			
					FolioResponseModel loanResponse = folioAPI.getUserOpenLoans(accessToken, userId);
					final List<FolioLoan> loans = folioAPI.getLoanList(loanResponse);
					boolean found = false;
					
					Date today = new Date();
					
					int typeLoan = 0;
					for (FolioLoan loan : loans) {		

						String dueDate = loan.getDueDate().substring(0, 10);  				    	
				    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						Date dueDateDt = formatter.parse(dueDate.substring(0, 10));						
						
						FolioItemModel item = folioAPI.getCopyById(loan.getItemId(), accessToken);
						String permanentLoanType = item.getPermanentLoanTypeContent();						
						if (permanentLoanType.equals(folioConfig.getString(FolioConstants.LOAN_TYPE_LOAN_DESCR)) && today.compareTo(dueDateDt) > 0) { 
							typeLoan = 1;
							found = true;
				    		break;
				    	}
						
						if (permanentLoanType.equals(folioConfig.getString(FolioConstants.LOAN_TYPE_CONS_DESCR)) 
								&& (item.getTemporaryLocationContent() == null || item.getTemporaryLocationContent().equals(null))) {
							typeLoan = 2;
							found = true;
				    		break;							
						}
					}
					
					if (!found) {
						response.setStatus(HttpServletResponse.SC_OK);						
					} else {
						response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
						outcome = "ko";
					}					
					response.getWriter().println(getJsonResponse(inout, typeLoan, locale, outcome));
				}
			} else 
			{
				Log.error("Called chkvarco:: barcode missing.");
				JSONObject error = new JSONObject();
				error.put("error", "Barcode missing.");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println(error.toString());	
			}
		} catch (Exception exception) {
			Log.error(exception.getMessage());
			JSONObject error = new JSONObject();
			error.put("error", "An error occurred during Folio verification");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(error.toString());
		}	
	}

	/**
	 * Make json response for the gate.
	 * 
	 * @param inout
	 * @param typeLoan
	 * @param locale
	 * @param outcome
	 * @return
	 */
	private String getJsonResponse(final String inout, final int typeLoan, final Locale locale, final String outcome) {		
		ResourceBundle bundle = ResourceBundle.getBundle("additional_resources", locale);		
		String message = "";
		
		if (outcome.equals("ko")) {
			message = (inout.equalsIgnoreCase("1")) 
				?bundle.getString("error_message_user_cannot_enter_gate") 
						:bundle.getString("error_message_user_cannot_exit_gate_"+typeLoan);
		} else {
			message = (inout.equalsIgnoreCase("1")) 
					?bundle.getString("message_user_authorized_enter_gate") 
							:bundle.getString("message_user_authorized_exit_gate");
		}
		
		JSONObject json = new JSONObject();
		json.put("open", outcome.equals("ko") ?false :true);		
		json.put("answer", message);
		
		return json.toString();
	}

}

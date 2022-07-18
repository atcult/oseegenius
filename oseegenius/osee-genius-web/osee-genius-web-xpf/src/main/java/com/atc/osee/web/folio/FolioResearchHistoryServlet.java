package com.atc.osee.web.folio;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.naming.InitialContext;

import org.jfree.util.Log;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

/**
 * Show the research history of User
 * 
 * @author
 * @since 
 */

public class FolioResearchHistoryServlet extends OseeGeniusServlet {
	//private static final long serialVersionUID = 124685457463L;
	private static final String DEFAULT_LIMIT = "10";
	protected static DataSource datasource;
	protected BncfDao dao;
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null) {
			response.sendRedirect("signIn");
			return;
		} else {

			try {
				
				final String researchHistoryOffset = isNotNullOrEmptyString(request.getParameter("researchHistoryOffset"))
						? request.getParameter("researchHistoryOffset")
						: "0";
				final String researchHistoryLimit = isNotNullOrEmptyString(request.getParameter("researchHistoryLimit"))
						? request.getParameter("researchHistoryLimit")
						: DEFAULT_LIMIT;
	
				String section= request.getParameter("section");
				if (section==null)
					section="simple";

				request.setAttribute("researchHistoryOffset", researchHistoryOffset);
				request.setAttribute("researchHistoryLimit", researchHistoryLimit);
				request.setAttribute("researchHistoryNumFound", dao.howManyResults(loggedUser.getId(), section));
				List <BncfElemtResearchHistory> result=dao.ResearchHistory(loggedUser.getId(), section,researchHistoryOffset,researchHistoryLimit);
				request.setAttribute("result", result);
				request.setAttribute("section", section);
//			} catch (FolioException e) {
			}catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("inError", true);
				request.setAttribute("errorMessage", "something go wrong");
				forwardTo(request, response, "/components/userPanel/folio_history.vm", "workspace_layout.vm");
				return;
			}
		}

		forwardTo(request, response, "/components/userPanel/folio_research_history.vm", "workspace_layout.vm");
	}

	
	

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final String id = request.getParameter("idSearch");
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null) {
			response.sendRedirect("signIn");
			return;
		} else 
		try {
			dao.deleteElementResearchHistory(Integer.parseInt(id));
			
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("inError", true);
			request.setAttribute("errorMessage", "something go wrong");
			forwardTo(request, response, "/components/userPanel/folio_research_history.vm", "workspace_layout.vm");
		}
	}
	
	

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			final InitialContext cxt = new InitialContext();
			datasource = (DataSource) cxt.lookup("java:/comp/env/jdbc/pg");
			dao = new BncfDao(datasource);
		} catch (Exception ignore) {
			Log.error("", ignore);
		}
	}
	
	
	
}

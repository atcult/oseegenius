package com.atc.osee.web.folio;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;

import com.atc.osee.web.log.Log;

public class FolioMunicipalityServlet extends FolioUserServlet {

	private static final long serialVersionUID = -6720266766035133644L;
	
	protected static DataSource datasource;
	protected BncfDao dao;
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		String cityId = request.getParameter("cityId");
		response.setContentType("application/json");		
		
		try {
			
			List<String> municipalities = dao.getMunicipalityByCityId(cityId);
			JSONObject json = new JSONObject();
			if (!municipalities.isEmpty()) {				
				JSONArray array = new JSONArray();
				for (String entry : municipalities) {				
					JSONObject o = new JSONObject();
					o.put("label", entry);					
					array.put(o);
				}
				json.put("municipalities", array);
			}
			
			response.getWriter().println(json.toString());
			
		} catch (Exception exception) {
			Log.error(exception.getMessage());	
			StringBuilder builderResponse = new StringBuilder("{");
			builderResponse.append("\"error\" : ").append("\"").append(exception.getMessage()).append("\"");
			builderResponse.append("}");
			
			response.getWriter().println(builderResponse.toString());
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

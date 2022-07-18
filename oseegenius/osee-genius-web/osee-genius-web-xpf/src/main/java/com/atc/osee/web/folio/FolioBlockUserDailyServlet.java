package com.atc.osee.web.folio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioBlockUserDailyServlet extends FolioUserServlet {
	private static final long serialVersionUID = 132578253L;

	protected static DataSource datasource;
	protected BncfDao dao;
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		
		int count = 0;
		StringBuilder errorIds = null;
		
		StringBuilder builderResponse = null;
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");			
			Date now = formatter.parse(formatter.format(new Date()));
			Timestamp today = new Timestamp(now.getTime());
			List<FolioUserModel> list = dao.getUserToUnblock(today);
			final String accessToken = folioAPI.loginFolioAdministrator();	
			if (builderResponse == null) {
				builderResponse = new StringBuilder("{");
			}
			builderResponse.append("\"num_found\" :").append(list.size()).append(",");
			for(FolioUserModel user : list) {
				String newPatrnoGroupCode = user.getPatronGroupCode();
				FolioUserModel folioUser = folioAPI.searchSimpleFolioUserbyId(accessToken, user.getId(),isCustomFieldActive);
				if (folioAPI.changeUserGroup(accessToken, folioUser, newPatrnoGroupCode)) {
					dao.unblockUserLoan(folioUser.getId());
					count ++;
				}
				else {
					Log.error("FolioBlockUserDailyServlet: Errore sullo sblocco dello user id " + folioUser.getId() );	
					if (errorIds == null) {
						errorIds = new StringBuilder();
					}
					errorIds.append(user.getId()).append(",");
				}
			}
			builderResponse.append("\"user_unblocked\" :").append(count);
			if (errorIds != null) {
				builderResponse.append(",").append("\"user_failed\" : ").append(errorIds.toString());
			}
			
		} 
		catch (Exception exception) {
			Log.error(exception.getMessage());
			if (builderResponse == null) {
				builderResponse = new StringBuilder("{");
			}
			else {
				builderResponse.append(",");
			}
			builderResponse.append("\"error\" : ").append("\"").append(exception.getMessage()).append("\"");
		}
		builderResponse.append("}");
		response.getWriter().println(builderResponse.toString());
			
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

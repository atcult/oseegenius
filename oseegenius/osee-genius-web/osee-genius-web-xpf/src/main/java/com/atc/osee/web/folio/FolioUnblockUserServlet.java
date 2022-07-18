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

public class FolioUnblockUserServlet extends FolioUserServlet {
	private static final long serialVersionUID = 132578253L;

	protected static DataSource datasource;
	protected BncfDao dao;
	
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		if (loggedUser == null) {
			response.sendRedirect("signIn");
			return;
		}
		else {
			// current implementation is only for admin
			if (loggedUser.isAdmin()) {
								
				final String userId = request.getParameter("userId");				
				try {									
					final String accessToken = loggedUser.getOkapiToken();				
					final String adminToken = folioAPI.loginFolioAdministrator();	
					final FolioUserModel userToUnblock= folioAPI.searchSimpleFolioUserbyId(accessToken, userId,isCustomFieldActive);
					final String newUserGroupCode = dao.getUserCategoryToReset(userId);		
					//update user with right patronGroup					
					userToUnblock.setPatronGroupCode(newUserGroupCode);
					userToUnblock.setPatronGroupName(folioAPI.getPatronGroupName(newUserGroupCode, adminToken));
					if (newUserGroupCode != null) {
						if (folioAPI.changeUserGroup(accessToken, userToUnblock, newUserGroupCode)) {
							
							//add to database
							try {
								dao.unblockUserLoan(userToUnblock.getId());								
							}
							catch (Exception exception) {
								Log.error(exception.getMessage());
							}
							
							request.setAttribute("message", "success");								
							request.setAttribute("isLoanEnabled", false);							
							request.setAttribute("user", userToUnblock);
							request.setAttribute("userId", userToUnblock.getId());
							forwardTo(request, response, "/components/userPanel/block_form.vm", "workspace_layout.vm");
						}
						else {
							request.setAttribute("inError", true);		
							request.setAttribute("errorMessage", "error");
							forwardTo(request, response, "/components/userPanel/block_form.vm", "workspace_layout.vm");			
						}						
					}
					else {
						request.setAttribute("inError", true);		
						request.setAttribute("errorMessage", "already_blocked");
						forwardTo(request, response, "/components/userPanel/block_form.vm", "workspace_layout.vm");						
					}	
				}
				catch (Exception e) {
					request.setAttribute("inError", true);		
					request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
					forwardTo(request, response, "/components/userPanel/block_form.vm", "workspace_layout.vm");
				}
			}
		}		
	}
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null) {
			response.sendRedirect("signIn");
			return;
		}
		else {
			// current implementation is only for admin
			if (loggedUser.isAdmin()) {
				try {
					final String accessToken = folioAPI.loginFolioAdministrator();
					final String userId = request.getParameter("userId");
					final FolioUserModel userJson = folioAPI.searchSimpleFolioUserbyId(accessToken, userId,isCustomFieldActive);
					userJson.setPatronGroupName(folioAPI.getAllPatronGroup(accessToken).get(userJson.getPatronGroupCode()));
					final String newUserGroupCode = folioAPI.getOrderedUserCategory().get(userJson.getPatronGroupCode());
					if (newUserGroupCode != null) {
						request.setAttribute("isLoanEnabled", true);
					}
					else {
						request.setAttribute("isLoanEnabled", false);
					}
					request.setAttribute("user", userJson);
					request.setAttribute("userId", userId);
					forwardTo(request, response, "/components/userPanel/block_form.vm", "workspace_layout.vm");
					
				}
				catch (Exception e) {
					request.setAttribute("inError", true);		
					request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
					forwardTo(request, response, "/components/userPanel/personal_area.vm", "workspace_layout.vm");
				}
			}
			else {				
				forwardTo(request, response, "/components/userPanel/personal_area.vm", "workspace_layout.vm");				
			}
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

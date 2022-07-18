package com.atc.osee.web.folio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class UpdateFolioUser extends FolioUserServlet {
	private static final long serialVersionUID = 132578253L;
	
	protected static DataSource datasource;
	protected BncfDao dao;
	protected Map<String, String> cityList;

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null) {
			response.sendRedirect("signIn");
		}
		else {
			final String userId = request.getParameter("userId");			
			FolioUserModel userToUpdate;				
			request.setAttribute("cityList", cityList);		
			try {							
					final String accessToken = folioAPI.loginFolioAdministrator();
					
					if (loggedUser.isAdmin() && isNotNullOrEmptyString(userId)) {
						
						// TO-DO with new steps
						userToUpdate = createUserFromRequest(request);
						userToUpdate.setId(userId);
						
						if (!mandatoryFieldsCheck(request, userToUpdate)) {
							request.setAttribute("user", userToUpdate);	
							request.setAttribute("inError", true);	
							request.setAttribute("errorMessage", "compile_mandatory");
							forwardTo(request, response, "/components/userPanel/edit_user.vm", "workspace_layout.vm");
							return;
						}				
					}
					else {
						userToUpdate = loggedUser;
						if (isNotNullOrEmptyString(request.getParameter("email"))) {
							userToUpdate.setEmail(request.getParameter("email"));													
						}			
						else {
							forwardTo(request, response, "/components/userPanel/folio_account.vm", "workspace_layout.vm");
							return;
						}
					}
					request.setAttribute("user", userToUpdate);				
					final String oldUserJson = folioAPI.searchCompleteFolioUserbyId(accessToken, userToUpdate.getId(),isCustomFieldActive).getFolioJson();
					boolean res = folioAPI.updateFolioUser(accessToken, oldUserJson, userToUpdate,isCustomFieldActive);
					if (res) {
						FolioUserModel updatedUserResponse = folioAPI.searchCompleteFolioUserbyId(accessToken, userToUpdate.getId(),isCustomFieldActive);
						
						//retrive permission
						updatedUserResponse.setPermission(folioAPI.searchPermissionByUserId(accessToken, userToUpdate.getId()));
						
					
						
						// update session of logged user with updated data
						if (userToUpdate.getId() == getVisit(request).getFolioAccount().getId()) {
							getVisit(request).injectFolioAccount(updatedUserResponse);
							forwardTo(request, response, "/components/userPanel/folio_account.vm", "workspace_layout.vm");
							return;
						}	
						else {		
							response.sendRedirect("searchFolio?userQuery=" + updatedUserResponse.getBarcode() + "&message=success");
							return;
						}						
					}
					else {
						request.setAttribute("inError", true);	
						request.setAttribute("errorMessage", "something go wrong");
						forwardTo(request, response, "/components/userPanel/edit_user.vm", "workspace_layout.vm");
						return;
					}							
			}
			catch (FolioException e) {
					request.setAttribute("inError", true);		
					request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
					forwardTo(request, response, "/components/userPanel/edit_user.vm", "workspace_layout.vm");
			}
		}
	}
	

	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null) {
			response.sendRedirect("signIn");
			return;
		}
		else {			
			request.setAttribute("cityList", cityList);	
			String language=request.getParameter("l");
			if (language==null)
				language= getVisit(request).getPreferredLocale().toString() ;
			String propertiesFile="country_address_" + language +".properties";
			setCountryAddress(request, propertiesFile);
			// current implementation is only for admin
			if (loggedUser.isAdmin()) {
				try {
					final String accessToken = folioAPI.loginFolioAdministrator();
					final String userId = request.getParameter("userId");
					final FolioUserModel userJson = folioAPI.searchCompleteFolioUserbyId(accessToken, userId,isCustomFieldActive);
					request.setAttribute("user", userJson);
					request.setAttribute("userId", userId);

					forwardTo(request, response, "/components/userPanel/edit_user.vm", "workspace_layout.vm");
				}
				catch (Exception e) {
					request.setAttribute("inError", true);		
					request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
					forwardTo(request, response, "/components/userPanel/edit_user.vm", "workspace_layout.vm");
				}
			}
			else {				
				forwardTo(request, response, "/components/userPanel/edit_user.vm", "workspace_layout.vm");				
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
			cityList = dao.getCityList();
		} catch (Exception ignore) {
			Log.error("", ignore);
		}
	}
	
	
}

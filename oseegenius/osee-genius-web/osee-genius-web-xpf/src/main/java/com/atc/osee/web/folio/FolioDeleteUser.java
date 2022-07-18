package com.atc.osee.web.folio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * delete FOLIO user 
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioDeleteUser extends OseeGeniusServlet {
	private static final long serialVersionUID = 132545653L;

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		final String userId = request.getParameter("userId");
		final String previousQuery = request.getParameter("previousQuery");
		if (loggedUser == null) {
			response.sendRedirect("signIn");
			return;
		}
		if (loggedUser.isSuperAdmin()) {
					
			if (isNotNullOrEmptyString(userId)) {
				
				try {
					final String accessToken = folioAPI.loginFolioAdministrator();	
					// USER DELETE
					//boolean res = folioAPI.deleteUser(accessToken, userId);
					// USER INACTIVE
					
					final FolioUserModel userToDelete= folioAPI.searchSimpleFolioUserbyId(accessToken, userId,isCustomFieldActive);
					boolean res = folioAPI.deactiveUser(accessToken, userToDelete);
					
					if (res) {
						request.setAttribute("message", "delete_success");
					}
					else {
						request.setAttribute("inError", true);		
						request.setAttribute("errorMessage", "error");
					}
					
				} catch (FolioException e) {
					request.setAttribute("inError", true);		
					request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));					
				}
			}
			else {				
				request.setAttribute("inError", true);	
				request.setAttribute("errorMessage", "compile_mandatory");
			}				
		}
		else {
			request.setAttribute("inError", true);	
			request.setAttribute("errorMessage", "compile_mandatory");
		}
		response.sendRedirect("searchFolio?userQuery=" + previousQuery);
	}
}

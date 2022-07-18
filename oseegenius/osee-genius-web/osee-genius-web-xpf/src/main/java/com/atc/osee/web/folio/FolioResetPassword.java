package com.atc.osee.web.folio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

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
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioResetPassword extends OseeGeniusServlet {
	private static final long serialVersionUID = 15748483L;

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		final String userId = request.getParameter("userId");
		String userIdToChange = null;	
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (isNotNullOrEmptyString(userId) &&
				loggedUser.isAdmin()) {
			userIdToChange = userId;				
		}
		else {
			//se l'utente loggato non è un admin, può modificare solo i propri dati, per cui l'id è quello dell'utente in loggato in sessione
			// e sovrascrivo qualsiasi altro id provenuto dall'esterno				
			userIdToChange = loggedUser.getId();
		}
		
			
		try {
			final String accessToken = loggedUser.getOkapiToken();
							
			boolean res = folioAPI.resetPassword(accessToken, userId);
			if(res) {
				request.setAttribute("inError", true);		
				request.setAttribute("errorMessage", "successo");
				forwardTo(request, response, "/components/userPanel/user_search.vm", "workspace_layout.vm");
			}
			else {
				request.setAttribute("inError", true);		
				request.setAttribute("errorMessage", "error");
				forwardTo(request, response, "/components/userPanel/user_search.vm", "workspace_layout.vm");
			}				
						
		}
		catch (FolioException e) {
			request.setAttribute("inError", true);		
			request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
			forwardTo(request, response, "/components/userPanel/user_search.vm", "workspace_layout.vm");
		}
	}
	
}

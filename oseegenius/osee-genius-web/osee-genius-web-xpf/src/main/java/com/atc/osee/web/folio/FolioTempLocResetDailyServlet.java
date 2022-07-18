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
 * Reset Temporary Location in Folio
 * This servlet needs to be called on daily basis during the night
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioTempLocResetDailyServlet extends FolioUserServlet {
	private static final long serialVersionUID = 132579675L;

	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		Log.info("Daily reset for Temporary Location");
		response.setContentType("application/json");
				
		int count = 0;
		StringBuilder errorIds = null;
		
		StringBuilder builderResponse = null;
		
		try {
			final String accessToken = folioAPI.loginFolioAdministrator();	
			List<JSONObject> list = folioAPI.getItemInDeposit(accessToken);
			if (list.size() == 0) {
				Log.info("No item to reset");
			}
			for (JSONObject currentJson : list) {
				
				FolioItemModel currentItem = new FolioItemModel();
				currentItem.setFolioJson(currentJson);
				Log.info("reset temporary location for item barcode" + currentItem.getBarcode() + " - status :" + currentItem.getStatus() + "- temporaryLocation: " + currentItem.getTemporaryLocationContent());
				
				currentJson.remove("temporaryLocation");
				String currentId = currentJson.getString("id");
				folioAPI.resetItemDeposit(currentId, currentJson.toString(), accessToken);
			}			
			
			if (builderResponse == null) {
				builderResponse = new StringBuilder("{");
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
}

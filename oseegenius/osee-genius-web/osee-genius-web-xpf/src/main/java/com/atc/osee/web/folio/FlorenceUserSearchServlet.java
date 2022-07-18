package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONObject;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.servlets.writers.ExcelOutputWriter;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FlorenceUserSearchServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 12645853L;
	private static  int DEFAULT_LIMIT = 9000;
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		ConfigurationTool configuration = getConfiguration(request);
		boolean isCustomFieldActive=configuration.isCustomFieldActive();
		final String withNotesParma = request.getParameter("withNotes");
		boolean withNotes = false;
		if ("true".equals(withNotesParma)) {
			withNotes = true;
		}
		FolioUserModel account = getVisit(request).getFolioAccount();
		if (account == null) {
			response.sendRedirect("signIn");			
		}
		else {						
			final String accessToken = account.getOkapiToken();		
			List<String> patronGroups = new ArrayList<String> ();
			patronGroups.add(folioConfig.getString(FolioConstants.USER_BASE_LOAN));
			patronGroups.add(folioConfig.getString(FolioConstants.USER_MUSIC_LOAN));
			patronGroups.add(folioConfig.getString(FolioConstants.USER_HANDWRITE_LOAN));
			final String city = "Firenze";
			try {		
				FolioResponseModel searchResponse = folioAPI.searchUserByCity(accessToken, DEFAULT_LIMIT, city, patronGroups);
						Map<String, String> patronGroupsName = folioAPI.getAllPatronGroup(accessToken);
						List<FolioUserModel> userList = folioAPI.getUserList(searchResponse,isCustomFieldActive);
						System.out.println("numFound " + folioAPI.getNumFound(searchResponse));
						injectData(userList, accessToken, patronGroupsName, withNotes, isCustomFieldActive);
											
						String relativePath = "/reports/user_excel.jrxml";		
						String reportName = request.getSession().getServletContext().getRealPath(relativePath);
						JasperDesign design = JRXmlLoader.load(new FileInputStream(reportName));	
						JasperReport report = JasperCompileManager.compileReport(design);
						Map<String, Object> params = new HashMap<String, Object>();	
						
						params.put("ITEMS", new JRBeanCollectionDataSource(userList));
						JasperPrint print = JasperFillManager.fillReport(report, params, new JRBeanCollectionDataSource(userList));
												
						response.reset();
						response.setContentType("application/excel");
						response.setHeader("Content-disposition", 
					"attachment; filename=\"" 
					+ "user_report" 
					+ "." 
					+ "xls" 
					+ "\"");
					JRXlsExporter exporter = new JRXlsExporter();
					exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, Boolean.TRUE);
					exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
					exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
					exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.FALSE);
					exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
					exporter.setParameter(JRXlsExporterParameter.IS_IGNORE_GRAPHICS, Boolean.TRUE);
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());						
					exporter.exportReport();	
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	
	}
	
	private void injectData(List<FolioUserModel> userList, final String accessToken, Map<String, String> patronGroups, boolean withNotes, boolean isCustomFieldActive) throws FolioException {
		for (FolioUserModel currentUser : userList) {
			injectPatronName(currentUser, patronGroups);
			if (withNotes) {
				injectNotes(currentUser, accessToken, isCustomFieldActive);
			}
		}
	}
	
	private void injectNotes(FolioUserModel currentUser, final String accessToken, boolean isCustomFieldActive) throws FolioException {		
			FolioUserModel completeUser = folioAPI.searchCompleteFolioUserbyId(accessToken, currentUser.getId(), isCustomFieldActive);
			currentUser.setNotes(completeUser.getNotes());		
	}
	
	private void injectPatronName(FolioUserModel currentUser, Map<String, String> patronGroups) {		
			currentUser.setPatronGroupName(patronGroups.get(currentUser.getPatronGroupCode()));		
	}
	
	
	
}

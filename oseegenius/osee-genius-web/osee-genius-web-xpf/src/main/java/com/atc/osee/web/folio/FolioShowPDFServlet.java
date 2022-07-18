package com.atc.osee.web.folio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 * loan in FOLIO
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioShowPDFServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 12467696843L;	
	
	protected Map<String, Map<String, Integer>> materialTypeLimits;
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		JasperPrint pdf = (JasperPrint) request.getSession().getAttribute("pdf");
		showPDF(pdf, response);
		return;
	}
		

	protected void showPDF(JasperPrint pdf, HttpServletResponse response) {
		response.reset();
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", 
				"attachment; filename=\"" 
				+ "loan_request" 
				+ "." 
				+ "pdf" 
				+ "\"");
		try {
			JasperExportManager.exportReportToPdfStream(pdf, response.getOutputStream());		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

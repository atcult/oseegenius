package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.atc.osee.web.log.Log;
import com.atc.osee.web.tools.FolioConfigurationTool;
import com.atc.osee.web.util.Utils;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioCatalogServlet extends FolioLoanServlet {
	private static final long serialVersionUID = 1568685453L;
	
	protected static DataSource datasource;
	protected BncfDao dao;
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null) {
			response.sendRedirect("signIn");
			return;
		}
		else {
			
			final String resourceType = request.getParameter("resourceType") != null ?  request.getParameter("resourceType") : "monograph";
			request.setAttribute("type", resourceType);
			List<NameValuePair> servicePointList = ("monograph".equals(resourceType)) ? getMonographServicePoint(loggedUser, request) : getPeriodicServicePoint();
			request.setAttribute("servicePointList", servicePointList);
			
			try {
				request.setAttribute("fondo", dao.getFondoList());
				
			}catch (Exception exception) {
				exception.printStackTrace();
				request.setAttribute("inError", true);	
				request.setAttribute("errorMessage", "something go wrong");
				forwardTo(request, response, "/components/userPanel/folio_catalog.vm", "workspace_layout.vm");
				return;
			}
			forwardTo(request, response, "/components/userPanel/folio_catalog.vm", "workspace_layout.vm");
		}
			
	}
	
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		ResourceBundle messages = ResourceBundle.getBundle("resources", Locale.ITALIAN);		
		ResourceBundle addMessages = ResourceBundle.getBundle("additional_resources", Locale.ITALIAN);
		
		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null || !isNotNullOrEmptyString(getVisit(request).getFolioAccount().getId())) {
			response.sendRedirect("signIn");
			return;
		}
		else {
			
			final String resourceType = request.getParameter("resourceType");
						
			FolioRecord record = new FolioRecord();			
			String note = request.getParameter("note");
			record.setNote(note);
			record.setLoanType(folioConfig.getString(FolioConstants.LOAN_TYPE_CONS_ID));
			
			boolean mandatoryFieldCheck = false;
			try {		
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");			
				String now = formatter.format(new Date()).toString();
				record.setCatalogDate(now);
				
				final String accessToken = folioAPI.loginFolioAdministrator();	
				
				String printingPoint = null;
				String servicePointId = null;
				String servicePointLabel = null;
				
				if ("monograph".equals(resourceType)) {
											
					final String servicePoint = request.getParameter("servicePoint_monograph");
					servicePointId = servicePoint.split("___")[0];
					servicePointLabel = servicePoint.split("___")[1];		
					printingPoint = getPrintingPoint(servicePointId);
									
					record.setCollocation(request.getParameter("collocation_monograph"));
					String title = request.getParameter("title_monograph");
					record.setTitle(title);
					record.setIndexedTitle(removeStopWords(title));					
					record.setPublisherName(request.getParameter("publisher_monograph"));
					record.setPlace(request.getParameter("place_monograph"));
					record.setYear(request.getParameter("year_monograph"));
					if (isNotNullOrEmptyString(request.getParameter("author_corporate_monograph"))){
						 record.setAuthorName(request.getParameter("author_corporate_monograph"));
						 record.setAuthorType(folioConfig.getString(FolioConstants.AUTHOR_CORPORATE_TYPE));	
					}
					else if (isNotNullOrEmptyString(request.getParameter("author_person_monograph"))) {
						record.setAuthorName(request.getParameter("author_person_monograph"));
						record.setAuthorType(folioConfig.getString(FolioConstants.AUTHOR_PERSON_TYPE));	
					}
					record.setPublicationDate(request.getParameter("publication_date_monograph"));
					record.setBibliographicLevel(folioConfig.getString(FolioConstants.TYPE_MONOGRAPH));
					record.setResourceType(folioConfig.getString(FolioConstants.TYPE_RESOURCE_MONOGRAPH));
					record.setHoldingType(folioConfig.getString(FolioConstants.TYPE_HOLDING_MONOGRAPH));
					record.setHoldingSection(folioConfig.getString(FolioConstants.SECTION_HOLDING_A));
					record.setItemType(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_ID));
					record.setBarcode(dao.getBarcode(FolioConstants.MONOGRAPH_AND_SERIAL_PREFIX));
					record.setPublisher(record.getPublisherString());
					mandatoryFieldCheck = monographMandatoryFieldsChecked(record);		
					request.setAttribute("servicePointList", getMonographServicePoint(loggedUser, request));
				}
				if ("periodic".equals(resourceType)) {			
					final String servicePoint = request.getParameter("servicePoint_periodic");
					servicePointId = servicePoint.split("___")[0];
					servicePointLabel = servicePoint.split("___")[1];		
					printingPoint = getPrintingPoint(servicePointId);
					
					record.setCollocation(request.getParameter("collocation_periodic"));
					String title = request.getParameter("title_periodic");
					record.setTitle(title);
					record.setIndexedTitle(removeStopWords(title));
					record.setPublisherName(request.getParameter("publisher_periodic"));
					record.setPlace(request.getParameter("place_periodic"));
					record.setPublicationDate(request.getParameter("publication_date_periodic"));
					record.setYear(request.getParameter("year_periodic"));
					record.setVolume(request.getParameter("volume_periodic"));
					record.setIssue(request.getParameter("issue_periodic"));
					record.setMonth(request.getParameter("month_periodic"));
					String day =  request.getParameter("day_periodic");
					record.setDay(day);
						
					record.setBibliographicLevel(folioConfig.getString(FolioConstants.TYPE_PERIODIC));
					record.setResourceType(folioConfig.getString(FolioConstants.TYPE_RESOURCE_PERIODIC));
					record.setHoldingType(folioConfig.getString(FolioConstants.TYPE_HOLDING_PERIODIC));
					record.setHoldingSection(folioConfig.getString(FolioConstants.SECTION_HOLDING_G));
					record.setItemType(folioConfig.getString(FolioConstants.JOURNAL_ID));
					record.setBarcode(dao.getBarcode(FolioConstants.MONOGRAPH_AND_SERIAL_PREFIX));
					record.setPublisher(record.getPublisherString());
					mandatoryFieldCheck = periodicMandatoryFieldsChecked(record);
					request.setAttribute("servicePointList", getPeriodicServicePoint());
				}
				if ("manuscript".equals(resourceType)) {
					printingPoint = FolioConstants.PRINTING_POINT_SMANOSCRITTI;
					servicePointId = folioConfig.getString(FolioConstants.HANDWRITE_ROOM);
					servicePointLabel = getText(messages, addMessages, folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC));
					
					record.setBibliographicLevel(folioConfig.getString(FolioConstants.TYPE_MONOGRAPH));
					record.setResourceType(folioConfig.getString(FolioConstants.TYPE_RESOURCE_MANUSCRIPT));
					record.setHoldingType(folioConfig.getString(FolioConstants.TYPE_HOLDING_MONOGRAPH));
					record.setHoldingSection(folioConfig.getString(FolioConstants.SECTION_HOLDING_H));
					record.setItemType(folioConfig.getString(FolioConstants.HANDWRITE_ID));
					record.setBarcode(dao.getBarcode(FolioConstants.MANUSCRIPT_PREFIX));					
					
					record.setManuscriptFondo(request.getParameter("fondo_manuscript_hidden").trim());
					record.setManuscriptCollocation(request.getParameter("collocation_manuscript_hidden").trim());
					record.setManuscriptSpec(request.getParameter("specification_manuscript_hidden"));
					String volumeSelectField = request.getParameter("volume_manuscript_select");
					String volumeInputField = request.getParameter("volume_manuscript_input");
					String volumeLabelField = request.getParameter("volume_label_hidden");
					if (volumeLabelField != null && !"".equals(volumeLabelField)) {			
						record.setManuscriptLabel(getText(messages, addMessages, (volumeLabelField)));
						if (volumeSelectField != null && !"".equals(volumeSelectField)) {
							record.setManuscriptVolume(volumeSelectField);
						}
						else {
							record.setManuscriptVolume(volumeInputField);
						}
					}
					String collocationManuscript = createManuscriptSignature(record);
					
					String newCollocation = request.getParameter("new_collocation");
					record.setCollocation(collocationManuscript);
					record.setNewCollocation(newCollocation);
					record.setTitle(collocationManuscript);
					record.setIndexedTitle(collocationManuscript);
					mandatoryFieldCheck = manuscriptMandatoryFieldChecked(request); 
				}
				
				if (mandatoryFieldCheck) {
					String instanceCreated = folioAPI.createInstance(accessToken, record);
					if (instanceCreated != null) {
						record.setInstanceId(getInstanceId(instanceCreated));
						FolioItemModel item = folioAPI.createItemWithHolding(accessToken, record);
						
						makeRequestOrLoan(request, 
								response, 
								loggedUser, 
								record,
								servicePointId, 
								servicePointLabel, 
								accessToken, 
								item,
								printingPoint);
						forwardTo(request, response, "/components/userPanel/request_result.vm", "workspace_layout.vm");
						return;
					}					
				}
				else {
					request.setAttribute("inError", true);	
					request.setAttribute("errorMessage", "compile_mandatory");
					forwardTo(request, response, "/components/userPanel/request_result.vm", "workspace_layout.vm");
					return;
				}
				
			} catch (SQLException sqlException) {
				request.setAttribute("inError", true);	
				sqlException.printStackTrace();
				request.setAttribute("errorMessage", "Errore nella creazione del barcode");
				forwardTo(request, response, "/components/userPanel/request_result.vm", "workspace_layout.vm");	
				return;
			} catch (FolioException e) {
				e.printStackTrace();
				request.setAttribute("inError", true);		
				request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
				forwardTo(request, response, "/components/userPanel/request_result.vm", "workspace_layout.vm");	
				return;
			}
		}
		
		
	}
	
	private boolean manuscriptMandatoryFieldChecked(final HttpServletRequest request) {
		return (isNotNullOrEmptyString(request.getParameter("fondo_manuscript_hidden")) &&
				isNotNullOrEmptyString(request.getParameter("collocation_manuscript_hidden")) &&
				isNotNullOrEmptyString(request.getParameter("specification_manuscript_hidden"))
				&& (!isNotNullOrEmptyString(request.getParameter("volume_label_hidden")) || 
						(isNotNullOrEmptyString(request.getParameter("volume_label_hidden")) && 
								((isNotNullOrEmptyString(request.getParameter("volume_manuscript_input")) || isNotNullOrEmptyString(request.getParameter("volume_manuscript_select")))
						))
					)
				);
	}
	
	private String createManuscriptSignature(FolioRecord record) {
		StringBuilder result = new StringBuilder();
		try {		
			
			result.append("Fondo: ").append(record.getManuscriptFondo())
				.append("; Coll.: ").append(record.getManuscriptCollocation())
				.append("; Specif.: ").append(record.getManuscriptSpec());
			if (record.getManuscriptLabel() != null) {			
				result.append("; ").append(record.getManuscriptLabel()).append(": ");
				result.append(record.getManuscriptVolume());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
	private boolean periodicMandatoryFieldsChecked(FolioRecord record) {
		return (isNotNullOrEmptyString(record.getCollocation()) &&
				isNotNullOrEmptyString(record.getTitle()) &&
				isNotNullOrEmptyString(record.getPublicationDate()) &&
				isNotNullOrEmptyString(record.getYear()));
	}
	
	private boolean monographMandatoryFieldsChecked(FolioRecord record) {
		return (isNotNullOrEmptyString(record.getCollocation()) &&				
				isNotNullOrEmptyString(record.getTitle()) &&
				isNotNullOrEmptyString(record.getPublicationDate()));
	}
	

	
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			final InitialContext cxt = new InitialContext();
			datasource = (DataSource) cxt.lookup("java:/comp/env/jdbc/pg");
			dao = new BncfDao(datasource);
		} catch (Exception ignore) {
			Log.error(ignore.getMessage());
			System.out.println(ignore);
		}
	}
	
	/**
	 * Extract instance id from instance location
	 * es. http://127.0.0.1:8082/inventory/instances/ece258e9-047e-4ba9-8a7b-dabaa1e829bb
	 * --> "ece258e9-047e-4ba9-8a7b-dabaa1e829bb"
	 * @param instanceLocation
	 * @return instance id
	 */
	private String getInstanceId(String instanceLocation) {
		if (instanceLocation != null) {
			int lastIndexOfSlash = instanceLocation.lastIndexOf("/");
			if (lastIndexOfSlash > -1) {
				return instanceLocation.substring(lastIndexOfSlash + 1); 
			}
		}
		return null;
	}
	
	/**
	 * remove articles in first position and single quotes to create index title. 
	 * es. "Un titolo" --> "titolo"
	 * es. "Unico titolo" --> "Unico titolo"
	 * es. "Un'altra volta" --> "altra volta"
	 * @param text
	 * @return cleaned text
	 */
	private String removeStopWords(String text) {
		String result = text;
		try {
			if (text != null) {
				InputStream is = getServletContext().getResourceAsStream("/WEB-INF/stopwords.txt");				
				 if (is != null) {
			        InputStreamReader isr = new InputStreamReader(is);
			        BufferedReader reader = new BufferedReader(isr);
			        String inputLine;		        	
					while ((inputLine = reader.readLine()) != null) {
						if (text.matches("^(" + inputLine + ")(\\s|').*")) {
							result = text.replaceFirst(inputLine, "").replaceAll("'", "").trim();							
						}
					}	
				 }			 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private List<NameValuePair> getPeriodicServicePoint() {
		List<NameValuePair> result = new ArrayList<NameValuePair>();
		result.add(new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)));
		result.add(new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)));
		return result;
	}
	
	private List<NameValuePair> getMonographServicePoint(FolioUserModel user, HttpServletRequest request) {
		List<NameValuePair> result = new ArrayList<NameValuePair>();		
		result.add(new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)));
		result.add(new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)));
				
		if (loanEnabledUser(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_CODE), user) && !Utils.isLoanServiceClose(getConfiguration(request))) {
			result.add(new BasicNameValuePair(folioConfig.getString(FolioConstants.LOAN_ROOM), folioConfig.getString(FolioConstants.LOAN_ROOM_DESC)));
		}
		
		if(manuscriptsRoomEnable(folioConfig.getString(FolioConstants.HANDWRITE_CODE),user)) {
			result.add(new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)));
		}	
		return result;
	}
	
	
	
	
	private boolean manuscriptsRoomEnable(final String materialType, FolioUserModel user) {
		NameValuePair servicePoints [] = user.getServicePoints().get(materialType);
		if (servicePoints == null) {
			return false;
		}
		for ( int i = 0; i < servicePoints.length; i ++) {
			if (servicePoints[i].getName().equals(folioConfig.getString(FolioConstants.HANDWRITE_ROOM))) {
				return true;
			}
		}
		return false;
	}
		
		
	
	private boolean loanEnabledUser(final String materialType, FolioUserModel user) {
		NameValuePair servicePoints [] = user.getServicePoints().get(materialType);
		if (servicePoints == null) {
			return false;
		}
		for ( int i = 0; i < servicePoints.length; i ++) {
			if (servicePoints[i].getName().equals(folioConfig.getString(FolioConstants.LOAN_ROOM))) {
				return true;
			}
		}
		return false;
	}
	
	private String getPrintingPoint(final String servicePointId) {
		if (folioConfig.getString(FolioConstants.CONSULT_ROOM).equals(servicePointId)) {
			return FolioConstants.PRINTING_POINT_SCONSULTAZIONE;
		}
		if (folioConfig.getString(FolioConstants.READ_ROOM).equals(servicePointId)) {
			return FolioConstants.PRINTING_POINT_SLETTURA;
		}
		if (folioConfig.getString(FolioConstants.LOAN_ROOM).equals(servicePointId)) {
			return FolioConstants.PRINTING_POINT_SLETTURA;
		}
		if (folioConfig.getString(FolioConstants.SERIAL_ROOM).equals(servicePointId)) {
			return FolioConstants.PRINTING_POINT_SPERIODICI;
		}
		if (folioConfig.getString(FolioConstants.HANDWRITE_ROOM).equals(servicePointId)) {
			return FolioConstants.PRINTING_POINT_SMANOSCRITTI;
		}
		return FolioConstants.PRINTING_POINT_SLETTURA;
	}
}

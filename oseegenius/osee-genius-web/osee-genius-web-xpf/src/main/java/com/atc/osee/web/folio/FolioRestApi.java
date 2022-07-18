package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jfree.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;
import com.google.gson.Gson;



public class FolioRestApi {
	
	final String ADMIN_LOGIN_ENDPOINT = "/authn/login";
	final String LOGIN_ENDPOINT = "/bl-users/login";
	final String REGISTER_ENDPOINT = "/users";
	final String REGISTER_CREDENTIAL_ENDPOINT = "/authn/credentials";
	final String REGISTER_PERMISSION_ENDPOINT = "/perms/users";
	final String SEARCH_USER = "/users";
	final String USER_LOAN_ENDPOINT = "/patron/account";
	final String NOTE_CREATION_ENDPOINT = "/notes";
	final String NOTE_LIST_ENDPOINT = "/note-links/domain/users/type/user/id";
	//final String COPY_INFO_ENDPOINT = "/item-storage/items";
	final String COPY_INFO_ENDPOINT = "/inventory/items";
	final String RESET_PASSWORD_ENDPOINT = "/bl-users/password-reset/link";		
	final String GET_USER_TYPES_ENDPOINT = "/groups?limit=30";
	final String REQUEST_ENDPOINT = "/circulation/requests";
	final String GET_USER_HISTORY_LOAN = "/circulation/loans";
	final String GET_PATRON_GROUP = "/groups";
	final String SEARCH_INSTANCES = "/inventory/instances";
	final String SEARCH_HOLDINGS = "/holdings-storage/holdings";
	final String RENEW_ENDPOINT = "/circulation/renew-by-id";
	final String CALENDAR_ENDPOINT = "/calendar/periods";
	final String LOCATIONS = "/locations";
	final String CHECKIN_ENDPOINT = "/circulation/check-in-by-barcode";
	final String USER_SERVICE_POINT_ENDPOINT = "/service-points-users";
	
	final String ENCRYPTION_KEY = "ABCDEFGHIJKLMNOP";
	
	final String HTML_QUOTES = "%22";
	final String HTML_OPEN_PAR = "%28";
	final String HTML_CLOSE_PAR = "%29";
	final String HTML_STAR = "%2A";
	final String HTML_SPACE = "%20";
	final String HTML_EQUAL = "%3D";
	final String HTML_MINOR = "%3C";
	final String ACCEPT_JSON = "application/json";
	
	
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	
	
	/**
	 * get item created from request in opac. Their barcode start with SRL
	 * @param folioToken
	 * @return
	 * @throws FolioException
	 */
	protected FolioResponseModel getPeriodicFromOpac(final String folioToken) throws FolioException  {
		StringBuilder URL = new StringBuilder();
		URL.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(COPY_INFO_ENDPOINT)
			.append("?").append("limit=500&query=");
		try {
			URL.append(URLEncoder.encode("(barcode=SRL* AND status.name=Available)", "UTF-8)"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return okapiCall(URL.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
	}
	
	protected FolioResponseModel getServicePointCalendar(final String folioToken, final String servicePointId)  throws FolioException  {
		StringBuilder URL = new StringBuilder();
		URL.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(CALENDAR_ENDPOINT)
			.append("/").append(servicePointId).append("/").append("period?withOpeningDays=true&showPast=true&showExceptional=false");
		return okapiCall(URL.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
	}
	
	
	protected FolioOpenings getOpeningDays(final FolioResponseModel folioResponse) {
		FolioOpenings openings = new FolioOpenings();
		if (folioResponse != null && folioResponse.getJsonResponse() != null) {
			try {
				JSONObject json = new JSONObject (folioResponse.getJsonResponse());
				JSONArray array = json.getJSONArray("openingPeriods");
				for (int j = 0; j < array.length(); j++) {		   
				    JSONObject current = array.getJSONObject(j);
				    JSONArray days = current.getJSONArray("openingDays");
				    for (int i = 0; j < days.length(); i++) {		   
					    JSONObject currentDay = array.getJSONObject(i);
					    String labelDay = currentDay.getJSONObject("weekdays").getString("day");
					    
				    }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Map<String, FolioOpeningDay> map = new HashMap<String, FolioOpeningDay>();
		openings.setOpenings(map);
		return openings;
	}
	
	protected boolean deleteUser (final String folioToken, final String userId)  throws FolioException {
		StringBuilder URL = new StringBuilder();
		URL.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(REGISTER_ENDPOINT)
			.append("/").append(userId);
		return String.valueOf(okapiCall(URL.toString(),
				"DELETE",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				"text/plain").getResponseCode()).startsWith("2");
		
	}
	
	protected boolean renewLoan (final String folioToken, final String itemId, final String userId) throws FolioException {
		String URL = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + RENEW_ENDPOINT;
		JSONObject json = new JSONObject();
		json.put("userId", userId);
		json.put("itemId", itemId);
		
		FolioResponseModel folioResponse = okapiCall(URL,
				"POST",
				folioToken,
				json.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		Log.info("RENEWLOAN: " + " -itemID: " + itemId + " -userId: " + userId);		
		return Integer.toString(folioResponse.getResponseCode()).startsWith("2");		
	}
	
	protected boolean deleteRequest (final String folioToken, final String requestId, final String userId)  throws FolioException {
		StringBuilder URL = new StringBuilder();
		
		URL.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(REQUEST_ENDPOINT)
			.append("/").append(requestId);
		
		JSONObject requestObject = new JSONObject(searchRequestById(folioToken, requestId));
		requestObject.put("status", folioConfig.getString(FolioConstants.STATUS_CLOSED_CANCELLED));
		requestObject.put("cancelledByUserId", userId);
		requestObject.put("cancellationReasonId", folioConfig.getString(FolioConstants.DELETE_BY_PATRON_REASON_ID));
		SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String now = date.format(new Date());
		requestObject.put("cancelledDate", now);
		
		FolioResponseModel folioResponse = okapiCall(URL.toString(),
				"PUT",
				folioToken,
				requestObject.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		Log.info("DELETEREQUEST: " + " -requestID: " + requestId + " -userId: " + userId);		
		return Integer.toString(folioResponse.getResponseCode()).startsWith("2");
	}
	
	protected String searchRequestById (final String folioToken, final String requestId)  throws FolioException {
		StringBuilder URL = new StringBuilder();
		URL.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(REQUEST_ENDPOINT)
			.append("/").append(requestId);		
		return (okapiCall(URL.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON)).getJsonResponse();
	}
	protected List<FolioLoan> searchRequestOpenByItemId(final String folioToken, final String itemId) throws FolioException {
		String URL = "";
		try {
			URL = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REQUEST_ENDPOINT +
				"?query=" + URLEncoder.encode("(itemId=" + itemId + " and position>=1)", "UTF-8");
		}
		catch (Exception e) {
			Log.error(e);
		}
		return getRequestList(okapiCall(URL.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON));
	}
	
	protected int howManyRequestOpenByItemId(final String folioToken, final String itemId) throws FolioException {
		String URL = "";
		try {
			URL = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REQUEST_ENDPOINT +
				"?query=" + URLEncoder.encode("(itemId=" + itemId + " and position>=1)", "UTF-8") + "&limit=0";
		}
		catch (Exception e) {
			Log.error(e);
		}
		return okapiCall(URL,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getTotalRecords();
	}
	
	/**
	 * get open request List by user id
	 * @param folioToken
	 * @param userId
	 * @return
	 * @throws FolioException
	 */
	
	protected List<FolioLoan> getUserRequestOpen(final String folioToken, final String userId)  throws FolioException {
		
		return getRequestList(getUserOpenRequestResponse(folioToken, userId, "50", null));		
	}
	
	
	protected FolioResponseModel getUserOpenRequestResponse (final String folioToken, final String userId, final String requestLimit, final String requestOffset)  throws FolioException {
		
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REQUEST_ENDPOINT + "?query=" +
				HTML_OPEN_PAR + "requesterId" + HTML_EQUAL + userId + HTML_SPACE + "and" + HTML_SPACE +
				"status" + HTML_EQUAL + HTML_QUOTES + "Open*" + HTML_QUOTES + HTML_CLOSE_PAR + 
				"sortby"+ HTML_SPACE + "requestDate/sort.descending" +
				"&limit=" + requestLimit;
		if (requestOffset != null) {
			url = url + "&offset=" + requestOffset;
		}
		return okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);		
	}
	
protected FolioResponseModel getUserCloseRequestResponse (final String folioToken, final String userId, final String requestLimit, final String requestOffset)  throws FolioException {
		
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REQUEST_ENDPOINT + "?query=" +
				HTML_OPEN_PAR + "requesterId" + HTML_EQUAL + userId + HTML_SPACE + "not" + HTML_SPACE +
				"status" + HTML_EQUAL + HTML_QUOTES + "Open*" + HTML_QUOTES + HTML_CLOSE_PAR + 
				"sortby"+ HTML_SPACE + "requestDate/sort.descending" +
				"&limit=" + requestLimit;
		if (requestOffset != null) {
			url = url + "&offset=" + requestOffset;
		}
		return okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);		
	}
	
	
	
	/**
	 * 
	 * @param configuration
	 * @param folioToken
	 * @param servicePoint
	 * @param userId
	 * @return number of open request (loan type) for the user
	 * @throws FolioException
	 */
	
	protected int getUserLoanRequestOpenCount(final String folioToken, final String servicePoint, final String userId) throws FolioException {
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REQUEST_ENDPOINT + "?query=" +
				HTML_OPEN_PAR + "requesterId" + HTML_EQUAL + userId + HTML_SPACE +  "and" + HTML_SPACE +
				"status" + HTML_EQUAL + HTML_QUOTES + "Open*" + HTML_QUOTES + HTML_SPACE + "and" + HTML_SPACE +
				"pickupServicePointId" + HTML_EQUAL + servicePoint + HTML_CLOSE_PAR + "&limit=0";
		return getNumFound(okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON));				
	}
	
	
	
	/**
	 * get number of results of a query
	 * @param folioResponse
	 * @return totalRecords field in json
	 */
	
	protected int getNumFound(FolioResponseModel folioResponse) {
		JSONObject json = new JSONObject(folioResponse.getJsonResponse());
		if (json != null && json.has("totalRecords")) {
			return json.getInt("totalRecords");
		}
		else return 0;
	}
	
	/**
	 * get all the request (all status) by user id
	 * @param folioToken
	 * @param input
	 * @param limit
	 * @param offset
	 * @return
	 * @throws FolioException
	 */
	protected FolioResponseModel userRequestHistory (final String folioToken, final String input, final String limit, final String offset)  throws FolioException {
		StringBuilder HISTORY_URL = new StringBuilder();
		HISTORY_URL.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(REQUEST_ENDPOINT)
			.append("?query=").append(HTML_OPEN_PAR)
			.append("requesterId=").append(input)
			.append(HTML_CLOSE_PAR)
			.append("sortby").append(HTML_SPACE).append("requestDate/sort.descending")
			.append("&limit=").append(limit);
		if (offset != null) {
			HISTORY_URL.append("&offset=").append(offset);
		}
		
		return okapiCall(HISTORY_URL.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);	
	}
		
	
	protected FolioResponseModel getLastLoan (final String folioToken, final String input, final String limit, final String offset)  throws FolioException {
		StringBuilder LAST_LOAN = new StringBuilder();
		LAST_LOAN.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(GET_USER_HISTORY_LOAN)
		.append("?limit=1")
		.append("&query=itemId").append(HTML_EQUAL).append(HTML_EQUAL).append(input)
		.append(HTML_SPACE).append("sortby").append(HTML_SPACE).append("loanDate/sort.descending");
		
		return okapiCall(LAST_LOAN.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);	
	}
	
	protected String getDueDate (FolioResponseModel folioResponse) {
		JSONObject loans = new JSONObject(folioResponse.getJsonResponse());
		JSONArray loanArray = loans.getJSONArray("loans");
		for (int j = 0; j < loanArray.length(); j++) {		   
		    JSONObject current = loanArray.getJSONObject(j);
		    String status = current.getJSONObject("status").getString("name");
		    if ("Open".equals(status)) {
		    	return current.getString("dueDate");
		    }		    	
		}
		return null;
	}
	
	protected FolioResponseModel userLoanHistory (final String folioToken, final String input, final String limit, final String offset)  throws FolioException {
		StringBuilder HISTORY_URL = new StringBuilder();
		HISTORY_URL.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(GET_USER_HISTORY_LOAN)
			.append("?query=").append(HTML_OPEN_PAR)
			.append("userId==").append(input)
			.append(HTML_CLOSE_PAR)
			.append("sortby").append(HTML_SPACE).append("loanDate/sort.descending")
			.append("&limit=").append(limit);
			if (offset != null) {
				HISTORY_URL.append("&offset=").append(offset);
			};
		return okapiCall(HISTORY_URL.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);	
	}
	
	/**
	 * Get "open" loans by user id.
	 * 
	 * @param configuration
	 * @param folioToken
	 * @param userId
	 * @return
	 * @throws FolioException
	 */
	protected FolioResponseModel getUserOpenLoans(final String folioToken, final String userId)  throws FolioException {
		
		return getUserOpenLoansResponse(folioToken, userId, "10", null);						
	}
	
	protected FolioResponseModel getUserOpenLoansBefore(final String folioToken, 
														final String userId, 
														final String limit, 
														final String offset,
														final String beforeDate) throws FolioException {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(GET_USER_HISTORY_LOAN)
		.append("?query=").append(HTML_OPEN_PAR)
		.append("userId==").append(userId)
		.append(HTML_SPACE).append("and").append(HTML_SPACE)
		.append("status").append(HTML_EQUAL).append(HTML_QUOTES).append("Open*").append(HTML_QUOTES)
		.append(HTML_SPACE).append("and").append(HTML_SPACE)
		.append("loanDate").append(HTML_MINOR).append(beforeDate)
		.append(HTML_CLOSE_PAR)			
		.append("sortby").append(HTML_SPACE).append("requestDate/sort.descending")
		.append("&limit=").append(limit);		
	if (offset != null) {
		urlBuilder.append("&offset=").append(offset);
	}
	
	return okapiCall(urlBuilder.toString(),
			"GET",
			folioToken,
			null,
			folioConfig.getString(FolioConstants.FOLIO_TENANT),
			ACCEPT_JSON);
					
		
	}
	
	protected FolioResponseModel getUserOpenLoansResponse(final String folioToken, final String userId, final String limit, final String offset)  throws FolioException {
		
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(GET_USER_HISTORY_LOAN)
			.append("?query=").append(HTML_OPEN_PAR)
			.append("userId==").append(userId)
			.append(HTML_SPACE).append("and").append(HTML_SPACE)
			.append("status").append(HTML_EQUAL).append(HTML_QUOTES).append("Open*").append(HTML_QUOTES)
			.append(HTML_CLOSE_PAR)			
			.append("sortby").append(HTML_SPACE).append("requestDate/sort.descending")
			.append("&limit=").append(limit);					
		if (offset != null) {
			urlBuilder.append("&offset=").append(offset);
		}

		return okapiCall(urlBuilder.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
						
	}
	
protected FolioResponseModel getUseCloseLoansResponse(final String folioToken, final String userId, final String limit, final String offset)  throws FolioException {
		
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(GET_USER_HISTORY_LOAN)
			.append("?query=").append(HTML_OPEN_PAR)
			.append("userId==").append(userId)
			.append(HTML_SPACE).append("not").append(HTML_SPACE)
			.append("status").append(HTML_EQUAL).append(HTML_QUOTES).append("Open*").append(HTML_QUOTES)
			.append(HTML_CLOSE_PAR)			
			.append("sortby").append(HTML_SPACE).append("requestDate/sort.descending")
			.append("&limit=").append(limit);					
		if (offset != null) {
			urlBuilder.append("&offset=").append(offset);
		}

		return okapiCall(urlBuilder.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
						
	}
	
	
	protected List<FolioLoan> getLoanList(FolioResponseModel folioResponse) {
		List<FolioLoan> result = new ArrayList<FolioLoan>();
		
		JSONObject json = new JSONObject(folioResponse.getJsonResponse());
		JSONArray loans = json.getJSONArray("loans");
		for (int i = 0; i < loans.length(); i++) {
			JSONObject currentRequest = loans.getJSONObject(i);
			FolioLoan loan = new FolioLoan();
			loan.setRenewable(false);
			loan.setJson(currentRequest.toString());
			if (currentRequest.has("id")) {
				loan.setId(currentRequest.getString("id"));				
			}
			if (currentRequest.has("userId")) {
				loan.setUserId(currentRequest.getString("userId"));				
			}
			if (currentRequest.has("itemId")) {
				loan.setItemId(currentRequest.getString("itemId"));				
			}			
			JSONObject status = currentRequest.getJSONObject("status");
			loan.setStatus(status.getString("name"));	
			
			if (currentRequest.has("loanDate")) {
				loan.setRequestDate(currentRequest.getString("loanDate"));				
			}
			if (currentRequest.has("dueDate")) {
				loan.setDueDate(currentRequest.getString("dueDate"));				
			}
			JSONObject item = currentRequest.getJSONObject("item");
			if (item.has("title")) {
				loan.setTitle(item.getString("title"));				
			}
			if (item.has("barcode")) {
				loan.setItemBarcode(item.getString("barcode"));				
			}
			if (item.has("enumeration")) {
				loan.setEnumeration(item.getString("enumeration"));
			}
			if (item.has("location")) {
				JSONObject location = item.getJSONObject("location");				
				loan.setLocation(location.getString("name"));
			}
			if (currentRequest.has("checkoutServicePointId")) {
				loan.setServicePointId(currentRequest.getString("checkoutServicePointId"));				
			}
			JSONObject servicePoint = currentRequest.getJSONObject("checkoutServicePoint");
			if (servicePoint.has("name")) {
				loan.setServicePointName(servicePoint.getString("name"));				
			}
			
			result.add(loan);
		}
		
		return result;
	}
	
	protected List<FolioLoan> getRequestList (FolioResponseModel folioResponse) {
		List<FolioLoan> result = new ArrayList<FolioLoan>();
		JSONObject json = new JSONObject(folioResponse.getJsonResponse());
		JSONArray requests = json.getJSONArray("requests");
		for (int i = 0; i < requests.length(); i++) {
			JSONObject currentRequest = requests.getJSONObject(i);
			FolioLoan loan = new FolioLoan();
			loan.setRenewable(false);
			loan.setJson(currentRequest.toString());
			if (currentRequest.has("id")) {
				loan.setId(currentRequest.getString("id"));				
			}
			if (currentRequest.has("requestType")) {
				loan.setRequestType(currentRequest.getString("requestType"));				
			}
			if (currentRequest.has("requestDate")) {
				loan.setRequestDate(currentRequest.getString("requestDate"));				
			}
			if (currentRequest.has("requesterId")) {
				loan.setUserId(currentRequest.getString("requesterId"));				
			}
			if (currentRequest.has("itemId")) {
				loan.setItemId(currentRequest.getString("itemId"));				
			}
			if (currentRequest.has("status")) {
				loan.setStatus(currentRequest.getString("status"));				
			}
			if (currentRequest.has("position")) {
				loan.setPosition(String.valueOf(currentRequest.getInt("position")));				
			}
			if (currentRequest.has("pickupServicePointId")) {
				loan.setServicePointId(currentRequest.getString("pickupServicePointId"));				
			}			
			JSONObject servicePoint = currentRequest.getJSONObject("pickupServicePoint");
			if (servicePoint.has("discoveryDisplayName")) {
				loan.setServicePointName(servicePoint.getString("discoveryDisplayName"));				
			}
			
			JSONObject item = currentRequest.getJSONObject("item");
			if (item.has("title")) {
				loan.setTitle(item.getString("title"));				
			}
			if (item.has("barcode")) {
				loan.setItemBarcode(item.getString("barcode"));				
			}
			if (item.has("enumeration")) {
				loan.setEnumeration(item.getString("enumeration"));
			}
			result.add(loan);
		}
		return result;
	}
	
	protected FolioResponseModel getNextRequest (final String folioToken, final String itemId) throws FolioException {
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REQUEST_ENDPOINT + "?query=" +
				HTML_OPEN_PAR + "itemId" + HTML_EQUAL + HTML_QUOTES + itemId + HTML_QUOTES + HTML_SPACE + 
				"and" + HTML_SPACE + "position" + HTML_EQUAL + "1" + HTML_CLOSE_PAR;
		return okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
	}
	
	protected FolioResponseModel searchFolioUserByBarcode (final String folioToken, final String input, final String limit, final String offset)  throws FolioException {
		try {	
			StringBuilder SEARCH_URL_WITH_INPUT = new StringBuilder();
			SEARCH_URL_WITH_INPUT.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(SEARCH_USER) 
									.append("?")
									.append("limit=").append(limit);
									if (offset != null) {
										SEARCH_URL_WITH_INPUT.append("&offset=").append(offset);
									}
			SEARCH_URL_WITH_INPUT.append("&query=")
									.append("barcode=").append(HTML_QUOTES).append( URLEncoder.encode(input, "UTF-8")).append(HTML_STAR).append(HTML_QUOTES);
			return okapiCall(SEARCH_URL_WITH_INPUT.toString(),
					"GET",
					folioToken,
					null,
					folioConfig.getString(FolioConstants.FOLIO_TENANT),
					ACCEPT_JSON);
		} catch (Exception e) {			
			return null;
		}		
	}
	
	protected FolioResponseModel searchUserByCity (final String folioToken, int limit, final String city, List<String> patronGroups) {
		StringBuilder SEARCH_URL = new StringBuilder();
		SEARCH_URL.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(SEARCH_USER) 
		.append("?")
		.append("query=")
		.append(HTML_OPEN_PAR)
			.append("personal.addresses=").append(HTML_QUOTES).append(city).append(HTML_QUOTES)
			.append(HTML_SPACE).append("and").append(HTML_SPACE)
			.append(HTML_OPEN_PAR)
				.append("patronGroup=")
				.append(HTML_OPEN_PAR);
				int count = 0;
				for (String patronGroup : patronGroups) {
					if (count > 0) {
						SEARCH_URL.append(HTML_SPACE).append("or").append(HTML_SPACE);
					}
					SEARCH_URL.append(HTML_QUOTES).append(patronGroup).append(HTML_QUOTES);
					count++;
				}
				SEARCH_URL.append(HTML_CLOSE_PAR)
			.append(HTML_CLOSE_PAR)
		.append(HTML_CLOSE_PAR)
		.append("&limit=").append(limit);
		try {
			return okapiCall(SEARCH_URL.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		} catch (Exception e) {			
			return null;
		}

	}
	
	protected FolioResponseModel searchUserByPersonalData (final String folioToken, final FolioUserModel user) {
		StringBuilder SEARCH_URL = new StringBuilder();
		SEARCH_URL.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(SEARCH_USER) 
			.append("?")
			.append("query=")
			.append(HTML_OPEN_PAR)
				.append("personal.lastName=").append(user.getLastName())
				.append(HTML_SPACE).append("and").append(HTML_SPACE)
				.append("personal.firstName=").append(user.getFirstName())
			.append(HTML_CLOSE_PAR)
			.append(HTML_SPACE).append("and").append(HTML_SPACE)
			.append(HTML_OPEN_PAR)
				.append("personal.email=").append(user.getEmail());
			if (user.getFiscalCode() != null && !"".equals(user.getFiscalCode())) {
				SEARCH_URL.append(HTML_SPACE).append("or").append(HTML_SPACE)
				.append("externalSystemId=").append(user.getFiscalCode());
			}
			SEARCH_URL.append(HTML_CLOSE_PAR);
		try {
			return okapiCall(SEARCH_URL.toString(),
					"GET",
					folioToken,
					null,
					folioConfig.getString(FolioConstants.FOLIO_TENANT),
					ACCEPT_JSON);
		} catch (Exception e) {			
			return null;
		}
	}
	
	protected FolioResponseModel searchRequestByQuery (final String folioToken, final String query, int limit, int offset) throws FolioException {
		StringBuilder url = new StringBuilder();
		url.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(REQUEST_ENDPOINT)
			.append("?")
			.append("limit=").append(limit)
			.append("&offset=").append(offset);
		try {
			url.append("&query=").append(URLEncoder.encode(query, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return okapiCall(url.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);	
			
	}
	
	protected FolioResponseModel searchFolioUserByQuery (final String folioToken, final String query, int limit, int offset)  throws FolioException {
		StringBuilder SEARCH_URL_WITH_INPUT = new StringBuilder();
		SEARCH_URL_WITH_INPUT.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(SEARCH_USER) 
								.append("?")
								.append("limit=").append(limit);								
								SEARCH_URL_WITH_INPUT.append("&offset=").append(offset);								
		try {
			SEARCH_URL_WITH_INPUT.append("&query=").append(URLEncoder.encode(query, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return okapiCall(SEARCH_URL_WITH_INPUT.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);		
	}
	
	protected FolioResponseModel searchFolioUser (final String folioToken, final String input, final String limit, final String offset)  throws FolioException {
		StringBuilder SEARCH_URL_WITH_INPUT = new StringBuilder();
		SEARCH_URL_WITH_INPUT.append(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND)).append(SEARCH_USER) 
								.append("?")
								.append("limit=").append(limit);
								if (offset != null) {
									SEARCH_URL_WITH_INPUT.append("&offset=").append(offset);
								}
		SEARCH_URL_WITH_INPUT.append("&query=").append(HTML_OPEN_PAR);
		
		String [] tokens = input.split(" ");
		try {
			for (int i = 0; i < tokens.length; i ++) {
				String escapedUrl = URLEncoder.encode(tokens[i], "UTF-8");
				SEARCH_URL_WITH_INPUT.append(HTML_OPEN_PAR)
					.append("username=").append(HTML_QUOTES).append(escapedUrl).append(HTML_STAR).append(HTML_QUOTES) 
					.append(HTML_SPACE).append("or").append(HTML_SPACE).append("personal.firstName=").append(HTML_QUOTES).append(escapedUrl).append(HTML_STAR).append(HTML_QUOTES)
					.append(HTML_SPACE).append("or").append(HTML_SPACE).append("personal.lastName=").append(HTML_QUOTES).append(escapedUrl).append(HTML_STAR).append(HTML_QUOTES)
					.append(HTML_SPACE).append("or").append(HTML_SPACE).append("personal.email=").append(HTML_QUOTES).append(escapedUrl).append(HTML_STAR).append(HTML_QUOTES)
					.append(HTML_SPACE).append("or").append(HTML_SPACE).append("barcode=").append(HTML_QUOTES).append(escapedUrl).append(HTML_STAR).append(HTML_QUOTES)
					.append(HTML_SPACE).append("or").append(HTML_SPACE).append("id=").append(HTML_QUOTES).append(escapedUrl).append(HTML_STAR).append(HTML_QUOTES)
					.append(HTML_SPACE).append("or").append(HTML_SPACE).append("externalSystemId=").append(HTML_QUOTES).append(escapedUrl).append(HTML_STAR).append(HTML_QUOTES).append(HTML_CLOSE_PAR);
				if (i != tokens.length - 1) {
					SEARCH_URL_WITH_INPUT.append(HTML_SPACE).append("and").append(HTML_SPACE);	
				}
			}		
		} catch (Exception e) {
			
		}			 
		SEARCH_URL_WITH_INPUT.append(HTML_CLOSE_PAR);	

		return okapiCall(SEARCH_URL_WITH_INPUT.toString(),
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);		
	}	
	
	protected Map<String, String> getAllUserTypes (final String folioToken)  throws FolioException {
		return getUserTypes(okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + GET_USER_TYPES_ENDPOINT,
					"GET",
					folioToken,
					null,
					folioConfig.getString(FolioConstants.FOLIO_TENANT),
					ACCEPT_JSON));		
	}
	
	protected Map<String, String> getUserTypes (final FolioResponseModel folioResponse) {
		Map<String, String> result = new HashMap<String, String>();
		if (folioResponse != null) {
			JSONObject json = new JSONObject(folioResponse.getJsonResponse());
			JSONArray array = json.getJSONArray("usergroups");
			for (int j = 0; j < array.length(); j++) {
				JSONObject currentElement = array.getJSONObject(j);
				result.put(currentElement.getString("id"), currentElement.getString("desc"));
			}
		}
		return result;
	}
	
	protected List<String> searchPermissionByUserId (final String folioToken, final String input)  throws FolioException {
		List<String> result = new ArrayList<String>();
		final String SEARCH_PERMISSION_URL = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REGISTER_PERMISSION_ENDPOINT + 
											"/" + input + "/permissions?full=true&indexField=userId";		
		FolioResponseModel folioResponse = okapiCall(SEARCH_PERMISSION_URL,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		JSONObject json = new JSONObject(folioResponse.getJsonResponse());
		JSONArray array = json.getJSONArray("permissionNames");
		for (int i = 0; i < array.length(); i++) {
			JSONObject current = array.getJSONObject(i);
			result.add(current.getString("id"));			
		}
		return result;
	}
	
	/**
	 * 
	 * @param configuration
	 * @param folioToken
	 * @param input
	 * @return folio user without notes
	 * @throws FolioException
	 */
	
	protected FolioUserModel searchSimpleFolioUserbyId (final String folioToken, final String input,  boolean isCustomFieldActive)  throws FolioException {
		
		final String SEARCH_URL_WITH_INPUT = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_USER + 
								"/" + input ;
				
		FolioResponseModel  folioResponse = (okapiCall(SEARCH_URL_WITH_INPUT,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON));
		JSONObject json = new JSONObject(folioResponse.getJsonResponse());
		return getUserData(json, null, isCustomFieldActive);
	}
	
	/**
	 * 
	 * @param configuration
	 * @param folioToken
	 * @param input
	 * @return folio user with folio Notes
	 * @throws FolioException
	 */
	protected FolioUserModel searchCompleteFolioUserbyId (final String folioToken, final String input,  boolean isCustomFieldActive)  throws FolioException {
		
		final String SEARCH_URL_WITH_INPUT = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_USER + 
								"/" + input ;
				
		FolioResponseModel  folioResponse = (okapiCall(SEARCH_URL_WITH_INPUT,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON));
		JSONObject json = new JSONObject(folioResponse.getJsonResponse());
		FolioUserModel simpleUser = getUserData(json, null, isCustomFieldActive);
		if(!isCustomFieldActive)
			simpleUser.setCustomFieldFromNotes(getFolioNoteByUserId(simpleUser.getId(), folioToken));
		return simpleUser;
	}
	
	protected List<FolioUserModel> searchFolioUserbyId (final String folioToken, final String input,  boolean isCustomFieldActive)  throws FolioException {
		
		final String SEARCH_URL_WITH_INPUT = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_USER + 
								"?query=" +
								"id=" + HTML_QUOTES + input + HTML_QUOTES;
				
		List<FolioUserModel> userList = getUserList(okapiCall(SEARCH_URL_WITH_INPUT,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON), isCustomFieldActive);
		for (FolioUserModel user : userList) {
			user.setCustomFieldFromNotes(getFolioNoteByUserId(user.getId(), folioToken));
		}
		return userList;
	}
	
	/**
	 * Return a map of couples made up location name (for example: A-MAGAZZINO MODERNO PRESTABILE" and respective
	 * @param folioToken
	 * @return a map of sectionId and primaryServicePoints
	 * @throws FolioException
	 */
	protected  Map<String, String> getLocations (final String folioToken) throws FolioException {
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + LOCATIONS + 
				"?query=(details=*)&limit=50";
		
		return getSectionServicePoints(okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON));		
	}
	
	protected Map<String, String> getSectionServicePoints (final FolioResponseModel folioResponse) {
		Map<String, String> result = new HashMap<String, String>();
		JSONObject response = new JSONObject (folioResponse.getJsonResponse());
		JSONArray locations = response.getJSONArray("locations");
		for (int i = 0 ; i < locations.length() ; i++) {
			JSONObject currentElement = locations.getJSONObject(i);
			result.put(currentElement.getString("name"), currentElement.getString("primaryServicePoint"));			
		}		
		return result;
	}
	
	protected boolean closeLoan (final String folioToken, 
								final String itemBarcode, 
								final String servicePointId) throws FolioException {
		SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String now = date.format(new Date()); 
		
		JSONObject json = new JSONObject();
		json.put("itemBarcode", itemBarcode);
		json.put("checkInDate", now);
		json.put("servicePointId", servicePointId);
		
		FolioResponseModel folioResponse = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + CHECKIN_ENDPOINT,
				"POST",
				folioToken,
				json.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		Log.info("CLOSELOAN: " + " -itemBarcode: " + itemBarcode + " -servicePointId: " + servicePointId);
		return Integer.toString(folioResponse.getResponseCode()).startsWith("2");
	}
	
	protected boolean resetPassword ( final String folioToken, final String userId)  throws FolioException {
		
		StringBuilder body = new StringBuilder()
									.append("{")
										.append("\"userId\"").append(":").append("\"").append(userId).append("\"")										
									.append("}");

		
		FolioResponseModel folioResponse = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + RESET_PASSWORD_ENDPOINT,
				"POST",
				folioToken,
				body.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		
		return Integer.toString(folioResponse.getResponseCode()).startsWith("2");
	}
	
	protected FolioResponseModel sendCopyRequest( 
										final String note,
										final String copyBarcode, 
										final String itemId, 
										final String userId, 
										final String servicePoint,
										final String requestType,
										final String folioToken) throws FolioException {
		SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String now = date.format(new Date()); 
		
		JSONObject mainObject = new JSONObject();
		mainObject.put("requestType", requestType);
		mainObject.put("itemId", itemId);
		mainObject.put("requesterId", userId);
		mainObject.put("fulfilmentPreference", "Hold Shelf");
		mainObject.put("requestDate", now);
		mainObject.put("pickupServicePointId", servicePoint);
		if (note != null) {
			mainObject.put("patronComments", note);
		}
			
		
		JSONObject item = new JSONObject();
		item.put("barcode", copyBarcode);
		mainObject.put("item", item);				
			
		FolioResponseModel folioResponse = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REQUEST_ENDPOINT,
				"POST",
				folioToken,
				mainObject.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		//return Integer.toString(folioResponse.getResponseCode()).startsWith("2");
		Log.info("SENDCOPYREQUEST: " + " -itemId: " + itemId + " -requesterId: " + userId);
		return folioResponse;
	}
	
	protected FolioItemModel getCopyById(final String id, final String folioToken) throws FolioException {
		FolioItemModel item = null;
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + COPY_INFO_ENDPOINT +
				"/" + id;
		String folioResponse = okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getJsonResponse();
		item = new FolioItemModel();
		item.setFolioJson(new JSONObject(folioResponse));				
		return item;
	}
	
	protected FolioItemModel getCopyBybarcode(final String barcode, final String folioToken) throws FolioException {
		FolioItemModel item = null;
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + COPY_INFO_ENDPOINT +
				"?query=" +
				"barcode==" + barcode.replaceAll(" ", HTML_SPACE);
		String folioResponse = okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getJsonResponse();
		JSONObject json = new JSONObject(folioResponse);
		if (json.has("items")) {
			JSONArray items = json.getJSONArray("items");
			if (items.length() > 0) {
				item = getItem(items.getJSONObject(0));						
			}
		}
		return item;
	}
	
	/**
	 * deprecated 
	 */
	protected String getCopyInfo(final String barcode, final String folioToken) throws FolioException {
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + COPY_INFO_ENDPOINT +
				"?query=" +
				"barcode==" + barcode.replaceAll(" ", HTML_SPACE);
		
		return okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getJsonResponse();
		
	}
	
	protected String getFirstCopy(final String item) {
		JSONObject items = new JSONObject(item);
		return items.getJSONArray("items").get(0).toString();
	}
	
	protected boolean updateCopy(final String copy, final String folioToken) throws FolioException {
		JSONObject item = new JSONObject(copy);
		final String id = item.getString("id");
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + COPY_INFO_ENDPOINT + "/" + id;
		int responseCode = okapiCall(url,
				"PUT",
				folioToken,
				copy,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getResponseCode();
		Log.info("UPDATECOPY: " + "-itemId: " + id);
		return Integer.toString(responseCode).startsWith("2");
	}
	
	/**
	 * login with admin user
	 * @param configuration
	 * @return token
	 * @throws FolioException
	 */
	
	protected String loginFolioAdministrator () throws FolioException {
		
		final String adminUsername = folioConfig.getString(FolioConstants.FOLIO_USER);
		final String adminPassword = decrypt(folioConfig.getString(FolioConstants.FOLIO_PASS));  
		
		JSONObject mainObject = new JSONObject();
		mainObject.put("username", adminUsername);
		mainObject.put("password", adminPassword);
		
		return okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + ADMIN_LOGIN_ENDPOINT,
				"POST",
				 null,
				 mainObject.toString(),
				 folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON)
				.getOkapiToken();		
	}
	
	
	/**
	 * login 
	 * @param configuration
	 * @return token
	 * @throws FolioException
	 */
	
	protected FolioUserModel loginFolioUser (String username, String password, String adminToken,  boolean isCustomFieldActive) throws FolioException {
		
		JSONObject mainObject = new JSONObject();
		mainObject.put("username", username);
		mainObject.put("password", password);   
		
		FolioResponseModel folioResponse = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + LOGIN_ENDPOINT,
				"POST",
				 null,
				 mainObject.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
				
		FolioUserModel folioUser = getUser(folioResponse, folioResponse.getOkapiToken(), isCustomFieldActive);	
		folioUser.setServicePoints(getServicePointAvailable(folioUser.getPatronGroupCode()));
		folioUser.setPatronGroupName(getPatronGroupName(folioUser.getPatronGroupCode(), adminToken));
		return folioUser;
		
	}
	
	protected String getPatronGroupName(final String patronGroupId,  final String folioToken) throws FolioException {
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + GET_PATRON_GROUP + "/" + patronGroupId;
		FolioResponseModel folioJson = okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		JSONObject json = new JSONObject(folioJson.getJsonResponse());
		return json.getString("group");		
	}
	
	protected HashMap<String, String> getAllPatronGroup( final String folioToken) throws FolioException {
		HashMap<String, String> map = new HashMap<String, String>();
		FolioResponseModel folioJson = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + GET_PATRON_GROUP + "?limit=30",
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		JSONObject json = new JSONObject(folioJson.getJsonResponse());
		JSONArray group =  json.getJSONArray("usergroups");
		for (int i = 0 ; i < group.length() ; i++) {
			JSONObject currentGroup = group.getJSONObject(i);
			map.put(currentGroup.getString("id"), currentGroup.getString("desc"));
		}
		return map;
		
	}
	
	protected HashMap<String, String> getFolioNoteByUserId (String userId,  final String folioToken) throws FolioException {
		
			HashMap<String, String> noteMap = new HashMap<String, String>();
			
			String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + NOTE_LIST_ENDPOINT + "/" + userId + "?limit=20&status=assigned";
			
			FolioResponseModel folioJson = okapiCall(url,
												"GET",
												folioToken,
												null,
												folioConfig.getString(FolioConstants.FOLIO_TENANT),
												ACCEPT_JSON);
			JSONObject json = new JSONObject(folioJson.getJsonResponse());
			JSONArray notes =  json.getJSONArray("notes");
			for (int i = 0 ; i < notes.length() ; i++) {
				JSONObject currentNote = notes.getJSONObject(i);
				noteMap.put(currentNote.getString("typeId"), currentNote.getString("content"));
			}
			
			return noteMap;
		
	}
	
	protected String getUserMainServicePoint (final String userId, final String folioToken) throws FolioException {
		String defaultServicePoint = folioConfig.getString(FolioConstants.READ_ROOM);
		String url =  folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + USER_SERVICE_POINT_ENDPOINT + "?query=" +
				HTML_OPEN_PAR + "userId==" + userId + HTML_CLOSE_PAR;
		FolioResponseModel folioJson = okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		JSONObject json = new JSONObject(folioJson.getJsonResponse());
		JSONArray array = json.getJSONArray("servicePointsUsers");
		for (int i = 0 ; i < array.length() ; i++) {
			JSONObject currentServicePoint = array.getJSONObject(i);
			if (currentServicePoint.has("defaultServicePointId")) {
				defaultServicePoint = currentServicePoint.getString("defaultServicePointId");
			}
		}
		return defaultServicePoint;
	}
	
	protected JSONObject getFolioNoteByTypeId (String userId,  final String folioToken, final String typeId) throws FolioException {
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + NOTE_LIST_ENDPOINT + "/" + userId  + "?limit=20&status=assigned";
		FolioResponseModel folioJson = okapiCall(url,
				"GET",
				folioToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		JSONObject json = new JSONObject(folioJson.getJsonResponse());
		JSONArray notes =  json.getJSONArray("notes");
		for (int i = 0 ; i < notes.length() ; i++) {
			JSONObject currentNote = notes.getJSONObject(i);
			if (currentNote.getString("typeId").equals(typeId)) {
				return currentNote;
			}
		}
		return null;
	}
	
	private List<FolioItemModel> getItemList(FolioResponseModel folioJson) {
		List<FolioItemModel> result = new LinkedList<FolioItemModel> ();
		JSONObject json = new JSONObject(folioJson.getJsonResponse());
		JSONArray items = json.getJSONArray("items");
		for (int i = 0 ; i < items.length() ; i++) {
			JSONObject currentItem = items.getJSONObject(i);			
			result.add(getItem(currentItem));			
		}		
		return result;
	}
	
	private FolioItemModel getItem(JSONObject json) {
		FolioItemModel item = new FolioItemModel();
		item.setFolioJson(json);
		return item;
	}
		
	
	private FolioUserModel getUser (FolioResponseModel folioJson, String okapiToken,  boolean isCustomFieldActive) {
		
		JSONObject json = new JSONObject(folioJson.getJsonResponse());
		JSONObject userJson = json.getJSONObject("user");		
		FolioUserModel user = getUserData(userJson, okapiToken, isCustomFieldActive);	
		
		if (json.has("permissions")) {
			List<String> permissionListString = new ArrayList<String>();
			JSONObject permission = json.getJSONObject("permissions");
			JSONArray permmissionList = permission.getJSONArray("permissions");
			for (int i = 0; i < permmissionList.length(); i++) {
				permissionListString.add(permmissionList.getString(i));
			}
			user.setPermission(permissionListString);			
		}
		return user;
		
	}
	
	protected List<FolioUserModel> getUserList (FolioResponseModel folioJson, boolean isCustomFieldActive) {
		List<FolioUserModel> result = new LinkedList<FolioUserModel> ();
		JSONObject json = new JSONObject(folioJson.getJsonResponse());
		JSONArray users = json.getJSONArray("users");
		
		for (int i = 0 ; i < users.length() ; i++) {
			JSONObject currentUser = users.getJSONObject(i);			
			result.add(getUserData(currentUser, null, isCustomFieldActive));			
		}		
		return result;
	}

	private FolioUserModel getUserData(JSONObject currentUser, String okapiToken, boolean isCustomFieldActive) {
		FolioUserModel folioUser = new FolioUserModel();				
								
		if(okapiToken != null) {
			folioUser.setOkapiToken(okapiToken);
		}
		folioUser.setFolioJson(currentUser.toString());
		
		if(currentUser.has("username")) {
			folioUser.setUsername(currentUser.getString("username"));
		}
		if(currentUser.has("id")) {
			folioUser.setId(currentUser.getString("id"));
		}
		if(currentUser.has("externalSystemId")) {
			folioUser.setFiscalCode(currentUser.getString("externalSystemId"));
		}
		if(currentUser.has("patronGroup")) {
			folioUser.setPatronGroupCode(currentUser.getString("patronGroup"));
		}
		if(currentUser.has("barcode")) {
			folioUser.setBarcode(currentUser.getString("barcode"));		
		}
		if(currentUser.has("enrollmentDate")) {
			folioUser.setEnrollmentDate(currentUser.getString("enrollmentDate"));		
		}
		if(currentUser.has("expirationDate")) {
			folioUser.setExpireDate(currentUser.getString("expirationDate"));
		}		
		if(currentUser.has("active")) {
			folioUser.setActive(currentUser.getBoolean("active"));
		}		
		JSONObject personal = currentUser.getJSONObject("personal");
		
		if(personal.has("lastName")) {
			folioUser.setLastName(personal.getString("lastName"));
		}
		if(personal.has("firstName")) {
			folioUser.setFirstName(personal.getString("firstName"));
		}
		if(personal.has("phone")) {
			folioUser.setTelephone(personal.getString("phone"));
		}
		if(personal.has("mobilePhone")) {
			folioUser.setMobile(personal.getString("mobilePhone"));
		}
		if(personal.has("email")) {
			folioUser.setEmail(personal.getString("email"));
		}
		if(personal.has("dateOfBirth")) {
			folioUser.setDateOfBirth(personal.getString("dateOfBirth"));
		}
		
		JSONArray addresses = personal.getJSONArray("addresses");
		List<FolioAddress> addressList = new ArrayList<FolioAddress>();
		for (int j = 0; j < addresses.length(); j++) {
		    FolioAddress address = new FolioAddress();
		    JSONObject currentAddress = addresses.getJSONObject(j);
		    if (currentAddress.has("countryId")) {
		    	address.setCountryId(currentAddress.getString("countryId"));
		    } 
		    if (currentAddress.has("addressLine1")) {
		    	address.setAddressLine(currentAddress.getString("addressLine1"));
		    } 
		    if (currentAddress.has("city")) {
		    	address.setCity(currentAddress.getString("city"));
		    } 
		    if (currentAddress.has("region")) {
		    	address.setRegion(currentAddress.getString("region"));
		    } 
		    if (currentAddress.has("postalCode")) {
		    	address.setPostalCode(currentAddress.getString("postalCode"));
		    } 
		    if (currentAddress.has("addressTypeId")) {
		    	address.setAddressTypeId(currentAddress.getString("addressTypeId"));
		    } 	
		    if (currentAddress.has("primaryAddress")) {
		    	address.setMainAddress(currentAddress.getBoolean("primaryAddress"));
		    } 
		    addressList.add(address);		    
		}
		if (!addressList.isEmpty()) {
			folioUser.setAddress(addressList);
		}
		if(currentUser.has("active")) {
			folioUser.setActive(currentUser.getBoolean("active"));
		}
		
		
		
		if(isCustomFieldActive) {
			if(currentUser.has("customFields")) {
				JSONObject customFields = currentUser.getJSONObject("customFields");
				Gson gson = new Gson();
				HashMap<String, String> cFields= new HashMap<String,String> ();
				cFields = gson.fromJson(customFields.toString(), HashMap.class);
				folioUser.setCustomFields(cFields);
			}
		}
		return folioUser;
	}
	
	/**
	 * update few fields
	 * 
	 * @param configuration
	 * @param folioToken
	 * @param oldJson
	 * @param updatedUser
	 * @return
	 * @throws FolioException
	 */
	
	protected boolean updateFolioUser_2 (	
										final String folioToken,
										final String oldJson,
										FolioUserModel updatedUser) throws FolioException {
		
		JSONObject userJson = new JSONObject(oldJson);
		JSONObject personal = userJson.getJSONObject("personal");		
		if (updatedUser.getEmail() != null) {
			personal.put("email", updatedUser.getEmail());
		}
		
		int responseCode = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_USER + "/" + updatedUser.getId(),
				"PUT",
				folioToken,
				userJson.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				"text/plain").getResponseCode();
		return Integer.toString(responseCode).startsWith("2");
	}
	
	/**
	 * Update user data with the new expiration date
	 * @param folioToken
	 * @param oldJson
	 * @param updatedUser
	 * @return true if success
	 * @throws FolioException
	 */
	
	protected boolean updateExpirationUsere (final String folioToken, final String oldJson, FolioUserModel updatedUser) throws FolioException {
		JSONObject userJson = new JSONObject(oldJson);
		userJson.put("expirationDate", updatedUser.getExpireDate());
		userJson.put("active", updatedUser.isActive());
		userJson.put("patronGroup", updatedUser.getPatronGroupCode());
		int responseCode = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_USER + "/" + updatedUser.getId(),
				"PUT",
				folioToken,
				userJson.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				"text/plain").getResponseCode();
		return Integer.toString(responseCode).startsWith("2");
	}
		
	protected boolean changeUserGroup (
										final String folioToken,
										final FolioUserModel oldUser,
										final String newUserGroupCode) throws FolioException {
		JSONObject userJson = new JSONObject(oldUser.getFolioJson());						
		userJson.put("patronGroup", newUserGroupCode);
		int responseCode = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_USER + "/" + oldUser.getId(),
				"PUT",
				folioToken,
				userJson.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				"text/plain").getResponseCode();
		return Integer.toString(responseCode).startsWith("2");	
	}
	
	protected boolean deactiveUser (
			final String folioToken,
			final FolioUserModel oldUser) throws FolioException {
				JSONObject userJson = new JSONObject(oldUser.getFolioJson());						
				userJson.put("active", false);
				userJson.remove("expirationDate");				
				int responseCode = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_USER + "/" + oldUser.getId(),
				"PUT",
				folioToken,
				userJson.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				"text/plain").getResponseCode();
				return Integer.toString(responseCode).startsWith("2");	
	}
	
		
	/**
	 * DEPRECATED 
	 * @param configuration
	 * @param folioToken
	 * @param oldJson
	 * @param updatedUser
	 * @return
	 * @throws FolioException
	 */
	protected boolean updateFolioUser (
										final String folioToken,
										final String oldJson,
										FolioUserModel updatedUser,
										 boolean isCustomFieldActive) throws FolioException {
		
		JSONObject userJson = new JSONObject(oldJson);
		
		
		JSONObject personal = userJson.getJSONObject("personal");
		if (updatedUser.getEmail() != null) {
			personal.put("email", updatedUser.getEmail());
		}
		if (updatedUser.getTelephone() != null) {
			personal.put("phone", updatedUser.getTelephone());
		}
		if (updatedUser.getMobile() != null) {
			personal.put("mobilePhone", updatedUser.getMobile());
		}		
		if (updatedUser.getDateOfBirth() != null) {
			personal.put("dateOfBirth", updatedUser.getDateOfBirth());
		}
		if (updatedUser.getFiscalCode() != null) {
			userJson.put("externalSystemId", updatedUser.getFiscalCode());
		}
		if(updatedUser.getPatronGroupCode() != null) {
			userJson.put("patronGroup", updatedUser.getPatronGroupCode());
		}
		List<FolioAddress> list = updatedUser.getAddress();		
		if (list != null && list.size() > 0 
				&& list.get(0).getCountryId() != null 
				&& list.get(0).getAddressLine() != null 
				&& list.get(0).getCity() != null
				&& list.get(0).getRegion() != null
				&& list.get(0).getPostalCode() != null
				&& list.get(0).getAddressTypeId() != null) {
			JSONArray addressesArray = new JSONArray();
			JSONObject addressObject = new JSONObject();	
			addressObject.put("countryId", list.get(0).getCountryId());
			addressObject.put("addressLine1", list.get(0).getAddressLine());
			addressObject.put("city", list.get(0).getCity());
			addressObject.put("region", list.get(0).getRegion());
			addressObject.put("postalCode", list.get(0).getPostalCode());
			addressObject.put("addressTypeId", list.get(0).getAddressTypeId());			
			addressObject.put("primaryAddress", list.get(0).isMainAddress());
			addressesArray.put(addressObject);
			
			if (list != null && list.size() > 1 
					&& list.get(1).getCountryId() != null 
					&& list.get(1).getAddressLine() != null 
					&& list.get(1).getCity() != null
					&& list.get(1).getRegion() != null
					&& list.get(1).getPostalCode() != null
					&& list.get(1).getAddressTypeId() != null) {
				JSONObject addressObject2 = new JSONObject();	
				addressObject2.put("countryId", list.get(1).getCountryId());
				addressObject2.put("addressLine1", list.get(1).getAddressLine());
				addressObject2.put("city", list.get(1).getCity());
				addressObject2.put("region", list.get(1).getRegion());
				addressObject2.put("postalCode", list.get(1).getPostalCode());
				addressObject2.put("addressTypeId", list.get(1).getAddressTypeId());			
				addressObject2.put("primaryAddress", list.get(1).isMainAddress());
				addressesArray.put(addressObject2);
			}
			
			personal.put("addresses", addressesArray);
		}		
		//Custom Fields
		if(isCustomFieldActive) {
			if (userJson.has("customFields")) {
				JSONObject customFields = userJson.getJSONObject("customFields");
		
				HashMap<String, String> cFields = updatedUser.getCustomFields();
				if (cFields != null && cFields.size()> 1) {
					for (Map.Entry<String, String> entry : cFields.entrySet()) {
					    customFields.put(entry.getKey(),entry.getValue() );
					}
			
				}
			}
		}
		//update general note
		 if (updatedUser.getNotes() != null) {
				HashMap<String, String> folioNotesConstants = getFolioNotesConstants(isCustomFieldActive);
				
				for (String key : updatedUser.getNotes().keySet()) {
									
					updateNote(key, folioNotesConstants.get(key), updatedUser.getNotes().get(key), updatedUser.getId(), folioToken);
				}
		 }
				
		//update notes
		else if (updatedUser.getNotesFromCustomFields() != null) {
			HashMap<String, String> folioNotesConstants = getFolioNotesConstants(isCustomFieldActive);
			
			for (String key : updatedUser.getNotesFromCustomFields().keySet()) {
								
				updateNote(key, folioNotesConstants.get(key), updatedUser.getNotesFromCustomFields().get(key), updatedUser.getId(), folioToken);
			}
		}
		
		
		int responseCode = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_USER + "/" + updatedUser.getId(),
				"PUT",
				folioToken,
				userJson.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				"text/plain").getResponseCode();
		return Integer.toString(responseCode).startsWith("2");	
		
	}
	
	private boolean updateNote(String noteType, String title, String value, String userId,  String folioToken) throws FolioException {
		if (value == null) {
			//da definire: elimino nota esistente? Mantengo nota esistente?
			return true;
		}
		else {
			JSONObject note = getFolioNoteByTypeId(userId, folioToken, noteType);
			if (note != null) {
				note.put("content", value);
				okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + NOTE_CREATION_ENDPOINT + "/" + note.getString("id"),
						"PUT",
						folioToken,
						note.toString(),
						folioConfig.getString(FolioConstants.FOLIO_TENANT),
						ACCEPT_JSON);
				
				return true;
			}
			else {
				createNote(noteType, title, value, userId, folioToken);
			}
			
		}	
		return true;
	}
	
	/**
	 * Folio sign up.
	 * @param configuration
	 * @param folioToken  token of an admin user
	 * @param password
	 * @param username
	 * @param name
	 * @param surname
	 * @param email
	 * @param phone
	 * @param nation
	 * @param mobile
	 * @return true if user creation successed
	 * @throws FolioException
	 */
	protected boolean createFolioUser ( final String folioToken, 
										final String password,
										FolioUserModel user,
										 boolean isCustomFieldActive ) throws FolioException {		
				
		boolean baseUserResponse = createBaseUser(folioToken, user, isCustomFieldActive);
		boolean permissionResponse = false;
		boolean credentialResponse = false;
		if (baseUserResponse) {					
			permissionResponse = createPermission(folioToken, user);		
			
			if (user.getUsername() != null) {
				credentialResponse = createCredentials(folioToken, password, user);
			}
			//atenzione a questa parte, da capire il funzionamento della nota
			if (user.getNotes() != null && user.getNotes().get(folioConfig.getString(FolioConstants.NOTE_NOTE_ID)) != null && !"".equals(user.getNotes().get(folioConfig.getString(FolioConstants.NOTE_NOTE_ID)).trim())) {
				createNote(folioConfig.getString(FolioConstants.NOTE_NOTE_ID), folioConfig.getString(FolioConstants.NOTE_NOTE_DESC), user.getNotes().get(folioConfig.getString(FolioConstants.NOTE_NOTE_ID)), user.getId(), folioToken);
			}
			
			if(!isCustomFieldActive) {
				if (user.getNotesFromCustomFields() != null && user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_JOB_ID)) != null && !"".equals(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_JOB_ID)).trim())) {
					createNote(folioConfig.getString(FolioConstants.NOTE_JOB_ID), folioConfig.getString(FolioConstants.NOTE_JOB_DESC), user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_JOB_ID)), user.getId(), folioToken);
				}
				if (user.getNotesFromCustomFields() != null && user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_NATION_ID)) != null && !"".equals(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_NATION_ID)).trim())) {
					createNote(folioConfig.getString(FolioConstants.NOTE_NATION_ID), folioConfig.getString(FolioConstants.NOTE_NATION_DESC), user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_NATION_ID)), user.getId(), folioToken);
				}
				if (user.getNotesFromCustomFields() != null && user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID)) != null && !"".equals(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID)).trim())) {
					createNote(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID), folioConfig.getString(FolioConstants.NOTE_DOCUMENT_DESC), user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID)), user.getId(), folioToken);
				}
				if (user.getNotesFromCustomFields() != null && user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID)) != null && !"".equals(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID)).trim())) {
					createNote(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID), folioConfig.getString(FolioConstants.NOTE_GUARDIAN_DESC), user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID)), user.getId(), folioToken);
				}
				if (user.getNotesFromCustomFields() != null && user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID)) != null && !"".equals(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID)).trim())) {
					createNote(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID), folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_DESC), user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID)), user.getId(), folioToken);
				}
				if (user.getNotesFromCustomFields() != null && user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_GENDER_ID)) != null && !"".equals(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_GENDER_ID)).trim())) {
					createNote(folioConfig.getString(FolioConstants.NOTE_GENDER_ID), folioConfig.getString(FolioConstants.NOTE_GENDER_DESC), user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_GENDER_ID)), user.getId(), folioToken);
				}
				if (user.getNotesFromCustomFields() != null && user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID)) != null && !"".equals(user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID)).trim())) {
					createNote(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID), folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_DESC), user.getNotesFromCustomFields().get(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID)), user.getId(), folioToken);
				}
			}
			
		}
		return (baseUserResponse && permissionResponse);
	}

	
	private boolean createBaseUser(final String folioToken, 
									FolioUserModel user,
									boolean isCustomFieldActive) throws FolioException {		
		/*
		 * Main object json
		 */
		JSONObject mainObject = new JSONObject();
		mainObject.put("id", user.getId());
		mainObject.put("patronGroup", folioConfig.getString(FolioConstants.USER_PREREG));
		mainObject.put("active", true);
		mainObject.put("barcode", user.getBarcode());
		SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		//SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");
		String now = date.format(new Date());
		mainObject.put("enrollmentDate", now);
		if (user.getUsername() != null) { 
			mainObject.put("username", user.getUsername());			
		}	
		if (user.getFiscalCode() != null) {
			mainObject.put("externalSystemId", user.getFiscalCode());
		}
		
		/*
		 * Custom Fields
		 */
		if(isCustomFieldActive) {
			HashMap<String, String> cFields = user.getCustomFields();
			if (user.getCustomFields() != null && user.getCustomFields().size()> 1) {
				JSONObject customFields = new JSONObject();
				for (Map.Entry<String, String> entry : cFields.entrySet()) {
				    customFields.put(entry.getKey(),entry.getValue() );
				}
				mainObject.put("customFields",customFields);
			}
		}
		
		/*
		 * Personal json
		 */
		JSONObject personal = new JSONObject();
		personal.put("lastName", user.getLastName());
		if (user.getUsername() != null) {
			personal.put("firstName", user.getFirstName());
		}
		if (user.getTelephone() != null) { 
			personal.put("phone", user.getTelephone());
		}
		if (user.getMobile() != null) { 
			personal.put("mobilePhone", user.getMobile());
		}
		personal.put("dateOfBirth", user.getDateOfBirth());
		personal.put("preferredContactTypeId", "002");
		personal.put("email", user.getEmail());
		
		/*
		 * Addresses json
		 */
		JSONArray addresses = new JSONArray();
		if (user.getAddress() != null && user.getAddress().size()> 0) {
			FolioAddress firstAddress = user.getAddress().get(0);
			JSONObject address1 = new JSONObject();
			address1.put("addressLine1", firstAddress.getAddressLine());
			address1.put("city", firstAddress.getCity());
			address1.put("region", firstAddress.getRegion());
			address1.put("countryId", firstAddress.getCountryId());
			address1.put("addressTypeId", firstAddress.getAddressTypeId());
			address1.put("postalCode", firstAddress.getPostalCode());
			address1.put("primaryAddress", firstAddress.isMainAddress());
			addresses.put(address1);
		}
		if (user.getAddress() != null && user.getAddress().size()> 1) {
			FolioAddress secondAddress = user.getAddress().get(1);
			JSONObject address2 = new JSONObject();
			address2.put("addressLine1", secondAddress.getAddressLine());
			address2.put("city", secondAddress.getCity());
			address2.put("region", secondAddress.getRegion());
			address2.put("countryId", secondAddress.getCountryId());
			address2.put("addressTypeId", secondAddress.getAddressTypeId());
			address2.put("postalCode", secondAddress.getPostalCode());
			address2.put("primaryAddress", secondAddress.isMainAddress());
			addresses.put(address2);			
		}		
		
		
		personal.put("addresses", addresses);
		
		mainObject.put("personal", personal);
									
		
		int registerResponseCode = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REGISTER_ENDPOINT,
				"POST",
				folioToken,
				mainObject.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON)
				.getResponseCode();	
		return Integer.toString(registerResponseCode).startsWith("2");
	}
	
	/**
	 * Create folio Item when Holding doesn't exsist. At first it create the holding and then the item
	 * @param folioToken user access token	
	 * @param record	
	 * @return item created
	 * @throws FolioException
	 */
	protected FolioItemModel createItemWithHolding (final String folioToken,
			FolioRecord record) throws FolioException {	
		String holdingId = createHolding(folioToken, record);
		return createItem(folioToken, record, holdingId);
	}
	
	/**
	 * Create an Item when holding already exsist
	 * @param folioToken user access Token
	 * @param record 
	 * @param holdingId id of exsisting holding
	 * @return item created
	 * @throws FolioException
	 */
	protected FolioItemModel createItem (final String folioToken,
						FolioRecord record,
						final String holdingId) throws FolioException {	
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + COPY_INFO_ENDPOINT;
		JSONObject body = new JSONObject();
		JSONObject status = new JSONObject();
		status.put("name", "Available");
		body.put("status", status);
		JSONObject permanentLoanType = new JSONObject();
		permanentLoanType.put("id", record.getLoanType());
		JSONObject materialType = new JSONObject();
		materialType.put("id", record.getItemType());
		body.put("holdingsRecordId", holdingId);
		body.put("barcode", record.getBarcode());
		body.put("permanentLoanType", permanentLoanType);
		body.put("materialType", materialType);
		
		if (record.getNote() != null && !"".equals(record.getNote())) {
			JSONArray notes = new JSONArray();
			JSONObject note = new JSONObject();
			note.put("note", record.getNote());
			note.put("itemNoteTypeId", folioConfig.getString(FolioConstants.GENERALE_NOTE_TYPE));
			notes.put(note);
			body.put("notes", notes);
		}
		
		if (record.getVolume() != null) {
			body.put("volume", record.getVolume());
		}
		if (record.getEnumeration() != null) {
			body.put("enumeration", record.getEnumeration());
		}
		String folioResponse = okapiCall(url,
				"POST",
				folioToken,
				body.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getJsonResponse();
		FolioItemModel item = new FolioItemModel();
		item.setFolioJson(new JSONObject(folioResponse));		
		Log.info("CREATEITEM: " + "-holdingId: " + holdingId + " -recordBarcode" + record.getBarcode());
		return item; 
	}
	
	protected String createHolding(final String folioToken,
										FolioRecord record) throws FolioException {	
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_HOLDINGS;
		JSONObject body = new JSONObject();
		body.put("callNumber", record.getCollocation());
		body.put("instanceId", record.getInstanceId());
		body.put("holdingsTypeId", record.getHoldingType());
		body.put("permanentLocationId", record.getHoldingSection());
		Log.info("CREATEHOLDING: " + "-instanceId: " +  record.getInstanceId() + " -recordBarcode" + record.getBarcode());
		return okapiCall(url,
				"POST",
				folioToken,
				body.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getResultHeader();		
	}
	
	protected String createInstance(final String folioToken,
										FolioRecord record) throws FolioException {	
		
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_INSTANCES;
		JSONObject body = new JSONObject();
		body.put("catalogedDate", record.getCatalogDate());
		body.put("statusId", folioConfig.getString(FolioConstants.STATUS_OTHER_LOCALE));
		body.put("title", record.getTitle());
		body.put("modeOfIssuanceId", record.getBibliographicLevel());
		body.put("instanceTypeId", record.getResourceType());
		if (record.getAuthorName() != null) {
			JSONArray contributors = new JSONArray();
			JSONObject author = new JSONObject();
			author.put("name", record.getAuthorName());
			author.put("contributorNameTypeId", record.getAuthorType());
			contributors.put(author);
			body.put("contributors", contributors);
		}		
		String publisherName = record.getPublisherName();
		String publisherPlace = record.getPlace();
		String publisherDate = record.getPublicationDate();
		if (publisherName != null || publisherPlace != null || publisherDate != null) {
			JSONArray publication = new JSONArray();
			JSONObject publisher = new JSONObject();
			if (publisherName != null) {
				publisher.put("publisher", publisherName);
			}
			if (publisherPlace != null) {
				publisher.put("place", publisherPlace);
			}
			if (publisherDate != null) {
				publisher.put("dateOfPublication", record.getPublicationDate());
			}
			publication.put(publisher);
			body.put("publication", publication);
		}
		
		body.put("source", FolioConstants.SOURCE_OPAC);
		
//		JSONArray notes = new JSONArray();
//		JSONObject note = new JSONObject();
//		note.put("note", record.getNote());
//		note.put("instanceNoteTypeId", folioConfig.getString(FolioConstants.GENERALE_NOTE_TYPE));
//		notes.put(note);
//		body.put("notes", notes);	
		
		Log.info("CREATEINSTANCE: " + " -recordBarcode" + record.getBarcode());
		return okapiCall(url,
				"POST",
				folioToken,
				body.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getResultHeader();		
	}
	
	private boolean createPermission(final String folioToken, 
									FolioUserModel user) throws FolioException {		
		/*
		 * Main object json
		 */
		JSONObject mainObject = new JSONObject();
		mainObject.put("userId", user.getId());
		
		int permissionResponseCode = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REGISTER_PERMISSION_ENDPOINT,
				"POST",
				folioToken,
				mainObject.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON)
				.getResponseCode();
		return Integer.toString(permissionResponseCode).startsWith("2");
	}
	
	private boolean createCredentials(final String folioToken, 
									final String password,
									FolioUserModel user) throws FolioException {
		/*
		 * Main object json
		 */
		JSONObject mainObject = new JSONObject();
		mainObject.put("username", user.getUsername());
		mainObject.put("id", user.getId());
		mainObject.put("password", password);
		
		//TO-DO capture response
		int credentialResponseCode = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + REGISTER_CREDENTIAL_ENDPOINT,
						"POST",
						folioToken,
						mainObject.toString(),
						folioConfig.getString(FolioConstants.FOLIO_TENANT),
						ACCEPT_JSON)
						.getResponseCode();	
		return Integer.toString(credentialResponseCode).startsWith("2");
	}
	
	
	private boolean createNote(String noteType, String title, String value, String userId, String adminToken) throws FolioException  {
		/*
		 * Main object json
		 */
		JSONObject mainObject = new JSONObject();
		mainObject.put("domain", "users");
		mainObject.put("typeId", noteType);
		mainObject.put("content", value);
		mainObject.put("title", title);
		mainObject.put("id", UUID.randomUUID());
		
		JSONArray links = new JSONArray();
		JSONObject link = new JSONObject();
		link.put("type", "user");
		link.put("id", userId);
		links.put(link);
		
		mainObject.put("links", links);		
		
		FolioResponseModel folioResponse = okapiCall(folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + NOTE_CREATION_ENDPOINT,
				"POST",
				adminToken,
				mainObject.toString(),
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON);
		int responseCode = folioResponse.getResponseCode();
		return String.valueOf(responseCode).startsWith("2");
	}
	
	protected boolean resetItemDeposit(String id, String newitem, String adminToken) throws FolioException {
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + COPY_INFO_ENDPOINT + "/" + id;
		int responseCode = okapiCall(url,
				"PUT",
				adminToken,
				newitem,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getResponseCode();
		Log.info("RESETITEMDEPOSIT: " + " -id" + id + " - newItem: " + newitem);
		return String.valueOf(responseCode).startsWith("2"); 
	}
	
	protected List<JSONObject> getItemWithLoanType(String statusQuery, String adminToken) throws FolioException {
		List<JSONObject> results = new ArrayList<JSONObject>();
		String statusQueryEncode = statusQuery;
		try {
			statusQueryEncode = URLEncoder.encode(statusQuery, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//do nothing
		}
		
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + COPY_INFO_ENDPOINT + 
				"?query=" + HTML_OPEN_PAR + "permanentLoanTypeId=" + HTML_QUOTES + folioConfig.getString(FolioConstants.LOAN_TYPE_LOAN_ID) + HTML_QUOTES + 
				HTML_SPACE + statusQueryEncode + 
				HTML_CLOSE_PAR + 
				"&limit=500";
		String json = okapiCall(url,
				"GET",
				adminToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getJsonResponse();
		JSONObject jsonObject = new JSONObject(json);
		JSONArray array = jsonObject.getJSONArray("items");
		for(int i = 0 ; i < array.length() ; i++){
		    JSONObject current = array.getJSONObject(i);
		    results.add(current);
		}
		return results;
	}
	
	protected List<JSONObject> getItemInDeposit(String adminToken) throws FolioException {
		String checkedOutEncode;
		try {
			checkedOutEncode = URLEncoder.encode(folioConfig.getString(FolioConstants.STATUS_CHECKED_OUT), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			checkedOutEncode = folioConfig.getString(FolioConstants.STATUS_CHECKED_OUT);
		}
		List<JSONObject> results = new ArrayList<JSONObject>();
		String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + COPY_INFO_ENDPOINT + 
				"?query=" + HTML_OPEN_PAR + 
				"temporaryLocationId" + HTML_EQUAL + HTML_QUOTES + folioConfig.getString(FolioConstants.TEMPORARY_LOCATION_STORAGE_AREA_ID) +  HTML_QUOTES + HTML_SPACE +
				"not" + HTML_SPACE + "status" + HTML_EQUAL + HTML_QUOTES + checkedOutEncode + HTML_QUOTES + HTML_CLOSE_PAR
				+ "&limit=100" ;
		String json = okapiCall(url,
				"GET",
				adminToken,
				null,
				folioConfig.getString(FolioConstants.FOLIO_TENANT),
				ACCEPT_JSON).getJsonResponse();
		JSONObject jsonObject = new JSONObject(json);
		JSONArray array = jsonObject.getJSONArray("items");
		for(int i = 0 ; i < array.length() ; i++){
		    JSONObject current = array.getJSONObject(i);
		    results.add(current);
		}
		return results;
	}
	
	/**
	 * http call to okapi services
	 * @param url
	 * @param method
	 * @param accessToken
	 * @param body
	 * @param tenant
	 * @return an object with response
	 * @throws FolioException
	 */
	
	private FolioResponseModel okapiCall (final String url, 
								final String method, 
								final String accessToken, 
								final String body, 
								final String tenant,
								final String accept) throws FolioException {
		FolioResponseModel folioResponse = new FolioResponseModel();
		
		StringBuffer datas = new StringBuffer();	
			
		try {		
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setConnectTimeout(90*1000);
			con.setReadTimeout(90*1000);
				
			con.setRequestMethod(method);
			con.setDoOutput(true);			
			
			/* ======== header ======= */
			con.setRequestProperty("Accept", accept);
			con.setRequestProperty("Content-type", "application/json");
			con.setRequestProperty("X-Okapi-Tenant", tenant);
			if (accessToken != null) {
				con.setRequestProperty("x-okapi-token", accessToken);
			}
			
			/* send body */
			if(body != null) {
				OutputStream os = con.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
				osw.write(body);
				osw.flush();
				osw.close();
				os.close();  
				con.connect();
			}			
		
			folioResponse.setResponseCode(con.getResponseCode());
			folioResponse.setOkapiToken(con.getHeaderField("x-okapi-token"));
			folioResponse.setResultHeader(con.getHeaderField("Location"));
			
			String folioError = getFolioError(con);
			
			if (folioError != null) {
				folioResponse.setErrorMessage(folioError);				
				FolioException folioException = new FolioException(folioError);				
				folioException.printStackTrace();
				throw folioException;				
			}	    
			
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream(), "UTF-8"));
			String inputLine;		
	
			while ((inputLine = in.readLine()) != null) {
				datas.append(inputLine).append("\n");
			}			
			in.close();		
			
			folioResponse.setJsonResponse(datas.toString());
			int numec = 0;
			if (datas.toString() != null && !"".equals(datas.toString().trim())) {
				JSONObject jsonResp = new JSONObject(datas.toString());				
				if (jsonResp.has("totalRecords")) {
					numec = jsonResp.getInt("totalRecords");
				}	
			}
			folioResponse.setTotalRecords(numec);			
			
		} catch (java.net.SocketTimeoutException e) {		   
		   e.printStackTrace();		  
		   return null;
		} catch (java.io.IOException e) {			   
		   e.printStackTrace();		
		   return null;		     
		}		
		
		return folioResponse;
	}
	
	/**
	 * get errors in folio response okapi
	 * @param con
	 * @return string with errors message
	 */
	
	private String getFolioError (HttpURLConnection con ) {
		InputStream errorStream = con.getErrorStream();
		StringBuffer errors = new StringBuffer();
	    if (errorStream != null) {
	    	try {
		    	BufferedReader inError = new BufferedReader( new InputStreamReader(errorStream));
				String inputError;	
				while ((inputError = inError.readLine()) != null) {
					errors.append(inputError).append("\n");
				}			
				inError.close();			
				
				JSONObject obj = new JSONObject(errors.toString());					
				
				// some json have structure: { "errorMessage" : "error ...."}
				// others have structure: { "errors" : [ { "message" : "error...." } ]}
				
				String errorMessage = obj.getString("errorMessage");
				if (errorMessage == null) {				
					JSONArray array = obj.getJSONArray("errors");
					for(int i = 0 ; i < array.length() ; i++){
					    String message = array.getJSONObject(i).getString("message");
					    return (message != null) ? message : errors.toString();
					}
				}
				else 
					return errorMessage;				
	    	}
	    	catch (JSONException jsonException) {
				return errors.toString();
			}
	    	catch (Exception e) {
	    		e.printStackTrace();
	    		return null;
	    	}
	    } 
	    return null;
	}
	
	
	
	/**
     * Method for Encrypt Plain String Data
     * @param plainText
     * @return encryptedText
     */
    private String encrypt(String plainText) {
        String encryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            byte[] key = ENCRYPTION_KEY.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF8"));            
            encryptedText = DatatypeConverter.printBase64Binary(cipherText);

        } catch (Exception E) {
             System.err.println("Encrypt Exception: " + E.getMessage());
        }
        return encryptedText;
    }

    /**
     * Method For Get encryptedText and Decrypted provided String
     * @param encryptedText
     * @return decryptedText
     */
    private String decrypt(String encryptedText) {
        String decryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            byte[] key = ENCRYPTION_KEY.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparameterspec);            
            byte[] cipherText = DatatypeConverter.parseBase64Binary(encryptedText);
            decryptedText = new String(cipher.doFinal(cipherText), "UTF-8");

        } catch (Exception E) {
            System.err.println("decrypt Exception: " + E.getMessage());
        }
        return decryptedText;
    }
    
    
    protected String getFolioErrorCode(final String errorJson) {    	
    	if (errorJson != null) {
    		try {
    			JSONObject json = new JSONObject(errorJson);
    			return json.getJSONArray("errors").getJSONObject(0).getString("code");
    		}
    		catch (Exception E) {
    			//do nothing
    		}
    		try {
    			JSONObject json = new JSONObject(errorJson);
    			return json.getJSONArray("errors").getJSONObject(0).getString("message");
    		}
    		catch (Exception E) {
    			//do nothing
    		}
    	}
    	return errorJson;
    }
    
    protected String getFolioErrorMessage(final String errorJson) {
    	if (errorJson != null) {
    		try {
    			JSONObject json = new JSONObject(errorJson);
    			return json.getJSONArray("errors").getJSONObject(0).getString("message");
    		}
    		catch (Exception E) {
    			return errorJson;
    		}
    	}
    	return errorJson;
    }
    
    
    protected HashMap getFolioNotesConstants(boolean isCustomFieldActive) {
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put(folioConfig.getString(FolioConstants.NOTE_NOTE_ID), folioConfig.getString(FolioConstants.NOTE_NOTE_DESC));	
    	if(!isCustomFieldActive) {
	    	map.put(folioConfig.getString(FolioConstants.NOTE_NOTE_ID), folioConfig.getString(FolioConstants.NOTE_NOTE_DESC));
	    	map.put(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID), folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_DESC));
	    	map.put(folioConfig.getString(FolioConstants.NOTE_NATION_ID), folioConfig.getString(FolioConstants.NOTE_NATION_DESC));
	    	map.put(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID), folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_DESC));
	    	map.put(folioConfig.getString(FolioConstants.NOTE_GENDER_ID), folioConfig.getString(FolioConstants.NOTE_GENDER_DESC));
	    	map.put(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID), folioConfig.getString(FolioConstants.NOTE_DOCUMENT_DESC));
	    	map.put(folioConfig.getString(FolioConstants.NOTE_JOB_ID), folioConfig.getString(FolioConstants.NOTE_JOB_DESC));
	    	map.put(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID), folioConfig.getString(FolioConstants.NOTE_GUARDIAN_DESC));
	    	map.put(folioConfig.getString(FolioConstants.NOTE_TAG_RFID_ID), folioConfig.getString(FolioConstants.NOTE_TAG_RFID_DESC));    	
    	}
    	
    	return map;
    }
    
    
    protected Map<String, NameValuePair []> getServicePointAvailable(final String userType) {
    	HashMap<String, NameValuePair []> result = new LinkedHashMap<String, NameValuePair []>();
    	
    	if (folioConfig.getString(FolioConstants.USER_PREREG).equals(userType)) {	   		
    		result.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.SERIAL_MOD_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});    	
    		result.put(folioConfig.getString(FolioConstants.SERIAL_AN_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.MUSIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});
    		result.put(folioConfig.getString(FolioConstants.JOURNAL_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});   
    		result.put(folioConfig.getString(FolioConstants.THESIS_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MICROFILM_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});    		
    		result.put(folioConfig.getString(FolioConstants.ELETRONIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		});   
    	}
    	else if (folioConfig.getString(FolioConstants.USER_BASE).equals(userType)) {	   		
    		result.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.SERIAL_MOD_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});    		
    		result.put(folioConfig.getString(FolioConstants.JOURNAL_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});   
    		result.put(folioConfig.getString(FolioConstants.THESIS_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MICROFILM_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});    		
    		result.put(folioConfig.getString(FolioConstants.ELETRONIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC))
    		});   
    	}
    	else if (folioConfig.getString(FolioConstants.USER_BASE_LOAN).equals(userType)) {
    		result.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.LOAN_ROOM), folioConfig.getString(FolioConstants.LOAN_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.SERIAL_MOD_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});    		
    		result.put(folioConfig.getString(FolioConstants.JOURNAL_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});   
    		result.put(folioConfig.getString(FolioConstants.THESIS_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MICROFILM_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});    		
    		result.put(folioConfig.getString(FolioConstants.ELETRONIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC))
    		});   
    	}
    	else if (folioConfig.getString(FolioConstants.USER_MUSIC).equals(userType)) {
    		
    		result.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});      		
    		result.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});     		
    		result.put(folioConfig.getString(FolioConstants.SERIAL_MOD_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.SERIAL_AN_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.MUSIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});
    		result.put(folioConfig.getString(FolioConstants.JOURNAL_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});   
    		result.put(folioConfig.getString(FolioConstants.THESIS_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MICROFILM_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});    		
    		result.put(folioConfig.getString(FolioConstants.ELETRONIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		}); 	
    		
    	}
    	else if (folioConfig.getString(FolioConstants.USER_MUSIC_LOAN).equals(userType)) {
    		result.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.LOAN_ROOM), folioConfig.getString(FolioConstants.LOAN_ROOM_DESC))
    		});      		
    		result.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});     		
    		result.put(folioConfig.getString(FolioConstants.SERIAL_MOD_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.SERIAL_AN_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.MUSIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});
    		result.put(folioConfig.getString(FolioConstants.JOURNAL_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});   
    		result.put(folioConfig.getString(FolioConstants.THESIS_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MICROFILM_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});    		
    		result.put(folioConfig.getString(FolioConstants.ELETRONIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		}); 
    	}
    	else if (folioConfig.getString(FolioConstants.USER_HANDWRITE).equals(userType)) {
    		result.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});     		
    		result.put(folioConfig.getString(FolioConstants.SERIAL_MOD_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.SERIAL_AN_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.MUSIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});
    		result.put(folioConfig.getString(FolioConstants.JOURNAL_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.HANDWRITE_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.THESIS_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MICROFILM_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});    		
    		result.put(folioConfig.getString(FolioConstants.ELETRONIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		}); 
    	}
    	else if (folioConfig.getString(FolioConstants.USER_HANDWRITE_LOAN).equals(userType)) {
    		result.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.LOAN_ROOM), folioConfig.getString(FolioConstants.LOAN_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});     		
    		result.put(folioConfig.getString(FolioConstants.SERIAL_MOD_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.SERIAL_AN_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.MUSIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});
    		result.put(folioConfig.getString(FolioConstants.JOURNAL_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.HANDWRITE_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.THESIS_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MICROFILM_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC))
    		});    		
    		result.put(folioConfig.getString(FolioConstants.ELETRONIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC))
    		}); 
    	}
    	else if (folioConfig.getString(FolioConstants.USER_STAFF).equals(userType) || folioConfig.getString(FolioConstants.USER_ADMIN).equals(userType) ) {
    		result.put(folioConfig.getString(FolioConstants.MONOG_MOD_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),    				
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.LOAN_ROOM), folioConfig.getString(FolioConstants.LOAN_ROOM_DESC))
    		}); 		
    		result.put(folioConfig.getString(FolioConstants.MONOG_AN_NOT_PREST_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),    				
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});     		
    		result.put(folioConfig.getString(FolioConstants.SERIAL_MOD_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),    				
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.SERIAL_AN_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),    				
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.MUSIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});
    		result.put(folioConfig.getString(FolioConstants.JOURNAL_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});  
    		result.put(folioConfig.getString(FolioConstants.HANDWRITE_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		}); 
    		result.put(folioConfig.getString(FolioConstants.THESIS_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		});   		
    		result.put(folioConfig.getString(FolioConstants.MICROFILM_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),    				
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    				
    		});    		
    		result.put(folioConfig.getString(FolioConstants.ELETRONIC_CODE), new NameValuePair [] {
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.READ_ROOM), folioConfig.getString(FolioConstants.READ_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.SERIAL_ROOM), folioConfig.getString(FolioConstants.SERIAL_ROOM_DESC)),    				
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.CONSULT_ROOM), folioConfig.getString(FolioConstants.CONSULT_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.HANDWRITE_ROOM), folioConfig.getString(FolioConstants.HANDWRITE_ROOM_DESC)),
    				new BasicNameValuePair(folioConfig.getString(FolioConstants.MUSIC_ROOM), folioConfig.getString(FolioConstants.MUSIC_ROOM_DESC))
    		}); 
    	}
    	
    	return result;
    }
        
    protected boolean isCopyPaged(String folioResponse) {
    	try {
    		JSONObject json = new JSONObject(folioResponse);
    		return folioConfig.getString(FolioConstants.STATUS_PAGED).equals(json.getJSONObject("status").get("name"));
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    protected boolean isCopyAvailable(String folioResponse) {
    	try {
    		JSONObject json = new JSONObject(folioResponse);
    		return folioConfig.getString(FolioConstants.AVAILABLE).equals(json.getJSONObject("status").get("name"));
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    public String getLoanDate(String folioResponse) {
    	JSONObject json = new JSONObject(folioResponse);
    	String result = null;
    	try {
    		result = json.getJSONArray("loans").getJSONObject(0).getString("dueDate");
    	}
    	catch (Exception e) {
    		
    	}
    	return result;
    }
    
    public void changeLoanTypeAsServicePoint ( final String folioToken, final String item, final String servicePoint) {
    	String loanTypeId;
		String loanTypeDesc;			
		if (folioConfig.getString(FolioConstants.LOAN_ROOM).equals(servicePoint)) {
			loanTypeId = folioConfig.getString(FolioConstants.LOAN_TYPE_LOAN_ID);
			loanTypeDesc = folioConfig.getString(FolioConstants.LOAN_TYPE_LOAN_DESCR);
		}
		else {
			loanTypeId = folioConfig.getString(FolioConstants.LOAN_TYPE_CONS_ID);
			loanTypeDesc = folioConfig.getString(FolioConstants.LOAN_TYPE_CONS_DESCR);
		}
		changeLoanType(folioToken, item, loanTypeId, loanTypeDesc);
    }
    
    public void resetLoanType ( final String folioToken, final String item) {    	
		final String loanTypeId = folioConfig.getString(FolioConstants.LOAN_TYPE_CONS_ID);
		final String loanTypeDesc = folioConfig.getString(FolioConstants.LOAN_TYPE_CONS_DESCR);		
		changeLoanType(folioToken, item, loanTypeId, loanTypeDesc);
    }
    
    public void changeLoanType( final String folioToken, final String item, final String loandId, final String loanDescr) {
    	JSONObject itemJson = new JSONObject(item);
    	try {
    		JSONObject loanType = new JSONObject();
    		loanType.put("id", loandId);
    		loanType.put("name", loanDescr);
    		itemJson.put("permanentLoanType", loanType);
    		updateCopy(itemJson.toString(), folioToken);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * Return an hashmap where key contains a user category and value contains the previous category (the one with lower permission)
     * @return hashmap
     */
    public LinkedHashMap<String, String> getOrderedUserCategory() {
    	LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
    	list.put(folioConfig.getString(FolioConstants.USER_BASE_LOAN), folioConfig.getString(FolioConstants.USER_BASE));
    	list.put(folioConfig.getString(FolioConstants.USER_MUSIC_LOAN), folioConfig.getString(FolioConstants.USER_MUSIC));
    	list.put(folioConfig.getString(FolioConstants.USER_HANDWRITE_LOAN), folioConfig.getString(FolioConstants.USER_HANDWRITE));
    	return list;
    }
    
    public String yesterdayDate () {
    	// Create a calendar object with today date. Calendar is in java.util pakage.
	    Calendar calendar = Calendar.getInstance();
	    // Move calendar to yesterday
	    calendar.add(Calendar.DATE, -1);
	    // Get current date of calendar which point to the yesterday now
	    Date yesterday = calendar.getTime();
		SimpleDateFormat date =  new SimpleDateFormat("yyyy-MM-dd");
		return date.format(yesterday);
    }
	protected FolioResponseModel getInstanceByID( final String inst_uuid, final String folioToken) throws FolioException {
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_INSTANCES +	"?query=" + "id=" + inst_uuid;
		
		return okapiCall(url, "GET", folioToken, null, folioConfig.getString(FolioConstants.FOLIO_TENANT), ACCEPT_JSON);
	}
    
	protected FolioResponseModel getInstanceByIdentifier( final String val, final String folioToken, final String type) throws FolioException, UnsupportedEncodingException {
		String filter="";
		if (type.equals("bid")) { filter = "/@value/@identifierTypeId=\"" + folioConfig.getString(FolioConstants.ID_BID) + "\"";}
		if (type.equals("doi")) { filter = "/@value/@identifierTypeId=\"" + folioConfig.getString(FolioConstants.ID_DOI) + "\"";}
		if (type.equals("isbn")) { filter = "/@value/@identifierTypeId=\"" + folioConfig.getString(FolioConstants.ID_ISBN) + "\"";}
		if (type.equals("issn")) { filter = "/@value/@identifierTypeId=\"" + folioConfig.getString(FolioConstants.ID_ISSN) + "\"";}
		
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_INSTANCES +	"?query=" + "identifiers" + HTML_EQUAL +  URLEncoder.encode(filter, "UTF-8") + HTML_QUOTES + val + HTML_QUOTES;
		return okapiCall(url, "GET", folioToken, null, folioConfig.getString(FolioConstants.FOLIO_TENANT), ACCEPT_JSON);
	}
	
	protected FolioResponseModel getInstanceByIdentifier(final String val, final String folioToken, final String type, final int limit) throws FolioException, UnsupportedEncodingException {
		String filter="";
		if (type.equals("bid")) { filter = "/@value/@identifierTypeId=\"" + FolioConstants.ID_BID + "\"";}
		if (type.equals("doi")) { filter = "/@value/@identifierTypeId=\"" + FolioConstants.ID_DOI + "\"";}
		if (type.equals("isbn")) { filter = "/@value/@identifierTypeId=\"" + FolioConstants.ID_ISBN + "\"";}
		if (type.equals("issn")) { filter = "/@value/@identifierTypeId=\"" + FolioConstants.ID_ISSN + "\"";}
		
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_INSTANCES +	"?limit="+limit+"&query=" + "identifiers="+  URLEncoder.encode(filter, "UTF-8") + HTML_QUOTES + val + HTML_QUOTES;
		return okapiCall(url, "GET", folioToken, null, folioConfig.getString(FolioConstants.FOLIO_TENANT), ACCEPT_JSON);
	}
    
	protected FolioResponseModel getHoldingByFormerID( final String former_id, final String folioToken) throws FolioException {
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_HOLDINGS +	"?query=" + "formerIds=" + former_id;
		
		return okapiCall(url, "GET", folioToken, null, folioConfig.getString(FolioConstants.FOLIO_TENANT), ACCEPT_JSON);
	}
	protected FolioResponseModel getHoldingByInstanceUUID( final String inst_uuid, final String folioToken) throws FolioException {
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_HOLDINGS +	"?query=" + "instanceId==" + inst_uuid;
		
		return okapiCall(url, "GET", folioToken, null, folioConfig.getString(FolioConstants.FOLIO_TENANT), ACCEPT_JSON);
	}
	protected FolioResponseModel deleteInstanceByID( final String inst_uuid, final String folioToken) throws FolioException {
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_INSTANCES +	"/" + inst_uuid;
		Log.info("DELETEINSTANCEBYID: " + " -inst_uuid" + inst_uuid);
		return okapiCall(url, "DELETE", folioToken, null, folioConfig.getString(FolioConstants.FOLIO_TENANT), ACCEPT_JSON);
	}
	
	protected FolioResponseModel deleteHoldingByID( final String hold_uuid, final String folioToken) throws FolioException {
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + SEARCH_HOLDINGS +	"/" + hold_uuid;
		Log.info("DELETEHOLDINGBYID: " + " -hold_uuid" + hold_uuid);
		return okapiCall(url, "DELETE", folioToken, null, folioConfig.getString(FolioConstants.FOLIO_TENANT), ACCEPT_JSON);
	}
	
	protected FolioResponseModel deleteItemByID( final String item_uuid, final String folioToken) throws FolioException {
		final String url = folioConfig.getString(FolioConstants.FOLIO_URL_BACKEND) + COPY_INFO_ENDPOINT +	"/" + item_uuid;
		Log.info("DELETEITEMBYID: " + " -item_uuid" + item_uuid);
		return okapiCall(url, "DELETE", folioToken, null, folioConfig.getString(FolioConstants.FOLIO_TENANT), ACCEPT_JSON);
	}
	
	protected JSONObject getFirstResult (FolioResponseModel folioResponse, final String rootElementName) throws FolioException {
		if (folioResponse != null && folioResponse.getJsonResponse() != null) {
			JSONObject json = new JSONObject (folioResponse.getJsonResponse());
			JSONArray array = json.getJSONArray(rootElementName);
			if (array.length() > 0) {
				return (JSONObject) array.get(0);
			}
			else return null;
		}
		else {
			return null;
		}
	}
}

package com.atc.osee.web.folio;

public class FolioResponseModel {
	private  String okapiToken;
	private  String jsonResponse;
	private int responseCode;
	private String errorMessage;
	private int totalRecords;
	private String resultHeader;
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public String getOkapiToken() {
		return okapiToken;
	}
	public void setOkapiToken(String okapiToken) {
		this.okapiToken = okapiToken;
	}
	public String getJsonResponse() {
		return jsonResponse;
	}
	public void setJsonResponse(String jsonResponse) {
		this.jsonResponse = jsonResponse;
	}
	
	/**
	 * Get number of records inside Folio answer. Not all API give this value.
	 */
	public int getTotalRecords() {
		return totalRecords;
	}
	
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
	/**
	 * used when Folio services return information about result operation in headers, 
	 * like the PUT n invetory/instace for the creation of a new instance
	 * @return
	 */
	public String getResultHeader() {
		return resultHeader;
	}
	public void setResultHeader(String resultHeader) {
		this.resultHeader = resultHeader;
	}
	
}

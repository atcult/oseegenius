package com.atc.osee.web.folio;

import com.atc.osee.web.tools.FolioConfigurationTool;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat; 
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.Locale;


public class FolioLoan {
	private String id;
	private String requestType;
	private String requestDate;
	private String dueDate;
	private String userId;
	private String itemId;
	private String status;
	private String title;
	private String itemBarcode;
	private String servicePointId;
	private String servicePointName;
	private String position;
	private String json;
	private boolean isRenewable;
	private String enumeration;
	private String location;
	
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
		
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}
	public boolean isRenewable() {
		return this.isRenewable;
	}
	public void setRenewable(boolean r) {
		this.isRenewable = r;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getItemBarcode() {
		return itemBarcode;
	}
	public void setItemBarcode(String itemBarcode) {
		this.itemBarcode = itemBarcode;
	}
	public String getServicePointId() {
		return servicePointId;
	}
	public void setServicePointId(String servicePointId) {
		this.servicePointId = servicePointId;
	}
	public String getServicePointName() {
		return servicePointName;
	}
	public void setServicePointName(String servicePointName) {
		this.servicePointName = servicePointName;
	}
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public boolean isOpen() {
		return folioConfig.getString(FolioConstants.STATUS_OPEN_LOAN).equals(this.status);
	}
	public String getEnumeration() {
		return enumeration;
	}
	public void setEnumeration(String enumeration) {
		this.enumeration = enumeration;
	}
	public FolioConfigurationTool getFolioConfig() {
		return folioConfig;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	private String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	public boolean isExpired() {
		return (this.dueDate.compareTo(this.getCurrentDate())<0);
	}
	private Date stringToDate(String dateString) {
		DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		Date date=null ;
		try {
		date = dateformat.parse(dateString);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}
	private long getDayDifference(Date date1, Date date2) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	
	public boolean twoDaysPassed() {
		long diffInDays=this.getDayDifference(stringToDate(this.requestDate),stringToDate(getCurrentDate()));
		if(diffInDays>1)
			return true;
		return false;
	}
	


	
	

}

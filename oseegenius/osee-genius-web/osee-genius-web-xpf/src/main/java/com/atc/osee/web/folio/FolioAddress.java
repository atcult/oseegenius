package com.atc.osee.web.folio;

public class FolioAddress {
	private String addressLine;
	private String city;
	private String region;
	private String postalCode;
	private String countryId;
	private String addressTypeId;
	private boolean mainAddress;
	
	public String getAddressLine() {
		return addressLine;
	}
	public void setAddressLine(String addressLine) {
		this.addressLine = addressLine;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getCountryId() {
		return countryId;
	}
	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}
	public String getAddressTypeId() {
		return addressTypeId;
	}
	public void setAddressTypeId(String addressTypeId) {
		this.addressTypeId = addressTypeId;
	}

	public boolean isMainAddress() {
		return mainAddress;
	}
	public void setMainAddress(boolean mainAddress) {
		this.mainAddress = mainAddress;
	}
	
	public String getAddressToString () {
		return this.addressLine + ", " + this.postalCode + ", " + this.city + ", " + this.countryId;
	}

}

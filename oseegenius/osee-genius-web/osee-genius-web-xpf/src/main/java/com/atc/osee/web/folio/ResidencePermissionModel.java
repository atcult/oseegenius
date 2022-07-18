package com.atc.osee.web.folio;

public class ResidencePermissionModel {	
	private String documentNumber;
	private String expirationDate;
	private String stringView;
	
	public String getDocumentNumber() {
		return documentNumber;
	}
	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}
	public String getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String toString() {
		return this.stringView;
	}
	
	
	public ResidencePermissionModel(final String input) {
		if (input != null) {
			this.stringView = input;
			String [] array = input.split("; ");
			String text;
			for(int i = 0; i < array.length; i ++) {
				switch (i) {
					case 0: 
						text = array[0];						
						this.documentNumber =  getDataFromString(text);						
						break;
					case 1:
						text = array[1];
						this.expirationDate = getDataFromString(text);
						break;
					default:	
				}
			}
		}		
	}
	
	//retrieve the index in which data starts inside the string 
	private String getDataFromString (final String text) {
		if (text != null && text.indexOf(":") > -1) {
			int dataStartingIndex = text.indexOf(":") + 2;
			if (text.length() > dataStartingIndex) {
				return text.substring(dataStartingIndex).replace(";", "");
			}
		}
		return null;
	}
}

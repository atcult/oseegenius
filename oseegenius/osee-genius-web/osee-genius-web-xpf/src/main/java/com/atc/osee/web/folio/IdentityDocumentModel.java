package com.atc.osee.web.folio;

public class IdentityDocumentModel {
	private String name;
	private String surname;
	private String type;
	private String documentNumber;
	private String city;
	private String expirationDate;
	private String stringView;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDocumentNumber() {
		return documentNumber;
	}
	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
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
	
	
	public IdentityDocumentModel(final String input) {
		if (input != null) {
			this.stringView = input;
			String [] array = input.split("; ");
			int arrayLength = array.length;
			String text;
			for(int i = 0; i < array.length; i ++) {
				switch (i) {
					case 0: 
						text = array[0];
						if (arrayLength == 6) {							
							this.name = text.substring(text.indexOf(":") + 2);
						}
						else {
							this.type =  text.substring(text.indexOf(":") + 2);
						}
						break;
					case 1:
						text = array[1];
						if (arrayLength == 6) {							
							this.surname = text.substring(text.indexOf(":") + 2);
						}
						else {
							this.documentNumber = text.substring(text.indexOf(":") + 2);
						}
						break;
					case 2:
						text = array[2];
						if (arrayLength == 6) {
							this.type = text.substring(text.indexOf(":") + 2);
						}
						else {
							this.city = text.substring(text.indexOf(":") + 2);
						}
						break;
					case 3:
						text = array[3];
						if (arrayLength == 6) {
							this.documentNumber = text.substring(text.indexOf(":") + 2);
						}
						else {
							this.expirationDate = text.substring(text.indexOf(":") + 2).replaceAll(";", "");
						}
						break;
					case 4:		
						text = array[4];
						this.city = text.substring(text.indexOf(":") + 2);					
						break;
					case 5:
						text = array[5];
						this.expirationDate = text.substring(text.indexOf(":") + 2).replaceAll(";", "");
					default:	
				}
			}
		}
		
	}
}

/**
 * 
 */
package com.atc.osee.web.folio;

import java.util.HashMap;

import com.atc.osee.web.tools.FolioConfigurationTool;

/**
 * @author mcece
 *
 */
public class FolioCustomField {
	private HashMap<String, String> customFields;
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	private HashMap<String, String> notes;

	
	public void setCustomFields(HashMap<String, String> customFields) {
		this.customFields = customFields;
	}
	public HashMap<String, String> getCustomFields() {
		return this.customFields;
	}
	
	
	
	public void setCustomFieldFromNotes(HashMap<String, String> customFieldsNotes) {
		this.notes=customFieldsNotes;
		HashMap<String, String> customFieldsApp=new HashMap<String,String>();
		if(customFieldsNotes.containsKey(folioConfig.getString(FolioConstants.NOTE_JOB_ID)))
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_JOB), customFieldsNotes.get(folioConfig.getString(FolioConstants.NOTE_JOB_ID)));
		if(customFieldsNotes.containsKey(folioConfig.getString(FolioConstants.NOTE_GENDER_ID)))
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GENDER), customFieldsNotes.get(folioConfig.getString(FolioConstants.NOTE_GENDER_ID)));
		if(customFieldsNotes.containsKey(folioConfig.getString(FolioConstants.NOTE_NATION_ID)))
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NATION), customFieldsNotes.get(folioConfig.getString(FolioConstants.NOTE_NATION_ID)));
		if(customFieldsNotes.containsKey(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID))){
			String documentString = customFieldsNotes.get(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID));
			String [] listOfData= documentString.split(";");
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT), listOfData[0].split(":")[1].substring(1) );
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT), listOfData[1].split(":")[1].substring(1) );
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY), listOfData[2].split(":")[1].substring(1) );
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_DOCUMENT), listOfData[3].split(":")[1].substring(1) );
		}
		if(customFieldsNotes.containsKey(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID))) {
			String residencePermission = customFieldsNotes.get(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID));
			String [] listOfData= residencePermission.split(";");
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RESIDENCE_PERMISSION_NUMBER), listOfData[0].split(":")[1].substring(1) );
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_RESIDENCE_PERMISSION), listOfData[1].split(":")[1].substring(1) );
		}
		if(customFieldsNotes.containsKey(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID))) {
			String guardianData =customFieldsNotes.get(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID));
			String [] listOfData= guardianData.split(";");
			String tutore= listOfData[0].split(":")[1].substring(1)+ "-"+ listOfData[1].split(":")[1].substring(1);
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GUARDIAN), tutore);
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT_GUARDIAN), listOfData[2].split(":")[1].substring(1) );
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT_GUARDIAN), listOfData[3].split(":")[1].substring(1) );
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY_GUARDIAN_DOCUMENT), listOfData[4].split(":")[1].substring(1) );
			customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_GUARDIAN), listOfData[5].split(":")[1].substring(1) );
		}
		if(customFieldsNotes.containsKey(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID))){
			String birthLocationData =customFieldsNotes.get(folioConfig.getString(FolioConstants.NOTE_BIRTH_LOCATION_ID));
			String [] listOfData= birthLocationData.split(";");
			if (listOfData.length==2) {
				//non è italiano e quindi ci sono 2 campi
				customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_COUNTRY_OF_BIRTH), listOfData[0] );
				customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_BIRTHPLACE), listOfData[1].substring(1) );
			}
			else {
				customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_COUNTRY_OF_BIRTH), listOfData[0] );
				customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_PROVINCE_OF_BIRTH), listOfData[1].substring(1) );
				customFieldsApp.put(folioConfig.getString(FolioConstants.CUSTOM_FIELD_BIRTHPLACE), listOfData[2].substring(1) );
				
			}
		}
		this.customFields=customFieldsApp;
	}
	
	
	
	public HashMap<String, String> getNotesFromCustomFields() {
		return this.notes;
	
	}
	
	
	
	
	public String getTutorName() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GUARDIAN)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GUARDIAN)).split("-")[0] : null;
	}
	
	public String getTutorSurname() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GUARDIAN)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GUARDIAN)).split("-")[1] : null;
	}
	
	public String getTutorDocumentType() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT_GUARDIAN)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT_GUARDIAN)) : null;
	}
	public String getTutorDocumentNumber() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT_GUARDIAN)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT_GUARDIAN)) : null;
	}
	public String getTutorDocumentReleasedBy() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY_GUARDIAN_DOCUMENT)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY_GUARDIAN_DOCUMENT)) : null;
	}
	public String getTutorDocumentExpiration() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_GUARDIAN)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_GUARDIAN)) : null;
	}
	

	

	public String getResidencePermissionNumber() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RESIDENCE_PERMISSION_NUMBER)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RESIDENCE_PERMISSION_NUMBER)) : null;
	}
	public String getResidencePermissionExpiration() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_RESIDENCE_PERMISSION)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_RESIDENCE_PERMISSION)) : null;
	}
	
	
	
	public String getNation() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NATION)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NATION)) : null;
	}
	
	
	public String getGender() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GENDER)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_GENDER)) : null;
	}
	
	public String getJob() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_JOB)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_JOB)) : null;
	}


	public String getDocumentType() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_TYPE_DOCUMENT)) : null;
	}
	public String getDocumentNumber() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_NUMBER_DOCUMENT)) : null;
	}
	public String getDocumentReleasedBy() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_RELEASEDBY)) : null;
	}
	public String getDocumentExpiration() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_DOCUMENT)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_EXPIRATION_DOCUMENT)) : null;
	}
	
	
	public String getBirthNation() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_COUNTRY_OF_BIRTH)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_COUNTRY_OF_BIRTH)) : null;
	}
	
	public String getBirthMunicipalityLocation() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_BIRTHPLACE)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_BIRTHPLACE)) : null;
	}
	
	public String getBirthCityLocation() {
		return (this.customFields != null && this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_PROVINCE_OF_BIRTH)) != null) ? this.customFields.get(folioConfig.getString(FolioConstants.CUSTOM_FIELD_PROVINCE_OF_BIRTH)) : null;
	}
	

	
}

package com.atc.osee.web.folio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.json.JSONObject;

import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

public class FolioUserModel {
	private String id;
	private String username;
	private String barcode;
	private String patronGroupCode;
	private String patronGroupName;
	private String lastName;
	private String firstName;
	private String email;
	private String okapiToken;	
	private String telephone;
	private String mobile;
	private String folioJson;
	private List<FolioAddress> address;
	private String fiscalCode;
	private String dateOfBirth;
	private String photo;
	private HashMap<String, String> notes;
	private Map<String, NameValuePair []> servicePoints;	
	private String defaultServicePoint;
	private List<String> permissions; 
	private String enrollmentDate;	
	private String expireDate;
	private boolean active;
	private String blockExpireDate;
	//private HashMap<String, String> customFields;
	private FolioCustomField customFields;
	
	

	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean status) {
		this.active = status;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getPatronGroupCode() {
		return patronGroupCode;
	}
	public void setPatronGroupCode(String patronGroupCode) {
		this.patronGroupCode = patronGroupCode;
	}
	public String getPatronGroupName() {
		return patronGroupName;
	}
	public void setPatronGroupName(String patronGroupName) {
		this.patronGroupName = patronGroupName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}	
	public String getOkapiToken() {
		return okapiToken;
	}
	public void setOkapiToken(String okapiToken) {
		this.okapiToken = okapiToken;
	}	
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
	public String getFolioJson() {
		return folioJson;
	}
	public void setFolioJson(String folioJson) {
		this.folioJson = folioJson;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public List<FolioAddress> getAddress() {
		return address;
	}
	public void setAddress(List<FolioAddress> address) {
		this.address = address;
	}	
	public FolioAddress getDomicileAddress() {
		if (this.address != null) {
			for (FolioAddress currentAddress : this.address) {
				if (folioConfig.getString(FolioConstants.DOMICILE_ADDRESS).equals(currentAddress.getAddressTypeId())) {
					return  currentAddress;
				}
			}			
		}
		return null;
	}
	public String getDomicileAddressString() {
		if (this.address != null) {
			for (FolioAddress currentAddress : this.address) {
				if (folioConfig.getString(FolioConstants.DOMICILE_ADDRESS).equals(currentAddress.getAddressTypeId())) {
					return  currentAddress.getAddressToString();
				}
			}			
		}
		return null;
	}
	
	
	
	
	
	public FolioAddress getResidenceAddress() {
		if (this.address != null) {
			for (FolioAddress currentAddress : this.address) {
				if (folioConfig.getString(FolioConstants.RESIDENCE_ADDRESS).equals(currentAddress.getAddressTypeId())) {
					return  currentAddress;
				}
			}			
		}
		return null;
	}
	
	public String getResidenceAddressString() {
		if (this.address != null) {
			for (FolioAddress currentAddress : this.address) {
				if (folioConfig.getString(FolioConstants.RESIDENCE_ADDRESS).equals(currentAddress.getAddressTypeId())) {
					return  currentAddress.getAddressToString();
				}
			}			
		}
		return null;
	}
	
	
	public HashMap<String, String> getCustomFields() {
		return this.customFields.getCustomFields();
	}
	
	public void setCustomFields(HashMap<String, String> customFields) {
		if(this.customFields==null)
			this.customFields =new FolioCustomField();
		this.customFields.setCustomFields(customFields);
	}
	
	
	public HashMap<String, String> getNotes() {
		if (notes!=null)
			return notes;
		else {
			HashMap<String, String> not=new HashMap<String, String>();
			notes=not;
			return notes;
		}
	}
	
	public void setNotes(HashMap<String, String> notes) {
		//lascio questo metodo cosi  com'è per le note vere, e me ne creo un altro per tutte le note che in realtà sono custom field
		//attento quando devo aggiungere le note
		this.notes = notes;
	}
	
	public void setCustomFieldFromNotes(HashMap<String, String> notes) {
		if(this.customFields==null)
			this.customFields =new FolioCustomField();
		this.customFields.setCustomFieldFromNotes(notes);
	}
	
	
	public HashMap<String, String> getNotesFromCustomFields() {
		HashMap<String, String> temp=new HashMap<String, String>();
		if(this.customFields!=null)
			temp.putAll(this.customFields.getNotesFromCustomFields());
		if(this.notes!=null)
			temp.putAll(this.notes);
		return temp;
		}
		
	
	
	public String getFiscalCode() {
		return fiscalCode;
	}
	public void setFiscalCode(String fiscalCode) {
		this.fiscalCode = fiscalCode;
	}
	public String getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	
	public String getFormattedDateOfBirth() {
		if (this.dateOfBirth != null && this.dateOfBirth.indexOf("T") > -1) {
			return dateOfBirth.substring(0, dateOfBirth.indexOf("T"));
		}
		return dateOfBirth;
	}
	
	public String getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}
	
	public String getEnrollmentDate() {
		return enrollmentDate;
	}
	public void setEnrollmentDate(String enrollmentDate) {
		this.enrollmentDate = enrollmentDate;
	}
	
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	
	public String getDefaultServicePoint() {
		return defaultServicePoint;
	}
	public void setDefaultServicePoint(String defaultServicePoint) {
		this.defaultServicePoint = defaultServicePoint;
	}
	
	public Map<String, NameValuePair []> getServicePoints() {
		return servicePoints;
	}
	public void setServicePoints(Map<String, NameValuePair []> servicePoints) {
		this.servicePoints = servicePoints;
	}	
	public List<String> getPermission() {
		return permissions;
	}
	public void setPermission(List<String> permissions) {
		this.permissions = permissions;
	}	
	
	public boolean isPreregistered() {
		return folioConfig.getString(FolioConstants.USER_PREREG).equals(this.patronGroupCode);
	}

	public boolean isStaff() {
		return (folioConfig.getString(FolioConstants.USER_ADMIN).equals(this.patronGroupCode) 
				|| folioConfig.getString(FolioConstants.USER_STAFF).equals(this.patronGroupCode));
	}
	
	public boolean isAdmin() {
    	//boolean permissionCheck = false;
    	for (String permission : this.permissions) {
    		if (folioConfig.getString(FolioConstants.BIBLIO_PROFILE_PERMISSION).equals(permission)
    				|| folioConfig.getString(FolioConstants.FOLIO_ADMIN_PROFILE_PERMISSION).equals(permission)	
    				|| folioConfig.getString(FolioConstants.ADMIN_BNCF_PROFILE_PERMISSION).equals(permission)) {
    			//permissionCheck = true;
    			return true;
    		}
    	}    	
    	/*
    	return (folioConfig.getString(FolioConstants.USER_ADMIN).equals(this.patronGroupCode) 
				|| (folioConfig.getString(FolioConstants.USER_STAFF).equals(this.patronGroupCode) && permissionCheck ));
				*/
    	return false;
    	
	}	
	
	public boolean isSuperAdmin() {		
		for (String permission : this.permissions) {
    		if (folioConfig.getString(FolioConstants.FOLIO_ADMIN_PROFILE_PERMISSION).equals(permission)	
    				|| folioConfig.getString(FolioConstants.ADMIN_BNCF_PROFILE_PERMISSION).equals(permission)) {
    			return true;
    		}
    	}   
		return false;
	}
	
	public boolean manuscriptEnabled() {
		return (folioConfig.getString(FolioConstants.USER_ADMIN).equals(this.patronGroupCode) 
				|| folioConfig.getString(FolioConstants.USER_STAFF).equals(this.patronGroupCode)
				|| folioConfig.getString(FolioConstants.USER_HANDWRITE).equals(this.patronGroupCode)
				|| folioConfig.getString(FolioConstants.USER_HANDWRITE_LOAN).equals(this.patronGroupCode)
				);
	}
	

	public IdentityDocumentModel getDocument() {		
		return (this.notes != null && this.notes.get(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID)) != null) ? new IdentityDocumentModel(this.notes.get(folioConfig.getString(FolioConstants.NOTE_DOCUMENT_ID))) : null;
	}
	
	public IdentityDocumentModel getTutor() {		
		return (this.notes != null && this.notes.get(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID)) != null) ? new IdentityDocumentModel(this.notes.get(folioConfig.getString(FolioConstants.NOTE_GUARDIAN_ID))) : null;
	}
	
	public ResidencePermissionModel getResidencePermission() {
		return (this.notes != null && this.notes.get(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID)) != null) ? new ResidencePermissionModel(this.notes.get(folioConfig.getString(FolioConstants.NOTE_RESIDENCE_PERMISSION_ID))) : null;
	}
	
	
	
	public String getGeneralNote() {
		return (this.notes != null && this.notes.get(folioConfig.getString(FolioConstants.NOTE_NOTE_ID)) != null) ? this.notes.get(folioConfig.getString(FolioConstants.NOTE_NOTE_ID)) : null;
	}
	public String getBlockExpireDate() {
		return blockExpireDate;
	}
	public void setBlockExpireDate(String blockExpireDate) {
		this.blockExpireDate = blockExpireDate;
	}
	
	public String getTutorName() {
		if (this.customFields!=null)
			return this.customFields.getTutorName();	
		else return null;
	}
	public String getTutorSurname() {
		if (this.customFields!=null)
			return this.customFields.getTutorSurname();
		else return null;
	}
	public String getTutorDocumentType() {
		if (this.customFields!=null)
			return this.customFields.getTutorDocumentType();
		else return null;
	}
	public String getTutorDocumentNumber() {
		if (this.customFields!=null)
			return this.customFields.getTutorDocumentNumber();
		else return null;
	}
	public String getTutorDocumentReleasedBy() {
		if (this.customFields!=null)
			return this.customFields.getTutorDocumentReleasedBy();
		else return null;
	}
	public String getTutorDocumentExpiration() {
		if (this.customFields!=null)
			return this.customFields.getTutorDocumentExpiration();
		else return null;
	}
	public String getResidencePermissionNumber() {
		if (this.customFields!=null)
			return this.customFields.getResidencePermissionNumber();
		else return null;
	}
	public String getResidencePermissionExpiration() {
		if (this.customFields!=null)
			return this.customFields.getResidencePermissionExpiration();
		else return null;
	}
	public String getNation() {
		if (this.customFields!=null)
			return this.customFields.getNation();
		else return null;
	}
	public String getGender() {
		if (this.customFields!=null)
			return this.customFields.getGender();
		else return null;
	}
	public String getJob() {
		if (this.customFields!=null)
			return this.customFields.getJob();
		else return null;
	}
	public String getDocumentType() {
		if (this.customFields!=null)
			return this.customFields.getDocumentType();
		else return null;
	}
	public String getDocumentNumber() {
		if (this.customFields!=null)
			return this.customFields.getDocumentNumber();
		else return null;
	}
	public String getDocumentReleasedBy() {
		if (this.customFields!=null)
			return this.customFields.getDocumentReleasedBy();
		else return null;
	}
	
	public String getDocumentExpiration() {
		if (this.customFields!=null)
			return this.customFields.getDocumentExpiration();
		else return null;
	}
	
	public String getBirthNation() {
		if (this.customFields!=null)
			return this.customFields.getBirthNation();
		else return null;
	}
	public String getBirthMunicipalityLocation() {
		if (this.customFields!=null)
			return this.customFields.getBirthMunicipalityLocation();
		else return null;
	}
	public String getBirthCityLocation() {
		if (this.customFields!=null)
			return this.customFields.getBirthCityLocation();
		else return null;
	}

	public String getBirthLocation() {
		if (this.customFields!=null){
			StringBuilder ret=new StringBuilder();
			if(this.customFields.getBirthNation()!=null) {
				ret.append(this.customFields.getBirthNation());
			}
			if(this.customFields.getBirthCityLocation()!=null) {
				ret.append(" ; ");
				ret.append(this.customFields.getBirthCityLocation());
			}
			if(this.customFields.getBirthMunicipalityLocation()!=null) {
				ret.append(" ; ");
				ret.append(this.customFields.getBirthMunicipalityLocation());
			}
				
			return ret.toString();	
			
		}
		return null;
	}
	
	
	
}
	
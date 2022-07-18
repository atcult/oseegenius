package com.atc.osee.web.folio;

import org.json.JSONObject;

import com.atc.osee.web.tools.FolioConfigurationTool;

public class FolioItemModel {
	private JSONObject folioJson;
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	
	public String getBarcode() {
		if (this.folioJson != null && this.folioJson.has("barcode")) {
			return this.folioJson.getString("barcode");
		}
		else return null;
	}
	
	public String getId() {
		if (this.folioJson != null && this.folioJson.has("id")) {
			return this.folioJson.getString("id");
		}
		else return null;
	}
	
	public String getStatus() {
		if (this.folioJson != null && this.folioJson.has("status")) {
			return this.folioJson.getJSONObject("status").getString("name");
		}
		else return null;
	}
	
	public JSONObject getFolioJson() {
		return folioJson;
	}
	public void setFolioJson(JSONObject folioJson) {
		this.folioJson = folioJson;
	}
	
	public boolean isCopyAvailable() {
		try {	    	
	    	return folioConfig.getString(FolioConstants.AVAILABLE).equals(this.folioJson.getJSONObject("status").get("name"));
	    } catch (Exception e) {
	    	return false;
	    }
	}
	
	public String getMaterialType() {
		if (this.folioJson != null && this.folioJson.has("materialType")) {
			return this.folioJson.getJSONObject("materialType").getString("id");
		}
		else return null;
	}
	
	public String getPermanentLoanTypeContent() {
		if (this.folioJson != null && this.folioJson.has("permanentLoanType")) {
			return this.folioJson.getJSONObject("permanentLoanType").getString("name");
		}
		else return null;
	}

	public String getTemporaryLocationContent() {
		if (this.folioJson != null && this.folioJson.has("temporaryLocation")) {
			return this.folioJson.getJSONObject("temporaryLocation").getString("name");
		}
		else return null;
	}

}

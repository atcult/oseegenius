package com.atc.osee.web.folio;

public class ManuscriptDataModel {
	private int id;
	private String label;
	private String newCollocation;
	private String volumeLabel;
	private String volumeType;
	
	public String getVolumeLabel() {
		return volumeLabel;
	}
	public void setVolumeLabel(String volumeLabel) {
		this.volumeLabel = volumeLabel;
	}
	public String getVolumeType() {
		return volumeType;
	}
	public void setVolumeType(String volumeType) {
		this.volumeType = volumeType;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getNewCollocation() {
		return newCollocation;
	}
	public void setNewCollocation(String newCollocation) {
		this.newCollocation = newCollocation;
	}

}

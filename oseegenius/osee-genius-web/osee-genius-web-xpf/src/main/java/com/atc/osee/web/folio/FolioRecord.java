package com.atc.osee.web.folio;

import com.atc.osee.web.tools.FolioConfigurationTool;

public class FolioRecord {
	private String title;
	private String authorName;
	private String authorType;
	private String publisher;
	private String publisherName;
	private String place;
	private String catalogDate;
	private String publicationDate;
	private String year;
	private String day;
	private String month;
	private String collocation;
	private String newCollocation;
	private String volume;
	private String issue;
	private String note;
	private String status;
	private String bibliographicLevel;
	private String resourceType;
	private String source;
	private String indexedTitle;
	private String instanceId;
	private String holdingType;
	private String holdingSection;
//	private String holdingId;
	private String itemType;
	private String loanType;
	private String barcode;
	private String manuscriptFondo;
	private String manuscriptCollocation;
	private String manuscriptSpec;
	private String manuscriptVolume;
	private String manuscriptLabel;
	private String edition;
	private String holding;

	
	public String getPublisherString() {
		StringBuilder result = new StringBuilder();
		if (getPlace() != null && !"".equals(getPlace())) {
			result.append(getPlace() + " : ");
		}
		if (getPublisherName() != null && !"".equals(getPublisherName())) {
			result.append(getPublisherName() + ", ");
		}
		if (getPublicationDate() != null && !"".equals(getPublicationDate())) {
			result.append(getPublicationDate());
		}
		return result.toString();
	}	
	
	public String getManuscriptLabel() {
		return manuscriptLabel;
	}
	public String getPublisherName() {
		return publisherName;
	}
	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}
	public void setManuscriptLabel(String manuscriptLabel) {
		this.manuscriptLabel = manuscriptLabel;
	}
	public String getManuscriptFondo() {
		return manuscriptFondo;
	}
	public void setManuscriptFondo(String manuscriptFondo) {
		this.manuscriptFondo = manuscriptFondo;
	}
	public String getManuscriptCollocation() {
		return manuscriptCollocation;
	}
	public void setManuscriptCollocation(String manuscriptCollocation) {
		this.manuscriptCollocation = manuscriptCollocation;
	}
	public String getManuscriptSpec() {
		return manuscriptSpec;
	}
	public void setManuscriptSpec(String manuscriptSpec) {
		this.manuscriptSpec = manuscriptSpec;
	}
	public String getManuscriptVolume() {
		return manuscriptVolume;
	}
	public void setManuscriptVolume(String manuscriptVolume) {
		this.manuscriptVolume = manuscriptVolume;
	}
	public FolioConfigurationTool getFolioConfig() {
		return folioConfig;
	}
	final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getAuthorType() {
		return authorType;
	}
	public void setAuthorType(String authorType) {
		this.authorType = authorType;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public String getCatalogDate() {
		return catalogDate;
	}
	public void setCatalogDate(String catalogDate) {
		this.catalogDate = catalogDate;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getCollocation() {
		return collocation;
	}
	public void setCollocation(String collocation) {
		this.collocation = collocation;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getBibliographicLevel() {
		return bibliographicLevel;
	}
	public void setBibliographicLevel(String bibliographicLevel) {
		this.bibliographicLevel = bibliographicLevel;
	}
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getIndexedTitle() {
		return indexedTitle;
	}
	public void setIndexedTitle(String indexedTitle) {
		this.indexedTitle = indexedTitle;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getHoldingType() {
		return holdingType;
	}
	public void setHoldingType(String holdingType) {
		this.holdingType = holdingType;
	}
	public String getHoldingSection() {
		return holdingSection;
	}
	public void setHoldingSection(String holdingSection) {
		this.holdingSection = holdingSection;
	}
//	public String getHoldingId() {
//		return holdingId;
//	}
//	public void setHoldingId(String holdingId) {
//		this.holdingId = holdingId;
//	}
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	public String getLoanType() {
		return loanType;
	}
	public void setLoanType(String loanType) {
		this.loanType = loanType;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getNewCollocation() {
		return newCollocation;
	}
	public void setNewCollocation(String newCollocation) {
		this.newCollocation = newCollocation;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}	
	public String getPublicationDate() {
		return publicationDate;
	}
	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}
	public boolean isPeriodic() {
		return folioConfig.getString(FolioConstants.TYPE_HOLDING_PERIODIC).equals(this.holdingType); 
	}
//	a.2005, v.15, m.10, nr.37, gg24
//	quindi l’anno verrà introdotto da “a.”, volume da “v.”, mese da “m.”, fascicolo da “nr.”, giorno da  “gg”, tutti separati da virgola e spazio (,#).
	public String getEnumeration() {
		if (isPeriodic()) {
			StringBuilder result = new StringBuilder();
			if (this.year != null && !"".equals(this.year.trim())) {
				result.append("a.").append(this.year.trim());
			}
			if (this.volume != null && !"".equals(this.volume.trim())) {
				result.append(", ");
				result.append("v.").append(this.volume.trim());
			}
			if (this.month != null && !"".equals(this.month.trim())) {
				result.append(", ");
				result.append("m.").append(this.month.trim());
			}
			if (this.issue != null && !"".equals(this.issue.trim())) {
				result.append(", ");
				result.append("nr.").append(this.issue.trim());
			}
			if (this.day != null && !"".equals(this.day.trim())) {
				result.append(", ");
				result.append("gg.").append(this.day.trim());
			}
			return result.toString();
		}
		else return null;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getHolding() {
		return holding;
	}

	public void setHolding(String holding) {
		this.holding = holding;
	}

	
}

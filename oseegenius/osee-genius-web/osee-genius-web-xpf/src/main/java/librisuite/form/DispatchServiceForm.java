package librisuite.form;

public class DispatchServiceForm extends LibrisuiteForm {
	
	private String ssid;
	private String unme;
	private String lcl;
	private String lcc;
	private String service;
	private String appName;
	
	// old ILL parameters:
	private String USR;
	private String ORG;
	private String BARCODE;
	private String AMICUS_NBR;
	private String SELECTED_MAIN;
	private String CPY_ID_NBR;
	private String SHELF;
	private String ENCP;
	private String ACTION;
	
	
	public String getAMICUS_NBR() {
		return AMICUS_NBR;
	}
	public void setAMICUS_NBR(String amicus_nbr) {
		AMICUS_NBR = amicus_nbr;
	}
	public String getSELECTED_MAIN() {
		return SELECTED_MAIN;
	}
	public void setSELECTED_MAIN(String selected_main) {
		SELECTED_MAIN = selected_main;
	}
	public String getCPY_ID_NBR() {
		return CPY_ID_NBR;
	}
	public void setCPY_ID_NBR(String cpy_id_nbr) {
		CPY_ID_NBR = cpy_id_nbr;
	}
	public String getSHELF() {
		return SHELF;
	}
	public void setSHELF(String shelf) {
		SHELF = shelf;
	}
	public String getENCP() {
		return ENCP;
	}
	public void setENCP(String encp) {
		ENCP = encp;
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getUnme() {
		return unme;
	}
	public void setUnme(String unme) {
		this.unme = unme;
	}
	public String getLcl() {
		return lcl;
	}
	public void setLcl(String lcl) {
		this.lcl = lcl;
	}
	public String getLcc() {
		return lcc;
	}
	public void setLcc(String lcc) {
		this.lcc = lcc;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	/**
	 * @param usr the uSR to set
	 */
	public void setUSR(String usr) {
		USR = usr;
	}
	/**
	 * @return the uSR
	 */
	public String getUSR() {
		return USR;
	}
	/**
	 * @param org the oRG to set
	 */
	public void setORG(String org) {
		ORG = org;
	}
	/**
	 * @return the oRG
	 */
	public String getORG() {
		return ORG;
	}
	/**
	 * @param barcode the bARCODE to set
	 */
	public void setBARCODE(String barcode) {
		BARCODE = barcode;
	}
	/**
	 * @return the bARCODE
	 */
	public String getBARCODE() {
		return BARCODE;
	}
	/**
	 * @param aCTION the aCTION to set
	 */
	public void setACTION(String aCTION) {
		ACTION = aCTION;
	}
	/**
	 * @return the aCTION
	 */
	public String getACTION() {
		return ACTION;
	}
}
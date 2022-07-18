/*
 * (c) LibriCore
 * 
 * Created on Jul 5, 2004
 * 
 * LogonBean.java
 */
package librisuite.bean.logon;

import javax.servlet.http.HttpServletRequest;

import librisuite.bean.LibrisuiteBean;
import librisuite.business.authentication.DAOUserAccount;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;

/**
 * This is the bean used by logon.
 * 
 * @author Wim Crols
 * @version $Revision: 1.4 $, $Date: 2005/07/13 12:45:10 $
 * @since 1.0
 */
public class LogonBean extends LibrisuiteBean {

	private String name = new String("");
	private String password = new String("");
	private String releaseAndServicePack;
	
	//private static final String HIBERNATE_CONNECTION_URL = Defaults.getString("hibernate.connection.url");
	private static final String MAIOR_VERSION = Defaults.getString("maior.version");
	private static final String MINOR_VERSION = Defaults.getString("minor.version");
	private static final String WEBAPP_VERSION = Defaults.getString("web.application.version");
	
	private String webAppVersion;
	private String maiorMinorVersion;
	private String dbName;

	public static LogonBean getInstance(HttpServletRequest request) {
		LogonBean logonBean = (LogonBean)getSessionAttribute(request, LogonBean.class);
		if (logonBean == null) {
			logonBean = new LogonBean();
			logonBean.setSessionAttribute(request, LogonBean.class);
		}
		/*
		 * make sure that any old LV session is stopped
		 */
		//MAURA
		/*LVMessage.LVExit(LVMessage.getLVSessionId());
		LVMessage.setLVSessionId(null);*/
		return logonBean;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	public String getReleaseAndServicePack(){
		return releaseAndServicePack;
	}
	
	public void setReleaseAndServicePack(String releaseAndServicePack) {
		this.releaseAndServicePack = releaseAndServicePack;
	}

	public void setMaiorMinorVersion(String maiorMinorVersion) {
		this.maiorMinorVersion = maiorMinorVersion;
	}

	public String getMaiorMinorVersion() 
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(MAIOR_VERSION).append(".").append(MINOR_VERSION).append(".");
		return buffer.toString();
	}
	
	public String getDbName() 
	{
		return "";
		//return HIBERNATE_CONNECTION_URL.substring(HIBERNATE_CONNECTION_URL.lastIndexOf(":") + 1);
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	public String getWebAppVersion() {
		return WEBAPP_VERSION;
	}
}
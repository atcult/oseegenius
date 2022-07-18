/*
 * (c) LibriCore
 * 
 * Created on Aug 30, 2004
 * 
 * CssBean.java
 */
package librisuite.bean.css;

import javax.servlet.http.HttpServletRequest;

import librisuite.bean.LibrisuiteBean;

/**
 * @author Wim Crols
 * @version $Revision: 1.2 $, $Date: 2006/02/03 16:35:32 $
 * @see
 * @since 1.0
 */
public class CssBean extends LibrisuiteBean {

	private String cssUrl = "/common/css/styles.jsp";
	private String madesCssUrl = "/theme/styles.css";
	private String notesCssUrl = "/theme/notes.css";
	private String opacaCssUrl = "/theme/opaca.css";

	public static CssBean getInstance(HttpServletRequest httpServletRequest) {
		CssBean cssBean =
			(CssBean) CssBean.getSessionAttribute(
				httpServletRequest,
				CssBean.class);
		if (cssBean == null) {
			cssBean = new CssBean();
			cssBean.setSessionAttribute(httpServletRequest, CssBean.class);

		}

		return cssBean;
	}

	/**
	 * Getter for cssUrl
	 * 
	 * @return cssUrl
	 * @since 1.0
	 */
	public String getCssUrl() {
		return cssUrl;
	}

	/**
	 * Setter for cssUrl
	 * 
	 * @param string cssUrl
	 * @since 1.0
	 */
	public void setCssUrl(String string) {
		cssUrl = string;
	}

	public String getMadesCssUrl() {
		return madesCssUrl;
	}

	public void setMadesCssUrl(String madesCssUrl) {
		this.madesCssUrl = madesCssUrl;
	}

	public String getNotesCssUrl() {
		return notesCssUrl;
	}

	public void setNotesCssUrl(String notesCssUrl) {
		this.notesCssUrl = notesCssUrl;
	}

	public String getOpacaCssUrl() {
		return opacaCssUrl;
	}

	public void setOpacaCssUrl(String opacaCssUrl) {
		this.opacaCssUrl = opacaCssUrl;
	}

}

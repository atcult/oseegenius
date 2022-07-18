/*
 * (c) LibriCore
 * 
 * Created on Aug 30, 2004
 * 
 * LocaleBean.java
 */
package librisuite.bean.locale;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import librisuite.bean.LibrisuiteBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Wim Crols
 * @version $Revision: 1.5 $, $Date: 2006/02/03 16:35:32 $
 * @see
 * @since 1.0
 */
public class LocaleBean extends LibrisuiteBean {

	public class AvailableLocale {
		//TODO not sure why this is a nested class
		private Locale locale;

		/**
		 * Class constructor
		 */
		public AvailableLocale(Locale locale) {
			this.locale = locale;
		}

		/**
		 */
		public String getLabel() {
			StringBuffer label = new StringBuffer("");
			label.append(this.locale.getDisplayLanguage(this.locale));
			if ((this.locale.getDisplayCountry(this.locale) != null)
				&& (this.locale.getDisplayCountry(this.locale).length() > 0)) {
				label.append(" (");
				label.append(this.locale.getDisplayCountry(this.locale));
				if ((this.locale.getDisplayVariant(this.locale) != null)
					&& (this.locale.getDisplayVariant(this.locale).length()
						> 0)) {
					label.append(" ");
					label.append(this.locale.getDisplayVariant(this.locale));
				}
				label.append(")");
			}
			return new String(label);
		}

		/**
		 */
		public String getValue() {
			return this.locale.toString();
		}

	}

	private static Log logger = LogFactory.getLog(LocaleBean.class);

	private static List availableLocales = null;

	private Locale locale = new Locale("en", "GB");

	public static LocaleBean getInstance(HttpServletRequest httpServletRequest) {
		LocaleBean localeBean =
			(LocaleBean) LocaleBean.getSessionAttribute(
				httpServletRequest,
				LocaleBean.class);
		if (localeBean == null) {
			localeBean = new LocaleBean();
			localeBean.setSessionAttribute(
				httpServletRequest,
				LocaleBean.class);

		}

		return localeBean;
	}

	/**
	 * Getter for localeList
	 */
	public List getAvailableLocales() {
		if (availableLocales == null) {
			availableLocales = new ArrayList();
			/*
			Locale[] locales = Locale.getAvailableLocales();
			for (int localeIndex = 0;
				localeIndex < locales.length;
				localeIndex++) {
				availableLocales.add(new AvailableLocale(locales[localeIndex]));
			}
			*/
			//TODO available locales should be externalised
			/* they need to correspond with those available in the LibriVision installation */
			availableLocales.add(new AvailableLocale(new Locale("ar")));
			availableLocales.add(new AvailableLocale(new Locale("ar", "OM")));
			availableLocales.add(new AvailableLocale(new Locale("en")));
			availableLocales.add(new AvailableLocale(new Locale("en", "GB")));
			availableLocales.add(new AvailableLocale(new Locale("en", "US")));
			availableLocales.add(new AvailableLocale(new Locale("es")));
			availableLocales.add(new AvailableLocale(new Locale("es", "ES")));
			availableLocales.add(new AvailableLocale(new Locale("eu")));
			availableLocales.add(new AvailableLocale(new Locale("eu", "ES")));
			availableLocales.add(new AvailableLocale(new Locale("fr")));
			availableLocales.add(new AvailableLocale(new Locale("fr", "BE")));
			availableLocales.add(new AvailableLocale(new Locale("hu")));
			availableLocales.add(new AvailableLocale(new Locale("it")));
			availableLocales.add(new AvailableLocale(new Locale("nl")));
			availableLocales.add(new AvailableLocale(new Locale("nl", "BE")));
		}

		return availableLocales;
	}

	/**
	 * Getter for locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Setter for locale
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
		logger.debug("locale changed to '" + locale.toString() + "'");
	}

}

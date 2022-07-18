/*
 	Copyright (c) 2010 @Cult s.r.l. All rights reserved.
	
	@Cult s.r.l. makes no representations or warranties about the 
	suitability of the software, either express or implied, including
	but not limited to the implied warranties of merchantability, fitness
	for a particular purpose, or non-infringement. 
	
	@Cult s.r.l.not be liable for any damage suffered by 
	licensee as a result of using, modifying or distributing this software 
	or its derivates.
	
	This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.web.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.model.Visit;

/**
 * Breadcrumb handler tool.
 * A Velocity tool (really a POJO) that handles breadcrumb operations.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
@DefaultKey("breadcrumbTool")
@ValidScope(Scope.APPLICATION)
public class BreadcrumbHandlingTool extends SafeConfig
{
	protected Map<String, String> i18nFacets = new HashMap<String, String>();
	private final String _3QM = "???";
	
	public String getHumanReadableFilterName(final HttpServletRequest request, final String filterQuery) {
		int indexOfColon =  filterQuery.indexOf(":");
		if (indexOfColon > -1) {
			return getHumanReadableLabel(request, filterQuery.substring(0, indexOfColon));
		}
		return filterQuery;
	}
	/**
	 * Returns the localized label associated with the given filter (name).
	 * 
	 * @param request the HTTP request.
	 * @param filterQuery the filter query (name:"value")
	 * @param locale the user preferred locale.
	 * @return the localized label associated with the given filter (name).
	 */
	@SuppressWarnings("unchecked")
	public String getHumanReadableLabel(final HttpServletRequest request, final String filterQuery, final Locale locale)
	{
		int indexOfColon =  filterQuery.indexOf(":");
	
		if (filterQuery.indexOf(ISolrConstants.PUBLICATION_DATE_FIELD_NAME) != -1 && filterQuery.indexOf(ISolrConstants.PUBLICATION_DATE_INTERVALS_FIELD_NAME) == -1)
		{
			ResourceBundle messages = ResourceBundle.getBundle(IConstants.DEFAULT_MESSAGE_BUNDLE_NAME, locale);
			try 
			{
				if (filterQuery.indexOf("[* TO") != -1)
				{
					return new StringBuilder()
						.append(messages.getString("until_year"))
						.append(" ")
						.append(filterQuery.substring(indexOfColon + 7, filterQuery.length() -1))
						.toString();
				}
				
				if (filterQuery.indexOf("TO *]") != -1)
				{
					return new StringBuilder()
						.append(messages.getString("from_year"))
						.append(" ")
						.append(filterQuery.substring(indexOfColon + 2, indexOfColon + 6))
						.append(" ")
						.append(messages.getString("to_und_year"))
						.toString();
				}
				
				return new StringBuilder()
					.append(messages.getString("from_year"))
					.append(" ")
					.append(filterQuery.substring(indexOfColon + 2, indexOfColon + 6))
					.append(" ")
					.append(messages.getString("to_year"))
					.append(" ")
					.append(filterQuery.substring(indexOfColon + 10, indexOfColon + 14))
					.toString();
			} catch (Exception ignore)
			{
				ignore.printStackTrace();
				// Nothing.
				//return IConstants.EMPTY_STRING;
				String attributeValue = filterQuery.substring(indexOfColon + 1).replace("\"", "").replaceAll("\\*", "");
				attributeValue = attributeValue.replaceAll("\\\\", "");
				return attributeValue;
			}
		}
		
		
		String attributeValue = filterQuery.substring(indexOfColon + 1).replace("\"", "").replaceAll("\\*", "");
		
		if (filterQuery != null && filterQuery.startsWith("authority_group")) {
			Visit v = (Visit) request.getSession(true).getAttribute(HttpAttribute.VISIT);
			String label = v.getIndexMap().get(attributeValue);
			if (label != null) {
				return label;
			}
		}		
		
		attributeValue = attributeValue.replaceAll("\\\\", "");
		
		String bundleName = i18nFacets.get(filterQuery.substring(0, indexOfColon));
		if (bundleName == null)
		{
			return attributeValue;
		}
		
		if (bundleName.startsWith("*"))
		{
			Map<String, String> codeValueMap = (Map<String, String>) request.getSession().getServletContext().getAttribute(bundleName.substring(1));
			String value = codeValueMap.get(attributeValue); 
			return value != null ? value : _3QM + attributeValue + _3QM;
		}
		
		try 		
		{
			ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
			if (request.getParameter("call") != null) {
				return bundle.getString(request.getParameter("call"));
			}
			return bundle.getString(attributeValue);
		} catch (MissingResourceException exception)
		{
			return _3QM + attributeValue + _3QM;
		}
	}
	
	
	public String getFilterValueAuthority (String filterQuery) {
		if(filterQuery != null && filterQuery.startsWith("authority_group")) { 
			int indexOfColon =  filterQuery.indexOf(":");
			return filterQuery.substring(indexOfColon + 1).replace("\"", "").replaceAll("\\*", "");
		}
		return null;
	}

	
	
	/**
	 * Returns the localized label associated with the given federated filter (name).
	 *
	 * @param request the HTTP request.
	 * @param filterQuery the filter query (name:"value").
	 * @return the localized label associated with the given filter (name).
	 */
	public String getHumanReadableFederatedLabel(final HttpServletRequest request, final String filterQuery)
	{
		int startIndex = filterQuery.indexOf("xt:");
		if (startIndex != -1)
		{		
			String attributeValue = filterQuery.substring(startIndex + 3);
			return getExternalTargetName(attributeValue, request);
		} else 
		{
			Locale locale = ((Visit)request.getSession(true).getAttribute(HttpAttribute.VISIT)).getPreferredLocale();
			return getHumanReadableLabel(request, filterQuery, locale);
		}
	}	
	
	/**
	 * Returns the localized label associated with the given facet name.
	 *
	 * @param request the HTTP request.
	 * @param facetName the facet name.
	 * @return the localized label associated with the given filter (name) or the original label.
	 */
	public String getI18nLabel(ResourceBundle bundle, final String facetName)
	{
		try 		
		{
			return bundle.getString(facetName);
		} catch (MissingResourceException exception)
		{	
			return  _3QM + facetName + _3QM;
		}
	}		
	
	/**
	 * Returns the localized label associated with the given facet name.
	 *
	 * @param request the HTTP request.
	 * @param facetName the facet name.
	 * @return the localized label associated with the given filter (name) or the original label.
	 */
	public String getI18nLabel(final ResourceBundle bundle, final ResourceBundle altBundle, final String facetName)
	{
		try 		
		{
			return bundle.getString(facetName);
			
		} catch (MissingResourceException exception)
		{	
			try  
			{
				return altBundle.getString(facetName);
			} catch (MissingResourceException ex) 
			{
				return  _3QM + facetName + _3QM;
			}
		}
	}		
	
	/**
	 * Returns the localized label associated with the given facet name.
	 *
	 * @param request the HTTP request.
	 * @param facetName the facet name.
	 * @return the localized label associated with the given filter (name) or the original label.
	 */
	public String getHumanReadableLabel(final HttpServletRequest request, final String facetName)
	{
		try 		
		{
			Locale locale = ((Visit)request.getSession(true).getAttribute(HttpAttribute.VISIT)).getPreferredLocale();
			ResourceBundle bundle = ResourceBundle.getBundle("resources", locale);
			return bundle.getString(facetName);
		} catch (MissingResourceException exception)
		{
			return  facetName;
		}
	}		
	
	public ResourceBundle getResourceBundle(final HttpServletRequest request, final String facetName)
	{
		String bundleName = i18nFacets.get(facetName);
		if (bundleName == null)
		{
			return null;
		}
		
		Locale locale = ((Visit)request.getSession(true).getAttribute(HttpAttribute.VISIT)).getPreferredLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
		return bundle;
	}

	public String getResourceBundleName(final HttpServletRequest request, final String facetName)
	{
		return i18nFacets.get(facetName);
	}
	
	/**
	 * Injects within this handler i18n resource bundle names.
	 * Names must be comma separated.
	 * 
	 * @param mappings the comma separated list of bundle names.
	 */
	public void setI18nMappings(final String mappings)
	{
		String [] facets = mappings.split(",");
		for (String facetName : facets)
		{
			int indexOfSlash = facetName.indexOf("/");
			String attributeName = (indexOfSlash == -1) ? facetName : facetName.substring(0, indexOfSlash);
			String bundleName = (indexOfSlash == -1) ? facetName : facetName.substring(indexOfSlash + 1);
			i18nFacets.put(attributeName, bundleName);		
		}
	}
	
	/**
	 * Returns the given string as MARC21 raw data.
	 * FIXME: This shouldn't be here!!!
	 * 
	 * @param rawData the MARC 21 (string) data.
	 * @return a MARC21 string
	 * @throws IOException in case the conversion fails.
	 */
	public final  String toMarc21(final String rawData) throws IOException
	{
		InputStream inputStream = null;
		try 
		{
			byte [] bytes = rawData.getBytes("UTF-8");
			inputStream = new ByteArrayInputStream(bytes);
			
			Record record = null;
			MarcReader reader = new MarcXmlReader(inputStream);
			if (reader.hasNext())
			{
				record = reader.next();
			}
			if (record != null)
			{
				return record.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>");
			}
		} catch (Exception exception) 
		{
			exception.printStackTrace();
		} 
		return "";
	}
	
	/**
	 * Returns the external name of the given (federated) resource identifier.
	 * 
	 * @param id the resource identifier.
	 * @param request the HTTP request.
	 * @return the external name of the given (federated) resource identifier.
	 */
	@SuppressWarnings("unchecked")
	private String getExternalTargetName(final String id, final HttpServletRequest request)
	{
		String result = null;
		Map<String, String> federatedTargets = (Map<String, String>) request.getSession(true).getServletContext().getAttribute(HttpAttribute.FEDERATED_SEARCH_TARGETS);
		if (federatedTargets != null)
		{
			result =  federatedTargets.get(id);
		}
		return result != null ? result : "Unknown";
	}
}
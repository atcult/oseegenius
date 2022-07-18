package com.atc.osee.web.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.bcel.generic.ReturnaddressType;
import org.apache.solr.common.util.Hash;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.SkipSetters;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.SafeConfig;

import com.atc.osee.web.XsltUtility;
import com.atc.osee.web.comparators.AdvancedFilterComparator;
import com.atc.osee.web.comparators.FacetComparator;
import com.atc.osee.web.folio.FolioConstants;

/**
 * String manipulation utility.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
@DefaultKey("string")
@SkipSetters
@ValidScope(Scope.APPLICATION)
public class StringManipulationTool extends SafeConfig
{
	private Random random = new Random();
	
	public String shorten(String value, int maxLength)
	{
		if (value != null && value.length() > maxLength)
		{
			return value.subSequence(0,  maxLength) + "...";
		}
		return value;
	}
	
	public int getRandomValue()
	{
		return random.nextInt(10);
	}
	
	/**
	 * Removes from the given "original" string the second parameter (toRemove).
	 * 
	 * @param original the original string.
	 * @param toRemove the string that has to be removed.
	 * @return a new string with the toRemove value removed.
	 */
	public String remove(String original, String toRemove)
	{	
		toRemove = toRemove
				.replaceAll("\\)", "\\\\)")
				.replaceAll("\\(","\\\\(")
				.replaceAll("\\]", "\\\\]")
				.replaceAll("\\[","\\\\[");
		return original != null ? original.replaceAll(toRemove, "").trim().replace("#","%23") : "";
	}
	
	/**
	 * Removes from the given "original" string the second parameter (toRemove).
	 * 
	 * @param original the original string.
	 * @param clauseToRemove the string that has to be removed.
	 * @return a new string with the toRemove value removed.
	 */
	public String removeFilterClause(String original, String clauseToRemove, String filter)
	{	
		if (original == null) 
		{
			return "";
		} 
		
		try {
			original = URLDecoder.decode(original, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			original = URLDecoder.decode(original);
		}
		
		String newClause = filter.replaceAll(Pattern.quote(clauseToRemove), "");
		
		if (newClause.startsWith(" OR "))
		{
			newClause = newClause.substring(4);
		} else if (newClause.endsWith(" OR "))
		{
			newClause = newClause.substring(0, newClause.length() - 4);
		} else 
		{
			newClause.replaceAll("OR%22%22OR", "OR");
			newClause = newClause.replaceAll("OR  OR", "OR");
		}
		
		return original.replaceFirst(Pattern.quote(filter), newClause).trim().replace("#","%23");
	}	
	
	/**
	 * This method was created for IMSS, to load their collections in precise order
	 * @param language
	 * @return an ordered hashmap of collections
	 */

	public HashMap<Integer, String> getCollections (String language) {
		//precise order for IMSS 
		
		String [] orderArray = {"0","2","19","3", "4", "5", "14", "12", "1", "13", "16", "20", "15"}; 
		LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>();
		Properties properties = new Properties();
		String languageToCall = (language == null || "".equals(language)) ? "it" : language;	
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("collections_" + languageToCall + ".properties");
		  try {
			 
	          properties.load(in);	         
	          for(String key : orderArray){
	        	  if (properties.getProperty(key) != null) {
	        		  map.put(Integer.parseInt(key), properties.getProperty(key));
	        	  }
	          }
	      } catch (IOException e) {
	          e.printStackTrace();
	      }				
		return map;		
	}
	
	/**
	 * 
	 * @param text
	 * @return reduced form of text
	 */
	
	public String reduceText (String text){
		int LIMIT_LENGTH = 170;
		if(text.length() < LIMIT_LENGTH) {
			return text;
		}
		else {
			String newText = text.substring(LIMIT_LENGTH, text.length());
			Integer indexOfFirstSpace = newText.indexOf(" ");
			newText = text.substring(0, LIMIT_LENGTH + indexOfFirstSpace);
			return newText + "...";
		}
	}
	
	/**
	 * Method to put custom sorting order in a hashMap from properties file.
	 * Created for IMSS for custom order in facet's elements
	 * 
	 * @param propertiesFileName
	 * @return map with custom order to sort 
	 */
	public Map<String, Integer> getOrderMap (String propertiesFileName) {	
		Map<String, Integer> map = new HashMap<String, Integer>();
		Properties properties = new Properties();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(propertiesFileName + "_order.properties");
		try {
			properties.load(in);	         
			for (String key : properties.stringPropertyNames()) {
				map.put(key, Integer.parseInt(properties.getProperty(key)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
    }		
	
	/**
	 * This method orders the input list with order specified in propertiesFileName
	 * facetOrFilter parameter chooses the right comparator. There are two different comparators:
	 * one for facets and one for advanced search filters
	 * Created for IMSS
	 * @param list
	 * @param propertiesFileName
	 * @param facetOrFilter  (FILTER or FACET) 
	 */
	
	public void customSorting (List list, String propertiesFileName, String facetOrFilter) {
		Map<String, Integer> order = getOrderMap(propertiesFileName);
		if (list != null){
			if("FILTER".equals(facetOrFilter)){			
				Collections.sort(list, new AdvancedFilterComparator(order));		
			}
			else {
				Collections.sort(list, new FacetComparator(order));		
			}
		}
	}
	
	/**
	 * 
	 * @param uri, a String where I can find an uri (http://...) or not
	 * @return a String with uri address only if I found it. If not, return null 
	 */
	
	public String getUri(String uri) {
		if(uri != null) {
			int indexOf = uri.indexOf("http");
			if(indexOf > -1) {
				return uri.substring(indexOf);
			}				
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param uri, a String where I can find label 'z' and label '3' (tag 856)
	 * @return a String with labels only if I found it. If not, return null 
	 */
	
	public String getLabelUri(String uri) {
		if(uri != null) {
			int indexOf = uri.indexOf("http");
			if(indexOf > -1) {
				return uri.substring(0,indexOf);
			}				
		}
		return null;
	}
	
	/**
	 * 
	 * @param text, a String where I can find an uri (http://...) or not
	 * @return a String with uri address only if I found it. If not, return null 
	 */
	
	public List<String> getUris(String text) {
		List<String> uris = new ArrayList<String>();
		if(text != null) {
			Matcher m = Pattern.compile("http([^\\s]+)")
				     .matcher(text);
			while (m.find()) {
				if(m.group() != null && m.group().startsWith("http")) {
					uris.add(m.group());
				}
				else {
					uris.add("http" + m.group());
				}
			}
		}
		return uris;
	}
	
	/**
	 * 
	 * @param text
	 * @return  a string with clickable uri
	 */
	public String makeClickable (String text) {
		if(text != null) {
			return text.replaceAll("http([^\\s]+)", "<a target='_blank' href='http$1'><img src='img/www.gif'></a>");
		}
		return text;
	}
	
	public String makeClickableStylizable (String text, String classHtml) {
		if(text != null) {
			return text.replaceAll("http([^\\s]+)", "<a target='_blank' class='" + classHtml + "' href='http$1'>http$1</a>");
		}
		return text;
	}
	
	/**
	 * 
	 * @param text
	 * @return  a string with clickable uri
	 */
	public String getNoUris (String text) {
		if(text != null) {
			return text.replaceAll("http([^\\s]+)", "");
		}
		return text;
	}
	
	public boolean isPositive (String text) {
		if (text != null) {
			try {
				int number = Integer.parseInt(text);
				return number > 0;
			}catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
	
	//punctuation decorator (like in indexer)
	public String decorate(final String value) 
	{
		if (value == null )
		{
			return value;
		}	
		
		return value.replaceAll(";", "").replaceAll("[.]", "").replaceAll(",", "");
	}
	
	/**
	 * Get Cluster title to label cluster work's facet
	 * @param text (es. 14355:_:Titolo del cluster)
	 * @return cluster name. If input isn't a cluster (doesn't have separator :_:) return input (right label)
	 */
	
	public String getClusterTitleF4Facet(String text) {
		final String SEPARATOR = ":_:";
		String [] array = text.split(SEPARATOR);
		try {
			return array[1].trim();
		}catch(Exception e){
			return text;
		}
	}
	/**
	 * Get Cluster title to label cluster work's facet
	 * @param text (es. 14355 :_: Titolo del cluster)
	 * @return cluster name. If input isn't a cluster (doesn't have separator :_:) return input (right label)
	 */
	
	public String getClusterIdClstrF4Facet(String text) {
		final String SEPARATOR = ":_:";
		String [] array = text.split(SEPARATOR);
		if(array.length < 2) {
			//I didn't find the separator, there is no cluster id
			return null;
		}
		else {
			return array[0].trim();
		}
	}
	
	/**
	 * convert the given marc_xml with a given xslt
	 * @param request 
	 * @param locale language
	 * @param marcXml 
	 * @param xsltFileName 
	 * @return
	 */
	public String convertXslt(HttpServletRequest request, Locale locale, String marcXml, String xsltFileName) {
		org.w3c.dom.Document recordDetail = XsltUtility.transformXSLT(request, locale, marcXml, xsltFileName);
		return XsltUtility.printString(recordDetail);
	}
	
	/**
	 * convert the given marc_xml with a given xslt and at least on externla param
	 * @param request 
	 * @param locale language
	 * @param marcXml 
	 * @param xsltFileName
	 * @param ExtParams
	 * @return
	 */
	public String convertXsltWebTable(HttpServletRequest request, Locale locale, String marcXml, String xsltFileName, HashMap<String, String> ExtParams) {
		org.w3c.dom.Document recordDetail = XsltUtility.transformXSLT(request, locale, marcXml, xsltFileName, ExtParams);
		return XsltUtility.printString(recordDetail);
	}
	
	
	
	/**
	 * Check if at least one terms in a phrase is contained in text
	 * @param phrase
	 * @param text
	 * @return
	 */
	public boolean containsTerms (String phrase, String text) {
		if (phrase != null && text != null) {
			String [] terms = phrase.split(" ");
			for (String currentTerms : terms) {
				if (text.toLowerCase().contains(currentTerms.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String showSimpleDate (final String date) {
		String result = date;
		if (date != null) {
			if (date.indexOf("T") > -1) {				
				result = date.substring(0, date.indexOf("T"));	
			}
			if(date.indexOf(" ") > -1) {
				result = date.substring(0, date.indexOf(" "));
			}
		}
		return result;
	}
	
	/**	 * 
	 * Compare two string without their accented  and special sequence like "&#34;"
	 * @param term1
	 * @param term2
	 * @return
	 */
	public boolean compareNormalization (final String term1, final String term2) {
		if (term1 == null || term1 == null) return false;
		return (normalizationReplace(term1)).equals(normalizationReplace(term2));
	}
	
	public String normalizationReplace(final String string) {
		if (string != null) {
			return string.replaceAll("&#(\\d)*;", "")
					.replaceAll("'", "")
					.replaceAll("ì", "")
					.replaceAll("ò", "")
					.replaceAll("ù", "")
					.replaceAll("é", "")
					.replaceAll("è", "")
					.replaceAll("à", "");
		}
		return string;
		
	}
	
	/**
	 * check if barcode start with one of the bncf prefixes for not catalog item
	 * @param barcode
	 * @return true if item comes from not catalog resource
	 */
	public boolean isNotCatalogItem(final String barcode) {
		if (barcode != null) {
			return barcode.startsWith(FolioConstants.MONOGRAPH_AND_SERIAL_PREFIX) ||
				barcode.startsWith(FolioConstants.MANUSCRIPT_PREFIX) ||
				barcode.startsWith(FolioConstants.SERIAL_OPAC_PREFIX);
					
		}
		return true;
	}
	
	public String dateFormatter(final String text) {
		StringBuffer result = new StringBuffer();
		if (text != null && text.length() >= 8) {			 
			//is digit or not? (like "304 a.C")
			System.out.println(text.substring(0, 8));
			try {
				Integer.parseInt(text.substring(0, 8));
			}
			catch (NumberFormatException e) {
				return text;
			}			
			for (int i = 0; i < text.length(); i++ ) {
				switch (i) {
					case 4 :
						result.append("-");
						break;
					case 6 :
						result.append("-");
						break;
					default:
						//do nothing
						break;
				}

				result.append(text.charAt(i));
			}
			return result.toString();
		}
		return text;
	}
	
	
	/**
	 * 
	 * @param text
	 * @return  sanitized strig for xss
	 */
	public String escape (String text) {
		if(text != null) {
			return text.replaceAll("[></\\\\]", " ");
		}
		return text;
	}
	
	
	/**
	 * 
	 * @param text
	 * @return  sanitized strig for xss, remove quotes
	 */
	public String escapeQuotes (String text) {
		if(text != null) {
			String ret=text.replaceAll("\"", " ");
			ret=ret.replaceAll("\'", " ");
			return ret;
		}
		return text;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
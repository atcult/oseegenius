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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.poi.hssf.record.formula.functions.Replace;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.servlets.search.AdvancedSearchServlet;

/**
 * OseeGenius -W- Highlighring Tool.
 * A Velocity tool that highlights user entered query terms on search results.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
@DefaultKey("highlighter")
@ValidScope(Scope.APPLICATION)
public class HighlightingTool extends SafeConfig
{	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchServlet.class);
	private final String pre = "<span class=\"highlighted\">";
	private final String post = "</span>";
	
	public static void main(String[] args) {
		HighlightingTool tool = new HighlightingTool();
		
		String [] tokens = "Sociologia della conoscenza e della cultura Sociologia e diffusione della cultura - Società della conoscenza - Cooperazione culturale, scientifica e tecnica - Beni culturali".split(IConstants.BLANK);
		String [] result = new String[tokens.length];
		for (int i = 0; i < tokens.length; i++)
		{ 
			final String v = tokens[i].trim();
			
			if (v.length() == 1 && !Character.isDigit(v.charAt(0)) && !Character.isLetter(v.charAt(0))) {
				tokens[i] = " ";
			} 
			result[i] = tokens[i].trim();
		}
		
//		System.out.println(Arrays.toString(result));
		
		System.out.println(tool.highlight(result, "Sociologia e diffusione della cultura - Società della conoscenza - Cooperazione culturale, scientifica e tecnica - Beni culturali."));	
	}
	
	/**
	 * Starting from a given query and query type returns the query tokens.
	 * 
	 * @param request the HTTP request.
	 * @return the query tokens.
	 */
	public String [] createTokensFromQuery(final HttpServletRequest request)
	{
		String query = request.getParameter(HttpParameter.QUERY);
		if (query != null && query.trim().length() != 0)
		{
			if (!ISolrConstants.ADVANCED_SEARCH_QUERY_TYPE_NAME.equals(request.getParameter(HttpParameter.QUERY_TYPE)))
			{
				String [] tokens = query.split(IConstants.BLANK);
				String [] result = new String[tokens.length];
				for (int i = 0; i < tokens.length; i++)
				{
					result[i] = tokens[i].trim();
				}
				return result;
			} else 
			{
				String [] tokens = query.split("ORNOT|ANDNOT|OR|AND");
				String [] result = null;
				List<String> app = new ArrayList<String>();
				for (int i = 0; i < tokens.length; i++)
				{
					String token = tokens[i];
					int indexOfColon = token.indexOf(IConstants.COLON);
					String substringToken = token.substring(indexOfColon + 1).trim();
					String [] splittedToken = substringToken.split(IConstants.BLANK);
					for (int j = 0; j < splittedToken.length; j++){
						app.add(splittedToken[j]);
					}
				}
				result = new String[app.size()];
				result = app.toArray(result);
				return result;
			}
		} 
		return null;
	}
		
	/**
	 * Manipulates the given text in order to highlight the user-entered terms.
	 * Note that this is a minimal highlight (compared with the sibling function on SOLR side) so 
	 * it takes care only about lowecasing (e.g. no diacritics, no stemming, no synonims)
	 * 
	 * The style associated with the highlighting text can be modified by defining a class in your 
	 * stylesheet called "highlighted"
	 * 
	 * Note: I'm not a big fan of this function because it makes an intensive use / creation of strings on client side.
	 * 
	 * @param tokens the tokens that will be highlighted.
	 * @param text the text where the query terms have to be searched.
	 */
	
	public  String highlight(String [] tokens, final String text)
	{
		Set<String> noRepetedToken = null;
		String textReplaced = text;
		
		if (tokens != null && text != null && text.trim().length() != 0)
		{
			noRepetedToken = new HashSet<String>(Arrays.asList(tokens));
			for (String token : noRepetedToken)
			{
				token = token.replaceAll("[-!\"#$%&'()*+,./:;<=>?@[\\\\]^_`{|}~]","").trim();
				
				if (token.length() == 1 || token.length() == 0 )
				{
					continue;
				}
				try {
					Pattern replace = Pattern.compile("(?i)\\b"+token+"(?i)\\b");
				    Matcher matcher2 = replace.matcher(text);
				 						
				    if(matcher2.find()){
				    	textReplaced =  textReplaced.replaceFirst("(?i)\\b"+token+"(?i)\\b", pre+matcher2.group()+post);
				    }
				} catch (Exception e) {
					LOGGER.error("Error for " + token, e);					
				}
				
			}
			return textReplaced;
		}
		return text;

	}
	
}
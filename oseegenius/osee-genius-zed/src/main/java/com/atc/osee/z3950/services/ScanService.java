/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Affero General Public License version 3 as published by the Free 
 * Software Foundation with the addition of the following permission added to Section 
 * 15 as permitted in Section 7(a). 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. 
 * You should have received a copy of the GNU Affero General Public License along with this program;
 */
package com.atc.osee.z3950.services;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jzkit.ServiceDirectory.CollectionDescriptionDBO;
import org.jzkit.ServiceDirectory.SearchServiceDescriptionDBO;
import org.jzkit.configuration.api.Configuration;
import org.jzkit.search.provider.iface.ScanException;
import org.jzkit.search.provider.iface.ScanInformation;
import org.jzkit.search.provider.iface.ScanRequestInfo;
import org.jzkit.search.provider.iface.Scanable;
import org.jzkit.search.util.QueryModel.Internal.AttrValue;
import org.jzkit.z3950.util.TermInformation;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SOLR Scan service client.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ScanService extends SearchService implements Scanable 
{
	private static final Log LOGGER = LogFactory.getLog(ScanService.class);

	private final DocumentBuilderFactory documentBuilderFactory;
	private XPath xpath = XPathFactory.newInstance().newXPath();
	private XPathExpression headingExpression;
	private XPathExpression positionInResponseExpression;
	
	{
		try 
		{
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
		} catch(Exception exception)
		{
			throw new ExceptionInInitializerError(exception);
		}
		
		try
		{
			headingExpression =  xpath.compile("/response/lst[@name='heading']");
		} catch (Exception exception)
		{
			throw new ExceptionInInitializerError(exception);
		}
		
		try
		{
			positionInResponseExpression =  xpath.compile("/response/lst[@name='responseHeader']/int[@name='position-in-response']");
		} catch (Exception exception)
		{
			throw new ExceptionInInitializerError(exception);
		}
	}

	@Override
	public boolean isScanSupported() 
	{
		return true;
	}

	@Override
	public ScanInformation doScan(ScanRequestInfo request) throws ScanException 
	{
		if (request.step_size > 0) throw new ScanException("Only step size of zero supported");
		
		HttpURLConnection http = null;
		try 
		{
			String term = request.term_list_and_start_point.getTermAsString(false);
			AttrValue accessPoint = (AttrValue) request.term_list_and_start_point.getAccessPoint();
					
			Configuration jzkit_conf = (Configuration) applicationContext.getBean("JZKitConfig");
	  	  	CollectionDescriptionDBO ci = jzkit_conf.lookupCollectionDescription("PUG");
	  	  	SearchServiceDescriptionDBO ssd = ci.getSearchServiceDescription();		
			 
	  	  	String indexStrictName = ssd.getServiceSpecificTranslations().get(accessPoint.toString()).getValue();
	  	  	
	  	  	URL url = new URL(serviceUrl + "?i=" + indexStrictName + "_browse&s=" + request.number_of_terms_requested + "&from=" + URLEncoder.encode(term, "UTF-8") + "&pos=" + request.position_in_response);
	  	  	http = (HttpURLConnection) url.openConnection();

	  	  	final Vector<TermInformation> result = new Vector<TermInformation>();
	  	  	
	        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
	        
	        Document document = docBuilder.parse(http.getInputStream());
	        
	        int positionInResponse = 0;
	        
	        try 
	        {
	        	positionInResponse = Integer.parseInt(positionInResponseExpression.evaluate(document));
	        } catch(Exception exception)
	        {
	        	// Ignore
	        }
	        
	        if (positionInResponse < 0)
	        {
	        	throw new ScanException("Negative values for preferred position in response are not supported.");
	        }
	        
	        NodeList nodes = (NodeList) headingExpression.evaluate(document, XPathConstants.NODESET);
	        for (int i = 0; i < nodes.getLength(); i++)
	        {
	        	Node node = nodes.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE)
				{
		        	TermInformation heading = new TermInformation(null, 0);
					NodeList children = node.getChildNodes();
					for (int index = 0; index < children.getLength(); index++)
					{
						Node child = children.item(index);
						if (child.getNodeType() == Node.ELEMENT_NODE)
						{
							NamedNodeMap attributes = child.getAttributes();
							if (attributes != null)
							{
								Attr nameAttribute = (Attr) attributes.getNamedItem("name");
								if (nameAttribute != null)
								{
									String value = nameAttribute.getNodeValue();
									if ("count".equals(value))
									{
										heading.number_of_occurences = Integer.parseInt(child.getTextContent());
									} else if ("term".equals(value))
									{
										heading.the_term = child.getTextContent();											
									}
								}
							}
						}
					 }
					result.add(heading);
				}						
	        }
	
			ScanInformation information = new ScanInformation(result, positionInResponse);
			return information;
		} catch (Exception exception) 
		{
			LOGGER.error("Error during SCAN operation...see the stacktrace below for further information.", exception);
			throw new ScanException(exception.getMessage());
		} finally 
		{
			try 
			{
				http.disconnect();
			} catch (Exception ignore) 
			{
				// Nothing to be done here...
			}
		}
	}
}
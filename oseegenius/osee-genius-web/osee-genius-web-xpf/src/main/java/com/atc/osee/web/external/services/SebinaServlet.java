package com.atc.osee.web.external.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atc.osee.web.Layout;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.model.SubscriptionSebina;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;

public class SebinaServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 587595502857856360L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		final String subscriptionCode = request.getParameter("subscriptionCode");
		final String libraryCode = request.getParameter("institution");
		final Integer yearId = (request.getParameter("yearId") != null ?Integer.parseInt(request.getParameter("yearId")) :null);		
		
		try {
			ConfigurationTool configuration = getConfiguration(request);
			String requestXml = null;
			if (yearId != null)
				requestXml = composeRequestXML(yearId);
			else
				requestXml = composeRequestXML(libraryCode, subscriptionCode);
			
			String responseXml = makePostRequest(requestXml, configuration.getSebinaServiceUrl());
			List<SubscriptionSebina> list = parseResponse(responseXml, yearId != null);
			setRequestAttribute(request, "subscriptionCode", subscriptionCode);
			if (yearId != null)
				setRequestAttribute(request, "detail", list);
			else
				setRequestAttribute(request, "subscriptions", list);			
			
			forwardTo(request, response, "/components/circulation/sebinaResult.vm", Layout.HOME_PAGE);
			
		} catch (Exception e) {
			Log.error("Error during call Sabina-API.", e);
			request.setAttribute("inError", true);
			
			if (e instanceof ArrayIndexOutOfBoundsException){
				request.setAttribute("errorMessage", "Error during call Sabina-API.");
				request.setAttribute("errorCode", "sebAPIErrorData");
				forwardTo(request, response, "Error.vm", Layout.HOME_PAGE);
			}
			
			request.setAttribute("errorMessage", "Error during call Sabina-API.");
			request.setAttribute("errorCode", "sebAPIWrong");
			forwardTo(request, response, "Error.vm", Layout.HOME_PAGE);
			return;
		}
	}
	
	private String makePostRequest(String requestXml, String sebinaServiceUrl) throws IOException {
		
		PostMethod post = new PostMethod(sebinaServiceUrl);
		RequestEntity reqentity = new StringRequestEntity(requestXml, "text/xml", "UTF-8");
		post.setRequestEntity(reqentity);
		post.setFollowRedirects(false);
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(20000);
		String responseXml = null;
		String outputString = "";
		try {
			int rc = client.executeMethod(post);
		    if (rc != 200) {
			  Log.error("An error occured during Sebina call api: %s /n %s",  post.getStatusText(), requestXml);
		    } else {		            
  
		    	InputStreamReader isr = new InputStreamReader(post.getResponseBodyAsStream(), Charset.forName("UTF-8"));
		    	BufferedReader in = new BufferedReader(isr);

				while ((responseXml = in.readLine()) != null) {
					outputString = outputString + responseXml;
				}	
				
				Log.debug(outputString);
		    }
		} finally {
			post.releaseConnection();
		}
		return outputString;
	}

	/**
	 * Call Sebina api for years in subscription.
	 * 
	 * @param libraryCode -- the library code
	 * @param subscriptionCode -- the subscription code.
	 * @return an xml string request.
	 */
	private String composeRequestXML(String libraryCode, String subscriptionCode) {
		StringBuffer req = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		req.append("<MServDoc xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		req.append("xsi:noNamespaceSchemaLocation=\"picos_sbn.xsd\">");
		req.append("<askfasc><xabbfas kbibl=\"").append(libraryCode);
		req.append("\" kordi=\"").append(subscriptionCode).append("\"/></askfasc>");
		req.append("</MServDoc>");
	    return req.toString();
	}

	/**
	 * Call a single year detail.
	 * 
	 * @param yearId -- the year id.
	 * @return an xml string request.
	 */
	private String composeRequestXML(Integer yearId) {
		StringBuffer req = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		req.append("<MServDoc xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		req.append("xsi:noNamespaceSchemaLocation=\"picos_sbn.xsd\">");
		req.append("<askfasc><idannata>").append(yearId).append("</idannata></askfasc>");
		req.append("</MServDoc>");
	    return req.toString();
	}
		
	private List<SubscriptionSebina> parseResponse(final String in, boolean idDetail) throws IOException, ParserConfigurationException, SAXException {
		
		List<SubscriptionSebina> list = new ArrayList<SubscriptionSebina>();
		try {			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			Document docResponse = db.parse(is);
			docResponse.getDocumentElement().normalize();
			
			Element documentElement = docResponse.getDocumentElement();
			NodeList nodeLst = documentElement.getElementsByTagName("annata");
			SubscriptionSebina item = null;
			for (int i = 0; i < nodeLst.getLength(); i++) 
			{
				item = new SubscriptionSebina();
				Node node = nodeLst.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element e = (Element)node;
					item.setYearId(Integer.parseInt(e.getAttribute("idannata")));
					item.setYearDesc(Integer.parseInt(e.getAttribute("anno")));
					item.setSubcriptionCode(e.getAttribute("kordi"));
					item.setPossessed(e.getAttribute("posseduto"));
					String bibl = e.getAttribute("kbibl").trim();
					String[] app = e.getAttribute("ninvent").split(bibl);
					if (app.length > 0)	{				
						item.setInventory(app[1].trim());
					    item.setCollocation(app[2].trim().replaceAll(" +", " "));
					}
					list.add(item);
				}
			}
			
			//detail
			if (documentElement.getElementsByTagName("sezione") != null && idDetail) {
				nodeLst = documentElement.getElementsByTagName("fascicolo");
				if (nodeLst.getLength() > -1) 
				{
					for (int i = 0; i < nodeLst.getLength(); i++) 
					{
						if (i == 0)
						{
							Node node = nodeLst.item(0);
							if (node != null && node.getNodeType() == Node.ELEMENT_NODE) 
							{
								Element e = (Element)node;
								item = list.get(0); 
								item.setLabel(e.getAttribute("etichetta"));
								item.setDate(e.getAttribute("data"));
								item.setStatus(e.getAttribute("statofas"));					
							}
						}
						
						if (i > 0)
						{
							Node node = nodeLst.item(i);
							SubscriptionSebina fascic = null;
							fascic = new SubscriptionSebina();
							if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
							{
								Element e = (Element)node;
								fascic.setLabel(e.getAttribute("etichetta"));
								fascic.setDate(e.getAttribute("data"));
								fascic.setStatus(e.getAttribute("statofas"));
								list.add(fascic);
							}
						}
					}
				}
			}
			
			
		} catch (IOException e) {
			throw e;
		} catch (ParserConfigurationException e) {			
			throw e;
		} catch (SAXException e) {
			throw e;
		}
		
		return list;
		
	}
}

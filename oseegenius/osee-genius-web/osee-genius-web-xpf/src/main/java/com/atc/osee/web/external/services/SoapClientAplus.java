package com.atc.osee.web.external.services;

/**
 * Soap client class for APLUS webService. 
 * 
 * @author Natascia Bianchini
 * @since 1.2
 *
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SoapClientAplus {
	
	/**
	 * Call soap aplus web service to insert a new user in aplus database. 
	 * 
	 * @param wsEndPoint -- the endpoint of web service
	 * @param name -- the name of user
	 * @param surname -- the surname of user
	 * @param cfu -- barcode of user
	 * @return a long number representing user identifier, if it's negative number, code error message.
	 * @throws Exception -- in case of Exception
	 */
	public long handleUserByCFU(final HttpServletRequest request, final String wsEndPoint, final String name, final String surname, final String cfu) throws Exception {
		try {
			String responseString = "";
			String outputString = "";			
			URL url = new URL(wsEndPoint);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();						   		
			
			String pathRequestXml = request.getSession().getServletContext().getRealPath("/soap/request.xml");
			String xmlInput = createRequestHandleUserByCFU(pathRequestXml, name, surname, cfu);
			
			byte[] buffer = new byte[xmlInput.length()];
			buffer = xmlInput.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();
			String SOAPAction = "urn:AxwSoapWebServiceIntf-IAxwSoapWebService#HandleUserByCFU";
			
			httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
			httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			httpConn.setRequestProperty("SOAPAction", SOAPAction);
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);

			OutputStream out = httpConn.getOutputStream();
			out.write(b);
			out.close();
			
			InputStreamReader isr = new InputStreamReader(httpConn.getInputStream(), Charset.forName("UTF-8"));
			BufferedReader in = new BufferedReader(isr);

			while ((responseString = in.readLine()) != null) {
				outputString = outputString + responseString;
			}
			
			return getResult(parseXmlFile(outputString));											
			
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	/**
	 * Read and get response code.
	 * 
	 * @param docResponse -- the document response to read.
	 * @return
	 */
	private long getResult(Document docResponse) {
		
		long result = 0;
		Element documentElement = docResponse.getDocumentElement();
		NodeList nodeLst = documentElement.getElementsByTagName("ns1:HandleUserByCFUResponse");
		for (int i = 0; i < nodeLst.getLength(); i++) 
		{
			Node node = nodeLst.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				Element e = (Element)node;
				result = Long.parseLong(e.getElementsByTagName("return").item(0).getTextContent());
			}
		}

		return result;
	}

	/**
	 * 
	 * @param name
	 * @param surname
	 * @param cfu
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	private String createRequestHandleUserByCFU(final String pathRequestXml, final String name, final String surname, final String cfu) throws SAXException, IOException, ParserConfigurationException, TransformerException {
		
		InputStream is = new FileInputStream(pathRequestXml);
		Document doc = DocumentBuilderFactory.newInstance()
	            .newDocumentBuilder()
	            .parse(is);
		replaceParams(doc, name, surname, cfu);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();		
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult xmlOutput = new StreamResult(new StringWriter());
		transformer.transform(source, xmlOutput);
		
		return xmlOutput.getWriter().toString();
		
	}
	
	/**
	 * Replace empty values with placeholder.
	 * 
	 * @param doc -- the document xml
	 * @param name -- the user name
	 * @param surname -- the user surname
	 * @param cfu -- the user barcode 
	 */
	private void replaceParams(final Document doc, final String name, final String surname, final String cfu ) {
						
		Element documentElement = doc.getDocumentElement();
		NodeList nodeLst = documentElement.getElementsByTagName("urn:HandleUserByCFU");
		for (int i = 0; i < nodeLst.getLength(); i++) 
		{
			Node node = nodeLst.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{	
				Element e = (Element)node;
				Node n = e.getElementsByTagName("Name").item(0);				
				n.setTextContent(name);			
				
				Node n2 = e.getElementsByTagName("Surname").item(0);				
				n2.setTextContent(surname);
				
				Node n3 = e.getElementsByTagName("CFU").item(0);				
				n3.setTextContent(cfu);
			}
		}
	}
	
	/**
	 * Parse response xml.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private Document parseXmlFile(final String in) throws IOException, ParserConfigurationException, SAXException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
			
		} catch (IOException e) {
			throw e;
		} catch (ParserConfigurationException e) {			
			throw e;
		} catch (SAXException e) {
			throw e;
		}
	}


}

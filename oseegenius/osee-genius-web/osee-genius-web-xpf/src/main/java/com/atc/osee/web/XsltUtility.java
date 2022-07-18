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
package com.atc.osee.web;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XsltUtility  {

/**
 * Method used when there are different xsl for different language and without parameters
 */
public static Document transformXSLT (HttpServletRequest request, Locale languageLocale, String xml, String fileName){
	String language;
	if ("it".equals(languageLocale.toString())){
		language = "_ita";
	}
	else {
		language = "_eng";
	}
	return transformXSL(request, xml, fileName, language, null);
}

/**
 * Method used when there are different xsl for different language and with parameters
 */
public static Document transformXSLT (HttpServletRequest request, Locale languageLocale, String xml, String fileName,  HashMap<String, String> XSLTparameters){
	String language;
	if ("it".equals(languageLocale.toString())){
		language = "_ita";
	}
	else {
		language = "_eng";
	}
	return transformXSL(request, xml, fileName, language, XSLTparameters);
}

/**
 * Method used where there is only one xsl for every language and without external parameters
 */
public static Document transformXSLT (HttpServletRequest request, String xml, String fileName){
	return transformXSL(request, xml, fileName, "", null);
}

/**
* Method used where there is only one xsl for every language and with external parameters
*/
public static Document transformXSLT (HttpServletRequest request, String xml, String fileName, HashMap<String, String> XSLTparameters){
	return transformXSL(request, xml, fileName, "", XSLTparameters);
}


/**
 * Method used where there with language and without external parameters
 */
public static Document transformXSL(HttpServletRequest request, String xml, String fileName, String language) {
	return transformXSL(request, xml,  fileName, language, null);
}


/**
 * Method with all param
 * @param servletContext
 * @param xml
 * @param fileName
 * @param language string
 * @param external keys/values to inject into XSLT
 * @return Document xhtml
 */

protected static Document transformXSL(HttpServletRequest request, String xml, String fileName, String language, HashMap<String, String> XSLTparameters)
		throws TransformerFactoryConfigurationError {
	ServletContext servletContext = request.getSession().getServletContext();
	String relativePath = "/xslt/" + fileName + language + ".xsl";
	//System.out.println(relativePath);
	String stylesheet = servletContext.getRealPath(relativePath);
	File file = new File(stylesheet);	
	Document xmlStyledDocument = null;
	try {
		// load the transformer using JAXP
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(new StreamSource(file));
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		// First set some parameters if there are any		
		if (XSLTparameters != null) {
			Iterator iterator = XSLTparameters.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				transformer.setParameter(entry.getKey().toString(), entry
						.getValue().toString());
			}
		}
					
		transformer.setParameter("BASE_URL", request.getRequestURL().toString().replace(request.getServletPath(), "") + "/");
		
		// now lets style the given document	
		Document sourceDocument = null;
		try{
			sourceDocument = toXmlDocument(xml);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		DOMSource source = new DOMSource(sourceDocument);
		DocumentBuilderFactory documentBuilderFactory =	DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			xmlStyledDocument = documentBuilder.newDocument();
			//<?xml version=\"1.0\" encoding=\"UTF-8\"?>
			DOMResult result = new DOMResult(xmlStyledDocument);
			transformer.transform(source, result);
		} catch (ParserConfigurationException parserConfigurationException) {		
			parserConfigurationException.printStackTrace();
		}
	} catch (TransformerConfigurationException transformerConfigurationException) {
		transformerConfigurationException.printStackTrace();
	} catch (TransformerException transformerException) {
		transformerException.printStackTrace();		
	}
	//printDocument(xmlStyledDocument, "transformXSLT");
	return xmlStyledDocument;
}


public static Document toXmlDocument(String xml) throws SAXException, IOException, ParserConfigurationException {
		Document document;
		DocumentBuilder builder = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		builder = dbf.newDocumentBuilder();
		StringReader sr = new StringReader(xml);
		InputSource is = new InputSource(sr);
		document = builder.parse(is);	
		//printDocument(document, "toXmlDocument");
		return document;	
}

public static String transformXSLTtoString (HttpServletRequest request, Locale languageLocale, String xml, String fileName){
	String language;
	if ("it".equals(languageLocale.toString())){
		language = "_ita";
	}
	else {
		language = "_eng";
	}
	return transformXSLtoString(request, xml, fileName, language);
}

protected static String transformXSLtoString(HttpServletRequest request, String xml, String fileName, String language)
		throws TransformerFactoryConfigurationError {
	ServletContext servletContext = request.getSession().getServletContext();
	String relativePath = "/xslt/" + fileName + language + ".xsl";
	//System.out.println(relativePath);
	String stylesheet = servletContext.getRealPath(relativePath);
	File file = new File(stylesheet);		
	StreamSource xslSource = new StreamSource(file);
	StringWriter xmlOutWriter = new StringWriter();
	try {
		StreamSource xmlInSource = new StreamSource(new StringReader(xml));
		Transformer tf = TransformerFactory.newInstance().newTransformer(xslSource);   
		tf.transform(xmlInSource, new StreamResult(xmlOutWriter));    
	}
	catch(Exception e){
		e.printStackTrace();
	}
	
	return xmlOutWriter.toString();
}


public static void printDocument (Document document, String position){
	DOMSource domSource = new DOMSource(document);
	StringWriter writer = new StringWriter();
	StreamResult result = new StreamResult(writer);
	TransformerFactory tf = TransformerFactory.newInstance();
	Transformer transformer;
	try {
		transformer = tf.newTransformer();
		transformer.transform(domSource, result);
		System.out.println("XML IN " + position + "is: \n" + writer.toString());
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}		
}

public static String printString(Document document){
	DOMSource domSource = new DOMSource(document);
	StringWriter writer = new StringWriter();
	StreamResult result = new StreamResult(writer);
	TransformerFactory tf = TransformerFactory.newInstance();
	Transformer transformer;
	try {
		transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(domSource, result);
		return writer.toString();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return "";
}
	
}

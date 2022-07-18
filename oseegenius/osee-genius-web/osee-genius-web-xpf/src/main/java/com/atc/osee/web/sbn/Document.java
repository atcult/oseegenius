/*
 * Created on 2013-06-25
 *
 */
package com.atc.osee.web.sbn;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

/**
 * Represents a retrieved item from a Z39.50 target. The item is in the form of
 * a pazpar2 xml document (http://www.indexdata.com/pazpar2/1.0) mapping the
 * users fields to the original target data.
 * 
 * @author paul
 * 
 */
public class Document {
    //private static final Logger logger = LogManager.getLogger(Document.class);
	private static final Logger LOGGER = LoggerFactory.getLogger(Document.class);
	
    private static XPathFactory xfac = XPathFactory.newInstance();
    private static XPath xpath = xfac.newXPath();
    private String metaData;
    private org.w3c.dom.Document dataMap;

    public Document(String s) {
        metaData = s;
        LOGGER.debug("Metadata: " + s);
        dataMap = parseData(s);
    }
    

    //per export
    public String getMetaData() {
		return metaData;
	}



	private org.w3c.dom.Document parseData(String s) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
        } catch (Exception e) {
        	LOGGER.warn(e.getMessage());
            return null;
        }
    }

    public String getFieldValue(String fieldName) {
        List<String> list = getFieldValues(fieldName);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<String> getFieldValues(String fieldName) {
        try {
            List<String> result = new ArrayList<String>();
            XPathExpression expr = xpath.compile("/collection/record/metadata[@type='"
                    + fieldName + "']");
            Object o = expr.evaluate(dataMap,
                    XPathConstants.NODESET);
            NodeList nodes = (NodeList)o;
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getTextContent());
            }
            return result;
        } catch (Exception e) {
        	LOGGER.warn(e.getMessage());
            return null;
        }
    }
}

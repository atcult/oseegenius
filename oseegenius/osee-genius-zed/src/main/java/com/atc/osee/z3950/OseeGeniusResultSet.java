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
package com.atc.osee.z3950;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Observer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.jzkit.search.provider.iface.ExplainDBInfoDTO;
import org.jzkit.search.provider.iface.IREvent;
import org.jzkit.search.provider.iface.IRQuery;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.util.RecordModel.ExplicitRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.InformationFragment;
import org.jzkit.search.util.RecordModel.InformationFragmentImpl;
import org.jzkit.search.util.RecordModel.RecordFormatSpecification;
import org.jzkit.search.util.ResultSet.AbstractIRResultSet;
import org.jzkit.search.util.ResultSet.IFSNotificationTarget;
import org.jzkit.search.util.ResultSet.IRResultSet;
import org.jzkit.search.util.ResultSet.IRResultSetException;
import org.jzkit.search.util.ResultSet.IRResultSetInfo;
import org.jzkit.search.util.ResultSet.IRResultSetStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ian Ibbotson
 * @version $Id: SOLRResultSet.java,v 1.2 2005/10/30 17:27:38 ibbo Exp $
 */
public class OseeGeniusResultSet extends AbstractIRResultSet implements IRResultSet 
{
	private static final Log LOGGER = LogFactory.getLog(OseeGeniusResultSet.class);
	private int hits;
	private String query;
	private String respositoryId;
	private String collectionName;
	private String baseUrl;
	private final String sort;
	private Map<String, String> field_lists;

	public OseeGeniusResultSet(String baseUrl, String query, String repositoryId, Map<String, String> field_lists, IRQuery irQuery, String sort) {
		this(null, baseUrl, query, repositoryId, field_lists, irQuery, sort);
	}

	public OseeGeniusResultSet(Observer[] observers, String baseUrl,
			String query, String repositoryId, Map<String, String> fieldList,
			IRQuery irQuery, String sort) 
	{
		super(observers);
		this.query = query;
		this.respositoryId = repositoryId;
		this.baseUrl = baseUrl;
		this.field_lists = fieldList;
		this.sort = sort;
		if ((irQuery.collections != null) && (irQuery.collections.size() > 0)) 
		{
			collectionName = irQuery.collections.get(0).toString();
		}
	}

	public void countHits() throws SearchException 
	{
		// Just count the hits, rows = 0
		requestRecords(1, 0, null); 
		setFragmentCount(hits);
	}

	private InformationFragment[] requestRecords(int firstRecord, int maxRecords, RecordFormatSpecification spec) throws SearchException 
	{
		InformationFragment[] result = null;

		int solrFirstRecord = firstRecord - 1;

		URL url = null;
		try 
		{
			String fl = "";
			LOGGER.debug("requestRecords " + firstRecord + "," + maxRecords + "," + spec);
			url = new URL(baseUrl + "?q=" + URLEncoder.encode(query)
					+ (sort != null ? "&sort=" + URLEncoder.encode(sort) : "")
					+ "&start=" + solrFirstRecord + "&rows=" + maxRecords + fl);

			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			
			// create a DocumentBuilderFactory and configure it
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

			// docFactory.setNamespaceAware(true);
			docFactory.setValidating(false);

			// create a DocumentBuilder that satisfies the constraints
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			try 
			{
				Document xml = docBuilder.parse(http.getInputStream());
				if (xml != null) 
				{
					Element namespaceNode = xml.createElement("nsnode");
					Node num_hits_node = XPathAPI.selectSingleNode(xml,"/response/result/@numFound", namespaceNode);
					if (num_hits_node != null) 
					{
						hits = Integer.parseInt(num_hits_node.getNodeValue());

						NodeList nl = XPathAPI.selectNodeList(xml, "/response/result/doc", namespaceNode);
						int num_recs = nl.getLength();
						result = new InformationFragment[num_recs];
						if (nl != null) 
						{
							for (int i = 0; i < num_recs; i++) 
							{
								Node record_node = nl.item(i);
								ExplicitRecordFormatSpecification record_spec = new ExplicitRecordFormatSpecification("xml:solr:F");
								Document new_result_document = docBuilder.newDocument();
								Node n = new_result_document.importNode(record_node, true);
								new_result_document.appendChild(n);
								result[i] = new InformationFragmentImpl(
										firstRecord + i, respositoryId,
										collectionName, null, new_result_document,
										record_spec);

								result[i].setHitNo(firstRecord + i);
								dumpResponseRecord(new_result_document);
							}
						} else 
						{
							LOGGER.info("No response records");
						}
					} else 
					{
						LOGGER.error("No numfound element in solr resuly");
					}
				} else 
				{
					LOGGER.error("No XML response document from SOLR server");
				}
			} catch (IllegalArgumentException iae) 
			{
				LOGGER.error("IllegalArgumentException SOLR response (" + url + ")", iae);
				this.setStatus(IRResultSetStatus.FAILURE);
				throw new SearchException(iae.getMessage(), iae);
			} catch (org.xml.sax.SAXException se) 
			{
				LOGGER.error("Error parsing SOLR response (" + url + ")", se);
				this.setStatus(IRResultSetStatus.FAILURE);
				throw new SearchException(se.getMessage(), se);
			}
		} catch (javax.xml.transform.TransformerException te) 
		{
			throw new SearchException(te.getMessage(), te);
		} catch (MalformedURLException mue) 
		{
			LOGGER.error("Malformed URL exception for SOLR request (" + url + ")", mue);
			throw new SearchException(mue.getMessage(), mue);
		} catch (ParserConfigurationException pce) 
		{
			throw new SearchException(pce.getMessage(), pce);
		} catch (java.io.IOException ie) 
		{
			LOGGER.error("Java IO exception for SOLR request (" + url + ")", ie);
			throw new SearchException(ie.getMessage(), ie);
		}
		return result;
	}

	// Fragment Source methods
	public InformationFragment[] getFragment(
			int startingFragment, 
			int count,
			RecordFormatSpecification spec) throws IRResultSetException 
	{
		InformationFragment[] result = null;
		try 
		{
			result = requestRecords(startingFragment, count, spec);
		} catch (SearchException se) 
		{
			LOGGER.error("Exception requesting records", se);
			throw new IRResultSetException("Problem with SOLR search in call to get fragment, query was " + query, se);
		}
		return result;
	}

	public void asyncGetFragment(int starting_fragment, int count, RecordFormatSpecification spec, IFSNotificationTarget target) 
	{
		try 
		{
			InformationFragment[] result = getFragment(starting_fragment, count, spec);
			target.notifyRecords(result);
		} catch (IRResultSetException re) {
			target.notifyError("SOLR", new Integer(0), "No reason = calling getFragment", re);
		}
	}

	public int getFragmentCount() 
	{
		return hits;
	}

	public int getRecordAvailableHWM() 
	{
		return hits;
	}

	/** Release all resources and shut down the object */
	public void close() 
	{
		// Nothing
	}

	public void setFragmentCount(int i) 
	{
		hits = i;
		IREvent e = new IREvent(IREvent.FRAGMENT_COUNT_CHANGE, new Integer(i));
		setChanged();
		notifyObservers(e);
	}

	public IRResultSetInfo getResultSetInfo() 
	{
		return new IRResultSetInfo(getResultSetName(), "SOLR", null,
				getFragmentCount(), getStatus(), null, getLastMessage());

	}

	public static org.jzkit.search.provider.iface.ExplainDTO explain(
			String base_url) {
		LOGGER.debug("Explaining..... " + base_url);
		org.jzkit.search.provider.iface.ExplainDTO result = new org.jzkit.search.provider.iface.ExplainDTO();

		try {
			URL url = new URL(base_url + "?version=1.1&operation=explain");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();

			docFactory.setNamespaceAware(true);
			docFactory.setValidating(false);

			// create a DocumentBuilder that satisfies the constraints
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			try {
				LOGGER.debug("Parsing response");
				Document xml = docBuilder.parse(http.getInputStream());

				if (xml != null) {
					LOGGER.debug("Processing Explain Response");
					Element namespaceNode = xml.createElement("nsnode");
					namespaceNode.setAttribute("xmlns:SRW",
							"http://www.loc.gov/zing/srw/");
					namespaceNode.setAttribute("xmlns:DIAG",
							"http://www.loc.gov/zing/srw/v1.0/diagnostic/");
					namespaceNode.setAttribute("xmlns:EXPLAIN",
							"http://explain.z3950.org/dtd/2.0/");

					Node server_info_node = XPathAPI
							.selectSingleNode(
									xml,
									"/SRW:explainResponse/SRW:record/SRW:recordData/EXPLAIN:explain/EXPLAIN:serverInfo",
									namespaceNode);
					Node database_info_node = XPathAPI
							.selectSingleNode(
									xml,
									"/SRW:explainResponse/SRW:record/SRW:recordData/EXPLAIN:explain/EXPLAIN:databaseInfo",
									namespaceNode);

					Node database_code_node = XPathAPI.selectSingleNode(
							server_info_node, "EXPLAIN:database/text()",
							namespaceNode);
					Node database_name_node = XPathAPI.selectSingleNode(xml,
							"EXPLAIN:title/text()", namespaceNode);
					Node database_desc_node = XPathAPI.selectSingleNode(xml,
							"EXPLAIN:description/text()", namespaceNode);

					String database_title = "Title Not Available";
					String database_desc = "Description Not Available";
					String database_code = null;

					if (database_name_node != null)
						database_title = database_name_node.getNodeValue();

					if (database_desc_node != null)
						database_desc = database_desc_node.getNodeValue();

					if (database_code_node != null)
						database_code = database_code_node.getNodeValue();
					else
						database_code = java.util.UUID.randomUUID().toString();

					result.setTitle(database_title);
					result.setDescription(database_desc);

					ExplainDBInfoDTO[] db_info = new ExplainDBInfoDTO[1];
					db_info[0] = new ExplainDBInfoDTO();
					db_info[0].setLocalCode(database_code);
					db_info[0].setTitle(database_title);
					db_info[0].setDescription(database_desc);
					result.setDatabases(db_info);
				} else {
					LOGGER.error("Error parsing explain response - no XML");
				}
			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
			} catch (org.xml.sax.SAXException se) {
				se.printStackTrace();
			}
		} catch (javax.xml.transform.TransformerException te) {
			te.printStackTrace();
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (java.io.IOException ie) {
			ie.printStackTrace();
		}

		LOGGER.debug("Result of explain: " + result);
		return result;
	}

	private void dumpResponseRecord(Document d) throws java.io.IOException {
		LOGGER.debug("dumpResponseRecord");
		OutputFormat format = new OutputFormat("xml", "utf-8", false);
		format.setOmitXMLDeclaration(true);
		java.io.StringWriter stringOut = new java.io.StringWriter();
		XMLSerializer serial = new XMLSerializer(stringOut, format);
		serial.setNamespaces(true);
		serial.asDOMSerializer();
		serial.serialize(d.getDocumentElement());
		LOGGER.debug("Result: " + stringOut.toString());
	}
}

package com.atc.osee.web.util;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.solr.common.SolrDocument;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.tools.ConfigurationTool;

public abstract class Utils 
{
	private static DocumentBuilder builder;
	private static XPath xpath = XPathFactory.newInstance().newXPath();
	
	static 
	{ 
		try 
		{
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (Exception exception)
		{
			throw new ExceptionInInitializerError();
		}
	}
	
	public static String getFederatedSearchEndpointString(final ServletContext application)
	{
		String federatedSearchEndpointUrl = System.getProperty(IConstants.FEDERATED_SEARCH_ENDPOINT_PARAMETER);
		if (federatedSearchEndpointUrl == null)
		{
			federatedSearchEndpointUrl = application.getInitParameter(IConstants.FEDERATED_SEARCH_ENDPOINT_PARAMETER);
		}
		return federatedSearchEndpointUrl;
	}
	
	public static String getSearchersString(final ServletContext application)
	{
		String urls = System.getProperty(IConstants.SEARCHER_URLS_PARAMETER);
		if (urls == null)
		{
			urls = application.getInitParameter(IConstants.SEARCHER_URLS_PARAMETER);
		}
		return urls;
	}

	/**
	 * Creates a {@link SolrDocument} starting from a "clustered" result.
	 * 
	 * @param clusteredRecord the clustered record (as string).
	 * @param recid the clustered record id.
	 * @param id the local record id.
	 */
	public static SolrDocument createSolrDocument(final String clusteredRecord, final String recid, final String id)
	{
		try 
		{
			final Document  document = builder.parse(new ByteArrayInputStream(clusteredRecord.getBytes("UTF-8")));
			final SolrDocument fakeSolrDocument = new SolrDocument();
			fakeSolrDocument.setField(ISolrConstants.ID_FIELD_NAME, id);
			   
			final XPathExpression idExpression =  xpath.compile("/record/location/md-id[text()=\"" + id +"\"]"); ;
			final Node node = (Node) idExpression.evaluate(document, XPathConstants.NODE);
			 
			final Node location = node.getParentNode();
			final NodeList children = location.getChildNodes();
			
			for (int i = 0; i < children.getLength(); i++)
			{
				final Node metadata = children.item(i);
				final String nodeName = metadata.getNodeName();
				if (metadata.getNodeType() == Node.ELEMENT_NODE)
				{
					if ("md-author".equals(nodeName) || "md-corporate".equals(nodeName) || "md-conference".equals(nodeName))
					{
						fakeSolrDocument.setField("author", metadata.getTextContent());
					} else if ("md-title".equals(nodeName))
					{
						fakeSolrDocument.addField("title", metadata.getTextContent());						
					} else if ("md-publisher".equals(nodeName))
					{
						multiple("publisher", fakeSolrDocument, metadata, false);
					} else if ("md-isbn".equals(nodeName))
					{
						multiple("isbn", fakeSolrDocument, metadata, false);
					} else if ("md-issn".equals(nodeName))
					{
						multiple("issn", fakeSolrDocument, metadata, false);
					} else if ("md-date".equals(nodeName))
					{
						fakeSolrDocument.setField("publication_date", metadata.getTextContent());									
					} else if ("md-physical-description".equals(nodeName))
					{
						multiple("physical_description", fakeSolrDocument, metadata, false);
					} else if ("md-other-author".equals(nodeName))
					{
						multiple("other_author_person", fakeSolrDocument, metadata, false);	
					} else if ("md-other-corporate".equals(nodeName))
					{
						multiple("other_author_corporate", fakeSolrDocument, metadata, false);	
					} else if ("md-other-conference".equals(nodeName))
					{
						multiple("other_author_conference", fakeSolrDocument, metadata, false);	
					} else if ("md-subject-person".equals(nodeName))
					{
						multiple("subject_person", fakeSolrDocument, metadata, true);
					} else if ("md-subject-corporate".equals(nodeName))
					{
						multiple("subject_corporate", fakeSolrDocument, metadata, true);
					} else if ("md-subject-conference".equals(nodeName))
					{
						multiple("subject_conference", fakeSolrDocument, metadata, true);
					} else if ("md-chronological_subject".equals(nodeName))
					{
						multiple("chronological_subject", fakeSolrDocument, metadata, true);
					} else if ("md-subject_uniform_title".equals(nodeName))
					{
						multiple("subject_uniform_title", fakeSolrDocument, metadata, true);
					} else if ("md-topical_subject".equals(nodeName))
					{
						multiple("topical_subject", fakeSolrDocument, metadata, true);
					} else if ("md-geographic_subject".equals(nodeName))
					{
						multiple("geographic_subject", fakeSolrDocument, metadata, true);
					} else if ("md-genre_form_subject".equals(nodeName))
					{
						multiple("genre_form_subject", fakeSolrDocument, metadata, true);
					} else if ("md-index_term".equals(nodeName))
					{
						multiple("index_term", fakeSolrDocument, metadata, true);
					} else if ("md-collocation".equals(nodeName))
					{
						multiple("collocation", fakeSolrDocument, metadata, true);
					} 
				}
			}
			
			Collection<Object> otherAuthorPerson = fakeSolrDocument.getFieldValues(ISolrConstants.OTHER_AUTHOR_PERSON_FIELD_NAME);
			Collection<Object>otherAuthorCorporate = fakeSolrDocument.getFieldValues(ISolrConstants.OTHER_AUTHOR_CORPORATION_FIELD_NAME);
			Collection<Object>otherAuthorConference = fakeSolrDocument.getFieldValues(ISolrConstants.OTHER_AUTHOR_MEETING_FIELD_NAME);
			
			List<Object> otherAuthor = new ArrayList<Object>();
			if (otherAuthorPerson != null)
			{
				otherAuthor.addAll(otherAuthorPerson);
			}

			if (otherAuthorCorporate!= null)
			{
				otherAuthor.addAll(otherAuthorCorporate);
			}

			if (otherAuthorConference!= null)
			{
				otherAuthor.addAll(otherAuthorConference);
			}
			
			if (!otherAuthor.isEmpty())
			{
				fakeSolrDocument.addField("other_author", otherAuthor);
			}
			
			fakeSolrDocument.setField("isFederated", true);
			return fakeSolrDocument;
		} catch (Exception exception)
		{
			LoggerFactory.getLogger(Utils.class).error("Unable to convert MARC record with id " + id, exception);
			return null;
		}
	}
	
	public static SolrDocument createSolrDocumentFromSBN(final String clusteredRecord, final String unimarcDoc, final String id)
	{
		try 
		{	
			
			final Document  document = builder.parse(new ByteArrayInputStream(clusteredRecord.getBytes("UTF-8")));
			final SolrDocument fakeSolrDocument = new SolrDocument();
			fakeSolrDocument.setField(ISolrConstants.ID_FIELD_NAME, id);
			   
			final XPathExpression idExpression =  xpath.compile("/collection/record/metadata");
			final NodeList nodes = (NodeList) idExpression.evaluate(document, XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++)
			{
				final Node metadata = nodes.item(i);
				final String nodeName = metadata.getAttributes().getNamedItem("type").getNodeValue();
				if (metadata.getNodeType() == Node.ELEMENT_NODE)
				{
					if ("author".equals(nodeName) || "corporate-name".equals(nodeName) || "meeting-name".equals(nodeName))
					{
						fakeSolrDocument.setField("author", metadata.getTextContent());
					} else if ("title".equals(nodeName))
					{
						fakeSolrDocument.addField("title", metadata.getTextContent());						
					} else if ("publication-place".equals(nodeName))
					{
						multiple("publisher", fakeSolrDocument, metadata, false);
					} else if ("isbn".equals(nodeName))
					{
						multiple("isbn", fakeSolrDocument, metadata, false);
					} else if ("issn".equals(nodeName))
					{
						multiple("issn", fakeSolrDocument, metadata, false);
					}else if ("physical-extent".equals(nodeName))
					{
						multiple("physical_description", fakeSolrDocument, metadata, false);
					} else if ("other-author".equals(nodeName))
					{
						multiple("other_author_person", fakeSolrDocument, metadata, false);	
					} else if ("other-corporate-name".equals(nodeName))
					{
						multiple("other_author_corporate", fakeSolrDocument, metadata, false);	
					} else if ("other-meeting-name".equals(nodeName))
					{
						multiple("other_author_conference", fakeSolrDocument, metadata, false);	
					} else if ("subject".equals(nodeName))
					{
						multiple("subject", fakeSolrDocument, metadata, false);
					}else if ("mather".equals(nodeName))
					{
						multiple("is_part_of", fakeSolrDocument, metadata, false);
					}
				}
			}
			
			Collection<Object> otherAuthorPerson = fakeSolrDocument.getFieldValues(ISolrConstants.OTHER_AUTHOR_PERSON_FIELD_NAME);
			Collection<Object>otherAuthorCorporate = fakeSolrDocument.getFieldValues(ISolrConstants.OTHER_AUTHOR_CORPORATION_FIELD_NAME);
			Collection<Object>otherAuthorConference = fakeSolrDocument.getFieldValues(ISolrConstants.OTHER_AUTHOR_MEETING_FIELD_NAME);
			
			List<Object> otherAuthor = new ArrayList<Object>();
			if (otherAuthorPerson != null)
			{
				otherAuthor.addAll(otherAuthorPerson);
			}

			if (otherAuthorCorporate!= null)
			{
				otherAuthor.addAll(otherAuthorCorporate);
			}

			if (otherAuthorConference!= null)
			{
				otherAuthor.addAll(otherAuthorConference);
			}
			
			if (!otherAuthor.isEmpty())
			{
				fakeSolrDocument.addField("other_author", otherAuthor);
			}
			
			fakeSolrDocument.setField(ISolrConstants.MARC_21_FIELD_NAME, unimarcDoc);
			
			fakeSolrDocument.setField("isFederated", true);
			return fakeSolrDocument;
		} catch (Exception exception)
		{
			LoggerFactory.getLogger(Utils.class).error("Unable to convert UNIMARC record with id " + id, exception);
			return null;
		}
	}
	
	
	/**
	 * Creates and adds a {@link SolrDocument} to the given list starting from a "clustered" result.
	 * 
	 * @param documents the holder collection.
	 * @param clusteredRecord the clustered record (as string).
	 * @param recid the clustered record id.
	 * @param id the local record id.
	 */
	public static void convertToSolrDocuments(
			final List<SolrDocument> documents, 
			final String clusteredRecord, 
			final String recid, 
			final String id) 
	{
		final SolrDocument document = createSolrDocument(clusteredRecord, recid, id);
		if (document != null)
		{			
			documents.add(document);
		}
	}
	
	public static void convertSBNToSolrDocuments(
			final List<SolrDocument> documents, 
			final String clusteredRecord, 
			final String unimarcDoc,
			final String id) 
	{
		final SolrDocument document = createSolrDocumentFromSBN(clusteredRecord, unimarcDoc, id);
		if (document != null)
		{			
			documents.add(document);
		}
	}
	
	private static void multiple(final String ogName, final SolrDocument fakeSolrDocument, final Node metadata, final boolean isSubject)
	{
		List<String> pd = null;
		if (fakeSolrDocument.get(ogName) == null)
		{
			pd = new ArrayList<String>();
			pd.add(metadata.getTextContent());
			fakeSolrDocument.setField(ogName, pd);
		} else
		{
			fakeSolrDocument.addField(ogName, metadata.getTextContent());				
		}			
		
		if (isSubject)
		{
			List<String> s = null;
			if (fakeSolrDocument.get("subject") == null)
			{
				s = new ArrayList<String>();
				s.add(metadata.getTextContent());
				fakeSolrDocument.setField("subject", s);
			} else
			{
				fakeSolrDocument.addField("subject", metadata.getTextContent());				
			}					
		}
	}
	
	public static boolean isLoanServiceClose(ConfigurationTool configuration) {
		try {
			final String loanCloseDate = configuration.getLoanServiceClose();
			if (loanCloseDate == null) {
				return false;
			}
			else {			
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");				
				Date today = formatter.parse(formatter.format(new Date()));
				String [] dates = loanCloseDate.split(";");
				int dateCount = dates.length;
				switch (dateCount) {				
						
					case 2: {						
						Date startingDate = new SimpleDateFormat("dd/MM/yyyy").parse(dates[0]);
						Date endingDate = new SimpleDateFormat("dd/MM/yyyy").parse(dates[1]);
						return (startingDate.compareTo(today) == 0 ||
								(today.after(startingDate) && today.before(endingDate)));						
					}
					default :
						for (int i = 0; i < dateCount; i ++) {
							Date closeDate = new SimpleDateFormat("dd/MM/yyyy").parse(dates[i]);
							if (closeDate.compareTo(today) == 0) {
								return true;
							}
						}
						return false;					
				}					
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
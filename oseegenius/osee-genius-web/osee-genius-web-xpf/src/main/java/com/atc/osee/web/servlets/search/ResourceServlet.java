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
package com.atc.osee.web.servlets.search;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.velocity.tools.Toolbox;
import org.jfree.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.XsltUtility;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.model.community.Review;
import com.atc.osee.web.model.community.Tag;
import com.atc.osee.web.plugin.CommunityPlugin;
import com.atc.osee.web.servlets.RememberLastVisitedResourceServlet;
import com.atc.osee.web.servlets.writers.AbstractOutputWriter;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.LicenseTool;

/**
 * Resource loader servlet.
 * Loads details for a requested resource.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ResourceServlet extends RememberLastVisitedResourceServlet 
{
	private static final long serialVersionUID = 1L;
	private static final char [] COPY_STATEMENT_SUBFIELDS = {'b','c','e','g','h','j','i','k','l','p','s','t'};
	
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "home");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "suggest");
		setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "search");
		
		String uri = request.getParameter(HttpParameter.URI);
		
		if (isNotNullOrEmptyString(uri))
		{
			uri = uri.replaceFirst("^0+(?!$)", "");
			try
			{
				ISearchEngine searchEngine = getSearchEngine(request);
				QueryResponse resourceResponse = searchEngine.findDocumentByURI(uri);
				
				if (resourceResponse != null && !resourceResponse.getResults().isEmpty())
				{
					AbstractOutputWriter writer = writers.get(request.getAttribute(HttpParameter.WRITE_MODE));
					
					//bug 5402
					nextPrevious(request, uri, searchEngine);
					
					if (writer == null)
					{
						
						html(request, response, resourceResponse, uri, searchEngine);
					} else 
					{
						writer.write(request, response, resourceResponse.getResults());
					}
				} else 
				{
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			} catch (SystemInternalFailureException exception)
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
			}
		}
		else {
			String auth_group_uri = request.getParameter(HttpParameter.AUTHORITY_GROUP_URI);
			if(isNotNullOrEmptyString(auth_group_uri)) {
				try
				{
					ISearchEngine searchEngine = getSearchEngine(request);
					QueryResponse resourceResponse = searchEngine.findAuthDocumentByAuthLink(auth_group_uri);
					if (resourceResponse != null && !resourceResponse.getResults().isEmpty())
					{
						AbstractOutputWriter writer = writers.get(request.getAttribute(HttpParameter.WRITE_MODE));
						if (writer == null)
						{
							
							html(request, response, resourceResponse, auth_group_uri, searchEngine);
						} else 
						{
							writer.write(request, response, resourceResponse.getResults());
						}
					} else 
					{
						response.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
				} catch (SystemInternalFailureException exception)
				{
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				}
					
			}
		}
	}
	
	/**
	 * Retrieves resource details and forward to the appropriate view component. 
	 * 
	 * @param request the http request.
	 * @param response the http response.
	 * @param resourceResponse the incoming response from the search engine.
	 * @param uri the uri of the requested resource.
	 * @param searchEngine the search engine reference.
	 * @throws IOException in case of I/O failures.
	 * @throws SystemInternalFailureException in case of system failure.
	 * @throws ServletException in case of general servlet container exception.
	 */
	private void html(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final QueryResponse resourceResponse, 
			final String uri, 
			final ISearchEngine searchEngine) throws IOException, SystemInternalFailureException, ServletException 
	{
		SolrDocument resource = resourceResponse.getResults().get(0);
		
		if (resource.getFieldValue("title") != null) {
			String specificHtmlResTitle = resource.getFieldValue("title").toString();
			request.setAttribute("specificHtmlResTitle", specificHtmlResTitle);
		}
		
		setRequestAttribute(
				request, 
				IConstants.RESOURCE_KEY, 
				resource);

		LicenseTool license = getLicense(request);
		ConfigurationTool configuration = getConfiguration(request);
		if(configuration.schemaToCreate()) {
			JSONObject schema= builtSchema(resource);
			request.setAttribute("schema", schema);
		}
		
		if("branch".equals(resource.getFieldValue(ISolrConstants.CATEGORY_CODE))){
			request.setAttribute("uri", uri);
			allLocations(request, searchEngine,resource.getFieldValue("association_name").toString());
			allHelpLines(request, searchEngine,resource.getFieldValue("association_id").toString());
		}
		else{
			
			moreLikeThis(license, request, searchEngine, uri, (String) resource.getFirstValue("authors_exact_match"));
			
			if (getConfiguration(request).getHoldingSbnEnable()) {
				holdingsSBN(license, request, resource, resourceResponse);
				subscriptionsSBN(license, request, resource);
			}
			else {
				holdings(license, request, resource, resourceResponse, uri);
			}
			
			community(license, request, resource, resourceResponse, uri);
		}				
		 
		forwardTo(request, response, "/hit.vm", "one_column.vm");				
	}

private JSONObject builtSchema(SolrDocument resource) {
	JSONObject json = new JSONObject();
	json.put("@context", "https://schema.org/");
	json.put("@type", "Book");
	
	if (resource.getFieldValue("title") != null) {
		String title = (String) resource.getFieldValue("title");
		json.put("name", title);
	}
	
	
	
	if (resource.getFieldValue("isbn") != null) {
		ArrayList<String> list_isbn=new ArrayList<String>();
		list_isbn=(ArrayList<String>) resource.getFieldValue("isbn");
		
		json.put("isbn", list_isbn.get(0));
		
	}
	
	if (resource.getFieldValue("summary_note") != null) {
		ArrayList<String> list_abstract=new ArrayList<String>();
		list_abstract=(ArrayList<String>)resource.getFieldValue("summary_note");
		json.put("abstract", list_abstract.get(0));
	}
	
	
	if (resource.getFieldValue("author_person") != null) {
		String author = (String) resource.getFieldValue("author_person");
		JSONObject jsonAuthor = new JSONObject();
		jsonAuthor.put("@type", "Person");
		jsonAuthor.put("name", author);
		json.put("author", jsonAuthor);
	}
	
	
	if (resource.getFieldValue("other_author_person") != null) {
		ArrayList<String> other_author=new ArrayList<String>();
		other_author= (ArrayList<String>) resource.getFieldValue("other_author_person");
		JSONArray other_authorArray = new JSONArray();
		for (String author :other_author) {
			JSONObject jsonOtherAuthor = new JSONObject();
			jsonOtherAuthor.put("@type", "Person");
			jsonOtherAuthor.put("name", author);
			other_authorArray.put(jsonOtherAuthor);
			
		}
		json.put("contributor", other_authorArray);
		
	}
	
	
	if (resource.getFieldValue("language") != null) {
		ArrayList<String> lan= new ArrayList<String>();
		lan=(ArrayList<String>) resource.getFieldValue("language");
		json.put("inLanguage", lan.get(0));
	}
	
	if (resource.getFieldValue("edition") != null) {
		ArrayList<String> edition= new ArrayList<String>();
		edition=(ArrayList<String>) resource.getFieldValue("edition");
		json.put("bookEdition", edition.get(0));	
	}

	if (resource.getFieldValue("numberOfPages") != null) {
		//esempio --> numberOfPage : 150 p.
		String n = (String) resource.getFieldValue("numberOfPages");
		try {
			String [] listData=n.split(" ");
			if (listData.length>1)
				if(listData[1].equals("p.")) {
					Integer number= Integer.valueOf(n.split(" ")[0]);
					json.put("numberOfPages", number);
				}
		}catch(Exception e) {
			
		}
		
		
	}
	
	
	if (resource.getFieldValue("about") != null) {
		ArrayList<String> about= new ArrayList<String>();
		about=(ArrayList<String>) resource.getFieldValue("about");
		JSONArray jsonAboutArray = new JSONArray();
		for(String ab: about) {
			JSONObject jsonAbout = new JSONObject();
			jsonAbout.put("@type", "Thing");
			jsonAbout.put("description", ab);
			jsonAboutArray.put(jsonAbout);
			
		}
		json.put("about", jsonAboutArray);
	}
	
	if (resource.getFieldValue("dateCreated") != null) {
		String temp=  (String) resource.getFieldValue("dateCreated");
		String dataCreated = temp.split("d")[0];
		try {
			json.put("dateCreated", stringToDate(dataCreated,"yyyyMMdd"));
		}catch (Exception e) {
			
		}
		
	}
	
	
	if (resource.getFieldValue("publication_date") != null) {
		ArrayList<String> pubDate= new ArrayList<String>();
		pubDate=(ArrayList<String>) resource.getFieldValue("publication_date");
		SimpleDateFormat date =  new SimpleDateFormat("yyyy");
		Date d=null;
		try {
			d=date.parse(pubDate.get(0));
			json.put("datePublished",d);
		} catch (ParseException e) {
		
		}
			
				
	}
	
	
	if (resource.getFieldValue("isPartOf") != null) {
		ArrayList<String> isPartOf= new ArrayList<String>();
		isPartOf=(ArrayList<String>) resource.getFieldValue("isPartOf");
		JSONArray jsonIsPartOfArray = new JSONArray();
		for(String el : isPartOf) {
			JSONObject jsonIsPartOf = new JSONObject();
			jsonIsPartOf.put("@type", "CreativeWork");
			jsonIsPartOf.put("description", el);
			jsonIsPartOfArray.put(jsonIsPartOf);
		}
		json.put("isPartOf", jsonIsPartOfArray);
	}
	
	if (resource.getFieldValue("hasPart") != null) {
		ArrayList<String> hasPart= new ArrayList<String>();
		hasPart=(ArrayList<String>) resource.getFieldValue("hasPart");
		JSONArray jsonHasPartArray = new JSONArray();
		for(String el : hasPart) {
			JSONObject jsonHasPart = new JSONObject();
			jsonHasPart.put("@type", "CreativeWork");
			jsonHasPart.put("description", el);
			jsonHasPartArray.put( jsonHasPart);
			}
		json.put("hasPart", jsonHasPartArray);
		}
	
	if (resource.getFieldValue("publisher_name") != null) {
		ArrayList<String> publisher_name= new ArrayList<String>();
		publisher_name=(ArrayList<String>) resource.getFieldValue("publisher_name");
		JSONArray jsonPublisherArray = new JSONArray();
		for (String el:publisher_name) {
			JSONObject jsonPublisher = new JSONObject();
			jsonPublisher.put("@type", "Organization");
			jsonPublisher.put("name", el);
			jsonPublisherArray.put(jsonPublisher);
		}
		json.put("publisher", jsonPublisherArray);

	}
	

	return json;
}
	
private Date stringToDate(String date, String format) {
	SimpleDateFormat dateParser=new SimpleDateFormat(format);
	Date d=null;
	try {
		d=dateParser.parse(date);
		
	} catch (ParseException e) {
		
	}
	
	return d;
	
}



	
/*
 * Method for browsing records of a search from detail page	
 */
	
protected void nextPrevious(HttpServletRequest request, String uri, ISearchEngine searchEngine){	
		
		String previous = null;
		String next = null;		
		boolean finded = false;		
		SolrDocumentList list = null; 	
		String last = null;
		String first = null;
		boolean firstCicle = true;
		
		try {
			//direction will tell me if user click on "next" or "previous" to immediatly know if I need to search in the 
			//next or previous page
			String direction = "";
			if(request.getParameter(HttpParameter.DIRECTION) != null) {
				direction = request.getParameter(HttpParameter.DIRECTION);
			}
			String nextDirection = "1";
			String previousDirection = "0";		
		
			//list = visit.getSearchExperience().getCurrentTab().getResponse().getResults();				
			//SearchTab currentTab = visit.getSearchExperience().getCurrentTab();	
			int tabId = getTabId(request);
			SearchTab currentTab = getSearchExperience(request).getTab(tabId);
			list = currentTab.getResponse().getResults();
			
			//if there is an active search
			if(list != null){
				last = list.get(list.size() - 1).get("id").toString();			
				Iterator<SolrDocument> iter = list.iterator();
				//first time iteration
				while(iter.hasNext()){
					SolrDocument current = iter.next();				
					String id = current.get("id").toString();
					if(firstCicle){
						first = id;
						firstCicle = false;
					}				
					if(id.equals(uri)){	
						finded = true;
						if(iter.hasNext()){
							SolrDocument nextDoc = iter.next();
							next = nextDoc.get("id").toString();
						}
						break;
					}
					else {
						previous = id;
					}		
				}
		
				if(!finded) {
					previous = null;
				}
			
				//se almeno uno degli estremi della lista è nullo ho 3 possibilità:
				// 1) E' il primo elemento. 2) E' l'ultimo. 3) Non è nella lista
				if(next == null || previous == null){						
					SolrQuery solrQuery = currentTab.getQuery();
					String parameters = currentTab.getQueryParameters();
					int rows = solrQuery.getRows();
					int start = solrQuery.getStart();				
					SolrQuery newQuery = solrQuery.getCopy();
					int page = (Integer.parseInt(parameters.substring((parameters.length())-1, parameters.length())));
					QueryResponse queryResponse = currentTab.getResponse();	
				
					//caso next 
					if(next == null && (finded || direction.equals(nextDirection))){				
						int newStart = start + rows;
						newQuery.setStart(newStart);					
						int newPage = page + 1;
						//String newParameters = parameters.substring(0, parameters.length()-1) + newPage;						
						queryResponse = searchEngine.executeQuery(newQuery);
						list = queryResponse.getResults();
						
						//if I finded it, I only need the first element of the page to build the link to next
						if(finded){						
							if(list != null && !list.isEmpty()){
								Iterator<SolrDocument> nextIter = list.iterator();
								next = nextIter.next().get("id").toString();
							}
						}
						//else, I need so save in the currentTab the new list
						else {							
							String tabName = currentTab.getTitle();							
							//new iteration for next results
							if(list != null){
								Iterator<SolrDocument> iterNext = list.iterator();								
								while(iterNext.hasNext()){
									SolrDocument current = iterNext.next();
									String id = current.get("id").toString();									
									if(id.equals(uri)){	
										finded = true;
										currentTab.setData(tabName, newQuery, queryResponse, newPage);							
										if(iterNext.hasNext()){
											SolrDocument nextDoc = iterNext.next();
											next = nextDoc.get("id").toString();
											previous = last; //l'ultimo della lista precedente
										}
										break;
									}
									else {
										previous = id;
									}		
								}							
								if(!finded) {
									previous = null;
								}
							}							
						}					
					}
					//caso previous and not firstPage
					if(previous == null && (finded || direction.equals(previousDirection)) && start != 0){					
						int newStart = start - rows;
						newQuery.setStart(newStart);
						int newPage = page - 1;
						//String newParameters = parameters.substring(0, parameters.length()-1) + newPage;
						
						queryResponse = searchEngine.executeQuery(newQuery);
						list = queryResponse.getResults();
							
						//if I finded it, I only need the last element of the page to build the link to previous
						if(finded){							
							if(list != null){								
								previous = list.get(list.size() - 1).get("id").toString();
							}
						}
						//else, I need so save in the currentTab the new list
						else {
							String tabName = currentTab.getTitle();
							currentTab.setData(tabName, newQuery, queryResponse, newPage);
								
							//new iteration for next results
							if(list != null){
								Iterator<SolrDocument> iterNext = list.iterator();								
								while(iterNext.hasNext()){
									SolrDocument current = iterNext.next();
									String id = current.get("id").toString();				
									if(id.equals(uri)){	
										finded = true;																			
										next = first;											
										break;
									}
									else {
										previous = id;
									}		
								}								
								if(!finded) {
									previous = null;
								}
							}
						}						
					}				
				}
			}
		}
		catch(Exception e){
			//ignore
		}
			request.setAttribute("previous", previous);
			request.setAttribute("next", next);						
		
			return;
		}
	
	protected int getTabId(final HttpServletRequest request)
	{
		String tabIdParameter = request.getParameter(HttpParameter.TAB_ID);
		int tabId = 0;
		
		// If needed because most of time this parameter is null so 
		// it's better to avoid try / catch block
		if (tabIdParameter != null)
		{
			try 
			{ 
				tabId = Integer.parseInt(tabIdParameter); 
			} catch (Exception exception) 
			{
				// Nothing
			}
		}
		return tabId;
	}

	
	
	
	
	
	
	/**
	 * Loads all locations associated with an association.
	 * 
	 * @param request the http request.
	 * @param searchEngine the search engine.
	 * @param associationName the association name.
	 * @throws SystemInternalFailureException in case of system internal failure.
	 */
	private void allLocations(
			final HttpServletRequest request, 
			final ISearchEngine searchEngine, 
			final String associationName) throws SystemInternalFailureException
	{
		
			setRequestAttribute(
					request,
					"locations", 
					searchEngine.findLocationsByAssocName(associationName));				
		
	}
	
	/**
	 * Loads all helplines associated with an association.
	 * 
	 * @param request the http request.
	 * @param searchEngine the search engine.
	 * @param associationId the association id.
	 * @throws SystemInternalFailureException in case of system internal failure.
	 */
	private void allHelpLines(
			final HttpServletRequest request, 
			final ISearchEngine searchEngine, 
			final String associationId) throws SystemInternalFailureException
	{
		
			setRequestAttribute(
					request,
					"helplines", 
					searchEngine.findHelpLinesByAssocId(associationId));				
		
	}
	
	/**
	 * Loads the more like this data associated with the requested resource.
	 * 
	 * @param license the license tool.
	 * @param request the http request.
	 * @param searchEngine the search engine.
	 * @param uri the resource URI.
	 * @throws SystemInternalFailureException in case of system internal failure.
	 */
	private void moreLikeThis(
			final LicenseTool license, 
			final HttpServletRequest request, 
			final ISearchEngine searchEngine, 
			final String uri,
			final String groupingAndFilteringCriteria) throws SystemInternalFailureException
	{
		if (license.isMoreLikeThisViewerEnabled())
		{
			String pageNumber = request.getParameter(HttpParameter.PAGE_INDEX);
			Toolbox toolbox = (org.apache.velocity.tools.Toolbox) getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
			String instituition = (String) toolbox.get("institution");			
			setRequestAttribute(
					request, 
					IConstants.MORE_LIKE_THIS_RESULT_KEY, 
					searchEngine.findDocumentByURIWithMLT(uri, pageNumber, instituition, groupingAndFilteringCriteria));				
		}
	}
	
	/**
	 * Loads the holdings data associated with the requested resource.
	 * Note: in order to get this kind of data you need a OseeGenius additional plugin.
	 * 
	 * @param license the license tool.
	 * @param request the http request.
	 * @param resource the requested resource.
	 * @param resourceResponse the whole incoming search engine response.
	 * @param uri the resource URI.
	 */
	@SuppressWarnings({ "rawtypes"})
	private void holdings(
			final LicenseTool license, 
			final HttpServletRequest request, 
			final SolrDocument resource, 
			final QueryResponse resourceResponse, 
			final String uri)
	{
		String categoryCode = (String) resource.get(ISolrConstants.CATEGORY_CODE);
		
		if (isBiblio(categoryCode) && license.isHoldingDataViewerEnabled())
		{  
			NamedList holdingData = ((NamedList)(NamedList) resourceResponse.getResponse().get(ISolrConstants.HOLDINGS_COMPONENT_NAME));
			if (holdingData != null)
			{
				Object val = holdingData.get(uri);
				if (val instanceof String && "ERROR".equals(val))
				{
					setRequestAttribute(request, "holdingDataNotAvailable", true);
				} else 
				{
					setRequestAttribute(request, "holdingDataNotAvailable", false);
					NamedList copies = (NamedList) val;
					if (copies != null && copies.size() != 0)
					{
						addCopyAdditionalData(resource, copies);
						
						setRequestAttribute(
								request,
								IConstants.HOLDING_DATA_ATTRIBUTE_NAME,
								copies);
					}
				}	
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	private void subscriptionsSBN( final LicenseTool license, final HttpServletRequest request, 
			final SolrDocument resource){
		
		String categoryCode = (String) resource.get(ISolrConstants.CATEGORY_CODE);
		if (isBiblio(categoryCode) && license.isHoldingDataViewerEnabled()){
			try {
				Record record = getMarcRecord((String) resource.get(ISolrConstants.MARC_XML_FIELD_NAME));
				NamedList<Object> subscriptionData = new NamedList<Object>();
				final List<VariableField> fields = (List<VariableField>) record.getVariableFields("961");
				int counter = 1;
				for(VariableField f : fields) {
					DataField field = (DataField) f;
					NamedList<Object> subscription = new SimpleOrderedMap<Object>();
					String sub_b = field.getSubfield('b').toString();
					if (sub_b != null && sub_b.length()>0) {
						subscription.add("institution-id", sub_b.substring(2, 4) );
						subscription.add("code-subscription", sub_b.substring(4, 14) );
						subscriptionData.add(String.valueOf(counter), subscription);
					}
				}
				if (subscriptionData != null  && subscriptionData.size() != 0){
					setRequestAttribute( request, IConstants. SUBSCRPTIONS_DATA_ATTRIBUTE_NAME, subscriptionData);
					setRequestAttribute(request, "holdingDataNotAvailable", true);
				}
			} catch (Exception e) {
				//Do nothing
			}
		}	
	}
	
	private String getHoldingStatement (final String sub_b, final String sub_c) {
		if(sub_b != null) {
			return sub_b.replaceAll("\\s{2,}", " ");
		}
		if (sub_c != null) {
			return sub_c.replaceAll("\\s{2,}", " ");
		}
		return null;
	}
	
	private String getYear (final String sub_e) {
		String result = null;
		if(sub_e.length()>=56) { 
			result = sub_e.substring(56);
			result = result.trim();
			result = result.replaceAll("\\s{2,}", " ");
			if (result.length()>0) { 
				return result;
			}
		}
		return result;
	}
	
	private void periodicCopies(final HttpServletRequest request, List<VariableField> fields_960 ) {	
		Map<String, Map> institutionMap = null;
		for(VariableField f : fields_960) {
			try {
				if (institutionMap == null) {
					institutionMap = new HashMap<String, Map>();
				}
				DataField field = (DataField) f;
				String sub_b = toStrigSub(field.getSubfield('b'), "trim", true);
				String sub_c = toStrigSub(field.getSubfield('c'), "trim", true);
				String sub_d = toStrigSub(field.getSubfield('d'), "notrim", true);
				String sub_e = field.getSubfield('e').toString();
				//String sub_s = toStrigSub(field.getSubfield('s'), "trim", true);
				String sub_m = toStrigSub(field.getSubfield('m'), "trim", true);
				String sub_u = toStrigSub(field.getSubfield('u'), "trim", true);
				String sub_z = toStrigSub(field.getSubfield('z'), "trim", true);			
							
				String institutionId = sub_d.substring(1, 3);
				String year = getYear(sub_e);
				String collocation = createCallNumberSBNWEB(sub_d, sub_e);			
				String holdingStatement = getHoldingStatement(sub_b, sub_c);
				String codeAvailable = sub_e.substring(48, 50).trim();	
				String codenotavailable = sub_e.substring(46, 48).trim();
				
				String barcode = calculateBarcodeSBNWBPoloBNCF(sub_e);
								
				Map<String, String> copy = new HashMap<String, String>();
				copy.put("institution_id", institutionId);
				copy.put("year", year);
				copy.put("collocation", collocation);
				copy.put("holding-statement", holdingStatement);
				copy.put("code-available", codeAvailable);
				copy.put("code-notavailable", codenotavailable);
				copy.put("barcode", barcode);
				
				if ("D".equals(codeAvailable) ||
					"E".equals(codeAvailable) ||
					"G".equals(codeAvailable) ||
					"M".equals(codeAvailable)) {
				}
				else {
					Log.error("Trovato periodico con diversi codici fruibilit. - barcode: " + copy.get("barcode"));
				}
				if (!institutionMap.containsKey(institutionId)) {
					institutionMap.put(institutionId, new HashMap<String, Map>());
				}
				Map<String, Map> collocationMap = institutionMap.get(institutionId);
				if (!collocationMap.containsKey(collocation)) {
					collocationMap.put(collocation, new HashMap<String, List<Map>>());
				}
				Map<String, List> yearMap = collocationMap.get(collocation);
				if (!yearMap.containsKey(holdingStatement)) {
					yearMap.put(holdingStatement, new ArrayList<Map>());
				}
				List<Map> copies = yearMap.get(holdingStatement);
				copies.add(copy);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setRequestAttribute(request, "copies", institutionMap);	
	}
	
	/**
	 * Check if record is periodic
	 * @param record
	 * @return true if record is a periodic
	 */
	private boolean isPeriodic(Record record) {
		String leader = record.getLeader().toString();
		final char leaderChar07 = leader.charAt(7);
		if (leaderChar07 == 's') {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings({ "unchecked" })
	private void holdingsSBN( final LicenseTool license, final HttpServletRequest request, 
			final SolrDocument resource, final QueryResponse resourceResponse )
	{
		String categoryCode = (String) resource.get(ISolrConstants.CATEGORY_CODE);
		if (isBiblio(categoryCode) && license.isHoldingDataViewerEnabled()){
			try {
				Record record = getMarcRecord((String) resource.get(ISolrConstants.MARC_XML_FIELD_NAME));
				
				NamedList<Object> holdingData = new NamedList<Object>();
	    		String ldr = record.getLeader().toString();
				final List<VariableField> fields_960 = (List<VariableField>) record.getVariableFields("960");
								
				
				if (isPeriodic(record)) {
					periodicCopies(request, fields_960);
					setRequestAttribute(request, "isPeriodic", true);
				}
				else {
					    		
					for(VariableField f : fields_960) {
						DataField field = (DataField) f;
		    			NamedList<Object> copy = new SimpleOrderedMap<Object>();
						String sub_b = toStrigSub(field.getSubfield('b'), "trim", true);
						String sub_c = toStrigSub(field.getSubfield('c'), "trim", true);
						String sub_d = toStrigSub(field.getSubfield('d'), "notrim", true);
						String sub_e = field.getSubfield('e').toString();
						String sub_s = toStrigSub(field.getSubfield('s'), "trim", true);
						String sub_m = toStrigSub(field.getSubfield('m'), "trim", true);
						String sub_u = toStrigSub(field.getSubfield('u'), "trim", true);
						String sub_z = toStrigSub(field.getSubfield('z'), "trim", true);
						
						if(ldr.substring(7, 8).equals("s")) {
							String hstatement = null;
							if (sub_b!=null) { hstatement = sub_b;}else{hstatement=sub_c;}
							if (hstatement != null) { 
								hstatement = hstatement.replaceAll("\\s{2,}", " "); 
								copy.add("holding-statement", hstatement );
							}
						}
						
						if(sub_e.length()>15) {
							copy.add("code-available", sub_e.substring(48, 50).trim());
							
							copy.add("code-notavailable", sub_e.substring(46, 48).trim());
						}
						
						if(sub_e.length()>=56) { 
							String volume = sub_e.substring(56);
							volume = volume.trim();
							volume = volume.replaceAll("\\s{2,}", " ");
							if (volume.length()>0) { copy.add("volume", volume );}
						}
										
						String shelflist = createCallNumberSBNWEB(sub_d, sub_e);
						String barcode = calculateBarcodeSBNWBPoloBNCF(sub_e);
						
						copy.add("barcode", barcode);
						if (sub_d != null ) { copy.add("institution-id", sub_d.substring(1, 3)); }
						if (shelflist != null) { copy.add("shelf-list", shelflist); }
						if (sub_m != null) { copy.add("note-inventory", sub_m); }
						//if (sub_s != null) { copy.add("note-fortebelvedere", sub_s); }
						if (sub_u != null) { copy.add("digital-copy-link", sub_u); }
						if (sub_z != null) { copy.add("digital-copy-available", sub_z); }
						
						
						holdingData.add(String.valueOf(barcode), copy);
					}
					
					if (holdingData != null  && holdingData.size() != 0){
						setRequestAttribute(request, "holdingDataNotAvailable", false);
						setRequestAttribute( request, IConstants.HOLDING_DATA_ATTRIBUTE_NAME, holdingData);
					}else {
						setRequestAttribute(request, "holdingDataNotAvailable", true);
					}
				}
			} catch (Exception e) {
				setRequestAttribute(request, "holdingDataNotAvailable", true);
			}
		}
		
	}
	
	private static String toStrigSub(Subfield subfield, String chkTrim, boolean cutSub) {
		if (subfield != null) {
			String data = subfield.toString();
			if (cutSub) {
				data = data.substring(2);
			}
			if (chkTrim.equals("trim")) {
				data = data.trim();
			}
			return data;
		}else {
			return null;
		}
	}
	
	private static String createCallNumberSBNWEB(final String sub_d, String sub_e) {
		String result;
		if (sub_d != null) {
			result= sub_d.substring(3);
			result = result + " " + sub_e.substring(16, 46);
			result = result.trim();
			result = result.replaceAll("\\s{2,}", " ");
			return result;
		}else {
			return null;
		}
	}
	
	private static String calculateBarcodeSBNWBPoloBNCF(String subf) {
		if (subf != null) {
			String string = subf.substring(2);
			string = string.substring(0,14);		
			if (string.substring(0,2).equals("CF"))  {
				string = string.substring(2);
				string = string.trim();
				return string;
			}else {
				return string;
			}	
		}else {
			return null;
		}
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addCopyAdditionalData(final SolrDocument resource, final NamedList copies) 
	{
		Record record = getMarcRecord((String) resource.get(ISolrConstants.MARC_XML_FIELD_NAME));
		if (record != null)
		{
			List<VariableField> fields = (List<VariableField>) record.getVariableFields("852");
			for (VariableField f : fields)
			{
				DataField field = (DataField)f;
				Subfield subfield = field.getSubfield('B');
				if (subfield != null)
				{
					String copyId = subfield.getData();
					if (isNotNullOrEmptyString(copyId))
					{
						NamedList copyData = (NamedList) copies.get(copyId);
						
						if (copyData != null)
						{
							addCopyStatements(copyData, field);
							
							Subfield inventorySubfield = field.getSubfield('D');
							if (inventorySubfield == null || isNullOrEmptyString(inventorySubfield.getData()))
							{
								inventorySubfield = field.getSubfield('H');
							}
						
							if (inventorySubfield != null)
							{
								String inventory = inventorySubfield.getData();
								if (isNotNullOrEmptyString(inventory))
								{
									copyData.add("inventory", inventory);
								}
							}
						
							Subfield copyNumberSubfield = field.getSubfield('n');
							if (copyNumberSubfield != null)
							{
								String copyNumber = copyNumberSubfield.getData();
								if (isNotNullOrEmptyString(copyNumber))
								{
									copyData.add("copy-number", copyNumber);
								}
							}										
							
							List<Subfield> remarkNoteSubfields = field.getSubfields('q');
							if (remarkNoteSubfields != null && !remarkNoteSubfields.isEmpty())
							{
								List<String> remarkNotes = new ArrayList<String>(remarkNoteSubfields.size());
								for (Subfield remarkNoteSubfield : remarkNoteSubfields)
								{
									String remarkNote = remarkNoteSubfield.getData();
									if (isNotNullOrEmptyString(remarkNote))
									{
										remarkNotes.add(remarkNote);
									}
								}
								if (!remarkNotes.isEmpty())
								{
									copyData.add("remark-notes", remarkNotes);
								}
							}		
						}
					}
				}
			}
		}
	}

	// #BZ 1803
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addCopyStatements(final NamedList copyData, final DataField field)
	{
		List<Subfield> posseduto = field.getSubfields('a');
		if (posseduto != null && posseduto.size() > 1)
		{
			Subfield secondA= posseduto.get(1);
			if (secondA != null)
			{
				String data = secondA.getData();
				if (data != null)
				{
					copyData.add("posseduto", data);					
				}
			}
		}
		
		for (char code : COPY_STATEMENT_SUBFIELDS)
		{
			Subfield copyStatementSubfield = field.getSubfield(code);
			if (copyStatementSubfield != null)
			{
				String copyStatement = copyStatementSubfield.getData();
				if (isNotNullOrEmptyString(copyStatement))
				{
					List<String> copyStatements = (List<String>) copyData.get(ISolrConstants.COPY_STATEMENT);
					if (copyStatements == null)
					{
						copyStatements = new ArrayList(5);
						copyData.add(ISolrConstants.COPY_STATEMENT, copyStatements);
					}
					copyStatements.add(copyStatement);
				}
			}
		}
	}
	
	/**
	 * Loads community stuff associated with a given document.
	 * 
	 * @param license the license associated with this OseeGenius -W- instance.
	 * @param request the http request.
	 * @param resource the document.
	 * @param resourceResponse the search engine response (containing document and search metadata).
	 * @param uri the URI of the document.
	 */
	private void community(
			final LicenseTool license, 
			final HttpServletRequest request, 
			final SolrDocument resource, 
			final QueryResponse resourceResponse, 
			final String uri)
	{
		if (license.isCommunityPluginEnabled() && getVisit(request).isAuthenticated())
		{
			try 
			{
				CommunityPlugin plugin = license.getCommunityPlugin();
				Account account = getVisit(request).getAccount();
				int accountId = account.getId();
				boolean isCommunityDataPrivate = getConfiguration(request).isCommunityDataPrivate();
				
				List<Review> reviews = isCommunityDataPrivate
						? plugin.getUserDocumentReviews(uri, true, -1, accountId) 
						: plugin.getDocumentReviews(uri, true, -1);
				if (!reviews.isEmpty()) 
				{
					setRequestAttribute(request, "reviews", reviews);
				}
				 
				List<Tag> tags = isCommunityDataPrivate 
						? plugin.getUserDocumentTags(uri, accountId) 
						: plugin.getDocumentTags(uri);
				if (!tags.isEmpty()) 
				{ 
					setRequestAttribute(request, "tags", tags);
				}
				
				setRequestAttribute(request, "bibliography", plugin.isInBibliography(uri, accountId));
				
				Map<Long, String> itemWishLists = plugin.getDocumentWishLists(uri, accountId);
				Map<Long, String> userWishLists = plugin.getWishLists(accountId);

				setRequestAttribute(request, "itemWishLists", itemWishLists);

				for (Long wishListId : itemWishLists.keySet())
				{
					userWishLists.remove(wishListId);
				}
				
				if (!userWishLists.isEmpty())
				{
					setRequestAttribute(request, "userWishLists", userWishLists);
				}
			} catch (SystemInternalFailureException exception)
			{
				// Nothing to be done here...
			}
		}
	}
	
	/**
	 * Returns true if the given resource belong to the bibliographic catalogue.
	 * 
	 * @param categoryCode the category code of the requested item.
	 * @return true if the given resource belong to the bibliographic catalogue.
	 */
	private boolean isBiblio(final String categoryCode)
	{
		return ISolrConstants.BIBLIOGRAPHIC_CATALOGUE.equals(categoryCode);
	}
	
	/**
	 * Returns the MARC record object representation of the given Marc XML.
	 * 
	 * @param marcxml the raw marc xml data.
	 * @return the MARC record object representation.
	 */
	private Record getMarcRecord(final String marcxml)
	{
		MarcReader reader = new MarcXmlReader(new InputSource(new StringReader(marcxml)));
		if (reader.hasNext())
		{
			return reader.next();
		}	
		return null;
	}
}
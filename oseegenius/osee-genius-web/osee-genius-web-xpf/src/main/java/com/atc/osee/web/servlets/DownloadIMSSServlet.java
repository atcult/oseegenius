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
package com.atc.osee.web.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;


import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.XsltUtility;
import com.atc.osee.web.model.FederatedItemSelection;
import com.atc.osee.web.model.ItemSelection;
import com.atc.osee.web.model.SBNFederatedItemSelection;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.sbn.Document;
import com.atc.osee.web.servlets.writers.HtmlOutputWriter;
import com.atc.osee.web.util.Utils;

/**
 * OseeGenius -W- download web controller.
 * 
 * @author aguercios
 *
 */
public class DownloadIMSSServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = -36244155654606512L;
			
	private final Map<String, HttpServlet> handlers = new HashMap<String, HttpServlet>();
	{
		handlers.put("html", new HttpServlet() 
		{
			private static final long serialVersionUID = 3976563096945578156L;
			
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
			{
				ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT))
						? getSearchExperience(request).getCurrentTab()
						: getWorkspaceSelection(request);				
				
				if (isNotNullOrEmptyString(request.getParameter(HttpParameter.FOLDER_SELECTION_CONTEXT)))
				{
					selection = getSearchExperience(request);
				}
						
				if (selection.howManySelectedForExport() == 0)
				{
					return;
				}
				
				if (selection instanceof SearchTab && ((SearchTab)(selection)).isExternalSearchTab())
				{
					if(((SearchTab)(selection)).isExternalSbnSearchTab()){
						SearchTab selectedTab = (SearchTab) selection;
						Map<String, SBNFederatedItemSelection> selectedSBNFederatedRecords = selectedTab.getSelectedSBNItemsForExportOrDownload();
						List<SolrDocument> documents = new ArrayList<SolrDocument>(selectedSBNFederatedRecords.size());
						try 
						{
							Source xslt = new StreamSource(getClass().getResourceAsStream("/sbn/unimarc.xsl"));
							Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
							
							for (Entry<String, SBNFederatedItemSelection> entry : selectedSBNFederatedRecords.entrySet()){
								
									SBNFederatedItemSelection item = entry.getValue();
									StringWriter w = new StringWriter();
									engine.transform(new StreamSource(new ByteArrayInputStream(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"))), new StreamResult(w));
									Document sbnDoc = new Document(w.toString());
									String unimarcDoc = new String(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"), "UTF-8");
									Utils.convertSBNToSolrDocuments(documents, sbnDoc.getMetaData(),unimarcDoc, item.id);
								
								} 
							}catch (Exception exception)
							{
								// Nothing to be done here...
							}
						HtmlOutputWriter writer = new HtmlOutputWriter();
						writer.write(request, response, documents);
						
					}else{
						SearchTab selectedTab = (SearchTab) selection;
						Map<String, FederatedItemSelection> selectedFederatedRecords = selectedTab.getSelectedFederatedItemsForExportOrDownload();
						List<SolrDocument> documents = new ArrayList<SolrDocument>(selectedFederatedRecords.size());
						for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet())
						{
							try 
							{
								FederatedItemSelection item = entry.getValue();
								
								Utils.convertToSolrDocuments(documents, selectedTab.getPazpar2().record(item.recid, null), item.recid, item.id);
							} catch (Exception exception)
							{
								// Nothing to be done here...
							}
						}		
						HtmlOutputWriter writer = new HtmlOutputWriter();
						writer.write(request, response, documents);
					}	
				} else if (selection instanceof SearchExperience)
				{
					HtmlOutputWriter writer = new HtmlOutputWriter();
					writer.write(request, response, ((SearchExperience)selection).getSelectedDocuments());
				}  else				
				{
					ISearchEngine searchEngine = getSearchEngine(request);
					Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
					try 
					{
						// imss transform 
						Locale locale = getVisit(request).getPreferredLocale();	
						QueryResponse queryResponse = searchEngine.documents(selectedItems);
						SolrDocumentList list = queryResponse.getResults();
						StringBuilder resultsToPrint = new StringBuilder();
						for(SolrDocument document : list){
							resultsToPrint.append(htmlTransform(request, locale, document, IConstants.HTML_FULL));
						}
						forwardDownload(response, resultsToPrint.toString());
						//
					} catch (SystemInternalFailureException exception) 
					{
						// Nothing to be done here...
					}
				}
			}
		});
		
		handlers.put("htmlShort",new HttpServlet() 
		{
			private static final long serialVersionUID = 39765687945578156L;
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
			{
				ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT))
						? getSearchExperience(request).getCurrentTab()
						: getWorkspaceSelection(request);				
				
				if (isNotNullOrEmptyString(request.getParameter(HttpParameter.FOLDER_SELECTION_CONTEXT)))
				{
					selection = getSearchExperience(request);
				}
						
				if (selection.howManySelectedForExport() == 0)
				{
					return;
				}
				
				if (selection instanceof SearchTab && ((SearchTab)(selection)).isExternalSearchTab())
				{
					if(((SearchTab)(selection)).isExternalSbnSearchTab()){
						SearchTab selectedTab = (SearchTab) selection;
						Map<String, SBNFederatedItemSelection> selectedSBNFederatedRecords = selectedTab.getSelectedSBNItemsForExportOrDownload();
						List<SolrDocument> documents = new ArrayList<SolrDocument>(selectedSBNFederatedRecords.size());
						try 
						{
							Source xslt = new StreamSource(getClass().getResourceAsStream("/sbn/unimarc.xsl"));
							Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
							
							for (Entry<String, SBNFederatedItemSelection> entry : selectedSBNFederatedRecords.entrySet()){
								
									SBNFederatedItemSelection item = entry.getValue();
									StringWriter w = new StringWriter();
									engine.transform(new StreamSource(new ByteArrayInputStream(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"))), new StreamResult(w));
									Document sbnDoc = new Document(w.toString());
									String unimarcDoc = new String(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"), "UTF-8");
									Utils.convertSBNToSolrDocuments(documents, sbnDoc.getMetaData(),unimarcDoc, item.id);
								
								} 
							}catch (Exception exception)
							{
								// Nothing to be done here...
							}
						HtmlOutputWriter writer = new HtmlOutputWriter();
						writer.write(request, response, documents);
						
					}else{
						SearchTab selectedTab = (SearchTab) selection;
						Map<String, FederatedItemSelection> selectedFederatedRecords = selectedTab.getSelectedFederatedItemsForExportOrDownload();
						List<SolrDocument> documents = new ArrayList<SolrDocument>(selectedFederatedRecords.size());
						for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet())
						{
							try 
							{
								FederatedItemSelection item = entry.getValue();
								
								Utils.convertToSolrDocuments(documents, selectedTab.getPazpar2().record(item.recid, null), item.recid, item.id);
							} catch (Exception exception)
							{
								// Nothing to be done here...
							}
						}		
						HtmlOutputWriter writer = new HtmlOutputWriter();
						writer.write(request, response, documents);
					}	
				} else if (selection instanceof SearchExperience)
				{
					HtmlOutputWriter writer = new HtmlOutputWriter();
					writer.write(request, response, ((SearchExperience)selection).getSelectedDocuments());
				}  else				
				{
					ISearchEngine searchEngine = getSearchEngine(request);
					Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
					try 
					{
						// imss transfom
						Locale locale = getVisit(request).getPreferredLocale();	
						QueryResponse queryResponse = searchEngine.documents(selectedItems);
						SolrDocumentList list = queryResponse.getResults();
						StringBuilder resultsToPrint = new StringBuilder();
						for(SolrDocument document : list){						
							resultsToPrint.append(htmlTransform(request, locale, document, IConstants.HTML_SHORT));
						}
						forwardDownload(response, resultsToPrint.toString());
												
					} catch (SystemInternalFailureException exception) 
					{
						// Nothing to be done here...
					}
				}
			}
			
		});
	}
	
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		HttpServlet handler = handlers.get(request.getParameter(HttpParameter.FORMAT));
		
		String id = request.getParameter(HttpParameter.ID);
		if (isNotNullOrEmptyString(id))
		{
			SearchTab selectedTab = getSearchExperience(request).getCurrentTab();			
			//selectedTab.getSelectedItemsForExportOrDownload().add(id);
			
			Set<String> items = selectedTab.getSelectedItem();
			items.add(id);
		}
		
		handler.service(request, response);
		
		SearchTab selectedTab = getSearchExperience(request).getCurrentTab();
		Set<String> items = selectedTab.getSelectedItem();
		items.clear();
	}
	
	private void forwardDownload (HttpServletResponse response, String datas){
		final String contentDispositionHeaderName = "Content-disposition";
		final String contentDispositionHeaderPrefix = "attachment; filename=\"";
		response.reset(); 
		response.setContentType("application/octect-stream; charset=" + IConstants.UTF_8);
		response.setHeader("Content-Transfer-Encoding", "binary");
		response.setCharacterEncoding("UTF-8");
		response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.html\""); 
				
		try {
			OutputStream out = response.getOutputStream();
			out.write(datas.toString().getBytes(Charset.forName("UTF-8")));
			out.flush();
		} catch (IOException e) {
			//do nothing...
		}
			
	}
	
	protected String htmlTransform(
			final HttpServletRequest request,
			Locale locale, 			
			SolrDocument document,
			String templateType) {		
		
		String templateName = "";
		if(IConstants.HTML_FULL.equals(templateType))
			templateName = "stile";
		else { // IConstants.HTML_SHORT.equals(templateType)
			templateName = "lv_display_record__marc_21_bibliographic__txt";
		}
		
		org.w3c.dom.Document recordDetail = XsltUtility.transformXSLT(request, locale, (String) document.getFieldValue("marc_xml"), templateName);
		String recordDatailString = XsltUtility.printString(recordDetail);
		//String recordDatailString = XsltUtility.transformXSLTtoString(request, locale, (String) document.getFieldValue("marc_xml"), "stile");
		return recordDatailString;		
	}
	
	
}
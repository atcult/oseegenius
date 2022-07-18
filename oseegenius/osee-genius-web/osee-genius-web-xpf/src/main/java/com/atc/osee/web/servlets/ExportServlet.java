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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.FederatedItemSelection;
import com.atc.osee.web.model.ItemSelection;
import com.atc.osee.web.model.SBNFederatedItemSelection;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.model.SearchTab;

/**
 * OseeGenius -W- export web controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ExportServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 1L;

	private final Map<String, HttpServlet> handlers = new HashMap<String, HttpServlet>();
	{
		handlers.put("refworks", new HttpServlet() 
		{
			private static final long serialVersionUID = 3976563096945578156L;
			
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
			{
				ISearchEngine searchEngine = getSearchEngine(request);
				QueryResponse qresponse = null;
				
				// If user is in detail view there will be just one record selected.
				String documentId = request.getParameter(HttpParameter.ID);
				if (isNotNullOrEmptyString(documentId))
				{
					try 
					{
						qresponse = searchEngine.findDocumentByURI(documentId);					
					} catch (SystemInternalFailureException exception) 
					{
						// Nothing to be done here...
					}
				} else 
				{				
					if (isNotNullOrEmptyString(request.getParameter(HttpParameter.FOLDER_SELECTION_CONTEXT)))
					{
						SearchExperience folder = getSearchExperience(request);
						if (folder.howManySelectedForExport() != 0)
						{
							StringBuilder refWorksData = new StringBuilder();
							for (Iterator<SolrDocument> iterator = folder.selection(); iterator.hasNext();)
							{
								refWorksData.append(getMarc21Text((String) iterator.next().get(ISolrConstants.MARC_21_FIELD_NAME))).append("\n");
							}
							
							request.setAttribute("data", refWorksData.toString());
							forwardTo(request, response, "/refworks.jsp");				
						}
						return;
					} else 
					{
					
						ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT))
								? getSearchExperience(request).getCurrentTab()
								: getWorkspaceSelection(request);		
								
						if (selection.howManySelectedForExport() == 0)
						{
							return;
						}
					
						if (selection instanceof SearchTab && ((SearchTab)(selection)).isExternalSearchTab())
						{
							
							if(((SearchTab)(selection)).isExternalSbnSearchTab()){
								SearchTab selectedTab = (SearchTab) selection;
								Map<String, SBNFederatedItemSelection> selectedSBNFederatedRecords = selectedTab.getSelectedSBNItemsForExportOrDownload();
								StringBuilder refWorksData = new StringBuilder();
								for (Entry<String, SBNFederatedItemSelection> entry : selectedSBNFederatedRecords.entrySet())
									{
											try 
											{
												SBNFederatedItemSelection item = entry.getValue();
												String sbnR = new String(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"));
												refWorksData.append(marcXmlToVertical(sbnR));
												
											} catch (Exception exception)
											{
												// Nothing to be done here...
											}
									}
									request.setAttribute("data", refWorksData.toString());
									request.setAttribute("from", "sbn");
									forwardTo(request, response, "/refworks.jsp");
								
							}else{
								SearchTab selectedTab = (SearchTab) selection;
								Map<String, FederatedItemSelection> selectedFederatedRecords = selectedTab.getSelectedFederatedItemsForExportOrDownload();
								StringBuilder refWorksData = new StringBuilder();
								for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet())
								{
									try 
									{
										FederatedItemSelection item = entry.getValue();
										refWorksData.append(marcXmlToVertical(selectedTab.getPazpar2().record(item.recid, item.offset)));
									} catch (Exception exception)
									{
										exception.printStackTrace();
									}
								}
								request.setAttribute("data", refWorksData.toString());
								forwardTo(request, response, "/refworks.jsp");	
							}
							
						} else
						{
							Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
							try 
							{
								qresponse = searchEngine.documents(selectedItems);
							} catch (SystemInternalFailureException exception) 
							{
								// Nothing to be done here...
							}
						}
					}//else
				}//else
				
						if (qresponse != null && !qresponse.getResults().isEmpty())
						{
							StringBuilder refWorksData = new StringBuilder();
							for (SolrDocument document : qresponse.getResults())
							{
								refWorksData.append(getMarc21Text((String) document.get(ISolrConstants.MARC_21_FIELD_NAME))).append("\n");
							}
							
							request.setAttribute("data", refWorksData.toString());
							forwardTo(request, response, "/refworks.jsp");				
						}
//modificato per bug 2087(pagina bianca nel dettaglio) per inserire l'if precedente e passare i dati alla pagina jsp						
//					}//else
//				}//else
			}
			
			private Object marcXmlToVertical(String marcXml) throws IOException
			{
				MarcReader reader = new MarcXmlReader(new ByteArrayInputStream(marcXml.getBytes("UTF-8")));
				if (reader.hasNext())
				{
					return reader.next().toString();
				}
				return null;
			}

			/**
			 * Returns the given string data as MARC 21 text.
			 * 
			 * @param rawData the MARC21 raw data
			 * @return the MARC 21 text.
			 * @throws IOException in case of I/O exception.
			 * @throws IllegalArgumentException in case no data is found within the given input.
			 */
			public final String getMarc21Text(final String rawData) throws IOException
			{
				byte [] data = rawData.getBytes("UTF-8");
				ByteArrayInputStream input = new ByteArrayInputStream(data);
				MarcReader reader = new MarcXmlReader(input);
				
				if (reader.hasNext())
				{
					return reader.next().toString().replace(
							IConstants.RECORD_LABEL_RAWDATA_LEADER, 
							IConstants.RECORD_LABEL_MARC21_LEADER);
				}
				throw new IllegalArgumentException();
			}			
		});
	}
	
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		HttpServlet handler = handlers.get(request.getParameter("target"));
		handler.service(request, response);
	}
}
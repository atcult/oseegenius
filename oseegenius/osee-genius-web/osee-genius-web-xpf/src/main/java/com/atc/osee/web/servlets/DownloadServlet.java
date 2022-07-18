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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;
import com.atc.osee.web.model.FederatedItemSelection;
import com.atc.osee.web.model.ItemSelection;
import com.atc.osee.web.model.SBNFederatedItemSelection;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.sbn.Document;
import com.atc.osee.web.servlets.writers.ExcelOutputWriter;
import com.atc.osee.web.servlets.writers.HtmlOutputWriter;
import com.atc.osee.web.servlets.writers.PDFOutputWriter;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.util.Utils;

/**
 * OseeGenius -W- download web controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class DownloadServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = -3624415330654606512L;
	private final String contentDispositionHeaderName = "Content-disposition";
	private final String contentDispositionHeaderPrefix = "attachment; filename=\"";
	
	private final Map<String, HttpServlet> handlers = new HashMap<String, HttpServlet>();
	{
		handlers.put("m21", new HttpServlet() 
		{
			private static final long serialVersionUID = 3976563096945578156L;
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
			{
				if (isNotNullOrEmptyString(request.getParameter(HttpParameter.FOLDER_SELECTION_CONTEXT)))
				{
					SearchExperience folder = getSearchExperience(request);
					if (folder.howManySelectedForExport() != 0)
					{
						response.reset(); 
						response.setContentType("application/octect-stream; charset=" + IConstants.UTF_8); 
						response.setCharacterEncoding("UTF-8");
						response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.mrc\""); 
						MarcWriter writer = null;
						MarcReader reader = null;
						
						 try 
						 {
							 writer = new MarcStreamWriter(response.getOutputStream(), "UTF-8");
							 for (Iterator<SolrDocument> iterator = folder.selection(); iterator.hasNext();)
							 {
								 reader = new MarcXmlReader(
										 new ByteArrayInputStream(((String)iterator.next().get(ISolrConstants.MARC_21_FIELD_NAME)).getBytes("UTF-8")));
								 if (reader.hasNext())
								 {
									Record record = reader.next();
									cleanIfTooMuchLong(record);
									writer.write(record);
								}
							 }
						 } catch (UnsupportedEncodingException exception)
						 {
							 Log.error(MessageCatalog._100024_UNSUPPORTED_ENCODING, exception);
						 } finally 
						 {
							 writer.close();
						 }
					}
					return;
				} 
				
				ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT))
						? getSearchExperience(request).getCurrentTab()
						: getWorkspaceSelection(request);				
				
				if (selection.howManySelectedForExport() == 0)
				{
					return;
				}
				
				response.reset(); 
				response.setContentType("application/octect-stream; charset=" + IConstants.UTF_8); 
				response.setCharacterEncoding("UTF-8");
				response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.mrc\""); 
				
				if (selection instanceof SearchTab && ((SearchTab)(selection)).isExternalSearchTab())
				{
					SearchTab selectedTab = (SearchTab) selection;
					MarcReader reader = null;
					MarcWriter writer = new MarcStreamWriter(response.getOutputStream(), "UTF-8");
					
					
					 if(((SearchTab)(selection)).isExternalSbnSearchTab()){
						 
						 Map<String, SBNFederatedItemSelection> selectedSBNFederatedRecords = selectedTab.getSelectedSBNItemsForExportOrDownload();
							for (Entry<String, SBNFederatedItemSelection> entry : selectedSBNFederatedRecords.entrySet())
							{
								try 
								{
									SBNFederatedItemSelection item = entry.getValue();
									reader = new MarcXmlReader(new ByteArrayInputStream(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml")));
									
									if (reader.hasNext())
									 {
										writer.write(reader.next());
									}
								} catch (Exception exception)
								{
									// Nothing to be done here...
								}
							}
			
					 }else{
					
					
						try 
						{
							Map<String, FederatedItemSelection> selectedFederatedRecords = selectedTab.getSelectedFederatedItemsForExportOrDownload();
							for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet())
							{
								try 
								{
									FederatedItemSelection item = entry.getValue();
									reader = new MarcXmlReader(new ByteArrayInputStream(selectedTab.getPazpar2().record(item.recid, item.offset).getBytes("UTF-8")));
									if (reader.hasNext())
									 {
										writer.write(reader.next());
									}
								} catch (Exception exception)
								{
									// Nothing to be done here...
								}
							}
						} finally 
						{
							try 
							{
								writer.close();
							} catch (Exception ignore) 
							{
								// Do nothing
							}
						}
					 }	
				} else 
				{
					ISearchEngine searchEngine = getSearchEngine(request);
					Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
					try 
					{
						QueryResponse queryResponse = searchEngine.documents(selectedItems);

						 
						MarcWriter writer = null;
						MarcReader reader = null;
						
						 try 
						 {
							 writer = new MarcStreamWriter(response.getOutputStream(), "UTF-8");
							 for (SolrDocument document : queryResponse.getResults())
							 {
								 reader = new MarcXmlReader(
										 new ByteArrayInputStream(((String)document.get(ISolrConstants.MARC_21_FIELD_NAME)).getBytes("UTF-8")));
								 if (reader.hasNext())
								 {
									Record record = reader.next();
									cleanIfTooMuchLong(record);
									writer.write(record);
								}
							 }
						 } catch (UnsupportedEncodingException exception)
						 {
							 Log.error(MessageCatalog._100024_UNSUPPORTED_ENCODING, exception);
						 } finally 
						 {
							 writer.close();
						 }
	
					} catch (SystemInternalFailureException exception) 
					{
						// Nothing to be done here...
					}
				}
			}
		});
		
		handlers.put("endnote", new HttpServlet() 
		{
			private static final long serialVersionUID = 3976563096945578156L;
			
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
			{
				ConfigurationTool configuration = getConfiguration(request);
				
				String templateUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() 
						+ request.getContextPath() + "/resource?uri=";
				URL baseURL = new URL(templateUrl);	
				
				if (isNotNullOrEmptyString(request.getParameter(HttpParameter.FOLDER_SELECTION_CONTEXT)))
				{
					SearchExperience folder = getSearchExperience(request);
					if (folder.howManySelectedForExport() != 0)
					{
						response.reset(); 
						response.setContentType("application/x-endnote-refer;charset=UTF-8"); 
						response.setCharacterEncoding("UTF-8");
						response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.endnote\""); 
						
						try 
						 {
							 Source xslt = new StreamSource(getClass().getResourceAsStream("/marc2ris.xsl"));
							 Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
							 engine.setParameter("baseurl", baseURL);
							 
							 StreamSource source = new StreamSource();
							 for (Iterator<SolrDocument> iterator = folder.selection(); iterator.hasNext();)
							 {
								 source.setReader(new StringReader((String)iterator.next().get(ISolrConstants.MARC_21_FIELD_NAME)));
								 engine.transform(source, new StreamResult(response.getWriter()));
							 }
						 } catch (Exception exception)
						 {
							Log.error("", exception); 
						 } finally 
						 {
							 response.flushBuffer();
						 }
					}
				
					return;
				} 
				
				ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT))
						? getSearchExperience(request).getCurrentTab()
						: getWorkspaceSelection(request);				
				
				if (selection.howManySelectedForExport() == 0)
				{
					return;
				}
				
				response.reset(); 
				response.setContentType("application/x-endnote-refer;charset=UTF-8"); 
				response.setCharacterEncoding("UTF-8");
				response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.endnote\""); 
				
				if (selection instanceof SearchTab && ((SearchTab)(selection)).isExternalSearchTab())
				{
					
					if(((SearchTab)(selection)).isExternalSbnSearchTab()){
						
						 SearchTab selectedTab = (SearchTab) selection;
						 Map<String, SBNFederatedItemSelection> selectedSBNFederatedRecords = selectedTab.getSelectedSBNItemsForExportOrDownload();
						
								try 
								{
									Source xslt = new StreamSource(getClass().getResourceAsStream("/sbn/unimarc2ris.xsl"));
									Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
									engine.setParameter("baseurl", baseURL);
									 
									StreamSource source = new StreamSource();
									for (Entry<String, SBNFederatedItemSelection> entry : selectedSBNFederatedRecords.entrySet())
									{
										try 
										{
											SBNFederatedItemSelection item = entry.getValue();
											String sbnR = new String(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"));
											StringBuffer r = new StringBuffer("<collection xmlns=\"http://www.loc.gov/MARC21/slim\">")
											.append(sbnR)
											.append("</collection>");
											source.setReader(new StringReader(r.toString()));
											engine.transform(source, new StreamResult(response.getWriter()));
										
										} catch (Exception exception)
										{
											// Nothing to be done here...
										}
									}
								} catch (Exception exception)
								{
									Log.error("", exception); 
								} finally 
								{
									try 
									{
										response.flushBuffer();
									} catch (Exception ignore) 
									{
										// Do nothing
									}
								}
								
			
					 }else{
						SearchTab selectedTab = (SearchTab) selection;
						 
						Map<String, FederatedItemSelection> selectedFederatedRecords = selectedTab.getSelectedFederatedItemsForExportOrDownload();
						
						try 
						{
							Source xslt = new StreamSource(getClass().getResourceAsStream("/marc2ris.xsl"));
							Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
							engine.setParameter("baseurl", baseURL);
							 
							StreamSource source = new StreamSource();
							for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet())
							{
								try 
								{
									FederatedItemSelection item = entry.getValue();	
									source.setReader(new StringReader(selectedTab.getPazpar2().record(item.recid, item.offset)));
									engine.transform(source, new StreamResult(response.getWriter()));
								} catch (Exception exception)
								{
									// Nothing to be done here...
								}
							}
						} catch (Exception exception)
						{
							Log.error("", exception); 
						} finally 
						{
							try 
							{
								response.flushBuffer();
							} catch (Exception ignore) 
							{
								// Do nothing
							}
						}
					 }
				} else {
					ISearchEngine searchEngine = getSearchEngine(request);
					Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
					try  
					{
						QueryResponse queryResponse = searchEngine.documents(selectedItems);
						Source xslt = new StreamSource(getClass().getResourceAsStream("/marc2ris.xsl"));
						Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
						engine.setParameter("baseurl", baseURL);
						 try 
						 {
							 StreamSource source = new StreamSource();
							 for (SolrDocument document : queryResponse.getResults())
							 {
								 source.setReader(new StringReader((String)document.get(ISolrConstants.MARC_21_FIELD_NAME)));
								 engine.transform(source, new StreamResult(response.getWriter()));
							 }
						 } catch (UnsupportedEncodingException exception)
						 {
							 Log.error(MessageCatalog._100024_UNSUPPORTED_ENCODING, exception);
						 } 
					} catch (Exception exception) 
					{
						 							Log.error("", exception); 
					}finally 
					 {
						 response.flushBuffer();
					 }
				}
			}
		});		
		
		handlers.put("zotero", new HttpServlet() 
		{
			private static final long serialVersionUID = 3976563096945578156L;
			
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
			{
				
				ConfigurationTool configuration = getConfiguration(request);
				
				
				String templateUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() 
						+ request.getContextPath() + "/resource?uri=";
				URL baseURL = new URL(templateUrl);	
				
				if (isNotNullOrEmptyString(request.getParameter(HttpParameter.FOLDER_SELECTION_CONTEXT)))
				{
					SearchExperience folder = getSearchExperience(request);
					if (folder.howManySelectedForExport() != 0)
					{
						response.reset(); 
						response.setContentType("application/x-research-info-systems;charset=UTF-8"); 
						response.setCharacterEncoding("UTF-8");
						response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.ris\""); 
						
						try 
						 {
							 Source xslt = new StreamSource(getClass().getResourceAsStream("/marc2ris.xsl"));
							 Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
							 engine.setParameter("baseurl", baseURL);
							 
							 StreamSource source = new StreamSource();
							 for (Iterator<SolrDocument> iterator = folder.selection(); iterator.hasNext();)
							 {
								 source.setReader(new StringReader((String)iterator.next().get(ISolrConstants.MARC_21_FIELD_NAME)));
								 engine.transform(source, new StreamResult(response.getWriter()));
							 }
						 } catch (Exception exception)
						 {
														Log.error("", exception);  
						 } finally 
						 {
								response.flushBuffer();
						 }
					}
				
					return;
				} 
				
				ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT))
						? getSearchExperience(request).getCurrentTab()
						: getWorkspaceSelection(request);				
				
				if (selection.howManySelectedForExport() == 0)
				{
					return;
				}
				
				response.reset(); 
				response.setContentType("application/x-research-info-systems;charset=UTF-8"); 
				response.setCharacterEncoding("UTF-8");
				response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.ris\""); 
				
				if (selection instanceof SearchTab && ((SearchTab)(selection)).isExternalSearchTab())
				{
					if(((SearchTab)(selection)).isExternalSbnSearchTab()){
									
						SearchTab selectedTab = (SearchTab) selection;
						 
						 Map<String, SBNFederatedItemSelection> selectedSBNFederatedRecords = selectedTab.getSelectedSBNItemsForExportOrDownload();
						
						try 
						{
							Source xslt = new StreamSource(getClass().getResourceAsStream("/sbn/unimarc2ris.xsl"));
							Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
							engine.setParameter("baseurl", baseURL);
							 
							StreamSource source = new StreamSource();
							for (Entry<String, SBNFederatedItemSelection> entry : selectedSBNFederatedRecords.entrySet())
							{
								try 
								{
									SBNFederatedItemSelection item = entry.getValue();
									String sbnR = new String(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"));
									StringBuffer r = new StringBuffer("<collection xmlns=\"http://www.loc.gov/MARC21/slim\">")
									.append(sbnR)
									.append("</collection>");
									source.setReader(new StringReader(r.toString()));
									engine.transform(source, new StreamResult(response.getWriter()));
								
								} catch (Exception exception)
								{
									// Nothing to be done here...
								}
							}
						} catch (Exception exception)
						{
														Log.error("", exception); 
						} finally 
						{
							try 
							{
								response.flushBuffer();
							} catch (Exception ignore) 
							{
								// Do nothing
							}
						}
						
						
					}else{
						SearchTab selectedTab = (SearchTab) selection;
						 
						Map<String, FederatedItemSelection> selectedFederatedRecords = selectedTab.getSelectedFederatedItemsForExportOrDownload();
						
						try 
						{
							Source xslt = new StreamSource(getClass().getResourceAsStream("/marc2ris.xsl"));
							Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
							engine.setParameter("baseurl", baseURL);
							 
							StreamSource source = new StreamSource();
							for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet())
							{
								try 
								{
									FederatedItemSelection item = entry.getValue();	
									source.setReader(new StringReader(selectedTab.getPazpar2().record(item.recid, item.offset)));
									engine.transform(source, new StreamResult(response.getWriter()));
								} catch (Exception exception)
								{
									// Nothing to be done here...
								}
							}
						} catch (Exception exception)
						{
														Log.error("", exception); 
						} finally 
						{
							try 
							{
								response.flushBuffer();
							} catch (Exception ignore) 
							{
								// Do nothing
							}
						}
					} 
			}else 
				{
					ISearchEngine searchEngine = getSearchEngine(request);
					Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
					try  
					{
						QueryResponse queryResponse = searchEngine.documents(selectedItems);
						Source xslt = new StreamSource(getClass().getResourceAsStream("/marc2ris.xsl"));
						Transformer engine = TransformerFactory.newInstance().newTransformer(xslt);
						engine.setParameter("baseurl", baseURL);
						 try 
						 {
							 StreamSource source = new StreamSource();
							 for (SolrDocument document : queryResponse.getResults())
							 {
								 source.setReader(new StringReader((String)document.get(ISolrConstants.MARC_21_FIELD_NAME)));
								 engine.transform(source, new StreamResult(response.getWriter()));
							 }
						 } catch (UnsupportedEncodingException exception)
						 {
							 Log.error(MessageCatalog._100024_UNSUPPORTED_ENCODING, exception);
						 } 
					} catch (Exception exception) 
					{
						Log.error("", exception); 
					}finally 
					 {
						 response.flushBuffer();
					 }
				}
			}
		});			
		
		handlers.put("mxml", new HttpServlet() 
		{
			private static final long serialVersionUID = 3976563096945578156L;
			
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
			{
				if (isNotNullOrEmptyString(request.getParameter(HttpParameter.FOLDER_SELECTION_CONTEXT)))
				{
					SearchExperience folder = getSearchExperience(request);
					if (folder.howManySelectedForExport() != 0)
					{
						response.reset(); 
						response.setCharacterEncoding("UTF-8");
						response.setContentType("text/html; charset=" + IConstants.UTF_8);
						response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix +"oseegenius_export.xml\""); 
						MarcReader reader = null;
						
						 try 
						 {
							 response.getWriter().write("<collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
							 for (Iterator<SolrDocument> iterator = folder.selection(); iterator.hasNext();)
							 {
								 reader = new MarcXmlReader(
										 new ByteArrayInputStream(((String)iterator.next().get(ISolrConstants.MARC_21_FIELD_NAME)).getBytes("UTF-8")));
								 if (reader.hasNext())
								 {
									 response.getWriter().write((String)iterator.next().get(ISolrConstants.MARC_21_FIELD_NAME));
								}
							 }
							 response.getWriter().write("</collection>");
						 } catch (UnsupportedEncodingException exception)
						 {
							 Log.error(MessageCatalog._100024_UNSUPPORTED_ENCODING, exception);
						 }
					}
					return;
				} 
				
				ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT))
						? getSearchExperience(request).getCurrentTab()
						: getWorkspaceSelection(request);				
						
				if (selection.howManySelectedForExport() == 0)
				{
					return;
				}
				response.reset(); 
				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html; charset=" + IConstants.UTF_8);
				response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.xml\""); 
				
				if (selection instanceof SearchTab && ((SearchTab)(selection)).isExternalSearchTab())
				{
					
					if(((SearchTab)(selection)).isExternalSbnSearchTab()){
						SearchTab selectedTab = (SearchTab) selection;
						response.getWriter().write("<collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
						
						 Map<String, SBNFederatedItemSelection> selectedSBNFederatedRecords = selectedTab.getSelectedSBNItemsForExportOrDownload();
							for (Entry<String, SBNFederatedItemSelection> entry : selectedSBNFederatedRecords.entrySet())
							{
								try 
								{
									SBNFederatedItemSelection item = entry.getValue();
									
									String r = new String(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml"));
									response.getWriter().write(r);
									
								} catch (Exception exception)
								{
									// Nothing to be done here...
								}
							}
							response.getWriter().write("</collection>");
						
					}else{
						SearchTab selectedTab = (SearchTab) selection;
						response.getWriter().write("<collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
						
						Map<String, FederatedItemSelection> selectedFederatedRecords = selectedTab.getSelectedFederatedItemsForExportOrDownload();
							for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet())
							{
								try 
								{
									FederatedItemSelection item = entry.getValue();
									response.getWriter().write(selectedTab.getPazpar2().record(item.recid, item.offset));
								} catch (Exception exception)
								{
									// Nothing to be done here...
								}
							}
							response.getWriter().write("</collection>");
					}	
				} else 
				{
					
					ISearchEngine searchEngine = getSearchEngine(request);
					Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
					try 
					{
						QueryResponse queryResponse = searchEngine.documents(selectedItems);
						
						 try 
						 {
						 	 response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
							 response.getWriter().write("<collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
							 for (SolrDocument document : queryResponse.getResults())
							 {
								 String record= (String)document.get(ISolrConstants.MARC_XML_FIELD_NAME);
								 record=record.substring(record.indexOf("<record>"), record.indexOf("</collection>"));
								 response.getWriter().write(record);
								 //response.getWriter().write((String)document.get(ISolrConstants.MARC_XML_FIELD_NAME));
							 }
							 response.getWriter().write("</collection>");
						 } catch (UnsupportedEncodingException exception)
						 {
							 Log.error(MessageCatalog._100024_UNSUPPORTED_ENCODING, exception);
						 } 
					} catch (SystemInternalFailureException exception) 
					{
						// Nothing to be done here...
					}
				}
			}
		});		
		
		handlers.put("tmarc", new HttpServlet() 
		{
			private static final long serialVersionUID = 3976563096945578156L;
			
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
			{
				if (isNotNullOrEmptyString(request.getParameter(HttpParameter.FOLDER_SELECTION_CONTEXT)))
				{
					SearchExperience folder = getSearchExperience(request);
					if (folder.howManySelectedForExport() != 0)
					{
						response.reset(); 
						response.setContentType("text/plain; charset=" + IConstants.UTF_8);
						response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.txt\""); 
						
						MarcReader reader = null;
						
						 try 
						 {
							 for (Iterator<SolrDocument> iterator = folder.selection(); iterator.hasNext();)
							 {
								 reader = new MarcXmlReader(
										 new ByteArrayInputStream(((String)iterator.next().get(ISolrConstants.MARC_21_FIELD_NAME)).getBytes("UTF-8")));
								 if (reader.hasNext())
								 {
									response.getWriter().println(reader.next());
								}
							 }
						 } catch (UnsupportedEncodingException exception)
						 {
							 Log.error(MessageCatalog._100024_UNSUPPORTED_ENCODING, exception);
						 } 
					}
					return;
				} 
				
				ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT))
						? getSearchExperience(request).getCurrentTab()
						: getWorkspaceSelection(request);				
						
				if (selection.howManySelectedForExport() == 0)
				{
					return;
				}
				
				
				response.reset(); 
				response.setContentType("text/plain"); 
				response.setHeader(contentDispositionHeaderName, contentDispositionHeaderPrefix + "oseegenius_export.txt\""); 
				
				if (selection instanceof SearchTab && ((SearchTab)(selection)).isExternalSearchTab())
				{
					SearchTab selectedTab = (SearchTab) selection;
					MarcReader reader = null;
					
					
					if(((SearchTab)(selection)).isExternalSbnSearchTab()){
						 
						 Map<String, SBNFederatedItemSelection> selectedSBNFederatedRecords = selectedTab.getSelectedSBNItemsForExportOrDownload();
							for (Entry<String, SBNFederatedItemSelection> entry : selectedSBNFederatedRecords.entrySet())
							{
								try 
								{
									SBNFederatedItemSelection item = entry.getValue();
									
									reader = new MarcXmlReader(new ByteArrayInputStream(selectedTab.getSbnResponse().getResultSet().getRecord(item.posInResultSet).get("xml")));
								
						         
									if (reader.hasNext())
									 {
										response.getWriter().println(reader.next());
									 }
								} catch (Exception exception)
								{
									// Nothing to be done here...
								}
							}
					}else{		
					 
						Map<String, FederatedItemSelection> selectedFederatedRecords = selectedTab.getSelectedFederatedItemsForExportOrDownload();
						for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet())
						{
							try 
							{
								FederatedItemSelection item = entry.getValue();
								reader = new MarcXmlReader(new ByteArrayInputStream(selectedTab.getPazpar2().record(item.recid, item.offset).getBytes("UTF-8")));
								if (reader.hasNext())
								 {
									response.getWriter().println(reader.next());
								}
							} catch (Exception exception)
							{
								// Nothing to be done here...
							}
						}
					}	
				} else 
				{
					ISearchEngine searchEngine = getSearchEngine(request);
					Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
					try 
					{ 
						QueryResponse queryResponse = searchEngine.documents(selectedItems);
						 try 
						 {
							 for (SolrDocument document : queryResponse.getResults())
							 {
								 MarcReader reader = new MarcXmlReader(new ByteArrayInputStream(((String)document.get(ISolrConstants.MARC_21_FIELD_NAME)).getBytes("UTF-8")));
								 if (reader.hasNext())
								 {
									Record record = reader.next();
									cleanIfTooMuchLong(record);
									response.getWriter().println(record.toString());
								}
							 }
						 } catch (UnsupportedEncodingException exception)
						 {
							 Log.error(MessageCatalog._100024_UNSUPPORTED_ENCODING, exception);
						 } 
					} catch (SystemInternalFailureException exception) 
					{
						// Nothing to be done here...
					}
				}
			}
		});				
		
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
						QueryResponse queryResponse = searchEngine.documents(selectedItems);
						HtmlOutputWriter writer = new HtmlOutputWriter();
						writer.write(request, response, queryResponse.getResults());
					} catch (SystemInternalFailureException exception) 
					{
						// Nothing to be done here...
					}
				}
			}
		});		
		
		handlers.put("xls", new HttpServlet() 
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
						ExcelOutputWriter writer = new ExcelOutputWriter();
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
						ExcelOutputWriter writer = new ExcelOutputWriter();
	//					documents.add(0, new SolrDocument());
						writer.write(request, response, documents);
					}		
				} else if (selection instanceof SearchExperience)
				{
					
					List<SolrDocument> documents = null;
					try 
					{
						ExcelOutputWriter writer = new ExcelOutputWriter();
						documents = ((SearchExperience)selection).getSelectedDocuments();
						//documents.add(0, new SolrDocument());
						writer.write(request, response, documents);
					} catch (Exception exception)
					{
						// Ignore
					} finally
					{
						if (documents != null) documents.remove(0);
					}
				} else
				{
					ISearchEngine searchEngine = getSearchEngine(request);
					Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
					List<SolrDocument> documents = null;
					try 
					{
						QueryResponse queryResponse = searchEngine.documents(selectedItems);
						ExcelOutputWriter writer = new ExcelOutputWriter();
						documents = queryResponse.getResults();
						//bug 2286
						//documents.add(0, new SolrDocument());
						writer.write(request, response, queryResponse.getResults());
					} catch (SystemInternalFailureException exception) 
					{
						// Nothing to be done here...
					} finally
					{
						if (documents != null) documents.remove(0);
					}
				}
			}
		});		
		
		handlers.put("pdf", new HttpServlet() 
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
						PDFOutputWriter writer = new PDFOutputWriter();
						writer.write(request, response,documents);
						
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
						PDFOutputWriter writer = new PDFOutputWriter();
						writer.write(request, response,documents);
					}	
				} else if (selection instanceof SearchExperience)
				{
					PDFOutputWriter writer = new PDFOutputWriter();
					writer.write(request, response, ((SearchExperience)selection).getSelectedDocuments());
				} else
				{
					ISearchEngine searchEngine = getSearchEngine(request);
					Set<String> selectedItems = selection.getSelectedItemsForExportOrDownload();
					
					try 
					{
						QueryResponse queryResponse = searchEngine.documents(selectedItems);
						PDFOutputWriter writer = new PDFOutputWriter();
						writer.write(request, response, queryResponse.getResults());
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
	
	private Record elaborateTitleTag(Record toElaborate){
		 Record r = toElaborate;
		 DataField fieldTitle = (DataField) r.getVariableField("245");
		 Subfield f = fieldTitle.getSubfield(new Character('a'));

		 f.setData(f.getData().replace(" :", ":"));
		 return r;
	}
	
	@SuppressWarnings("unchecked")
	private void cleanIfTooMuchLong(final Record record) {
		List<VariableField> fields = record.getVariableFields("850");
		if (fields.size() > 70) {
			for (VariableField f : fields) {
				record.removeVariableField(f);
			}
		}
		
		fields = record.getVariableFields("852");
		if (fields.size() > 70) {
			for (VariableField f : fields) {
				record.removeVariableField(f);
			}
		}		
	}
	
}
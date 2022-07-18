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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.taglibs.standard.lang.jpath.adapter.StatusIterationContext;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.model.community.WorkspaceSelection;

/**
 * Web controller for selecting / deselecting items.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class SelectorServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 1L;
    
	private final Map<String, HttpServlet> actions = new HashMap<String, HttpServlet>();
	{
		
		actions.put("toggleAll", new HttpServlet() 
		{
			private static final long serialVersionUID = -6867188958557892855L;
	
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				boolean checkAll = Boolean.valueOf(request.getParameter("checkAll"));	
				
				final SearchTab tab = getSearchExperience(request).getCurrentTab();
				tab.selectOrUnselectAll(checkAll);
				int howManySendableItems  = tab.howManySelectedForSend();		
				int howManyExportableItems = tab.howManySelectedForExport();
				response.getWriter().print(
						new StringBuilder("{\"result\": {")
							.append("\"selected\":\"").append(checkAll).append("\",")
							.append("\"export2\":").append(howManyExportableItems).append(",")
							.append("\"send\":").append(howManySendableItems).append("}}").toString());
			}
		});


		actions.put("toggleCopy", new HttpServlet() {
			private static final long serialVersioUID = 35363463463533L;
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
				String barcode = request.getParameter("barcode");				
				String title = request.getParameter("title");
				String author = request.getParameter("author");
				String inventory = request.getParameter("inventory");
				String location = request.getParameter("location");
				String collocation = request.getParameter("collocation");
				String recordId = request.getParameter("recordId");
				if (barcode != null) {
					SearchTab tab = getSearchExperience(request).getCurrentTab();
					tab.toggleCopySelection(barcode, title, author, inventory, location, collocation, recordId);
				}
			}
		});
		
		actions.put("toggle", new HttpServlet() 
		{
			private static final long serialVersionUID = -6867188958557892855L;
	
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				String uri = request.getParameter(HttpParameter.URI);	
				String recid = request.getParameter(HttpParameter.RECID);	
				
				boolean canBeExported = Boolean.valueOf(request.getParameter(HttpParameter.CAN_BE_EXPORTED_OR_DOWNLOADED));	
				
				if (uri != null)
				{
					SearchTab tab = getSearchExperience(request).getCurrentTab();
					boolean hasBeenSelected = false;
					if (recid != null)
					{
						try
						{
							hasBeenSelected = tab.toggleFederatedSendableSelection(
									uri,
									recid, 
									Integer.parseInt(request.getParameter(HttpParameter.OFFSET)));
						} catch (Exception exception)
						{
							return;
						}
					} else 
					{
						 hasBeenSelected = tab.toggleSendableSelection(uri);
					}
					
					int howManySendableItems  = tab.howManySelectedForSend();						

					int howManyExportableItems = 0;
					if (canBeExported)
					{
						if (recid != null)
						{
							try 
							{
								hasBeenSelected = tab.toggleFederatedSelection(
										uri, 
										recid, 
										Integer.parseInt(request.getParameter(HttpParameter.OFFSET)));
							} catch (Exception exception)
							{
								return;
							}
						} else 
						{
							hasBeenSelected = tab.toggleSelection(uri);
						}
					
						howManyExportableItems = tab.howManySelectedForExport();
					}
					
					/*
					 	{"result":
							{ "selected" : "true"},
							{ "for-export": 10}
							{ "for-send" :5}
						}	
					*/
					response.getWriter().print(
							new StringBuilder("{\"result\": {")
								.append("\"selected\":\"").append(hasBeenSelected).append("\",")
								.append("\"export2\":").append(howManyExportableItems).append(",")
								.append("\"send\":").append(howManySendableItems).append("}}").toString());
					return;
				}
				response.getWriter().print(true);			
			}
		});

		actions.put("foldertoggle", new HttpServlet() 
		{
			private static final long serialVersionUID = -6867188958557892855L;
	
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				String uri = request.getParameter(HttpParameter.URI);	
				
				boolean canBeExported = Boolean.valueOf(request.getParameter(HttpParameter.CAN_BE_EXPORTED_OR_DOWNLOADED));	
				
				if (uri != null)
				{					
					SearchExperience folder = getSearchExperience(request);
					boolean hasBeenSelected = false;
					hasBeenSelected = folder.toggleSendableSelection(uri);
					
					int howManySendableItems  = folder.howManySelectedForSend();						

					int howManyExportableItems = 0;
					if (canBeExported)
					{
						hasBeenSelected = folder.toggleSelection(uri);
						howManyExportableItems = folder.howManySelectedForExport();
					}
					
					/*
					 	{"result":
							{ "selected" : "true"},
							{ "for-export": 10}
							{ "for-send" :5}
						}	
					*/
					response.getWriter().print(
							new StringBuilder("{\"result\": {")
								.append("\"selected\":\"").append(hasBeenSelected).append("\",")
								.append("\"export2\":").append(howManyExportableItems).append(",")
								.append("\"send\":").append(howManySendableItems).append("}}").toString());
					return;
				}
				response.getWriter().print(true);			
			}
		});
		
		actions.put("copyFoldertoggle", new HttpServlet() 
		{
			private static final long serialVersionUID = -6867188958235363455L;
	
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				String uri = request.getParameter(HttpParameter.URI);					
				
				if (uri != null)
				{					
					SearchExperience folder = getSearchExperience(request);
					boolean hasBeenSelected = false;
					hasBeenSelected = folder.toggleCopySelection(uri);

					int	howMany = 0;
					howMany = folder.howManyCopySelected();
					
					response.getWriter().print(
							new StringBuilder("{\"result\": {")
								.append("\"selected\":\"").append(hasBeenSelected).append("\",")
								.append("\"send\":").append(howMany).append("}}").toString());
					return;
				}
				response.getWriter().print(true);			
			}
		});
		
		
		actions.put("wtoggle", new HttpServlet() 
		{
			private static final long serialVersionUID = -6867188958557892855L;
	
			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				String uri = request.getParameter(HttpParameter.URI);	
				
				boolean canBeExported = Boolean.valueOf(request.getParameter(HttpParameter.CAN_BE_EXPORTED_OR_DOWNLOADED));	
				
				if (uri != null)
				{
					
					WorkspaceSelection selection = getWorkspaceSelection(request);
					boolean hasBeenSelected = false;
					hasBeenSelected = selection.toggleSendableSelection(uri);
					
					int howManySendableItems  = selection.howManySelectedForSend();						

					int howManyExportableItems = 0;
					if (canBeExported)
					{
						hasBeenSelected = selection.toggleSelection(uri);
						howManyExportableItems = selection.howManySelectedForExport();
					}
					
					/*
					 	{"result":
							{ "selected" : "true"},
							{ "for-export": 10}
							{ "for-send" :5}
						}	
					*/
					response.getWriter().print(
							new StringBuilder("{\"result\": {")
								.append("\"selected\":\"").append(hasBeenSelected).append("\",")
								.append("\"export2\":").append(howManyExportableItems).append(",")
								.append("\"send\":").append(howManySendableItems).append("}}").toString());
					return;
				}
				response.getWriter().print(true);			
			}
		});

		actions.put("cleanFolder", new HttpServlet() 
		{	
			private static final long serialVersionUID = -5730330866782930969L;

			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				SearchExperience experience = getSearchExperience(request);
				experience.cleanSelection(); 
				response.getWriter().print(
							new StringBuilder("{\"result\": [{\"selected\":\"")
								.append("\"},{\"count\":\"")
								.append(0)
								.append("\"}]}").toString());
			}
		});	
		
		actions.put("cleanCopyFolder", new HttpServlet() 
		{	
			private static final long serialVersionUID = -5730330836463930969L;

			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				SearchExperience experience = getSearchExperience(request);
				experience.cleanCopySelection(); 
				response.getWriter().print(
							new StringBuilder("{\"result\": [{\"selected\":\"")
								.append("\"},{\"count\":\"")
								.append(0)
								.append("\"}]}").toString());
			}
		});	

		actions.put("selectOrDeselectAllInFolder", new HttpServlet() 
		{	
			private static final long serialVersionUID = -5730330866782930969L;

			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				SearchExperience experience = getSearchExperience(request);

				boolean folderIsNowSelected = false;
				if (experience.isFolderAlreadySelected())
				{
					experience.cleanSelection();					
				} else
				{
					experience.selectAll();
					folderIsNowSelected = true; 
				}
				
				response.getWriter().print("{\"allselected\": " + folderIsNowSelected + "}");
			}
		});	
		
		actions.put("selectOrDeselectAllCopiesInFolder", new HttpServlet() 
		{	
			private static final long serialVersionUID = -573033086745450969L;

			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				SearchExperience experience = getSearchExperience(request);

				boolean folderIsNowSelected = false;
				if (experience.isCopyFolderAlreadySelected())
				{
					experience.cleanCopySelection();					
				} else
				{
					experience.selectAllCopies();
					folderIsNowSelected = true; 
				}
				
				response.getWriter().print("{\"allselected\": " + folderIsNowSelected + "}");
			}
		});	

		actions.put("clean", new HttpServlet() 
		{	
			private static final long serialVersionUID = -5730330866782930969L;

			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				SearchTab tab = getSearchExperience(request).getCurrentTab();
				WorkspaceSelection workspaceSelection = getWorkspaceSelection(request);
				workspaceSelection.clearSelection();
				tab.clearSelection();
				request.getSession().removeAttribute("selection");
				response.getWriter().print(
							new StringBuilder("{\"result\": [{\"selected\":\"")
								.append("\"},{\"count\":\"")
								.append(0)
								.append("\"}]}").toString());
			}
		});	
		
		actions.put("get", new HttpServlet() 
		{	
			private static final long serialVersionUID = -5730330866782930969L;

			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				SearchTab tab = getSearchExperience(request).getCurrentTab();
				Set<String> selectedIds = tab.getSelectedFederatedItemsForExportOrDownload().keySet();
				StringBuilder builder = new StringBuilder(
						"{" +
							"\"result\": " +
								"[");
				int index = 0;
				for (String id : selectedIds)
				{
					if (index > 0) { builder.append(","); }
					builder.append(id);
					index++;
				}
				
				builder.append("]}");
					
				response.getWriter().print(builder.toString());
			}
		});		
		
		actions.put("sbntoggle", new HttpServlet() 
		{
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 5802417773998147114L;

			@Override
			protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
			{
				String id = request.getParameter("id");	
				String posInResultSet = request.getParameter("pos");
				
				
				boolean canBeExported = Boolean.valueOf(request.getParameter(HttpParameter.CAN_BE_EXPORTED_OR_DOWNLOADED));	
				
				if (id != null)
				{
					SearchTab tab = getSearchExperience(request).getCurrentTab();
					boolean hasBeenSelected = false;
						try
						{
							hasBeenSelected = tab.toggleSBNFederatedSendableSelection(id,Long.parseLong(posInResultSet));
						} catch (Exception exception)
						{
							return;
						}
				
					
					int howManySendableItems  = tab.howManySelectedForSBNSend();						

					int howManyExportableItems = 0;
					if (canBeExported)
					{
							try 
							{
								hasBeenSelected = tab.toggleSBNFederatedSelection(id,Long.parseLong(posInResultSet));
							} catch (Exception exception)
							{
								return;
							}
					
						howManyExportableItems = tab.howManySelectedForSBNExport();
					}
					
			
					response.getWriter().print(
							new StringBuilder("{\"result\": {")
								.append("\"selected\":\"").append(hasBeenSelected).append("\",")
								.append("\"export2\":").append(howManyExportableItems).append(",")
								.append("\"send\":").append(howManySendableItems).append("}}").toString());
					return;
				}
				response.getWriter().print(true);			
			}
		});
	}
	
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		HttpServlet action = actions.get(request.getParameter(HttpParameter.ACTION));
		if (action != null)
		{
			action.service(request, response);
		}
	}
}

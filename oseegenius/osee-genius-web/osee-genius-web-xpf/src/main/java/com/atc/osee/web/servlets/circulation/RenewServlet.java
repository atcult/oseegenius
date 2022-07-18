package com.atc.osee.web.servlets.circulation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.servlets.OseeGeniusServlet;

public class RenewServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 3241519278141007241L;
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	protected void doGet(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		if (getVisit(request).isAuthenticated() && getLicense(request).isCirculationPluginEnabled())
		{
			String copyId = request.getParameter(HttpParameter.COPY_ID);
			String itemId = request.getParameter(HttpParameter.URI);
			
			try 
			{
				if (isNotNullOrEmptyString(copyId) && isNotNullOrEmptyString(itemId))
				{
					QueryResponse resourceResponse = getSearchEngine(request).findDocumentByURI(itemId);
					if (resourceResponse != null && !resourceResponse.getResults().isEmpty())
					{
						NamedList holdingData = ((NamedList)(NamedList) resourceResponse.getResponse().get(ISolrConstants.HOLDINGS_COMPONENT_NAME));
						if (holdingData != null)
						{
							NamedList copies = (NamedList) holdingData.get(itemId);
							if (copies != null && copies.size() != 0)
							{
								NamedList copyData = (NamedList) copies.get(copyId);
								if (copyData != null && copyData.size() != 0)
								{
									String barcode = (String) copyData.get("barcode");
									Integer branchId = (Integer) copyData.get("sub-institution-id");
									if (barcode != null && branchId != null)
									{
										try 
										{
											int returnCode = getLicense(request).getCirculationPlugin().renew(barcode, branchId);
											addSessionRenewResult(request, copyId, returnCode);
											goBack(request, response, itemId);
											return;
										} catch (RenewalNotAllowedException exception)
										{
											addSessionRenewResult(request, copyId, 987653);
											goBack(request, response, itemId);
											return;
										} catch (SystemInternalFailureException exception)
										{
											addSessionRenewResult(request, copyId, 987654);
											goBack(request, response, itemId);
											return;
										}
									} else
									{
										goBack(request, response, itemId);
										return;
									}
								} else 
								{
									response.sendError(HttpServletResponse.SC_NOT_FOUND);					
									return;
								}
							}
						}						
					} else
					{
						response.sendError(HttpServletResponse.SC_NOT_FOUND);					
						return;
					}
				} else
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
			} catch (SystemInternalFailureException exception)
			{
				
			}
		} else 
		{
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addSessionRenewResult(final HttpServletRequest request, String copyId, Integer returnCode)
	{
		HttpSession session = request.getSession();
		Map<String, Integer> sessionRenews = (Map<String, Integer>) session.getAttribute("renews");
		if (sessionRenews == null)
		{
			sessionRenews = new HashMap<String, Integer>();
			session.setAttribute("renews", sessionRenews);
		}
		sessionRenews.put(copyId, returnCode);
	}
	
	private void goBack(final HttpServletRequest request, final HttpServletResponse response, final String uri) throws IOException
	{
		if (isNotNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT)))
		{
			response.sendRedirect("workspace?view=myloans");											
		} else
		{
			response.sendRedirect("resource?uri=" + uri);
		}	
	}
}

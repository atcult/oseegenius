package com.atc.osee.web.servlets.desiderata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.FederatedItemSelection;
import com.atc.osee.web.model.ItemSelection;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.util.Utils;

/**
 * Desiderata Servlet is the main front controller of the desiderata module.
 * First version of this module is very simple...just a form (eventually
 * pre-populated with a selection) that the user can send to library for some
 * reason.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class DesiderataServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 703694171887518212L;

	@Override
	protected void doGet(
			final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException, IOException 
	{
		if (getLicense(request).isDesiderataEnabled()) 
		{
			try 
			{
				Visit visit = getVisit(request);
				if (visit.isAnonymous())
				{
					Account account = getLicense(request).getAccountPlugin().getAccount(request.getRemoteUser());
					visit.injectAccount(account);					
				}
				
				String documentId = request.getParameter(HttpParameter.ID);
				if (isNotNullOrEmptyString(documentId))
				{
					QueryResponse qresponse = getSearchEngine(request).findDocumentByURI(documentId);
					setSessionAttribute(request, "selection", qresponse.getResults());
				} else
				{
				
					ItemSelection selection = isNullOrEmptyString(request.getParameter(HttpParameter.SELECTION_CONTEXT)) 
							? getSearchExperience(request).getCurrentTab()
							: getWorkspaceSelection(request);
		
					if (selection.howManySelectedForExport() != 0) 
					{
						if (selection instanceof SearchTab && ((SearchTab) (selection)).isExternalSearchTab()) 
						{
							SearchTab selectedTab = (SearchTab) selection;
							Map<String, FederatedItemSelection> selectedFederatedRecords = selectedTab.getSelectedFederatedItemsForExportOrDownload();
		
							List<SolrDocument> documents = new ArrayList<SolrDocument>(selectedFederatedRecords.size());
							for (Entry<String, FederatedItemSelection> entry : selectedFederatedRecords.entrySet()) 
							{
								try 
								{
									FederatedItemSelection item = entry.getValue();
									
									SolrDocument document = Utils.createSolrDocument(selectedTab.getPazpar2().record(item.recid, null), item.recid, item.id);
									if (document != null) {
										document.setField(ISolrConstants.MARC_21_FIELD_NAME, selectedTab.getPazpar2().record(item.recid, item.offset));
										documents.add(document);
									}
									
								} catch (Exception exception) 
								{
									// Nothing to be done here...
								}
							}
						
							if (!documents.isEmpty()) {
								setSessionAttribute(request, "selection", documents);
							}
						} 
					}
				}
				forwardTo(request, response, "components/desiderata/form.vm", "/login_layout.vm");
			} catch (Exception exception)
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		} else 
		{
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		final Visit visit = getVisit(request);
		if (getLicense(request).isDesiderataEnabled() && visit.isAuthenticated()) 
		{
			final Account account = visit.getAccount();
			final String mode = request.getParameter("mode");
			
			final String libraryId = request.getParameter("libraryId");
			if (isNullOrEmptyString(libraryId))
			{
				setRequestAttribute(request, "errorMessage", "missing_required_library_id");
				forwardTo(request, response, "components/desiderata/form.vm", "/login_layout.vm");
				return;
			}
			
			if ("manual".equals(mode)) {
				
				// Sanity check: title is required
				final String title = request.getParameter("title");
				if (isNullOrEmptyString(title))
				{
					setRequestAttribute(request, "errorMessage", "missing_required_title");
					forwardTo(request, response, "components/desiderata/form.vm", "/login_layout.vm");
					return;
				}
				// Inserimento manuale di un suggerimento 
				
				final String author = request.getParameter("author");
				final String publicationYear = request.getParameter("publication_year");
				final String series = request.getParameter("series");
				final String volume = request.getParameter("volume");
				final String publisher= request.getParameter("publisher");
				final String publicationPlace = request.getParameter("publication_place");
				final String isbn = request.getParameter("isbn");
				final String blevel = request.getParameter("bibliographic_level");	
				final String notes = request.getParameter("notes");	
				
				try {
					PostDesiderata.postManualSuggestion(
							getConfiguration(request).getDesiderataAddress(), 
							account.getId(), 
							notes,
							libraryId, title, author, publicationYear, series, volume, publisher, publicationPlace, isbn, blevel);
					forwardTo(request, response, "components/desiderata/form_ok.vm", "/login_layout.vm");
					return;
				} catch (Exception exception) {
					exception.printStackTrace();
					setRequestAttribute(request, "errorMessage", "msg_500");
					forwardTo(request, response, "components/desiderata/form.vm", "/login_layout.vm");
					return;
				}
			} else {
				try {
					List<SolrDocument> selectedItems = (List<SolrDocument>) request.getSession().getAttribute("selection");
					
					if (selectedItems != null && !selectedItems.isEmpty()) {
						PostDesiderata.postSelectedSuggestions(
								getConfiguration(request).getDesiderataAddress(), 
								account.getId(), 
								libraryId, 
								selectedItems);
					}

					forwardTo(request, response, "components/desiderata/form_ok.vm", "/login_layout.vm");
					request.getSession().removeAttribute("selection");
					return;
				} catch (Exception exception) {
					exception.printStackTrace();
					setRequestAttribute(request, "errorMessage", "msg_500");
					forwardTo(request, response, "components/desiderata/form.vm", "/login_layout.vm");
					return;
				}						
			}
		}
	}
}
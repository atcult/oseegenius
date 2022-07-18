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
package com.atc.osee.web.servlets.account;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.Layout;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.Fine;
import com.atc.osee.web.model.Hold;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.model.Loan;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.model.community.Review;
import com.atc.osee.web.model.community.WorkspaceSelection;
import com.atc.osee.web.plugin.AccountPlugin;
import com.atc.osee.web.plugin.CirculationPlugin;
import com.atc.osee.web.plugin.CommunityPlugin;
import com.atc.osee.web.plugin.NoSuchAccountException;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.LicenseTool;

/**
 * My Workspace web controller.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class WorkspaceServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Abstract interface for a workspace command.
	 * 
	 * @author agazzarini
	 * @since 1.0
	 */
	private interface WorkspaceSectionHandler {
		/**
		 * Executes a given command.
		 * 
		 * @param request
		 *            the HTTP request.
		 * @param response
		 *            the HTTP response.
		 * @param license
		 *            the OseeGenius -W- license.
		 * @throws ServletException
		 *             in case of servlet container failure.
		 * @throws IOException
		 *             in case of I/O failure.
		 */
		void service(HttpServletRequest request, HttpServletResponse response,
				LicenseTool license) throws ServletException, IOException;
	}

	private final WorkspaceSectionHandler getWishListDocuments = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isCommunityPluginEnabled()) {
				String wishListIdParameter = request
						.getParameter(HttpParameter.ID);

				if (isNullOrEmptyString(wishListIdParameter)) {
					wishListIdParameter = (String) request
							.getSession()
							.getAttribute(
									HttpAttribute.CURRENTLY_SELECTED_WISHLIST_ID);
				}

				if (isNotNullOrEmptyString(wishListIdParameter)) {
					try {
						long wishlistId = Long.parseLong(wishListIdParameter);

						request.getSession().setAttribute(
								HttpAttribute.CURRENTLY_SELECTED_WISHLIST_ID,
								wishListIdParameter);

						List<String> documentIds = license.getCommunityPlugin()
								.getWishListDocuments(wishlistId);

						StringBuilder builder = new StringBuilder(
								HttpParameter.ID).append(":").append(
								IConstants.OPEN_PARENTHESIS);
						for (String id : documentIds) {
							builder.append(IConstants.BLANK).append(id);
						}
						builder.append(IConstants.CLOSE_PARENTHESIS);
						SolrQuery query = new SolrQuery(builder.toString());
						query.setQueryType(ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME);
						query.setRows(documentIds.size());

						QueryResponse queryresponse = getSearchEngine(request)
								.executeQuery(query);
						setRequestAttribute(request, "documents",
								queryresponse.getResults());
						setRequestAttribute(request, "objectId", wishlistId);
					} catch (Exception exception) {
						// Nothing to be done here...
					}
				}
				myWishList.service(request, response, license);
			}
		}
	};

	private final WorkspaceSectionHandler removeCurrentSelectionFromWishList = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isCommunityPluginEnabled()) {
				String wishListIdParameter = request
						.getParameter(HttpParameter.ID);
				try {
					long wishListId = Long.parseLong(wishListIdParameter);
					WorkspaceSelection selection = getWorkspaceSelection(request);
					license.getCommunityPlugin().removeFromWishList(wishListId,
							selection.getSelectedItemsForEmail());
					selection.clearSelection();
				} catch (Exception exception) {
					// Nothing to be done here...
				}
				getWishListDocuments.service(request, response, license);
			}
		}
	};

	private final WorkspaceSectionHandler removeCurrentSelectionFromTag = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isCommunityPluginEnabled()) {
				try {
					Account account = getVisit(request).getAccount();
					long tagId = Long.parseLong(request
							.getParameter(HttpParameter.ID));

					WorkspaceSelection selection = getWorkspaceSelection(request);
					license.getCommunityPlugin().removeFromTag(tagId,
							selection.getSelectedItemsForEmail(),
							account.getId());
					selection.clearSelection();
				} catch (Exception exception) {
					// Nothing to be done here...
				}
				getTaggedDocuments.service(request, response, license);
			}
		}
	};

	private final WorkspaceSectionHandler getTaggedDocuments = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isCommunityPluginEnabled()) {
				Account account = getVisit(request).getAccount();
				String tagIdParameter = request.getParameter(HttpParameter.ID);
				if (isNullOrEmptyString(tagIdParameter)) {
					tagIdParameter = (String) request.getSession()
							.getAttribute(
									HttpAttribute.CURRENTLY_SELECTED_TAG_ID);
				}

				if (isNotNullOrEmptyString(tagIdParameter)) {
					try {
						long tagId = Long.parseLong(tagIdParameter);
						request.getSession().setAttribute(
								HttpAttribute.CURRENTLY_SELECTED_TAG_ID,
								tagIdParameter);

						List<String> documentIds = license.getCommunityPlugin()
								.getTaggedDocuments(account.getId(), tagId);

						StringBuilder builder = new StringBuilder(
								HttpParameter.ID).append(":").append(
								IConstants.OPEN_PARENTHESIS);
						for (String id : documentIds) {
							builder.append(IConstants.BLANK).append(id);
						}
						builder.append(IConstants.CLOSE_PARENTHESIS);
						SolrQuery query = new SolrQuery(builder.toString());
						query.setQueryType(ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME);
						query.setRows(documentIds.size());

						QueryResponse queryresponse = getSearchEngine(request)
								.executeQuery(query);
						setRequestAttribute(request, "taggedDocuments",
								queryresponse.getResults());
						setRequestAttribute(request, "objectId", tagIdParameter);
					} catch (Exception exception) {
						// Nothing to be done here...
					}
				}
				myTags.service(request, response, license);
			}
		}
	};

	private final WorkspaceSectionHandler removeTag = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isCommunityPluginEnabled()) {
				Account account = getVisit(request).getAccount();
				String tagIdParameter = request
						.getParameter(ISolrConstants.ID_FIELD_NAME);
				try {
					long tagId = Long.parseLong(tagIdParameter);
					license.getCommunityPlugin().removeTag(account.getId(),
							tagId);
				} catch (Exception exception) {
					// Nothing to be done here...
				}
				myTags.service(request, response, license);
			}
		}
	};

	private final WorkspaceSectionHandler removeSelectionFromBibliography = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isCommunityPluginEnabled()) {
				try {
					Account account = getVisit(request).getAccount();
					WorkspaceSelection selection = getWorkspaceSelection(request);
					license.getCommunityPlugin().removeFromBibliography(
							account.getId(),
							selection.getSelectedItemsForEmail());
					selection.clearSelection();
				} catch (SystemInternalFailureException exception) {
					// Nothing to be done here...
				}
				myBibliography.service(request, response, license);
			}
		}
	};

	private final WorkspaceSectionHandler myReviews = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isAccountPluginEnabled()) {
				try {
					Account account = getVisit(request).getAccount();
					if (license.isCommunityPluginEnabled()) {
						setSessionAttribute(request, "section", "myreviews");
						Map<String, List<Review>> reviews = license
								.getCommunityPlugin().getReviews(
										account.getId());
						if (reviews != null && !reviews.isEmpty()) {
							setRequestAttribute(
									request,
									"reviews",
									license.getCommunityPlugin().getReviews(
											account.getId()));

							StringBuilder builder = new StringBuilder(
									ISolrConstants.ID_FIELD_NAME + ":(");
							for (String id : reviews.keySet()) {
								builder.append(IConstants.BLANK).append(id);
							}
							builder.append(IConstants.CLOSE_PARENTHESIS);
							SolrQuery query = new SolrQuery(builder.toString());
							query.setQueryType(ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME);
							query.setRows(reviews.size());

							QueryResponse queryresponse = getSearchEngine(
									request).executeQuery(query);
							setRequestAttribute(request, "documents",
									queryresponse.getResults());
						}
						forwardTo(request, response, "/protected/myreviews.vm",
								Layout.WORKSPACE);
					} else {
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
					}
				} catch (Exception exception) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};

	private final WorkspaceSectionHandler myTags = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isAccountPluginEnabled()) {
				setSessionAttribute(request, "section", "mytags");
				try {
					Account account = getVisit(request).getAccount();
					if (license.isCommunityPluginEnabled()) {
						setRequestAttribute(request, "tags", license
								.getCommunityPlugin().getTags(account.getId()));
					}
					forwardTo(request, response, "/protected/mytags.vm",
							Layout.WORKSPACE);
				} catch (SystemInternalFailureException exception) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};

	private final WorkspaceSectionHandler myWishList = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isAccountPluginEnabled()) {
				setSessionAttribute(request, "section", "mywishlists");
				try {
					Account account = getVisit(request).getAccount();
					if (license.isCommunityPluginEnabled()) {
						setRequestAttribute(
								request,
								"wishlists",
								license.getCommunityPlugin().getWishLists(
										account.getId()));
					}
					forwardTo(request, response, "/protected/mywishlists.vm",
							Layout.WORKSPACE);
				} catch (SystemInternalFailureException exception) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};

	private final WorkspaceSectionHandler myBibliography = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isAccountPluginEnabled()) {
				try {
					Account account = getVisit(request).getAccount();
					if (license.isCommunityPluginEnabled()) {
						setSessionAttribute(request, "section",
								"mybibliography");
						List<String> bibliography = license
								.getCommunityPlugin().getBibliography(
										account.getId());
						if (bibliography != null && !bibliography.isEmpty()) {
							StringBuilder builder = new StringBuilder("id:(");
							for (String id : bibliography) {
								builder.append(" ").append(id);
							}
							builder.append(")");
							SolrQuery query = new SolrQuery(builder.toString());
							query.setQueryType(ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME);
							query.setRows(bibliography.size());

							QueryResponse queryresponse = getSearchEngine(
									request).executeQuery(query);
							setRequestAttribute(request, "bibliography",
									queryresponse.getResults());
						}
						forwardTo(request, response,
								"/protected/mybibliography.vm",
								Layout.WORKSPACE);
					} else {
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
					}
				} catch (Exception exception) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};

	private final WorkspaceSectionHandler myAccount = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isAccountPluginEnabled()) {
				setSessionAttribute(request,
						HttpAttribute.ACTIVE_WORKSPACE_SECTION, "myaccount");
				AccountPlugin plugin = license.getAccountPlugin();
				try {
					Account account = plugin
							.getAccount(request.getRemoteUser());
					getVisit(request).injectAccount(account);

					if (license.isCirculationPluginEnabled()) {
						CirculationPlugin circulationPlugin = license
								.getCirculationPlugin();
						request.setAttribute("loansCount",
								circulationPlugin.countLoans(account.getId()));
						request.setAttribute("holdsCount",
								circulationPlugin.countHolds(account.getId()));

						if (isLibraryUsingFinesByMoney(request,
								circulationPlugin)) {
							request.setAttribute("finesCount",
									circulationPlugin.countFines(account
											.getId()));
							// bug 1648
							Map<Library, List<Fine>> finesGroupedByLibraries = circulationPlugin
									.findFines(account.getId());
							setRequestAttribute(request, "finesByLibraries",
									finesGroupedByLibraries);
							//
						} else {
							// bug 1648
							Map<Library, Date> finesGroupedByLibraries = circulationPlugin
									.getBlackListStatusByLibrary(account
											.getId());
							setRequestAttribute(request, "finesByLibraries",
									finesGroupedByLibraries);
							//
							request.setAttribute("finesCount",
									circulationPlugin
											.countBlackListEntries(account
													.getId()));
						}
					}

					if (license.isCommunityPluginEnabled()) {
						CommunityPlugin communityPlugin = license
								.getCommunityPlugin();
						request.setAttribute("howManyTags",
								communityPlugin.countTags(account.getId()));
						request.setAttribute("howManyReviews",
								communityPlugin.countReviews(account.getId()));
						request.setAttribute("bibliographySize",
								communityPlugin.getBibliographySize((account
										.getId())));
					}
					forwardTo(request, response, "/protected/myaccount.vm",
							Layout.WORKSPACE);
				} catch (SystemInternalFailureException exception) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (NoSuchAccountException exception) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
				}
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};

	private final WorkspaceSectionHandler mySubscriptions = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			if (license.isAccountPluginEnabled()) {
				setSessionAttribute(request,
						HttpAttribute.ACTIVE_WORKSPACE_SECTION,
						"mysubscriptions");
				forwardTo(request, response, "/protected/mysubscriptions.vm",
						Layout.WORKSPACE);
				return;
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};

	private final WorkspaceSectionHandler myLoans = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			Visit visit = getVisit(request);
			if (license.isCirculationPluginEnabled() && visit.isAuthenticated()) {
				try {
					setSessionAttribute(request,
							HttpAttribute.ACTIVE_WORKSPACE_SECTION, "myloans");
					Map<Library, List<Loan>> loansGroupedByLibraries = license
							.getCirculationPlugin().findLoans(
									visit.getAccount().getId());
					setRequestAttribute(request, "loansByLibraries",
							loansGroupedByLibraries);
					forwardTo(request, response, "/protected/myloans.vm",
							Layout.WORKSPACE);
				} catch (SystemInternalFailureException exception) {
					setErrorFlag(request);
				}
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};
	
	private final WorkspaceSectionHandler myLoanHistory = new WorkspaceSectionHandler() 
	{	
		@Override
		public void service(
				final HttpServletRequest request, 
				final HttpServletResponse response, 
				final LicenseTool license) throws ServletException, IOException 
		{
			Visit visit = getVisit(request);
			if (visit.isAuthenticated())
			{
				try 
				{
					setSessionAttribute(request, HttpAttribute.ACTIVE_WORKSPACE_SECTION, "loanhistory");
					String pageNumberAsString = request.getParameter(HttpParameter.PAGE_INDEX);
					String pageSizeAsString = request.getParameter(HttpParameter.PAGE_SIZE);
					int page = 0;
					int pageSize = 5;
					try 
					{
						page = Integer.parseInt(pageNumberAsString);
					} catch (Exception ignore) { 
						// Nothing 
					}

					try 
					{
						pageSize = Integer.parseInt(pageSizeAsString);
					} catch (Exception ignore) { 
						// Nothing 
					}
					
					request.setAttribute("pageSize", pageSize);
					request.setAttribute("page", page);
					request.setAttribute("loansCount", license.getCirculationPlugin().countLoanHistory(visit.getAccount().getId()));
					setRequestAttribute(request, "loans", license.getCirculationPlugin().getLoanHistory(visit.getAccount().getId(), page, pageSize));
					forwardTo(request, response, "/protected/myloanhistory.vm", Layout.WORKSPACE);
				} catch (SystemInternalFailureException exception) 
				{
					//Log.error("Error while counting loans.", exception);
					setErrorFlag(request);
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} else
			{
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};	

	private final WorkspaceSectionHandler myHolds = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			Visit visit = getVisit(request);
			if (license.isCirculationPluginEnabled() && visit.isAuthenticated()) {
				try {
					setSessionAttribute(request,
							HttpAttribute.ACTIVE_WORKSPACE_SECTION, "myholds");
					Map<Library, List<Hold>> loansGroupedByLibraries = license
							.getCirculationPlugin().findHolds(
									visit.getAccount().getId());
					setRequestAttribute(request, "holdsByLibraries",
							loansGroupedByLibraries);
					forwardTo(request, response, "/protected/myholds.vm",
							Layout.WORKSPACE);
				} catch (SystemInternalFailureException exception) {
					setErrorFlag(request);
				}
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};

	private final WorkspaceSectionHandler myFines = new WorkspaceSectionHandler() {
		@Override
		public void service(final HttpServletRequest request,
				final HttpServletResponse response, final LicenseTool license)
				throws ServletException, IOException {
			Visit visit = getVisit(request);
			if (license.isCirculationPluginEnabled() && visit.isAuthenticated()) {
				try {
					setSessionAttribute(request,
							HttpAttribute.ACTIVE_WORKSPACE_SECTION, "myfines");
					CirculationPlugin circulationPlugin = license
							.getCirculationPlugin();
					if (isLibraryUsingFinesByMoney(request, circulationPlugin)) {
						Map<Library, List<Fine>> finesGroupedByLibraries = circulationPlugin
								.findFines(visit.getAccount().getId());
						setRequestAttribute(request, "finesByLibraries",
								finesGroupedByLibraries);
					} else {
						Map<Library, Date> finesGroupedByLibraries = circulationPlugin
								.getBlackListStatusByLibrary(visit.getAccount()
										.getId());
						setRequestAttribute(request, "finesByLibraries",
								finesGroupedByLibraries);
					}
					forwardTo(request, response, "/protected/myfines.vm",
							Layout.WORKSPACE);
				} catch (SystemInternalFailureException exception) {
					setErrorFlag(request);
				}
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	};

	private Map<String, WorkspaceSectionHandler> viewHandler = new HashMap<String, WorkspaceSectionHandler>();
	{
		viewHandler.put("myaccount", myAccount);
		viewHandler.put("mysubscriptions", mySubscriptions);
		viewHandler.put("mytags", getTaggedDocuments);
		viewHandler.put("myreviews", myReviews);
		viewHandler.put("mybibliography", myBibliography);
		viewHandler.put("myloans", myLoans);
		viewHandler.put("myholds", myHolds);
		viewHandler.put("loanhistory", myLoanHistory);
		
		viewHandler.put("myfines", myFines);
		viewHandler.put("mywishlists", getWishListDocuments);
	}

	private Map<String, WorkspaceSectionHandler> actions = new HashMap<String, WorkspaceSectionHandler>();
	{
		actions.put("remove_from_bibliography", removeSelectionFromBibliography);
		actions.put("get_tagged_documents", getTaggedDocuments);
		actions.put("remove_tag", removeTag);

		actions.put("get_wish_list_documents", getWishListDocuments);
		actions.put("remove_from_wish_list", removeCurrentSelectionFromWishList);
		actions.put("remove_from_tag", removeCurrentSelectionFromTag);
	}

	/**
	 * Loads the plain account information.
	 * 
	 * @param request
	 *            the http request.
	 * @param response
	 *            the http response.
	 * @throws ServletException
	 *             in case of web container failure.
	 * @throws IOException
	 *             in case of I/O failure.
	 */
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "home");
		setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "search");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "suggest");
		setRequestAttribute(request,
				HttpAttribute.DONT_SHOW_PERSPECTIVE_BUTTON, true);
		LicenseTool license = getLicense(request);
		if (license.isAccountPluginEnabled()) {
			String requestedAction = request.getParameter("action");
			if (isNotNullOrEmptyString(requestedAction)) {
				WorkspaceSectionHandler action = getAction(requestedAction);
				if (action != null) {
					action.service(request, response, license);
					return;
				}
			}
			String requestedSection = request.getParameter("view");
			WorkspaceSectionHandler handler = getHandler(requestedSection,
					request);
			handler.service(request, response, license);
		} else {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}

	@Override
	protected void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		String requestedAction = request.getParameter("action");
		if (isNotNullOrEmptyString(requestedAction)) {
			LicenseTool license = getLicense(request);
			WorkspaceSectionHandler action = getAction(requestedAction);
			if (action != null) {
				action.service(request, response, license);
				return;
			}
		} else {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}

	}

	@Override
	protected void doDelete(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		super.doDelete(request, response);
	}

	/**
	 * Returns the handler associated with the given workspace section name.
	 * 
	 * @param sectionName
	 *            the name of the workspace section.
	 * @param request
	 *            the HTTP request.
	 * @return the handler associated with the given workspace section name.
	 */
	private WorkspaceSectionHandler getHandler(final String sectionName,
			final HttpServletRequest request) {
		WorkspaceSectionHandler handler = viewHandler.get(sectionName);
		if (handler == null) {
			handler = viewHandler.get(request.getSession(true).getAttribute(
					HttpParameter.SECTION));
		}
		return handler != null ? handler : myAccount;
	}

	/**
	 * Returns the workspace action associated with the given name.
	 * 
	 * @param actionName
	 *            the action name.
	 * @return the workspace action associated with the given name.
	 */
	private WorkspaceSectionHandler getAction(final String actionName) {
		return actions.get(actionName);
	}

	/**
	 * Returns true if the library is using fines by money.
	 * 
	 * @param request
	 *            the HTTP request.
	 * @param plugin
	 *            the {@link CirculationPlugin}.
	 * @return true if the library is using fines by money.
	 * @throws SystemInternalFailureException
	 *             in case of system failure.
	 */
	private boolean isLibraryUsingFinesByMoney(
			final HttpServletRequest request,
			final CirculationPlugin circulationPlugin)
			throws SystemInternalFailureException {
		Boolean isLibraryUsingFinesByMoney = (Boolean) request.getSession()
				.getAttribute(HttpAttribute.MONEY_FINES_IN_USE);
		if (isLibraryUsingFinesByMoney == null) {
			isLibraryUsingFinesByMoney = circulationPlugin
					.isUsingFinesByMoney();
			setSessionAttribute(request, HttpAttribute.MONEY_FINES_IN_USE,
					isLibraryUsingFinesByMoney);
		}
		return isLibraryUsingFinesByMoney;
	}
}
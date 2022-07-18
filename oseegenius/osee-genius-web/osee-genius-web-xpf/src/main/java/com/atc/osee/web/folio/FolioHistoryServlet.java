package com.atc.osee.web.folio;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.util.Log;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.FolioConfigurationTool;

/**
 * Login to FOLIO system
 * 
 * @author Alice Guercio
 * @since 1.2
 */

public class FolioHistoryServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 124685457463L;
	private static final String DEFAULT_LIMIT = "10";

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		if (loggedUser == null) {
			response.sendRedirect("signIn");
			return;
		} else {
			try {
				final String accessToken = folioAPI.loginFolioAdministrator();

				final String openRequestOffset = isNotNullOrEmptyString(request.getParameter("openRequestOffset"))
						? request.getParameter("openRequestOffset")
						: null;
				final String openRequestLimit = isNotNullOrEmptyString(request.getParameter("openRequestLimit"))
						? request.getParameter("openRequestLimit")
						: DEFAULT_LIMIT;
				FolioResponseModel openRequestResponse = folioAPI.getUserOpenRequestResponse(accessToken,
						loggedUser.getId(), openRequestLimit, openRequestOffset);
				final List<FolioLoan> openRequests = folioAPI.getRequestList(openRequestResponse);
				request.setAttribute("openRequestNumFound", folioAPI.getNumFound(openRequestResponse));
				request.setAttribute("openRequests", openRequests);

				final String closeRequestOffset = isNotNullOrEmptyString(request.getParameter("closeRequestOffset"))
						? request.getParameter("closeRequestOffset")
						: null;
				final String closeRequestLimit = isNotNullOrEmptyString(request.getParameter("closeRequestLimit"))
						? request.getParameter("closeRequestLimit")
						: DEFAULT_LIMIT;
				FolioResponseModel closeRequestResponse = folioAPI.getUserCloseRequestResponse(accessToken,
						loggedUser.getId(), closeRequestLimit, closeRequestOffset);
				final List<FolioLoan> closeRequests = folioAPI.getRequestList(closeRequestResponse);
				request.setAttribute("closeRequestNumFound", folioAPI.getNumFound(closeRequestResponse));
				request.setAttribute("closeRequests", closeRequests);

				final String loanOffset = isNotNullOrEmptyString(request.getParameter("loanOffset"))
						? request.getParameter("loanOffset")
						: null;
				final String loanLimit = isNotNullOrEmptyString(request.getParameter("loanLimit"))
						? request.getParameter("loanLimit")
						: DEFAULT_LIMIT;
				FolioResponseModel loanResponse = folioAPI.userLoanHistory(accessToken, loggedUser.getId(), loanLimit,
						loanOffset);
				final List<FolioLoan> loans = folioAPI.getLoanList(loanResponse);
				for (FolioLoan currentLoan : loans) {
					currentLoan.setRenewable(isLoanRenewable(currentLoan, loggedUser, accessToken));
				}
				request.setAttribute("loanNumFound", folioAPI.getNumFound(loanResponse));
				request.setAttribute("loans", loans);

				final String openLoanOffset = isNotNullOrEmptyString(request.getParameter("openLoanOffset"))
						? request.getParameter("openLoanOffset")
						: null;
				final String openLoanLimit = isNotNullOrEmptyString(request.getParameter("openLoanLimit"))
						? request.getParameter("openLoanLimit")
						: DEFAULT_LIMIT;
				FolioResponseModel openLoanResponse = folioAPI.getUserOpenLoansResponse(accessToken, loggedUser.getId(),
						openLoanLimit, openLoanOffset);
				final List<FolioLoan> openLoans = folioAPI.getLoanList(openLoanResponse);
				for (FolioLoan currentLoan : openLoans) {
					currentLoan.setRenewable(isLoanRenewable(currentLoan, loggedUser, accessToken));
				}
				request.setAttribute("openLoanNumFound", folioAPI.getNumFound(openLoanResponse));
				request.setAttribute("openLoans", openLoans);

				final String closeLoanOffset = isNotNullOrEmptyString(request.getParameter("closeLoanOffset"))
						? request.getParameter("closeLoanOffset")
						: null;
				final String closeLoanLimit = isNotNullOrEmptyString(request.getParameter("closeLoanLimit"))
						? request.getParameter("closeLoanLimit")
						: DEFAULT_LIMIT;
				FolioResponseModel closeLoanResponse = folioAPI.getUseCloseLoansResponse(accessToken,
						loggedUser.getId(), closeLoanLimit, closeLoanOffset);
				final List<FolioLoan> closeLoans = folioAPI.getLoanList(closeLoanResponse);
				for (FolioLoan currentLoan : closeLoans) {
					currentLoan.setRenewable(isLoanRenewable(currentLoan, loggedUser, accessToken));
				}
				request.setAttribute("closeLoanNumFound", folioAPI.getNumFound(closeLoanResponse));
				request.setAttribute("closeLoans", closeLoans);

				request.setAttribute("defaultLimit", DEFAULT_LIMIT);
			} catch (FolioException e) {
				request.setAttribute("inError", true);
				request.setAttribute("errorMessage", "something go wrong");
				forwardTo(request, response, "/components/userPanel/folio_history.vm", "workspace_layout.vm");
			}
		}

		forwardTo(request, response, "/components/userPanel/folio_history.vm", "workspace_layout.vm");
	}

	protected boolean isLoanRenewable(FolioLoan loan, FolioUserModel user, String accessToken) {
		return user.isActive() && !user.isPreregistered() && loan.isOpen() && !isHandwrite(loan, accessToken)
				&& !hasRequests(loan, accessToken) && !loan.isExpired() && loan.twoDaysPassed();
	}

	protected boolean hasRequests(FolioLoan loan, String accessToken) {
		try {
			int pendentRequests = folioAPI.howManyRequestOpenByItemId(accessToken, loan.getItemId());
			return pendentRequests > 0;
		} catch (FolioException e) {
			Log.error(e);
			return true;
		}
	}

	protected boolean isHandwrite(FolioLoan loan, String accessToken) {
		try {
			FolioItemModel item = folioAPI.getCopyById(loan.getItemId(), accessToken);
			final FolioConfigurationTool folioConfig = new FolioConfigurationTool();
			return folioConfig.getString(FolioConstants.HANDWRITE_ID).equals(item.getMaterialType());
		} catch (FolioException e) {
			Log.error(e);
			return true;
		}
	}

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final String requestId = request.getParameter("requestId");

		FolioUserModel loggedUser = getVisit(request).getFolioAccount();
		try {
			final String accessToken = folioAPI.loginFolioAdministrator();
			boolean result = folioAPI.deleteRequest(accessToken, requestId, loggedUser.getId());
			if (result) {
				response.sendRedirect("folioHistory");
				return;
			} else {
				request.setAttribute("inError", true);
				request.setAttribute("errorMessage", "something go wrong");
				forwardTo(request, response, "/components/userPanel/folio_history.vm", "workspace_layout.vm");
			}

		} catch (FolioException e) {
			request.setAttribute("inError", true);
			request.setAttribute("errorMessage", folioAPI.getFolioErrorCode(e.getMessage()));
			forwardTo(request, response, "/components/userPanel/folio_history.vm", "workspace_layout.vm");
		}
	}
}

package com.atc.osee.web.filters;

import com.atc.osee.web.HttpParameter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A filter that shows a different details page if the record type is "ar" (Archivial).
 * Customer requested that the details page for that kind of records mustn't be the classic
 * detail page of OseeGenius but instead the sane page that user sees when browses archivials.
 * 
 * That is, the page with the document hierarchy on the left and the details on the right side.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ShowArchivialDetailsFilter implements Filter {
	@Override
	public void init(final FilterConfig filterConfig) {
		// Nothing to be done here...
	}

	@Override
	public void doFilter(
			final ServletRequest request,
			final ServletResponse response,
			final FilterChain chain) throws IOException, ServletException {
		String categoryCode = ((HttpServletRequest)request).getParameter(HttpParameter.CATEGORY_CODE);
		if ("ar".equals(categoryCode)) {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/children");
			dispatcher.forward(request, response);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// Nothing to be done here...
	}
}
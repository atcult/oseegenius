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
package com.atc.osee.web.servlets.thexplorer;

import javax.servlet.http.HttpServletRequest;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.IConstants;

/**
 * ThGenius AutoSuggestion web controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AutoSuggestionServlet extends com.atc.osee.web.servlets.AutoSuggestionServlet 
{
	private static final long serialVersionUID = 1L;

	@Override
	protected ISearchEngine getSearchEngine(final HttpServletRequest request)
	{
		return (ISearchEngine) request.getSession().getServletContext().getAttribute(IConstants.TH_SEARCH_ENGINE_ATTRIBUTE_NAME);
	}
}

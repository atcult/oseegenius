package com.atc.osee.web.servlet.authority;

import com.atc.osee.web.servlets.AutoSuggestionServlet;

public class AuthorityAutoSuggestionServlet extends AutoSuggestionServlet {

	private static final long serialVersionUID = -3018087807685951157L;
	
	@Override
	protected String getAutocompleteCore(){
		return "autocomplete_auth";
	}

}

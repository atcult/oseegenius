package com.atc.osee.genius.indexer.biblio.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atc.osee.genius.indexer.biblio.Decorator;

public class FixPunctuation extends Decorator {
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) {	
		if (values == null || values.isEmpty()) {
			return values;
		}
		
		final List<String> newResult = new ArrayList<String>();
		for (final String value : values) {
			newResult.add((String) decorate(fieldName, value));
		}
		
		return newResult;
	}

	@Override
	public Object decorate(final String fieldName, final String value) {
		String textReplaced = value.replaceAll(" ,", ",");
		StringBuilder builder = new StringBuilder();
		boolean addSpaceAfter = false;
		for ( int i = 0; i < textReplaced.length(); i ++ ){
			char currentChar = textReplaced.charAt(i);
			
			
			if(currentChar == ":".charAt(0)){
				if(i > 0 && " ".charAt(0) != textReplaced.charAt(i-1)){		
					if("_".charAt(0) == textReplaced.charAt(i-1)) {
						//do nothing, becouse :_: is the separator I use for clusters
					}
					else {
						builder.append(" ");
					}
				}				
				
				if(i < textReplaced.length() - 1 && " ".charAt(0) != textReplaced.charAt(i+1)){
					if("_".charAt(0) == textReplaced.charAt(i+1)) {
						//do nothing, becouse :_: is the separator I use for clusters
					}
					else {
						addSpaceAfter = true;
					}
				}				
			}
			
			if(currentChar == "(".charAt(0)){
				if(i > 0 && " ".charAt(0) != textReplaced.charAt(i-1)){
					builder.append(" ");
				}
			}
			
			
			if(currentChar == "<".charAt(0)){
				// if I have "<"  and not  "<<"
				if(i > 0 && " ".charAt(0) != textReplaced.charAt(i-1) && "<".charAt(0) != textReplaced.charAt(i-1)){
					builder.append(" ");
				}
			}
			
			
			if (currentChar == ",".charAt(0) || currentChar == ";".charAt(0)){
				if(i < textReplaced.length() - 1 && " ".charAt(0) != textReplaced.charAt(i+1)){
					addSpaceAfter = true;
				}					
				
			}
						
			
		
			builder.append(currentChar);
			if(addSpaceAfter){
				builder.append(" ");
				addSpaceAfter = false;
			}
		}
		return builder.toString();
	}
}

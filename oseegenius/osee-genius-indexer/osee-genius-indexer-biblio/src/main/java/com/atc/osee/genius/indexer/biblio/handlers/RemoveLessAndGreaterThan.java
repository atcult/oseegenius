package com.atc.osee.genius.indexer.biblio.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atc.osee.genius.indexer.biblio.Decorator;

public class RemoveLessAndGreaterThan extends Decorator {
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
		return value.replaceAll("<<", "").replaceAll(">>", "").replaceFirst("\\*", "").replaceAll("\u0088", "").replaceAll("\u0089", "").replaceAll("\u0098", "").replaceAll("\u009C", "");
	}
}

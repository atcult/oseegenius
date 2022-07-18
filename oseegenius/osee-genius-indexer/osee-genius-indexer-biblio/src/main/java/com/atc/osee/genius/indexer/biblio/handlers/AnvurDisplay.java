package com.atc.osee.genius.indexer.biblio.handlers;

import java.util.Iterator;
import java.util.List;

import org.marc4j.marc.Subfield;

public class AnvurDisplay extends AnvurFacet {
	public String createValue(final List<Subfield> values) {
		StringBuilder builder = new StringBuilder();
		final Iterator<Subfield> iterator = values.iterator();
		if (iterator.hasNext()) {
			builder.append(iterator.next().getData());
		}
		
		if (iterator.hasNext()) {
			builder.append(": ").append(iterator.next().getData());
		}
		
		if (iterator.hasNext()) {
			builder.append(", ").append(iterator.next().getData());
		}
		return builder.toString();
	}
}

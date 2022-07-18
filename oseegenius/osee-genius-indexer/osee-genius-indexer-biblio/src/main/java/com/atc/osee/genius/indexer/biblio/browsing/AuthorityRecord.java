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
package com.atc.osee.genius.indexer.biblio.browsing;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus.ICacheStrategy;

/**
 * OseeGenius -I- authority record domain object.
 * Provides a (SKOS like) normalized view of an authority record, regardless the origin
 * source (RDF, Lucene, Solr, ThGenius, database and so on)
 *  
 * @author agazzarini
 * @since 1.0
 */
public final class AuthorityRecord implements Serializable
{
	private static final long serialVersionUID = 3651984638975710664L;
	
	private Set<String> seenFromTerms;
	private Set<String> seeInsteadTerms;
	
	private final String note;
	
	private Set<String> seeAlsoTerms;
	private Set<String> seenAlsoFromTerms;

	private Set<String> equivalentTerms;
	
	/**
	 * Builds a new authority record with the given data.
	 * 
	 * @param preferredLabel the preferred label.
	 * @param seenFromTerms an array of alternate labels.
	 * @param note usually the first scope note of the underlying record, but we would like to remain generic. 
	 * @param seeAlsoTerms an array of related labels.
	 */
	public AuthorityRecord(
			final Set<String> seenFromTerms, 
			final String note, 
			final Set<String> seeAlsoTerms,
			final Set<String> seenAlsoFromTerms,
			final Set<String> equivalentTerms,
			final Set<String> seeInsteadTerms)
	{
		this.seenFromTerms = (seenFromTerms != null) ? seenFromTerms : IConstants.EMPTY_STRING_SET;
		this.note = note;
		this.seeAlsoTerms = (seeAlsoTerms != null) ? seeAlsoTerms : IConstants.EMPTY_STRING_SET;
		this.seenAlsoFromTerms = (seenAlsoFromTerms != null) ? seenAlsoFromTerms : IConstants.EMPTY_STRING_SET;
		this.equivalentTerms = (equivalentTerms != null) ? equivalentTerms : IConstants.EMPTY_STRING_SET;
		this.seeInsteadTerms = (seeInsteadTerms != null) ? seeInsteadTerms : IConstants.EMPTY_STRING_SET;
	}
	
	/**
	 * Builds a new authority record with the given data.
	 * 
	 * @param preferredLabel the preferred label.
	 * @param seenFromTerms an array of alternate labels.
	 * @param note usually the first scope note of the underlying record, but we would like to remain generic. 
	 * @param seeAlsoTerms an array of related labels.
	 */
	public AuthorityRecord()
	{
		note = null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SEE INSTEAD: " + seeInsteadTerms);		
		builder.append("SEEN FROM: " + seenFromTerms);
		builder.append("SEE ALSO: " + seeAlsoTerms);
		builder.append("SEEN ALSO FROM: " + seeAlsoTerms);
		builder.append("EQUIVALENT: " + equivalentTerms);
		builder.append("IS PREFERRED: " + isPreferredForm());
		return builder.toString();
	}
	
	/**
	 * Returns the alternate labels of this record.
	 * 
	 * @return the alternate labels of this record.
	 */
	public Set<String> getSeenFromTerms()
	{
		if (seenFromTerms == null)
		{
			seenFromTerms = new HashSet<String>();
		}
		return seenFromTerms;
	}

	/**
	 * Returns the note of this record.
	 * 
	 * @return the note of this record.
	 */
	public String getNote()
	{
		return note;
	}

	/**
	 * Returns the related labels of this record.
	 * 
	 * @return the related labels of this record.
	 */
	public Set<String> getSeeAlsoTerms()
	{
		if (seeAlsoTerms == null)
		{
			seeAlsoTerms = new HashSet<String>();
		}
		return seeAlsoTerms;
	}
	
	/**
	 * Returns the "seen also from" headings of this record.
	 * 
	 * @return the "seen also from" headings of this record.
	 */
	public Set<String> getSeenAlsoFromTerms() 
	{
		if (seenAlsoFromTerms == null)
		{
			seenAlsoFromTerms = new HashSet<String>();
		}
		return seenAlsoFromTerms;
	}

	/**
	 * Returns the equivalent headings of this record.
	 * 
	 * @return the equivalent headings of this record.
	 */
	public Set<String> getEquivalentTerms() 
	{
		if (equivalentTerms == null)
		{
			equivalentTerms = new HashSet<String>();
		}
		return equivalentTerms;
	}
	
	/**
	 * Returns the preferred headings of this record.
	 * 
	 * @return the preferred headings of this record.
	 */
	public Set<String> getSeeInsteadTerms() 
	{
		if (seeInsteadTerms == null)
		{
			seeInsteadTerms = new HashSet<String>();
		}
		return seeInsteadTerms;
	}	

	/**
	 * Adds a new preferred heading to this record.
	 * 
	 * @param heading the preferred heading.
	 */
	public void addSeeInstead(final String heading) 
	{
		getSeeInsteadTerms().add(heading);
	}
	
	/**
	 * Adds a new alternate heading to this record.
	 * 
	 * @param heading the alternate heading.
	 */
	public void addSeenFrom(final String heading) 
	{
		getSeenFromTerms().add(heading);
	}

	/**
	 * Adds a new related heading to this record.
	 * 
	 * @param heading the related heading.
	 */
	public void addSeeAlso(final String heading) 
	{
		getSeeAlsoTerms().add(heading);
	}

	/**
	 * Adds a new related heading to this record.
	 * 
	 * @param heading the alternate heading.
	 */
	public void addSeenAlsoFrom(final String heading) 
	{
		getSeenAlsoFromTerms().add(heading);
	}
	
	/**
	 * Adds a new equivalent heading to this record.
	 * 
	 * @param heading the equivalent heading.
	 */
	public void addEquivalent(final String heading) 
	{
		getEquivalentTerms().add(heading);
	}
	
	/**
	 * Returns true if this AO represents a preferred heading.
	 * 
	 * @return true if this AO represents a preferred heading.
	 */
	public boolean isPreferredForm()
	{
		return seeInsteadTerms != null && this != ICacheStrategy.UNCONTROLLED_AUTHORITY_RECORD && seeInsteadTerms.isEmpty();
	}
}
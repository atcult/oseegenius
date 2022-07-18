package com.atc.osee.genius.indexer.biblio.browsing;

/**
 * Interface definition for sort key (and inverted sort key) strategy.
 * Each concrete implementor must provide its own strategy for 
 * defining sort and inverted sort keys.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface ISortKeyStrategy 
{
	/**
	 * Creates a sort key using the given data.
	 * 
	 * @param heading the heading label.
	 * @param filter the heading filter.
	 * @return a sort key. 
	 */
	String sortKey(final String heading, final IHeadingFilter filter);
}
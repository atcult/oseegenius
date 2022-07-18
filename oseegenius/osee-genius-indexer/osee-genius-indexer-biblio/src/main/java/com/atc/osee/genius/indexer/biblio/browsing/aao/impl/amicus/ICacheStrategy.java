package com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.browsing.AuthorityRecord;

/**
 * Authority Record caching strategy.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface ICacheStrategy extends Iterable<AuthorityRecord>
{
	AuthorityRecord UNCONTROLLED_AUTHORITY_RECORD = new AuthorityRecord(
			IConstants.EMPTY_STRING_SET, 
			null, 
			IConstants.EMPTY_STRING_SET, 
			IConstants.EMPTY_STRING_SET, 
			IConstants.EMPTY_STRING_SET,
			null);
	
	/**
	 * Returns, if it has been already cached, the AuthorityRecord associated with the given key.
	 * 
	 * @param key the AuthorityRecord key.
	 * @return if it has been already cached, the AuthorityRecord associated with the given key.
	 */
	AuthorityRecord getAuthorityRecord(String key);

	/**
	 * Caches the given authority record.
	 * @param key the key that will be used later for retrieval.
	 * @param record the authority record. 
	 */
	void storeAuthorityRecord(String key, AuthorityRecord record);

	int size();

	void clear();
}
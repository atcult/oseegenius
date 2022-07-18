package com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.atc.osee.genius.indexer.biblio.browsing.AuthorityRecord;

/**
 * Authority in-memory Record cache. 
 * 
 * That avoid repeated database lookups for the same headings.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class InMemoryCache implements ICacheStrategy
{
	private Map<String, AuthorityRecord> authorityRecords = new HashMap<String, AuthorityRecord>();
	
	@Override
	public AuthorityRecord getAuthorityRecord(final String key) 
	{
		return authorityRecords.get(key);
	}

	@Override
	public void storeAuthorityRecord(final String key, final AuthorityRecord record) 
	{
		authorityRecords.put(key, record);
	}
	
	@Override
	public String toString() 
	{
		return "Authority Record In-memory Cache";
	}

	@Override
	public int size() 
	{
		return authorityRecords.size();
	}

	@Override
	public void clear() 
	{
		authorityRecords.clear();
	}

	@Override
	public Iterator<AuthorityRecord> iterator() 
	{
		return authorityRecords.values().iterator();
	}
}
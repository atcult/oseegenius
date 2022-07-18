package com.atc.osee.web.model;

/**
 * A value object identifying a (selected) federated record.
 * Federated records are clustered by Pazpar2 so in order to get a member in  a 
 * given cluster we need the recid, the id (e.g. AN) and the offset. This latter will be
 * eventually used for exporting MARC data.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class FederatedItemSelection 
{
	public final String recid;
	public final String id;
	public final int offset;
	
	/**
	 * Builds a new item selection with the given identifiers.
	 * 
	 * @param recid the cluster record identifier.
	 * @param id the record (cluster member) identifier.
	 * @param offset the offset of the given record within the cluster.
	 */
	public FederatedItemSelection(final String recid, final String id, final int offset) 
	{
		this.recid = recid;
		this.id = id;
		this.offset = offset;
	}
	
	@Override
	public int hashCode() 
	{
		return recid.hashCode() + id.hashCode() + offset;
	}
	
	@Override
	public boolean equals(Object object) 
	{ 
		FederatedItemSelection item = (FederatedItemSelection) object;
		return (recid.equals(item.recid) && id.equals(item.id)  && offset == item.offset);
	}
}
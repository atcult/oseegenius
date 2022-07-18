package com.atc.osee.web.model;

/**
 * @author mbraddi
 * @since 1.1
 */
public class SBNFederatedItemSelection 
{
	
	public final String id;
	public final long posInResultSet;

	
	public SBNFederatedItemSelection(final String id, final long position) 
	{
		this.id = id;
		this.posInResultSet = position;
		
	}
	
}
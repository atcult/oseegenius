package com.atc.osee.genius.indexer.biblio;

import java.io.Serializable;
import java.util.List;

public final class CompiledExpression implements Serializable 
{
	private static final long serialVersionUID = 7316473674387251401L;
	
	public final String argument;
	public final ITagHandler tagHandler;
	public final List<IDecorator> chain;
	
	CompiledExpression(final String argument, final ITagHandler tagHandler, final List<IDecorator> chain)
	{
		this.argument = argument;
		this.tagHandler = tagHandler;
		this.chain = chain;
	}
}

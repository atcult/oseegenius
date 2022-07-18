/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Affero General Public License version 3 as published by the Free 
 * Software Foundation with the addition of the following permission added to Section 
 * 15 as permitted in Section 7(a). 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. 
 * You should have received a copy of the GNU Affero General Public License along with this program;
 */
package com.atc.osee.z3950;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jzkit.ServiceDirectory.SearchServiceDescriptionDBO;
import org.jzkit.configuration.api.Configuration;
import org.jzkit.configuration.api.ConfigurationException;
import org.jzkit.search.util.QueryFormatter.QueryFormatter;
import org.jzkit.search.util.QueryFormatter.QueryFormatterException;
import org.jzkit.search.util.QueryModel.Internal.AttrPlusTermNode;
import org.jzkit.search.util.QueryModel.Internal.AttrValue;
import org.jzkit.search.util.QueryModel.Internal.ComplexNode;
import org.jzkit.search.util.QueryModel.Internal.InternalModelNamespaceNode;
import org.jzkit.search.util.QueryModel.Internal.InternalModelRootNode;
import org.jzkit.search.util.QueryModel.Internal.QueryNode;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Service;

/**
 * OseeGenius -Z- query formatter.
 * 
 * @author agazzarini
 * @since 1.0
 */
@Service("OGQF")
public class OseeGeniusQueryFormatter implements QueryFormatter, ApplicationContextAware, org.springframework.context.ApplicationListener 
{
	private static final Log LOGGER = LogFactory.getLog(OseeGeniusQueryFormatter.class);
	private static final String OPEN_PARENTHESIS = "(";
	private static final String CLOSE_PARENTHESIS = ")";
	private ApplicationContext applicationContext;
	private Map<String, AttrValue> dictionary;
	
	/**
	 * Query parser / adapter.
	 * Provides transformation logic.
	 * 
	 * @param query the internal representation of a given query.
	 * @return the query in the target (OseeGenius -S-) format. 
	 * @throws QueryFormatterException in case the query builder fails.
	 */
	public String format(final InternalModelRootNode query) throws QueryFormatterException 
	{
		StringWriter writer = new StringWriter();
		visitNode(query, writer);
		return writer.toString();
	}

	/**
	 * Visit the given node and write the corresponding query section.
	 * 
	 * @param node the query node.
	 * @param writer the output writer.
	 * @throws QueryFormatterException in query the node cannot be formatted as a valid query.
	 */
	@SuppressWarnings("unchecked")
	public void visitNode(final QueryNode node, final StringWriter writer) throws QueryFormatterException 
	{	
		if (node instanceof InternalModelRootNode) 
		{
			visitNode(((InternalModelRootNode) node).getChild(), writer); 
		} else if (node instanceof InternalModelNamespaceNode) 
		{
			InternalModelNamespaceNode namespaceNode = (InternalModelNamespaceNode) node;
			visitNode(namespaceNode.getChild(), writer);
		} else if (node instanceof ComplexNode) 
		{
			writer.write(OPEN_PARENTHESIS);
			// Query node operators are 0=none, 1=and, 2=or, 3=andnot, 4=prox
			visitNode(((ComplexNode) node).getLHS(), writer);
			switch (((ComplexNode) node).getOp()) 
			{
				case Operator.AND: 
					writer.write(" AND ");
					break;
				case Operator.OR: 
					writer.write(" OR ");
					break;
				case Operator.NOT: 
					writer.write(" NOT ");
					break;
				case Operator.PROXIMITY: 
					throw new QueryFormatterException("Proximity search not supported by this database");
				default: 
					throw new QueryFormatterException("Unhandled query operator");
			}
			visitNode(((ComplexNode) node).getRHS(), writer);
			writer.write(CLOSE_PARENTHESIS);
		} else if (node instanceof AttrPlusTermNode) 
		{
			AttrPlusTermNode aptn = (AttrPlusTermNode) node;

			boolean mustTruncateOnTheRight = "bib-1.5.1".equals(String.valueOf(aptn.getTruncation()));
			boolean isPhraseSearch = "bib-1.4.1".equals(String.valueOf(aptn.getStructure()));
			boolean isIncompleteSubfield = "bib-1.6.1".equals(String.valueOf(aptn.getCompleteness()));
			
			if (aptn.getAccessPoint() != null) 
			{	
				if (dictionary != null)
				{
					AttrValue translation = dictionary.get(aptn.getAccessPoint().toString());		
					
					writer.write("_query_:\"{!dismax qf=$");
					
					if (!isPhraseSearch)
					{
						
						writer.write(
								(translation != null)
									? translation.getValue() + (!mustTruncateOnTheRight ? "_keyword" : "_right_truncated_keyword")
									: aptn.getAccessPoint().toString());					
					} else if (isPhraseSearch)
					{
						if (isIncompleteSubfield)
						{
							writer.write(
									(translation != null)
										? translation.getValue() + "_right_truncated_phrase"
										: aptn.getAccessPoint().toString());				
						} else 
						{
							writer.write(
									(translation != null)
										? translation.getValue() + "_exact_phrase"
										: aptn.getAccessPoint().toString());	
						}
					}
					writer.write("}");
				} else 
				{
					LOGGER.error("Dictionary cannot be null!! Unable to determine the IR Target attribute of " + aptn.getAccessPoint().toString());
					throw new QueryFormatterException();
				}
			}

			if (aptn.getTerm() instanceof List) 
			{
				List<Object> terms = (List<Object>) aptn.getTerm();

				writer.write("(");

				for (Object term : terms) 
				{
					writeTerm(term, mustTruncateOnTheRight, isPhraseSearch, writer);
				}

				writer.write(")");
			} else if (aptn.getTerm() instanceof String[]) 
			{
				String[] terms = (String[]) aptn.getTerm();
				writer.write("(");
				for (String term : terms) 
				{
					writeTerm(term, mustTruncateOnTheRight, isPhraseSearch, writer);
				}
				writer.write(")");
			} else 
			{
				String term = String.valueOf(aptn.getTerm());
				
				QueryStrategy strategy = Repository.getQueryStrategy(
						aptn.getRelation(), 
						aptn.getPosition(), 
						aptn.getStructure(), 
						aptn.getTruncation(), 
						aptn.getCompleteness());
				
				writeTerm(strategy, term, writer);
				writer.write("\"");
				
//				if (isPhraseSearch)
//				{
//					writeTerm(term, mustTruncateOnTheRight, isPhraseSearch, writer);					
//				} else 
//				{
//					String [] tokens = term.split(" ");
//					if (tokens != null && tokens.length > 1)
//					{					
//						writer.write("(");
//						
//						for (int index = 0; index < tokens.length; index++) 
//						{
//							writeTerm(tokens[index], (index == (tokens.length -1)), isPhraseSearch, writer);
//						}
//						writer.write(")");
//					} else 
//					{
//						writeTerm(term, mustTruncateOnTheRight, isPhraseSearch, writer);
//					}
//				}
			}
		}
	}
	
	@Override
	public void setApplicationContext(final ApplicationContext context)
	{
		this.applicationContext = context;
		try 
		{
			Configuration configuration = (Configuration) applicationContext.getBean("JZKitConfig");
			SearchServiceDescriptionDBO searchService = configuration.lookupSearchService("osee-genius-searcher");
			dictionary = searchService.getServiceSpecificTranslations();
		} catch (ConfigurationException exception) 
		{
			LOGGER.error("Unable to get the service description.", exception);
		}
	}

	@Override
	public void onApplicationEvent(final ApplicationEvent event) 
	{
		// Nothing to be done here...
	}

	/**
	 * Writes out a term using the given data.
	 * 
	 * @param strategy the query strategy.
	 * @param inputTerm the input term.
	 * @param writer the output writer.
	 */
	private void writeTerm(final QueryStrategy strategy, final String inputTerm, final StringWriter writer)
	{
		String term = inputTerm;		
		if (strategy.isUseDoubleQuotes())
		{
			writer.write("\\\"");
		}
		
		writer.write(term);		
		if (strategy.isUseDoubleQuotes())
		{
			writer.write("\\\"");
		}
	}
	
	/**
	 * Writes out a term.
	 * 
	 * @param inputTerm the input term.
	 * @param truncateOnTheRight a flag indicatig if right truncation is needed.
	 * @param isPhraseSearch a flag indicating if phrase search is needed.
	 * @param writer the output writer.
	 */
	private void writeTerm(
			final Object inputTerm, 
			final boolean truncateOnTheRight, 
			final boolean isPhraseSearch, 
			final StringWriter writer) 
	{
		String term = String.valueOf(inputTerm);
		writer.write(String.valueOf(term));
		if (truncateOnTheRight) 
		{
			writer.write("*");
		}
		writer.write(" ");
	}
}
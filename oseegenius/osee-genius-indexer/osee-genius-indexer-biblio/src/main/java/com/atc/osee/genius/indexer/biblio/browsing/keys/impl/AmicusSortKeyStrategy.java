package com.atc.osee.genius.indexer.biblio.browsing.keys.impl;

import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;
import com.atc.osee.genius.indexer.biblio.browsing.ISortKeyStrategy;

/**
 * The default algorithm used for computing sort and inverted sort keys.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AmicusSortKeyStrategy implements ISortKeyStrategy 
{
	private final static Logger LOGGER = LoggerFactory.getLogger(AmicusSortKeyStrategy.class);
	
	private AmicusSortFormAnalyzer analyzer = new AmicusSortFormAnalyzer(IConstants.LUCENE_VERSION);
	
	@Override
	public String sortKey(final String heading, final IHeadingFilter filter) 
	{ 
		try 
		{
			String text = filter.doFilter(heading.toLowerCase().replaceAll("/", " "));
			return createAmicusSortForm(text).trim();
		} catch(Exception exception)
		{
			LOGGER.error("Unable to create sort key for heading " + heading, exception);
			return heading.toUpperCase().trim();
		}	
	}
	
	/**
	 * Returns the AMICUS sort form of the given heading.
	 * 
	 * @param heading the heading value.
	 * @return the AMICUS sort form of the given heading.
	 * @throws Exception in case the heading cannot be controlled.
	 */
	public String createAmicusSortForm(String heading) throws Exception
	{
		StringBuilder builder = new StringBuilder();
		try 
		{
			TokenStream stream = new ASCIIFoldingFilter(analyzer.tokenStream("sortkey", new StringReader(heading.trim())));
			CharTermAttribute term = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) 
			{
				builder.append(term.toString());
			}
			stream.end();
			stream.reset();
		
			for (int index = 0; index < builder.length(); index++)
			{
				char aChar = builder.charAt(index);
				if (aChar == 31)
				{
					if (index == 0)
					{
						builder.delete(index, index + 2);
						continue;
					} else
					{
						if (builder.charAt(index -1) != ' ')
						{
							builder.replace(index, index + 2, " ");		
							index++;
						} else
						{
							builder.delete(index, index + 2);		
							continue;
						}
					}
					aChar = builder.charAt(index);
				}	
				
				if (aChar == '-')
				{
					if (index != builder.length()-1)
					{
						if ( (Character.isDigit(builder.charAt(index-1)) || Character.isLetter(builder.charAt(index-1))) 
								&& 
								( Character.isDigit(builder.charAt(index+1)) || Character.isLetter(builder.charAt(index+1))))
						{
							builder.replace(index, index + 1, " ");
						} else
						{
							builder.deleteCharAt(index);					
							continue;
						}
						index--;
						continue;
					} else 
					{
						builder.replace(index, index + 1, " ");
						aChar = builder.charAt(index);
					}
				} else if (aChar == ' ')
				{
					if (index < builder.length()-1  && builder.charAt(index + 1) == ' ')
					{
						builder.deleteCharAt(index);		
						index-=2;
						continue;
					}
				} else
				{
					if (!(Character.isLetter(aChar) || Character.isDigit(aChar) || aChar == ' '))
					{
						builder.deleteCharAt(index);
						continue;
					} 
				}
			}
			return builder.toString().replace("-", "").replace("  ", " ");
		} catch(Exception exception)
		{
			throw new IllegalArgumentException(exception);
		}	
	}	
}
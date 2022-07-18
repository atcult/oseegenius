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
package com.atc.osee.genius.indexer.biblio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

/**
 * OseeGenius -I- BIBLIO Utilities.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class Utils 
{    
    private static final Pattern FOUR_DIGIT_PATTERN_BRACES = Pattern.compile("\\[[12]\\d{3,3}\\]");
    private static final Pattern FOUR_DIGIT_PATTERN_ONE_BRACE = Pattern.compile("\\[[12]\\d{3,3}");
    private static final Pattern FOUR_DIGIT_PATTERN_STARTING_WITH_1_2 = Pattern.compile("(20|19|18|17|16|15)[0-9][0-9]");
    private static final Pattern FOUR_DIGIT_PATTERN_OTHER_1 = Pattern.compile("l\\d{3,3}");
    private static final Pattern FOUR_DIGIT_PATTERN_OTHER_2 = Pattern.compile("\\[19\\]\\d{2,2}");
    private static final Pattern FOUR_DIGIT_PATTERN_OTHER_3 = Pattern.compile("(20|19|18|17|16|15)[0-9][-?0-9]");
    private static final Pattern FOUR_DIGIT_PATTERN_OTHER_4 = Pattern.compile("i.e. (20|19|18|17|16|15)[0-9][0-9]");
    private static final Pattern BC_DATE_PATTERN = Pattern.compile("[0-9]+ [Bb][.]?[Cc][.]?");
    private static final Pattern FOUR_DIGIT_PATTERN = Pattern.compile("\\d{4,4}");
    private static Matcher matcher;
    private static Matcher matcherBraces;
    private static Matcher matcherOneBrace;
    private static Matcher matcherStartWith1or2;
    private static Matcher matcherlPlusThreeDigits;
    private static Matcher matcherBracket19PlusTwoDigits;
    private static Matcher matcherIeDate;
    private static Matcher matcherBcDate;
    private static Matcher matcherThreeDigitsPlusUnk;
	
    /**
     * Removes trailing characters (space, comma, slash, semicolon, colon).
     * Trailing period if it is preceded by at least three letters, 
     * and single square bracket characters if they are the start and/or end
     * chars of the cleaned string
     *
     * @param inputString String to clean
     * @return cleaned string
     */
    public static String cleanData(final String inputString)
    {
        String currResult = inputString; 
        String prevResult;
        do 
        {
            prevResult = currResult;
            currResult = currResult.trim();
    
            currResult = currResult.replaceAll(" *([,/;:])$", "");

            // trailing period removed in certain circumstances
            if (currResult.endsWith("."))
            {
                if (currResult.matches(".*\\w\\w\\.$"))
                {
                    currResult = currResult.substring(0, currResult.length() - 1);
                } else if (currResult.matches(".*\\p{L}\\p{L}\\.$"))
                {
                    currResult = currResult.substring(0, currResult.length() - 1);
                } else if (currResult.matches(".*\\w\\p{InCombiningDiacriticalMarks}?\\w\\p{InCombiningDiacriticalMarks}?\\.$"))
                {
                    currResult = currResult.substring(0, currResult.length() - 1);
                }
            }

            currResult = removeOuterSquareBrackets(currResult);

            if (currResult.length() == 0)
            {
                return currResult;
            }
        } while (!currResult.equals(prevResult));
        return currResult;       
    }
    
    /**
     * Removes single outer square bracket characters. 
     * 
     * @param string the input string
     * @return a string without the suare brackets (only if they were at the beginning and / or at the end).
     */
    public static String removeOuterSquareBrackets(final String string) 
    {
        if (string == null || string.length() == 0)
        {
            return string;
        }
        
        String result = string.trim();
                
        if (result.length() > 0)
        {
            boolean openBracketFirst = result.charAt(0) == '[';
            boolean closeBracketLast = result.endsWith("]");
            if (openBracketFirst && closeBracketLast && result.indexOf('[', 1) == -1 && result.lastIndexOf(']', result.length() - 2) == -1)
            {
                return result.substring(1, result.length() - 1);
            } else if (openBracketFirst && result.indexOf(']') == -1)
            {
                return result.substring(1);                
            } else if (closeBracketLast && result.indexOf('[') == -1)
            {
                return result.substring(0, result.length() - 1);                
            }
        }
        return result.trim();
    }
    
    /**
     * Returns the value of the requested subfield.
     * 
     * @param dataField the data field.
     * @param code the subfield code.
     * @return the value of the requested subfield.
     */
    public static final String getSubfieldData(final DataField dataField, final char code) 
    {
        if (dataField != null) 
        {
            Subfield subfield = dataField.getSubfield(code);
            if (subfield != null && subfield.getData() != null) 
            {
                return subfield.getData();
            }
        }
        return null;
    }

    /**
     * Returns the values of a (presumably repetible) subfield.
     * 
     * @param datafield the data field.
     * @param code the subfield code.
     * @return the values of the requested subfield.
     */
    @SuppressWarnings("unchecked")
    public static final List<String> getSubfieldStrings(final DataField datafield, final char code) 
    {
        List<Subfield> subfields = datafield.getSubfields(code);
        List<String> result = new ArrayList<String>(subfields.size());
        for (Subfield subfield : subfields) 
        {
        	String value = subfield.getData();
        	if (value != null)
        	{
        		result.add(subfield.getData());
        	}
        }
        return result;
    }
    
    /**
     * Cleans non-digits from a String.
     * 
     * @param date String to parse
     * @return Numeric part of date String (or null)
     */
    public static String cleanDate(final String date)
    {
        matcherBraces = FOUR_DIGIT_PATTERN_BRACES.matcher(date);
        matcherOneBrace = FOUR_DIGIT_PATTERN_ONE_BRACE.matcher(date);
        matcherStartWith1or2 = FOUR_DIGIT_PATTERN_STARTING_WITH_1_2.matcher(date);
        matcherlPlusThreeDigits = FOUR_DIGIT_PATTERN_OTHER_1.matcher(date);
        matcherBracket19PlusTwoDigits = FOUR_DIGIT_PATTERN_OTHER_2.matcher(date);
        matcherThreeDigitsPlusUnk = FOUR_DIGIT_PATTERN_OTHER_3.matcher(date);
        matcherIeDate = FOUR_DIGIT_PATTERN_OTHER_4.matcher(date);
        matcher = FOUR_DIGIT_PATTERN.matcher(date);
        matcherBcDate = BC_DATE_PATTERN.matcher(date);
        
        String cleanDate = null;
        
        if (matcherBraces.find())
        {   
            cleanDate = matcherBraces.group();
            cleanDate = Utils.removeOuterSquareBrackets(cleanDate);
            if (matcher.find())
            {
                String tmp = matcher.group();
                if (!tmp.equals(cleanDate))
                {
                    tmp = "" + tmp;
                }
            }
        } else if (matcherIeDate.find())
        {
            cleanDate = matcherIeDate.group().replaceAll("i.e. ", "");
        } else if (matcherOneBrace.find())
        {   
            cleanDate = matcherOneBrace.group();
            cleanDate = Utils.removeOuterSquareBrackets(cleanDate);
            if (matcher.find())
            {
                String tmp = matcher.group();
                if (!tmp.equals(cleanDate))
                {
                    tmp = "" + tmp;
                }
            }
        } else if (matcherBcDate.find())
        {   
            cleanDate = null;
        } else if (matcherStartWith1or2.find())
        {   
            cleanDate = matcherStartWith1or2.group();
        } else if (matcherlPlusThreeDigits.find())
        {   
            cleanDate = matcherlPlusThreeDigits.group().replaceAll("l", "1");
        } else if (matcherBracket19PlusTwoDigits.find())
        {   
            cleanDate = matcherBracket19PlusTwoDigits.group().replaceAll("\\[", "").replaceAll("\\]", "");
        } else if (matcherThreeDigitsPlusUnk.find())
        {   
            cleanDate = matcherThreeDigitsPlusUnk.group().replaceAll("[-?]", "0");
        } 
        
        if (cleanDate != null)
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
            String thisYear = dateFormat.format(calendar.getTime());
            try 
            {
                if (Integer.parseInt(cleanDate) > Integer.parseInt(thisYear) + 1) 
                {
                	cleanDate = null;	
                }
            } catch (NumberFormatException nfe)
            {
                cleanDate = null;
            }
        }
        return cleanDate;
    }    
    
    /**
     * Join two fields together with seperator.
     * 
     * @param set a set of marc fields to join.
     * @param separator separation character to put between.
     * @return two fields together with seperator.
     */
    public static String join(final Collection<String> set, final String separator)
    {
        Iterator<String> iterator = set.iterator();
        StringBuffer result = new StringBuffer("");
       
        while (iterator.hasNext())
        {
            result.append(iterator.next());
            if (iterator.hasNext())  
            {
                result.append(separator);
            }
        }
        return result.toString();
    }    
    
    public static int howManySkipChars(DataField field, String tagName, Properties tags2SkipIndicator)
	{
    	// Workaround: AMICUS handles 246 skip too
    	String tag = field.getTag();
    	if ("246".equals(tag) 
    			|| "100".equals(tag) 
    			|| "110".equals(tag) 
    			|| "111".equals(tag) 
    			|| "700".equals(tag) 
    			|| "710".equals(tag) 
    			|| "711".equals(tag) 
    			|| "800".equals(tag) 
    			|| "810".equals(tag) 
    			|| "811".equals(tag))
    	{
    		final Subfield subfield = field.getSubfield('e');
    		if (subfield != null && subfield.getData() != null)
    		{
    			try { return Integer.parseInt(subfield.getData()); } catch (NumberFormatException nAn) { return 0; }
    		} else
    		{
    			return 0;
    		}
    	}
    	
		String indicatorNumber = tags2SkipIndicator.getProperty(tagName);
		if (indicatorNumber == null)
		{
			return 0;
		}
		
		char result = '0';
		result = ("1".equals(indicatorNumber)) ? field.getIndicator1() : field.getIndicator2();
		switch (result)
		{
			case '0' : return 0;
			case '1' : return 1;
			case '2' : return 2;
			case '3' : return 3;
			case '4' : return 4;
			case '5' : return 5;
			case '6' : return 6;
			case '7' : return 7;
			case '8' : return 8;
			case '9' : return 9;
			default: return 0;
		}
	}    
}
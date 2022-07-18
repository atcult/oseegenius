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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.util.Version;

/**
 * Osee Genius -I- constants. 
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface IConstants
{
	Version LUCENE_VERSION = Version.LUCENE_47;
	
	String TXT_DIRECTORY_SYSTEM_PROPERTY_NAME = "txt.directory";
	String PHYSICAL_DESCRIPTION_PREFIX = "physical_description_";
	String EMPTY_STRING = "";
	String [] EMPTY_STRING_ARRAY = new String[0];
	List<String> EMPTY_STRING_LIST = new ArrayList<String>(0);
	Set<String> EMPTY_STRING_SET = new HashSet<String>();
	
	String UTF8 = "UTF-8";
	
	String _000 = "000";
	String _001 = "001";
	String _007 = "007";
	String _008 = "008";
	String _006 = "006";
	String _502 = "502";
	String _300 = "300";
	String _856 = "856";
	
	String ENG = "eng";

	char _9 = '9';
	char _3 = '3';
	
	char A = 'a';
	char M = 'm';
	char B = 'b';
	char P = 'p';
	char C = 'c';
	char D = 'd';
	char E = 'e';
	char F = 'f';
	char G = 'g';
	char I = 'i';
	char J = 'j';
	char K = 'k';
	char O = 'o';
	char R = 'r';
	char T = 't';
	char S = 's';
	char L = 'l';
	char N = 'n';
	char W = 'w';
	char X = 'x';
	char V = 'v';
	char U = 'u';
	
	String _020A = "020a";
	String _020Z = "020z";
	String _022A = "022a";
	String _022Z = "022z";
}
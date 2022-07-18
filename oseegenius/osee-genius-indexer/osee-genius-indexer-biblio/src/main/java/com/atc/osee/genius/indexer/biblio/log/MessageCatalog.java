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
package com.atc.osee.genius.indexer.biblio.log;

/** 
 * OSeeGenius -I- BIBLIO message catalog.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface MessageCatalog 
{
	String PREFIX = "<OGIB-";
	String _000001_INVALID_TAG = PREFIX + "000001> : Wrong tag definition: %s. This procedure won't work and therefore the corresponsing record will be skept.";
	String _000002_MISSING_FT_DIR = PREFIX + "000002> : Full Text directory not found. The extraction won't work and the corresponsing record attribute will be skept.";
	
	String _000003_INVALID_FT_DIR = 
			PREFIX + "000003> : Full Text directory (%s) is not valid or cannot be read. " 
			+ "The extraction won't work and the corresponsing record attribute will be skept.";
	
	String _000004_EXTRACT_TEXT_FAILURE = PREFIX + "000004> : Text cannot be extracted from file %s.";
	String _000005_FULL_TEXT_NOT_FOUND = PREFIX + "000005> : FullText document cannot be found for document with control number %s.";
	String _000006_TEXT_TOO_SHORT = PREFIX + "000006> : Unable to extract keywords for document with control number %s; textual content is too short (or null). ";
	
	String _000007_KI_PLUGIN_NOT_FOUND = 
			PREFIX + "000007> : Keywords extraction cannot proceed because digital plugin seems not properly installed...are you sure you're allowed to do that?";
	
	String _000008_UNABLE_TO_COMMIT = PREFIX + "000008> : Unable to commit. There are %s pending records at the moment.";
	String _000009_MAPPING_OR_MARC_ERROR = PREFIX + "000009> : Malformed directives  (%s) or source (%s) file.";	
	String _000010_MARC_INDEXER_ENDS = PREFIX + "000010> : Marc indexer cycle completed.";		
	String _000011_CLASSIFICATION_FILE_READ_FAILURE = PREFIX + "000011> : Problems while loading the supplied classification file %s.";		
	String _000012_WRONG_ARGUMENT_LIST = PREFIX + "000012> : Tag Handler %s is unable to proceed: wrong argument list.";
	String _000013_UNABLE_TO_CLOSE_INDEXER = PREFIX + "000013> : Failure while optimizing or commiting an index.";		
	String _000014_UNABLE_TO_INITIALISE_AAO = PREFIX + "000014> : Unable to initialize an Authority Access Object.";		
	String _000015_AUTHORITY_SEARCH_FAILURE = PREFIX + "000015> : Authority search failure while dealing with field %s.";		
	String _000016_NO_SUCH_CONTROLLED_HEADING = PREFIX + "000016> : No such controlled heading for %s (field %s).";		
	String _000017_CONTROLLED_BROWSING_IO_FAILURE = PREFIX + "000017> : I/O exception while creating the browsing index..";		
	String _000018_INVALID_EXPRESSION = PREFIX + "000018> : Invalid OML expression (%s). Check your tag expression and have a look at the stacktrace below.";		
}
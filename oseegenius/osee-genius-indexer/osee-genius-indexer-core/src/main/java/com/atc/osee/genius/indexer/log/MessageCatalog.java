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
package com.atc.osee.genius.indexer.log;

/**
 * Osee Genius -I- message catalog.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface MessageCatalog 
{
	String PREFIX = "<OGI-";
	String _000001_REQUEST_RECEIVED = PREFIX + "000001> : Received a indexing request with %s as target.";
	String _000002_BAD_REQUEST = PREFIX + "000002> : %s record indexing is not enabled on this OseeGenius instance.";			
	String _000003_NULL_TARGET_PARAMETER_VALUE = PREFIX + "000003> : record kind cannot be null. I can't know what you want to do!";	
	String 	_000004_INDEXER_START =  PREFIX + "000004> : %s indexing starts...";
	String 	_000005_INDEXER_END =  PREFIX + "000005> : %s indexing has been completed.";
	
	String 	_000006_INDEXER_INSTANTIATION_FAILURE =  PREFIX + "000006> : Unable to instantiate a valid indexer for %s target type.";
	String 	_000007_INDEXER_CONFIGURATION_FAILURE =  PREFIX + "000007> : Unable to configure a valid indexer for %s target type.";
	String 	_000008_COMMON_SETTINGS_MISSING =  PREFIX + "000008> : Common configuration settings are missing so OGI won't be able to work.";
	String 	_000009_INVALID_SOURCE_DIR =  PREFIX + "000009> : Root directory doesn't exist or doesn't have sufficient permission.";
	String 	_000010_DEFINE_INDEXER_FAILURE =  PREFIX 
			+ "000010> : Unable to load the given implementor definition. As consequence of that no indexing will be activated on %s requests.";
	String 	_000011_INVALID_WORKED_DIR =  PREFIX + "000011> : Worked directory doesn't exist or doesn't have sufficient permission.";	
	String 	_000012_MOVING_FILE_2_WORKED_DIR =  PREFIX + "000012> : File %s is going to be moved to the worked out folder.";
	String _000013_FILE_MOVED_2_WORKED_DIR = PREFIX + "000013> : File {} has been moved to {}";	
	String _000014_UNABLE_TO_MOVE_WORKED_FILE = PREFIX + "000014> : File %s hasn't been moved to worked out directory!";
	String _000015_INDEXER_FAILURE = PREFIX + "000015> : Indexer %s failed while executing indexing process.";

}
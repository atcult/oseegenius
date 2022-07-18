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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;

/**
 * A simple utility for creating MARC records.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class MarcFileCreator 
{
	static int [] AN = {67461};
	
	/**
	 * Runs this utility.
	 * 
	 * @param args the command line arguments.
	 * @throws FileNotFoundException in case the input marc file is not found.
	 */
	public static void main(final String[] args) throws FileNotFoundException 
	{
		String filename = "/home/andreag/marc-data/pug.mrc"; //args[0];
		String outputFilename = "/home/andreag/marc-data/pug-test.mrc"; //args[0];
		
		File marcFile = new File(filename);
		if (marcFile.exists())
		{
			MarcReader reader =  new MarcStreamReader(new FileInputStream(marcFile)); 
			MarcWriter writer = new MarcStreamWriter(new FileOutputStream(outputFilename),"UTF-8");
			
			while (reader.hasNext())
			{
				Record record = reader.next();
				
				if (mustBeIncluded(Integer.parseInt(record.getControlNumber())))
				{
					writer.write(record);
				}
			}
		
			writer.close();
		}
	}
	
	private static boolean mustBeIncluded(int id)
	{
		for (int an : AN)
		{
			if (an == id)
			{
				return true;
			}
		}
		return false;
	}
}
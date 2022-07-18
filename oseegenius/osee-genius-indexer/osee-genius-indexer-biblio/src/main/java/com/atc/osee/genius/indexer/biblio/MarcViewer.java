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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.marc4j.MarcDirStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;

/**
 * A simple utility for analizying MARC records.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class MarcViewer 
{
	/**
	 * Runs this utility.
	 * 
	 * @param args the command line arguments.
	 * @throws IOException 
	 */
	public static void main(final String[] args) throws IOException 
	{
		String directoryOrFileName = "/home/agazzarini/test/oseegeniusi/worked-out/casalini.mrc"; //args[0];
//		String filter = args.length > 1 ? args[1] : null;
		File inputFile = new File(directoryOrFileName);
		File outputFile = new File("/home/agazzarini/test/oseegeniusi/pug.mv");
		if (inputFile.exists())
		{
			MarcReader reader = inputFile.isFile() 
					? new MarcStreamReader(new FileInputStream(inputFile)) 
					: new MarcDirStreamReader(inputFile);
					
			MarcWriter mwriter = new MarcStreamWriter(new FileOutputStream("/home/agazzarini/test/oseegeniusi/worked-out/c.mrc"));		
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			int i = 0;
			while (reader.hasNext())
			{				
				Record record = reader.next();
				
				if (i < 10) {
					mwriter.write(record);
					i++;
//					break;
				}
				
				if (i == 10) {
					break;
				}

				writer.write(record.toString());
				writer.newLine();
			}
			
			writer.close();
			mwriter.close();
		}
	}
}
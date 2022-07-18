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

public class MarcSplitter {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws Exception 
	{
		String name = "/home/agazzarini/test/oseegeniusi/biblio/export_1_2012_05_17.mrc";
		String output = "/home/agazzarini/test/oseegeniusi/biblio/export_1_2012_05_17_split_";
		File inputFile = new File(name);
	
		MarcReader reader = new MarcStreamReader(new FileInputStream(inputFile));
		MarcWriter writer = null;
		int count = 0;
		int splitNumber = 1;
		while (reader.hasNext())
		{
			Record record = reader.next();
			if (count == 0)
			{
				writer = new MarcStreamWriter(new FileOutputStream(output + splitNumber + ".mrc"));
			} else if (count % 50000 == 0)
			{
				splitNumber++;
				writer.close();
				writer = new MarcStreamWriter(new FileOutputStream(output + splitNumber + ".mrc"));
			}
			
			writer.write(record);
			count++;
		}
		
		try
		{
			writer.close();
		} catch (Exception exception)
		{
			
		}
	}
}

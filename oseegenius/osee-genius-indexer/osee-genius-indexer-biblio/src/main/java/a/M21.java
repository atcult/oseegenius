package a;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.marc4j.MarcDirStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;

public class M21 {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException 
	{
		
		MarcReader reader =  new MarcStreamReader(new FileInputStream("/home/agazzarini/a1.mrc"), "UTF-8");
		int count = 0;
		while (reader.hasNext())
		{
			Record record = reader.next();

				System.out.println(record.toString());
				
			
			count++;
		}
		
		System.out.println("*****************************");
		System.out.println("TOTALE RECORD = " + count);
	}
}

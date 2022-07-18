/**
 * 
 */
package it.atcult.folio.prework;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;




/**
 * Legge il file config.properties 
 * Questo file è stato messo fuori dal .jar in modo da semplificare la modifica dei 
 * parametri quando necessario.
 * 
 * @author Natascia Bianchini
 *
 */
public class Configuration 
{
	private static Properties configProperties = null;

	//private final static Logger logger = LoggerFactory.getLogger(Configuration.class);
	private static Logger logger = Logger.getLogger(Configuration.class);
	
	public Configuration()
	{
		init();
	}
	
	private void init() {
	    FileInputStream file;
	    
	    if (configProperties == null) {
		    configProperties = new Properties();
			
		    try {
		    	String pathConfig = ""; 
		    	if (System.getProperty("confpathconfig")!= null) {
		    		//pathConfig = System.getProperty("resources")+"/preworkconf.properties";
		    		pathConfig = System.getProperty("confpathconfig");
		    	} else {
		    		pathConfig = "preworkconf.properties";
		    	}
		    	
				file = new FileInputStream(pathConfig);
				configProperties.load(file);
				file.close();
				setConfigProperties(configProperties);
			} 
		    catch (FileNotFoundException e) {	logger.fatal("File config.properties non trovato. Verificare la posizione del file.", e);} 
		    catch (IOException e) { logger.fatal("Errore nella lettura del file configImport.properties.", e);}
	    }
	}
	
	
	public String getUnimarcFile() {
		return configProperties.getProperty("path.file.unimarc_recs");
	}
	
	public String getUnimarcOut() {
		return configProperties.getProperty("path.file.unimarc_worked");
	}
	
	public String getUnimarcFileT2007() {
		return configProperties.getProperty("path.file.unimarc_tesi_2007");
	}
	
	public String getUnimarcOutT2007() {
		return configProperties.getProperty("path.file.unimarc_tesi_wrk_2007");
	}
	
	public String getUnimarcFileT1987() {
		return configProperties.getProperty("path.file.unimarc_tesi_1987");
	}
	
	public String getUnimarcOutT1987() {
		return configProperties.getProperty("path.file.unimarc_tesi_wrk_1987");
	}
	
	public String getDeleteOut() {
		return configProperties.getProperty("path.file.unimarc_todel_worked");
	}
	public String getBidFile() {
		return configProperties.getProperty("path.file.bid");
	}
	
	public String getStringProperty(String keyProperty){
		try {
			return configProperties.getProperty(keyProperty);
		}
		catch (Exception e) {
			logger.fatal("Chiave non trovata in configImport.properties :"+keyProperty);
		}
		return "";
	}
	
	public static Properties getConfigProperties() {
		return configProperties;
	}

	public static void setConfigProperties(Properties configProperties) {
		Configuration.configProperties = configProperties;
	}
	
	public String getConnectionUrl() {	    
	    return configProperties.getProperty("db.connection.url");		
	}
	
	public String getConnectionPassword(){	   
		return configProperties.getProperty("db.connection.password");
	}
	
	public String getConnectionUser(){	    
		return configProperties.getProperty("db.connection.username");
	}

}

package it.atcult.folio.prework;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.apache.log4j.Logger;
/**
 * Prework of from UNIMARC records of SBNWEB
 * 30-07-2019 
 *
 */
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;


public class Prework extends TagDecorator{

	private static Configuration configuration = new Configuration();
	private static Logger logger = Logger.getLogger(Prework.class);
	public static int exit_status = 0;
	
    public static void main( String[] args ) throws Exception    {
    	try{
    		logger.info("Iniziato");
    		System.setProperty("org.marc4j.marc.MarcFactory", "org.marc4j.marc.impl.SortedMarcFactoryImpl");
            String type_work = "standard";
    		if(args.length == 1) {type_work = args[0];}
    		if(args.length == 3) {type_work = args[0];}
    		String path_in = "";
    		String path_out = "";
    		String path_delete = "";
    		PrintWriter deletedStream = null;
    		OutputStream out = null;
    		OutputStream bidFile = null;
    		PrintStream printStream = null;
    		path_delete = configuration.getDeleteOut();
    		
    		if (type_work.equals("standard")) { 
    			path_in =  configuration.getUnimarcFile(); 
    			path_out = configuration.getUnimarcOut();
    			deletedStream = new PrintWriter(path_delete);
    			out = new FileOutputStream(path_out);
    		}
    		
    		if (type_work.equals("tesimaddig")) { 
    			path_in =  configuration.getUnimarcFile(); 
    			path_out = configuration.getUnimarcOut();
    			out = new FileOutputStream(path_out);
    		}
    		
    		if (type_work.equals("artmaddig")) { 
    			path_in =  configuration.getUnimarcFile(); 
    			path_out = configuration.getUnimarcOut();
    			out = new FileOutputStream(path_out);
    		}
    		
    		
    		if (type_work.equals("tesi2007")) { 
    			path_in =  configuration.getUnimarcFileT2007(); 
    			path_out = configuration.getUnimarcOutT2007();
    			out = new FileOutputStream(path_out,true);
    		}	
    		if (type_work.equals("tesi1987")) { 
    			path_in =  configuration.getUnimarcFileT1987(); 
    			path_out = configuration.getUnimarcOutT1987();
    			out = new FileOutputStream(path_out,true);
    		}
    		
    	    bidFile = new FileOutputStream(configuration.getBidFile(), true);
    	    printStream = new PrintStream(bidFile, true);
    		
    		if(args.length == 3) {path_in = args[1];path_out = args[2];}
    		
    		InputStream in = new FileInputStream(path_in);
    		    		
    		MarcReader reader = new MarcStreamReader(in, "UTF-8");
	        MarcWriter writer = new MarcStreamWriter(out, "UTF8");
	        VariableField t997CF = calculate997fix();
	       	while (reader.hasNext()) {
	            try {
	            	Record record = reader.next();
		            String ldr = record.getLeader().toString();
		            String bid = ((ControlField) record.getVariableField("001")).getData();
		            
		            if (ldr.substring(5,6).equals("d") ) {
		            	if (type_work.equals("standard")) {
		            		ControlField field = (ControlField) record.getVariableField("001");
		            		String data = field.getData();
		            		deletedStream.println(data);
		            		continue;
		            	}
		            }
		            
		            if (ldr.substring(7,8).equals("c") ) { 
		            	record = cancItemsInColl(record);
		            } else {
		            	record = cancItemsDubSign(record);
		            }
		            
		            VariableField t049 = calculate049(record, type_work);
		            if (t049!=null) {record.addVariableField(t049);}
		            
		            String [] listTagForteBel = {"960"};
		            ArrayList<VariableField> itemsWithForteBel = insertItemForteBel(record, listTagForteBel);
		            if (itemsWithForteBel!=null){
		            	itemsWithForteBel = cancFruizioneB(itemsWithForteBel);
		                itemsWithForteBel =	replaceAvailabilityCode(record,itemsWithForteBel, bid, printStream);
	            	    record = cancelFields(record, listTagForteBel);
		            	Iterator<VariableField> i = itemsWithForteBel.iterator();
		            	while (i.hasNext()) {
		            		VariableField field = i.next();
		            		record.addVariableField(field);
		            	}
		            }
		            
		            String[] list4xx = {"410", "421", "422", "423", "430", "431", "434", "440",
		            		            "441", "447", "451", "454", "461", "462", "463", "464", "488"};
		            
		            ArrayList<VariableField> new4xx = calculateNew4XX(record, list4xx);
		            if (new4xx!=null){
		            	record = cancelFields(record, list4xx);
		            	Iterator<VariableField> i = new4xx.iterator();
		            	while (i.hasNext()) {
		            		VariableField field = i.next();
		            		record.addVariableField(field);
		            	}
		            }
		            String[] listTitles = {"200", "225", "500", "510", "517", "530", "560"}; 
		            ArrayList<VariableField> titlesWithoutNSB = cleanNSB(record, listTitles);
		            if (titlesWithoutNSB!=null){
		            	record = cancelFields(record, listTitles);
		            	Iterator<VariableField> i = titlesWithoutNSB.iterator();
		            	while (i.hasNext()) {
		            		VariableField field = i.next();
		            		record.addVariableField(field);
		            	}
		            }
		            
		            if (type_work.equals("tesi2007")) {
		            	String[] listMIUR = {"686"};
		            	ArrayList<VariableField> new689 =  calculateNew689(record);
		            	if (new689!=null){
		            		record = cancelFields(record, listMIUR);
			            	Iterator<VariableField> i = new689.iterator();
			            	while (i.hasNext()) {
			            		VariableField field = i.next();
			            		record.addVariableField(field);
			            	}
		            	}
		            	ArrayList<VariableField> new960 = calculateNew960(record);
		            	if (new960!=null){
			            	Iterator<VariableField> i = new960.iterator();
			            	while (i.hasNext()) {
			            		VariableField field = i.next();
			            		record.addVariableField(field);
			            	}
		            	}
		            	if(t997CF!=null ){record.addVariableField(t997CF);}
		            }
		            
		            if (type_work.equals("tesi1987")) {
		            	String[] listMIUR = {"686"};
		            	ArrayList<VariableField> new689 =  calculateNew689(record);
		            	if (new689!=null){
		            		record = cancelFields(record, listMIUR);
			            	Iterator<VariableField> i = new689.iterator();
			            	while (i.hasNext()) {
			            		VariableField field = i.next();
			            		record.addVariableField(field);
			            	}
		            	}
		            	ArrayList<VariableField> new960 = calculateNew960(record);
		            	if (new960!=null){
		            		Iterator<VariableField> i = new960.iterator();
		            		while (i.hasNext()) {
			            		VariableField field = i.next();
			            		record.addVariableField(field);
			            	}
		            	}
		            	if(t997CF!=null ){record.addVariableField(t997CF);}
		            }
		            
		            if (type_work.equals("tesimaddig")) {
		            	String[] f_todel = {"017","FMT"};
		            	record = cancelFields(record,  f_todel);
		            }
		            
		            if (type_work.equals("artmaddig")) {
		            	String[] f_todel = {"FMT"};
		            	record = cancelFields(record,  f_todel);
		            }
		            
		            
		            VariableField FMT = calculateFMT(record, type_work);
		            if (FMT!=null) {record.addVariableField(FMT);}
		           
		            VariableField FOR = calculateFOR(record, type_work);
		            if (FOR!=null) {record.addVariableField(FOR);}
		       		            
		            writer.write(record);    
	            } catch (MarcException e) {
	            	logger.error(e.getMessage(), e);
	            	continue;
	            }
	        }
	        
	        writer.close();
	        out.close();
	        bidFile.close();
	        if (type_work.equals("standard")) { 
	        	deletedStream.close();
	        }
    	}catch (Exception ex) {
    		logger.fatal(ex.getMessage(), ex);
    		exit_status = 1;
    	}
    	logger.info("Finito");
    	System.exit(exit_status);
    }
    
    /**
     * Funzione che cancella  il codice di fruizione 'B' e lo sostituisce con 'A' nella specifica posizione di $e dentro il tag 960
     * Nello stesso momento inserisce il codice di non disponibilità 'P' nella sua specifica posizione di tag 960.
     * Le due modifiche vanno insieme, il codice di non disponibilità 'P' porta la copia a diventare di sola consultazione
     * anche se il codice 'A' la darebbe prestabile.
     * <br/>
     * Analisi in:
     * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Unimarc -> Opac]</a>
     * 
     * @param itemsWithForteBel Lista dei tag con i dati di copia (960)
     * @return new960 La lista dei tag 960 modificati
     */
	private static ArrayList<VariableField> cancFruizioneB(ArrayList<VariableField> itemsWithForteBel) {
		ArrayList<VariableField> new960 = new ArrayList<VariableField>();
		Iterator<VariableField> i = itemsWithForteBel.iterator();
		while (i.hasNext()) {
			VariableField field = i.next();
			DataField df_field = (DataField) field;
			String data = formatField(df_field);
			char ind1 = df_field.getIndicator1();
			char ind2 = df_field.getIndicator2();
			String sub_e = Isbd.extractSubfield(data, "$e", "$");
			
			if ((sub_e != null) && (sub_e.length()>48)){
				int pos_inz_sub_e = data.indexOf("$e");
				String prima = data.substring(0,pos_inz_sub_e);
				String poi = data.substring(pos_inz_sub_e);
				if (poi.substring(48,49).trim().equals("B")) {
					StringBuilder change_str = new StringBuilder(poi);
					change_str.setCharAt(46, 'P');
					change_str.setCharAt(48, 'A');
					poi = String.valueOf(change_str);
					data = prima+poi;
					VariableField out = creaField("960", data, ind1, ind2);
					new960.add(out);
				} else {
					new960.add(field);
				}	
			} else {
				new960.add(field);
			}
		}
		return new960;
	}

	
	
	/**
	 * Utilità per cancellare dei campi
	 * 
	 * @param record Il record che esprime i dati in Unimarc
	 * @param listFields Lista dei campi da cancellare
	 * @return record Il record con i campi tolti
	 */
	private static Record cancelFields(Record record, String[] listFields) {
		for (String tag : listFields) {
			List<VariableField> fields = record.getVariableFields(tag);
			if (fields.isEmpty()){} else {
				Iterator<VariableField> i = fields.iterator();
				while (i.hasNext()) {
					VariableField field = i.next();
					record.removeVariableField(field);
				}
			}
		}
		return record;
	}
    
	
	
	/**
	 * Utilità con cui gli indicatori 'non da indicizzare' di Unimarc diventano << >>.
	 * Può essere usata su tutti i tag da 010 in poi, sia ripetibili che non
	 * <br/>
	 * Analisi in:
	 * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Unimarc -> Opac]</a>
	 * @param record Il record che esprime i dati in Unimarc
	 * @param listTitles La lista dei campi da lavorare
	 * @return newTitles I campi lavorati or null
	 */
	private static ArrayList<VariableField> cleanNSB(Record record, String[] listTitles) {
		ArrayList<VariableField> newTitles = new ArrayList<VariableField>();
		MarcFactory factory = MarcFactory.newInstance();
		
		for (String tag : listTitles) {
			List<VariableField> fields = record.getVariableFields(tag);
			if (fields.isEmpty()){} else {
				Iterator<VariableField> i = fields.iterator();
				while (i.hasNext()) {
					VariableField field = i.next();
					DataField df_field = (DataField) field;
					char ind1 = df_field.getIndicator1();
					char ind2 = df_field.getIndicator2();
					DataField newfield = factory.newDataField(tag, ind1, ind2 );
					List<Subfield> subs = df_field.getSubfields();
					Iterator<Subfield> x = subs.iterator();
					int count = 0;
					while (x.hasNext()) {
						Subfield subfield = (Subfield) x.next();
						String data = subfield.getData();
						char code = subfield.getCode();
						data = Isbd.clearNSB(data);
						newfield.addSubfield(count, factory.newSubfield(code, data));
						count++;
					}
					if (newfield!=null) {
						newTitles.add( (VariableField) newfield );
					}
				}
			}
		}
		
		if(newTitles.isEmpty()) {
			return null;
		} else {
			return newTitles;
		}
	}
	
	/**
	 * Inserimento di un tag (049) inspirato a MARC21 con una sigla che indica la fonte di provenienza dei dati.
	 * <br/>
	 * Nel file ../osee-genius-indexer-distribution/src/solr-configuration/bncf/main/conf/solr2marc,properties NON vi è ancora
	 * un indice con questo campo / sottocampo.
	 * <br/>
	 * Analisi qui:
	* <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [più fogli]</a>
	 * @param record Il record che esprime i dati in Unimarc
	 * @param type_work Tipologia del file di carico
	 * @return Il campo compilato nella classe VariableField di marc4j or null
	 */
	private static VariableField calculate049(Record record, String type_work) {
		MarcFactory factory = MarcFactory.newInstance();
		DataField newfield = factory.newDataField("049", ' ', ' ');
		String identifier = configuration.getStringProperty("code.049."+type_work);
		if (identifier != null) {
			newfield.addSubfield(factory.newSubfield('a', identifier)); 
			return (VariableField) newfield;
		}else {
			return null;
		}
	}

	/**
	 * I tag 4xx passano dalla 'embedded tecnique' tipica di UNIMARC alla 'subfields tecnique' tipica di MARC21
	 * <br/>
	 * Analisi in: 
	 * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Unimarc -> Opac]</a>
	 * @param record Il record che esprime i dati in Unimarc
	 * @param list4xx I tag 4xx presenti nel record
	 * @return new4xx I campi 4xx modificati or null
	 */
	private static ArrayList<VariableField> calculateNew4XX(Record record, String[] list4xx) {
		ArrayList<VariableField> new4xx = new ArrayList<VariableField>();
		
		for (String tag : list4xx) {
			List<VariableField> fields = record.getVariableFields(tag);
			if (fields!=null) {
				Iterator<VariableField> i = fields.iterator();
				while (i.hasNext()) {
					VariableField field = i.next();
					DataField df_field = (DataField) field;
					
					String data = formatField(df_field);
					String[] sections = data.split("\\$1");
					String campo = "";
					for (String piece : sections) {
						if (piece.isEmpty()) { }else {
							if (piece.substring(0, 3).equals("200")) {campo = campo + Isbd.createTitle200(piece.substring(5));}
							if (piece.substring(0, 3).equals("001")) {campo = campo + "$0"+piece.substring(3)+"$3"+piece.substring(3);}	
							if (piece.substring(0, 3).equals("700")) {campo = campo + "$a"+Isbd.create7xx(piece.substring(5));}
							if (piece.substring(0, 3).equals("710")) {campo = campo + "$a"+Isbd.create7xx(piece.substring(5));}	
						}
					}
					char ind1 = df_field.getIndicator1();
					char ind2 = df_field.getIndicator2();
					if (campo.isEmpty()) {} else {
						VariableField out = creaField(tag, campo, ind1, ind2);
						new4xx.add(out);
					}
				}
			}
		}
		
		if (new4xx.isEmpty()) {
			return null;
		}else {
			return new4xx;
			
		}
	}
	
	/**
	* Manipolazione del dato di copia di SBNWEB per gestire le particolarita della collocazione al Forte Belvedere
	* e per le copie elettroniche definite dai tag 956 che si vuole che creiono link a livello di visualizzazione delle copie
	* <br/>
	* Analisi qui:
	* <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Unimarc -> Opac]</a> 
	* 
	* @param record Il record che esprime i dati in Unimarc
	* @param listTagForteBel La lista dei campo 960 da lavore
	* @return new960 La lista dei 960 modificati o null
	*/
	public static ArrayList<VariableField> insertItemForteBel(Record record, String[] listTagForteBel) {
		ArrayList<VariableField> new960 = new ArrayList<VariableField>();
		String ldr = record.getLeader().toString();
		String datoForte= "";
		HashMap<String, String> copiedig = new HashMap<>();
		List<VariableField> links956 = record.getVariableFields("956");
		if ((links956 != null) && (!links956.isEmpty()) ){ copiedig = calculateCopieDig(links956);} 
		
		if (!ldr.substring(7,8).equals("s") ) { datoForte="NOFORTE"; }
		
        for (String tag : listTagForteBel) {
			List<VariableField> fields = record.getVariableFields(tag);
			if (fields !=null) {
				Iterator<VariableField> i = fields.iterator();
				while (i.hasNext()) {
					String key_inventory = null;
					VariableField field = i.next();
					DataField df_field = (DataField) field;
					
					String data = formatField(df_field);
	        		char ind1 = df_field.getIndicator1();
					char ind2 = df_field.getIndicator2();
					
					List<Subfield> subs_d = df_field.getSubfields('d');
					if (subs_d.get(0).getData().length() < 17) {  datoForte="NOFORTE"; }
					String colloc = subs_d.get(0).getData();
					
					Subfield sub_e = df_field.getSubfield('e');
					if (sub_e.getData().length() < 14) {
						key_inventory = null;
					} else {
						key_inventory = sub_e.getData();
						key_inventory = key_inventory.substring(0, 14);
					}
										
					
					if (colloc.matches(" CF.*$")) { 

					} else {
						datoForte="NOFORTE"; 
					}
					
					if (!"NOFORTE".equals(datoForte)) {
						colloc = colloc.substring(13);
						if (colloc.matches("GA\\..*")) {
							datoForte = "SIFORTE";
						} else if (colloc.matches("GE\\..*")) {
							datoForte = "SIFORTE";
						} else if (colloc.matches("Gi\\.1.*")) {
							datoForte = "SIFORTE";
						} else if (colloc.matches("Gi\\.2.*")) {
							datoForte = "SIFORTE";
						} else if (colloc.matches("Gi\\.3.*")) {
							datoForte = "SIFORTE";
						} else if (colloc.matches("Gi\\.4.*")) {
							datoForte = "SIFORTE";
						} else if (colloc.matches("Gi\\.I\\..*")) {
							datoForte = "SIFORTE";
						} else if (colloc.matches("Gi\\.II\\..*")) {
							datoForte = "SIFORTE";
						} else {
							datoForte = "NOFORTE";
						}
					}
					if (datoForte.equals("")) {datoForte = "NOFORTE"; };
					
					if (!data.isEmpty()) {
						//mod. 29/09/2020
						if (datoForte.equals("SIFORTE")) data = data+"$s"+datoForte;
						
						if (!copiedig.isEmpty()) {
							String extra_subs = copiedig.get(key_inventory);
							if ((extra_subs != null) && (extra_subs.length() > 2)) {
								data = data+extra_subs;
							}
						}
						
						VariableField out = creaField("960", data, ind1, ind2);
						new960.add(out);
					}
				}
			}
		}
        
        if (new960.isEmpty()) {
			return null;
		}else {
			return new960;
		}
        
	}
	
	/**
	* Funzione di servizio che a partire dai dati dei tag 956 costruise un oggetto utilizzabile per inserire
	* i link dentro i dati delle copie
	* <br/>
	* Analisi qui:
	* <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Unimarc -> Opac]</a>
	*  
	* @param links956 campi 956 provenienti da lovaorazione per SBNWEB
	* @return out HashMap con 	keys = valori del $e contenuti nel 956 (serve a capire dove aggiungere il link,
	* 							values = stringhe da aggiungere per fare il link dentro le copie
	*/
	private static HashMap<String, String> calculateCopieDig(List<VariableField> links956) {
		HashMap<String, String> out = new HashMap<>();
		Iterator<VariableField> i = links956.iterator();
		
		while (i.hasNext()) {
			VariableField field = i.next();
			DataField df_field = (DataField) field;
			List<Subfield> subs = df_field.getSubfields();
			String sub_u = null; String sub_c = null; String sub_d = null; String sub_e = null;
			for(Subfield sub : subs) {
				if (sub.getCode() == 'c') { sub_c = sub.getData(); }
				if (sub.getCode() == 'd') { sub_d = sub.getData(); }
				if (sub.getCode() == 'u') { sub_u = sub.getData();}
				if (sub.getCode() == 'e') { sub_e = sub.getData(); }
				if (sub.getCode() == 'z') {
					out.put(sub_e, "$u"+ sub_u+"$z"+sub_c+sub_d.trim());
					sub_u = null; sub_c = null; sub_d = null; sub_e = null;
				}
			}
		}
		return out;
	}

	/**
	 * Riformattazione del codice MIUR da 686 a 689, usato per le vecchie tesi su file
	 *  <br/>
	 * Analisi qui: 
	 * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Vecchie Tesi Dottorato]</a> 
	 * 
	 * @param record Il record che esprime i dati in Unimarc
	 * @return new689 Il campo compilato nella classe VariableField di marc4j or null
	 */

	private static ArrayList<VariableField> calculateNew689(Record record) {
		ArrayList<VariableField> new689 = new ArrayList<VariableField>();
		
		List<VariableField> fields = record.getVariableFields("686");
		if (fields !=null) {
			Iterator<VariableField> i = fields.iterator();
        	while (i.hasNext()) {
        		VariableField field = i.next();
        		DataField df_field = (DataField) field;
        		
        		String data = formatField(df_field);
        		char ind1 = df_field.getIndicator1();
				char ind2 = df_field.getIndicator2();
				
				if (data.isEmpty()) {} else {
					VariableField out = creaField("689", data, ind1, ind2);
					new689.add(out);
				}
			}
		}
		
		if (new689.isEmpty()) {
			return null;
		}else {
			return new689;
		}
	}
	
	/**
	 * Sistemazione dei dati di copie delle vecchie tesi di dottorato su file
	 *  <br/>
	 * Analisi qui: 
	 * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Vecchie Tesi Dottorato]</a> 
	 * 
	 * @param record Il record che esprime i dati in Unimarc
	 * @return new960 Il campo compilato nella classe VariableField di marc4j or null
	 */
	private static ArrayList<VariableField> calculateNew960(Record record) {
		ArrayList<VariableField> new960 = new ArrayList<VariableField>();
		
		List<VariableField> items = record.getVariableFields("950");
		if (items !=null) {
			Iterator<VariableField> i = items.iterator();
        	while (i.hasNext()) {
        		VariableField field = i.next();
        		DataField df_field = (DataField) field;
        		char ind1 = df_field.getIndicator1();
				char ind2 = df_field.getIndicator2();
				String campo = "";
				String s_e =""; 
				String s_f="";
				try {				
					s_e = df_field.getSubfield('e').getData();
					s_f = df_field.getSubfield('f').getData();
				} catch ( NullPointerException e ) {
					continue;					
				}
				
				s_f = " CF" + s_f + "                                  ";  
				s_f = s_f.substring(0, 34);
				s_e = "CFTVD"+s_e.substring(2, 11) + "                              " + "  L       ";
				campo = "$aBibl. Nazionale Centrale Di Firenze" + "$d" + s_f + "$e" + s_e; //+"$sNOFORTE" 29/09/2020
				if (campo.isEmpty()) {} else {
					VariableField out = creaField("960", campo, ind1, ind2);
					new960.add(out);
				}
        	}
		}
		
		if (new960.isEmpty()) {
			return null;
		}else {
			return new960;
		}
	}
	
	/**
	 * Per inserire la biblioteca 'CF' in quei records che non possiedono il dato.
	 * Per ora sono solo di BNCF. 
	 * Lo scarico SBNWEB posiziona il codice dele biblioteche che hanno il record in 977 $a
	 * Il 997 non è ripetibile ma il $a sì.
	 *  <br/>
	 * Nel file ../osee-genius-indexer-distribution/src/solr-configuration/bncf/main/conf/solr2marc,properties la linea rilevante è:
	 * library=distinct(977a)
	 * <br/>
	 * Analisi qui:
	* <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data</a> [Tesi Dottorato Mag. Dig., Vecchie Tesi Dottorato, Articoli OAI Mag. Dig.]
	 *  
	 * @return newfield Il campo compilato nella classe VariableField di marc4j 
	 */
	private static VariableField calculate997fix() {
		MarcFactory factory = MarcFactory.newInstance();
		DataField newfield = factory.newDataField("977", ' ', ' ');
		newfield.addSubfield(factory.newSubfield('a', " CF")); 
		return (VariableField) newfield;
	}
	/**
	 * Per creare il tag proprietario FMT $a, non ripetibile. Anche $a non ripetibile
	 *  <br/>
	 * L'analisi è disponibile al link
	 * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [più fogli]</a>
	 * <br/>
	 * Un check importante è fatto sul tag 110
	 * 
	 * @param record Il record che esprime i dati in Unimarc
	 * @param type_work La tipologia di lavorazione, aseconda della fonte
	 * @return newfield Il campo compiltato nella classe VariableField di marc4j oppure null se non riesce il calcolo
	*/
	private static VariableField calculateFMT(Record record, String type_work) {
		MarcFactory factory = MarcFactory.newInstance();
		DataField newfield = factory.newDataField("FMT", ' ', ' ');
		
		//Test if is digital, "DI"
		boolean isdigital = false;
		String pattern = "http://teca\\.bncf.firenze\\.sbn\\.it/ImageViewer";
		//http://teca.bncf.firenze.sbn.it/ImageViewer
		List<VariableField> tag899 = record.getVariableFields("899");
		Iterator<VariableField> i = tag899.iterator();
		while (i.hasNext()) {
			VariableField field = i.next();
			if (field.find(pattern)) {
				isdigital = true;
				break;
			}
		};
		
		if (type_work.equals("tesi2007")) { newfield.addSubfield(factory.newSubfield('a', "TD")); return (VariableField) newfield; }
		if (type_work.equals("tesi1987")) { newfield.addSubfield(factory.newSubfield('a', "TD")); return (VariableField) newfield; }
		if (type_work.equals("tesimaddig")) { newfield.addSubfield(factory.newSubfield('a', "TD")); return (VariableField) newfield; }
		if (type_work.equals("artmaddig")) { newfield.addSubfield(factory.newSubfield('a', "AR")); return (VariableField) newfield; }
		
		if(isdigital) {newfield.addSubfield(factory.newSubfield('a', "DI")); return (VariableField) newfield;}
		
		//Test if is a Doctoral Thesis, "TD"
		List<VariableField> thesis = record.getVariableFields("689");
		if (thesis.isEmpty()) { } else {
			newfield.addSubfield(factory.newSubfield('a', "TD")); return (VariableField) newfield;
		}
		
		
		String ldr = record.getLeader().toString();
		String ldr_7 = ldr.substring(7,8);
		String ldr_6 = ldr.substring(6,7);
		
		//Test on char 7 of Leader; could be a Serial "SE"[periodici], a Series "CO"[collane], an analytic "SP"[spoglio]
		if (ldr_7.equals("a"))  {newfield.addSubfield(factory.newSubfield('a', "SP")); return (VariableField) newfield;}
		if (ldr_7.equals("c"))  {newfield.addSubfield(factory.newSubfield('a', "CO")); return (VariableField) newfield;}
		if (ldr_7.equals("s"))  {
			List<VariableField> tag110 = record.getVariableFields("110");
			String codevalue = "SE";
			String pattern2 = "^b.*$";
			Iterator<VariableField> i2 = tag110.iterator();
			//Same periodical are not 'SE'[periodici] but 'CO'[collane], I need to test on 110
			//Record are 'CO'[collane] if tag 110 start with 'b' char. See unimarc manual pag. 116
			while (i2.hasNext()) {
				VariableField field = i2.next();
				if (field.find(pattern2)) {
					codevalue = "CO";
					break;
				}
			};
			
			newfield.addSubfield(factory.newSubfield('a', codevalue)); return (VariableField) newfield;
		}
		
		//Second level of test on char 6 of Leader
		if (ldr_6.equals("a"))  {newfield.addSubfield(factory.newSubfield('a', "BK")); return (VariableField) newfield;}
		if (ldr_6.equals("b"))  {newfield.addSubfield(factory.newSubfield('a', "MN")); return (VariableField) newfield;}
		if (ldr_6.equals("c"))  {newfield.addSubfield(factory.newSubfield('a', "MS")); return (VariableField) newfield;}
		if (ldr_6.equals("d"))  {newfield.addSubfield(factory.newSubfield('a', "MM")); return (VariableField) newfield;}
		if (ldr_6.matches("e|f"))  {newfield.addSubfield(factory.newSubfield('a', "MP")); return (VariableField) newfield;}
		if (ldr_6.equals("g"))  {newfield.addSubfield(factory.newSubfield('a', "VD")); return (VariableField) newfield;}
		if (ldr_6.equals("i"))  {newfield.addSubfield(factory.newSubfield('a', "AU")); return (VariableField) newfield;}
		if (ldr_6.equals("j"))  {newfield.addSubfield(factory.newSubfield('a', "MU")); return (VariableField) newfield;}
		if (ldr_6.equals("k"))  {newfield.addSubfield(factory.newSubfield('a', "GR")); return (VariableField) newfield;}
		if (ldr_6.equals("l"))  {newfield.addSubfield(factory.newSubfield('a', "ED")); return (VariableField) newfield;}
		if (ldr_6.equals("m"))  {newfield.addSubfield(factory.newSubfield('a', "ML")); return (VariableField) newfield;}
		if (ldr_6.equals("r"))  {newfield.addSubfield(factory.newSubfield('a', "OG")); return (VariableField) newfield;}
		
		return null;
	}
	
	/**
	 * Per creare il tag proprietario FOR $a, non ripetibile MA $a é ripetibile
	 * L'analisi è disponibile al link
	 * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [più fogli]</a>
	 * <br/>
	 * Un check importante è fatto sul tag 110
	 * 
	 * @param record Il record che esprime i dati in Unimarc
	 * @param type_work La tipologia di lavorazione, aseconda della fonte
	 * @return newfield Il campo compiltato nella classe VariableField di marc4j oppure null se non riesce il calcolo
	*/
	private static VariableField calculateFOR(Record record, String type_work) {
		MarcFactory factory = MarcFactory.newInstance();
		DataField newfield = factory.newDataField("FOR", ' ', ' ');
		
		//Test if is digital, "DI"
		boolean isdigital = false;
		String pattern = "http://teca\\.bncf.firenze\\.sbn\\.it/ImageViewer";
		//http://teca.bncf.firenze.sbn.it/ImageViewer
		List<VariableField> tag899 = record.getVariableFields("899");
		Iterator<VariableField> i = tag899.iterator();
		while (i.hasNext()) {
			VariableField field = i.next();
			if (field.find(pattern)) {
				isdigital = true;
				break;
			}
		};
		List<VariableField> tag956 = record.getVariableFields("956");
		if (!tag956.isEmpty()) { isdigital = true; }
		if(isdigital) {newfield.addSubfield(factory.newSubfield('a', "DI"));}
		
		
		if (type_work.equals("tesi2007")) { newfield.addSubfield(factory.newSubfield('a', "TD")); return (VariableField) newfield; }
		if (type_work.equals("tesi1987")) { newfield.addSubfield(factory.newSubfield('a', "TD")); return (VariableField) newfield; }
		if (type_work.equals("tesimaddig")) { newfield.addSubfield(factory.newSubfield('a', "TD")); return (VariableField) newfield; }
		if (type_work.equals("artmaddig")) { newfield.addSubfield(factory.newSubfield('a', "AR")); return (VariableField) newfield; }
		
		
		//Test if is a Doctoral Thesis, "TD"
		List<VariableField> thesis = record.getVariableFields("689");
		if (thesis.isEmpty()) { } else {
			newfield.addSubfield(factory.newSubfield('a', "TD")); return (VariableField) newfield;
		}
		
		
		String ldr = record.getLeader().toString();
		String ldr_7 = ldr.substring(7,8);
		String ldr_6 = ldr.substring(6,7);
		
		//Test on char 7 of Leader; could be a Serial "SE"[periodici], a Series "CO"[collane], an analytic "SP"[spoglio]
		if (ldr_7.equals("a"))  {newfield.addSubfield(factory.newSubfield('a', "SP")); return (VariableField) newfield;}
		if (ldr_7.equals("c"))  {newfield.addSubfield(factory.newSubfield('a', "CO")); return (VariableField) newfield;}
		if (ldr_7.equals("s"))  {
			List<VariableField> tag110 = record.getVariableFields("110");
			String codevalue = "SE";
			String pattern2 = "^b.*$";
			Iterator<VariableField> i2 = tag110.iterator();
			//Same periodical are not 'SE'[periodici] but 'CO'[collane], I need to test on 110
			//Record are 'CO'[collane] if tag 110 start with 'b' char. See unimarc manual pag. 116
			while (i2.hasNext()) {
				VariableField field = i2.next();
				if (field.find(pattern2)) {
					codevalue = "CO";
					break;
				}
			};
			
			newfield.addSubfield(factory.newSubfield('a', codevalue)); return (VariableField) newfield;
		}
		
		//Second level of test on char 6 of Leader
		if (ldr_6.equals("a"))  {newfield.addSubfield(factory.newSubfield('a', "BK")); return (VariableField) newfield;}
		if (ldr_6.equals("b"))  {newfield.addSubfield(factory.newSubfield('a', "MN")); return (VariableField) newfield;}
		if (ldr_6.equals("c"))  {newfield.addSubfield(factory.newSubfield('a', "MS")); return (VariableField) newfield;}
		if (ldr_6.equals("d"))  {newfield.addSubfield(factory.newSubfield('a', "MM")); return (VariableField) newfield;}
		if (ldr_6.matches("e|f"))  {newfield.addSubfield(factory.newSubfield('a', "MP")); return (VariableField) newfield;}
		if (ldr_6.equals("g"))  {newfield.addSubfield(factory.newSubfield('a', "VD")); return (VariableField) newfield;}
		if (ldr_6.equals("i"))  {newfield.addSubfield(factory.newSubfield('a', "AU")); return (VariableField) newfield;}
		if (ldr_6.equals("j"))  {newfield.addSubfield(factory.newSubfield('a', "MU")); return (VariableField) newfield;}
		if (ldr_6.equals("k"))  {newfield.addSubfield(factory.newSubfield('a', "GR")); return (VariableField) newfield;}
		if (ldr_6.equals("l"))  {newfield.addSubfield(factory.newSubfield('a', "ED")); return (VariableField) newfield;}
		if (ldr_6.equals("m"))  {newfield.addSubfield(factory.newSubfield('a', "ML")); return (VariableField) newfield;}
		if (ldr_6.equals("r"))  {newfield.addSubfield(factory.newSubfield('a', "OG")); return (VariableField) newfield;}
		
		return null;
	}
	
	/**
	 * Procedura interna che cancella le linee di copia se si sono dei duplicati di $a, $b, $c, $d, $e nei dati
	 * 
	 * @param record Il record che esprime i dati in Unimarc
	 * @return record Il record modificato con la pulizia dei sottocampi
	 */
	private static Record cancItemsDubSign(Record record) {
		List<VariableField> items = record.getVariableFields("960");
		if (items !=null) {
			Iterator<VariableField> i = items.iterator();
        	while (i.hasNext()) {
        		VariableField field = i.next();
        		DataField df_field = (DataField) field;
        		List<Subfield> subs_a = df_field.getSubfields('a');
        		List<Subfield> subs_b = df_field.getSubfields('b');
        		List<Subfield> subs_c = df_field.getSubfields('c');
        		List<Subfield> subs_d = df_field.getSubfields('d');
        		List<Subfield> subs_e = df_field.getSubfields('e');
        		
        		if ( (subs_a.size() >1) || (subs_b.size() >1) || (subs_c.size() >1) || (subs_d.size() >1) || (subs_e.size() >1)    ) {
        			record.removeVariableField(field);
        			continue;
        		}
        		if (subs_d.isEmpty()) {
        			record.removeVariableField(field);
        			continue;
        		}
        		if (subs_e.isEmpty()) {
        			record.removeVariableField(field);
        			continue;
        		}
        		        		
        		if (subs_d.get(0).getData().length() < 13) {
        			record.removeVariableField(field);
        			continue;
        		}
        		if (subs_e.get(0).getData().length() < 54) {
        			record.removeVariableField(field);
        			continue;
        		}
        		
        	}
		}
		return record;
	}

	
	/**
	 * Cancellazione delle linee di copia (960) nel record. Usato nei record di collana per toglire le copie spurie
	 * 
	 * @param record Il record che esprime i dati in Unimarc
	 * @return record Il record con tolto i campi 960
	 */
	private static Record cancItemsInColl(Record record) {
		List<VariableField> items = record.getVariableFields("960");
		if (items !=null) {
			Iterator<VariableField> i = items.iterator();
        	while (i.hasNext()) {
        		VariableField field = i.next();
        		record.removeVariableField(field);
        	}
		}
		return record;
	}
	
	
	

	
	
	
}

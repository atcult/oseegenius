package it.atcult.folio.prework;


public class Isbd {
	public Isbd() 	{ 
		//Do nothing because we don't need it as object; class of functions
	}
	/**
	 * Al campo 200 vengono cambiati i sottocampi e in alcuni casi e messa la punteggiatura, usato per la gestione dei 4xx
	 * <br/>
	 * Analisi in: 
	 * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Unimarc -> Opac]</a>
	 * @param campo Stringa con il valore di tag 200
	 * @return campo Stringa con il campo che contiene punteggiatura al posto dei sottocampi or null
	 */
	
	public static String createTitle200(String campo) {
		if (campo !=null) {
			campo = clearNSB(campo);
			campo = campo.replaceFirst("\\$a", "\\$t");
			campo = campo.replaceAll("\\$a", " ; ");
			campo = campo.replaceAll("\\$c", " . ");
			campo = campo.replaceAll("\\$d", "\\$l");
			campo = campo.replaceAll("\\$e", "\\$o");
			return campo;
		}else {
			return null;
		}
	}
	
	/**
	 * Ai campi 7xx vengono tolti i sottocampi e messi i dati senza di essi, usato per la gestione dei 4xx
	 * <br/>
	 * Analisi in: 
	 * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Unimarc -> Opac]</a>
	 *   
	 * @param campo Stringa con il valore dei tag 7xx
	 * @return campo Stringa con il campo che contiene punteggiatura al posto dei sottocampi or null
	 */
	public static String create7xx(String field) {
		if (field !=null) {
			field = field.replaceAll("\\$[a-z]", "");
			return field;
		}else {
			return null;
		}
	}
	
	/**
	 * Ai campi vengono i possibili indicatori di 'caratteri non indicizzabili' e sostituiti con << >>
	 * <br/>
	 * Analisi in:
	 * <a href="https://docs.google.com/spreadsheets/d/1gU-ybz0aud6iGBxIISvrK46i3XxnHh5ZF2YqKehnczY/">mappings_biblio_data [Unimarc -> Opac]</a>
	 * @param campo Stringa con il valore dei tag xxx
	 * @return campo Stringa con il campo che contiene << >> al posto dei valori usati da Unimarc
	 * 
	 */
	public static String clearNSB(String campo) {
		if (campo !=null) {
			campo = deleteSubfield(campo, "$4", "$"); // An error that is present sometime
			campo = campo.replaceAll("\\x1BH", "<<");
			campo = campo.replaceAll("\\x1BI", ">>");
			campo = campo.replaceAll("\\x98", "<<");
			campo = campo.replaceAll("\\x9C", ">>");
			campo = campo.replaceAll("\\x88", "<<");
			campo = campo.replaceAll("\\x89", ">>");
			return campo;
		}else {
			return null;
		}
	}
	
	/**
	 * Utilità per cancellare un sottocampo
	 * <br/>
	 * Elimina un sottocampo delimitato da search e delim
	 * 
	 * @param source Campo in formato string
	 * @param search Il sottocampo da cancellare
	 * @param delim Indica quando inizia un nuovo sottocampo, se presente.
	 * @return snew Campo in formato stringa senza il sottocampo di cui si è chiesta la cancellazione
	 */
	
	public static String deleteSubfield(String source, String search, String delim) {
		int i, k, l;
		boolean count;
		String snew;
		l     =  search.length();
		snew  = source;
		count = true;    
		while(count){
			count = false;
			i = snew.indexOf(search);      
			if(i > -1){            
				count = true;
				k = snew.indexOf(delim, i + l - 1);            
				if(k > -1){                
					snew = snew.substring(0, i) + snew.substring(k);                
				}else{
					snew = snew.substring(0, i);
				}
			}
		}
		return snew;
	}
	
	/**
	 * Utilità per estrarre un sottocampo all'itrno di un campo
	 * 
	 * @param source Campo in formato string
	 * @param search Il sottocampo da cercare
	 * @param delim Indica quando inizia un nuovo sottocampo, se presente.
	 * @return subf Valore in formato string del sottocampo di cui si è fatto richiesta or null
	 */

	public static String extractSubfield(String source, String search, String delim) {
		int k;
		String subf = null;
		int l = search.length();
		int i = source.indexOf(search);
		
		if(i > -1){
			k = source.indexOf(delim, i + l - 1);
			if(k > -1){
				subf = source.substring(i,k);
			}else {
				subf = source.substring(i);
			}	
		}else {
			return subf;	
		}
		
		return subf;
	}

}

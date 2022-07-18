package it.atcult.folio.prework;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

/**
* TagDecorator.
*
* @description: With "TagDecorator" we can manipulate the tags
*
*/
public abstract class TagDecorator {
	
	private static final List<Character> typesOfPublicationDate = Collections.unmodifiableList(Arrays.asList('d', 'e', 'f', 'h', 'i', 'j', 'k'));
	
	/**
	 * Checks if is not loanable by checking the time interval between current year and tag 100  
	 * depending on position 9,12 or 13,16  by type of date
	 *
	 * @param content the content
	 * @return true, if is not loanable and false if is loanable
	 */
	private static boolean checkNotLoanable(String content, String bid, PrintStream bidFile) {
		//boolean  notLoanable = true;
		if (isNotLoanable(content, 0) && isNotLoanable(content, 1)) {
			return true;
			//TODO Forse l'elenco degli AN non serve più ?
		}else if (isNotLoanable(content, 0) &&  !isNotLoanable(content, 1) && bidFile != null) {//Non Prestabile nell'anno corrente (0), es: record troppo vecchi e prestabili nell'anno precedente (-1) es: 1950
			bidFile.append(bid + "\r\n");
			return true;
		}else if (!isNotLoanable(content, 0) &&  isNotLoanable(content, 1) && bidFile != null) {//Prestabile nell'anno corrente (0), es: record nuovi, non prestabili nell'anno precedente (-1) es: 2018
			bidFile.append(bid + "\r\n");
			return false;
		}else 
			return false;
	}

	/**
	 * Checks if is not loanable by checking the time interval between current year and tag 100  
	 * 
	 * depending on position 9,12 or 13,16  by type of date
	 * an old (>70)  or new (<3) book cannot be borrowed (P = NON DISPONIBILE) 
	 * @param content the content
	 * @param lastYear the last year
	 * @return true, if is not loanable
	 */
	public static boolean isNotLoanable(String content, int lastYear) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy");
		Date date = new Date();
		String currentYear = dateFormat.format(date);
		content = content.substring(2);
		char typeDate = content.charAt(8);
		String publicationDate = content.substring(9, 13);
		String publicationDate2 = content.substring(13, 17);
		boolean notLoanable = false;
		if (typesOfPublicationDate.contains(typeDate)) {
			try {
				int dif = 0;
				if(typeDate == 'f' || typeDate == 'i') {
					dif = getDifferenceYears(lastYear, currentYear, publicationDate2);
				} else
					dif = getDifferenceYears(lastYear, currentYear, publicationDate);
				
				if (dif >= 70 || dif < 3)
					notLoanable = true;
	
			} catch (NumberFormatException e) {
				notLoanable = false;
			}
		}
		return notLoanable;
	}

	/**
	 * return the different of years
	 *
	 * @param lastYear the last year
	 * @param currentYear the current year
	 * @param lastYear the last publication date
	 * @return dif the difference of years
	 */
	private static int getDifferenceYears(int lastYear, String currentYear, String publicationDate) {
		return (Integer.valueOf(currentYear) - lastYear) - Integer.valueOf(publicationDate);
	}
	
	 /**
	    * Funzione che sostituisce il codice di disponibilità <spazio><spazio> e lo sostituisce con P<spazio> (Solo consultabile) se sono verificate le tre condizioni
	    *
	    * @param record
	    * @param itemsWithForteBel Lista dei tag con i dati di copia (960)
	    * @param bid the tag 001 data
	    * @param bidFile the file to append the bid 
	    * @return new960 La lista dei tag 960 modificati
   */
	public static ArrayList<VariableField> replaceAvailabilityCode(Record record, ArrayList<VariableField> itemsWithForteBel, String bid, PrintStream bidFile) {
		VariableField tag100 = (VariableField) record.getVariableField("100");
		DataField dField = (DataField) tag100;
		String content = formatField(dField);
		//Cambio P con disponibile mettiamo <spazio><spazio> cioè disponibile al prestito
		if(!checkNotLoanable(content, bid, bidFile)) 
			return itemsWithForteBel;
		return changeWithNotAvailable(itemsWithForteBel);
	}

	/**
	    * Funzione che sostituisce con P<spazio> (Solo consultabile) 
	    * Se sono verificate le tre condizioni : copia BNCF(CF),  codice di fruizione uguale ad 'A', 
	    * codice di disponibilità <spazio><spazio>
	    *
	    * @param itemsWithForteBel Lista dei tag con i dati di copia (960)
	 	* @return new960 La lista dei tag 960 modificati
    */
	private static ArrayList<VariableField> changeWithNotAvailable (ArrayList<VariableField> itemsWithForteBel) {
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
				int pos = data.indexOf("$e");
				String preSubfieldE = data.substring(0, pos);
				String postSubfieldE = data.substring(pos);
				if (postSubfieldE.substring(2,4).equals("CF") && postSubfieldE.substring(48,49).trim().equals("A")
						&& postSubfieldE.substring(46,48).equals("  ")) {
					StringBuilder change_str = new StringBuilder(postSubfieldE);
					change_str.setCharAt(46, 'P');
					change_str.setCharAt(47, ' ');
					postSubfieldE = String.valueOf(change_str);
					data = preSubfieldE + postSubfieldE;
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
	    * Funzione che sostituisce con <spazio><spazio> (Prestabile) alle copie che hanno <P><spazio>
	    *
	    * @param itemsWithForteBel Lista dei tag con i dati di copia (960)
	 	* @return new960 La lista dei tag 960 modificati
 */
	private static ArrayList<VariableField> changeWithAvailable (ArrayList<VariableField> itemsWithForteBel) {
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
				int pos = data.indexOf("$e");
				String preSubfieldE = data.substring(0, pos);
				String postSubfieldE = data.substring(pos);
				if (postSubfieldE.substring(2,4).equals("CF") && postSubfieldE.substring(48,49).trim().equals("A")
						&& postSubfieldE.substring(46,48).equals("P ")) {
					StringBuilder change_str = new StringBuilder(postSubfieldE);
					change_str.setCharAt(46, ' ');
					change_str.setCharAt(47, ' ');
					postSubfieldE = String.valueOf(change_str);
					data = preSubfieldE + postSubfieldE;
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
	 * Utilità che traforma un DataField di marc4j in una stringa
	 *  
	 * @param field Il valore DataField da trasformare
	 * @return buffer La stringa risultante o null
	 */
	public static String formatField(DataField field) {
		if (field == null) {return null;}
		StringBuilder buffer = new StringBuilder();
		
		List<Subfield> subfields = field.getSubfields();
		Iterator<Subfield> i = subfields.iterator();
		
		while (i.hasNext()) {
			Subfield subfield = (Subfield) i.next();
			char code = subfield.getCode();
			String data = subfield.getData();
			if (data.length() >= 1) {
				data = data.replaceAll("\\$", " ");
				buffer.append("$"+code+data);
			}
		}
		if (buffer.toString().length() >= 3) {
			return buffer.toString();
		}else {
			return null;
		}
	}

	/**
	 * Utilità che crea un oggetto VariableField di mar4j partendo dai valori passati in formato String
	 * 
	 * @param tag Il tag da usare
	 * @param campo La stringa con i sottocampi indicati con $
	 * @param ind1 Il valore del primo indicatore del campo
	 * @param ind2 Il valore del secondo indicatore del campo
	 * @return field, un oggetto  VariableField
	 */
	public static VariableField creaField(String tag, String campo, char ind1, char ind2) {
		MarcFactory factory = MarcFactory.newInstance();
		DataField field = factory.newDataField(tag, ind1, ind2 );
		String[] sections = campo.split("\\$");
		for (String piece : sections) {
			if (piece.isEmpty()) { }else {
				char sub = piece.charAt(0);
				field.addSubfield(factory.newSubfield(sub, piece.substring(1)));
			}
		}
		
		return (VariableField) field;	
	}
    
}

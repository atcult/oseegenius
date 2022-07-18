package com.atc.osee.genius.indexer.biblio.urp;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

public class DiacriticsReplacementUpdateRequestProcessorFactory extends UpdateRequestProcessorFactory 
{	
	final static String cx = new StringBuilder("c").append((char)780).toString();
	final static String Cx = new StringBuilder("C").append((char)780).toString();
	final static String sx = new StringBuilder("s").append((char)780).toString();
	final static String Sx = new StringBuilder("S").append((char)780).toString();
	
	final static String xc = new StringBuilder().append((char)780).append("c").toString();
	final static String xC = new StringBuilder().append((char)780).append("C").toString();
	final static String xs = new StringBuilder().append((char)780).append("s").toString();
	final static String xS = new StringBuilder().append((char)780).append("S").toString();
	
	final static String wc = new StringBuilder().append((char)769).append("c").toString();//accento acuto (COMBINING ACUTE ACCENT)
	final static String wC = new StringBuilder().append((char)769).append("C").toString();//accento acuto 
	final static String cw = new StringBuilder("c").append((char)769).toString();//accento acuto 
	final static String Cw = new StringBuilder("C").append((char)769).toString();//accento acuto 
	
	final static String ws = new StringBuilder().append((char)769).append("s").toString();//accento acuto (COMBINING ACUTE ACCENT)
	final static String wS = new StringBuilder().append((char)769).append("S").toString();//accento acuto 
	final static String sw = new StringBuilder("s").append((char)769).toString();//accento acuto 
	final static String Sw = new StringBuilder("S").append((char)769).toString();//accento acuto
	
	final static String ls = new StringBuilder().append((char)807).append("s").toString();// cedilla (COMBINING CEDILLA)
	final static String lS = new StringBuilder().append((char)807).append("S").toString();//cedilla
	final static String sl = new StringBuilder("s").append((char)807).toString();//cedilla
	final static String Sl = new StringBuilder("S").append((char)807).toString();//cedilla
	
	final static String at = new StringBuilder("a").append((char)771).toString();//tilde sovrapposta(combining tilde)
	final static String At = new StringBuilder("A").append((char)771).toString();//tilde sovrapposta
	final static String ta = new StringBuilder().append((char)771).append("a").toString();//tilde sovrapposta
	final static String tA = new StringBuilder().append((char)771).append("A").toString();//tilde sovrapposta
	final static String am = new StringBuilder("a").append((char)772).toString();//trattino sovrapposto (combining macron)
	final static String Am = new StringBuilder("A").append((char)772).toString();//trattino sovrapposto 
	final static String ma = new StringBuilder().append((char)772).append("a").toString();//trattino sovrapposto 
	final static String mA = new StringBuilder().append((char)772).append("A").toString();//trattino sovrapposto 
	final static String ax = new StringBuilder("a").append((char)780).toString();//haceck
	final static String Ax = new StringBuilder("A").append((char)780).toString();//haceck
	final static String xa = new StringBuilder().append((char)780).append("a").toString();//haceck
	final static String xA = new StringBuilder().append((char)780).append("A").toString();//haceck
	
	final static String em = new StringBuilder("e").append((char)772).toString();//trattino sovrapposto (cobining macron)
	final static String Em = new StringBuilder("E").append((char)772).toString();//trattino sovrapposto 
	final static String me = new StringBuilder().append((char)772).append("e").toString();//trattino sovrapposto 
	final static String mE = new StringBuilder().append((char)772).append("E").toString();//trattino sovrapposto 
	final static String ex = new StringBuilder("e").append((char)780).toString();//haceck
	final static String Ex = new StringBuilder("E").append((char)780).toString();//haceck
	final static String xe = new StringBuilder().append((char)780).append("e").toString();//haceck
	final static String xE = new StringBuilder().append((char)780).append("E").toString();//haceck
	final static String ed = new StringBuilder("e").append((char)775).toString();//puntino sovrapposto (COMBINING DOT ABOVE)
	final static String Ed = new StringBuilder("E").append((char)775).toString();//puntino sovrapposto
	final static String de = new StringBuilder().append((char)775).append("e").toString();//puntino sovrapposto
	final static String dE = new StringBuilder().append((char)775).append("E").toString();//puntino sovrapposto
	
	final static String gx = new StringBuilder("g").append((char)780).toString();//haceck
	final static String Gx = new StringBuilder("G").append((char)780).toString();//haceck
	final static String xg = new StringBuilder().append((char)780).append("g").toString();//haceck
	final static String xG = new StringBuilder().append((char)780).append("G").toString();//haceck
	
	
	final static String ib = new StringBuilder("i").append((char)774).toString();//COMBINING BREVE
	final static String Ib = new StringBuilder("I").append((char)774).toString();//COMBINING BREVE
	final static String bi = new StringBuilder().append((char)774).append("i").toString();//COMBINING BREVE
	final static String bI = new StringBuilder().append((char)774).append("I").toString();//COMBINING BREVE
	
	final static String im = new StringBuilder("i").append((char)772).toString();//trattino sovrapposto (cobining macron)
	final static String Im = new StringBuilder("I").append((char)772).toString();//trattino sovrapposto 
	final static String mi = new StringBuilder().append((char)772).append("i").toString();//trattino sovrapposto 
	final static String mI = new StringBuilder().append((char)772).append("I").toString();//trattino sovrapposto 
	
	
	final static String ot = new StringBuilder("o").append((char)771).toString();//tilde sovrapposta
	final static String Ot = new StringBuilder("O").append((char)771).toString();//tilde sovrapposta
	final static String to = new StringBuilder().append((char)771).append("o").toString();//tilde sovrapposta
	final static String tO = new StringBuilder().append((char)771).append("O").toString();//tilde sovrapposta
	
	final static String om = new StringBuilder("o").append((char)772).toString();//trattino sovrapposto (cobining macron)
	final static String Om = new StringBuilder("O").append((char)772).toString();//trattino sovrapposto 
	final static String mo = new StringBuilder().append((char)772).append("o").toString();//trattino sovrapposto 
	final static String mO = new StringBuilder().append((char)772).append("O").toString();//trattino sovrapposto 
	
	final static String nw = new StringBuilder("n").append((char)769).toString();//accento acuto (COMBINING ACUTE ACCENT)
	final static String wn = new StringBuilder().append((char)769).append("n").toString();//accento acuto
	
	final static String yw = new StringBuilder("y").append((char)769).toString();//accento acuto (COMBINING ACUTE ACCENT)
	final static String wy = new StringBuilder().append((char)769).append("y").toString();//accento acuto
	
	final static String zx = new StringBuilder("z").append((char)780).toString();//haceck
	final static String Zx = new StringBuilder("Z").append((char)780).toString();//haceck
	final static String xz = new StringBuilder().append((char)780).append("z").toString();//haceck
	final static String xZ = new StringBuilder().append((char)780).append("Z").toString();//haceck
	
	final static String zd = new StringBuilder("z").append((char)775).toString();//puntino sovrapposto (COMBINING DOT ABOVE)
	final static String dz = new StringBuilder().append((char)775).append("z").toString();//puntino sovrapposto

	private Map<String, String> configuration; 
	private String [] fieldNames;
	
	@Override
	public void init(@SuppressWarnings("rawtypes") final NamedList args) 
	{
		SolrParams parameters = SolrParams.toSolrParams(args);
		String replacementsParam = parameters.get("rules");
		
		String fieldNamesParam = parameters.get("fields");
		fieldNames = fieldNamesParam.split(",");
		
		if (replacementsParam != null && replacementsParam.trim().length() != 0)
		{
			String [] tmp = replacementsParam.split(",");
			configuration = new HashMap<String, String>(tmp.length);
			for (String rule : tmp)
			{
				int indexOfEqual = rule.indexOf("=");
				if (indexOfEqual != -1)
				{
					configuration.put(rule.substring(0, indexOfEqual), rule.substring(indexOfEqual + 1));
				}
			}
		} else
		{
			configuration = new HashMap<String, String>(74);
			configuration.put(cx, "č");
			configuration.put(Cx, "Č");
			configuration.put(Sx, "Š");
			configuration.put(sx, "š");
			configuration.put(xc, "č");
			configuration.put(xC, "Č");
			configuration.put(xS, "Š");
			configuration.put(xs, "š");
			
			configuration.put(cw, "ć");
			configuration.put(Cw, "Ć");
			configuration.put(wc, "ć");
			configuration.put(wC, "Ć");
			
			configuration.put(sw, "ś");
			configuration.put(Sw, "Ś");
			configuration.put(ws, "ś");
			configuration.put(wS, "Ś");
			
			configuration.put(sl, "ş");
			configuration.put(Sl, "Ş");
			configuration.put(ls, "ş");
			configuration.put(lS, "Ş");
			
			configuration.put(at, "ã");
			configuration.put(ta, "ã");
			configuration.put(At, "Ã");
			configuration.put(tA, "Ã");
			configuration.put(ax, "̌a");
			configuration.put(xa, "̌a");
			configuration.put(Ax, "Ǎ");
			configuration.put(xA, "Ǎ");
			configuration.put(am, "ā");
			configuration.put(ma, "ā");
			configuration.put(Am, "Ā");
			configuration.put(mA, "Ā");
			
			configuration.put(em, "ē");
			configuration.put(me, "ē");
			configuration.put(Em, "Ē");
			configuration.put(mE, "Ē");
			configuration.put(ex, "̌e");
			configuration.put(xe, "̌e");
			configuration.put(Ex, "Ě");
			configuration.put(xE, "Ě");
			configuration.put(ed, "ė");
			configuration.put(Ed, "Ė");
			configuration.put(de, "ė");
			configuration.put(dE, "Ė");
			
			configuration.put(gx, "̌g");
			configuration.put(xg, "̌g");
			configuration.put(Gx, "Ǧ");
			configuration.put(xG, "Ǧ");
			
			configuration.put(ib, "ĭ");
			configuration.put(Ib, "Ĭ");
			configuration.put(bi, "ĭ");
			configuration.put(bI, "Ĭ");
			configuration.put(im, "ī");
			configuration.put(mi, "ī");
			configuration.put(Im, "Ī");
			configuration.put(mI, "Ī");
			
			configuration.put(ot, "õ");
			configuration.put(to, "õ");
			configuration.put(Ot, "Õ");
			configuration.put(tO, "Õ");
			
			configuration.put(om, "ō");
			configuration.put(mo, "ō");
			configuration.put(Om, "Ō");
			configuration.put(mO, "Ō");
			
			configuration.put(nw, "ń");
			configuration.put(wn, "ń");
			
			configuration.put(yw, "ý");
			configuration.put(wy, "ý");
			
			configuration.put(zx, "ž");
			configuration.put(xz, "ž");
			configuration.put(Zx, "Ž");
			configuration.put(xZ, "Ž");
			
			configuration.put(zd, "ż");
			configuration.put(dz, "ż");
			
		}
	}

	@Override
	public UpdateRequestProcessor getInstance(
			SolrQueryRequest req,
			SolrQueryResponse rsp, 
			UpdateRequestProcessor next) 
	{
		return new DiacriticsReplacementUpdateRequestProcessor(next, configuration, fieldNames);
	}
}
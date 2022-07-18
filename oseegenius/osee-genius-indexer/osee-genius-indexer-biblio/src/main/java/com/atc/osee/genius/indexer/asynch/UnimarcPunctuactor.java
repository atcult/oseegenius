package com.atc.osee.genius.indexer.asynch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

public class UnimarcPunctuactor {
	public Record record(final Record record) {
		for (final Object o : record.getDataFields()) {
			final DataField field = (DataField) o;
			final String tag = field.getTag();
			if ("011".equals(tag)) {
				_011(field);
			} else if ("200".equals(tag)) {
				_200(field);
			} else if ("205".equals(tag)) {
				_205(field);				
			} else if ("206".equals(tag)) {
				_206(field);				
			} else if ("207".equals(tag)) {
				_207(field);				
			} else if ("208".equals(tag)) {
				_208(field);				
			} else if ("210".equals(tag)) {
				_210(field);				
			} else if ("215".equals(tag)) {
				_215(field);				
			} else if ("225".equals(tag)) {
				_225(field);				
			} else if ("316".equals(tag) || "317".equals(tag) || "320".equals(tag)) {
				__316_317_320(field);				
			} else if ("318".equals(tag)) {
				_318(field);				
			} else if ("321".equals(tag)) {
				_321(field);				
			} else if ("327".equals(tag)) {
				_327(field);				
			} else if ("328".equals(tag)) {
				_328(field);				
			} else if ("334".equals(tag) || "345".equals(tag)) {
				_334(field);				
			} else if (tag.startsWith("4")) {				
				_4XX(field);				
			} else if (tag.startsWith("5")) {				
				_5XX(field);				
			} else if ("676".equals(tag)) {				
				_676(field);				
			} else if ("700".equals(tag) || "701".equals(tag) || "702".equals(tag)) {				
				_700_701_702(field);				
				//_676(field);				
			} else if ("710".equals(tag) || "711".equals(tag) || "712".equals(tag)) {				
				_710_711_712(field);				
			}				
		}		
		return record;
	}

	void _700_701_702(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'a':
					if (data.trim().endsWith(",") || data.trim().endsWith(".")) {
						subfield.setData(data.trim().substring(0, data.trim().length() - 1));
					}
					break;	
				case 'b':
				case 'c':
					if (isPunctuactionMissing(data)) {
						subfield.setData(", " + data);
					}
					break;	
				case 'd':
					if(data.trim().endsWith(",")){
						subfield.setData(data.trim().substring(0, data.length() - 1));
					}
					break;
				case 'f':
					if (isPunctuactionMissing(data)) {
						subfield.setData(" <" + data + "> ");
					}
					break;							
				case 'g':
				case 'p':
					if (isPunctuactionMissing(data)) {
						subfield.setData(" (" + data + ") ");
					}
					break;		
				}
			}
		}
	}
	
	boolean isPunctuactionMissing(final String value) {
		return Character.isDigit(value.charAt(0)) || Character.isLetter(value.charAt(0));
	}
	
	void _710_711_712(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'a':
					if (data.trim().endsWith(",") || data.trim().endsWith(".")) {
						subfield.setData(data.trim().substring(0, data.trim().length() - 1));
					}
					break;	
				case 'p':
				case 'b':
					if (isPunctuactionMissing(data)) {
						subfield.setData(". " + data);
					}
					break;			
				case 'c':
				case 'g':	
					if (isPunctuactionMissing(data)) {
						subfield.setData(" (" + data + ") ");
					}
					break;	
				case 'd':	
				case 'e':	
				case 'f':
					subfield.setData(" : " + data);
					break;	
				}
			}
		}
	}	
	
	void _676(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'v':
					subfield.setData(" (" + data + ") ");
					break;
				case 'c':
					subfield.setData(" (" + data + ") ");
					break;
								
				}
			}
		}
	}
	
	void _5XX(final DataField field) {
		final Set<Character> processedSubfields = new HashSet<Character>();
		
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
					case 'b':
						subfield.setData(" [" + data + "] ");
						break;				
					case 'h':
					case 'l':
						subfield.setData(". " + data);
						break;										
					case 'i':
						subfield.setData(", " + data);
						break;
					case 'k':
						subfield.setData(" <" + data + "> ");
						break;										
					case 'j':
						union('j', field, " (", ") ", " ; ", processedSubfields);
						break;										
				}
			}
		}
	}

	void union(final char code, final DataField field, final String prefix, final String suffix, final String separator, final Set<Character> marks) {
		if (marks.contains(code)) {
			return;
		}
		
		final List<?> subfields = field.getSubfields(code);
		for (int i = 0; i < subfields.size(); i++) {
			final Subfield subfield = (Subfield) subfields.get(i);
			final String data = subfield.getData();
			StringBuilder value = new StringBuilder();
			if (i == 0) {
				value.append(prefix);
			}
			
			if (i > 0 && (i < subfields.size()-1)) {
				value.append(separator);
			}
			
			value.append(data);
			
			if (i == subfields.size() - 1) {
				value.append(suffix);				
			}
			
			subfield.setData(value.toString());
		}
		
		marks.add(code);
	}
	
	void _4XX(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'b':
				case 'e':	
				case 'n':						
				case 'o':						
					subfield.setData(" : " + data);
					break;				
				case 'c':
				case 'p':					
					subfield.setData(". -" + data);
					break;									
				case 'd':
				case 'i':				
				case 'u':									
					subfield.setData(", " + data);
					break;				
				case 'f':	
					subfield.setData(" / " + data);
					break;				
				case 'g':	
				case 'v':						
					subfield.setData(" ; " + data);
					break;				
				case 'h':	
				case 'x':	
				case 'y':	
				case 'z':						
					subfield.setData(". " + data);
					break;				
				case 'l':	
					subfield.setData(" = " + data);
					break;				
				case 's':
				case '0':
				case '5':
					subfield.setData(". (" + data + ") ");
					break;				
				}
			}
		}
	}	

	void _334(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'b':
				case 'c':
				case 'd':
					subfield.setData(", " + data);
					break;				
				case 'u':
					subfield.setData(" (" + data + ") ");
					break;				
				}
			}
		}
	}	

	void _328(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 't':
				case 'z':
					subfield.setData(", " + data);
					break;				
				}
			}
		}
	}	
	
	void _327(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'g':
				case 'h':
				case 'i':
					subfield.setData(" ; " + data);
					break;				
				case 'p':
				case 'u':
				case 'z':
					subfield.setData(" (" + data + ") ");
					break;				
				}
			}
		}
	}	
	
	void __316_317_320(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'u':
				case '5':
					subfield.setData(" (" + data + ") ");
					break;				
				}
			}
		}
	}

	void _318(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'h':
				case 'i':
				case 'j':
				case 'k':
				case 'l':					
				case 'n':					
				case 'o':					
				case 'r':					
					subfield.setData(", " + data);
					break;				
				case 'p':					
					subfield.setData(" [" + data + "] ");
					break;				
				case 'u':
				case '5':
					subfield.setData(" (" + data + ") ");
					break;				
				}
			}
		}
	}

	void _321(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'b':
				case 'c':
				case 'x':
					subfield.setData(", " + data);
					break;				
				}
			}
		}
	}

	void _011(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'b':
					subfield.setData(" (" + data + ") ");
					break;
				case 'd':
					subfield.setData(" : " + data);
					break;				
				case 'f':
				case 'g':
				case 'y':
				case 'z':
					subfield.setData(", " + data);
					break;				
				}
			}
		}
	}	
	
	void _200(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'b':
					subfield.setData(" [" + data + "] ");
					break;
				case 'h':						
				case 'c':	
					subfield.setData(". " + data);
					break;
				case 'd':	
					subfield.setData(" = " + data);
					break;		
				case 'e':	
					subfield.setData(" : " + data);
					break;		
				case 'f':	
					subfield.setData(" / " + data);
					break;		
				case 'g':	
					subfield.setData(" ; " + data);
					break;							
				case 'v':	
				case 'z':
				case '5':					
					subfield.setData(" (" + data + ") ");
					break;							
				}
			}				
		}
	}

	void _205(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'b':
					subfield.setData(", ");
					break;
				case 'd':	
					subfield.setData(" = " + data);
					break;		
				case 'f':	
					subfield.setData(" / " + data);
					break;		
				case 'g':	
					subfield.setData(" ; " + data);
					break;
				}
			}				
		}
	}
		
	void _206(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'd':	
					subfield.setData(" (" + data + ")");
					break;			
				case 'c':
				case 'e':	
				case 'f':		
					subfield.setData(" ; " + data);
					break;
				}
			}				
		}
	}
	
	void _207(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'z':	
					subfield.setData(" (" + data + ")");
					break;			
				}
			}				
		}
	}
	
	void _208(final DataField field) {
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'd':	
					subfield.setData(" = " + data);
					break;			
				}
			}				
		}
	}	
	
	void _210(final DataField field) {
		int aCount = 0;
		int eCount = 0;
		Subfield firstMemberOfTheGroup = null;
		Subfield lastMemberOfTheGroup = null;
		
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'a':	
					if (aCount == 0) {
						aCount++;
					} else {
						subfield.setData(" ; " + data);
					}
					break;			
				case 'b':
					subfield.setData(" [" + data + "] ");
					break;
				case 'c':
					subfield.setData(" : " + data);
					break;	
				case 'd':
					subfield.setData(", " + data);
					break;	
				case 'e':	
					if (firstMemberOfTheGroup == null) {
						firstMemberOfTheGroup = subfield;
					}
					
					if (lastMemberOfTheGroup == null) {
						lastMemberOfTheGroup = subfield;
					}
					
					if (eCount == 0) {
						subfield.setData(" (" + data);
						eCount++;
					} else {
						subfield.setData(" ; " + data);
					}
					break;	
				case 'h':
				case 'f':
					if (firstMemberOfTheGroup == null) {
						firstMemberOfTheGroup = subfield;
					}
					
					if (lastMemberOfTheGroup == null) {
						lastMemberOfTheGroup = subfield;
					}

					subfield.setData(", " + data);
					break;				
				case 'g':
					if (firstMemberOfTheGroup == null) {
						firstMemberOfTheGroup = subfield;
					}
					
					if (lastMemberOfTheGroup == null) {
						lastMemberOfTheGroup = subfield;
					}					
					subfield.setData(" : " + data);
					break;
				}	
			}
		}
		
		if (firstMemberOfTheGroup != null) {
			firstMemberOfTheGroup.setData(" ("+firstMemberOfTheGroup.getData());
		}
		
		if (lastMemberOfTheGroup != null) {
			lastMemberOfTheGroup.setData(lastMemberOfTheGroup.getData()+ ") ");
		}
	}	
	
	void _215(final DataField field) {
		int aCount = 0;
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'a':	
					if (aCount == 0) {
						aCount++;
					} else {
						subfield.setData(", " + data);
					}
					break;		
				case 'c':
					subfield.setData(" : " + data);
					break;	
				case 'd':
					subfield.setData(" ; " + data);
					break;		
				case 'e':
					subfield.setData(" + " + data);
					break;						
				}
			}				
		}
	}	
	
	void _225(final DataField field) {
		int fCount = 0;
		for (final Object o : field.getSubfields()) {
			final Subfield subfield = (Subfield) o;
			final String data = subfield.getData();
			if (data != null && data.trim().length() != 0) {
				switch (subfield.getCode()){
				case 'd':
					subfield.setData(" = " + data);
					break;		
				case 'e':
					subfield.setData(" : " + data);
					break;		
				case 'f':	
					if (fCount == 0) {
						fCount++;
						subfield.setData(" / " + data);
					} else {
						subfield.setData(" ; " + data);
					}
					break;		
				case 'h':
					subfield.setData(". " + data);
					break;						
				case 'i':
					subfield.setData(", " + data);
					break;						
				case 'v':
					subfield.setData(" ; " + data);
					break;						
				case 'x':
					subfield.setData(", " + data);
					break;						
				case 'z':
					subfield.setData(" (" + data + ") ");
					break;						
				}
			}				
		}
	}		
}

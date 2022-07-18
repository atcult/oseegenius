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
package com.atc.osee.genius.indexer.biblio.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the FTP content type from a MARC record.
 * 
 * @author agazzarini
 * @since 1.2
 */ 
public class FTPFulltext extends TagHandler implements IConstants {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FTPFulltext.class);

	private SolrServer highlighter;

	private FTPBibliographicLevel typeDetector = new FTPBibliographicLevel();
	
	@Override
	public Object getValue(final String tagMappings, final Record record,
			final SolrParams settings, final SolrCore core,
			final SolrInputDocument document) {
		long begin = System.currentTimeMillis();
		if (highlighter == null) {
			highlighter = new EmbeddedSolrServer(core.getCoreDescriptor()
					.getCoreContainer(), "highlighter");
		}

		final ControlField field = (ControlField) record.getVariableField(_001);
		if (field != null) {
			final boolean isPaper = typeDetector.isPaper(record, "942");
			try {
				Integer amicusNumber = Integer.parseInt(field.getData());

				if ("TOC".equals(tagMappings)) {
					String txt = readFile(amicusNumber, tagMappings, isPaper);
					return txt;
				} else if ("DOC".equals(tagMappings) && !isPaper) {
					final String fulltext = readFullText(amicusNumber);
					if (fulltext != null && fulltext.trim().length() != 0) {
						begin = System.currentTimeMillis();
//						indexHighlighter(amicusNumber, fulltext);

						return fulltext;
					}
				}
			} catch (final Exception exception) {
				LOGGER.error("Some error occured while trying to extract fulltext of record n. "
						+ field);
				return null;
			}
		}
		return null;
	}

	/**
	 * Reads the txt file associated with the given document identifier.
	 * 
	 * @param documentId
	 *            the document identifier.
	 * @param documentKind
	 *            the kind of document (e.g. ABS, DOC, TOC)
	 * @return the txt (extracted) content of the requested document.
	 */
	private String readFile(Integer documentId, String documentKind,
			boolean silent) {
		BufferedReader reader = null;
		String filename = System.getProperty("txt.directory")
				+ File.separatorChar + documentKind.toLowerCase()
				+ File.separatorChar + documentId + "_" + documentKind
				+ ".pdf.txt";
		File f = new File(filename);
		if (!f.canRead()) {
			return null;
		}

		try {
			// ABS: 2250056_ABS_ita.txt
			// TOC: 2195934_TOC.pdf.txt
			// DOC: 2237530.pdf.txt

			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"), 800000);
			
			StringBuilder builder = new StringBuilder();
			String actLine = null;
			while ((actLine = reader.readLine()) != null) {
				builder.append(actLine);
				builder.append("\n");
			}
			return clean(builder.toString());
		} catch (FileNotFoundException exception) {
			if (!silent) {
				LOGGER.error("<OG-1000023> : " + documentKind
						+ " associated with AN " + documentId
						+ " cannot be found (" + filename + ")");
			}
			return null;
		} catch (Exception exception) {
			if (!silent) {
				LOGGER.error("<OG-1000024> : I/O Failure while reading "
						+ documentKind + " associated with AN " + documentId
						+ " cannot be found (" + filename + ")");
			}
			return null;
		} finally {
			try {
				reader.close();
			} catch (Exception exception) {
			}
		}
	}

	/**
	 * Reads the txt file associated with the given document identifier. Note
	 * that in production environment files are named differently because the
	 * suffix "DOC" doesn't appear.
	 * 
	 * @param documentId
	 *            the document identifier.
	 * @return the txt (extracted) content of the requested document.
	 */
	private String readFullText(Integer documentId) {
		InputStream iis = null;
		BufferedReader reader = null;
		String filename = System.getProperty("txt.directory")
				+ File.separatorChar + "doc" + File.separatorChar + documentId
				+ ".pdf.txt";
		File f = new File(filename);
		if (!f.canRead()) {
			return null;
		}

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"), 800000);
			
			StringBuilder builder = new StringBuilder();
			String actLine = null;
			while ((actLine = reader.readLine()) != null) {
				builder.append(actLine);
				builder.append("\n");
			}
			return clean(builder.toString());
		} catch (FileNotFoundException exception) {
			LOGGER.error("<OG-1000023> : Fulltext associated with AN "
					+ documentId + " cannot be found (" + filename + ")");
			return null;
		} catch (Exception exception) {
			LOGGER.error("<OG-1000024> : I/O Failure while reading fulltext associated with AN "
					+ documentId + " cannot be found (" + filename + ")");
			return null;
		} finally {
			try {
				reader.close();
			} catch (Exception exception) {
			}
		}
	}

	private void indexHighlighter(final Integer amicusNumber,
			final String fulltext) {
		try {
			final SolrInputDocument hlDoc = new SolrInputDocument();
			hlDoc.setField("an", amicusNumber);
			hlDoc.setField("text", fulltext);
			highlighter.add(hlDoc);
		} catch (Exception hlexception) {
			LOGGER.error(
					"<OG-1900032> : There was some problem while updating the highlighter core with fulltext associated with AN : "
							+ amicusNumber, hlexception);
		}
	}

	private static byte[] read(InputStream source, int initialSize)
			throws IOException {
		int capacity = initialSize;
		byte[] buf = new byte[capacity];
		int nread = 0;
		int rem = buf.length;
		int n;
		// read to EOF which may read more or less than initialSize (eg: file
		// is truncated while we are reading)
		while ((n = source.read(buf, nread, rem)) > 0) {
			nread += n;
			rem -= n;
			assert rem >= 0;
			if (rem == 0) {
				// need larger buffer
				int newCapacity = capacity << 1;
				if (newCapacity < 0) {
					if (capacity == Integer.MAX_VALUE)
						throw new OutOfMemoryError(
								"Required array size too large");
					newCapacity = Integer.MAX_VALUE;
				}
				rem = newCapacity - capacity;
				buf = Arrays.copyOf(buf, newCapacity);
				capacity = newCapacity;
			}
		}
		return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
	}
	
    private static String clean(String s)
    {
        StringBuilder out = new StringBuilder();

        int codePoint;
        int i = 0;

        while (i < s.length())
        {
            // This is the unicode code of the character.
            codePoint = s.codePointAt(i);
            if ((codePoint == 0x9) ||
                    (codePoint == 0xA) ||
                    (codePoint == 0xD) ||
                    ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                    ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                    ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)))
            {
                out.append(Character.toChars(codePoint));
            } 
            i += Character.charCount(codePoint);
        }
        return out.toString();
    }	
}
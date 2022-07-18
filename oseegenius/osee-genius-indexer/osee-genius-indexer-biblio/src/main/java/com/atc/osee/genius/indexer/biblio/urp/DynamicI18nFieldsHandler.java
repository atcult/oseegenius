package com.atc.osee.genius.indexer.biblio.urp;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import com.atc.osee.genius.indexer.biblio.handlers.RemoveTrailingPunctuationTagHandler;

/**
 * {@link UpdateRequestProcessor} for dealing with FTP dynamic fields (like notes).
 * 
 * @author agazzarini
 * @since 1.2
 */
public class DynamicI18nFieldsHandler extends UpdateRequestProcessor 
{
	private final String [] fields;
	private RemoveTrailingPunctuationTagHandler handler = new RemoveTrailingPunctuationTagHandler();
	/**
	 * Builds a new {@link UpdateRequestProcessor} with the given data.
	 * 
	 * @param next the next processor in the chain.
	 * @param fields the fields that must be i18nized.
	 */
	public DynamicI18nFieldsHandler(final UpdateRequestProcessor next, final String [] fields) 
	{
		super(next);
		this.fields = fields;
	}
	
	@Override
	public void processAdd(final AddUpdateCommand cmd) throws IOException 
	{
		final SolrInputDocument document = cmd.getSolrInputDocument();
		
		for (final String field : fields) 
		{
			processField(document, field);
		}
		super.processAdd(cmd);
	}

	void processField(final SolrInputDocument document, final String field) 
	{
		final Collection<Object> values = document.getFieldValues(field);
		if (values != null) {
			for (final Object value : values) {
				final String v = ((String) value).trim();
				final int indexOfLastSpace = v.lastIndexOf(" ");
				if (indexOfLastSpace != -1 && v.length() == (indexOfLastSpace + 4)) {
					final String language = v.substring(v.length() - 3);
					final String fieldName = field + "_" + language;
					if (fieldName.contains("description") ||(fieldName.contains("subject") && !fieldName.equals("topical_subject"))) {
						try {
								document.addField(fieldName, handler.decorate("", v.substring(0, indexOfLastSpace)));
							
						} catch(Exception ex) {							
							document.addField(fieldName, v.substring(0, indexOfLastSpace));
						}
					} else {
						document.addField(fieldName, v.substring(0, indexOfLastSpace));
					}
				}
			}
		}
		document.removeField(field);
	}
}
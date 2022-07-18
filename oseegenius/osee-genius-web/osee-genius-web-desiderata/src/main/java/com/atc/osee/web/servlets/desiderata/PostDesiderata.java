package com.atc.osee.web.servlets.desiderata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.log.Log;

public class PostDesiderata {
	public static void postManualSuggestion(
			final String targetUrl,
			final int accountId, 
			final String notes,
			final String libraryId,
			final String title, 
			final String author,
			final String publicationYear,
			final String series,
			final String volume,
			final String publisher,
			final String publicationPlace,
			final String isbn,
			final String blevel) throws ClientProtocolException, IOException {
		final CloseableHttpClient httpclient = HttpClients.createDefault(); 
		try {
			final HttpPost post = new HttpPost(targetUrl + "suggestion");
			final List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			 
			nvps.add(new BasicNameValuePair("libraryId", libraryId));
			nvps.add(new BasicNameValuePair("title", title));
			nvps.add(new BasicNameValuePair("author", author));
			nvps.add(new BasicNameValuePair("publicationYear", publicationYear));
			nvps.add(new BasicNameValuePair("series", series));
			nvps.add(new BasicNameValuePair("volume", volume));
			nvps.add(new BasicNameValuePair("publisher", publisher));
			nvps.add(new BasicNameValuePair("publicationPlace", publicationPlace));
			nvps.add(new BasicNameValuePair("isbn", isbn));
			nvps.add(new BasicNameValuePair("blevel", blevel));
			nvps.add(new BasicNameValuePair("notes", notes));
			nvps.add(new BasicNameValuePair("personNumber", String.valueOf(accountId)));
			
			post.setEntity(new UrlEncodedFormEntity(nvps));
			final CloseableHttpResponse response = httpclient.execute(post);
			 try {
			     if (response.getStatusLine().getStatusCode() != 200) {
			    	 Log.error("Error code from Olisuite: " + response.getStatusLine().getStatusCode() +", message = "+ response.getStatusLine().getReasonPhrase());
			    	 throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
			     }
			 } finally {
			     try { EntityUtils.consume(response.getEntity()); } catch (Exception exception) {}
			     try { response.close();} catch (Exception exception) {}
			 }
		} finally {
			httpclient.close();
		}
	}
	
	public static void postSelectedSuggestions(
			final String targetUrl,
			final int accountId, 
			final String libraryId,
			final List<SolrDocument> documents) throws ClientProtocolException, IOException {
		final CloseableHttpClient httpclient = HttpClients.createDefault(); 
		try {
			final HttpPost post = new HttpPost(targetUrl + "suggestions");
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addTextBody("libraryId", libraryId, ContentType.TEXT_PLAIN);
			builder.addTextBody("personNumber", String.valueOf(accountId), ContentType.TEXT_PLAIN);
			
			
			final byte [] header = "<marc:collection xmlns:marc=\"http://www.loc.gov/MARC21/slim\">".getBytes("UTF-8");
			final byte [] footer = "</marc:collection>".getBytes("UTF-8");
			
			final byte [][] streams = new byte[documents.size()][];
			final byte [] stream  = new byte [serializeAndGetSize(documents, streams) + header.length + footer.length];
			int pos = 0;

			System.arraycopy(header, 0, stream, pos, header.length);
			pos+=header.length;
			for (final byte [] recordStream : streams) {
				System.arraycopy(recordStream, 0, stream, pos, recordStream.length);
				pos += recordStream.length;
			}
			System.arraycopy(footer, 0, stream, pos, footer.length);
			
			builder.addBinaryBody("records", stream);
			HttpEntity multipart = builder.build();
			post.setEntity(multipart);
			
			final CloseableHttpResponse response = httpclient.execute(post);
			try {
			     if (response.getStatusLine().getStatusCode() != 200) {
			    	 Log.error("Error code from Olisuite: " + response.getStatusLine().getStatusCode() +", message = "+ response.getStatusLine().getReasonPhrase());
			    	 throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
			     }
			 } finally {
			     try { EntityUtils.consume(response.getEntity()); } catch (Exception exception) {}
			     try { response.close();} catch (Exception exception) {}
			 }
		} finally {
			httpclient.close();
		}
	}
	
	static int serializeAndGetSize(final List<SolrDocument> records, final byte[][] streams) throws UnsupportedEncodingException {
		int size = 0;
		int index = 0;
		for (final SolrDocument record : records) {
			streams[index] = ((String)record.getFieldValue(ISolrConstants.MARC_XML_FIELD_NAME)).getBytes("UTF-8");
			size += streams[index].length;
			index++;
		}
		return size;
	}
}

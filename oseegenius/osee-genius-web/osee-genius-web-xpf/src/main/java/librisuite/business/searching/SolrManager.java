/*
 * (c) LibriCore
 * 
 * Created on 17-aug-2004
 * 
 * SearchBean.java
 */
package librisuite.business.searching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 * This class is scope application, so it is accessed in multithreading mode
 * 
 * @author
 *
 */
public class SolrManager {

	private static Log logger = LogFactory.getLog(SolrManager.class);
	private static final String SOLR_CONNECTION_TIMEOUT = "solrj.connection.timeout";
	private static final String SOLR_SO_CONNECTION_TIMEOUT = "solrj.so.connection.timeout";
	private SolrServer solr;
	private SolrServer solrAuthority;

	private static SolrManager instance = null;

	private SolrManager() {
		super();
	}

	/*
	 * initialized normally in LibrisuiteServlet
	 */
	public static void initInstance(String url) {
		if (instance == null) {
			instance = new SolrManager();
			instance.connect(url);
		}
		
	}

	public static SolrManager getInstance() {
		return instance;
	}

	private void connect(String url) {
		int connectionTimeout = 60;
		int soConnectionTimeout = 3000;
		try {
			connectionTimeout = Integer.parseInt(System.getProperty(SOLR_CONNECTION_TIMEOUT));
			soConnectionTimeout = Integer.parseInt(System.getProperty(SOLR_SO_CONNECTION_TIMEOUT));
		} catch (final Exception exception) {
			// Ignore and use default timeouts
		}

		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder = requestBuilder.setConnectTimeout(soConnectionTimeout);
		requestBuilder = requestBuilder.setConnectionRequestTimeout(connectionTimeout);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setDefaultRequestConfig(requestBuilder.build());
		HttpClient client = builder.build();
		solr = new HttpSolrServer(url, client);
		String urlAuth = url.replace("main", "auth");
		if("true".equals(System.getProperty("authorityEnabled")))
			solrAuthority = new HttpSolrServer(urlAuth, client);
	
	}

	public SolrServer getSolr() {
		return solr;
	}

	public SolrServer getSolrAuthority() {
		return solrAuthority;
	}
}
/*
 * Created on 2013-05-31
 *
 */
package com.atc.osee.web.sbn;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaz4j.Connection;
import org.yaz4j.ResultSet;

/**
 * Represents an open result set through which the user can access the documents
 * of a result set.
 * 
 * @author paul
 * 
 */
public class QueryResponseSbn {
//    private static final Logger logger = LogManager
//            .getLogger(QueryResponse.class);
    
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryResponseSbn.class);

    private TransformerFactory tfac;
    private Target target;
    private Connection connection;
    private String index;
    private String term;
    private ResultSet resultSet;
    private int pageNumber = 0;
    private List<Document> Documents = new ArrayList<Document>();
    
    //per paginazione
    private int startInd;

	public int getStartInd() {
		return startInd;
	}

	public void setStartInd(int startInd) {
		this.startInd = startInd;
	}


	public int getPageNumber() {
        return pageNumber;
    }

    private QueryResponseSbn() {
        tfac = TransformerFactory.newInstance();
    }

    public QueryResponseSbn(Target t, Connection con, String index, String term,
            ResultSet set) {
        this();
        setTarget(t);
        setConnection(con);
        setIndex(index);
        setTerm(term);
        setResultSet(set);
    }

    private void fetchPage(int pageNumber) throws TransformerException {

        StreamSource stylesource = null;
   //     try {
   
        // stylesource = new StreamSource(new FileReader(getTarget().getTransform()));
         
   //      stylesource = new StreamSource(getClass().getResourceAsStream("/unimarc.xsl"));
        
//        } catch (FileNotFoundException e1) {
//        	LOGGER.warn("xsl not found");
//            throw new TransformerException(e1);
//            
//        }
        Transformer transformer = null;
        try {
        	stylesource = new StreamSource(getClass().getResourceAsStream(getTarget().getTransform()));
            transformer = tfac.newTransformer(stylesource);
        } catch (TransformerConfigurationException e) {
        	LOGGER.warn(e.getMessage());
            throw e;
        }
        this.pageNumber = pageNumber;
        int start = (pageNumber - 1) * getTarget().getPageSize();
        int end = start + getTarget().getPageSize() - 1;
        for (int i = start; i <= end && i < getNumFound(); i++) {
            try {
            byte[] content = resultSet.getRecord(i).get("xml");
            LOGGER.debug("input:\n" + new String(content, "UTF-8"));
            StringWriter w = new StringWriter();
            transformer.transform(new StreamSource(new ByteArrayInputStream(
                    content)), new StreamResult(w));
            Documents.add(new Document(w.toString()));
            }
            catch (Exception e) {
            	LOGGER.info("Error fetching record " + (i + 1));
            	LOGGER.info(e.getMessage());
                Documents.add(null);
            }
        }
    }

    //riscritto da me per la paginazione dalla servlet SBNSearchServlet
    public void fetchPage(int pageNumber, int pageSize) throws TransformerException {
    	
        StreamSource stylesource = null;
        Transformer transformer = null;
        
        Documents = new ArrayList<Document>();
        
        try {
        	stylesource = new StreamSource(getClass().getResourceAsStream(getTarget().getTransform()));
            transformer = tfac.newTransformer(stylesource);
        } catch (TransformerConfigurationException e) {
        	LOGGER.warn(e.getMessage());
            throw e;
        }
        this.pageNumber = pageNumber;
        int start = (pageNumber - 1) * pageSize;
        int end = start + pageSize - 1;
        
        startInd = start;
        
        for (int i = start; i <= end && i < getNumFound(); i++) {
            try {
            byte[] content = resultSet.getRecord(i).get("xml");
            LOGGER.debug("input:\n" + new String(content, "UTF-8"));
            StringWriter w = new StringWriter();
            transformer.transform(new StreamSource(new ByteArrayInputStream(
                    content)), new StreamResult(w));
            
            String recordString = w.toString().replaceAll("&#136;", "");
            recordString = recordString.replaceAll("&#137;", "");            
            
            //Documents.add(new Document(w.toString()));
            Documents.add(new Document(recordString));
            
            }
            catch (Exception e) {
            	LOGGER.info("Error fetching record " + (i + 1));
            	LOGGER.info(e.getMessage());
                Documents.add(null);
            }
        }
    }
    
    public void fetch() throws TransformerException {
        if (more()) {
            fetchPage(getPageNumber() + 1);
        }
    }

    public boolean more() {
        if (Documents.size() == getNumFound()) {
            return false;
        } else {
            return true;
        }
    }

    public List<Document> getDocuments() {
        return Documents;
    }

    public int getStart() {
        return getPageNumber();
    }

    public int getRows() {
        return getTarget().getPageSize();
    }

    public long getNumFound() {
        return getResultSet().getHitCount();
    }

    public List<Document> getResults() {
        return Documents;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

}

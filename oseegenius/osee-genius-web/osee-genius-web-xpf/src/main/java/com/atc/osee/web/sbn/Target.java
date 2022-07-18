/*
 * Created on 2013-05-31
 *
 */
package com.atc.osee.web.sbn;

import java.util.HashMap;
import java.util.Map;

import org.yaz4j.Connection;
import org.yaz4j.exception.ZoomException;

/**
 * Represents a Z39.50 search target implementation
 * @author paul
 *
 */
public class Target {
    private String name;
    private String host;
    private int port;
    private String database;
    private Map<String, Index> indexes = new HashMap<String, Index>();
    public String recordSyntax;
    private String transform;
    private int pageSize;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Target(String name, String host, int port, String database) {
        setName(name);
        setHost(host);
        setPort(port);
        setDatabase(database);
    }

    public Connection connection() throws ZoomException {
        Connection c = new Connection(getHost(), getPort());
        c.setSyntax(getRecordSyntax());
        c.setDatabaseName(getDatabase());
        c.connect();
        return c;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof Target) {
            return ((Target)arg0).getName().equals(getName());
        }
        else {
            return false;
        }
    }
    
    private String getDatabase() {
        return database;
    }
    
    private String getHost() {
        return host;
    }
    public Map<String, Index> getIndexes() {
        return indexes;
    }
    public String getName() {
        return name;
    }
    private int getPort() {
        return port;
    }
    public String getRecordSyntax() {
        return recordSyntax;
    }
    
    public String getTransform() {
        return transform;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    private void setDatabase(String database) {
        this.database = database;
    }

    private void setHost(String host) {
        this.host = host;
    }

    public void setIndexes(Map<String, Index> indexes) {
        this.indexes = indexes;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setPort(int port) {
        this.port = port;
    }

    public void setRecordSyntax(String recordSyntax) {
        this.recordSyntax = recordSyntax;
    }

    public void setTransform(String transform) {
        this.transform = transform;
    }

    @Override
    public String toString() {
        return "Target('" + getName() + "')";
    }

}

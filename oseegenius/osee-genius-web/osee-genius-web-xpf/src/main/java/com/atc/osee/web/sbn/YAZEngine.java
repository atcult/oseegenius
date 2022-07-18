/*
 * Main class for initiating Z39.50 searches using the YAZ4J
 * library
 * 
 * Created on 2013-05-31
 *
 */
package com.atc.osee.web.sbn;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaz4j.Connection;
import org.yaz4j.*;
import org.yaz4j.exception.ZoomException;

public class YAZEngine {
    //private static Logger logger = LogManager.getLogger(YAZEngine.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(YAZEngine.class);
    
    private static Map<String, Target> targets = new HashMap<String, Target>();
    private static Map<String, Index> defaultIndexes = new HashMap<String, Index>();
    private static String defaultRecordSyntax;
    private static String defaultIndexName;
    private static String defaultTransform;
    private static int defaultPageSize;

    static {
//        DOMConfigurator.configure("log4j.config");
//        System.loadLibrary("yaz4j");
        try {
            configure();
        } catch (ConfigurationException e) {
            System.out.println("Cannot configure YAZEngine.");
        }
    }

    private static void configure() throws ConfigurationException {
        XMLConfiguration config = new XMLConfiguration("sbn/config.xml");
        HierarchicalConfiguration defaults = config.configurationAt("defaults");
        YAZEngine.defaultRecordSyntax = defaults.getString("record-syntax");
        YAZEngine.defaultIndexName = defaults.getString("default-index[@name]");
        YAZEngine.defaultTransform = defaults.getString("transform");
        YAZEngine.defaultPageSize = defaults.getInt("page-size", 10);
        for (HierarchicalConfiguration index : defaults
                .configurationsAt("index")) {
            Index i = new Index(index.getString("[@name]"),
                    index.getString("[@use]"));
            YAZEngine.defaultIndexes.put(i.getName(), i);
        }
        HierarchicalConfiguration targets = config.configurationAt("targets");
        for (HierarchicalConfiguration target : targets
                .configurationsAt("target")) {
            Target t = new Target(target.getString("[@name]"),
                    target.getString("[@host]"), Integer.parseInt(target
                            .getString("[@port]")),
                    target.getString("[@database]"));
            t.setRecordSyntax(target.getString("record-syntax"));
            if (t.getRecordSyntax() == null) {
                t.setRecordSyntax(YAZEngine.defaultRecordSyntax);
            }
            t.setTransform(target.getString("transform"));
            LOGGER.debug("Target transform: " + t.getTransform());
            if (t.getTransform() == null) {
                t.setTransform(YAZEngine.defaultTransform);
            }
            t.setPageSize(target.getInt("page-size", 0));
            LOGGER.debug("Target page-size: " + t.getPageSize());
            if (t.getPageSize() == 0) {
                t.setPageSize(YAZEngine.defaultPageSize);
            }
            t.getIndexes().putAll(YAZEngine.defaultIndexes);
            for (HierarchicalConfiguration index : target
                    .configurationsAt("index")) {
                Index i = new Index(index.getString("[@name]"),
                        index.getString("[@use]"));
                t.getIndexes().put(i.getName(), i);
            }
            YAZEngine.targets.put(t.getName(), t);
        }
    }

    /**
     * Initiates a new search against the specified target
     * 
     * @param target
     * @param index
     * @param term
     * @throws ZoomException
     */
    public static QueryResponseSbn search(String target, String index,
            String term, String sortField) throws ZoomException {
        Target t = YAZEngine.targets.get(target);
        Connection con = t.connection();
        StringBuffer query = new StringBuffer();
        Formatter format = new Formatter(query);
        format.format("@attr 1=%s %s", t.getIndexes().get(index).getUse(), term);
        PrefixQuery q = new PrefixQuery(query.toString());
        if (con.option("sort") != null) {
            q.sortBy("z3950", "1=4 i>");
        }
        ResultSet set = con.search(q);
        QueryResponseSbn result = new QueryResponseSbn(t, con, index, term, set);
        return result;
    }
}

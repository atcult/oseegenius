JETTY_HOME=*JETTY_HOME!
SOLR_DATA_DIR=*SOLR_DATA_DIR!
SOLR_HOME=*SOLR_HOME!

cd $JETTY_HOME
java -Xms1024m -Xmx1512m -Doseegenius.i.port=*OSEEGENIUS_I_PORT! -Doseegenius.i.host=*OSEEGENIUS_I_HOST! -Djava.util.logging.config.file=$JETTY_HOME/etc/logging-s.properties  -Dog.instance.name=*CUSTOMER_CODE!-s -Djetty.home=$JETTY_HOME -Dsolr.data.dir=$SOLR_DATA_DIR  -Dsolr.solr.home=$SOLR_HOME -Djetty.port=*OSEEGENIUS_S_PORT! -Djetty.host=*OSEEGENIUS_S_HOST! -DSTOP.PORT=*JETTY_STOP_PORT! -DSTOP.KEY=secret -jar start.jar $JETTY_HOME/etc/jetty.xml $JETTY_HOME/etc/jetty-*CUSTOMER_CODE!.xml &
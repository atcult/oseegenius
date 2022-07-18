JETTY_HOME=*JETTY_HOME!
SOLR_DATA_DIR=*SOLR_DATA_DIR!
WORKED_OUT_DIR=*WORKED_OUT_DIR!
SOURCE_DIR=*SOURCE_DIR!
SOLR_HOME=*SOLR_HOME!

cd $JETTY_HOME
java -Xms1024m -Xmx1024m -Djava.util.logging.config.file=$JETTY_HOME/etc/logging-i.properties  -Dog.instance.name=*CUSTOMER_CODE!-i -Djetty.home=$JETTY_HOME -Dsolr.data.dir=$SOLR_DATA_DIR -Dworked.out.directory=$WORKED_OUT_DIR -Dsource.directory=$SOURCE_DIR -Dsolr.solr.home=$SOLR_HOME -Djetty.port=$OSEEGENIUS_I_PORT -Djetty.host=*OSEEGENIUS_I_HOST! -DSTOP.PORT=*JETTY_STOP_PORT! -DSTOP.KEY=secret -jar start.jar $JETTY_HOME/etc/jetty.xml $JETTY_HOME/etc/jetty-*CUSTOMER_CODE!.xml &
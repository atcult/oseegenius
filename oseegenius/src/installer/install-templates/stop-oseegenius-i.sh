JETTY_HOME=*JETTY_HOME!

cd $JETTY_HOME
java -Djetty.home=$JETTY_HOME -DSTOP.PORT=*JETTY_STOP_PORT! -DSTOP.KEY=secret -jar start.jar --stop 
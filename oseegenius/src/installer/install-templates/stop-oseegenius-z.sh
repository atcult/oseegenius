export JZKIT_HOME=/app/jzkit-3.0.0

kill -9 `cat $JZKIT_HOME/etc-*CUSTOMER_CODE!/jzkit.pid`

rm $JZKIT_HOME/etc-*CUSTOMER_CODE!/jzkit.pid
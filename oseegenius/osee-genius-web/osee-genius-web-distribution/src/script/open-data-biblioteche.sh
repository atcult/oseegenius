DB_USERNAME=$1
DB_PASSWORD=$2
DB_ADDRESS=$3
DB_PORT=$4
DB_SID=$5
OUTPUT_FILE_PATH=/usr/local/app_loc/cbtosg/deploy/oseegenius/opendata/
ORACLE_DRIVER_LIB=/usr/local/tomcat/cbtosg/lib/ojdbc6.jar
OSEEGENIUS_WEB_HOME=/usr/local/app_loc/cbtosg/deploy/oseegenius
OSEEGENIUS_TOOL_XML_FILE_PATH=/usr/local/app_loc/cbtosg/conf/

java -Durl="jdbc:oracle:thin:@${DB_ADDRESS}:${DB_PORT}:${DB_SID}" \
-Dusername=$DB_USERNAME -Dpassword=$DB_PASSWORD \
-DfileXMLPath=$OUTPUT_FILE_PATH -DfileToolPath=$OSEEGENIUS_TOOL_XML_FILE_PATH \
-cp $ORACLE_DRIVER_LIB:$OSEEGENIUS_WEB_HOME/WEB-INF/lib/osee-genius-web-cbt-1.2.jar:$OSEEGENIUS_WEB_HOME/WEB-INF/lib/osee-genius-web-xpf-1.2.jar \
it.opendata.CreateXMLDataset

curl "http://$OSEEGENIUS_I_HOST:$OSEEGENIUS_I_PORT/solr/auth/update" -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'
curl "http://$OSEEGENIUS_I_HOST:$OSEEGENIUS_I_PORT/solr/autocomplete_auth/update" -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'

----- README -----

I file:

Il file recs_test_BNCF.mrc è quanto arriva da SBNWEB senza alcuna modifica
Il file recs_worked_test_BNCF è quanto risulta dopo la lavorazione.

Analisi:

Si tratta del software che pulisce ed uniforma i dati bibliografici prima del caricamento in Opac.
E' quello che realizza la 'conversione' Unimarc-->Opac descritta in http://tiny.cc/s2148y

Oltre a quanto ivi datto fa anche la pulizia dei campi 960 problematici.
Vengono tolti tutti questi 960:

1)I 960 presenti nei record di collana [LDR 7 = 'c']
2)I 960 che presentano una ripetizione di uno tra $a, $b, $c, $d, $e
3)I 960 in cui $d è lungo meno di 37 caratteri
4)I 960 in cui $e è lungo meno di 54 caratteri

Per i tag 200, 225, tutti i 4xx e tutti i 5xx i segni di no sort vengono tolti e
cambiati con "<<"   ">>"



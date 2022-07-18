<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE stylesheet [<!ENTITY nbsp "&#160;" >]>

<xsl:stylesheet version="1.0"
  xmlns:marc="http://www.loc.gov/MARC21/slim"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:str="http://exslt.org/strings"
  exclude-result-prefixes="marc str">
  
	<xsl:template name="label_ind2_899">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='0'">Digitalizzazione parziale</xsl:when>
		<xsl:when test="$value='1'">Risorsa digitale</xsl:when>
		<xsl:when test="$value='2'">Risorsa digitale</xsl:when>
		<xsl:otherwise>Risorsa digitale</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<xsl:template name="label_956">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='SBN3'">Digitalizzato nel progetto Google-BNCF</xsl:when>
		<xsl:when test="$value='SBN4'">Fotografie Alluvione 1966 digitalizzate</xsl:when>
		<xsl:otherwise>Risorsa digitale</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	
	<xsl:template name="labels5xx">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='500'">Titolo uniforme:</xsl:when>
		<xsl:when test="$value='510'">Titolo parallelo:</xsl:when>
		<xsl:when test="$value='517'">Altre varianti del titolo:</xsl:when>
		<xsl:when test="$value='530'">Titolo chiave:</xsl:when>
		<xsl:when test="$value='560'">Titolo costruito:</xsl:when>
		<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
	</xsl:template>
  
	<xsl:template name="labels4xx">
	<xsl:param name="value" />
	<xsl:param name="hrc" />
	<xsl:choose>
		<xsl:when test="$value='410'">Legame alla serie:</xsl:when>
		<xsl:when test="$value='421'">Supplemento:</xsl:when>
		<xsl:when test="$value='422'">Supplemento di:</xsl:when>
		<xsl:when test="$value='423'">Pubblicato con:</xsl:when>
		<xsl:when test="$value='430'">Continua:</xsl:when>
		<xsl:when test="$value='431'">Continua in parte:</xsl:when>
		<xsl:when test="$value='434'">Ha assorbito:</xsl:when>
		<xsl:when test="$value='440'">Continuato da:</xsl:when>
		<xsl:when test="$value='441'">Continuato parzialmente da:</xsl:when>
		<xsl:when test="$value='447'">Fuso per formare:</xsl:when>
		<xsl:when test="$value='451'">Altra edizione:</xsl:when>
		<xsl:when test="$value='454'">Traduzione di:</xsl:when>
		<xsl:when test="$value='461'">Fa parte di:</xsl:when>
		<xsl:when test="$value='462'">
			<xsl:if test="$hrc='2'">
				Fa parte di:
			</xsl:if>
			<xsl:if test="not($hrc='2')">
				Contiene:
			</xsl:if>
		</xsl:when>
		<xsl:when test="$value='463'">Contiene:</xsl:when>
		<xsl:when test="$value='464'">Contiene:</xsl:when>
		<xsl:when test="$value='488'">Altre relazioni:</xsl:when>
		<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<xsl:template name="labels225">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='225'">Serie:</xsl:when>
		<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	
	<xsl:template name="code101">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='ita'">Italiano</xsl:when>
		<xsl:when test="$value='eng'">Inglese</xsl:when>
		<xsl:when test="$value='ara'">Arabo</xsl:when>
		<xsl:when test="$value='arc'">Aramaico</xsl:when>
		<xsl:when test="$value='arm'">Armeno</xsl:when>
		<xsl:when test="$value='bul'">Bulgaro</xsl:when>
		<xsl:when test="$value='cat'">Catalano</xsl:when>
		<xsl:when test="$value='che'">Ceco</xsl:when>
		<xsl:when test="$value='chi'">Cinese</xsl:when>
		<xsl:when test="$value='cop'">Copto</xsl:when>
		<xsl:when test="$value='dan'">Danese</xsl:when>
		<xsl:when test="$value='heb'">Ebraico</xsl:when>
		<xsl:when test="$value='esp'">Esperanto</xsl:when>
		<xsl:when test="$value='fin'">Finlandese</xsl:when>
		<xsl:when test="$value='fre'">Francese</xsl:when>
		<xsl:when test="$value='fro'">Francese antico (842-1400 ca.)</xsl:when>
		<xsl:when test="$value='frm'">Francese medio (1400-1600 ca.)</xsl:when>
		<xsl:when test="$value='jpn'">Giapponese</xsl:when>
		<xsl:when test="$value='grc'">Greco classico (fino al 1453)</xsl:when>
		<xsl:when test="$value='gre'">Greco moderno (1453- )</xsl:when>
		<xsl:when test="$value='enm'">Inglese medio (1100-1500 ca)</xsl:when>
		<xsl:when test="$value='lat'">Latino</xsl:when>
		<xsl:when test="$value='lin'">Lingala</xsl:when>
		<xsl:when test="$value='lit'">Lituano</xsl:when>
		<xsl:when test="$value='mul'">Multilingua</xsl:when>
		<xsl:when test="$value='nor'">Norvegese</xsl:when>
		<xsl:when test="$value='dut'">Olandese</xsl:when>
		<xsl:when test="$value='per'">Persiano moderno</xsl:when>
		<xsl:when test="$value='pol'">Polacco</xsl:when>
		<xsl:when test="$value='por'">Portoghese</xsl:when>
		<xsl:when test="$value='pro'">Provenzale (fino al 1500)</xsl:when>
		<xsl:when test="$value='roh'">Retroromanzo</xsl:when>
		<xsl:when test="$value='rus'">Russo</xsl:when>
		<xsl:when test="$value='san'">Sanscrito</xsl:when>
		<xsl:when test="$value='scc'">Serbo-croato (cirillico)</xsl:when>
		<xsl:when test="$value='scr'">Serbo-croato (latino)</xsl:when>
		<xsl:when test="$value='sla'">Slava (altra lingua)</xsl:when>
		<xsl:when test="$value='slo'">Slovacco</xsl:when>
		<xsl:when test="$value='slv'">Sloveno</xsl:when>
		<xsl:when test="$value='spa'">Spagnolo</xsl:when>
		<xsl:when test="$value='swe'">Svedese</xsl:when>
		<xsl:when test="$value='ger'">Tedesco</xsl:when>
		<xsl:when test="$value='tib'">Tibetano</xsl:when>
		<xsl:when test="$value='tur'">Turco</xsl:when>
		<xsl:when test="$value='hun'">Ungherese</xsl:when>
        <xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  
  <xsl:template name="code102">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='it'">Italia</xsl:when>
		<xsl:when test="$value='gb'">Regno Unito</xsl:when>
		<xsl:when test="$value='za'">Africa del sud</xsl:when>
		<xsl:when test="$value='al'">Albania</xsl:when>
		<xsl:when test="$value='sa'">Arabia Saudita</xsl:when>
		<xsl:when test="$value='ar'">Argentina</xsl:when>
		<xsl:when test="$value='at'">Austria</xsl:when>
		<xsl:when test="$value='be'">Belgio</xsl:when>
		<xsl:when test="$value='bo'">Bolivia</xsl:when>
		<xsl:when test="$value='br'">Brasile</xsl:when>
		<xsl:when test="$value='bg'">Bulgaria</xsl:when>
		<xsl:when test="$value='ca'">Canada</xsl:when>
		<xsl:when test="$value='cz'">Cecoslovacchia</xsl:when>
		<xsl:when test="$value='cn'">Cina</xsl:when>
		<xsl:when test="$value='co'">Colombia</xsl:when>
		<xsl:when test="$value='cr'">Costa Rica</xsl:when>
		<xsl:when test="$value='cu'">Cuba</xsl:when>
		<xsl:when test="$value='dk'">Danimarca</xsl:when>
		<xsl:when test="$value='et'">Etiopia</xsl:when>
		<xsl:when test="$value='fi'">Finlandia</xsl:when>
		<xsl:when test="$value='fr'">Francia</xsl:when>
		<xsl:when test="$value='de'">Germania</xsl:when>
		<xsl:when test="$value='jp'">Giappone</xsl:when>
		<xsl:when test="$value='gr'">Grecia</xsl:when>
		<xsl:when test="$value='in'">India</xsl:when>
		<xsl:when test="$value='ir'">Iran</xsl:when>
		<xsl:when test="$value='ie'">Irlanda</xsl:when>
		<xsl:when test="$value='il'">Israele</xsl:when>
		<xsl:when test="$value='yu'">Iugoslavia</xsl:when>
		<xsl:when test="$value='li'">Liechtenstein</xsl:when>
		<xsl:when test="$value='lu'">Lussemburgo</xsl:when>
		<xsl:when test="$value='mg'">Madagascar</xsl:when>
		<xsl:when test="$value='my'">Malesia</xsl:when>
		<xsl:when test="$value='mt'">Malta</xsl:when>
		<xsl:when test="$value='ma'">Marocco</xsl:when>
		<xsl:when test="$value='mx'">Messico</xsl:when>
		<xsl:when test="$value='mc'">Monaco</xsl:when>
		<xsl:when test="$value='ne'">Niger</xsl:when>
		<xsl:when test="$value='ng'">Nigeria</xsl:when>
		<xsl:when test="$value='no'">Norvegia</xsl:when>
		<xsl:when test="$value='pe'">Perù</xsl:when>
		<xsl:when test="$value='pl'">Polonia</xsl:when>
		<xsl:when test="$value='pt'">Portogallo</xsl:when>
		<xsl:when test="$value='ro'">Romania</xsl:when>
		<xsl:when test="$value='sm'">San Marino</xsl:when>
		<xsl:when test="$value='so'">Somalia</xsl:when>
		<xsl:when test="$value='es'">Spagna</xsl:when>
		<xsl:when test="$value='us'">Stati Uniti</xsl:when>
		<xsl:when test="$value='se'">Svezia</xsl:when>
		<xsl:when test="$value='ch'">Svizzera</xsl:when>
		<xsl:when test="$value='tr'">Turchia</xsl:when>
		<xsl:when test="$value='ua'">Ucraina</xsl:when>
		<xsl:when test="$value='ru'">Federazione Russa</xsl:when>
		<xsl:when test="$value='uy'">Uruguay</xsl:when>
		<xsl:when test="$value='ve'">Venezuela</xsl:when>
		<xsl:when test="$value='va'">Città del Vaticano</xsl:when>
		<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  
  <xsl:template name="code7xx">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='005'">Attore</xsl:when>
		<xsl:when test="$value='010'">Adattatore</xsl:when>
		<xsl:when test="$value='018'">Animatore, cartoonist</xsl:when>
		<xsl:when test="$value='020'">Annotatore</xsl:when>
		<xsl:when test="$value='030'">Arrangiatore</xsl:when>
		<xsl:when test="$value='040'">Artista (Pittore, ecc.)</xsl:when>
		<xsl:when test="$value='050'">Assegnatario della licenza di pubblicazione</xsl:when>
		<xsl:when test="$value='060'">Nome associato</xsl:when>
		<xsl:when test="$value='065'">Banditore</xsl:when>
		<xsl:when test="$value='070'">Autore</xsl:when>
		<xsl:when test="$value='072'">Autore citato</xsl:when>
		<xsl:when test="$value='075'">Autore della postfazione, colophon, ecc.</xsl:when>
		<xsl:when test="$value='080'">Autore dell'introduzione</xsl:when>
		<xsl:when test="$value='090'">Autore del dialogo</xsl:when>
		<xsl:when test="$value='100'">Antecedente bibliografico</xsl:when>
		<xsl:when test="$value='110'">Legatore</xsl:when>
		<xsl:when test="$value='120'">Designer della legatura</xsl:when>
		<xsl:when test="$value='130'">Designer del libro</xsl:when>
		<xsl:when test="$value='140'">Designer della sovraccoperta</xsl:when>
		<xsl:when test="$value='160'">Libraio</xsl:when>
		<xsl:when test="$value='170'">Calligrafo</xsl:when>
		<xsl:when test="$value='180'">Cartografo</xsl:when>
		<xsl:when test="$value='190'">Censore</xsl:when>
		<xsl:when test="$value='195'">Direttore di coro</xsl:when>
		<xsl:when test="$value='200'">Coreografo</xsl:when>
		<xsl:when test="$value='205'">Collaboratore</xsl:when>
		<xsl:when test="$value='206'">Collettore di materiale sul campo</xsl:when>
		<xsl:when test="$value='207'">Comico</xsl:when>
		<xsl:when test="$value='210'">Commentatore (audiovisivi)</xsl:when>
		<xsl:when test="$value='212'">Commentatore di testo scritto</xsl:when>
		<xsl:when test="$value='220'">Compilatore</xsl:when>
		<xsl:when test="$value='230'">Compositore</xsl:when>
		<xsl:when test="$value='233'">Compositore della musica parafrasata</xsl:when>
		<xsl:when test="$value='235'">Compositore della musica parafrasata</xsl:when>
		<xsl:when test="$value='236'">Autore dell'opera in cui va inserito il brano</xsl:when>
		<xsl:when test="$value='245'">Ideatore</xsl:when>
		<xsl:when test="$value='250'">Direttore d'orchestra</xsl:when>
		<xsl:when test="$value='255'">Consulente di progetto</xsl:when>
		<xsl:when test="$value='257'">Continuatore</xsl:when>
		<xsl:when test="$value='260'">Titolare dei diritti d'autore</xsl:when>
		<xsl:when test="$value='270'">Correttore</xsl:when>
		<xsl:when test="$value='273'">Curatore di mostra</xsl:when>
		<xsl:when test="$value='275'">Ballerino</xsl:when>
		<xsl:when test="$value='280'">Dedicatario</xsl:when>
		<xsl:when test="$value='290'">Dedicante</xsl:when>
		<xsl:when test="$value='295'">Conferitore di titolo accademico</xsl:when>
		<xsl:when test="$value='300'">Regista</xsl:when>
		<xsl:when test="$value='305'">Autore della dissertazione</xsl:when>
		<xsl:when test="$value='310'">Distributore</xsl:when>
		<xsl:when test="$value='320'">Provenienza</xsl:when>
		<xsl:when test="$value='330'">Autore incerto</xsl:when>
		<xsl:when test="$value='340'">Curatore</xsl:when>
		<xsl:when test="$value='350'">Incisore</xsl:when>
		<xsl:when test="$value='360'">Acquafortista</xsl:when>
		<xsl:when test="$value='365'">Perito</xsl:when>
		<xsl:when test="$value='370'">Montatore di film</xsl:when>
		<xsl:when test="$value='380'">Falsificatore</xsl:when>
		<xsl:when test="$value='390'">Possessore</xsl:when>
		<xsl:when test="$value='395'">Fondatore</xsl:when>
		<xsl:when test="$value='400'">Finanziatore</xsl:when>
		<xsl:when test="$value='410'">Grafico</xsl:when>
		<xsl:when test="$value='420'">Onorato</xsl:when>
		<xsl:when test="$value='430'">Miniatore</xsl:when>
		<xsl:when test="$value='440'">Illustratore</xsl:when>
		<xsl:when test="$value='445'">Impresario</xsl:when>
		<xsl:when test="$value='450'">Firmatario del dono</xsl:when>
		<xsl:when test="$value='460'">Intervistato</xsl:when>
		<xsl:when test="$value='470'">Intervistatore</xsl:when>
		<xsl:when test="$value='475'">Ente emittente</xsl:when>
		<xsl:when test="$value='480'">Librettista</xsl:when>
		<xsl:when test="$value='490'">Licenziatario</xsl:when>
		<xsl:when test="$value='500'">Licenziatore</xsl:when>
		<xsl:when test="$value='510'">Litografo</xsl:when>
		<xsl:when test="$value='520'">Paroliere, autore della poesia</xsl:when>
		<xsl:when test="$value='530'">Incisore su metallo</xsl:when>
		<xsl:when test="$value='540'">Garante</xsl:when>
		<xsl:when test="$value='550'">Narratore</xsl:when>
		<xsl:when test="$value='557'">Organizzatore di meeting</xsl:when>
		<xsl:when test="$value='560'">Autore dell'inchiesta</xsl:when>
		<xsl:when test="$value='570'">Altro</xsl:when>
		<xsl:when test="$value='580'">Cartaro</xsl:when>
		<xsl:when test="$value='587'">Detentore del brevetto</xsl:when>
		<xsl:when test="$value='590'">Interprete</xsl:when>
		<xsl:when test="$value='595'">Ente di ricerca</xsl:when>
		<xsl:when test="$value='600'">Fotografo</xsl:when>
		<xsl:when test="$value='605'">Presentatore</xsl:when>
		<xsl:when test="$value='610'">Stampatore</xsl:when>
		<xsl:when test="$value='620'">Stampatore delle tavole</xsl:when>
		<xsl:when test="$value='630'">Produttore</xsl:when>
		<xsl:when test="$value='632'">Scenografo</xsl:when>
		<xsl:when test="$value='633'">Personale di produzione</xsl:when>
		<xsl:when test="$value='635'">Programmatore</xsl:when>
		<xsl:when test="$value='637'">Project manager</xsl:when>
		<xsl:when test="$value='640'">Correttore di bozze</xsl:when>
		<xsl:when test="$value='650'">Editore</xsl:when>
		<xsl:when test="$value='651'">Direttore editoriale</xsl:when>
		<xsl:when test="$value='660'">Destinatario</xsl:when>
		<xsl:when test="$value='665'">Produttore discografico</xsl:when>
		<xsl:when test="$value='670'">Tecnico della registrazione</xsl:when>
		<xsl:when test="$value='673'">Direttore della ricerca</xsl:when>
		<xsl:when test="$value='675'">Recensore</xsl:when>
		<xsl:when test="$value='677'">Membro del gruppo di ricerca</xsl:when>
		<xsl:when test="$value='690'">Sceneggiatore</xsl:when>
		<xsl:when test="$value='695'">Consulente scientifico</xsl:when>
		<xsl:when test="$value='700'">Copista, amanuense</xsl:when>
		<xsl:when test="$value='705'">Scultore</xsl:when>
		<xsl:when test="$value='710'">Segretario, portavoce</xsl:when>
		<xsl:when test="$value='720'">Firmatario</xsl:when>
		<xsl:when test="$value='721'">Cantante</xsl:when>
		<xsl:when test="$value='723'">Committente</xsl:when>
		<xsl:when test="$value='725'">Ente di standardizzazione</xsl:when>
		<xsl:when test="$value='727'">Relatore di tesi</xsl:when>
		<xsl:when test="$value='730'">Traduttore</xsl:when>
		<xsl:when test="$value='740'">Disegnatore dei caratteri</xsl:when>
		<xsl:when test="$value='750'">Tipografo</xsl:when>
		<xsl:when test="$value='753'">Venditore</xsl:when>
		<xsl:when test="$value='760'">Xilografo</xsl:when>
		<xsl:when test="$value='770'">Autore dell'allegato</xsl:when>
		<xsl:when test="$value='900'">Inventore</xsl:when>
		<xsl:when test="$value='901'">Disegnatore</xsl:when>
		<xsl:when test="$value='904'">Costumista</xsl:when>
		<xsl:when test="$value='906'">Strumentista</xsl:when>
		<xsl:when test="$value='909'">Doppiatore</xsl:when>
		<xsl:when test="$value='910'">Telecineoperatore</xsl:when>
		<xsl:when test="$value='911'">Relatore, oratore</xsl:when>
        <xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
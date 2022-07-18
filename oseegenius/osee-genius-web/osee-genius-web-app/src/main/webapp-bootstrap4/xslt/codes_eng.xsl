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
		<xsl:when test="$value='0'">Partial Digitization</xsl:when>
		<xsl:when test="$value='1'">Digital Resource</xsl:when>
		<xsl:when test="$value='2'">Digital Resource</xsl:when>
		<xsl:otherwise>Digital Resource</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<xsl:template name="label_956">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='SBN3'">Google-BNCF Project</xsl:when>
		<xsl:when test="$value='SBN4'">Photos of 1966 flood</xsl:when>
		<xsl:otherwise>Digital Resource</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	
	<xsl:template name="labels5xx">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='500'">Uniform title:</xsl:when>
		<xsl:when test="$value='510'">Parallel Title:</xsl:when>
		<xsl:when test="$value='517'">Other variant titles:</xsl:when>
		<xsl:when test="$value='530'">Key title:</xsl:when>
		<xsl:when test="$value='560'">Artificial title:</xsl:when>
		<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
	</xsl:template>
  
	<xsl:template name="labels4xx">
	<xsl:param name="value" />
	<xsl:param name="hrc" />
	<xsl:choose>
		<xsl:when test="$value='410'">Series:</xsl:when>
		<xsl:when test="$value='421'">Supplement:</xsl:when>
		<xsl:when test="$value='422'">Parent of Supplement:</xsl:when>
		<xsl:when test="$value='423'">Issued with:</xsl:when>
		<xsl:when test="$value='430'">Continues:</xsl:when>
		<xsl:when test="$value='431'">Continues in Part:</xsl:when>
		<xsl:when test="$value='434'">Absorbed:</xsl:when>
		<xsl:when test="$value='440'">Continued by:</xsl:when>
		<xsl:when test="$value='441'">Continued in Part by:</xsl:when>
		<xsl:when test="$value='447'">Merged to Form:</xsl:when>
		<xsl:when test="$value='451'">Other Edition in Same Medium:</xsl:when>
		<xsl:when test="$value='454'">Translation of:</xsl:when>
		<xsl:when test="$value='461'">Set Level:</xsl:when>
		<xsl:when test="$value='462'">
			<xsl:if test="$hrc='2'">
				Subset Level:
			</xsl:if>
			<xsl:if test="not($hrc='2')">
				Subset Level:
			</xsl:if>
		</xsl:when>
		<xsl:when test="$value='463'">Piece Level:</xsl:when>
		<xsl:when test="$value='464'">Piece-Analytic Level:</xsl:when>
		<xsl:when test="$value='488'">Other Related Works:</xsl:when>
		<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<xsl:template name="labels225">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='225'">Series:</xsl:when>
		<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<xsl:template name="code101">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='ita'">Italian</xsl:when>
		<xsl:when test="$value='eng'">English</xsl:when>
		<xsl:when test="$value='ara'">Arabic</xsl:when>
		<xsl:when test="$value='arc'">Aramaic</xsl:when>
		<xsl:when test="$value='arm'">Armenian</xsl:when>
		<xsl:when test="$value='bul'">Bulgarian</xsl:when>
		<xsl:when test="$value='cat'">Catalan</xsl:when>
		<xsl:when test="$value='che'">Chechen</xsl:when>
		<xsl:when test="$value='chi'">Chinese</xsl:when>
		<xsl:when test="$value='cop'">Coptic</xsl:when>
		<xsl:when test="$value='dan'">Danish</xsl:when>
		<xsl:when test="$value='heb'">Hebrew</xsl:when>
		<xsl:when test="$value='esp'">Esperanto</xsl:when>
		<xsl:when test="$value='fin'">Finnish</xsl:when>
		<xsl:when test="$value='fre'">French</xsl:when>
		<xsl:when test="$value='fro'">Franch Old (842-1400 ca.)</xsl:when>
		<xsl:when test="$value='frm'">Francese Middle (1400-1600 ca.)</xsl:when>
		<xsl:when test="$value='jpn'">Japanese</xsl:when>
		<xsl:when test="$value='grc'">Greco Ancient (until 1453)</xsl:when>
		<xsl:when test="$value='gre'">Greco Modern (1453- )</xsl:when>
		<xsl:when test="$value='enm'">English Middle (1100-1500 ca)</xsl:when>
		<xsl:when test="$value='lat'">Latin</xsl:when>
		<xsl:when test="$value='lin'">Lingala</xsl:when>
		<xsl:when test="$value='lit'">Lithuanian</xsl:when>
		<xsl:when test="$value='mul'">Multiple languages</xsl:when>
		<xsl:when test="$value='nor'">Norwegian</xsl:when>
		<xsl:when test="$value='dut'">Dutch</xsl:when>
		<xsl:when test="$value='per'">Persian</xsl:when>
		<xsl:when test="$value='pol'">Polish</xsl:when>
		<xsl:when test="$value='por'">Portuguese</xsl:when>
		<xsl:when test="$value='pro'">Provençal (until 1500)</xsl:when>
		<xsl:when test="$value='roh'">Raeto-Romance</xsl:when>
		<xsl:when test="$value='rus'">Russian</xsl:when>
		<xsl:when test="$value='san'">Sanskrit</xsl:when>
		<xsl:when test="$value='scc'">Serbian</xsl:when>
		<xsl:when test="$value='scr'">Croatian</xsl:when>
		<xsl:when test="$value='sla'">Slavic (Other)</xsl:when>
		<xsl:when test="$value='slo'">Slovak</xsl:when>
		<xsl:when test="$value='slv'">Slovenian</xsl:when>
		<xsl:when test="$value='spa'">Spanish</xsl:when>
		<xsl:when test="$value='swe'">Swedish</xsl:when>
		<xsl:when test="$value='ger'">German</xsl:when>
		<xsl:when test="$value='tib'">Tibetan</xsl:when>
		<xsl:when test="$value='tur'">Turkish</xsl:when>
		<xsl:when test="$value='hun'">Hungarian</xsl:when>
        <xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  
  <xsl:template name="code102">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='it'">Italy</xsl:when>
		<xsl:when test="$value='gb'">United Kingdom</xsl:when>
		<xsl:when test="$value='za'">South Africa</xsl:when>
		<xsl:when test="$value='al'">Albania</xsl:when>
		<xsl:when test="$value='sa'">Saudi Arabia</xsl:when>
		<xsl:when test="$value='ar'">Argentina</xsl:when>
		<xsl:when test="$value='at'">Austria</xsl:when>
		<xsl:when test="$value='be'">Belgium</xsl:when>
		<xsl:when test="$value='bo'">Bolivia</xsl:when>
		<xsl:when test="$value='br'">Brazil</xsl:when>
		<xsl:when test="$value='bg'">Bulgaria</xsl:when>
		<xsl:when test="$value='ca'">Canada</xsl:when>
		<xsl:when test="$value='cz'">Czechoslovakia</xsl:when>
		<xsl:when test="$value='cn'">China</xsl:when>
		<xsl:when test="$value='co'">Colombia</xsl:when>
		<xsl:when test="$value='cr'">Costa Rica</xsl:when>
		<xsl:when test="$value='cu'">Cuba</xsl:when>
		<xsl:when test="$value='dk'">Denmark</xsl:when>
		<xsl:when test="$value='et'">Ethiopia</xsl:when>
		<xsl:when test="$value='fi'">Finland</xsl:when>
		<xsl:when test="$value='fr'">France</xsl:when>
		<xsl:when test="$value='de'">Germany</xsl:when>
		<xsl:when test="$value='jp'">Japan</xsl:when>
		<xsl:when test="$value='gr'">Greece</xsl:when>
		<xsl:when test="$value='in'">India</xsl:when>
		<xsl:when test="$value='ir'">Iran</xsl:when>
		<xsl:when test="$value='ie'">Ireland</xsl:when>
		<xsl:when test="$value='il'">Israel</xsl:when>
		<xsl:when test="$value='yu'">Yugoslavia</xsl:when>
		<xsl:when test="$value='li'">Liechtenstein</xsl:when>
		<xsl:when test="$value='lu'">Luxembourg</xsl:when>
		<xsl:when test="$value='mg'">Madagascar</xsl:when>
		<xsl:when test="$value='my'">Maleysia</xsl:when>
		<xsl:when test="$value='mt'">Malta</xsl:when>
		<xsl:when test="$value='ma'">Morocco</xsl:when>
		<xsl:when test="$value='mx'">Mexico</xsl:when>
		<xsl:when test="$value='mc'">Monaco</xsl:when>
		<xsl:when test="$value='ne'">Niger</xsl:when>
		<xsl:when test="$value='ng'">Nigeria</xsl:when>
		<xsl:when test="$value='no'">Norvegia</xsl:when>
		<xsl:when test="$value='pe'">Peru</xsl:when>
		<xsl:when test="$value='pl'">Polond</xsl:when>
		<xsl:when test="$value='pt'">Portugal</xsl:when>
		<xsl:when test="$value='ro'">Romania</xsl:when>
		<xsl:when test="$value='sm'">San Marino</xsl:when>
		<xsl:when test="$value='so'">Somalia</xsl:when>
		<xsl:when test="$value='es'">Spain</xsl:when>
		<xsl:when test="$value='us'">United States</xsl:when>
		<xsl:when test="$value='se'">Sweden</xsl:when>
		<xsl:when test="$value='ch'">Switzerland</xsl:when>
		<xsl:when test="$value='tr'">Turkey</xsl:when>
		<xsl:when test="$value='ua'">Ukraine</xsl:when>
		<xsl:when test="$value='ru'">Russian Federation</xsl:when>
		<xsl:when test="$value='uy'">Uruguay</xsl:when>
		<xsl:when test="$value='ve'">Venezuela</xsl:when>
		<xsl:when test="$value='va'">Vatican City State</xsl:when>
		<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  
  <xsl:template name="code7xx">
	<xsl:param name="value" />
	<xsl:choose>
		<xsl:when test="$value='005'">Actor</xsl:when>
		<xsl:when test="$value='010'">Adapter</xsl:when>
		<xsl:when test="$value='018'">Animator</xsl:when>
		<xsl:when test="$value='020'">Annotator</xsl:when>
		<xsl:when test="$value='030'">Arranger</xsl:when>
		<xsl:when test="$value='040'">Artist</xsl:when>
		<xsl:when test="$value='050'">Assignee</xsl:when>
		<xsl:when test="$value='060'">Associated name</xsl:when>
		<xsl:when test="$value='065'">Auctioneer</xsl:when>
		<xsl:when test="$value='070'">Author</xsl:when>
		<xsl:when test="$value='072'">Author in quotations or text extracts</xsl:when>
		<xsl:when test="$value='075'">Author of afterword, postface, colophon, etc.</xsl:when>
		<xsl:when test="$value='080'">Author of introduction, etc</xsl:when>
		<xsl:when test="$value='090'">Author of dialogue</xsl:when>
		<xsl:when test="$value='100'">Bibliographic antecedent</xsl:when>
		<xsl:when test="$value='110'">Binder</xsl:when>
		<xsl:when test="$value='120'">Binding designer</xsl:when>
		<xsl:when test="$value='130'">Book designer</xsl:when>
		<xsl:when test="$value='140'">Bookjacket designer</xsl:when>
		<xsl:when test="$value='160'">Bookseller</xsl:when>
		<xsl:when test="$value='170'">Calligrapher</xsl:when>
		<xsl:when test="$value='180'">Cartographer</xsl:when>
		<xsl:when test="$value='190'">Censor</xsl:when>
		<xsl:when test="$value='195'">Choral director</xsl:when>
		<xsl:when test="$value='200'">Choreographer</xsl:when>
		<xsl:when test="$value='205'">Collaborator</xsl:when>
		<xsl:when test="$value='206'">Collector of field material</xsl:when>
		<xsl:when test="$value='207'">Comedian</xsl:when>
		<xsl:when test="$value='210'">Commentator</xsl:when>
		<xsl:when test="$value='212'">Commentator for written text</xsl:when>
		<xsl:when test="$value='220'">Compilatore</xsl:when>
		<xsl:when test="$value='230'">Compositore</xsl:when>
		<xsl:when test="$value='233'">Composer of adapted work</xsl:when>
		<xsl:when test="$value='235'">Composer of adapted work</xsl:when>
		<xsl:when test="$value='236'">Composer of main musical work</xsl:when>
		<xsl:when test="$value='245'">Conceptor</xsl:when>
		<xsl:when test="$value='250'">Conductor</xsl:when>
		<xsl:when test="$value='255'">Consultant to a project</xsl:when>
		<xsl:when test="$value='257'">Continuator</xsl:when>
		<xsl:when test="$value='260'">Copyright holder</xsl:when>
		<xsl:when test="$value='270'">Corrector</xsl:when>
		<xsl:when test="$value='273'">Curator of an exhibition</xsl:when>
		<xsl:when test="$value='275'">Dancer</xsl:when>
		<xsl:when test="$value='280'">Dedicatee</xsl:when>
		<xsl:when test="$value='290'">Dedicator</xsl:when>
		<xsl:when test="$value='295'">Degree-grantor</xsl:when>
		<xsl:when test="$value='300'">Director</xsl:when>
		<xsl:when test="$value='305'">Dissertant</xsl:when>
		<xsl:when test="$value='310'">Distributor</xsl:when>
		<xsl:when test="$value='320'">Donor</xsl:when>
		<xsl:when test="$value='330'">Dubious authoro</xsl:when>
		<xsl:when test="$value='340'">Editor</xsl:when>
		<xsl:when test="$value='350'">Engraver</xsl:when>
		<xsl:when test="$value='360'">Etcher</xsl:when>
		<xsl:when test="$value='365'">Expert</xsl:when>
		<xsl:when test="$value='370'">Film editor</xsl:when>
		<xsl:when test="$value='380'">Forger</xsl:when>
		<xsl:when test="$value='390'">Former owner</xsl:when>
		<xsl:when test="$value='395'">Founder</xsl:when>
		<xsl:when test="$value='400'">Sponsor</xsl:when>
		<xsl:when test="$value='410'">Graphic technician</xsl:when>
		<xsl:when test="$value='420'">Honoree</xsl:when>
		<xsl:when test="$value='430'">Illuminator</xsl:when>
		<xsl:when test="$value='440'">Illustrator</xsl:when>
		<xsl:when test="$value='445'">Impresario</xsl:when>
		<xsl:when test="$value='450'">Inscriber</xsl:when>
		<xsl:when test="$value='460'">Interviewee</xsl:when>
		<xsl:when test="$value='470'">Interviewer</xsl:when>
		<xsl:when test="$value='475'">Issuing body</xsl:when>
		<xsl:when test="$value='480'">Librettist</xsl:when>
		<xsl:when test="$value='490'">Licensee</xsl:when>
		<xsl:when test="$value='500'">Licensor</xsl:when>
		<xsl:when test="$value='510'">Lithographer</xsl:when>
		<xsl:when test="$value='520'">Lyricist</xsl:when>
		<xsl:when test="$value='530'">Metal-engraver</xsl:when>
		<xsl:when test="$value='540'">Monitor</xsl:when>
		<xsl:when test="$value='550'">Narrator</xsl:when>
		<xsl:when test="$value='557'">Organiser of meeting</xsl:when>
		<xsl:when test="$value='560'">Originator</xsl:when>
		<xsl:when test="$value='570'">Other</xsl:when>
		<xsl:when test="$value='580'">Papermaker</xsl:when>
		<xsl:when test="$value='587'">Patentee</xsl:when>
		<xsl:when test="$value='590'">Performer</xsl:when>
		<xsl:when test="$value='595'">Performer of research</xsl:when>
		<xsl:when test="$value='600'">Photographer</xsl:when>
		<xsl:when test="$value='605'">Presenter</xsl:when>
		<xsl:when test="$value='610'">Printer</xsl:when>
		<xsl:when test="$value='620'">Printer of plates</xsl:when>
		<xsl:when test="$value='630'">Producer</xsl:when>
		<xsl:when test="$value='632'">Production designer</xsl:when>
		<xsl:when test="$value='633'">Production personnel</xsl:when>
		<xsl:when test="$value='635'">Programmer</xsl:when>
		<xsl:when test="$value='637'">Project manager</xsl:when>
		<xsl:when test="$value='640'">Proof-reader</xsl:when>
		<xsl:when test="$value='650'">Publisher</xsl:when>
		<xsl:when test="$value='651'">Publishing director</xsl:when>
		<xsl:when test="$value='660'">Recipient of letters</xsl:when>
		<xsl:when test="$value='665'">Record producer</xsl:when>
		<xsl:when test="$value='670'">Recording engineer</xsl:when>
		<xsl:when test="$value='673'">Research team head</xsl:when>
		<xsl:when test="$value='675'">Reviewer</xsl:when>
		<xsl:when test="$value='677'">Research team member</xsl:when>
		<xsl:when test="$value='690'">Scenarist</xsl:when>
		<xsl:when test="$value='695'">Scientific advisor</xsl:when>
		<xsl:when test="$value='700'">Scribe</xsl:when>
		<xsl:when test="$value='705'">Sculptor</xsl:when>
		<xsl:when test="$value='710'">Secretary</xsl:when>
		<xsl:when test="$value='720'">Signer</xsl:when>
		<xsl:when test="$value='721'">Singer</xsl:when>
		<xsl:when test="$value='723'">Sponsor</xsl:when>
		<xsl:when test="$value='725'">Standards body</xsl:when>
		<xsl:when test="$value='727'">Thesis advisor</xsl:when>
		<xsl:when test="$value='730'">Translator</xsl:when>
		<xsl:when test="$value='740'">Type designer</xsl:when>
		<xsl:when test="$value='750'">Typographer</xsl:when>
		<xsl:when test="$value='753'">Vendor</xsl:when>
		<xsl:when test="$value='760'">Wood-engraver</xsl:when>
		<xsl:when test="$value='770'">Writer of accompanying material</xsl:when>
		<xsl:when test="$value='900'">Inventor</xsl:when>
		<xsl:when test="$value='901'">Drawer</xsl:when>
		<xsl:when test="$value='904'">Costume designer</xsl:when>
		<xsl:when test="$value='906'">Instrumentalist</xsl:when>
		<xsl:when test="$value='909'">Dubber</xsl:when>
		<xsl:when test="$value='910'">Cameraman</xsl:when>
		<xsl:when test="$value='911'">Speaker</xsl:when>
        <xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
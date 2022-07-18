<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="1.0"
	xmlns:marc="http://www.loc.gov/MARC21/slim"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:str="http://exslt.org/strings"
	exclude-result-prefixes="marc str">
	<xsl:import href="utils.xsl"/>
	
	<xsl:output method="html" indent="yes"/>
 
	<xsl:template match="/">
		<div>
			<xsl:apply-templates select="//record"/>
		</div>
	</xsl:template>
	
	<xsl:template match="record">
		
		<xsl:call-template name="tag_205" />
		<xsl:if test="datafield[@tag=205]">
			<xsl:if test="datafield[@tag=210]">
				<span> | </span>
			</xsl:if>
		</xsl:if>
		<xsl:if test="datafield[@tag=210]">
			<xsl:call-template name="tag_210" />
		</xsl:if>
		<xsl:if test="datafield[@tag=205 or @tag=210]">
			<br/>
		</xsl:if>
		
		<xsl:call-template name="tag_215" />
		<xsl:if test="datafield[@tag=215]">
			<xsl:if test="datafield[@tag=225]">
				<span> | </span>
			</xsl:if>
		</xsl:if>
		<xsl:call-template name="many_225">
			<xsl:with-param name="tag">225</xsl:with-param>
			<xsl:with-param name="label">Series</xsl:with-param>
			<xsl:with-param name="spanclass">serie</xsl:with-param>
		</xsl:call-template>
		
		<xsl:if test="datafield[@tag=215 or @tag=225]">
			<br/>
		</xsl:if>

		<xsl:call-template name="tag_fmt" />
		<xsl:if test="datafield[@tag=977]">
			<span> | </span>
			<xsl:call-template name="biblioteche_977" />
		</xsl:if>
		
	</xsl:template>
	
	
	
	<!-- Template ausiliari -->
	
	<xsl:template name="tag_210">
    <span class="results_summary publication">
      <span class="labelDati">Publication: </span>
      <xsl:for-each select="datafield[@tag=210]">
        <span>
		<xsl:call-template name="addClassRtl" />
		
		<xsl:for-each select="subfield[(@code='a') or (@code='b') or (@code='c') or (@code='d')]">
			<xsl:choose>
				<xsl:when test="@code='c'">
					<xsl:if test="position()>1">
						<xsl:text> : </xsl:text>
					</xsl:if>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="position()>1">
						<xsl:text>, </xsl:text>
					</xsl:if>
					<xsl:value-of select="."/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		
		<xsl:if test="subfield[@code='e'] or subfield[@code='f'] or subfield[@code='g'] or subfield[@code='h']">
			<xsl:text> (</xsl:text>
		</xsl:if>
		<xsl:for-each select="subfield[(@code='e') or (@code='f') or (@code='g') or (@code='h')]">
			<xsl:choose>
				<xsl:when test="@code='g'">
					<xsl:if test="position()>1">
						<xsl:text> : </xsl:text>
					</xsl:if>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="position()>1">
						<xsl:text>, </xsl:text>
					</xsl:if>
					<xsl:value-of select="."/>
				</xsl:otherwise>
			</xsl:choose>			
		</xsl:for-each>
		<xsl:if test="subfield[@code='e'] or subfield[@code='f'] or subfield[@code='g'] or subfield[@code='h']">
			<xsl:text>)</xsl:text>
		</xsl:if>
		
		<xsl:if test="not (position() = last())">
			<xsl:text>. </xsl:text>
		</xsl:if>
        </span>
      </xsl:for-each>
    </span>
	</xsl:template>
	
	<xsl:template name="tag_215">
    <xsl:for-each select="datafield[@tag=215]">
	  <span class="results_summary description">
        <span class="labelDati">Description: </span>
        <xsl:if test="subfield[@code='a']">
          <xsl:value-of select="subfield[@code='a']"/>
        </xsl:if>
        <xsl:if test="subfield[@code='c']"> :
          <xsl:value-of select="subfield[@code='c']"/>
        </xsl:if>
        <xsl:if test="subfield[@code='d']"> ;
          <xsl:value-of select="subfield[@code='d']"/>
        </xsl:if>
        <xsl:if test="subfield[@code='e']"> +
          <xsl:value-of select="subfield[@code='e']"/>
        </xsl:if>
      </span> 
    </xsl:for-each>
	</xsl:template>

	<xsl:template name="tag_205">
    <xsl:for-each select="datafield[@tag=205]">
	  <span class="results_summary edit">
        <span class="labelDati">Edition: </span>
        <xsl:if test="subfield[@code='a']">
          <xsl:value-of select="subfield[@code='a']"/>
        </xsl:if>
        <xsl:if test="subfield[@code='d']"> =
          <xsl:value-of select="subfield[@code='d']"/>
        </xsl:if>
        <xsl:if test="subfield[@code='f']"> /
          <xsl:value-of select="subfield[@code='f']"/>
        </xsl:if>
        <xsl:if test="subfield[@code='g']"> ;
          <xsl:value-of select="subfield[@code='g']"/>
        </xsl:if>
		<xsl:if test="subfield[@code='b']"> ,
          <xsl:value-of select="subfield[@code='b']"/>
        </xsl:if>
      </span> 
    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="tag_fmt">
		<span class="labelDati">Format: </span>
		<xsl:for-each select="datafield[@tag='FMT']">
			<span class="results_summary bibformat">
				<xsl:if test="subfield[@code='a']">
					<xsl:variable name="formatrec" select="subfield[@code='a']"/>
					<xsl:choose>
						<xsl:when test="$formatrec='CO'">Collection</xsl:when>
						<xsl:when test="$formatrec='DI'">Digitization</xsl:when>
						<xsl:when test="$formatrec='SE'">Serial</xsl:when>
						<xsl:when test="$formatrec='TD'">Phd degree</xsl:when>
						<xsl:when test="$formatrec='BK'">Book</xsl:when>
						<xsl:when test="$formatrec='MN'">Manuscript</xsl:when>
						<xsl:when test="$formatrec='MS'">Printed score</xsl:when>
						<xsl:when test="$formatrec='MM'">Manuscript scores</xsl:when>
						<xsl:when test="$formatrec='MP'">Maps</xsl:when>
						<xsl:when test="$formatrec='VD'">Video</xsl:when>
						<xsl:when test="$formatrec='AU'">Audiobook</xsl:when>
						<xsl:when test="$formatrec='MU'">Music</xsl:when>
						<xsl:when test="$formatrec='GR'">Graphic</xsl:when>
						<xsl:when test="$formatrec='ED'">CD and DVD</xsl:when>
						<xsl:when test="$formatrec='ML'">Multimedia</xsl:when>
						<xsl:when test="$formatrec='OG'">Object</xsl:when>
						<xsl:when test="$formatrec='AR'">Digital articles</xsl:when>
					</xsl:choose>
				</xsl:if>
			</span>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="biblioteche_977">
    <span class="results_summary list_libraries">
      <span class="labelDati">Owned by: </span>
      <xsl:for-each select="datafield[@tag=977]">
        <span>
          <xsl:for-each select="subfield">
            <xsl:if test="@code='a'">
            <xsl:variable name="library" select="."/>
			<xsl:choose>
				<xsl:when test="$library=' AM'">Biblioteca dell'Istituto di scienze militari aeronautiche</xsl:when>
				<xsl:when test="$library=' CF'">Biblioteca nazionale centrale di Firenze</xsl:when>
				<xsl:when test="$library=' EC'">Biblioteca dell'Eremo di Camaldoli</xsl:when>
				<xsl:when test="$library=' FC'">Biblioteca del Conservatorio Luigi Cherubini</xsl:when>
				<xsl:when test="$library=' FT'">Biblioteca Fondazione Turati e Associazione Pertini</xsl:when>
				<xsl:when test="$library=' IG'">Biblioteca Attilio Mori dell'Istituto geografico militare</xsl:when>
				<xsl:when test="$library=' MF'">Biblioteca Marucelliana</xsl:when>
				<xsl:when test="$library=' ML'">Biblioteca Medicea Laurenziana</xsl:when>
				<xsl:when test="$library=' RF'">Biblioteca Riccardiana</xsl:when>
				<xsl:when test="$library=' SA'">Biblioteca del Museo Archeologico Nazionale di Firenze</xsl:when>
				<xsl:when test="$library=' SC'">Biblioteca di Santa Croce</xsl:when>
				<xsl:when test="$library=' SE'">Biblioteca dell'Istituto Nazionale di Studi Etruschi ed Italici</xsl:when>
				<xsl:when test="$library=' SL'">Biblioteca dell'archivio della Basilica di San Lorenzo</xsl:when>
			</xsl:choose>
			<xsl:if test="not (position() = last())">
				<xsl:if test="position()>0">
					<xsl:text> | </xsl:text>
				</xsl:if>
			</xsl:if>
		  </xsl:if>
          </xsl:for-each>
        </span>
      </xsl:for-each>
    </span>
  </xsl:template>
	
	
	
</xsl:stylesheet>
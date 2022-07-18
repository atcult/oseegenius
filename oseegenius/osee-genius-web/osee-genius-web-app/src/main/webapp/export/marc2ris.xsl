<xsl:stylesheet version="1.0" xmlns:marc="http://www.loc.gov/MARC21/slim" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" encoding="UTF-8" indent="no" omit-xml-declaration="yes"/>
	<xsl:param name="baseurl" />
	<xsl:template match="/">
		<xsl:apply-templates select="marc:record"/>
	</xsl:template>
	<xsl:template match="marc:record">
		<xsl:call-template name="TY"/>
		<xsl:call-template name="TI"/>
		<xsl:call-template name="AU"/>
		<xsl:call-template name="T3"/>		
		<xsl:call-template name="VL"/>			
		<xsl:call-template name="SN"/>		
		<xsl:call-template name="PB"/>			
		<xsl:call-template name="PY"/>	
		<xsl:call-template name="CY"/>			
		<xsl:call-template name="PY"/>			
		<xsl:call-template name="KW"/>		
		<xsl:call-template name="ET"/>	
		<xsl:call-template name="N1"/>	
		<xsl:call-template name="AV"/>				
		<xsl:call-template name="UR"/>								
	</xsl:template>
	
	<xsl:template name="TY">
		<xsl:variable name="leader" select="marc:leader/text()"/>
		<xsl:variable name="leader5" select="substring($leader,6,1)"/><!-- record status -->
		<xsl:variable name="leader6" select="substring($leader,7,1)"/><!-- type of record -->
		<xsl:variable name="leader7" select="substring($leader,8,1)"/><!-- bibliographic level: mono, series, etc. -->
		<xsl:variable name="leader8" select="substring($leader,9,1)"/><!-- type of control -->
		<xsl:variable name="leader9" select="substring($leader,10,1)"/><!-- character encoding a=UTF-8; blank = MARC-8 -->
		<xsl:variable name="leader17" select="substring($leader,18,1)"/><!-- encoding level -->
		<xsl:variable name="leader18" select="substring($leader,19,1)"/><!-- descriptive cataloguing form -->
		<xsl:variable name="leader19" select="substring($leader,20,1)"/><!-- linked record requirement -->
		<xsl:choose>
			<xsl:when test="$leader6='a'">
				<xsl:choose>
					<xsl:when test="$leader7='m'">TY  - BOOK</xsl:when>
					<xsl:when test="$leader7='s'">TY  - SER</xsl:when>
					<xsl:when test="$leader7='a' or $leader7='b'">TY  - JOUR</xsl:when>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$leader6='c' or $leader6='d'">TY  - MUSIC</xsl:when>
			<xsl:when test="$leader6='e' or $leader6='f'">TY  - MAP</xsl:when>
			<xsl:when test="$leader6='g'">TY  - ADVS</xsl:when>
			<xsl:when test="$leader6='i' or $leader6='j'">TY  - SOUND</xsl:when>
			<xsl:when test="$leader6='k' or $leader6='r'">TY  - ART</xsl:when>
			<xsl:when test="$leader6='m'">TY  - DATA</xsl:when>
			<xsl:when test="$leader6='o' or $leader6='p' or $leader6='t'">TY  - GEN</xsl:when>
			<xsl:otherwise>TY  - GEN</xsl:otherwise>
		</xsl:choose>	
	</xsl:template>
	
	<xsl:template name="AU">
		<xsl:for-each select="marc:datafield[@tag='100' or @tag='110' or @tag='111' or @tag='700' or @tag='710' or @tag='711']">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>AU  - </xsl:text>
			<xsl:for-each select="marc:subfield">
				<xsl:if test="position() > 1"><xsl:text> </xsl:text></xsl:if>
				<xsl:value-of select="normalize-space(./text())" />
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="TI">
		<xsl:text>&#13;&#10;</xsl:text><xsl:text>TI  - </xsl:text>
		<xsl:for-each select="marc:datafield[@tag='245']/marc:subfield">
			<xsl:if test="position() > 1"><xsl:text> </xsl:text></xsl:if>
			<xsl:value-of select="normalize-space(text())" />
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="T3">	
		<xsl:for-each select="marc:datafield[@tag='490']/marc:subfield[@code='a']">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>T3  - </xsl:text>
			<xsl:value-of select="normalize-space(translate(text(),';/:', ''))" />
		</xsl:for-each>
	</xsl:template>	
	
	<xsl:template name="VL">
		<xsl:for-each select="marc:datafield[@tag='490']/marc:subfield[@code='v']">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>VL  - </xsl:text>		
			<xsl:value-of select="normalize-space(text())" />
		</xsl:for-each>
	</xsl:template>	

	<xsl:template name="SN">
		<xsl:choose>
			<xsl:when test="marc:datafield[@tag='020']">
				<xsl:choose>
					<xsl:when test="marc:datafield[@tag='020']/marc:subfield[@code='a']">
						<xsl:text>&#13;&#10;</xsl:text><xsl:text>SN  - </xsl:text>
						<xsl:variable name="value" select="marc:datafield[@tag='020']/marc:subfield[@code='a']/text()"/>
						<xsl:value-of select="normalize-space($value)" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="marc:datafield[@tag='020']/marc:subfield[@code='z']">
							<xsl:text>&#13;&#10;</xsl:text><xsl:text>SN  - </xsl:text>
							<xsl:variable name="value" select="marc:datafield[@tag='020']/marc:subfield[@code='z']/text()"/>
							<xsl:value-of select="normalize-space($value)" />
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="marc:datafield[@tag='022']/marc:subfield[@code='a']">
					<xsl:text>&#13;&#10;</xsl:text><xsl:text>SN  - </xsl:text>
					<xsl:variable name="value" select="marc:datafield[@tag='022']/marc:subfield[@code='a']/text()"/>
					<xsl:value-of select="normalize-space($value)" />
				</xsl:if>	
			</xsl:otherwise>	
		</xsl:choose>
	</xsl:template>
	<xsl:template name="PB">
		<xsl:if test="marc:datafield[@tag='260']/marc:subfield[@code='b']">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>PB  - </xsl:text>		
			<xsl:variable name="value" select="marc:datafield[@tag='260']/marc:subfield[@code='b']/text()"/>
			<xsl:value-of select="normalize-space(translate($value,',;/:', ''))" />
		</xsl:if>	
	</xsl:template>

	<xsl:template name="CY">
		<xsl:if test="marc:datafield[@tag='260']/marc:subfield[@code='a']">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>CY  - </xsl:text>		
			<xsl:variable name="place" select="marc:datafield[@tag='260']/marc:subfield[@code='a']/text()"/>
			<xsl:value-of select="normalize-space(translate($place,';/:', ''))" />
		</xsl:if>	
	</xsl:template>
	
	<xsl:template name="PY">
		<xsl:if test="marc:datafield[@tag='260']/marc:subfield[@code='c']">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>PY  - </xsl:text>	
			<xsl:variable name="value" select="marc:datafield[@tag='260']/marc:subfield[@code='c']/text()"/>
			<xsl:value-of select="normalize-space(translate($value,';/:', ''))" />
		</xsl:if>	
	</xsl:template>	
	
	<xsl:template name="KW">	
		<xsl:for-each select="marc:datafield[@tag='655']">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>KW  - </xsl:text>		
			<xsl:value-of select="normalize-space(marc:subfield[@code='a']/text())" />
			<xsl:if test="marc:subfield[@code='x']" >
				<xsl:text> -- </xsl:text>
				<xsl:value-of select="normalize-space(marc:subfield[@code='x']/text())" />
			</xsl:if>				
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="AV">	
		<xsl:for-each select="marc:datafield[@tag='852']/marc:subfield[@code='m']">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>AV  - </xsl:text>		
			<xsl:value-of select="normalize-space(./text())" />
		</xsl:for-each>
	</xsl:template>	
	
	<xsl:template name="UR">	
		<xsl:text>&#13;&#10;</xsl:text><xsl:text>UR  - </xsl:text>		
		<xsl:value-of select="$baseurl" /><xsl:value-of select="normalize-space(marc:controlfield[@tag='001']/text())" />
	</xsl:template>	
	
	<xsl:template name="ET">	
		<xsl:if test="marc:datafield[@tag='250']/marc:subfield[@code='a']">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>ET  - </xsl:text>		
			<xsl:value-of select="normalize-space(marc:datafield[@tag='250']/marc:subfield[@code='a']/text())" />
		</xsl:if>
	</xsl:template>	
	<xsl:template name="N1">	
		<xsl:for-each select="marc:datafield[starts-with(@tag, '5')]">
			<xsl:text>&#13;&#10;</xsl:text><xsl:text>N1  - </xsl:text>		
			<xsl:value-of select="normalize-space(marc:subfield[@code='a']/text())" />			
		</xsl:for-each>
	</xsl:template>
	
</xsl:stylesheet>			
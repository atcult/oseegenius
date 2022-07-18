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
		<xsl:if test="datafield[@tag=676]">
			<b><xsl:call-template name="tag_676" /></b>
		</xsl:if>
	</xsl:template>
	<xsl:template name="tag_676">
		<xsl:for-each select="datafield[@tag='676']">
			<xsl:for-each select="subfield">
				<xsl:choose>
					<xsl:when test="@code='a'">
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='v'">
						<xsl:text> (ed. </xsl:text>
						<xsl:value-of select="."/>
						<xsl:text>)</xsl:text>
					</xsl:when>
					<xsl:when test="@code='9'">
						<xsl:text> </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
			<xsl:if test="not (position() = last())">
				<br/>
			</xsl:if>
			
		</xsl:for-each>	
	</xsl:template>
	
	
</xsl:stylesheet>
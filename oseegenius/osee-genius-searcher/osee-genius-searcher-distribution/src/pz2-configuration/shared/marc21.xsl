<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:pz="http://www.indexdata.com/pazpar2/1.0"
	xmlns:marc="http://www.loc.gov/MARC21/slim">

	<xsl:output indent="yes" method="xml" version="1.0" encoding="UTF-8" />
	
	<xsl:template name="record-hook" />
	
	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="marc:record">
	<xsl:value-of select="."/>	
		<pz:record>
			<pz:metadata type="marcxml"><xsl:value-of select="/"/></pz:metadata>
			
			<xsl:choose>
				<xsl:when test="marc:controlfield[@tag='001']">
					<xsl:for-each select="marc:controlfield[@tag='001']">
						<pz:metadata type="id"><xsl:value-of select="." /></pz:metadata>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="marc:datafield[@tag='020']">
							<xsl:for-each select="marc:datafield[@tag='020'][1]">
								<pz:metadata type="id"><xsl:value-of select="normalize-space(.)" /></pz:metadata>
							</xsl:for-each>
						</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="marc:datafield[@tag='022'][1]">
							<pz:metadata type="id"><xsl:value-of select="normalize-space(.)" /></pz:metadata>
						</xsl:for-each>
					</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
			

			<xsl:for-each select="marc:datafield[@tag='020']">
				<pz:metadata type="isbn"><xsl:value-of select="marc:subfield[@code='a']" /></pz:metadata>
			</xsl:for-each>
			<xsl:for-each select="marc:datafield[@tag='022']">
				<pz:metadata type="issn"><xsl:value-of select="marc:subfield[@code='a']" /></pz:metadata>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='027']">
				<pz:metadata type="tech-rep-nr"><xsl:value-of select="marc:subfield[@code='a']" /></pz:metadata>
			</xsl:for-each>
			
			<xsl:for-each select="marc:datafield[@tag='100']">
				<pz:metadata type="author">
					<xsl:value-of select="marc:subfield[@code='a']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='q']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='b']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='c']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='d']" />
				</pz:metadata>
			</xsl:for-each>
			<xsl:for-each select="marc:datafield[@tag='110']">
				<pz:metadata type="corporate">
					<xsl:value-of select="marc:subfield[@code='a']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='b']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='d']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='c']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='q']" />
				</pz:metadata>
			</xsl:for-each>
			<xsl:for-each select="marc:datafield[@tag='111']">
				<pz:metadata type="conference">
					<xsl:value-of select="marc:subfield[@code='a']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='b']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='n']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='d']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='c']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='q']" />				
				</pz:metadata>
			</xsl:for-each>
			
			<xsl:for-each select="marc:datafield[@tag='210']">
				<pz:metadata type="abbreviated-title">
					<xsl:for-each select="marc:subfield">
						<xsl:if test="position() > 1">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="." />
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>			
			
			<xsl:for-each select="marc:datafield[@tag='222']">
				<pz:metadata type="key-title">
					<xsl:for-each select="marc:subfield">
						<xsl:if test="position() > 1">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="." />
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>			
			
			<xsl:for-each select="marc:datafield[@tag='242']">
				<pz:metadata type="cat-agency-translation-title">
					<xsl:for-each select="marc:subfield">
						<xsl:if test="position() > 1">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="." />
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>		

			<xsl:for-each select="marc:datafield[@tag='245']">
				<pz:metadata type="title-merge-key">
					<xsl:value-of select="marc:subfield[@code='a']" />
					<xsl:value-of select="marc:subfield[@code='b']" />
				</pz:metadata>
				<pz:metadata type="title">
					<xsl:for-each select="marc:subfield">
						<xsl:if test="position() > 1">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="." />
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>			
			
			<xsl:for-each select="marc:datafield[@tag='260']">
				<pz:metadata type="date">
					<xsl:value-of select="marc:subfield[@code='c']" />
				</pz:metadata>
				<pz:metadata type="publisher">
					<xsl:value-of select="marc:subfield[@code='a']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='b']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='c']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='e']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='f']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='g']" />
					<xsl:text> </xsl:text>										
				</pz:metadata>
				<pz:metadata type="publication-place">
                                        <xsl:value-of select="marc:subfield[@code='a']" />
                                </pz:metadata>

				<pz:metadata type="publisher-name">
					<xsl:value-of select="marc:subfield[@code='b']" />
				</pz:metadata>
				<pz:metadata type="publication-date">
					<xsl:value-of select="marc:subfield[@code='c']" />
				</pz:metadata>				
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='250']">
				<pz:metadata type="edition"><xsl:value-of select="marc:subfield[@code='a']" /></pz:metadata>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='300']">
					<pz:metadata type="physical-description">
						<xsl:for-each select="marc:subfield">
							<xsl:if test="position() > 1">
								<xsl:text> </xsl:text>
							</xsl:if>
							<xsl:value-of select="." />
						</xsl:for-each>
					</pz:metadata>	
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='440']">
				<pz:metadata type="series-title">
					<xsl:value-of select="marc:subfield[@code='a']" />
				</pz:metadata>
			</xsl:for-each>

			<xsl:for-each
				select="marc:datafield[@tag = '500' or @tag = '505' or
      		@tag = '518' or @tag = '520' or @tag = '522']">
				<pz:metadata type="description">
					<xsl:value-of select="*/text()" />
				</pz:metadata>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='600']">
				<pz:metadata type="subject_person">
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>
			
			<xsl:for-each select="marc:datafield[@tag='610']">
				<pz:metadata type="subject_corporate">
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>			

			<xsl:for-each select="marc:datafield[@tag='611']">
				<pz:metadata type="subject_conference"> 
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='630']">
				<pz:metadata type="subject_uniform_title"> 
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='648']">
				<pz:metadata type="chronological_subject"> 
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>
			<xsl:for-each select="marc:datafield[@tag='650']">
				<pz:metadata type="topical_subject"> 
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>
			<xsl:for-each select="marc:datafield[@tag='651']">
				<pz:metadata type="geographic_subject"> 
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>			
			<xsl:for-each select="marc:datafield[@tag='653']">
				<pz:metadata type="index_term"> 
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>	
			<xsl:for-each select="marc:datafield[@tag='655']">
				<pz:metadata type="genre_form_subject"> 
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>				
			
			<xsl:for-each
				select="marc:datafield[@tag='600' or @tag='610' or @tag='611' or @tag='630' or @tag='648' or @tag='650' or @tag='651' or @tag='653' or @tag='655']">
				<pz:metadata type="subject">
					<xsl:for-each select="marc:subfield">
						 <xsl:variable name="code" select="@code" />
						<xsl:if test="position() > 1">
							<xsl:text>, </xsl:text>
						</xsl:if>
				
						<xsl:if test="number($code) != $code">
							<xsl:value-of select="." />
						</xsl:if>
					</xsl:for-each>
				</pz:metadata>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='700']">
				<pz:metadata type="other-author">
					<xsl:value-of select="marc:subfield[@code='a']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='q']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='b']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='c']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='d']" />
				</pz:metadata>
			</xsl:for-each>
			<xsl:for-each select="marc:datafield[@tag='710']">
				<pz:metadata type="other-corporate">
					<xsl:value-of select="marc:subfield[@code='a']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='b']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='d']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='c']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='q']" />
				</pz:metadata>
			</xsl:for-each>
			<xsl:for-each select="marc:datafield[@tag='711']">
				<pz:metadata type="other-conference">
					<xsl:value-of select="marc:subfield[@code='a']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='b']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='n']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='d']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='c']" />
					<xsl:text> </xsl:text>
					<xsl:value-of select="marc:subfield[@code='q']" />				
				</pz:metadata>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='852']">
				<pz:metadata type="collocation">
					<xsl:value-of select="marc:subfield[@code='m']" />
				</pz:metadata>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='856']">
				<pz:metadata type="electronic-url">
					<xsl:value-of select="marc:subfield[@code='u']" />
				</pz:metadata>
				<pz:metadata type="electronic-text">
					<xsl:value-of select="marc:subfield[@code='y' or @code='3']" />
				</pz:metadata>
				<pz:metadata type="electronic-note">
					<xsl:value-of select="marc:subfield[@code='z']" />
				</pz:metadata>
				<pz:metadata type="electronic-format-instruction">
					<xsl:value-of select="marc:subfield[@code='i']" />
				</pz:metadata>
				<pz:metadata type="electronic-format-type">
					<xsl:value-of select="marc:subfield[@code='q']" />
				</pz:metadata>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='773']">
				<pz:metadata type="citation">
					<xsl:for-each select="*">
						<xsl:value-of select="normalize-space(.)" />
						<xsl:text> </xsl:text>
					</xsl:for-each>
				</pz:metadata>
				<xsl:if test="marc:subfield[@code='t']">
					<pz:metadata type="journal-title">
						<xsl:value-of select="marc:subfield[@code='t']" />
					</pz:metadata>
				</xsl:if>
				<xsl:if test="marc:subfield[@code='g']">
					<pz:metadata type="journal-subpart">
						<xsl:value-of select="marc:subfield[@code='g']" />
					</pz:metadata>
				</xsl:if>
			</xsl:for-each>

			<xsl:for-each select="marc:datafield[@tag='852']">
				<xsl:if test="marc:subfield[@code='y']">
					<pz:metadata type="publicnote">
						<xsl:value-of select="marc:subfield[@code='y']" />
					</pz:metadata>
				</xsl:if>
				<xsl:if test="marc:subfield[@code='h']">
					<pz:metadata type="callnumber">
						<xsl:value-of select="marc:subfield[@code='h']" />
					</pz:metadata>
				</xsl:if>
			</xsl:for-each>

			<!-- passthrough id data -->
			<xsl:for-each select="pz:metadata">
				<xsl:copy-of select="." />
			</xsl:for-each>

			<!-- other stylesheets importing this might want to define this -->
			<xsl:call-template name="record-hook" />

		</pz:record>
	</xsl:template>

	<xsl:template match="text()" />

</xsl:stylesheet>

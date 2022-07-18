<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE stylesheet [<!ENTITY nbsp "&#160;" >]>

<xsl:stylesheet version="1.0"
  xmlns:marc="http://www.loc.gov/MARC21/slim"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:str="http://exslt.org/strings"
  exclude-result-prefixes="marc str">
	<!-- Definition of global variables for this file -->
	<xsl:variable name="linktosearch">search?</xsl:variable>
	<xsl:variable name="linkbasic"></xsl:variable>
	<!--  <xsl:variable name="linktosearch">http://127.0.0.1:8080/bncf/search?</xsl:variable>
	<xsl:variable name="linkbasic">http://127.0.0.1:8080/bncf/</xsl:variable> -->


	<xsl:template name="datafield">
    <xsl:param name="tag"/>
    <xsl:param name="ind1"><xsl:text> </xsl:text></xsl:param>
    <xsl:param name="ind2"><xsl:text> </xsl:text></xsl:param>
    <xsl:param name="subfields"/>
    <xsl:element name="datafield">
      <xsl:attribute name="tag">
        <xsl:value-of select="$tag"/>
      </xsl:attribute>
      <xsl:attribute name="ind1">
        <xsl:value-of select="$ind1"/>
      </xsl:attribute>
      <xsl:attribute name="ind2">
       <xsl:value-of select="$ind2"/>
         </xsl:attribute>
       <xsl:copy-of select="$subfields"/>
    </xsl:element>
  </xsl:template>

  <xsl:template name="subfieldSelect">
    <xsl:param name="codes"/>
    <xsl:param name="delimeter"><xsl:text> </xsl:text></xsl:param>
    <xsl:param name="subdivCodes"/>
    <xsl:param name="subdivDelimiter"/>
    <xsl:param name="urlencode"/>
    <xsl:variable name="str">
      <xsl:for-each select="subfield">
        <xsl:if test="contains($codes, @code)">
          <xsl:if test="contains($subdivCodes, @code)">
            <xsl:value-of select="$subdivDelimiter"/>
          </xsl:if>
          <xsl:value-of select="text()"/><xsl:value-of select="$delimeter"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$urlencode=1">
        <xsl:value-of select="str:encode-uri(substring($str,1,string-length($str)-string-length($delimeter)), true())"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="substring($str,1,string-length($str)-string-length($delimeter))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="buildSpaces">
    <xsl:param name="spaces"/>
    <xsl:param name="char"><xsl:text> </xsl:text></xsl:param>
    <xsl:if test="$spaces>0">
      <xsl:value-of select="$char"/>
      <xsl:call-template name="buildSpaces">
        <xsl:with-param name="spaces" select="$spaces - 1"/>
        <xsl:with-param name="char" select="$char"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="chopPunctuation">
    <xsl:param name="chopString"/>
    <xsl:variable name="length" select="string-length($chopString)"/>
    <xsl:choose>
      <xsl:when test="$length=0"/>
      <xsl:when test="contains('.:,;/ ', substring($chopString,$length,1))">
        <xsl:call-template name="chopPunctuation">
          <xsl:with-param name="chopString" select="substring($chopString,1,$length - 1)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="not($chopString)"/>
      <xsl:otherwise><xsl:value-of select="$chopString"/></xsl:otherwise>
    </xsl:choose>
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template name="addClassRtl">
    <xsl:variable name="lang" select="subfield[@code='7']" />
    <xsl:if test="$lang = 'ha' or $lang = 'Hebrew' or $lang = 'fa' or $lang = 'Arabe'">
      <xsl:attribute name="class">rtl</xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template name="tag_title">
    <xsl:param name="tag" />
    <xsl:param name="label" />
    <xsl:param name="spanclass" />
    <xsl:if test="datafield[@tag=$tag]">
      <span class="results_summary {$spanclass}">
        <span class="labelDati">
        <xsl:value-of select="$label"/>: </span>
        <xsl:for-each select="datafield[@tag=$tag]">
          <xsl:call-template name="addClassRtl" />
          <xsl:for-each select="subfield">
            <xsl:choose>
              <xsl:when test="@code='a'">
                <xsl:variable name="title" select="."/>
                <xsl:variable name="ntitle" select="translate($title, '&#60;&#60;','')"/>
				<xsl:variable name="ntitle2" select="translate($ntitle, '&#62;&#62;','')"/>
                <xsl:value-of select="$ntitle2" />
              </xsl:when>
              <xsl:when test="@code='b'">
                <xsl:text>[</xsl:text>
                <xsl:value-of select="."/>
                <xsl:text>]</xsl:text>
              </xsl:when>
              <xsl:when test="@code='d'">
                <xsl:text> = </xsl:text>
                <xsl:value-of select="."/>
              </xsl:when>
              <xsl:when test="@code='e'">
                <xsl:text> : </xsl:text>
                <xsl:value-of select="."/>
              </xsl:when>
              <xsl:when test="@code='f'">
                <xsl:text> / </xsl:text>
                <xsl:value-of select="."/>
              </xsl:when>
              <xsl:when test="@code='g'">
                <xsl:text> ; </xsl:text>
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
          <xsl:if test="not (position() = last())">
            <xsl:text> </xsl:text>
          </xsl:if>
        </xsl:for-each>
      </span>
    </xsl:if>
  </xsl:template>

	<xsl:template name="tag_comma">
    <xsl:param name="tag" />
    <xsl:param name="label" />
    <xsl:param name="spanclass" />
    <xsl:if test="datafield[@tag=$tag]">
      <span class="results_summary {$spanclass}">
        <span class="labelDati">
        <xsl:value-of select="$label"/>: </span>
        <xsl:for-each select="datafield[@tag=$tag]">
          <xsl:call-template name="addClassRtl" />
          <xsl:for-each select="subfield">
            <xsl:if test="position()>1">
              <xsl:text>, </xsl:text>
            </xsl:if>
            <xsl:value-of select="."/>
          </xsl:for-each>
          <xsl:if test="not (position() = last())">
            <xsl:text> </xsl:text>
          </xsl:if>
        </xsl:for-each>
      </span>
    </xsl:if>
	</xsl:template>
	
	<xsl:template name="tag_identifiers">
		<xsl:param name="tag" />
		<xsl:param name="label" />
		<xsl:param name="trclass" />
		<xsl:param name="tdclass" />
		<xsl:if test="datafield[@tag=$tag]">
			<div class="row attribute-row py-2" id="{$trclass}">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					<xsl:value-of select="$label"/>
				</div>
				<div class="col-md-10 col-9 strSizeOnly {$tdclass}">
					<xsl:for-each select="datafield[@tag=$tag]">
						<xsl:if test="$tag = 010">
						<xsl:call-template name="tag_oneISBN"/>
					</xsl:if>
					<xsl:if test="$tag != 010">
						<xsl:call-template name="tag_oneidentifier">
							<xsl:with-param name="tag" select="$tag"/>
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="not (position() = last())">
						<xsl:text>. | </xsl:text>
					</xsl:if>
				</xsl:for-each>
				</div>
			</div>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="tag_oneISBN">
		<xsl:value-of select="subfield[@code='a']"/>
		<xsl:value-of select="subfield[@code='z']"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="subfield[@code='b']"/>
	</xsl:template>
	
	<xsl:template name="tag_oneidentifier">
		<xsl:param name="tag" />
		<xsl:for-each select="subfield">
		<xsl:choose>
			<xsl:when test="@code='a'">
				<xsl:if test="$tag = 020">
					<xsl:variable name="a" select="."/>					
					<xsl:value-of select="translate($a,'IT', 'BN')"/>
				</xsl:if>
				<xsl:if test="$tag != 020">					
					<xsl:value-of select="."/> 
				</xsl:if>				
            </xsl:when>
			<xsl:when test="@code='z'">
				<xsl:value-of select="."/>
            </xsl:when>
			<xsl:when test="@code='b' or @code='c'">
				<xsl:text> </xsl:text>
				<xsl:value-of select="."/>
            </xsl:when>
			<xsl:when test="@code='f'">
				<xsl:value-of select="."/>
				<xsl:text> linking ISSN</xsl:text>
            </xsl:when>
			<xsl:when test="@code='9'">
				<xsl:text>[</xsl:text>
				<xsl:value-of select="."/>
				<xsl:text>]</xsl:text>
            </xsl:when>
		</xsl:choose>
		</xsl:for-each>
	</xsl:template>	
	
	<xsl:template name="showNotes">
		<xsl:param name="notesfield" />
		<xsl:param name="label" />
		<xsl:param name="tr_id" />
		<xsl:param name="tdclass" />
		<xsl:param name="label_5_bib" />
		<xsl:param name="label_5_coll" />
		
		<xsl:for-each select="$notesfield">
			<div class="row attribute-row py-2" id="{$tr_id}{position()}">									
				<div class="col-md-2 col-3 label-search strSizeOnly">
					<xsl:choose>
						<xsl:when test="position()=1">
							<xsl:value-of select="$label"/>
						</xsl:when>
						<xsl:otherwise>
							<br/>
						</xsl:otherwise>
					</xsl:choose>
				</div>
				<div class="col-md-10 col-9 strSizeOnly {$tdclass}">
					<xsl:value-of select="subfield[@code='a']"/>
					<xsl:if test="@tag=317">
						<xsl:text> </xsl:text>
						<xsl:call-template name="showNote317">
							<xsl:with-param name="note317_5" select="subfield[@code='5']"/>
							<xsl:with-param name="n_label_5_bib"><xsl:value-of select="$label_5_bib"/></xsl:with-param>
							<xsl:with-param name="n_label_5_coll"><xsl:value-of select="$label_5_coll"/></xsl:with-param>
						</xsl:call-template>							
					</xsl:if>
				</div>
			</div>
		</xsl:for-each>
	</xsl:template>
	
	
	<xsl:template name="showNote317">
		<xsl:param name="note317_5" />
		<xsl:param name="n_label_5_bib" />
		<xsl:param name="n_label_5_coll" />
		
		<xsl:value-of select="$n_label_5_bib"/>
		<xsl:call-template name="biblioteche"><xsl:with-param name="value"><xsl:value-of select="substring($note317_5, 1, 9)"/></xsl:with-param></xsl:call-template>
		<xsl:value-of select="$n_label_5_coll"/>
		<xsl:value-of select="substring($note317_5, 11)"/>
	</xsl:template>	
	
		
	<xsl:template name="tag_one_225">
		<xsl:call-template name="addClassRtl" />
		<xsl:for-each select="subfield">	
			<xsl:choose>
				<xsl:when test="@code='a'">
					<xsl:variable name="title" select="."/>
					<xsl:variable name="ntitle" select="translate($title, '&#60;&#60;','')"/>
					<xsl:variable name="ntitle2" select="translate($ntitle, '&#62;&#62;','')"/>
					<xsl:value-of select="$ntitle2" />
				</xsl:when>
				<xsl:when test="@code='b'">
					<xsl:text>[</xsl:text>
					<xsl:value-of select="."/>
					<xsl:text>]</xsl:text>
				</xsl:when>
				<xsl:when test="@code='d'">
					<xsl:text> = </xsl:text>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='e'">
					<xsl:text> : </xsl:text>
				<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='f'">
				<xsl:text> / </xsl:text>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='g'">
					<xsl:text> ; </xsl:text>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='i'">
					<xsl:variable name="chk_i" select="."/>
					<xsl:if test = "not(starts-with($chk_i, '. '))">
						<xsl:text>. </xsl:text>
					</xsl:if>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='v'">
					<xsl:text> ; </xsl:text>
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
		<xsl:if test="not (position() = last())">
			<xsl:text> </xsl:text>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="many_225">
    <xsl:param name="tag" />
    <xsl:param name="label" />
    <xsl:param name="spanclass" />
    <xsl:if test="datafield[@tag=$tag]">
      <span class="results_summary {$spanclass}">
        <span class="labelDati">
        <xsl:value-of select="$label"/>: </span>
        <xsl:for-each select="datafield[@tag=$tag]">
          <xsl:call-template name="addClassRtl" />
          		<xsl:for-each select="subfield">	
			<xsl:choose>
				<xsl:when test="@code='a'">
					<xsl:variable name="title" select="."/>
					<xsl:variable name="ntitle" select="translate($title, '&#60;&#60;','')"/>
					<xsl:variable name="ntitle2" select="translate($ntitle, '&#62;&#62;','')"/>
					<xsl:value-of select="$ntitle2" />
				</xsl:when>
				<xsl:when test="@code='b'">
					<xsl:text>[</xsl:text>
					<xsl:value-of select="."/>
					<xsl:text>]</xsl:text>
				</xsl:when>
				<xsl:when test="@code='d'">
					<xsl:text> = </xsl:text>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='e'">
					<xsl:text> : </xsl:text>
				<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='f'">
				<xsl:text> / </xsl:text>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='g'">
					<xsl:text> ; </xsl:text>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='i'">
					<xsl:variable name="chk_i" select="."/>
					<xsl:if test = "not(starts-with($chk_i, '. '))">
						<xsl:text>. </xsl:text>
					</xsl:if>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='v'">
					<xsl:text> ; </xsl:text>
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
          <xsl:if test="not (position() = last())">
            <xsl:text> . - </xsl:text>
          </xsl:if>
        </xsl:for-each>
      </span>
    </xsl:if>
	</xsl:template>
	
	<xsl:template name="tag_title_4xx">
		<xsl:param name="tag" />
		<xsl:call-template name="addClassRtl" />
		<xsl:choose>
			<xsl:when test="subfield[@code=0]">
				<xsl:variable name="idreclink" select="subfield[@code='0']"/>
				<xsl:for-each select="subfield">
					<xsl:choose>
					<xsl:when test="@code='t'">
						<xsl:variable name="title" select="."/>
						<xsl:variable name="ntitle" select="translate($title, '&#60;&#60;','')"/>
						<xsl:variable name="ntitle2" select="translate($ntitle, '&#62;&#62;','')"/>
						<a>
						<xsl:attribute name="href">
							<xsl:if test="not($tag='454')">
								<xsl:value-of select="$linkbasic"/>resource?uri=<xsl:value-of select="$idreclink"/>
							</xsl:if>
							<xsl:if test="$tag='454'">
								<xsl:value-of select="$linktosearch"/>v=l&amp;q=&amp;h=any_bc&amp;s=10&amp;o=score&amp;f=id:<xsl:value-of select="$idreclink"/>
							</xsl:if>  
						</xsl:attribute>
						<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
						<xsl:value-of select="$ntitle2" />
						</a>
					</xsl:when>
					<xsl:when test="@code='b'">
					<xsl:text>[</xsl:text>
						<xsl:value-of select="."/>
						<xsl:text>]</xsl:text>
					</xsl:when>
					<xsl:when test="@code='l'">
						<xsl:text> = </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='o'">
						<xsl:text> : </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='f'">
						<xsl:text> / </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='g'">
						<xsl:text> ; </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='h'">
						<xsl:text>. </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='i'">
						<xsl:text>. </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='a'">
						<xsl:text>. </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='v'">
						<xsl:text>. Vol. </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="subfield">
					<xsl:choose>
					<xsl:when test="@code='t'">
						<xsl:variable name="title" select="."/>
						<xsl:variable name="ntitle" select="translate($title, '&#60;&#60;','')"/>
						<xsl:variable name="ntitle2" select="translate($ntitle, '&#62;&#62;','')"/>
						<xsl:value-of select="$ntitle2" />
					</xsl:when>
					<xsl:when test="@code='b'">
					<xsl:text>[</xsl:text>
						<xsl:value-of select="."/>
						<xsl:text>]</xsl:text>
					</xsl:when>
					<xsl:when test="@code='l'">
						<xsl:text> = </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='o'">
						<xsl:text> : </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='f'">
						<xsl:text> / </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='g'">
						<xsl:text> ; </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='h'">
						<xsl:text>. </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='i'">
						<xsl:text>. </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='a'">
						<xsl:text> (</xsl:text><span class="auth_into_4xx">
						<xsl:value-of select="."/>
						</span><xsl:text>)</xsl:text>
					</xsl:when>
					<xsl:when test="@code='v'">
						<xsl:text>. Vol. </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="tag_title_5xx">
		<xsl:call-template name="addClassRtl" />
		<xsl:choose>
			<xsl:when test="subfield[@code=3]">
				<xsl:variable name="iduniformlink" select="subfield[@code='3']"/>
				<a>
				<xsl:attribute name="href">
					<xsl:value-of select="$linktosearch"/>v=l&amp;q=&amp;h=any_bc&amp;s=10&amp;o=score&amp;f=title_uniform_search:<xsl:value-of select="subfield[@code='3']"/>  
				</xsl:attribute>
				<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
				<xsl:for-each select="subfield">
					<xsl:choose>
					<xsl:when test="@code='a'">
						<xsl:if test="position()>1">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:variable name="title" select="."/>
						<xsl:variable name="ntitle" select="translate($title, '&#60;&#60;','')"/>
						<xsl:variable name="ntitle2" select="translate($ntitle, '&#62;&#62;','')"/>
						<xsl:value-of select="$ntitle2" />
					</xsl:when>
					<xsl:when test="@code='d'">
						<xsl:text> = </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='e'">
						<xsl:text> : </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='x'">
						<xsl:text> -- </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='y'">
						<xsl:text> -- </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					</xsl:choose>	
				</xsl:for-each>
				</a>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="subfield">
					<xsl:choose>
					<xsl:when test="@code='a'">
						<xsl:if test="position()>1">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:variable name="title" select="."/>
						<xsl:variable name="ntitle" select="translate($title, '&#60;&#60;','')"/>
						<xsl:variable name="ntitle2" select="translate($ntitle, '&#62;&#62;','')"/>
						<xsl:value-of select="$ntitle2" />
					</xsl:when>
					<xsl:when test="@code='d'">
						<xsl:text> = </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='e'">
						<xsl:text> : </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='x'">
						<xsl:text> -- </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					<xsl:when test="@code='y'">
						<xsl:text> -- </xsl:text>
						<xsl:value-of select="."/>
					</xsl:when>
					</xsl:choose>	
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="show_mark">
		<xsl:param name="fields_mark" />
		<xsl:param name="label" />
		<xsl:param name="tr_id" />
		<xsl:param name="tdclass" />
		<xsl:for-each select="$fields_mark">
			<div class="row attribute-row py-2" id="{$tr_id}{position()}">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					<xsl:value-of select="$label"/>
				</div>
				<div class="col-md-10 col-9 strSizeOnly {$tdclass}">
					<xsl:choose>
					<xsl:when test="subfield[@code='a']">
						<xsl:variable name="idmark" select="subfield[@code='a']"/>
						<xsl:for-each select="subfield">
							<xsl:choose>
								<xsl:when test="@code='b'">
									<a>
									<xsl:attribute name="href">
										<xsl:value-of select="$linktosearch"/>q=<xsl:value-of select="$idmark"/>&amp;qt=mark_head  
									</xsl:attribute>
									<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
									<xsl:value-of select="."/>
									</a>
								</xsl:when>
								<xsl:when test="@code='d'">
									<xsl:text> - </xsl:text>
									<xsl:value-of select="."/>
								</xsl:when>
							</xsl:choose>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="subfield">
							<xsl:choose>
								<xsl:when test="@code='b'">
									<xsl:value-of select="."/>
								</xsl:when>
								<xsl:when test="@code='d'">
									<xsl:text> - </xsl:text>
									<xsl:value-of select="."/>
								</xsl:when>
							</xsl:choose>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
				</div>
			</div>	
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="biblioteche">
		<xsl:param name="value" />
		<xsl:choose>
			<xsl:when test="$value='IT-FI0098'">Biblioteca Nazionale Centrale</xsl:when>
			<xsl:when test="$value='IT-FI0600'">Istituto di Scienze Militari Aeronautiche</xsl:when>
			<xsl:when test="$value='IT-AR0101'">Eremo di Camaldoli</xsl:when>
			<xsl:when test="$value='IT-FI0035'">Conservatorio Luigi Cherubini</xsl:when>
			<xsl:when test="$value='IT-FI0611'">Fondazione Turati</xsl:when>
			<xsl:when test="$value='IT-FI0612'">Fondazione Turati</xsl:when>
			<xsl:when test="$value='IT-FI0331'">Fondazione Turati</xsl:when>
			<xsl:when test="$value='IT-FI0022'">Istituto Geografico Militare</xsl:when>
			<xsl:when test="$value='IT-FI0101'">Biblioteca Marucelliana</xsl:when>
			<xsl:when test="$value='IT-FI0100'">Biblioteca Medicea Laurenziana</xsl:when>
			<xsl:when test="$value='IT-FI0094'">Biblioteca Riccardiana</xsl:when>
			<xsl:when test="$value='IT-FI0419'">Museo archeologico nazionale</xsl:when>
			<xsl:when test="$value='IT-FI0109'">Biblioteca di Santa Croce</xsl:when>
			<xsl:when test="$value='IT-FI0061'">Instituto Nazionale di Studi Etruschi ed Italici</xsl:when>
			<xsl:when test="$value='IT-FI0934'">Archivio della Basilica di San Lorenzo</xsl:when>
			<xsl:otherwise>Bib. non censita</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	
</xsl:stylesheet>
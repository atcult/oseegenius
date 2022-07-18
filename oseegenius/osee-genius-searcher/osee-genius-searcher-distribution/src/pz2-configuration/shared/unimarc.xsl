<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pz="http://www.indexdata.com/pazpar2/1.0"
    xmlns:marc="http://www.loc.gov/MARC21/slim">

 <xsl:output indent="yes" method="xml" version="1.0" encoding="UTF-8"/>


  <xsl:template match="/marc:record">
    <pz:record>

      <xsl:for-each select="marc:controlfield[@tag='001']">
        <pz:metadata type="id">
          <xsl:value-of select="."/>
        </pz:metadata>
      </xsl:for-each>

      <!-- -->
      <xsl:for-each select="marc:datafield[@tag='020']">
        <xsl:if test="marc:subfield[@code='a'] = 'US'">
          <pz:metadata type="lccn">
            <xsl:value-of select="marc:subfield[@code='b']"/>
          </pz:metadata>
        </xsl:if>
      </xsl:for-each>

      <xsl:for-each select="marc:datafield[@tag='010']">
        <pz:metadata type="isbn">
          <xsl:value-of select="marc:subfield[@code='a']"/>
        </pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="marc:datafield[@tag='011']">
        <pz:metadata type="issn">
          <xsl:value-of select="marc:subfield[@code='a']"/>
        </pz:metadata>
      </xsl:for-each>


    <xsl:for-each select="marc:datafield[@tag='200']">
        <pz:metadata type="title">
                        <xsl:for-each select="marc:subfield">
                        <xsl:choose>
              <xsl:when test="@code='a'">
                <xsl:value-of select="."/>
              </xsl:when>
              <xsl:when test="@code='b'">
                <xsl:text> [</xsl:text>
                <xsl:value-of select="."/>
                <xsl:text>] </xsl:text>
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
        </pz:metadata>
    </xsl:for-each>


      <!-- Date of Pulbication -->
      <xsl:for-each select="marc:datafield[@tag='210']">
        <pz:metadata type="date">
          <xsl:value-of select="marc:subfield[@code='d']"/>
        </pz:metadata>
      </xsl:for-each>

        <xsl:for-each select="marc:datafield[@tag=215]">
          <pz:metadata type="physical-description">
        <xsl:if test="marc:subfield[@code='a']">
          <xsl:value-of select="marc:subfield[@code='a']"/>
        </xsl:if>
        <xsl:if test="marc:subfield[@code='c']"> :
          <xsl:value-of select="marc:subfield[@code='c']"/>
        </xsl:if>
        <xsl:if test="marc:subfield[@code='d']"> ;
          <xsl:value-of select="marc:subfield[@code='d']"/>
        </xsl:if>
        <xsl:if test="marc:subfield[@code='e']"> +
          <xsl:value-of select="marc:subfield[@code='e']"/>
        </xsl:if>
      </pz:metadata>
    </xsl:for-each>




      <!--  Usmarc 650 maps to unimarc 606 and marc21 653 maps to unimarc 610 -->
      <xsl:for-each select="marc:datafield[@tag='606' or @tag='610']">
        <pz:metadata type="subject">
          <xsl:value-of select="marc:subfield[@code='a']"/>
        </pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="marc:datafield[@tag &gt;= 300 and @tag &lt;= 345]
                            [@tag != '325']">
        <pz:metadata type="description">
            <xsl:value-of select="*/text()"/>
        </pz:metadata>
      </xsl:for-each>


      <!-- Author : primary, alternative and secondary responsibility (equivalent marc21 tags : 100, 700 -->
    <xsl:for-each select="marc:datafield[@tag='700' or @tag='701' or @tag='702' or @tag='710' or @tag='711' or @tag='712']">
        <pz:metadata type="author">
          <xsl:for-each select="marc:subfield">
                        <xsl:choose>
                        <xsl:when test="@code='c' or @code='d' or @code='e'">
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="."/>
                        </xsl:when>
                        <xsl:when test="@code='a'">
                          <xsl:value-of select="."/>
                        </xsl:when>
                        <xsl:when test="@code='b'">
                          <xsl:text>, </xsl:text>
                          <xsl:value-of select="."/>
                        </xsl:when>
                        <xsl:when test="@code='c' or @code='d' or @code='e'">
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="."/>
                        </xsl:when>
                        </xsl:choose>
          </xsl:for-each>
        </pz:metadata>
    </xsl:for-each>


      <xsl:for-each select="marc:datafield[@tag='856']">
        <pz:metadata type="url">
          <xsl:value-of select="marc:subfield[@code='u']"/>
        </pz:metadata>
      </xsl:for-each>

    </pz:record>
  </xsl:template>

</xsl:stylesheet>
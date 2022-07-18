<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pz="http://www.indexdata.com/pazpar2/1.0"
    xmlns:m="http://www.loc.gov/MARC21/slim"
    exclude-result-prefixes="m">
  
  <xsl:output indent="yes" method="xml" version="1.0" encoding="UTF-8"/>

<!-- Extract metadata from MARC21/USMARC 
      http://www.loc.gov/marc/bibliographic/ecbdhome.html
-->  
  <xsl:template name="record-hook"/>

  <xsl:template match="m:record">
    <xsl:variable name="title_medium" select="m:datafield[@tag='200']/m:subfield[@code='h']"/>
    <xsl:variable name="journal_title" select="m:datafield[@tag='773']/m:subfield[@code='t']"/>
    <xsl:variable name="electronic_location_url" select="m:datafield[@tag='856']/m:subfield[@code='u']"/>
    <xsl:variable name="fulltext_a" select="m:datafield[@tag='900']/m:subfield[@code='a']"/>
    <xsl:variable name="fulltext_b" select="m:datafield[@tag='900']/m:subfield[@code='b']"/>
    <xsl:variable name="medium">
      <xsl:choose>
	<xsl:when test="$title_medium">
	  <xsl:value-of select="translate($title_medium, ' []/', '')"/>
	</xsl:when>
	<xsl:when test="$fulltext_a">
	  <xsl:text>electronic resource</xsl:text>
	</xsl:when>
	<xsl:when test="$fulltext_b">
	  <xsl:text>electronic resource</xsl:text>
	</xsl:when>
	<xsl:when test="$journal_title">
	  <xsl:text>article</xsl:text>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:text>book</xsl:text>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="has_fulltext">
      <xsl:choose>
        <xsl:when test="m:datafield[@tag='856']/m:subfield[@code='q']">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="m:datafield[@tag='856']/m:subfield[@code='i']='TEXT*'">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>no</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="oclc_number">
      <xsl:variable name="c001" select='m:controlfield[@tag="001"]' />
      <xsl:choose>
        <xsl:when test='contains(m:controlfield[@tag="001"],"ocn") or
                        contains(m:controlfield[@tag="001"],"ocm") or
                        contains(m:controlfield[@tag="001"],"OCoLC")'>
            <xsl:value-of select="m:controlfield[@tag='001']"/>
        </xsl:when>
        <xsl:when test='contains(m:datafield[@tag="035"]/m:subfield[@code="a"],"ocn") or
                        contains(m:datafield[@tag="035"]/m:subfield[@code="a"],"ocm") or
                        contains(m:datafield[@tag="035"]/m:subfield[@code="a"],"OCoLC") '>
         <xsl:value-of select="m:datafield[@tag='035']/m:subfield[@code='a']"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="date_008">
      <xsl:choose>
        <xsl:when test="contains('cestpudikmr', substring(m:controlfield[@tag='008'], 7, 1))">
          <xsl:value-of select="substring(m:controlfield[@tag='008'], 8, 4)" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="date_end_008">
      <xsl:choose>
        <xsl:when test="contains('dikmr', substring(m:controlfield[@tag='008'], 7, 1))">
          <xsl:value-of select="substring(m:controlfield[@tag='008'], 12, 4)" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <pz:collection>
    <pz:record>
      <xsl:for-each select="m:controlfield[@tag='001']">
        <pz:metadata type="id">
          <xsl:value-of select="."/>
        </pz:metadata>
      </xsl:for-each>

      <pz:metadata type="oclc-number">
        <xsl:value-of select="$oclc_number" />
      </pz:metadata>

      <xsl:for-each select="m:datafield[@tag='010']">
	     <pz:metadata type="isbn">
		  <xsl:value-of select="m:subfield[@code='a']"/>
		</pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='011']">
	    <pz:metadata type="issn">
		  <xsl:value-of select="m:subfield[@code='a']"/>
		</pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='700']">
		<pz:metadata type="author">
			<xsl:value-of select="m:subfield[@code='a']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='b']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='f']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='e']" />
		</pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='710'][@ind0]">
		<pz:metadata type="corporate-name">
	   		<xsl:value-of select="m:subfield[@code='a']" />
	   		<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='b']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='f']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='e']" />
		</pz:metadata>
      </xsl:for-each>

       <xsl:for-each select="m:datafield[@tag='710'][@ind1]">
		<pz:metadata type="meeting-name">
	    	<xsl:value-of select="m:subfield[@code='a']" />
	    	<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='b']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='f']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='e']" />
		</pz:metadata>
      </xsl:for-each>
      
      <xsl:for-each select="m:datafield[@tag='702']">
		<pz:metadata type="other-author">
			<xsl:value-of select="m:subfield[@code='a']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='b']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='e']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='f']" />
		</pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='712'][@ind0]">
		<pz:metadata type="other-corporate-name">
	   		<xsl:value-of select="m:subfield[@code='a']" />
	   		<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='b']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='e']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='f']" />
		</pz:metadata>
      </xsl:for-each>

       <xsl:for-each select="m:datafield[@tag='712'][@ind1]">
		<pz:metadata type="other-meeting-name">
	    	<xsl:value-of select="m:subfield[@code='a']" />
	    	<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='b']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='e']" />
			<xsl:text> </xsl:text>
			<xsl:value-of select="m:subfield[@code='f']" />
		</pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='500']/m:subfield[@code='a']">
        <pz:metadata type="title-uniform">
          <xsl:value-of select="normalize-space(translate(text(),'.-', ''))" />
        </pz:metadata>
       </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='200']">
        <pz:metadata type="title">
          <xsl:value-of select="m:subfield[@code='a']"/>
          <xsl:choose>
		         <xsl:when test="m:subfield[@code='e']">
		          	<xsl:text> </xsl:text>
		          	<xsl:text>:</xsl:text>
		          	<xsl:text> </xsl:text>
		            <xsl:value-of select="m:subfield[@code='e']" />
		        </xsl:when>
		        <xsl:when test="m:subfield[@code='f']">
		          	<xsl:text> </xsl:text>
		          	<xsl:text>/</xsl:text>
		          	<xsl:text> </xsl:text>
		            <xsl:value-of select="m:subfield[@code='f']" />
		            <xsl:choose>
		             <xsl:when test="m:subfield[@code='g']">
			          	<xsl:text> </xsl:text>
			          	<xsl:text>;</xsl:text>
			          	<xsl:text> </xsl:text>
			            <xsl:value-of select="m:subfield[@code='g']" />
		            </xsl:when>
		            </xsl:choose>
		        </xsl:when>
	        </xsl:choose>
        </pz:metadata>
      </xsl:for-each>
      
      
      <xsl:for-each select="m:datafield[@tag='205']">
	<pz:metadata type="edition">
	    <xsl:value-of select="m:subfield[@code='a']"/>
	</pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='210']">
        <pz:metadata type="publication-place">
	  		<xsl:value-of select="m:subfield[@code='a']" />
	  		<xsl:if test="m:subfield[@code='c']">
			      <xsl:text> : </xsl:text>
			      <xsl:value-of select="m:subfield[@code='c']" />
			</xsl:if>
	  		<xsl:if test="m:subfield[@code='d']">
			     <xsl:text>, </xsl:text>
				<xsl:value-of select="m:subfield[@code='d']" />
			</xsl:if>
		</pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='215']">
		<pz:metadata type="physical-extent">
		 	<xsl:value-of select="m:subfield[@code='a']" />
			<xsl:if test="m:subfield[@code='c']">
			      <xsl:text> : </xsl:text>
			      <xsl:value-of select="m:subfield[@code='c']" />
			</xsl:if>
			<xsl:if test="m:subfield[@code='d']">
			     <xsl:text> ; </xsl:text>
				<xsl:value-of select="m:subfield[@code='d']" />
			</xsl:if>
			<xsl:if test="m:subfield[@code='e']">
			      <xsl:text> + </xsl:text>
			      <xsl:value-of select="m:subfield[@code='e']" />
			</xsl:if>
		</pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='225']">
		<pz:metadata type="series-title">
		  <xsl:value-of select="m:subfield[@code='a']"/>
		</pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag = '300' or @tag = '305' or
      		@tag = '330']">
		<pz:metadata type="description">
            <xsl:value-of select="*/text()"/>
        </pz:metadata>
      </xsl:for-each>

	<xsl:for-each
		select="m:datafield[@tag='600' or @tag='601'  or @tag='605' or @tag='606'  or @tag='608']">
		<pz:metadata type="subject">
			<xsl:value-of select="m:subfield[@code='a']" />
			<xsl:if test="m:subfield[@code='x']" >
				<xsl:text> - </xsl:text>
				<xsl:value-of select="m:subfield[@code='x']" />
			</xsl:if>
		</pz:metadata>
		<pz:metadata type="subject-long">
			<xsl:for-each select="node()/text()">
				<xsl:if test="position() > 1">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:variable name='value'>
					<xsl:value-of select='normalize-space(.)' />
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="substring($value,string-length($value)) = ','">
						<xsl:value-of select="substring($value,1,string-length($value)-1)" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$value" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</pz:metadata>
	</xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='856']">
		<pz:metadata type="electronic-url">
		  <xsl:value-of select="m:subfield[@code='u']"/>
		</pz:metadata>
	</xsl:for-each> 

    <!-- equivalente al 773 in marc21 -->
    <xsl:for-each select="m:datafield[@tag='461' or @tag='462' or @tag='463' or @tag='464']">
        <pz:metadata type="mather">
          <xsl:value-of select="m:subfield[@code='a']/text()"/>
 			<xsl:if test="m:subfield[@code='e']">
		          	<xsl:text> </xsl:text>
		          	<xsl:text>:</xsl:text>
		          	<xsl:text> </xsl:text>
		            <xsl:value-of select="m:subfield[@code='e']" />
		    </xsl:if>
        </pz:metadata>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='852']">
        <xsl:if test="m:subfield[@code='y']">
	  <pz:metadata type="publicnote">
	    <xsl:value-of select="m:subfield[@code='y']"/>
	  </pz:metadata>
	</xsl:if>
	<xsl:if test="m:subfield[@code='h']">
	  <pz:metadata type="callnumber">
	    <xsl:value-of select="m:subfield[@code='h']"/>
	  </pz:metadata>
	</xsl:if>
      </xsl:for-each>

      <xsl:for-each select="m:datafield[@tag='900']/m:subfield[@code='a']">
        <pz:metadata type="fulltext">
          <xsl:value-of select="."/>
        </pz:metadata>
      </xsl:for-each>

      <pz:metadata type="medium">
        <xsl:value-of select="$medium"/>
      </pz:metadata>

      <pz:metadata type="has-fulltext">
        <xsl:value-of select="$has_fulltext"/> 
      </pz:metadata>

      <xsl:for-each select="m:datafield[@tag='900']/m:subfield[@code='b']">
        <pz:metadata type="fulltext">
          <xsl:value-of select="."/>
        </pz:metadata>
      </xsl:for-each> -->

      <!-- <xsl:if test="$fulltext_b">
	<pz:metadata type="fulltext">
	  <xsl:value-of select="$fulltext_b"/>
	</pz:metadata>
      </xsl:if> -->

      <!-- passthrough id data -->
      <xsl:for-each select="pz:metadata">
          <xsl:copy-of select="."/>
      </xsl:for-each>

      <!-- other stylesheets importing this might want to define this -->
      <xsl:call-template name="record-hook"/>

    </pz:record>    
    </pz:collection>
  </xsl:template>
  
  <xsl:template match="text()"/>

</xsl:stylesheet>

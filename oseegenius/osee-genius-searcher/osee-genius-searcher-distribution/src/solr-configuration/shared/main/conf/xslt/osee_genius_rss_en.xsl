<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

  <xsl:output
       method="xml"
       encoding="utf-8"
       media-type="text/xml; charset=UTF-8"
  />
  <xsl:variable name="osee-genius" select="/response/lst/lst/str[@name='og']/text()"/>
  <xsl:template match='/'>
  	
    <rss version="2.0">
       <channel>
	 <title>Osee Genius - Search and Discovery</title>
         <link><xsl:value-of select="$osee-genius"/></link>
         <description>Subcribe this feed and you will receive in real-time updates of your search.</description>
         <language>en</language>
         <docs><xsl:value-of select="$osee-genius"/></docs>
         <xsl:apply-templates select="response/result/doc"/>
       </channel>
    </rss>
  </xsl:template>
  
  <xsl:template match="doc">
    <xsl:variable name="id" select="str[@name='id']"/>
    <item>
      <title><xsl:value-of select="str[@name='title']"/></title>
      <link>
        <xsl:value-of select="$osee-genius"/>/resource?uri=<xsl:value-of select="$id"/>
      </link>
      <description>
		&lt;p&gt;
        <xsl:value-of select="str[@name='author_person']"/>
        <xsl:value-of select="str[@name='author_corporate']"/>        
		<xsl:value-of select="str[@name='author_conference']"/>
		&lt;/p&gt;
		&lt;p&gt;
			<xsl:value-of select="arr[@name='publisher']"/>
		&lt;/p&gt;
      </description>
      <pubDate><xsl:value-of select="str[@name='publication_date']"/></pubDate>
      <guid>
        <xsl:value-of select="$osee-genius"/>/resource?uri=<xsl:value-of select="$id"/>
      </guid>
    </item>
  </xsl:template>
</xsl:stylesheet>

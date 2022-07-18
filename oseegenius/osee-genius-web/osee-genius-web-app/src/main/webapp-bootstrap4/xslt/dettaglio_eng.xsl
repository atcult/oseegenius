<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="1.0"
	xmlns:marc="http://www.loc.gov/MARC21/slim"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:str="http://exslt.org/strings"
	exclude-result-prefixes="marc str">
	
	<xsl:import href="utils.xsl"/>
	<xsl:import href="codes_ita.xsl"/>
	
	<xsl:output method="html" indent="yes"/>
	<!--
	856: Risorsa elettronica
	956: Digitalizzato nel progetto Google-BNCF
	899: ind2: 0 - Digitalizzazione parziale, 1 - Digitalizzazione totale, 2 - Copia di opera già digitale in origine
	-->
	<xsl:param name="ext_title" />
	
	<xsl:template match="/">	
		<div id="bib_details">	
			<xsl:apply-templates select="//record"/>	
		</div>	
	</xsl:template>
	
	
	<xsl:template match="record">
		<xsl:variable name="leader" select="leader"/>
		<xsl:variable name="leader8" select="substring($leader,9,1)"/>
			
		<xsl:if test="datafield[@tag=700 or @tag=710]">
			<xsl:call-template name="show_authors">
				<xsl:with-param name="fields7xx" select="datafield[(@tag=700 or @tag=710)]"/>
				<xsl:with-param name="label">Author:</xsl:with-param>
				<xsl:with-param name="tr_id">trAuthPrinc</xsl:with-param>
				<xsl:with-param name="tdclass">tagAuthPrinc</xsl:with-param>
				<xsl:with-param name="type">aut_princ</xsl:with-param>
			</xsl:call-template>
		</xsl:if>	
				
		<div class="row attribute-row py-2" id="tagTest">
			<div class="col-md-2 col-3 label-search strSizeOnly">
				Title: 
			</div>
			<div class="col-md-10 col-9 strSizeOnly">
				<xsl:value-of select="$ext_title"/>
			</div>
		</div>
		
		<xsl:if test="datafield[@tag=205]">
			<div class="row attribute-row py-2" id="tag205">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Edition: 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:call-template name="tag_205" />
				</div>
			</div>
		</xsl:if>				
		
		<xsl:if test="datafield[@tag=206]">
			<div class="row attribute-row py-2" id="tag206">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Carthographic:: 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:value-of select="//datafield[@tag=206]/subfield[@code='a']"/>
				</div>
			</div>
		</xsl:if>
		
		<xsl:if test="datafield[@tag=207]">
			<div class="row attribute-row py-2" id="tag207">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Numbering: 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:value-of select="//datafield[@tag=207]/subfield[@code='a']"/>
				</div>
			</div>
		</xsl:if>
		
		<xsl:if test="datafield[@tag=208]">
			<div class="row attribute-row py-2" id="tag208">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Printed music:
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:call-template name="tag_208" />
				</div>
			</div>
		</xsl:if>
		
		<xsl:if test="datafield[@tag=210]">
			<div class="row attribute-row py-2" id="tag210">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Publication: 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:call-template name="tag_210" />				
				</div>				
			</div>			
		</xsl:if>
		
		<xsl:if test="datafield[@tag=215]">
			<div class="row attribute-row py-2" id="tag215">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Physical description: 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:call-template name="tag_215" />
				</div>				
			</div>
		</xsl:if>
		
		<xsl:call-template name="show225">
			<xsl:with-param name="fields225" select="datafield[@tag=225]"/>
			<xsl:with-param name="tr_id">tr225_</xsl:with-param>
			<xsl:with-param name="tdclass">tag225</xsl:with-param>
		</xsl:call-template>
		
		<SCRIPT LANGUAGE="JavaScript">
		     jQuery(document).ready(function(){ 
		     	jQuery(".toExpand").click(function() { 
		    		var el, elp, ps, up, totalHeight; 
		     		totalHeight = 0; 
		  			el = jQuery(this); 
		     		elp = el.parent(); 
		     		up = elp.prev(); 
		     		ps = up.find("p"); 
		       		ps.each(function() { 
		         		totalHeight += jQuery(this).outerHeight(); 
		     		}); 
		     		up 
		     		.css({ 
		         		"height": up.height(), 
		         		"max-height": 9999 
		     		}) 
		     		.animate({ 
		         		"height": totalHeight 
		     		}); 	       
					$("#reduceText").show();
					$("#showText").hide();
		     	}); 
		       
		    jQuery(".toReduce").click(function() { 
				$("#content").height("70px");       
				$("#reduceText").hide();
				$("#showText").show();
		     }); 
		   }); 
		</SCRIPT>

		
		<xsl:if test="datafield[@tag=330]">
			<div class="row attribute-row py-2" id="tag330">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Abstract: 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:for-each select="datafield[@tag=330]">
						<xsl:variable name="abs" select="subfield[@code='a']"/>
						<xsl:variable name="length" select="string-length(string($abs))"/>
						<xsl:choose>
							<xsl:when test="$length>500">
								<div id = "content" style="max-height: 70px; overflow: hidden;">
									<p><xsl:value-of select="$abs" /></p>
								</div>
								<div class="col-md-5 col-12">
									<a id="showText" class="toExpand" href="#" >Show all</a>
									<a id="reduceText" class="toReduce" href="#"  style="display: none">Reduce</a>
								</div>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="subfield[@code='a']"/>
								<xsl:choose>
									<xsl:when test="position()=last()">
										<xsl:text>.</xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>; </xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose>
						
					</xsl:for-each>
				</div>
			</div>		
		</xsl:if>
		
		<xsl:call-template name="showNotes">
            <xsl:with-param name="notesfield" select="datafield[(@tag=300 or @tag=311 or @tag=312 or @tag=317 or @tag=323 or @tag=327 or @tag=336 or @tag=337)]"/>
            <xsl:with-param name="label">Notes:</xsl:with-param>
            <xsl:with-param name="label_5_bib">In: </xsl:with-param>
            <xsl:with-param name="label_5_coll">. Loc.: </xsl:with-param>
			<xsl:with-param name="tr_id">tr3xx_</xsl:with-param>
			<xsl:with-param name="tdclass">tag3xx</xsl:with-param>
        </xsl:call-template>
        		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">010</xsl:with-param><xsl:with-param name="label">ISBN:</xsl:with-param>
			<xsl:with-param name="trclass">tag010</xsl:with-param><xsl:with-param name="tdclass">td010</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">011</xsl:with-param><xsl:with-param name="label">ISSN:</xsl:with-param>
			<xsl:with-param name="trclass">tag011</xsl:with-param><xsl:with-param name="tdclass">td011</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">012</xsl:with-param><xsl:with-param name="label">Fingerprint:</xsl:with-param>
			<xsl:with-param name="trclass">tag012</xsl:with-param><xsl:with-param name="tdclass">td012</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">013</xsl:with-param><xsl:with-param name="label">ISMN:</xsl:with-param>
			<xsl:with-param name="trclass">tag013</xsl:with-param><xsl:with-param name="tdclass">td013</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">017</xsl:with-param><xsl:with-param name="label">Other st. identifier:</xsl:with-param>
			<xsl:with-param name="trclass">tag017</xsl:with-param><xsl:with-param name="tdclass">td017</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">020</xsl:with-param><xsl:with-param name="label">National bib. number:</xsl:with-param>
			<xsl:with-param name="trclass">tag020</xsl:with-param><xsl:with-param name="tdclass">td020</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">022</xsl:with-param><xsl:with-param name="label">Gov. publication number:</xsl:with-param>
			<xsl:with-param name="trclass">tag022</xsl:with-param><xsl:with-param name="tdclass">td022</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">071</xsl:with-param><xsl:with-param name="label">Publisher's number:</xsl:with-param>
			<xsl:with-param name="trclass">tag071</xsl:with-param><xsl:with-param name="tdclass">td071</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">072</xsl:with-param><xsl:with-param name="label">UPC:</xsl:with-param>
			<xsl:with-param name="trclass">tag072</xsl:with-param><xsl:with-param name="tdclass">td072</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="tag_identifiers">
			<xsl:with-param name="tag">073</xsl:with-param><xsl:with-param name="label">EAN:</xsl:with-param>
			<xsl:with-param name="trclass">tag073</xsl:with-param><xsl:with-param name="tdclass">td073</xsl:with-param>
		</xsl:call-template>
        
        <xsl:if test="datafield[@tag=921]">
			<xsl:call-template name="show_mark">
			<xsl:with-param name="fields_mark" select="datafield[@tag=921]"/>
            <xsl:with-param name="label">Printe's marc:</xsl:with-param>
			<xsl:with-param name="tr_id">trPMark_</xsl:with-param>
			<xsl:with-param name="tdclass">tagPMark</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
                
        <xsl:if test="datafield[@tag=620]">
	        <div class="row attribute-row py-2" id="tag620">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Place (formal): 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:call-template name="tag_620" />
				</div>				
			</div>
		</xsl:if>
        
        <xsl:if test="datafield[@tag=701 or @tag=711]">
			<xsl:call-template name="show_authors">
				<xsl:with-param name="fields7xx" select="datafield[(@tag=701 or @tag=711)]"/>
				<xsl:with-param name="label">Other authors:</xsl:with-param>
				<xsl:with-param name="tr_id">tr7x1_</xsl:with-param>
				<xsl:with-param name="tdclass">tag7x1</xsl:with-param>
				<xsl:with-param name="type">aut_others</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
		
		<xsl:if test="datafield[@tag=702 or @tag=712]">
			<xsl:call-template name="show_authors">
				<xsl:with-param name="fields7xx" select="datafield[(@tag=702 or @tag=712)]"/>
				<xsl:with-param name="label">Sec. Responsability:</xsl:with-param>
				<xsl:with-param name="tr_id">tr7x2_</xsl:with-param>
				<xsl:with-param name="tdclass">tag7x2</xsl:with-param>
				<xsl:with-param name="type">not_aut</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
		
		<xsl:call-template name="show4xx">
			<xsl:with-param name="fields4xx" select="datafield[(@tag=410 or @tag=421 or @tag=422 or @tag=423 or @tag=430 or @tag=431 or @tag=434 or @tag=440 or @tag=441 or @tag=447 or @tag=451 or @tag=454 or @tag=461 or @tag=462)]"/>
			<xsl:with-param name="tr_id">tr4xx_</xsl:with-param>
			<xsl:with-param name="tdclass">tag4xx</xsl:with-param>
			<xsl:with-param name="ldr_hrc" select="$leader8"/>
		</xsl:call-template>
		
		<xsl:call-template name="show463">
			<xsl:with-param name="fields463" select="datafield[(@tag=463)]"/>
			<xsl:with-param name="tr_id">tr4xx_</xsl:with-param>
			<xsl:with-param name="tdclass">tag4xx</xsl:with-param>
			<xsl:with-param name="ldr_hrc" select="$leader8"/>
		</xsl:call-template>
		
		<xsl:call-template name="show4xx">
			<xsl:with-param name="fields4xx" select="datafield[(@tag=464 or @tag=488)]"/>
			<xsl:with-param name="tr_id">tr4xx_</xsl:with-param>
			<xsl:with-param name="tdclass">tag4xx</xsl:with-param>
			<xsl:with-param name="ldr_hrc" select="$leader8"/>
		</xsl:call-template>
		
		<xsl:call-template name="show5xx">
			<xsl:with-param name="fields5xx" select="datafield[(@tag=500 or @tag=510 or @tag=517 or @tag=530 or @tag=560)]"/>
			<xsl:with-param name="tr_id">tr5xx_</xsl:with-param>
			<xsl:with-param name="tdclass">tag5xx</xsl:with-param>
		</xsl:call-template>
		
		<xsl:if test="datafield[@tag=606]">
			<div class="row attribute-row py-2" id="tag606">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Subject: 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:call-template name="tag_606" />
				</div>				
			</div>
		</xsl:if>
		

		
		<xsl:if test="datafield[@tag=676]">
			<div class="row attribute-row py-2" id="tag676">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					Dewey class.: 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:call-template name="tag_676" />
				</div>				
			</div>			
		</xsl:if>
		
		<xsl:if test="datafield[@tag=689]">
			<div class="row attribute-row py-2" id="tag689">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					MIUR class.: 
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:call-template name="tag_689" />
				</div>				
			</div>			
		</xsl:if>
		
		<xsl:if test="datafield[@tag=928]">
			<xsl:call-template name="tag_928">
				<xsl:with-param name="fields928" select="datafield[@tag=928]"/>
				<xsl:with-param name="tr_id">tr928_</xsl:with-param>
				<xsl:with-param name="tdclass">tag928</xsl:with-param>
				<xsl:with-param name="label">Organico Strum.:</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
		
		<xsl:if test="datafield[@tag=856]">
			<xsl:if test="datafield[@tag=049]/subfield[@code='a'] != 'TDMAGDIG'">
			<div class="row attribute-row py-2" id="tag865">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					<img alt="" src="img/CD.jpg"/>
				</div>
				<div class="col-md-10 col-9 strSizeOnly">
					<xsl:call-template name="tag_856"><xsl:with-param name="label">Digital Resource</xsl:with-param></xsl:call-template>
				</div>				
			</div>
			</xsl:if>
			
			<xsl:if test="datafield[@tag=049]/subfield[@code='a'] = 'TDMAGDIG'">
				<xsl:for-each select="datafield[@tag=856]">
				<div class="row attribute-row py-2 strSizeOnly" id="tag865">
					<xsl:call-template name="tag_856MGDIG">
						<xsl:with-param name="tag" select="."/>
						<xsl:with-param name="label1">Legal deposit in BNCF</xsl:with-param>
						<xsl:with-param name="label2">Institutional Repository</xsl:with-param>
					</xsl:call-template>
				</div>
				</xsl:for-each>
			</xsl:if>	
		</xsl:if>
		
		<xsl:if test="datafield[@tag=899]">
			<xsl:for-each select="datafield[@tag=899]">
				<div class="row attribute-row py-2" id="tr899_{position()}">
					<div class="col-md-2 col-3 label-search strSizeOnly">
						<img src="img/CD.jpg"/>
					</div>
					<div class="col-md-10 col-9 strSizeOnly">
						<xsl:call-template name="show_899"/>
					</div>				
				</div>
			</xsl:for-each>
		</xsl:if>
				
		<br/>
		<br/>
		
		<div class="row py-3" id="generaldata">
			<div class="col-md-7 col-12 strSizeOnly">
				<xsl:call-template name="tag_fmt" />. | Language: <xsl:call-template name="tag_101" /> | Country: <xsl:call-template name="tag_102" /> | BID: <xsl:value-of select="controlfield[@tag=001]" />
			</div>
			<div class="col-md-5 col-12">
				<a class="float-md-right btn button-search  current-button" href="javascript:manipulateQueryString('v','m21')" >Show Unimarc</a>
			</div>
		</div>
		

		<xsl:if test="datafield[@tag='FMT']/subfield[@code='a']='CO'">
			<br/>
			<div class="row" id="searchlinkcol">
				<div class="col-md-12 col-12 tdSearchLinkCol">
					<a>
						<xsl:attribute name="href">
							<xsl:value-of select="$linktosearch"/>v=l&amp;q=&amp;h=any_bc&amp;s=10&amp;o=score&amp;f=main_series_linking_entry_id:<xsl:value-of select="controlfield[@tag=001]"/>  
						</xsl:attribute>
						<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
						<xsl:text>Find what is connect with this series</xsl:text>
					</a>
				</div>
			</div>
		</xsl:if>
		
	</xsl:template>
	
	
	
	<!-- Templates for specific fields -->
	
	<xsl:template name="show463">
		<xsl:param name="fields463" />
		<xsl:param name="tr_id" />
		<xsl:param name="tdclass" />
		<xsl:param name="ldr_hrc" />
		<xsl:for-each select="$fields463">
			<xsl:sort select="subfield[@code='v']" data-type="number"/>
			<xsl:sort select="subfield[@code='t']" data-type="text"/>
				<div class="row attribute-row py-2" id="{$tr_id}{position()}">
					<div class="col-md-2 col-3 label-search strSizeOnly">
						<xsl:call-template name="labels4xx">
							<xsl:with-param name="value"><xsl:value-of select="463"/> </xsl:with-param>
							<xsl:with-param name="hrc" select="$ldr_hrc"/>
						</xsl:call-template>
					</div>
					<div class="col-md-10 col-9 strSizeOnly {$tdclass}">
						<xsl:call-template name="tag_title_4xx">
							<xsl:with-param name="tag"><xsl:value-of select="463"/> </xsl:with-param>
						</xsl:call-template>
					</div>				
				</div>
		</xsl:for-each>
	</xsl:template>
	
	
	<xsl:template name="show_899">
		<xsl:variable name="label">
			<xsl:call-template name="label_ind2_899">
				<xsl:with-param name="value"><xsl:value-of select="@ind2"/></xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		<xsl:for-each select="subfield">
			<xsl:choose>
				<xsl:when test="@code='u'">
					<a>
					<xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
					<xsl:attribute name="target">_blank</xsl:attribute>
					<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
					<xsl:value-of select="$label"/>
					</a><xsl:text> </xsl:text>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="show_956">
		<xsl:variable name="label">
			<xsl:call-template name="label_956">
				<xsl:with-param name="value"><xsl:value-of select="subfield[@code='c']"/></xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		<xsl:for-each select="subfield">
			<xsl:choose>
				<xsl:when test="@code='u'">
					<a>
					<xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
					<xsl:attribute name="target">_blank</xsl:attribute>
					<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
					<xsl:value-of select="$label"/>
					</a><xsl:text> </xsl:text>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	
	<xsl:template name="tag_101">
		<xsl:if test="datafield[@tag=101]">
			<xsl:for-each select="datafield[@tag=101]">
			<xsl:for-each select="subfield">
				<xsl:call-template name="code101">
					<xsl:with-param name="value"><xsl:value-of select="."/></xsl:with-param>
				</xsl:call-template>
				<xsl:if test="@code='b'">
					<xsl:text> (intermediate language)</xsl:text>
				</xsl:if>
				<xsl:if test="@code='c'">
					<xsl:text> (original language)</xsl:text>
				</xsl:if>
				<xsl:choose>
				<xsl:when test="position()=last()">
					<xsl:text>.</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text> ; </xsl:text>
				</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="tag_102">
		<xsl:if test="datafield[@tag=102]">
			<xsl:for-each select="datafield[@tag=102]">
			<xsl:for-each select="subfield">
				<xsl:call-template name="code102">
					<xsl:with-param name="value"><xsl:value-of select="."/></xsl:with-param>
				</xsl:call-template>
				<xsl:choose>
				<xsl:when test="position()=last()">
					<xsl:text>.</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text> ; </xsl:text>
				</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="tag_205">
    <xsl:for-each select="datafield[@tag=205]">
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
		<xsl:if test="not (position() = last())">
			<xsl:text>. </xsl:text>
		</xsl:if>
		<xsl:if test="not (position() = last())">
			<xsl:text>. </xsl:text>
		</xsl:if>
		<br/>
    </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="tag_208">
		<xsl:for-each select="datafield[@tag=208]">
			<xsl:for-each select="subfield">
			<xsl:choose>
				<xsl:when test="@code='a'">
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='d'">
					<xsl:text> = </xsl:text>
					<xsl:value-of select="."/>
				</xsl:when>
			</xsl:choose>
			</xsl:for-each>
			<xsl:if test="not (position() = last())">
				<xsl:text>. </xsl:text>
			</xsl:if>
		</xsl:for-each>
		<br/>
	</xsl:template>
	
	<xsl:template name="tag_210">
    <xsl:for-each select="datafield[@tag=210]">
		<xsl:call-template name="addClassRtl" />
		
		<xsl:for-each select="subfield[(@code='a') or (@code='b') or (@code='c') or (@code='d')]">
			<xsl:choose>
				<xsl:when test="@code='c'">
					<xsl:if test="position()>1">
						<xsl:text> : </xsl:text>
					</xsl:if>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:when test="@code='d'">
					<xsl:if test="position()>1">
						<xsl:text>, </xsl:text>
					</xsl:if>
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="position()>1">
						<xsl:text> ; </xsl:text>
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
	</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="tag_215">
    <xsl:for-each select="datafield[@tag=215]">
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
		<xsl:if test="not (position() = last())">
			<xsl:text>. </xsl:text>
		</xsl:if>
    </xsl:for-each>
	</xsl:template>

	<xsl:template name="show225">
		<xsl:param name="fields225" />
		<xsl:param name="tr_id" />
		<xsl:param name="tdclass" />
		<xsl:for-each select="$fields225">
			<div class="row attribute-row py-2" id="{$tr_id}{position()}">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					<xsl:call-template name="labels225">
						<xsl:with-param name="value"><xsl:value-of select="@tag"/> </xsl:with-param>
					</xsl:call-template>
				</div>
				<div class="col-md-10 col-9 strSizeOnly {$tdclass}">
					<xsl:call-template name="tag_one_225"/>
				</div>				
			</div>		
		</xsl:for-each>
	</xsl:template>	

	
	<xsl:template name="show4xx">
		<xsl:param name="fields4xx" />
		<xsl:param name="tr_id" />
		<xsl:param name="tdclass" />
		<xsl:param name="ldr_hrc" />
		<xsl:for-each select="$fields4xx">
			<div class="row attribute-row py-2" id="{$tr_id}{position()}">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					<xsl:call-template name="labels4xx">
						<xsl:with-param name="value"><xsl:value-of select="@tag"/> </xsl:with-param>
						<xsl:with-param name="hrc" select="$ldr_hrc"/>
					</xsl:call-template>
				</div>
				<div class="col-md-10 col-9 strSizeOnly {$tdclass}">
					<xsl:call-template name="tag_title_4xx">
						<xsl:with-param name="tag"><xsl:value-of select="@tag"/> </xsl:with-param>
					</xsl:call-template>
				</div>				
			</div>			
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="show5xx">
		<xsl:param name="fields5xx" />
		<xsl:param name="tr_id" />
		<xsl:param name="tdclass" />
		<xsl:for-each select="$fields5xx">
			<div class="row attribute-row py-2" id="{$tr_id}{position()}">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					<xsl:call-template name="labels5xx">
						<xsl:with-param name="value"><xsl:value-of select="@tag"/> </xsl:with-param>
					</xsl:call-template>
				</div>
				<div class="col-md-10 col-9 strSizeOnly {$tdclass}">
					<xsl:call-template name="tag_title_5xx"/>
				</div>				
			</div>			
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="tag_606">
		<xsl:for-each select="datafield[@tag='606']">
			<xsl:call-template name="addClassRtl" />
			<xsl:choose>
				<xsl:when test="subfield[@code=3]">
					<a>
					<xsl:attribute name="href"><xsl:value-of select="$linktosearch"/>v=l&amp;q=&amp;h=any_bc&amp;s=10&amp;o=score&amp;f=heading_subject_id:<xsl:value-of select="subfield[@code='3']"/></xsl:attribute>
					<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
					<xsl:for-each select="subfield">
						<xsl:choose>
							<xsl:when test="@code='a'">
								<xsl:value-of select="."/>
							</xsl:when>
							<xsl:when test="@code='x'">
								<xsl:text> - </xsl:text>
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
								<xsl:value-of select="."/>
							</xsl:when>
							<xsl:when test="@code='x'">
								<xsl:text> - </xsl:text>
								<xsl:value-of select="."/>
							</xsl:when>
						</xsl:choose>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="not (position() = last())">
				<br/>
			</xsl:if>
		</xsl:for-each>	
	</xsl:template>
	
	<xsl:template name="tag_620">
		<xsl:for-each select="datafield[@tag='620']">
			<xsl:call-template name="addClassRtl" />
			<xsl:for-each select="subfield">
				<xsl:choose>
					<xsl:when test="@code='a'">
						<xsl:value-of select="."/>
							<xsl:if test="not (position() = last())">
								<xsl:text> - </xsl:text>
							</xsl:if>
					</xsl:when>
					<xsl:when test="@code='d'">
						<xsl:value-of select="."/>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
			<xsl:if test="not (position() = last())">
				<br/>
			</xsl:if>
		</xsl:for-each>	
	</xsl:template>
	
	<xsl:template name="tag_676">
		<xsl:for-each select="datafield[@tag='676']">
			<xsl:call-template name="addClassRtl" />
			<a>
			<xsl:attribute name="href"><xsl:value-of select="$linktosearch"/>v=l&amp;q=&amp;h=any_bc&amp;s=10&amp;o=score&amp;f=dewey:<xsl:value-of select="subfield[@code='a']"/></xsl:attribute>
			<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
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
			</a>
			<xsl:if test="not (position() = last())">
				<br/>
			</xsl:if>
		</xsl:for-each>	
	</xsl:template>
	
	<xsl:template name="tag_689">
		<xsl:for-each select="datafield[@tag='689']">
			<xsl:call-template name="addClassRtl" />
			<xsl:choose>
				<xsl:when test="subfield[@code=3]">
					<a>
					<xsl:attribute name="href"><xsl:value-of select="$linktosearch"/>v=l&amp;q=&amp;h=any_bc&amp;s=10&amp;o=score&amp;f=heading_miur_id:<xsl:value-of select="subfield[@code='3']"/></xsl:attribute>
					<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
					<xsl:for-each select="subfield">
						<xsl:choose>
							<xsl:when test="@code='a'">
								<xsl:value-of select="."/>
							</xsl:when>
							<xsl:when test="@code='b'">
								<xsl:text> - </xsl:text>
								<xsl:value-of select="."/>
							</xsl:when>
							<xsl:when test="@code='9'">
								<xsl:text> - </xsl:text>
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
								<xsl:value-of select="."/>
							</xsl:when>
							<xsl:when test="@code='b'">
								<xsl:text> - </xsl:text>
								<xsl:value-of select="."/>
							</xsl:when>
							<xsl:when test="@code='9'">
								<xsl:text> - </xsl:text>
								<xsl:value-of select="."/>
							</xsl:when>
						</xsl:choose>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="not (position() = last())">
				<br/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="tag_928">
		<xsl:param name="fields928" />
		<xsl:param name="tr_id" />
		<xsl:param name="tdclass" />
		<xsl:param name="label" />
		<xsl:for-each select="$fields928">
			<div class="row attribute-row py-2" id="{$tr_id}{position()}">
				<div class="col-md-2 col-3 label-search strSizeOnly">
					<xsl:value-of select="$label"/>
				</div>
				<div class="col-md-10 col-9 strSizeOnly {$tdclass}">
					<xsl:for-each select="subfield">
						<xsl:choose>
							<xsl:when test="@code='a'">
								<xsl:text>Forma: </xsl:text>
								<xsl:value-of select="."/>
								<xsl:if test="not (position() = last())">
									<xsl:text> - </xsl:text>
								</xsl:if>
							</xsl:when>
							<xsl:when test="@code='b'">
								<xsl:text>Organico Sintetico: </xsl:text>
								<xsl:value-of select="."/>
								<xsl:if test="not (position() = last())">
									<xsl:text> - </xsl:text>
								</xsl:if>
							</xsl:when>
							<xsl:when test="@code='c'">
								<xsl:text>Organico Analitico: </xsl:text>
								<xsl:value-of select="."/>
								<xsl:if test="not (position() = last())">
									<xsl:text> - </xsl:text>
								</xsl:if>
							</xsl:when>
						</xsl:choose>
					</xsl:for-each>
				</div>				
			</div>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="show_authors">
		<xsl:param name="fields7xx" />
		<xsl:param name="label" />
		<xsl:param name="tr_id" />
		<xsl:param name="tdclass" />
		<xsl:param name="type" />
		
		<div class="row attribute-row py-2" id="{$tr_id}{position()}">
			<div class="col-md-2 col-3 label-search strSizeOnly">
				<xsl:value-of select="$label"/>
			</div>
			<div class="col-md-10 col-9 strSizeOnly {$tdclass}">
				<xsl:for-each select="$fields7xx">
					<xsl:choose>
						<xsl:when test="subfield[@code=3]">
							<a>
							<xsl:attribute name="href">
								<xsl:value-of select="$linktosearch"/>v=l&amp;q=&amp;h=any_bc&amp;s=10&amp;o=score&amp;f=heading_author_id:<xsl:value-of select="subfield[@code=3]"/>  
							</xsl:attribute>
							<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
							<xsl:for-each select="subfield">
								<xsl:choose>
									<xsl:when test="@code='a' or @code='b' or @code='c' or @code='d' or @code='e' or @code='f'">
										<xsl:value-of select="."/>
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>
							</a>
							<xsl:if test="$type='not_aut'">
								<xsl:if test="subfield[@code=4]">
									<xsl:if test="not(subfield[@code=5])">
										<xsl:text> [</xsl:text>
										<xsl:call-template name="code7xx">
											<xsl:with-param name="value"><xsl:value-of select="subfield[@code=4]"/></xsl:with-param>
										</xsl:call-template>
										<xsl:text>]</xsl:text>
									</xsl:if>
								</xsl:if>
								<xsl:if test="subfield[@code=5]">
									<xsl:if test="not(subfield[@code=4])">
										<xsl:text> [Item in: </xsl:text>
										<xsl:value-of select="subfield[@code=5]"/>
										<xsl:text>]</xsl:text>
									</xsl:if>
									<xsl:if test="subfield[@code=4]">
										<xsl:text> [</xsl:text>
										<xsl:call-template name="code7xx">
											<xsl:with-param name="value"><xsl:value-of select="subfield[@code=4]"/></xsl:with-param>
										</xsl:call-template>
										<xsl:text> item in: </xsl:text>
										<xsl:value-of select="subfield[@code=5]"/>
										<xsl:text>]</xsl:text>
									</xsl:if>
								</xsl:if>
							</xsl:if>
							</xsl:when>
						<xsl:otherwise>
							<xsl:for-each select="subfield">
								<xsl:choose>
									<xsl:when test="@code='a' or @code='b' or @code='c' or @code='d' or @code='e' or @code='f'">
										<xsl:value-of select="."/>
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>
						</xsl:otherwise>
					</xsl:choose>
					<br/>				
				</xsl:for-each>
			</div>				
		</div>	
	</xsl:template>
	
	<xsl:template name="tag_856">
		<xsl:param name="label" />
		<xsl:for-each select="datafield[@tag=856]">
			<a>
			<xsl:attribute name="href">
				<xsl:value-of select="subfield[@code='u']"/>
			</xsl:attribute>
			<xsl:attribute name="target">_blank</xsl:attribute>
			<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
			<xsl:value-of select="$label"/>
			</a>
			<xsl:if test="not (position() = last())">
				<xsl:text> | </xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="tag_856MGDIG">
		<xsl:param name="tag"/>
		<xsl:param name="label1" />
		<xsl:param name="label2" />
		<xsl:if test="$tag/subfield[@code='u']">
			<a>
			<xsl:attribute name="href">
				<xsl:value-of select="$tag/subfield[@code='u']"/>
			</xsl:attribute>
			<xsl:attribute name="target">_blank</xsl:attribute>
			<img alt="" src="img/dep_dig.gif" width="20" height="20"/>
			</a>
			&#160; &#160;
			<a>
			<xsl:attribute name="href">
				<xsl:value-of select="$tag/subfield[@code='u']"/>
			</xsl:attribute>
			<xsl:attribute name="target">_blank</xsl:attribute>
			<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
			
			<xsl:value-of select="$label1"/>
			</a>			
		</xsl:if>
		
		<xsl:if test="$tag/subfield[@code='2']">
			<xsl:variable name="control" select="$tag/subfield[@code='2']"/>
			<xsl:if test="not(starts-with($control, 'http://tesi.depositolegale.it'))">
				<xsl:text>&#160; &#160; &#160; &#160;</xsl:text>
				<a>
				<xsl:attribute name="href">
					<xsl:value-of select="$tag/subfield[@code='2']"/>
				</xsl:attribute>
				<xsl:attribute name="target">_blank</xsl:attribute>
				<img alt="" src="img/inst_archive.gif" width="20" height="20"/>
				</a>
				&#160; &#160;
				<a>
				<xsl:attribute name="href">
					<xsl:value-of select="$tag/subfield[@code='2']"/>
				</xsl:attribute>
				<xsl:attribute name="target">_blank</xsl:attribute>
				<xsl:attribute name="class">colorLinkOnly</xsl:attribute>
				
				<xsl:value-of select="$label2"/>
				</a>
			</xsl:if>			
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="tag_fmt">
		<xsl:for-each select="datafield[@tag='FMT']">
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
		<xsl:if test="not (position() = last())">
			<xsl:text>, </xsl:text>
		</xsl:if>
		</xsl:for-each>	
	</xsl:template>

</xsl:stylesheet>
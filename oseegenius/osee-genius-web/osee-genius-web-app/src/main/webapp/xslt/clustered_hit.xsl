<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<xsl:apply-templates select="//hit"/>
	</xsl:template>
	<xsl:template match="//hit">
			<xsl:variable name="recid" select="recid/text()" />
		<div class="hit" id="hit">
				<table width="100%">
					<tr>
						<td width="5%" valign="top" align="center">
							<center><xsl:value-of select="position()" />/<xsl:value-of select="/show/merged/text()" /></center>
			            	<img width="4" height="1" src="img/blank.png"/>
						</td>
						<td width="96%" valign="top" style="border-bottom:1px solid #cccccc;">
							<div id="document_header">	
								<a class="document_title"><xsl:value-of select="md-title"/></a>
								<xsl:if test="md-author">
									<br/><a class="document_author"><xsl:value-of select="md-author"/></a>
								</xsl:if>
								<xsl:if test="md-corporate">
									<br/><a class="document_author"><xsl:value-of select="md-corporate"/></a>
								</xsl:if>
								<xsl:if test="md-conference">
									<br/><a class="document_author"><xsl:value-of select="md-conference"/></a>
								</xsl:if>
							</div>
							<table width="100%" cellpadding="0" cellspacing="0" >
								<tr>
									<td>
										<table width="100%">
											<xsl:if test="md-publisher-name">
												<tr>
										    		<td valign="top" nowrap="nowrap" width="1">
										    			<span class="attribute_name">Editore</span>	
										    		</td>
													<td width="1"><img width="10" height="1" src="img/blank.png"/></td>
										    		<td>
														<span class="attribute_value"><xsl:value-of select="md-publisher-name"/></span>
										    		</td>
												</tr>	
											</xsl:if>
											<xsl:for-each select="location">
												<xsl:variable name="an" select="md-id/text()" />
												<xsl:variable name="offset"  select="position()" />
												<tr><td colspan="3">
												<div id="info" class="viewlet">
													<div class="viewlet_toggable_head">
														<h4>
															<a class="mark" style="display:none;" id="check_link" href="javascript:ftoggle('{$an}','{$recid}', '{$offset}')"><img style=" vertical-align: bottom;" id="check_{$an}" src="img/checked_false.png"/></a>
															<img width="5" height="1" src="img/blank.png"/>
															<xsl:value-of select="./@name"/>
														</h4>
													</div>
													<div>
														<table>
															<tr>
																<td valign="top" nowrap="nowrap">
													    			<span class="attribute_name">ID:</span>	
													    		</td>
																<td><img width="5" height="1" src="img/blank.png"/></td>
													    		<td>
																	<span class="attribute_value"><xsl:value-of select="md-id/text()"/></span>
													    		</td>
															</tr>
															<xsl:if test="./md-publication-place">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">Luogo di pubblicazione:</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value"><xsl:value-of select="./md-publication-place/text()"/></span>
														    		</td>
																</tr>
															</xsl:if>		
															<xsl:if test="./md-publication-date">												
															<tr>
																<td valign="top" nowrap="nowrap">
													    			<span class="attribute_name">Data di pubblicazione:</span>	
													    		</td>
																<td><img width="5" height="1" src="img/blank.png"/></td>
													    		<td>
																	<span class="attribute_value"><xsl:value-of select="./md-publication-date/text()"/></span>
													    		</td>
															</tr>
															</xsl:if>
															<xsl:if test="./md-issn">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">ISSN:</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-issn">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
															<xsl:if test="./md-isbn">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">ISBN:</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-isbn">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
															<xsl:if test="md-physical-description">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">Descrizione fisica:</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:value-of select="./md-physical-description/text()"/>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
															<xsl:if test="./md-subject-long">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">Soggetti:</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-subject-long">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
															
															<xsl:if test="./md-other-author">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">Altri autori:</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-other-author">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
															<xsl:if test="./md-other-corporate">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">Altri autori (enti):</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-other-corporate">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
														<xsl:if test="./md-other-conference">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">Altri autori (convegni):</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-other-conference">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>															
															<xsl:if test="./md-collocation">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">Collocazione:</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-collocation">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
															
															<xsl:if test="./md-description">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name">Note:</span>	
														    		</td>
																	<td><img width="5" height="1" src="img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-description">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
														</table>
													</div>
												</div>
												</td></tr>
											</xsl:for-each>
										</table>
									</td>
								</tr>
							</table>				
						</td>
					</tr>
				</table>
		</div>		
	</xsl:template>
</xsl:stylesheet>
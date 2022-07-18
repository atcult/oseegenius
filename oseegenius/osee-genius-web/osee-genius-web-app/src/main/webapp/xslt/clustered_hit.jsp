<%@page contentType="text/xml; charset=UTF-8" %>
<%@page import="com.atc.osee.web.model.Visit" %>
<%@page import="java.util.*" %>

<%
	Visit visit = (Visit)session.getAttribute("visit");
	Locale locale = visit.getPreferredLocale();
	ResourceBundle messages = ResourceBundle.getBundle("resources", locale);
	String contextPath = request.getContextPath();
%>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<xsl:apply-templates select="//hit"/>
	</xsl:template>
	<xsl:template match="//hit">
		<xsl:variable name="recid" select="recid/text()" />
		<xsl:variable name="offset" select="<%=request.getParameter("offset") %>" /> 
		<div class="hit" id="hit">
				<table width="100%">
					<tr>
						<td width="5%" valign="top" align="center">
							<center><xsl:value-of select="position() + $offset" />/<xsl:value-of select="/show/total/text()" /></center>
			            	<img width="4" height="1" src="<%=contextPath %>/img/blank.png"/>
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
													<td width="1"><img width="10" height="1" src="<%=contextPath %>/img/blank.png"/></td>
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
															<img width="5" height="1" src="<%=contextPath %>/img/blank.png"/>
															<xsl:value-of select="./@name"/>
														</h4>
													</div>
													<div>
														<table>
															<tr>
																<td valign="top" nowrap="nowrap">
													    			<span class="attribute_name">ID:</span>	
													    		</td>
																<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
													    		<td>
																	<span class="attribute_value"><xsl:value-of select="md-id/text()"/></span>
													    		</td>
															</tr>
															<xsl:if test="./md-publication-place">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("publication_place")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value"><xsl:value-of select="./md-publication-place/text()"/></span>
														    		</td>
																</tr>
															</xsl:if>		
															<xsl:if test="./md-publication-date">												
															<tr>
																<td valign="top" nowrap="nowrap">
													    			<span class="attribute_name"><%=messages.getString("publication_date")%>:</span>	
													    		</td>
																<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
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
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
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
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
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
														    			<span class="attribute_name"><%=messages.getString("physical_description")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:value-of select="./md-physical-description/text()"/>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
															<xsl:if test="./md-subject_person">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("subject_person")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-subject_person">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
															<xsl:if test="./md-subject_corporate">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("subject_corporate")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-subject_corporate">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>															
															<xsl:if test="./md-subject_conference">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("subject_conference")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-subject_conference">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>	
															<xsl:if test="./md-chronological_subject">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("chronological_subject")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-chronological_subject">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>		
															<xsl:if test="./md-subject_uniform_title">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("subject_uniform_title")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-subject_uniform_title">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>				
															<xsl:if test="./md-topical_subject">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("topical_subject")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-topical_subject">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>				
															<xsl:if test="./md-geographic_subject">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("geographic_subject")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-geographic_subject">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>	
															<xsl:if test="./md-genre_form_subject">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("genre_form_subject")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-genre_form_subject">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>		
															<xsl:if test="./md-index_term">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("index_term")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
														    		<td>
																		<span class="attribute_value">
																			<xsl:for-each select="./md-index_term">
																				<xsl:value-of select="./text()"/><br/>
																			</xsl:for-each>
																		</span>
														    		</td>
																</tr>
															</xsl:if>
															<xsl:if test="./md-other-author">
																<tr>
																	<td valign="top" nowrap="nowrap">
														    			<span class="attribute_name"><%=messages.getString("other_author_person")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
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
														    			<span class="attribute_name"><%=messages.getString("other_author_corporate")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
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
														    			<span class="attribute_name"><%=messages.getString("other_author_conference")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
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
														    			<span class="attribute_name"><%=messages.getString("collocation")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
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
														    			<span class="attribute_name"><%=messages.getString("note")%>:</span>	
														    		</td>
																	<td><img width="5" height="1" src="<%=contextPath %>/img/blank.png"/></td>
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
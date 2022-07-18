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
			 <div class="item clear mb-3">
				 <span class="row title strSizeOnly" style="color: #507B9A"><b><xsl:value-of select="md-title"/></b></span>
				<xsl:if test="md-author">
					<span class="row edition strSizeOnly"><xsl:value-of select="md-author"/></span>
				</xsl:if>
				<xsl:if test="md-corporate">
					<span class="row edition strSizeOnly"><xsl:value-of select="md-corporate"/></span>					
				</xsl:if>
				<xsl:if test="md-conference">
					<span class="row edition strSizeOnly"><xsl:value-of select="md-conference"/></span>											
				</xsl:if>
				<xsl:if test="md-publisher-name">
					<span class="publisher strSizeColor"><xsl:value-of select="md-publisher-name"/></span>		
				</xsl:if>
				<table width="100%" cellpadding="0" cellspacing="0" >
					<tr>
						<td>
							<div class="group strSizeColor">
								<table width="100%" cellpadding="0" cellspacing="0" >
									<xsl:for-each select="location">
										<xsl:variable name="an" select="normalize-space(md-id/text())" />
										<xsl:variable name="offset"  select="position() " />
										<tr>
											<td>
												<table>
													<tr>
														<td valign="top" align="left">
															<a class="mark" style="display:none;" id="check_link" href="javascript:ftoggle('{$an}','{$recid}', '{$offset}')"><img style=" vertical-align: top;" id="check_{$an}" src="img/checked_false.png"/></a>
														</td>
														<td>	
															<xsl:value-of select="./@name"/>
														</td>
														<td></td>
													</tr>
													<xsl:if test="./md-publication-place">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute"><%=messages.getString("publication_place")%>:</span>	
												    		</td>
												    		<td>
																<xsl:value-of select="./md-publication-place/text()"/>
												    		</td>
														</tr>
													</xsl:if>										
													<xsl:if test="./md-publication-date">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute"><%=messages.getString("publication_date")%>:</span>	
												    		</td>
												    		<td>
																<xsl:value-of select="./md-publication-date/text()"/>
												    		</td>
														</tr>
													</xsl:if>
													<xsl:if test="./md-issn">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute">ISSN:</span>	
												    		</td>
												    		<td>
																<xsl:for-each select="./md-issn">
																	<xsl:value-of select="./text()"/><br/>
																</xsl:for-each>
												    		</td>
														</tr>
													</xsl:if>
													<xsl:if test="./md-isbn">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute">ISBN:</span>	
												    		</td>
												    		<td>
																<xsl:for-each select="./md-isbn">
																	<xsl:value-of select="./text()"/><br/>
																</xsl:for-each>
												    		</td>
														</tr>
													</xsl:if>													
													<xsl:if test="./md-physical-description">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute"><%=messages.getString("physical_description")%>:</span>	
												    		</td>
												    		<td>
																<xsl:value-of select="./md-physical-description/text()"/>
												    		</td>
														</tr>
													</xsl:if>
													<xsl:if test="./md-subject-long">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute"><%=messages.getString("sub_bc")%></span>	
												    		</td>
												    		<td>
																<xsl:for-each select="./md-subject-long">
																	<xsl:value-of select="./text()"/><br/>
																</xsl:for-each>
												    		</td>
														</tr>
													</xsl:if>
													<xsl:if test="./md-other-author or ./md-other-corporate or ./md-other-conference">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute"><%=messages.getString("other_author")%>:</span>	
												    		</td>
												    		<td>
																<xsl:for-each select="./md-other-author">
																	<xsl:value-of select="./text()"/><br/>
																</xsl:for-each>
																<xsl:for-each select="./md-other-corporate">
																	<xsl:value-of select="./text()"/><br/>
																</xsl:for-each>
																<xsl:for-each select="./md-other-conference">
																	<xsl:value-of select="./text()"/><br/>
																</xsl:for-each>																																
												    		</td>
														</tr>
													</xsl:if>
													<xsl:if test="./md-collocation">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute"><%=messages.getString("collocation")%>:</span>	
												    		</td>
												    		<td>
																<xsl:for-each select="./md-collocation">
																	<xsl:value-of select="./text()"/><br/>
																</xsl:for-each>
												    		</td>
														</tr>
													</xsl:if>													
													<xsl:if test="./md-description">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute"><%=messages.getString("note")%>:</span>	
												    		</td>
												    		<td>
																<xsl:for-each select="./md-description">
																	<xsl:value-of select="./text()"/><br/>
																</xsl:for-each>
												    		</td>
														</tr>
													</xsl:if>				
													<xsl:if test="./md-electronic-url">
														<tr>
															<td><img width="5" height="1" src="img/blank.png"/></td>
															<td valign="top" nowrap="nowrap">
												    			<span class="attribute">URL:</span>	
												    		</td>
												    		<td>
																<xsl:for-each select="./md-electronic-url">
																	<xsl:value-of select="./text()"/><br/>
																</xsl:for-each>
												    		</td>
														</tr>
													</xsl:if>						
												</table>
											</td>
										</tr>
									</xsl:for-each>
								</table>
							</div>
						</td>
					</tr>		
				</table>
		</div>		
	</xsl:template>
</xsl:stylesheet>
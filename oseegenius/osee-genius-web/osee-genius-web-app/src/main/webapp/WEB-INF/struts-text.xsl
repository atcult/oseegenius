<?xml version="1.0" encoding="UTF-8"?>
<?altova_samplexml struts-config.xml?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="lastIndexOf">
		<!-- declare that it takes two parameters - the string and the subs -->
		<xsl:param name="string" />
		<xsl:param name="subs" />
		<xsl:choose>
			<!-- if the string contains the character... -->
			<xsl:when test="contains($string, $subs)">
				<!-- call the template recursively... -->
				<xsl:call-template name="lastIndexOf">
					<!-- with the string being the string after the character
					-->
					<xsl:with-param name="string" select="substring-after($string, $subs)" />
					<!-- and the character being the same as before -->
					<xsl:with-param name="subs" select="$subs" />
				</xsl:call-template>
			</xsl:when>
			<!-- otherwise, return the value of the string -->
			<xsl:otherwise>
				<xsl:value-of select="$string" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="action_description">
		<xsl:choose>
			<xsl:when test="contains(./description,'@unused')">
				(
				<img src="theme/validator.gif" height="10" width="10" />
				unused?)
				<xsl:value-of
					select="substring-after(description,'@unused')" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="description" />
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
	<xsl:template name="mainform">
		<!-- declare that it takes two parameters - the string and the subs -->
		<xsl:param name="string" />
		<xsl:param name="subs" />
		<xsl:choose>
			<!-- if the string contains the character... -->
			<xsl:when test="contains($string, $subs)">
				<li />
				<xsl:value-of select="substring-before($string, $subs)" />
				<!-- call the template recursively... -->
				<xsl:call-template name="mainform">
					<!-- with the string being the string after the character
					-->
					<xsl:with-param name="string" select="substring-after($string, $subs)" />
					<!-- and the character being the same as before -->
					<xsl:with-param name="subs" select="$subs" />
				</xsl:call-template>
			</xsl:when>
			<!-- otherwise, return the value of the string -->
			<xsl:otherwise>
				<li />
				<xsl:value-of select="$string" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="to_global_forward">
		<xsl:param name="path" />
		<xsl:choose>
			<xsl:when test="not(contains($path,'.'))">
				<a>
					<xsl:attribute name="href">
						#fwd_
						<xsl:value-of select="$path" />
					</xsl:attribute>
					<img src="theme/forward.gif" border="0" width="15" height="10" />
					<xsl:value-of select="$path" />

				</a>
			</xsl:when>
			<xsl:otherwise>
				<img src="theme/jsp.gif" border="0" />
				<xsl:value-of select="$path" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="isComplexBuilder">
		<xsl:for-each select="set-property">
			<xsl:if test="@property='builderName' and not(@value='')">implements SbnComplexObjectBuilder</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="forward">
		<tr>
			<td width="25%" class="forward_name">
				<xsl:if test="starts-with(@name,'fail')">
					<img src="theme/error.gif" border="0" height="10" width="10" />
				</xsl:if>
				<img src="theme/forward.gif" />
				<xsl:value-of select="@name" />
			</td>
			<td width="75%" class="forward_path">
				<xsl:choose>
					<xsl:when test="contains(@path,'.do')">
						<img src="theme/action.gif" height="12" width="15" />
						<a>
							<xsl:attribute name="href">#<xsl:value-of select="substring-before(@path, '.do')" />
							</xsl:attribute>
							<xsl:value-of select="substring-before(@path, '.do')" />
						</a>
						<img src="theme/forward.gif" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="to_global_forward">
							<xsl:with-param name="path">
								<xsl:value-of select="@path" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="rowProperty">
		<xsl:param name="prop"></xsl:param>
		<tr>
			<td class="setproperty_property">
				<xsl:value-of select="substring-before($prop, '=')" />
			</td>
			<td class="setproperty_value">
				<xsl:value-of select="substring-after($prop, '=')" />
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="parseProperty">
		<xsl:param name="prop"></xsl:param>
		<xsl:choose>
			<xsl:when test="contains($prop,',')">
				<!-- call the template recursively... -->
				<xsl:call-template name="parseProperty">
					<xsl:with-param name="prop" select="substring-after($prop, ',')" />
				</xsl:call-template>
				<xsl:call-template name="rowProperty">
					<xsl:with-param name="prop">
						<xsl:value-of select="substring-before($prop, ',')" />
					</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="rowProperty">
					<xsl:with-param name="prop">
						<xsl:value-of select="$prop" />
					</xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:key name="action-by-path" match="action" use="substring-before(substring(@path,2),'/')" />
	<xsl:key name="action-by-object" match="action" use="substring-before(substring(substring-after(@path, substring-before(substring(@path,2),'/')),2),'/')" />
	<xsl:key name="action-by-name" match="action" use="@name" />
	<xsl:key name="action-by-type" match="action" use="@type" />
	<xsl:key name="forms-by-type" match="form-bean" use="@type" />
	
	<xsl:template name="forEachActionGroup">
		<OL>
		<H4>
			<xsl:for-each select="action[count(. | key('action-by-path', substring-before(substring(@path,2),'/'))[1]) = 1]">
				<xsl:if test="substring-before(substring(@path,2),'/')">
				<LI><a class="name">
					<xsl:attribute name="href">#group1<xsl:value-of select="translate(@path,'/','_')" /></xsl:attribute>
					<xsl:value-of select="substring-before(substring(@path,2),'/')" />
				</a></LI></xsl:if>
			</xsl:for-each>
		</H4>
		</OL>
	</xsl:template>
	<xsl:template name="actionIndex">
		<xsl:for-each select="action[count(. | key('action-by-path', substring-before(substring(@path,2),'/'))[1]) = 1]">
			<BR />
			<a class="name">
				<xsl:attribute name="name">#group1<xsl:value-of select="translate(@path,'/','_')" /></xsl:attribute>

				<H4>
					<xsl:value-of select="substring-before(substring(@path,2),'/')" />
				</H4>
			</a>
			<table width="80%" border="0" cellspacing="0" cellpadding="0" class="actiontable">
				<xsl:for-each select="key('action-by-path', substring-before(substring(@path,2),'/'))">
					<xsl:sort select="@path" />
					<tr>
						<td width="20">
							<xsl:value-of select="position()" />
						</td>
						<xsl:choose>
							<xsl:when test="position() mod 2 = 0">

								<td class="descpath_low">
									<a href="#{@path}">
										<xsl:value-of select="@path" />
									</a>
								</td>
								<td class="desc_low">
									<xsl:call-template name="action_description" />
								</td>

							</xsl:when>
							<xsl:otherwise>
								<td class="descpath_hi">
									<a href="#{@path}">
										<xsl:value-of select="@path" />
									</a>
								</td>
								<td class="desc_hi">
									<xsl:call-template name="action_description" />
								</td>


							</xsl:otherwise>
						</xsl:choose>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="forEachActionGroupObject">
		<OL>
		<H4>
		<xsl:for-each select="action[count(. | key('action-by-object',substring-before(substring(substring-after(@path, substring-before(substring(@path,2),'/')),2),'/'))[1]) = 1]">
				<xsl:if test="substring-before(substring(@path,2),'/')">
				<LI><a class="name">
					<xsl:attribute name="href">#group2<xsl:value-of select="translate(@path,'/','_')" /></xsl:attribute>
					<xsl:value-of select="substring-before(substring(@path,2),'/')" />.<xsl:value-of select="substring-before(substring(substring-after(@path, substring-before(substring(@path,2),'/')),2),'/')" />
				</a></LI></xsl:if>
			</xsl:for-each>
		</H4>
		</OL>
	</xsl:template>
	<xsl:template name="actionIndexObjects">
		<xsl:for-each select="action[count(. | key('action-by-object',substring-before(substring(substring-after(@path, substring-before(substring(@path,2),'/')),2),'/'))[1]) = 1]">
			<BR />
			<a class="name">
				<xsl:attribute name="name">#group2<xsl:value-of select="translate(@path,'/','_')" /></xsl:attribute>
				<H4>
					<xsl:value-of select="substring-before(substring(@path,2),'/')" />.<xsl:value-of select="substring-before(substring(substring-after(@path, substring-before(substring(@path,2),'/')),2),'/')" />
				</H4>
			</a> 
			<table width="80%" border="0" cellspacing="0" cellpadding="0" class="actiontable">
				<xsl:for-each select="key('action-by-object', substring-before(substring(substring-after(@path, substring-before(substring(@path,2),'/')),2),'/'))">
					<xsl:sort select="@path" />
					<tr>
						<td width="20">
							<xsl:value-of select="position()" />
						</td>
						<xsl:choose>
							<xsl:when test="position() mod 2 = 0">

								<td class="descpath_low">
									<a href="#{@path}">
										<xsl:value-of select="@path" />
									</a>
								</td>
								<td class="desc_low">
									<xsl:call-template name="action_description" />
								</td>

							</xsl:when>
							<xsl:otherwise>
								<td class="descpath_hi">
									<a href="#{@path}">
										<xsl:value-of select="@path" />
									</a>
								</td>
								<td class="desc_hi">
									<xsl:call-template name="action_description" />
								</td>


							</xsl:otherwise>
						</xsl:choose>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="forEachFormGroup">
		<A name="#formgroup" ><H3>Form Groups</H3></A>
		<table width="60%" border="0" cellspacing="2" cellpadding="2" class="actiontable">
			<xsl:for-each select="action[count(. | key('action-by-name', @name)[1]) = 1]">
				<xsl:sort select="@name" />
				<xsl:if test="count(key('action-by-name', @name))>0">
					<tr >
						<td class="multiline" >
							<a class="name">
								<xsl:attribute name="name">#groupform_<xsl:value-of select="@name" />
								</xsl:attribute>
								<xsl:value-of select="@name" />
							</a>
						</td>
						<td class="multiline" ><OL>
							<xsl:for-each select="key('action-by-name', @name)">
								<xsl:sort select="@name" />
								<LI><a href="#{@path}">
									<xsl:value-of select="@path" />
								</a>								
								<xsl:if test="count(key('action-by-name', @name))!=position()">, </xsl:if>
								</LI>
							</xsl:for-each></OL>
						</td>
					</tr>

				</xsl:if>
			</xsl:for-each>
		</table>
	</xsl:template>
	<xsl:template name="forEachFormGroup2">
		<A name="#formgrouptype" ><H3>Forms Group by Type</H3></A>
		<table width="60%" border="0" cellspacing="2" cellpadding="2" class="actiontable">
			<xsl:for-each select="form-bean[count(. | key('forms-by-type', @type)[1]) = 1]">
				<xsl:sort select="@type" />
				<xsl:if test="count(key('forms-by-type', @type))>0">
					<tr >
						<td class="multiline" >
							<a class="name">
								<xsl:attribute name="name">#groupform2_<xsl:value-of select="@type" />
								</xsl:attribute>
								<xsl:value-of select="@type" />
							</a>
						</td>
						<td class="multiline" ><OL>
							<xsl:for-each select="key('forms-by-type', @type)">
								<xsl:sort select="@type" />
								<LI><a href="#groupform_{@name}">
									<xsl:value-of select="@name" />
								</a>								
								<xsl:if test="count(key('action-by-name', @name))=0">
									(<img src="theme/validator.gif" height="10" width="10" />unused?)
								</xsl:if>
								<xsl:if test="count(key('forms-by-type', @type))!=position()">, </xsl:if>
								</LI>
							</xsl:for-each></OL>
						</td>
					</tr>

				</xsl:if>
			</xsl:for-each>
		</table>
	</xsl:template>
	
	<xsl:template name="forEachTypeGroup">
		<A name="#typegroup" ><H3>Action Type Groups</H3></A>
		<table width="60%" border="0" cellspacing="2" cellpadding="2" class="actiontable">
			<xsl:for-each select="action[count(. | key('action-by-type', @type)[1]) = 1]">
				<xsl:sort select="@type" />				
				<xsl:if test="count(key('action-by-type', @type))>0">
					<tr >
						<td class="multiline" >
							<a class="name">
								<xsl:attribute name="name">#grouptype_<xsl:value-of select="@type" />
								</xsl:attribute>
								<xsl:value-of select="@type" />
							</a>
						</td>
						<td class="multiline" ><OL>
							<xsl:for-each select="key('action-by-type', @type)">
								<xsl:sort select="@path" />
								<LI><a href="#{@path}">
									<xsl:value-of select="@path" />
								</a>								
								<xsl:if test="count(key('action-by-type', @type))!=position()"><xsl:text>, </xsl:text></xsl:if>
							</LI></xsl:for-each></OL>
						</td>
					</tr>

				</xsl:if>
			</xsl:for-each>
		</table>
	</xsl:template>

	<xsl:template name="tableAdapterParam">
		<xsl:param name="title">Adapter Settings</xsl:param>
		<xsl:param name="params"></xsl:param>
		<table border="0" class="dinamicbuilder">
			<th class="setproperty" colspan="2">
				<xsl:value-of select="$title" />
			</th>
			<xsl:call-template name="parseProperty">
				<xsl:with-param name="prop" select="$params"></xsl:with-param>
			</xsl:call-template>
		</table>
	</xsl:template>
	<xsl:template name="forEachActionMenu">
		<xsl:for-each select="action">
			<xsl:sort select="@path" />
			<tr>
				<td width="20">
					<xsl:value-of select="position()" />
				</td>
				<xsl:choose>
					<xsl:when test="position() mod 2 = 0">

						<td class="descpath_low">
							<a href="#{@path}">
								<xsl:value-of select="@path" />
							</a>
						</td>
						<td class="desc_low">
							<xsl:call-template name="action_description" />
						</td>

					</xsl:when>
					<xsl:otherwise>
						<td class="descpath_hi">
							<a href="#{@path}">
								<xsl:value-of select="@path" />
							</a>
						</td>
						<td class="desc_hi">
							<xsl:call-template name="action_description" />
						</td>


					</xsl:otherwise>
				</xsl:choose>
			</tr>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="forEachAdapter">
		<xsl:for-each select="set-property">
			<xsl:if test="@property = 'adapter' ">
				<tr>
					<td class="adapter">
						<img src="theme/adapter.gif" align="center" />
						<br />
						<xsl:call-template name="lastIndexOf">
							<xsl:with-param name="string" select="@value" />
							<xsl:with-param name="subs">.</xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="forEachBuilderName">
		<xsl:for-each select="set-property">
			<xsl:if test="@property = 'builderName' ">
				<tr>
					<td class="builder">
						<xsl:if test="not(@value='it.ps.sbnmarc.builders.DinamicBuilder')">
							<img src="theme/builder.gif" align="center" />
						</xsl:if>
						<br />
						<xsl:call-template name="lastIndexOf">
							<xsl:with-param name="string" select="@value" />
							<xsl:with-param name="subs">.</xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:if>

		</xsl:for-each>
	</xsl:template>
	<xsl:template name="forEachAdapterParam">
		<xsl:for-each select="set-property">
			<xsl:if test="@property = 'adapterParam' ">
				<tr>
					<td class="builder">
						<xsl:call-template name="tableAdapterParam">
							<xsl:with-param name="params">
								<xsl:value-of select="@value" />
							</xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="forEachSuccessField">
		<xsl:for-each select="set-property">
			<xsl:if test="@property = 'successField' ">
				<tr>
					<td class="switch" colspan="1">
						Si decide in base a
						<span class="parameter">
							<xsl:value-of select="@value" />
						</span>
					</td>
				</tr>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	

	<xsl:template name="ifDinamicBuilderActionMapping">
		<xsl:if test="@className = 'it.ps.struts.actions.DinamicBuilderActionMapping' ">
			<tr>
				<td>
					<xsl:call-template name="tableDinamicBuilder" />
				</td>
			</tr>
		</xsl:if>
	</xsl:template><xsl:template name="genericTableProperty">
		<xsl:param name="title">property</xsl:param>

		<table border="0" class="dinamicbuilder">
			<th class="setproperty" colspan="2">
				<xsl:value-of select="$title" />
			</th>
			<xsl:for-each select="set-property">
				<tr>
					<td class="setproperty_property">
						<xsl:value-of select="@property" />
					</td>
					<td class="setproperty_value">
						<xsl:value-of select="@value" />
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<xsl:template name="tableForms">
		<xsl:param name="title">properties</xsl:param>

		<table border="0" width="100%" class="dinamicbuilder">
			<th class="setproperty" colspan="4">
				<xsl:value-of select="$title" />
			</th>
			<xsl:for-each select="form-property">
				<tr>
					<td class="setproperty_property">
						<xsl:value-of select="@name" />
					</td>
					<td class="setproperty_value">
						<xsl:value-of select="@type" />
					</td>
					<td class="setproperty_value">
						<xsl:value-of select="@initial" />
					</td>
					<td class="setproperty_value">
						<xsl:value-of select="@size" />
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<xsl:template name="tableDinamicBuilder">
		<xsl:param name="title">DinamicBuilder</xsl:param>

		<table border="0" class="dinamicbuilder" align="center">
			<th class="setproperty" colspan="2">
				<img src="theme/builder.gif" align="center" />
				<BR />
				<xsl:value-of select="$title" />
				<xsl:call-template name="isComplexBuilder" />
			</th>
			<xsl:for-each select="set-property">
				<tr>
					<td class="setproperty_property">
						<xsl:value-of select="@property" />
					</td>
					<td class="setproperty_value">
						<xsl:value-of select="@value" />
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<xsl:template name="forEachMainForm">
		<xsl:for-each select="set-property">
			<xsl:if test="@property = 'mainForm' ">
				<tr>
					<td class="mainForm">
						<img src="theme/form.gif" align="center" />
						<span class="mainForm">
							MAINFORM:
							<xsl:choose>
								<xsl:when test="contains(@value,',')">
									<xsl:call-template name="mainform">
										<xsl:with-param name="string" select="@value" />
										<xsl:with-param name="subs">,</xsl:with-param>
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="@value" />
								</xsl:otherwise>
							</xsl:choose>
						</span>
					</td>
				</tr>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="ifValidate">
		<xsl:if test="@validate = 'true' ">
			<table>
				<tr>
					<td>
						<img src="theme/validator.gif" />
						<xsl:for-each select="set-property">
							<xsl:if test="@property = 'validateForm' ">
								<span class="parameter">
									[
									<xsl:value-of select="@value" />
									]
								</span>
							</xsl:if>
						</xsl:for-each>
					</td>
				</tr>
			</table>
		</xsl:if>
	</xsl:template>

	<xsl:template name="ifNoName">
		<xsl:if test="@name != '' ">
			<tr>
				<td class="form">
					<img src="theme/form.gif" />
				</td>
			</tr>
			<tr>
				<td class="form_name">
					<xsl:value-of select="@name" />
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<xsl:template name="ifNoScope">

		<xsl:if test="@scope != '' ">
			<tr>
				<td class="form_scope">
					<xsl:if test="@scope='session'">
						<img src="theme/session.gif" height="15" width="45" />
					</xsl:if>
					<xsl:if test="@scope='request'">
						<img src="theme/request.gif" height="15" width="30" />
					</xsl:if>
					<xsl:value-of select="@scope" />
					<BR />
				</td>
			</tr>
		</xsl:if>
	</xsl:template>

	<xsl:template name="ifForwardField">
		<xsl:if test="@forward">
			<tr>
				<td class="forward_special">
					<img src="theme/forward.gif" />
					inoltra su
				</td>
				<td class="forward_path">
					<xsl:call-template name="to_global_forward">
						<xsl:with-param name="path">
							<xsl:value-of select="@forward" />
						</xsl:with-param>
					</xsl:call-template>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>


	<xsl:template name="ifForwardAction">
		<xsl:if test="@type='org.apache.struts.actions.ForwardAction'">
			<tr>
				<td class="forward_special">
					<img src="theme/forward.gif" />
					inoltra su
				</td>
				<td class="forward_path">
					<xsl:call-template name="to_global_forward">
						<xsl:with-param name="path">
							<xsl:value-of select="@parameter" />
						</xsl:with-param>
					</xsl:call-template>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>

	<xsl:template name="ifInputForValidator">
		<xsl:if test="@input!=''+@validate='true' ">
			<!-- validation error forward -->
			<tr>
				<td class="forward_special">
					<img src="theme/validator.gif" height="10" width="10" />
					<img src="theme/forward.gif" />
					validation
				</td>
				<td class="forward_path">
					<xsl:call-template name="to_global_forward">
						<xsl:with-param name="path">
							<xsl:value-of select="@input" />
						</xsl:with-param>
					</xsl:call-template>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<xsl:template name="forEachOptionalProperties">
		<xsl:for-each select="set-property">
			<xsl:if test="@property = 'keyError' ">
				<tr><td class="property_name">In caso di errore generico viene recuperato il messaggio <B><xsl:value-of select="@value" /></B> dal bundle</td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'tipoControllo' ">
				<tr>
					<td class="property_name">tipoControllo assume <B><xsl:value-of select="@value" /></B></td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'keyValidationError' ">
				<tr>
					<td class="property_name">In caso di validazione di II livello non riuscita (Castor) viene recuperato il messaggio <B><xsl:value-of select="@value" /></B> dal bundle </td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'focusOn' ">
				<tr>
					<td class="property_name">In seguito ad errore il cursore verrà posizionato sul campo <B><xsl:value-of select="@value" /></B></td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'localForm' ">
				<tr>
					<td class="property_name">Utilizzerà una ActionForm locale registrata in sessione col nome <B><xsl:value-of select="@value" /></B></td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'authKey' ">
				<tr>
					<td class="property_name">Verrà eseguita solo se la chiave di autorizzazione <B><xsl:value-of select="@value" /></B> sarà presente nel profilo dell'utente</td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'formToCopy' ">
				<tr>
					<td class="property_name">Allo scopo di preservare l'operatività dei campi ripetuti (Auto-Growing) verrà utilizzata una copia della ActionForm originale e posta in sessione col nome di <B><xsl:value-of select="@value" /></B></td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'formToClean' ">
				<tr>
					<td class="property_name">Da qui ha inizio di una transazione secondaria. L'ActionForm <B><xsl:value-of select="@value" /></B> verrà preventivamente ripulita da eventuali dati precedenti.</td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'newTransactionFormName' ">
				<tr>
					<td class="property_name">Il nome della transazione che ha inizio è <B><xsl:value-of select="@value" /></B> corrispondente al nome della ActionForm che verrà utilizzata come form base della transazione</td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'childList' ">
				<tr>
					<td class="property_name">Verrà gestita una lista di oggetti secondari con chiave <B><xsl:value-of select="@value" /></B></td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'occursedChildList' ">
			<tr>
					<td class="property_name">Verrà gestita una lista di oggetti secondari <B>ripetuti</B>. L'assegnazione <B><xsl:value-of select="@value" /></B> comunicherà al builder di creare per il campo dichiarato un nuovo oggetto e registrarlo con la chiave assegnata</td>
				</tr>
			</xsl:if>
			<xsl:if test="@property = 'validatorName' ">
				<tr>
					<td class="property_name">Verrà utilizzato <B><xsl:value-of select="@value" /></B> come Validator di III Livello</td>
				</tr>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<!-- ***************** ACTIONS *******************	-->
	<xsl:template match="action-mappings">
		<!-- links -->
		<h2>ACTIONS</h2>
		<BR />
		<H4>
			<LI><A href="#formbean">Form Beans</A></LI>
			<LI><A href="#globalforward">Global Forwards</A></LI>
			<LI><A href="#formgroup">Azioni raggruppate per form</A></LI>
			<LI><A href="#typegroup">Azioni raggruppate per type</A></LI>
			<xsl:call-template name="forEachActionGroup" />
			<xsl:call-template name="forEachActionGroupObject" />
		</H4>
		<xsl:call-template name="actionIndex" />
		<xsl:call-template name="actionIndexObjects" />
		<h2>ACTION MAPPING</h2>
		<br />
		<hr />
		<!-- tutta l'azione -->
		<xsl:for-each select="action">
			<table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor="#d0f0d0">
				<tr>
					<td width="100%">
						<xsl:call-template name="action" />
					</td>
				</tr>
			</table>
			<hr />
			<br />
		</xsl:for-each>
		<xsl:call-template name="forEachTypeGroup" />
		<xsl:call-template name="forEachFormGroup" />

	</xsl:template>
	<xsl:template name="action">

		<a name="{@path}">
			<table align="left" width="100%" class="amrow">
				<tr>
					<td class="haction" />
					<td class="haction">
						<xsl:value-of select="@path" />
					</td>
					<!-- colonna NOTE -->
				</tr>
				<tr>
				<td/>
				<td colspan="2">		
				<xsl:if test="description">
					<xsl:call-template name="action_description" />
				</xsl:if>
				</td>
				</tr>
				<tr>
					<!-- colonna forms -->
					<td class="forms" width="25%">
						<table border="0" width="100%">
							<tr>
								<td class="form" width="100%">
									<table>
										<xsl:call-template name="ifNoName" />
										<xsl:call-template name="ifNoScope" />
									</table>
								</td>
							</tr>
							<tr>
								<td class="validator" width="100%">
									<xsl:call-template name="ifValidate" />
								</td>
							</tr>
							<xsl:call-template name="forEachMainForm" />
						</table>
					</td>
					<!-- colonna actions -->
					<td class="actions" width="25%">
						<table border="0" width="100%">
							<tr>
								<td class="action">
									<img src="theme/action.gif" />
									<br />
									<xsl:call-template name="lastIndexOf">
										<xsl:with-param name="string" select="@type" />
										<xsl:with-param name="subs">.</xsl:with-param>
									</xsl:call-template>
									<xsl:if test="@parameter != '' ">
										<span class="parameter">
											[
											<xsl:value-of select="@parameter" />
											]
										</span>
									</xsl:if>
								</td>
							</tr>
							<xsl:call-template name="forEachAdapter" />
							<xsl:call-template name="forEachAdapterParam" />
							<xsl:call-template name="forEachBuilderName" />
							<xsl:call-template name="ifDinamicBuilderActionMapping" />
						</table>
					</td>
					<!-- colonna forwards -->
					<td class="forwards" width="45%">
						<table width="100%" border="0">
							<xsl:if test="count(forward) >0">
								<xsl:call-template name="forEachSuccessField" />
								<xsl:for-each select="forward">
									<xsl:call-template name="forward" />
								</xsl:for-each>
							</xsl:if>
							<xsl:call-template name="ifForwardField" />
							<xsl:call-template name="ifForwardAction" />
							<xsl:call-template name="ifInputForValidator" />
						</table>
					</td>
				</tr>
				<!-- **** RIGA ALTRE NOTE OPZIONALI *** -->
				<tr>
					<td colspan="3">
						<table border='0' align="right" class="property">
							<xsl:call-template name="forEachOptionalProperties" />
						</table>
					</td>
				</tr>
			</table>
		</a>
	</xsl:template>
	<xsl:template name="set-property">
		<tr>
			<td>
				<img src="theme/property.gif" />
				<xsl:value-of select="@property" />
			</td>
			<td>
				<xsl:value-of select="@value" />
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="controller"></xsl:template>
	<xsl:template match="message-resources"></xsl:template>
	<!-- ***************** GLOBAL FORWARDS *******************	-->
	<xsl:template name="global-forwards">
		<br />
		<A name="globalforward" > <h2>GLOBAL FORWARDS</h2></A>
		<table>
			<xsl:for-each select="forward">
				<tr>
					<td width="20">
						<xsl:value-of select="position()" />
					</td>
					<td class="forward_name">
						<a name="#fwd_{@name}">
							<img src="theme/forward.gif" border="0" />
							<xsl:value-of select="@name" />
						</a>
					</td>
					<td class="forward_path">
						<xsl:choose>
							<xsl:when test="contains(@path,'.do')">
								<img src="theme/action.gif" height="12" width="15" />
								<xsl:value-of select="@path" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="to_global_forward">
									<xsl:with-param name="path">
										<xsl:value-of select="@path" />
									</xsl:with-param>
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<!-- ***************** FORM BEANS *******************	-->
	<xsl:template name="form-beans">
		<br />
		<A name="formbean" >
		<h2>
			<img src="theme/form.gif" border="0" />
			FORM BEANS
		</h2></A>
		<table width="90%">
			<xsl:for-each select="form-bean">
				<tr>
					<td class="bean_name">
						<xsl:value-of select="@name" />
					</td>
					<td class="bean">
						<xsl:value-of select="@type" />
					</td>
					<td class="bean">
						<xsl:if test="count(form-property)>0">
							<xsl:call-template name="tableForms">
								<xsl:with-param name="title">
									<xsl:value-of select="@name" />
									dinamic properties
								</xsl:with-param>
							</xsl:call-template>
						</xsl:if>
					</td>
					<td class="bean_name">
					<xsl:if test="count(key('action-by-name', @name))=0">
						<img src="theme/validator.gif" height="10" width="10" /> unused?
					</xsl:if>
					</td>
				</tr>
			</xsl:for-each>
		</table>
		<xsl:call-template name="forEachFormGroup2" />
	</xsl:template>
	<!-- ***************** MAIN *******************	-->
	<xsl:template match="/">
		<html>
			<head>
				<title>SBNWEB Struts-Config</title>
				<LINK rel="stylesheet" href="theme/gra.css" type="text/css" />
			</head>
			<body>
				<h1>Struts-Config (Graphic Mode)</h1>
				<xsl:for-each select="struts-config">
					<xsl:apply-templates />
					<xsl:for-each select="global-forwards">
						<xsl:call-template name="global-forwards" />
					</xsl:for-each>
					<hr />
					<xsl:for-each select="form-beans">
						<xsl:call-template name="form-beans" />
					</xsl:for-each>
					<hr />
					<HR />
					<BR />
				</xsl:for-each>
				<h3>
					Author:
					<a href="mailto:hgweb04-site@yahoo.com">Mercurio Michele</a>
					- (
					<a href="http://www.primesource.it">Prime Source srl</a>
					)
				</h3>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
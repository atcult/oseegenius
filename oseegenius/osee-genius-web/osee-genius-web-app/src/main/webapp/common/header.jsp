<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<html:xhtml/>

<bean:define id="crossBean" name="librisuite.bean.crossreference.CrossReferenceBean"/>
<bean:define id="localBean" name="librisuite.bean.locale.LocaleBean"/>

<div id="authority-logo">
	<a href="home" title="Home">
		<html:img src="img/logo.jpg"></html:img>	
	</a>	
</div>
<div style="font: bold 2em arial,sans-serif;margin-left: 190px;position: absolute; top: 45px; color:#00568E;">
 <logic:equal name="crossBean" property="crossReference" value="true" >
  <bean:message bundle="crossReference" key="title.cross.reference"/>
</logic:equal>
 <logic:equal name="crossBean" property="crossReference" value="false" >
	<bean:message bundle="browse" key="title.search"/>
 </logic:equal>
</div>

<div id="topmenu">	
	<ul>
		<li><a href="info.vm" title="Info">Info</a></li>
		<li class="last">
			<a href="mailto:biblioteca@museogalileo.it" title="<bean:message bundle="browse" key="contact_us"/>">
				<bean:message bundle="browse" key="contact_us"/>
			</a>
		</li>
	</ul>
</div>

<html:form action="/changeLocale.do">
<div id="languages">
	<logic:equal name="localBean" property="locale.language" value="en">
	    <a href="javascript:l10n('it')" title="Italiano"><img src="img/flag_it.png" width="14" height="13" alt="Italiano" /></a>
	</logic:equal>
	<logic:equal name="localBean" property="locale.language" value="it">
		<a href="javascript:l10n('en')" title="English"><img src="img/flag_en.png" width="14" height="13" alt="English" /></a>
	</logic:equal>
</div>
</html:form>
<div id="topmenu2">
	<table>
		<tr>
			<td>				
				<a  href="authHome" title="Archivio autori">
					<div class="blueButton">
						<bean:message bundle="browse" key="authority_link"/>
					</div>
				</a>
			</td>
			<td>
				<a data="advanced?a=reset" href="javascript:void(0);" title="Ricerca avanzata" onclick="javascript:goToOseeGenius(this)">
					<div class="blueButton">
						<bean:message bundle="browse" key="advanced_search"/>
					</div>
				</a>
			</td>
		</tr>
		<tr>
			<td></td>
			<td>
				<a data="search?q=&v=l&h=any_bc&s=25&o=score" href="javascript:void(0);" title="Torna al catalogo" onclick="javascript:goToOseeGenius(this)">
					<div class="blueButton">
						<bean:message bundle="browse" key="back_to_catalogue"/>
					</div>
				</a>
			</td>		
		</tr>		
	</table>
</div>
<script type="text/javascript">

function l10n(language) 
{
	document.forms[0].action = document.forms[0].action + "?method=refreshLanguage&language="+language;
	document.forms[0].submit();
}

</script>
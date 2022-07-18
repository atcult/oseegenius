<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<html:xhtml/>
	<!--  INIT FAILED -->
	<html:errors/>
  		<logic:present name="LAST_LOGON_MESSAGE">
  			<div id="errors"><TABLE cellapadding="0" cellaspacing="0" align="center" class="errortab" ><TR><TD class="ERROR">
  				<bean:write name="LAST_LOGON_MESSAGE" />
 			</TD></TR></TABLE></div>
 		</logic:present>
  		<!-- /INIT FAILED -->  		
<div id="logon">
	<html:form action="/logon/logon.do">
		<input type="hidden" name="changePassword"/>
		<div id="logonContent">
			<div id="logonWelcome"><h1><bean:message bundle="logon" key="welcome"/></h1></div>
			<div id="enterYourLogonData"><bean:message bundle="logon" key="enter.your.login.data"/></div>
				
			<fieldSet>
				<p class="namePassword"><bean:message bundle="logon" key="name"/></p>
				<p class="inputNamePassword">
						<input type="text" class="orange" name="name" 
							value="<bean:write name="librisuite.bean.logon.LogonBean" property="name"/>" 
							size="15"
							onKeyUp="javascript:this.value = this.value.toUpperCase();">
						</input></p>
				
				<p class="namePassword"><bean:message bundle="logon" key="password" /></p>
				<p class="inputNamePassword"><input class="orange" type="password" name="password" value="" size="15"></input></p>
				<p class="enterLibriSuite"><input class="orangeButton" type="submit" name="enterLibriSuiteButton" value="<bean:message bundle="logon" key="enter.librisuite"/>"></input></p>
			</fieldSet>	
					
			<div id="changeLocale">
				<html:select onchange="javascript: window.document.getElementById('changeLocaleButton').click()" name="librisuite.bean.locale.LocaleBean" property="locale">
					<html:optionsCollection name="librisuite.bean.locale.LocaleBean" property="availableLocales"/>
				</html:select>
				<input id="changeLocaleButton" class="button" type="submit" name="changeLocaleButton" value="<bean:message bundle="logon" key="change.locale"/>"></input>
			</div>
  		
  	
		</div>
	</html:form>
</div>

<script type="text/javascript">
//<![CDATA[
document.forms[0].name.focus();

function changePassword() {
   document.forms[0].changePassword.value='true';
   document.forms[0].submit();
}
//]]>
</script>

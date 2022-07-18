<%@ page language="java" contentType="text/html; charset=ISO-8859-1" isErrorPage="true"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:errors />

<div class="blank">
	<center>
		<br>
		<br>
		<br>
		<H1>
			<html:img src="../theme/images/Critic.gif" />
			<bean:message key="errors.title.IO" />
		</H1>
		<br>
		<h4>
			<bean:message key="errors.help.IO" />
		</h4>
		<!--  -->
		<logic:messagesPresent message="true"> 
		<br>
		Risultati: 
			<html:messages id="message" message="true"> 
				<span>
					${message}
				</span>
				<br> 
			</html:messages> 
		</logic:messagesPresent> 
		<!--  -->
		<br>
		<div align="center">
			<html:link action="/logon/logon.do" styleClass="button">Esci</html:link> 
		</div>
	</center>
</div>


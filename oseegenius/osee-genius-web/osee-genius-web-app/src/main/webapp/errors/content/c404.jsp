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
			<bean:message key="errors.title.E404" />
		</H1>	
		<br>
		<h4>
			<bean:message key="errors.help.E404" />
		</h4>
		<input type="button" class="button" value="Indietro" onclick="history.back(); return false;">
	</center>
	
	<BR/>
</div>


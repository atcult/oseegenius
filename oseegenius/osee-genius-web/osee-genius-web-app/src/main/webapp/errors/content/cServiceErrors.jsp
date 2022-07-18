<%@ page language="java" contentType="text/html; charset=ISO-8859-1" isErrorPage="true"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>


<div class="blank">

	<center>
		
		<br>
		<img src='<html:rewrite page="/images/Critic.gif" />'  >
		<H3>
			<!-- bean:message key="errors.title.E404"  /-->
			 Servizio non disponibile
		</H3>	
		<br>
		<br>
		<html:errors />
		<br>
		<input type="button" class="button" value="Chiudi" onclick="window.close(); return false;">
	</center>
	
	<BR/>
</div>


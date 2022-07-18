<%@ page language="java" contentType="text/html; charset=ISO-8859-1" isErrorPage="true"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:errors />

<div class="blank">

	<center>
		<br>
		<br>
		<H1>
			<html:img src="../theme/images/Critic.gif" /> 
		</H1>	
		Non è stato possibile recuperare la pagina di destinazione.<br>
		Ciò potrebbe essere stato causato dalla doppia pressione accidentale di un tasto [chiudi] o [indietro] senza attendere la risposta dal server o di aver utilizzato la funzione [indietro] del browser.<br><br>
		E' possibile riprendere comunque la catalogazione 
		<br>
		<br>
		<input type="button" class="button" value="Indietro" onclick="history.go(-2); return false;">
	</center>
	
	<BR/>
</div>


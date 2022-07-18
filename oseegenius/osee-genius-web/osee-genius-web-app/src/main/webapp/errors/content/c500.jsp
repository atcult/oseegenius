<%@ page language="java" contentType="text/html; charset=ISO-8859-1" isErrorPage="true"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>

<script type="text/javascript">
	//<![CDATA[
		stackHide = true;
		rootHide = true;
		function showDett(){
			if(stackHide) {
				stack.style.display='inline';
				stackHide=false;
			}
			else {
				stack.style.display='none';
				stackHide=true;
			}
		}
		
		function showRootDett(){
			if(rootHide) {
				root.style.display='inline';
				rootHide=false;
			}
			else {
				root.style.display='none';
				rootHide=true;
			}
		}
	//]]>
</script>

<html:errors />

<div class="blank">
	<BR>
	<BR>
	<BR>
	<TABLE border="0" width="65%" align="center" id="tab">
		<tr>
			<td class="hresult">Messaggio:</td>
			<td class="result"><%=exception.getMessage()%></td>
		</tr>
		<tr>
			<td class="hresult">Tipo di errore:</td>
			<td class="result"><%=it.ps.utils.PSUtil.findName(exception)%></td>
		</tr>
	</TABLE>
	<BR>
	<CENTER>
		<H1>
			<html:img src="../theme/images/Critic.gif" /> 
			<bean:message key="errors.title.E500" />
		</H1>
		<br>
		<h4>
			<bean:message key="errors.help.E500" />
		</h4>
		<input type="button" class="button" value="Indietro" onclick="history.back(); return false;">
		<BR>
		<BR>
		<A class="help" onclick="maindiv.style.display='inline'">dettaglio errore</A>
	</CENTER>
		<BR>
		<div id="maindiv" style="display: none;">
			<TABLE border="1" width="100%" align="center" id="tab">
				<tr>
					<td><a onclick="showDett();" style="cursor: pointer;">Dettaglio</a>:</td>
					<td>
					<%
						java.io.StringWriter stackTrace = null;
						try {
					 		stackTrace = new java.io.StringWriter();
					 		exception.printStackTrace(new java.io.PrintWriter(stackTrace));
					 		if (exception instanceof ServletException) 
					 		{
								%>
								<%=((ServletException) exception).getRootCause()%>
							<%
							}
							%>
							... 
							<%
						} 
						catch (Throwable th) {
							%>Dettaglio non disponibile 
							<%
								th.printStackTrace();
								}
							%> 	
						<pre>
							<%=stackTrace.toString()%>
						</pre>
					</td>
				</tr>
				<tr>
					<td>Form:</td>
					<td><pre><%=request.getAttribute("org.apache.struts.taglib.html.BEAN")%></pre></td>
				</tr>
			</table>
		<br/>
			<table border="1" width="100%">
				<tr>
					<th>Attributo</th>
					<th>Valore</th>
				</tr>
					<%
						java.util.Enumeration rEnum = request.getAttributeNames();
						while (rEnum.hasMoreElements()) {
							String name = (String) rEnum.nextElement();
					%>
				<tr>
					<td><%=name%></td>
					<td class="note"><%=request.getAttribute(name)%></td>
				</tr>
					<%
					}
					%>
			</table>
		<h3>Sessione:</h3>
		<TABLE>
			<TBODY>
				<%
					java.util.Enumeration re = session.getAttributeNames();
					while (re.hasMoreElements()) {
						String rname = (String) re.nextElement();
				%>
				<TR>
					<TD><%=rname%></TD>
					<TD><%=session.getAttribute(rname)%></TD>
				</TR>
				<%
				}
				%>
			</TBODY>
		</TABLE>

</div>
<BR>
</div>

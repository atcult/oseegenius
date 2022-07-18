<%@ page language="java" contentType="text/html; charset=ISO-8859-1" isErrorPage="true"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:errors />

<CENTER>
	<br>
	<br>
	<br>	
	<H1>
		<bean:define id="imgSource" >
			<html:rewrite page="/images/error.gif"/>
		</bean:define>
		<img src="${imgSource}" /> 
		<bean:message key="errors.title" />
	</H1>
	<br>
		<h4>
			<bean:message key="errors.help.IO" />
		</h4>
	<br>
	<logic:messagesPresent message="true"> 
		Risultati: 
			<html:messages id="message" message="true"> 
				<span>
					${message}
				</span>
				<br> 
			</html:messages> 
	</logic:messagesPresent> 
	<BR/>
	
<logic:present name="librisuite.bean.cataloguing.common.EditBean" >
	<bean:define id="bean" name="librisuite.bean.cataloguing.common.EditBean" />
	<logic:equal name="bean" property="cataloguingMode" value="true">
		<!-- CATALOGUING MODE -->
		<div align="center">
			<html:link action="/cataloguing/bibliographic/showItem.do" styleClass="button">Torna al Record</html:link> 
		</div>
		<logic:equal name="bean" property="navigation" value="true">
			<!-- NAVIGATION TAG MODE -->
		</logic:equal>
		<logic:equal name="bean" property="navigation" value="false">
			<!-- EDIT TAG MODE -->
		</logic:equal>
	</logic:equal>
	<logic:equal name="bean" property="cataloguingMode" value="false">
		<div align="center">
			<html:link action="/searching/search.do?advancedSearchButton=advancedSearchButton" styleClass="button">Torna al Menu Principale</html:link> 
		</div>
		<logic:present name="librisuite.bean.searching.ResultSummaryBean" >
			<bean:define id="rsBean" name="librisuite.bean.searching.ResultSummaryBean" />
			<logic:equal name="rsBean" property="searchingRelationship" value="true">
				<!-- RELATIONSHIP MODE -->
			</logic:equal>
		</logic:present>
	</logic:equal>
	<logic:present name="librisuite.bean.searching.BrowseBean" >
		<bean:define id="bBean" name="librisuite.bean.searching.BrowseBean" />
		<logic:notEqual name="bBean" property="browseLinkMethod" value="editHdg" >
			<logic:notPresent name="HeadingBean" >
				<!-- BROWSE MODE -->
			</logic:notPresent>
		</logic:notEqual>
		<logic:present name="HeadingBean" >
			<!-- EDIT HEADING MODE -->
		</logic:present>
	</logic:present>
</logic:present>

	<BR>
	<BR>
</CENTER>


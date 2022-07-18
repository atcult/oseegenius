<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<html:xhtml/>

<%--Questa pagina contiene la navbar, che dovrà essere visualizzata in OGNI pagina del browsing --%>

<div id="form-elements">		
	<div class="banca_dati">
		<span id="select_db">
			<bean:message bundle="browse" key="select_database"/>
		</span>
		<select class="permanent_select" name="collection_data" id="permanent_select" onchange="javascript:changeCollection()">	
			<option value="0" <%if ("0".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.0"/></option>		 
			<option value="2" <%if ("2".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.2"/> </option>				
			<option value="19" <%if ("19".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.19"/> </option>
			<option value="3" <%if ("3".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.3"/></option>
		   	<option value="4" <%if ("4".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.4"/></option>
		   	<option value="5" <%if ("5".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.5"/></option>
		   	<option value="14" <%if ("14".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.14"/></option>
		   	<option value="12" <%if ("12".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.12"/></option>
		   	<option value="1" <%if ("1".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.1"/></option>
		   	<option value="13" <%if ("13".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.13"/></option>
		   	<option value="16" <%if ("16".equals(request.getParameter("collection_code"))) out.println("selected");%>><bean:message bundle="browse" key="database.16"/></option>
		</select>
	</div>
			<div id="pageSize">
					<bean:message bundle="browse" key="and.display"/>
					<html:text styleClass="termsToDisplay" property="termsToDisplay" value="${BrowseBean.termsToDisplay}" size="1"/>
			</div>
			
	     	<div id="contentLabel">  
				<!-- modifica barbara PRN 0051 -->
				
				<span style="color: #F00; font-size: 0.75em;" >${BrowseBean.skippedTerm}</span>
				
				
				<html:textarea property="searchTerm" 
					   value="${BrowseBean.lastBrowseTermSkip}"
					   rows="1"
					   cols="80"/>
				
			
				<bean:message bundle="browse" key="in"/>
						
				<logic:equal value="pickHdg" name="BrowseBean" property="browseLinkMethod" >
					<html:select name="BrowseForm" property="selectedIndex" value="${BrowseBean.selectedIndex}" onchange="javascript:change('${BrowseBean.selectedIndex}');">
						<html:optionsCollection name="BrowseBean" property="browseIndexList"/>
					</html:select>
				</logic:equal>
				<logic:notEqual value="pickHdg" name="BrowseBean" property="browseLinkMethod" >
					<html:select name="BrowseForm" property="selectedIndex" value="${BrowseBean.selectedIndex}">
						<html:optionsCollection name="BrowseBean" property="browseIndexList"/>
					</html:select>
				</logic:notEqual>
				
									
				<!--<html:submit styleClass="button" onclick="javascript:setMethod('refreshFromForm'); return false;">
					<bean:message bundle="browse" key="browse.for"/>
				</html:submit>-->
			   <input name="bBrowse" value=<bean:message  bundle="browse" key="browse.for"/> class="button" type="button" onclick="browse('refreshFromForm','${BrowseBean.cataloguingView}','${BrowseBean.collectionCode}');"/> 
			 </div>
</div>
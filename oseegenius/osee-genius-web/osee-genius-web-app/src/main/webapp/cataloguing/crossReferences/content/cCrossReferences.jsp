<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<html:xhtml/>

<script type="text/javascript">
	//<![CDATA[
	function setMethod(method) {
		var crossAction =  '<html:rewrite page="/browse.do" />';
		crossAction = crossAction+"?method="+method;
		document.forms[1].action = crossAction;
		document.forms[1].submit();
	}
	//]]>
</script>
<bean:define id="bean" name="librisuite.bean.crossreference.CrossReferenceBean"/>
<bean:define id="BrowseBean" name="librisuite.bean.searching.BrowseBean"/>
<div id="form-elements">	
</div>	
<div id="contentLabel">	
	  <logic:equal name="bean" property="attribute" value="false">
		<bean:message bundle="crossReference" key="cross.reference"
					  arg0="${bean.numberOfReferences}"/>
	 </logic:equal>
	 <logic:equal name="bean" property="attribute" value="true">
			<bean:message bundle="crossReference" key="attribute"/> (${bean.numberOfReferences}):
	 </logic:equal>
</div>
 </br>
  <html:form action="/crossReferences.do">
  <input type="hidden" name="method"/>
  
 	 <table  width="80%">
      <tr>
		 <td class="crossHeading">
	            <bean:write name="bean" property="sourceDescriptor.displayText"/>
		  </td>
	  </tr>
	  <logic:notEmpty name="bean" property="crossReferenceList">			
	   <tr>
	      <td>
			<table class="contentTable" width="60%">
				<tr>
					<th><bean:message bundle="crossReference" key="type"/></th>
					<th><bean:message bundle="crossReference" key="reference"/>	</th>
					<th><bean:message bundle="crossReference" key="index_column_doc" /></th>
			    </tr>
				<logic:iterate id="anXref" name="bean" property="crossReferenceList" indexId="i">
					<tr>
						<td>
							<bean:message bundle="crossReference" key="decode.type_${anXref.XRef.key.type}"/>							
						</td>
						<!-- <td><bean:write name="anXref" property="decodedType"/></td> -->
						<td><bean:write name="anXref" property="target"/></td>
						<td>
					
						<logic:greaterThan name="anXref" property="docTargetCounts" value="0" >
							<a href="search?q=&&h=any_bc&collection_data=${BrowseBean.collectionCode}&s=25&f=authority_group_${anXref.headingType}:${anXref.XRef.target}&label=${anXref.target}" class="browse">
								<bean:write name="anXref" property="docTargetCounts"/>
							</a>
						</logic:greaterThan>
		                <logic:lessEqual name="anXref" property="docTargetCounts" value="0">	
		                	<bean:write name="anXref" property="docTargetCounts"/>
		                </logic:lessEqual>	
						<!-- 
							<a href="/olisuite/searching/browse.do?entryNumber=indexId&method=searchByCrossReference" class="browse">
								
							</a>
							-->
						</td>
					</tr>
				</logic:iterate> 
			</table>
	   </td>
	  </tr>
	  </logic:notEmpty>
	</table>
    </br>
   		 <input type="button" value='<bean:message bundle="crossReference" key="go.to.index"/>' class="button nextPreviousButton"  onclick="javascript:setMethod('refreshFromXref');">
	<html:errors/>
	
	
	
	
	</html:form>	
	

	
  

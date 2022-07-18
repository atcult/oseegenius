<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<html:xhtml/>

<bean:define id="AuthorityBean" name="librisuite.bean.authority.AuthorityBean"/>
<html:errors/>
<div style="background-color:rgb(16, 78, 139); color:white; padding: 6px;">
	<b>Authority</b>
</div>

 <div class="account-form-sub-title"> <bean:write name="AuthorityBean" property="descriptor.displayText"/></div>
 
 <logic:present name="AuthorityBean" property="itemNotes">
 <logic:iterate  name="AuthorityBean" property="itemNotes" id="mapEntry" >
   <table class="infoDataTable" width="100%" cellpadding="0" cellspacing="0" style="font-size:0.8em;">
	<tr class="row0">
		<bean:define id="noteType" name='mapEntry' property='key'/>
     	<td><bean:message bundle="authorities" key="${noteType}"/> </td>
	</tr>
	
	<logic:iterate name="mapEntry" property="value" id="noteAuthority" indexId="i">
	
 	 <tr class="row${i%2 == 0 ? 'alt' : ''}">  
 	     <td>       
             <bean:write name="noteAuthority" property="note"/>
    	 </td>
     </tr>
   </logic:iterate>
    </table>
 </logic:iterate>
</logic:present>

 <logic:notPresent  name="AuthorityBean" property="itemNotes">
 <table class="infoDataTable" width="100%" cellpadding="0" cellspacing="0" style="font-size:0.8em;">
	<tr class="row0">
	  <td>   
     	  <bean:message bundle="authorities" key="no_authority_record"/>
      </td>
     </tr>
 </table>
 </logic:notPresent>
 

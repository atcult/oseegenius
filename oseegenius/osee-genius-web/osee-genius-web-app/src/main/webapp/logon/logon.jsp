<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>

<tiles:insert page="/common/layout.jsp" flush="true">
  <tiles:put name="header" value="/logon/header/hLogon.jsp" />
  <tiles:put name="footer" value="/common/footer.jsp"/>
  <tiles:put name="leftmenu" value="/common/leftmenu.jsp" />
  <tiles:put name="body" value="/logon/content/cLogon.jsp" />
 	<tiles:put name="rightmenu" value="/common/blank.jsp" /> 
</tiles:insert>

<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>

<tiles:insert page="/mades/errors/layout.jsp" flush="true">
  <tiles:put name="header" value="/mades/common/header.jsp" />
  <tiles:put name="footer" value="/mades/common/footer.jsp"/>
  <tiles:put name="leftmenu" value="/mades/common/leftmenu.jsp" />
  <tiles:put name="body" value="/mades/errors/content/c503.jsp" />
  <tiles:put name="rightmenu" value="/mades/common/rightmenus/mRError.jsp" />
</tiles:insert>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>

<tiles:insert page="/common/layout_browse.jsp" flush="true">
  <tiles:put name="header" value="/common/header.jsp" />
  <tiles:put name="footer" value="/common/footer.jsp"/>
  <tiles:put name="leftmenu" value="/common/leftmenu.jsp" />
  <tiles:put name="body" value="/searching/content/cBrowse.jsp" />
  <tiles:put name="rightmenu" value="/common/rightmenus/blank.jsp" />
</tiles:insert>
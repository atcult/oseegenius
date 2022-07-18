<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<tiles:insert page="/errors/smallLayout.jsp" flush="true">
  <tiles:put name="body" value="/errors/content/cServiceErrors.jsp" />
 </tiles:insert>

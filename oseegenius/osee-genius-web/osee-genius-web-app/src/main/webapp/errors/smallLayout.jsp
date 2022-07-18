<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<html:xhtml/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

  <head>
    <title>ERROR</title>
    <html:base />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta>
    <meta http-equiv="Content-Script-Type" content="text/javascript; charset=UTF-8"></meta>
    <link rel="stylesheet" href="<html:rewrite page="/common/css/styles.jsp"/>" type="text/css"></link>
  </head>

  <body >
		<div id="menu">
			<div id="headerContent">
				<div id="headerContentOrangeBorder">
					<div id="headerAmicusLogo"></div>
					<div id="headerMadesLogo"></div>
				</div>
			</div>
		</div>
		<div id="content" style="width:80%; ">
			<tiles:insert attribute="body" />
		</div>
	</body>

</html>

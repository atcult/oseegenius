<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<html:xhtml/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

  <head>
    <title>Dispatcher Error</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta>
    <meta http-equiv="Content-Script-Type" content="text/javascript; charset=UTF-8"></meta>
	<bean:define id="cssUrl" name="librisuite.bean.css.CssBean" property="cssUrl"/>
    <link rel="stylesheet" href="<html:rewrite page="${cssUrl}"/>" type="text/css"></link>
		<script type="text/javascript" src="<html:rewrite page="/common/css/styles.js"/>"></script>
  </head>

  <body onload="javascript: setWidthHeight();" >
		<div id="menu">
			<div id="headerContent">
				<div id="headerContentOrangeBorder">
					<div id="headerAmicusLogo"></div>
					<div id="headerMadesLogo"></div>
					<div id="headerMenuImage"></div>
					<tiles:insert attribute="header" />
				</div>
			</div>
		</div>
		<div id="subMenu">
			<tiles:insert attribute="leftmenu" />
		</div>
		<div id="content" >
			<tiles:insert attribute="body" />
		</div>
		<div id="options"  >
			<tiles:insert attribute="rightmenu" />
		</div>		
		<div id="copyright">
			<tiles:insert attribute="footer" />
		</div>
	</body>

</html>

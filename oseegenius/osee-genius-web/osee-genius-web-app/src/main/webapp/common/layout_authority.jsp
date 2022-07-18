<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<html:xhtml/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

  <head>
    <title>Browse</title>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta>
    <meta http-equiv="Content-Script-Type" content="text/javascript; charset=UTF-8"></meta>
    <script type="text/javascript" src="<html:rewrite page="/js/jquery.min.js"/>"></script>
    <link href="<html:rewrite page="/style/prettyPhoto.css"/>" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="<html:rewrite page="/js/jquery.prettyPhoto.js"/>"></script>
	<bean:define id="cssUrl" name="librisuite.bean.css.CssBean" property="cssUrl"/>
    <link rel="stylesheet" href="<html:rewrite page="${cssUrl}"/>" type="text/css"></link>
		<script type="text/javascript" src="<html:rewrite page="/common/css/styles.js"/>"></script>
		
  </head>

  <body onload="javascript: loadPage();" onresize="javascript: setWidthHeight();">
					
		<div id="content-authority">
			<tiles:insert attribute="body" />
		</div>
		
		
	</body>
	
</html>

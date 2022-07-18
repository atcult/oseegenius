<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<html:xhtml/>
<!-- <bean:define id="bean" name="librisuite.bean.searching.ResultSummaryBean"/> -->
<bean:define id="BrowseBean" name="librisuite.bean.searching.BrowseBean"/>
<!-- <bean:define id="editBean" name="librisuite.bean.cataloguing.common.EditBean"/> -->

<tiles:insert page="/common/rightmenuLayout.jsp" flush="true">
	<tiles:put name="optionItems" type="String">
 <!--modifica barbara 02/05/2007-->
  <logic:equal name="bean" property="searchingRelationship" value="false">
  		<!-- <logic:equal name="editBean" property="cataloguingMode" value="false">-->
			<!-- %@ include file="/common/options/bibAutSearchingToggle.jsp" % -->
			<logic:notEqual value="pickHdg" name="BrowseBean" property="browseLinkMethod" >
				< %@ include file="/common/options/newBib.jsp" %>
				< %@ include file="/common/options/activeQueries.jsp" %>
			</logic:notEqual>
		<!-- </logic:equal> -->
			< %@ include file="/common/options/newHeading.jsp" %>

<!-- 20090723 inizio  -->
		<!--<logic:equal name="editBean" property="casaliniBean.enabled" value="true"> -->
			<logic:equal name="BrowseBean" property="selectedIndex" value="PU       "> 
				<div class="rightMenuGroup">
					<div class="rightMenuGroupTitle">
					 	<bean:message bundle="optionsMenu" key="menu.editor"/>
					</div>
				 	<div class="rightMenuItem">
						<html:link page="/casalini/searching/editor.do#init">
						  <img src='<html:rewrite page="/images/rating_star.png" />' border="0" style="margin-right: 0px; vertical-align: middle;"></img>
								<bean:message bundle="optionsMenu" key="editor.search"/>
	             		</html:link>
			     	</div>
			    </div>
			</logic:equal> 
		<!-- </logic:equal> -->
<!-- 20090723 fine  -->

<!-- inizio  -->
			< %@ include file="/common/options/diacritici.jsp" %>
<!-- fine  -->
			< %@ include file="/common/options/search.jsp" %>
	</logic:equal>
		< %@ include file="/common/options/exit.jsp" %>		
	</tiles:put>
</tiles:insert>
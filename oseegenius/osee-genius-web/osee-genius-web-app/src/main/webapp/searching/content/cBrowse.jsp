<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<html:xhtml/>
<script type="text/javascript" charset="utf-8">
	$(document).ready(function(){
		setTimeout('doRedirect();', 900000);
		$("[rel^='prettyPhoto']").prettyPhoto({social_tools: false, deeplinking:false});
	});
	
</script>
<bean:define id="BrowseBean" name="librisuite.bean.searching.BrowseBean"/>
<html:errors/>


<script type="text/JavaScript">
    $(document).ready(function() {
      var collectionCode = ${BrowseBean.collectionCode};   

      //bibliografici  
      $('.docCell').each(function(){
          //docCell è formattato nel seguente modo:  headingNumber-Index	
          var headingIdAndIndex = $(this).attr('id').split("-");
          var headingId =  headingIdAndIndex[0]
		  var index = headingIdAndIndex[1]; 
		  var label =  $(this).attr('data-label');
		  var url = "browseSearch?searchType=bib&headingId=" + headingId + "&indexId=" + index;
		  var current = $(this);
		  
          $.ajax({
              	url: url,
              	dataType: "json",           	
              	success: function(data){
                  	if(data.count > 0) {
                  		current.prepend("<a href=\"search?q=&&h=any_bc&collection_data=" + collectionCode + "&s=25&f=authority_group_" + index + ":" + headingId + "&label=" + label + "\">" + data.count + "</a>");
              		}
          			else {
          				current.text(data.count);	
              		}               		
              	}           
          });          
   	  });	
   });
  </script> 


<div id="browse">
	<html:form action="/browse.do">
		<input type="hidden" name="method"/>
		
<div id="form-elements">				
	<div id="contentLabel">  
				<!-- modifica barbara PRN 0051 -->
				
				<span style="color: #F00; font-size: 0.75em;" >${BrowseBean.skippedTerm}</span>
				
				
				<html:textarea property="searchTerm" 
					   value="${BrowseBean.lastBrowseTermSkip}"
					   rows="1"
					   cols="80"
					   onkeydown = "if (event.keyCode == 13)document.getElementById('bBrowse').click()"/>
				
							
				<bean:message bundle="browse" key="in"/>
				<!-- Indici di Trento. Da duplicare la pagina e settare quelli specifici del cliente in caso di altri clienti-->		
				<logic:equal value="pickHdg" name="BrowseBean" property="browseLinkMethod" >
					<html:select name="BrowseForm" property="selectedIndex" value="${BrowseBean.selectedIndex}" onchange="javascript:change('${BrowseBean.selectedIndex}');">
						<html:option value="NA" ><bean:message bundle="browse" key="index.name"/></html:option>
                  		<html:option value="SU"><bean:message bundle="browse" key="index.subject"/></html:option>
                   		<html:option  value="TI"><bean:message bundle="browse" key="index.title"/></html:option> 
					</html:select>
				</logic:equal>
				<logic:notEqual value="pickHdg" name="BrowseBean" property="browseLinkMethod" >
					<html:select name="BrowseForm" property="selectedIndex" value="${BrowseBean.selectedIndex}">
						<html:option value="NA" ><bean:message bundle="browse" key="index.name"/></html:option>
                  		<html:option value="SU"><bean:message bundle="browse" key="index.subject"/></html:option>
                   		<html:option  value="TI"><bean:message bundle="browse" key="index.title"/></html:option> 
					</html:select>
				</logic:notEqual>
				
				<bean:message bundle="browse" key="and.display"/>					
				<html:select name="BrowseBean" property="termsToDisplay">
					<html:option value="5">5</html:option >
					<html:option value="10">10</html:option>
					<html:option value="25">25</html:option>
					<html:option value="50">50</html:option>
					<html:option value="100">100</html:option>
				</html:select>	
	
				<!--<html:submit styleClass="button" onclick="javascript:setMethod('refreshFromForm'); return false;">
					<bean:message bundle="browse" key="browse.for"/>
				</html:submit>-->
			   <input id="bBrowse" name="bBrowse" value=<bean:message  bundle="browse" key="browse.for"/> class="button nav-button" type="button" onclick="browse('refreshFromForm','${BrowseBean.cataloguingView}','${BrowseBean.collectionCode}');"/>
			   <input name="reset" value=<bean:message  bundle="browse" key="cancel"/> class="button nav-button" type="button" onclick="resetAll()"/> 
			 </div>
</div>
     <logic:equal name="BrowseBean" property="showResults" value="true">
			
			
			<table class="contentTable headerTable" width="100%" >
				<tr class="contentTableHeading">
      				
					<th class="heading"></th>
					
					<logic:equal value="true" name="BrowseBean" property="dewey" >
						<th class="edition"><bean:message bundle="browse" key="edition"/></th>
					</logic:equal>
	                <logic:equal value="true" name="BrowseBean" property="supportsNameTitle" >
		                <logic:equal value="true" name="BrowseBean" property="nameTitle" >
		               	   <th class="documentCount">NT</th>
						</logic:equal>   
                 	</logic:equal> 
                 	
              		<logic:equal value="true" name="BrowseBean" property="supportsCrossReferences">
							<th class="crossReferenceCount"><bean:message bundle="browse" key="refs"/></th>
					</logic:equal>
                
                             
					<logic:equal value="true" name="BrowseBean" property="supportsAuthorities">
						<th class="crossReferenceCount"><bean:message bundle="browse" key="authority.count.header" /></th>
					 </logic:equal>
					
                  	<!-- 
                  	<logic:equal value="true" name="BrowseBean" property="supportsMades">
                  	 	<th class="crossReferenceCount"><bean:message bundle="browse" key="mades.count.header"/></th>
                  	</logic:equal>
					-->
					<th class="documentCount"><bean:message bundle="browse" key="docs"/></th> 				   				
				</tr>	
				</table>
				<table class="contentTable" width="100%">
				<!-- riga fittizia per evitare la mancata visualizzazione della prima riga per via del fixed
				header -->
				<tr class="" style="">				    	      
					<td class="heading"> <div style="min-height: 16px;"></div></td>
					<td class="crossReferenceCount"></td>
					<td class="authorityCount"></td>
				</tr>						
				<logic:iterate name="BrowseBean" property="decoratedBrowseList" id="aDescriptor" indexId="i">
					<tr class="contentTableRow${i%2}">
				    	      
						 <td class="heading">
                  			 <bean:write name="aDescriptor" property="displayText"/>
						 </td>			
						
 		                <logic:equal value="true" name="BrowseBean" property="supportsNameTitle" >
	 		                <logic:equal value="true" name="BrowseBean" property="nameTitle" >
								<td class="documentCount">
									<logic:greaterThan name="BrowseBean" property="ntCount[${i}]" value="0" >
			                        	<html:link action="/browse.do?method=browseNt&searchTerm=${aDescriptor.displayText}">
											<bean:write name="BrowseBean" property="ntCount[${i}]"/>
			                            </html:link>
			                         </logic:greaterThan>
			                          <logic:lessEqual name="BrowseBean" property="ntCount[${i}]" value="0">
			                          		<bean:write name="BrowseBean" property="ntCount[${i}]"/>
			                          </logic:lessEqual>
								</td>
							</logic:equal>
						</logic:equal>

	
						<logic:equal value="true" name="BrowseBean" property="dewey" >
					    	<td class="edition">
								<bean:write name="BrowseBean" property="editionNbr[${i}]"></bean:write>
							</td>
						</logic:equal>
							
						<logic:equal value="true" name="BrowseBean" property="supportsCrossReferences">
							<td class="crossReferenceCount">
								<logic:greaterThan name="BrowseBean" property="xrefCount[${i}]" value="0" >
									<html:link styleClass="browse" action="/browse.do?entryNumber=${i}&method=showXrefs">
										<bean:write name="BrowseBean" property="xrefCount[${i}]"/>
									</html:link>
								</logic:greaterThan>
								 <logic:lessEqual name="BrowseBean" property="xrefCount[${i}]" value="0">
								 	<bean:write name="BrowseBean" property="xrefCount[${i}]"/>
								 </logic:lessEqual>
							</td>
						</logic:equal>
						
						<logic:equal value="true" name="BrowseBean" property="supportsAuthorities">
							<td class="authorityCount">
							<logic:equal value="false" name="BrowseBean" property="authorityEnabled">
						    <logic:greaterThan name="BrowseBean" property="authCount[${i}]" value="0" >
								<a rel="prettyPhoto" href="browse.do?method=showAuths&entryNumber=${i}&iframe=true&height=500&width=460">
									<img src="img/icon-details.png"/>
                                </a>					
		   					</logic:greaterThan>
							</logic:equal>
							<logic:equal value="true" name="BrowseBean" property="authorityEnabled">
								<logic:greaterThan name="BrowseBean" property="authCount[${i}]" value="0" >
									<a class="browse" href="authSearch?q=&&h=any_bc&collection_data=0&s=25&f=authority_group_${aDescriptor.headingType}:${aDescriptor.key.headingNumber}&label=${aDescriptor.displayText}">
										<bean:write name="BrowseBean" property="authCount[${i}]"/>
			                		</a>									
		                        </logic:greaterThan>
		                        </logic:equal>
		                        <logic:lessEqual name="BrowseBean" property="authCount[${i}]" value="0">							
			             			<bean:write name="BrowseBean" property="authCount[${i}]"/>
			               	 	</logic:lessEqual>	
	                    	</td>
						</logic:equal>
						
	                			
			           <logic:equal value="true" name="BrowseBean" property="supportsMades">
	            			<td class="documentCount">
	               				<html:link styleClass="browse" action="/browse.do?entryNumber=${i}&method=search&operation=showMades">
		              				<bean:write name="BrowseBean" property="madesCount[${i}]"/>
	                			</html:link>	                			
	                  		</td>
	         			</logic:equal>
					
						<td class="documentCount docCell" id="${aDescriptor.key.headingNumber}-${aDescriptor.headingType}" data-label="${aDescriptor.displayText}"></td>					
	              		
				 	</tr>
			 	</logic:iterate>

			</table>
        	</div>
        	
        	<html:submit styleClass="nextPreviousButton" onclick="javascript:setMethod('previous', '${BrowseBean.lastTerm}'); return false;">
				<bean:message bundle="browse" key="previous"/>
			</html:submit>
        	
        	
			<html:submit styleClass="nextPreviousButton" onclick="javascript:setMethod('next', '${BrowseBean.nextTerm}'); return false;">
				<bean:message bundle="browse" key="next"/>
			</html:submit>
		
		
 </logic:equal>
		
       
	</html:form>	  
</div>

<script type="text/javascript">
//<![CDATA[               
	document.forms[1].searchTerm.focus();

	function setMethod(method, term) {
		var browseAction =  '<html:rewrite page="/browse.do" />';
		browseAction = browseAction + "?method="+method+"&term="+term;
		document.forms[1].method.value = method;
		document.forms[1].action = browseAction
		document.forms[1].submit();
		return false;
	}
	
	function browse(method, lv, collection)
	{
	    var index = document.forms[1].selectedIndex.value;
		var term = document.forms[1].searchTerm.value;
		var browseAction =  '<html:rewrite page="/browse.do" />';
		browseAction = browseAction + "?method="+method +"&index="+index +"&term="+term+"&lv="+lv+"&collection_code="+collection; 
		document.forms[1].action = browseAction
		document.forms[1].submit();
			
	}

	
	
	function change(indice){
		document.forms[1].selectedIndex.value= indice;
	}
	document.onkeydown=keyDown;
	function keyDown(){
	 //Tasto Nuova Intestazione F9
	 if(event.keyCode==120)
		 window.document.getElementById('newHeading').click(); 
	}

	function changeCollection() {		
		var browseAction =  '<html:rewrite page="/browse.do" />';
		browseAction = "?method=refreshCollection";
		document.forms[1].action = browseAction
		document.forms[1].submit()
     }
	
	function doRedirect() {		
		window.location.href = 'lifeSession.do';	
	}

    function goToOseeGenius(obj) {       
    	var sel = document.getElementById('collection_select');    	
		var collection_value = sel.value;		
		window.location.href = obj.getAttribute("data") + "&collection_code=" + collection_value;	
		
    }
    function openAuthority(index){
    	var url ='<html:rewrite page="/browse.do?entryNumber='+index+'&method=showAuths"/>';
     	window.open(url,'authorityCard','scrollbars=yes,resizable=yes,width=440,height=480,top=30,left=100');
    }
//]]>
</script>

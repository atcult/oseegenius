<html><body onload="document.ExportRWForm.submit();">
<%
    if ("sbn".equals(request.getAttribute("from")))
    {%>
        <form name="ExportRWForm" method="post" action="http://www.refworks.com/express/ExpressImport.asp?vendor=IFLA%20UNIMARC%20Format&amp;filter=UniMARC&amp;encoding=65001">
    <%}
    else
    {%>
    	<form name="ExportRWForm" method="post" action="http://www.refworks.com/express/ExpressImport.asp?vendor=Library%20of%20Congress%20MARC%20Format&amp;filter=Library%20of%20Congress%20MARC&amp;encoding=65001">
   <% }%>			
<textarea name="ImportData" style="display:none;"><%=request.getAttribute("data") %></textarea>
</form></body></html>
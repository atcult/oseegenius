<%@page contentType="text/javascript; charset=UTF-8" %>
<%@page import="com.atc.osee.web.model.Visit" %>
<%@page import="java.util.*" %>

<%
	Visit visit = (Visit)session.getAttribute("visit");
	Locale locale = visit.getPreferredLocale();
	ResourceBundle messages = ResourceBundle.getBundle("resources", locale);
%>
var facetNameMap = {};
<%
	if ("it".equals(locale.getLanguage()))
	{
%>
facetNameMap['Sources'] = 'Fonte';
facetNameMap['Subject'] = 'Soggetto';
facetNameMap['Author'] = 'Autore';
<%} else
	{
%>
facetNameMap['Sources'] = 'Source';
facetNameMap['Subject'] = 'Subject';
facetNameMap['Author'] = 'Author';
<%
	}
%>


var curPage = 1;
var recPerPage = <%=request.getParameter("num")%>;
if (recPerPage == null || recPerPage== undefined) recPerPage = 5;
var totalRec = 0;
var curDetRecId = '';
var curDetRecData = null;
var curSort = 'relevance';
var curFilter = null;
var facetNames = "xtargets,subject,author";

function poll()
{
	var startPage = parseInt(Request.parameter('start'));
	
	startPage= isNaN(startPage) ? 0 : startPage;
	curPage = (startPage / recPerPage) +1;
	
	$.ajax({
		 url: "pazpar2?command=termlist&name="+facetNames,
		 success: function(data)
		 {
			var facets= [];
			$(data).find("list").each(function()
			{		
				facets.push(facet(this));
			});
			$("#extra").html(facets.join(' '));
			$(".viewlet_toggable_head").click(function()
			{
				$(this).next(".middle_viewlet_body").slideToggle(600);
			});
		 }
	});
	
	$.ajax({
	    url: "pazpar2?command=show&start="+startPage+"&num="+$("#num").val()+"&sort="+$("#sort").val(),
	    success: function(data) 
	    {
	    	var total = parseInt($(data).find("total").text());
	    	$('#documents').xslt({xml: data, xslUrl: 'xslt/clustered_hit.jsp?offset='+ (startPage)});
	    	
			var start = parseInt($(data).find("start").text());
			var num = parseInt($(data).find("num").text());
			var merged = parseInt($(data).find("merged").text());
			
			totalRec = total;
			
			var pager = document.getElementById("pager");
		    pager.innerHTML = "";
		  
		  	if(total > 0){
		   		 pager.innerHTML += '<b>' + (startPage + 1) + ' - ' + (startPage + num) + ' <%=messages.getString("of") %> ' + total + ' (' + merged+' <%=messages.getString("merged") %> )</b>' ;
		   		 drawPager (pager);
		    }
		    
			$.ajax({
				url: "pazpar2?command=stat",
				success: function(statresponse)
				{
					var progress = $(statresponse).find("progress").text();
					if (progress != 1.00)
					{
						setTimeout(function() { poll(); }, 1500);
					} else
					{
						$(".mark").slideDown();
						document.getElementById("tabstate").src='img/close_small.png';
						
						var total = parseInt($(data).find("total").text());
						if(total == 0){
						 var doc = document.getElementById("documents");
		     			 doc.innerHTML+= '<div style="height: 100px; clear: both; margin-top:50px;"><font style="font-size:18px; color:#C0C0C0; margin-left:10px;">'+'<%=messages.getString("no_results")%>'+'</font></div></br>';
						}
						
						
						$.ajax({
							url: "selector?a=get",
							success:function(response)
							{
								var howManySelectedItems = response.result.length;
								for (i = 0; i < howManySelectedItems; i++)
								{
									$("#check_"+response.result[i]).attr("src", "img/checked_true.png"); 			
								} 
							}
						});
					}
				}
			});
	    },
	    contentType: 'text/plain'
	  });	
}

function hit_template(hit)
{
	var template="" +
			"<div class='hit' id='hit'>"+
				"	<table width='100%'>"+
				"		<tr>"+
				"			<td width='4%' valign='top'></td>"+
				"			<td width='96%' valign='top' style='border-bottom:1px dotted gray;'>"+
				"				<div id='document_header'>";
	var title = $(hit).find(">md-title");
	if (title != undefined && title.text() != '')
	{
		template += 
			"<a href='metaresource?id="+$(hit).find(">recid").text()+"' class='document_title'>"+
			title.text()+
			"</a>";
	} else 
	{
		template += 
			"<a href='metaresource?id="+$(hit).find(">recid").text()+"' class='document_title'>"+
			"Title not available"+
			"</a>";
	}

	var remainder = $(hit).find(">md-title-remainder");
	if (remainder != undefined && remainder.text() != '')
	{
		template+="&nbsp;<a class='title_remainder'>"+remainder.text()+"</a>";
	} 

	var date = $(hit).find(">md-date");
	if (date != null && date != undefined && date.text() != '')
	{
		template+="&nbsp;("+date.text()+")";
	}

	var author = $(hit).find(">md-title-responsibility");
	if (author == null || author.text() == '')
	{
		author = $(hit).find(">md-author");
	}

	if (author != undefined)
	{
		template+="<br/><a href='' class='document_author'>"+author.text().replace("[","").replace("]","")+"</a>";
	}
			
	var description = $(hit).find(">md-description");
	if (description != undefined)
	{
		template+="<br/>";
		description.each(function()
		{
			template+="<br/><span class='description'>"+$(this).text()+"</span>";
			return false;
		});
	}
	
	template+=
				"					<br/>"+		
				"				</div>" +
				"			</td>" +
				"		</tr>" +
				"	<table>" +
				"</div>";
	return template;			
}

function drawPager (pagerDiv)
{
    //client indexes pages from 1 but pz2 from 0
    var onsides = 6;
    var pages = Math.ceil(totalRec / recPerPage);
    var firstClkbl = ( curPage - onsides > 0 ) 
        ? curPage - onsides
        : 1;

    var lastClkbl = firstClkbl + 2*onsides < pages
        ? firstClkbl + 2*onsides
        : pages;

    var prev = '';
    if (curPage > 1)
    {
        prev = '<a href="javascript:showPage(1);" title="<%=messages.getString("goToFirstPage")%>"><img src="img/fastRewindEnabled.png" class="fast-navigation-button"/></a>';
        prev += '<a href="javascript:showPage('+ (curPage - 1) +')" title="<%=messages.getString("goToPreviousPage")%>"><img src="img/stepRewindEnabled.png" class="fast-navigation-button"/></a>';
        
	} else
	{
		 prev = '<img src="img/fastRewindDisabled.png" class="fast-navigation-button" alt="<%=messages.getString("goToFirstPage")%>"/>';
        prev += '<img src="img/stepRewindDisabled.png" class="fast-navigation-button" alt="<%=messages.getString("goToPreviousPage")%>"/>';
	}
    var middle = '';
    for(var i = firstClkbl; i <= lastClkbl; i++) {
        var numLabel = i;
        if(i == curPage)
            middle += '<a><span class="current-page-number">' + i + '</span></a>';
        else
        	middle += '<a href="javascript:showPage(' + i + ')"> ' + numLabel + ' </a>';
    }
    
    var next = '';
    if (pages - curPage > 0)
    {
        next =  '<a href="javascript:showPage('+(curPage + 1)+')" title="<%=messages.getString("goToNextPage")%>"><img src="img/stepForwardEnabled.png" class="fast-navigation-button"/></a>';
        next +=  '<a href="javascript:showPage('+pages+')" title="<%=messages.getString("goToLastPage")%>"><img src="img/fastForwardEnabled.png" class="fast-navigation-button"/></a>';
	} else
	{
        next =  '<img src="img/stepForwardDisabled.png" class="fast-navigation-button" alt="<%=messages.getString("goToNextPage")%>"/>';
        next +=  '<img src="img/fastForwardDisabled.png" class="fast-navigation-button" alt="<%=messages.getString("goToLastPage")%>"/>';	
	}
	
	var predots = '';
    if (firstClkbl > 1)
        predots = '...';

    var postdots = '';
    if (lastClkbl < pages)
        postdots = '...';


    pagerDiv.innerHTML += prev + predots + middle + postdots + next;
}

function showPage (pageNum)
{
   manipulateQueryString('start', (pageNum -1) * recPerPage);
}

function goTo (start)
{
   manipulateQueryString('start', start);
}

function facet(facetData)
{
	if ($(facetData).find("term").size() == 0)
	{
		return "";
	}
	
	// Capitalize the literal value
	var facetName = $(facetData).attr("name");
	var indexName = facetName.substring(0,2);
	var facetName = facetName.replace( /(^|\s)([a-z])/g , function(m,p1,p2){ return p1+p2.toUpperCase(); } );

	if (facetName.indexOf("X") == 0) { facetName = "Sources"; }
	
	var facet = "<div class='viewlet'>"+
	"<div>"+
		"<div class='viewlet_toggable_head'><h3>"+facetNameMap[facetName]+"</h3></div>"+
		"<div class='middle_viewlet_body'>"+
	      "<ul>";		

	$(facetData).find("term").each(function()
	{		
		var name = $(this).find(">name").text();
		
		var count = parseInt($(this).find(">frequency").text());
		var location= URLDecode(document.location.href);
		
		var value = name;
		if (facetName=="Sources")
		{
			value= $(this).find(">id").text();
		}
		
		if ((location.indexOf(indexName + ":\""+value+"\"") != -1) || (location.indexOf(indexName + ":"+value) != -1))
		{
			facet+=	"<li>"+
			"<span>"+
			"<a>"+name+"</a>"+
			"<span class='count'> ("+count+")</span>"+
			"<a href='javascript:removeFilter(\""+indexName+"\",\""+value+"\")'><img src='img/cancel.png' style='vertical-align:middle;'/></a>"+
			"</span>"+	
			"</li>";							
		} else 
		{
			facet+=	"<li>"+
			"<span>"+
			"<a href='javascript:filter(\""+indexName+"\",\""+value+"\");'>"+name+"</a>"+
			"<span class='count'> ("+count+")</span>"+
			"</span>"+	
			"</li>";			
		}
	});
	
	facet +="</ul>"+
	"</div>"+
	"</div>"+
	"</div>";		
	return facet;
}

$(document).ready(function(){
 	poll();
 });	
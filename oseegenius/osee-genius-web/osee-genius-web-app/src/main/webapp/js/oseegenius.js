var Request;
$(document).ready(function(){

	Request = 
	{	
		parameter: function(name) 
		{
 			return this.parameters()[name];
		},
		parameters: function() 
		{
			var result = {};
 			var url = window.location.href;
 			var parameters = url.slice(url.indexOf('?') + 1).split('&');
 		
 			for(var i = 0;  i < parameters.length; i++) 
			{
 				var parameter = parameters[i].split('=');
 				result[parameter[0]] = parameter[1];
 			}
 			return result;
 		}			
	};
});

$(document).ready(function(){
	$("#o").val(Request.parameter('o'));
	$("#num").val(Request.parameter('num'));
	$("#sort").val(URLDecode(Request.parameter('sort')));

	var query = Request.parameter('q');
	var isAssoQuery = "associations"==Request.parameter('h');
	if (!isAssoQuery && query != null && (query.indexOf(":") == -1 && query.indexOf("%3A") == -1) ) {$("#q").val(URLDecode(query));}
	
	$(function(){	
		$('select#h').selectmenu({style:'dropdown', width:120,menuWidth:180,maxHeight: 300}); 
		$('select#s').selectmenu({style:'dropdown', width:40,maxHeight: 800}); 
		$('select#o').selectmenu({style:'dropdown', width:140,maxHeight: 800, menuWidth:150}); 
		$('select#b').selectmenu({style:'dropdown', width:140,maxHeight: 800, menuWidth:150}); 
		$('select#i').selectmenu({style:'dropdown', width:160,menuWidth:180,maxHeight: 800}); 
		$('select#sort').selectmenu({style:'dropdown', width:120,maxHeight: 800, menuWidth:130}); 
		$('select#num').selectmenu({style:'dropdown', width:40,maxHeight: 800}); 
		$( "button, input:submit,").button();
		$( "button, input:button,").button();
	});		
	
	$(".viewlet_toggable_head").click(function()
		{
			$(this).next(".middle_viewlet_body").slideToggle(600);
			$(this).next(".viewlet_body").slideToggle(600);
		});
});

function reSubmitSearch()
{
	if ($("#q").val() == '') return;
	
	var v = Request.parameter('v');
	if (v != undefined && v != '')
	{
		document.searchForm.v.value=v;
	}
	document.searchForm.submit();
}			

function reSubmitBrowse(){if ($("#q").val() == '') return;document.searchForm.submit();}			
function l10n(code){manipulateQueryString('l',code);}

function mlt(parameterName, parameterValue)
{
	// search for &p=
	var parameter="&"+parameterName+"=";
	var currentLocation = document.location.href;
	var startIndex = currentLocation.indexOf(parameter);
	if (startIndex == -1) 
	{
		// search for ?p=
		parameter = "?"+parameterName+"=";
		startIndex = currentLocation.indexOf(parameter);	
	}
	
	if (startIndex != -1)
	{
		
		var endIndex = currentLocation.indexOf("&",startIndex+1);
		if (endIndex != -1)
		{
			currentLocation = currentLocation.substring(0, startIndex+1) + currentLocation.substring(endIndex);
		} else 
		{
			currentLocation = currentLocation.substring(0, startIndex);					
		}
	}
	
	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")+parameterName+"=";
	newLocation = (currentLocation+parameter+parameterValue);
	if (newLocation.indexOf("#mlt") == -1)
	{
		newLocation += "#mlt";
	}
	document.location.href=newLocation;
}

function changePerspective(parameterName, parameterValue)
{	
	var parameter="&"+parameterName+"=";
	var currentLocation = document.location.href;
	var startIndex = currentLocation.indexOf(parameter);
	if (startIndex == -1) 
	{
		// search for ?p=
		parameter = "?"+parameterName+"=";
		startIndex = currentLocation.indexOf(parameter);	
	}
	
	if (startIndex != -1)
	{
		
		var endIndex = currentLocation.indexOf("&",startIndex+1);
		if (endIndex != -1)
		{
			currentLocation = currentLocation.substring(0, startIndex+1) + currentLocation.substring(endIndex);
		} else 
		{
			currentLocation = currentLocation.substring(0, startIndex);					
		}
	}
	
	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")+parameterName+"=";
	var location=currentLocation+parameter+parameterValue;
	if (location.indexOf("dls=true") == -1)
	{
		location+="&dls=true";
	}
	document.location.href=location;
}

function requery(parameterName, parameterValue)
{
	$.ajaxSetup({ cache: false });
	
	var a = $('#q').autocomplete({ 
		serviceUrl:'suggest',
		minChars:2, 
		maxHeight:500,
		width:300,
		zIndex: 9999,
		deferRequestBy: 0, 
		noCache: false, 
		onSelect: function(value, data){ reSubmitSearch(); }
	});
	
	if (document.location.href.indexOf("q=") == -1) 
	{
		return;
	}
	
	manipulateQueryString(parameterName, parameterValue);
}

function manipulateQueryString(parameterName, parameterValue)
{	
	// search for &p=
	var parameter="&"+parameterName+"=";
	var currentLocation = document.location.href;
	var startIndex = currentLocation.indexOf(parameter);
	if (startIndex == -1) 
	{
		// search for ?p=
		parameter = "?"+parameterName+"=";
		startIndex = currentLocation.indexOf(parameter);	
	}
	
	if (startIndex != -1)
	{
		
		var endIndex = currentLocation.indexOf("&",startIndex+1);
		if (endIndex != -1)
		{
			currentLocation = currentLocation.substring(0, startIndex+1) + currentLocation.substring(endIndex);
		} else 
		{
			currentLocation = currentLocation.substring(0, startIndex);					
		}
	}
	
	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")+parameterName+"=";
	document.location.href=(currentLocation+parameter+parameterValue);
}

function gotoPage()
{
	manipulateQueryString('p',$("#requestedPage").val());
}

function back(from)
{
	var currentLocation = document.location.href;
	currentLocation = replaceParameter('from',from, currentLocation);
	currentLocation = replaceParameter('direction','back', currentLocation);	
	document.location.href = currentLocation;
}

function forward(from)
{
	var currentLocation = document.location.href;
	currentLocation = replaceParameter('from',from, currentLocation);
	currentLocation = replaceParameter('direction','forward', currentLocation);	
	document.location.href = currentLocation;
}

function replaceParameter(parameterName, parameterValue, currentLocation)
{
	var parameter="&"+parameterName+"=";
	var startIndex = currentLocation.indexOf(parameter);
	if (startIndex == -1) 
	{
		parameter = "?"+parameterName+"=";
		startIndex = currentLocation.indexOf(parameter);	
	}
	
	if (startIndex != -1)
	{
		var endIndex = currentLocation.indexOf("&",startIndex+1);
		if (endIndex != -1)
		{
			currentLocation = currentLocation.substring(0, startIndex)+currentLocation.substring(endIndex);
		} else 
		{
			currentLocation = currentLocation.substring(0, startIndex);					
		}
	}
	
	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")+parameterName+"=";
	return currentLocation+parameter+parameterValue;
}

function filter(fieldName, fieldValue)
{
	fieldValue = fieldValue.replace("&","%26");
	parameterName = "f";
	parameterValue = (fieldName != 'xt' ? (fieldName+":\""+fieldValue.replace(/\"/g,"\\\"")+"\"") : (fieldName+":"+fieldValue));
	var parameter="&"+parameterName+"="+parameterValue;
	var currentLocation = document.location.href;
	var startIndex = currentLocation.indexOf(parameter);
	
	if (startIndex == -1) 
	{
		parameter="?"+parameterName+"="+parameterValue;
		startIndex = currentLocation.indexOf(parameter);	
	}
	
	if (startIndex != -1)
	{
		var endIndex = currentLocation.indexOf("&",startIndex+1);
		if (endIndex != -1)
		{
			location = currentLocation.substring(0, startIndex)+currentLocation.substring(endIndex);
		} else 
		{
			currentLocation = currentLocation.substring(0, startIndex);					
		}
	}
	
	var startIndexOfPageNumber = currentLocation.indexOf("&p=");
	if (startIndexOfPageNumber == -1)
	{
		startIndexOfPageNumber = currentLocation.indexOf("?p=");
	}
	
	if (startIndexOfPageNumber != -1)
	{
		currentLocation = currentLocation.substring(0, startIndexOfPageNumber)+currentLocation.substring(startIndexOfPageNumber+4);
	}
	
	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")+parameterName+"=";
	document.location.href=(currentLocation+parameter+parameterValue);
}

function removeFilter(fieldName, fieldValue)
{
	var currentLocation = URLDecode(document.location.href);

	parameterName = "f";
	/*parameterValue = (fieldName != 'xt') ? (fieldName+":\""+fieldValue+"\"") : (fieldName+":"+fieldValue);*/
	parameterValue = (fieldName != 'xt' ? (fieldName+":\""+fieldValue.replace(/\"/g,"\\\"")+"\"") : (fieldName+":"+fieldValue));

	var parameter=parameterValue + " OR ";
	currentLocation = currentLocation.replace(parameter,"");

	parameter=" OR " + parameterValue;
	currentLocation = currentLocation.replace(parameter,"");	
	
	var parameter="&"+parameterName+"="+parameterValue;
	
	currentLocation = currentLocation.replace(parameter,"");

	var startIndexOfPageNumber = currentLocation.indexOf("&p=");
	if (startIndexOfPageNumber == -1)
	{
		startIndexOfPageNumber = currentLocation.indexOf("?p=");
	}
	
	if (startIndexOfPageNumber != -1)
	{
		currentLocation = currentLocation.substring(0, startIndexOfPageNumber)+currentLocation.substring(startIndexOfPageNumber+4);
	}
	
	parameter="?"+parameterName+"="+parameterValue;
	currentLocation = currentLocation.replace(parameter,"");

	$.ajax({
		type: "POST",
		dataType: "json",
		url: "pinunpin",
		data: "a=r&filter="+fieldName+":\""+fieldValue+"\"",
		success: function()
		{
			document.location.href=currentLocation;
		}
	});			
}

function removeFilterFromBreadcrumb(filterId)
{
	var currentLocation = URLDecode(document.location.href);

	parameterName = "f";
	parameterValue = 	$("#"+filterId).html();

	parameterValue = parameterValue.replace("&amp;","&");

	var parameter=parameterValue + " OR ";
	currentLocation = currentLocation.replace(parameter,"");

	parameter=" OR " + parameterValue;
	currentLocation = currentLocation.replace(parameter,"");	
	
	var parameter="&"+parameterName+"="+parameterValue;
	
	currentLocation = currentLocation.replace(parameter,"");
	
	parameter="?"+parameterName+"="+parameterValue;
	currentLocation = currentLocation.replace(parameter,"");
	
	$.ajax({
		type: "POST",
		dataType: "json",
		url: "pinunpin",
		data: "a=r&filter="+filter,
		success: function()
		{
			document.location.href=currentLocation;
		}
	});			
}

function removePzFilter(filter)
{
	var currentLocation = URLDecode(document.location.href);
	currentLocation = currentLocation.replace(filter,"");
	document.location.href=currentLocation;
}

function URLDecode( encoded)
{
	if (encoded == null || encoded == undefined) return '';
	return decodeURIComponent(encoded.replace(/\+/g,  " "));
}

function export2()
{
	var newWindow = window.open("export?target="+document.getElementById('export').value, '_new');
	newWindow.focus();
	return false;
}

function ogdownload(){document.location="download?format="+document.getElementById('download').value;}

function goTo(id)
{	
	$('html,body').animate({scrollTop: ($("#"+id).offset().top-175)},'slow');
}

function toggle(uri,category)
{
	$.ajax({
		type: "POST",
		dataType: "json",
		mimeType: "application/json",
		url: "selector",
		data: "a=toggle&uri="+uri+"&cbs="+(category != 'dg'),
		success: function(data)
		{
			var exportCount = data.result.export2;
			var sendCount = data.result.send;
			if (exportCount > 0)
			{
				$("#export-or-download").slideDown(1000);
				document.getElementById('selected-export-count').innerHTML=exportCount;
			} else 
			{
				$("#export-or-download").hide(1000);		
			}

			if (sendCount > 0)
			{
				$("#send-by-email").slideDown(1000);
				document.getElementById('selected-send-count').innerHTML=sendCount;
			} else 
			{
				$("#send-by-email").hide(1000);		
			}
			
			if (sendCount == 0 || exportCount == 0)
			{
				$("#info").slideDown(1000);		
				$("#deselect-info").hide(1000);	
			} else
			{
				$("#info").hide(600);										
				$("#deselect-info").slideDown(600);	
			}
			
			document.getElementById("check_"+uri).src="img/checked_"+data.result.selected+".png";
			
		}
	});	
}

function removeLibFilter(filterValue)
{
	$.ajax({
		type: "GET",
		dataType: "json",
		url: "advanced",
		data: "a=filterLibRemove&filter="+filterValue,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function removeBranchFilter(filterValue)
{
	$.ajax({
		type: "GET",
		dataType: "json",
		url: "advanced",
		data: "a=filterBranchRemove&filter="+filterValue,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function pin(filter)
{
	$.ajax({
		type: "POST",
		dataType: "json",
		url: "pinunpin",
		data: "a=pin&filter="+filter,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function unpin(filter)
{
	$.ajax({
		type: "POST",
		dataType: "json",
		url: "pinunpin",
		data: "a=r&filter="+filter,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function ftoggle(uri, recid, offset)
{
	$.ajax({
		type: "POST",
		dataType: "json",
		mimeType: "application/json",
		url: "selector",
		data: "a=toggle&uri="+uri+"&cbs=true&recid="+recid+"&offset="+(parseInt(offset) - 1),
		success: function(data)
		{
			var exportCount = data.result.export2;
			var sendCount = data.result.send;
			if (exportCount > 0)
			{
				$("#export-or-download").slideDown(1000);
				document.getElementById('selected-export-count').innerHTML=exportCount;
			} else 
			{
				$("#export-or-download").hide(1000);		
			}

			if (sendCount > 0)
			{
				$("#send-by-email").slideDown(1000);
				document.getElementById('selected-send-count').innerHTML=sendCount;
			} else 
			{
				$("#send-by-email").hide(1000);		
			}
			
			if (sendCount == 0 || exportCount == 0)
			{
				$("#info").slideDown(1000);		
				$("#deselect-info").hide(1000);	
			} else
			{
				$("#info").hide(600);										
				$("#deselect-info").slideDown(600);	
			}
			
			document.getElementById("check_"+uri).src="img/checked_"+data.result.selected+".png";
			
		}
	});	
}

function sbntoggle(id,posInResultSet)
{
	$.ajax({
		type: "POST",
		dataType: "json",
		mimeType: "application/json",
		url: "selector",
		data: "a=sbntoggle&id="+id+"&pos="+posInResultSet+"&cbs=true",
		success: function(data)
		{
			var exportCount = data.result.export2;
			var sendCount = data.result.send;
			if (exportCount > 0)
			{
				$("#export-or-download").slideDown(1000);
				document.getElementById('selected-export-count').innerHTML=exportCount;
			} else 
			{
				$("#export-or-download").hide(1000);		
			}

			if (sendCount > 0)
			{
				$("#send-by-email").slideDown(1000);
				/*document.getElementById('selected-send-count').innerHTML=sendCount;*/
			} else 
			{
				$("#send-by-email").hide(1000);		
			}
			
			if (sendCount == 0 || exportCount == 0)
			{
				$("#info").slideDown(1000);		
				$("#deselect-info").hide(1000);	
			} else
			{
				$("#info").hide(600);										
				$("#deselect-info").slideDown(600);	
			}
			document.getElementById("check_"+id).src="img/checked_"+data.result.selected+".png";
			
		}
	});	
}

function clearSelection()
{
	$.ajax({
		type: "POST",
		dataType: "json",
		url: "selector",
		data: "a=clean",
		success: function()
		{
			$("#info").slideDown(1000);		
			$("#deselect-info").hide(1000);	
			$("#export-or-download").hide(1000);	
			$("#send-by-email").hide(1000);
			$('img[id*="check_"]').attr("src","img/checked_false.png");
		}
	});	
}

function removeSearchEntry(id)
{
	$.ajax({
		type: "POST",
		url: "history",
		data: "id="+id,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function add2Bibliography(id)
{
	$.ajax({
		type: "POST",
		url: "bibliography",
		data: "a=add&uri="+id,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function removeFromBibliography(id)
{
	$.ajax({
		type: "POST",
		url: "bibliography",
		data: "a=remove&uri="+id,
		success: function()
		{
			document.location.reload();
		}
	});			
}

function createTag(tagNameText,uri)
{
	$.ajax({
		type: "POST",
		url: "newtag",
		data: "tag=" +tagNameText.value+"&uri="+uri,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function addNewTag(id)
{
	$("#newtag_"+id).html("<input id=\"taglabel\" type=\"text\" name=\"tag\" onchange=\"createTag(this,'" +id+"')\">");
	$("#taglabel").focus();
}

function createWishList(wlnameText,uri)
{
	$.ajax({
		type: "POST",
		url: "newwishlist",
		data: "name=" +wlnameText.value+"&uri="+uri,
		success: function()
		{
			document.location.reload();
		}
	});		
}


function addNewWishList(id)
{
	$("#newwishlist_"+id).html("<input id='wlname' type=\"text\" onchange=\"createWishList(this,'" +id+"')\">");
	$("#wlname").focus();
}

function add2WishList(id)
{
	$.ajax({
		type: "POST",
		url: "add2Wishlist",
		data: "id=" +document.getElementById('add2wl').value+"&uri="+id,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function removeFromWishList(wishlist_id, item_id)
{
	$.ajax({
		type: "POST",
		url: "removeFromWishlist",
		data: "wid=" +wishlist_id+"&uri="+item_id,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function removeFromTag(tag_id, item_id)
{
	$.ajax({
		type: "POST",
		url: "removeFromTag",
		data: "tagid=" +tag_id+"&uri="+item_id,
		success: function()
		{
			document.location.reload();
		}
	});		
}

function addNewReview(item_id)
{
	$("#newreview_"+item_id).html("<form action=\"newreview\" method=\"post\"><input id=\"review\" type=\"text\" name=\"review\"><input type=\"hidden\" name=\"location\" value=\""+document.location.href+"\"><input type=\"hidden\" name=\"uri\" value=\""+item_id+"\"></form>");
	$("#newreview").focus();	
}


function closeModalPopup()
{
	$.prettyPhoto.close();
}

function toggleLimit(limitName, limitEntryName, checked, count, outsider)
{
	$.ajax({
		type: "POST",
		url: "toggleLimit",
		data: "l="+limitName+"&n="+limitEntryName+"&co="+count+"&c="+checked+"&s="+outsider
	});			
}

function checkBrowsingQuery(message)
{
	if (document.searchForm.from.value == '')
	{
		alert(message);
	} else
	{
		document.searchForm.submit();
	}
}

function checkQuery(message)
{
	if (document.searchForm.q.value == '')
	{
		alert(message);
	} else
	{
		document.searchForm.submit();
	}
}
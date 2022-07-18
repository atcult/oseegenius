var Request;
$(document).ready(function() {

	Request = {
		parameter : function(name) {
			return this.parameters()[name];
		},
		parameters : function() {
			var result = {};
			var url = window.location.href;
			var parameters = url.slice(url.indexOf('?') + 1).split('&');

			for (var i = 0; i < parameters.length; i++) {
				var parameter = parameters[i].split('=');
				result[parameter[0]] = parameter[1];
			}
			return result;
		}
	};
});

function checkTargets(element){
	var formTargetsData={}
	for (var i = 0; i < element.form.length; i++) {
		if(element.form[i].checked){
			formTargetsData[element.form[i].name]=i;
		}
	}
		$.ajax({
			type: "POST",
			url: "targets",
			dataType: "text",
			async: true,
			data: formTargetsData
		});
}

function availability() {
	var id = $.map($('.availability'), function(i) {
		return $(i).attr('id');
	});

	if (id.length > 0) {
		for (var i = 0; i < id.length; i++) {
			var uri = id[i];
			$.ajax({
				dataType : 'json',
				url : 'availability?uri=' + uri,
				success : function(response) {
					$("#" + response.u).removeClass("looking-for");
					$("#" + response.u).addClass("book_avaible_" + response.c);
					document.getElementById(response.u).innerHTML = response.m;
				}
			});
		}
	}
}

function mlCheck(ml, limitName, hasBeenChecked) {
	if (limitName == "library") {
		if (hasBeenChecked) {
			$('input[id=' + ml + ']').attr("checked", true);
			$('input[id*=' + ml + '_]').attr("checked", true);
			$('input[id*=' + ml + '_]').attr("disabled", true);
		} else {
			$('input[id=' + ml + ']').removeAttr("checked");
			$('input[id*=' + ml + '_]').removeAttr("checked");
			$('input[id*=' + ml + '_]').removeAttr("disabled");
		}
	} else {
		if (hasBeenChecked) {
			$('input[id*=' + '_' + ml + ']').attr("checked", true);
		} else {
			$('input[id*=' + '_' + ml + ']').removeAttr("checked");
		}
	}
}

function brCheck(br, ml) {
	$('#' + ml).attr("checked", false);
}

function reSubmitSearch() {
	if ($("#q").val() == '')
		return;

	var v = Request.parameter('v');
	if (v != undefined && v != '') {
		document.searchForm.v.value = v;
	}
	document.searchForm.submit();
}

function reSubmitBrowse() {
	if ($("#q").val() == '')
		return;
	document.searchForm.submit();
}

// [USED]
function l10n(code) {
	manipulateQueryString('l', code);
}

function changePerspective(parameterName, parameterValue) {
	// search for &p=
	var parameter = "&" + parameterName + "=";
	var currentLocation = document.location.href;
	var startIndex = currentLocation.indexOf(parameter);
	if (startIndex == -1) {
		// search for ?p=
		parameter = "?" + parameterName + "=";
		startIndex = currentLocation.indexOf(parameter);
	}

	if (startIndex != -1) {

		var endIndex = currentLocation.indexOf("&", startIndex + 1);
		if (endIndex != -1) {
			currentLocation = currentLocation.substring(0, startIndex + 1)
					+ currentLocation.substring(endIndex);
		} else {
			currentLocation = currentLocation.substring(0, startIndex);
		}
	}

	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")
			+ parameterName + "=";
	var location = currentLocation + parameter + parameterValue;
	if (location.indexOf("dls=true") == -1) {
		location += "&dls=true";
	}
	document.location.href = location;
}

function requery(parameterName, parameterValue) {
	$('.autocomplete-w1').remove();
	$.ajaxSetup({
		cache : false
	});

	var a = $('#q').autocomplete({
		serviceUrl : 'suggest',
		minChars : 2,
		maxHeight : 500,
		width : 300,
		zIndex : 9999,
		deferRequestBy : 0,
		noCache : false,
		onSelect : function(value, data) {
			reSubmitSearch();
		}
	});

	if (document.location.href.indexOf("q=") == -1) {
		return;
	}

	manipulateQueryString(parameterName, parameterValue);
}

function requeryAuth(parameterName, parameterValue) {

	$.ajaxSetup({
		cache : false
	});

	var a = $('#q').autocomplete({
		serviceUrl : 'authsuggest',
		minChars : 2,
		maxHeight : 500,
		width : 300,
		zIndex : 9999,
		deferRequestBy : 0,
		noCache : false,
		onSelect : function(value, data) {
			reSubmitSearch();
		}
	});

	if (document.location.href.indexOf("q=") == -1) {
		return;
	}

	manipulateQueryString(parameterName, parameterValue);
}

function manipulateQueryString(parameterName, parameterValue) {
	// if (document.location.href.indexOf("q=") == -1 && parameterName != 'v' &
	// parameterName != 'l' && document.location.href.indexOf("show") == -1)
	// return;

	// search for &p=
	var parameter = "&" + parameterName + "=";
	var currentLocation = document.location.href;
	var startIndex = currentLocation.indexOf(parameter);
	if (startIndex == -1) {
		// search for ?p=
		parameter = "?" + parameterName + "=";
		startIndex = currentLocation.indexOf(parameter);
	}

	if (startIndex != -1) {

		var endIndex = currentLocation.indexOf("&", startIndex + 1);
		if (endIndex != -1) {
			currentLocation = currentLocation.substring(0, startIndex + 1)
					+ currentLocation.substring(endIndex);
		} else {
			currentLocation = currentLocation.substring(0, startIndex);
		}
	}

	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")
			+ parameterName + "=";
	document.location.href = (currentLocation + parameter + parameterValue);
}

function mlt(parameterName, parameterValue) {
	// search for &p=
	var parameter = "&" + parameterName + "=";
	var currentLocation = document.location.href;
	var startIndex = currentLocation.indexOf(parameter);
	if (startIndex == -1) {
		// search for ?p=
		parameter = "?" + parameterName + "=";
		startIndex = currentLocation.indexOf(parameter);
	}

	if (startIndex != -1) {

		var endIndex = currentLocation.indexOf("&", startIndex + 1);
		if (endIndex != -1) {
			currentLocation = currentLocation.substring(0, startIndex + 1)
					+ currentLocation.substring(endIndex);
		} else {
			currentLocation = currentLocation.substring(0, startIndex);
		}
	}

	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")
			+ parameterName + "=";
	newLocation = (currentLocation + parameter + parameterValue);
	if (newLocation.indexOf("#mlt") == -1) {
		newLocation += "#mlt";
	}
	document.location.href = newLocation;
}

function gotoPage() {
	manipulateQueryString('p', $("#requestedPage").val());
}

function back(from) {
	var currentLocation = document.location.href;
	currentLocation = replaceParameter('from', from, currentLocation);
	currentLocation = replaceParameter('direction', 'back', currentLocation);
	document.location.href = currentLocation;
}

function forward(from) {
	var currentLocation = document.location.href;
	currentLocation = replaceParameter('from', from, currentLocation);
	currentLocation = replaceParameter('direction', 'forward', currentLocation);
	document.location.href = currentLocation;
}

function replaceParameter(parameterName, parameterValue, currentLocation) {
	var parameter = "&" + parameterName + "=";
	var startIndex = currentLocation.indexOf(parameter);
	if (startIndex == -1) {
		parameter = "?" + parameterName + "=";
		startIndex = currentLocation.indexOf(parameter);
	}

	if (startIndex != -1) {
		var endIndex = currentLocation.indexOf("&", startIndex + 1);
		if (endIndex != -1) {
			currentLocation = currentLocation.substring(0, startIndex)
					+ currentLocation.substring(endIndex);
		} else {
			currentLocation = currentLocation.substring(0, startIndex);
		}
	}

	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")
			+ parameterName + "=";
	return currentLocation + parameter + parameterValue;
}

function filter(fieldName, fieldValue) {
	fieldValue = fieldValue.replace("&", "%26");
	// replace square bracket works only on production...
	fieldValue = fieldValue.replace("\[", "%5B").replace("\]", "%5D");
	parameterName = "f";
	parameterValue = (fieldName != 'xt' ? (fieldName + ":\""
			+ fieldValue.replace(/\"/g, "\\\"") + "\"")
			: (fieldName + ":" + fieldValue));
	var parameter = "&" + parameterName + "=" + parameterValue;
	var currentLocation = document.location.href;
	var startIndex = currentLocation.indexOf(parameter);

	if (startIndex == -1) {
		parameter = "?" + parameterName + "=" + parameterValue;
		startIndex = currentLocation.indexOf(parameter);
	}

	if (startIndex != -1) {
		var endIndex = currentLocation.indexOf("&", startIndex + 1);
		if (endIndex != -1) {
			location = currentLocation.substring(0, startIndex)
					+ currentLocation.substring(endIndex);
		} else {
			currentLocation = currentLocation.substring(0, startIndex);
		}
	}

	currentLocation = currentLocation.replace(/[&]p=[0-9]*/i, "");
	currentLocation = currentLocation.replace(/[?]p=[0-9]*/i, "?");

	parameter = ((currentLocation.indexOf("?") != -1) ? "&" : "?")
			+ parameterName + "=";
	document.location.href = (currentLocation + parameter + parameterValue);
}

function removeFilter(fieldName, fieldValue) {
	var currentLocation = URLDecode(document.location.href);

	parameterName = "f";
	/*
	 * parameterValue = (fieldName != 'xt') ? (fieldName+":\""+fieldValue+"\"") :
	 * (fieldName+":"+fieldValue);
	 */
	parameterValue = (fieldName != 'xt' ? (fieldName + ":\""
			+ fieldValue.replace(/\"/g, "\\\"") + "\"")
			: (fieldName + ":" + fieldValue));

	var parameter = parameterValue + " OR ";
	currentLocation = currentLocation.replace(parameter, "");

	parameter = " OR " + parameterValue;
	currentLocation = currentLocation.replace(parameter, "");

	var parameter = "&" + parameterName + "=" + parameterValue;

	currentLocation = currentLocation.replace(parameter, "");

	var startIndexOfPageNumber = currentLocation.indexOf("&p=");
	if (startIndexOfPageNumber == -1) {
		startIndexOfPageNumber = currentLocation.indexOf("?p=");
	}

	if (startIndexOfPageNumber != -1) {
		currentLocation = currentLocation.substring(0, startIndexOfPageNumber)
				+ currentLocation.substring(startIndexOfPageNumber + 4);
	}

	parameter = "?" + parameterName + "=" + parameterValue;
	currentLocation = currentLocation.replace(parameter, "");

	$.ajax({
		type : "POST",
		dataType : "json",
		url : "pinunpin",
		data : "a=r&filter=" + fieldName + ":\"" + fieldValue + "\"",
		success : function() {
			document.location.href = currentLocation;
		}
	});
}

// [USED]
function toggleLimit(limitName, limitEntryName, checked, count, outsider) {
	$
			.ajax({
				type : "POST",
				url : "toggleLimit",
				data : "l=" + limitName + "&n=" + limitEntryName + "&co="
						+ count + "&c=" + checked + "&s=" + outsider,
				success : function(response) {
					if (count == -1 && limitName != 'library'
							&& limitName != 'branch') {
						$("[name='" + limitName + ":" + limitEntryName + "']")
								.removeAttr("checked");
					}

					mlCheck(limitEntryName, limitName, checked);
					showConstraints();
				}
			});
}

function showConstraints() {
	$.getJSON('toggleLimit', function(data) {
		var limits = [];
		$.each(data.limits, function(index) {
			var link = '';
			if (index > 0) {
				link += ' &nbsp;&gt;&gt;&nbsp;';
			}
			link += '<a onclick="javascript:toggleLimit(\''
					+ data.limits[index].name + '\',\''
					+ data.limits[index].value
					+ '\',false,-1,false)" style="cursor:pointer;">' + '<i>'
					+ data.limits[index].lname + '</i>:<b>'
					+ data.limits[index].lvalue + '</b></a>';
			limits.push(link);
		});

		$("#constraints").html(limits.join(''))
	});
}

function refreshConstraints() {
	$.getJSON('toggleLimit', function(data) {
		var limits = [];
		$.each(data.limits, function(index) {
			mlCheck(data.limits[index].value, data.limits[index].name, true);
			var link = '';
			if (index > 0) {
				link += ' &nbsp;&gt;&gt;&nbsp;';
			}
			link += '<a onclick="javascript:toggleLimit(\''
					+ data.limits[index].name + '\',\''
					+ data.limits[index].value
					+ '\',false,-1,false)" style="cursor:pointer;">' + '<i>'
					+ data.limits[index].lname + '</i>:<b>'
					+ data.limits[index].lvalue + '</b></a>';
			limits.push(link);
		});

		$("#constraints").html(limits.join(''))
	});
}

function removeFilterFromBreadcrumb(filterId) {
	var currentLocation = URLDecode(document.location.href);

	parameterName = "f";
	parameterValue = $("#" + filterId).html();
	parameterValue = parameterValue.replace("&amp;", "&");

	/*
	 * Bug 3118:Aggiunto filtro per formato e-book per query da homepage su
	 * anteprima novità di narrativa; correzione per eliminazione parametro che
	 * indica il chiamante nella rimozione del filtro dalla pagina dei risultati
	 */
	var startIndexOfCall = currentLocation.indexOf("&call=");
	if (startIndexOfCall != -1 && parameterValue == "dewey:853*") {
		var parCall = "&call=itFiction";
		currentLocation = currentLocation.replace(parCall, "");
	}
	var startIndexFormatFilter = currentLocation.indexOf("&f=-format:docebook");
	if (startIndexFormatFilter != -1 && parameterValue == "dewey:853*") {
		var parFormatFilter = "&f=-format:docebook";
		currentLocation = currentLocation.replace(parFormatFilter, "");
	}
	/* Bug 3118:fine */

	var parameter = parameterValue + " OR ";
	currentLocation = currentLocation.replace(parameter, "");

	parameter = " OR " + parameterValue;
	currentLocation = currentLocation.replace(parameter, "");

	var parameter = "&" + parameterName + "=" + parameterValue;

	currentLocation = currentLocation.replace(parameter, "");

	parameter = "?" + parameterName + "=" + parameterValue;
	currentLocation = currentLocation.replace(parameter, "");

	$.ajax({
		type : "POST",
		dataType : "json",
		url : "pinunpin",
		data : "a=r&filter=" + filter,
		success : function() {
			document.location.href = currentLocation;
		}
	});
}

function removePzFilter(filter) {
	var currentLocation = URLDecode(document.location.href);
	currentLocation = currentLocation.replace(filter, "");
	document.location.href = currentLocation;
}

function URLDecode(encoded) {
	if (encoded == null || encoded == undefined)
		return '';
	return decodeURIComponent(encoded.replace(/\+/g, " "));
}

function export2() {
	var newWindow = window.open("export?target=refworks", '_new');
	newWindow.focus();
	return false;
}

function ogdownload(format) {
	document.location = "download?format=" + format;
}

function ogdownloadImss(format) {
	document.location = "downloadHtml?format=" + format;
}

function toggle(uri, category) {
	$
			.ajax({
				type : "POST",
				dataType : "json",
				mimeType : "application/json",
				url : "selector",
				data : "a=toggle&uri=" + uri + "&cbs=" + (category != 'dg'),
				success : function(data) {
					var exportCount = data.result.export2;
					var sendCount = data.result.send;
					if (exportCount > 0) {
						$("#export-or-download").slideDown(1000);
						document.getElementById('selected-export-count').innerHTML = exportCount;
					} else {
						$("#export-or-download").hide(1000);
					}

					if (sendCount > 0) {
						$("#send-by-email").slideDown(1000);
						document.getElementById('selected-send-count').innerHTML = sendCount;
					} else {
						$("#send-by-email").hide(1000);
					}

					if (sendCount == 0 || exportCount == 0) {
						$("#info").slideDown(1000);
						$("#deselect-info").hide(1000);
					} else {
						$("#info").hide(600);
						$("#deselect-info").slideDown(600);
					}

					document.getElementById("check_" + uri).src = "img/checked_"
							+ data.result.selected + ".png";

				}
			});
}

function toggle2(uri, check_id, category) {
	$
			.ajax({
				type : "POST",
				dataType : "json",
				mimeType : "application/json",
				url : "selector",
				data : "a=toggle&uri=" + uri + "&cbs=" + (category != 'dg'),
				success : function(data) {
					var exportCount = data.result.export2;
					var sendCount = data.result.send;
					if (exportCount > 0) {
						$("#export-or-download").slideDown(1000);
						document.getElementById('selected-export-count').innerHTML = exportCount;
					} else {
						$("#export-or-download").hide(1000);
					}

					if (sendCount > 0) {
						$("#send-by-email").slideDown(1000);
						document.getElementById('selected-send-count').innerHTML = sendCount;
					} else {
						$("#send-by-email").hide(1000);
					}

					if (sendCount == 0 || exportCount == 0) {
						$("#info").slideDown(1000);
						$("#deselect-info").hide(1000);
					} else {
						$("#info").hide(600);
						$("#deselect-info").slideDown(600);
					}

					document.getElementById("check_" + uri).src = "img/checked_"
							+ data.result.selected + ".png";
				}
			});
}

function toggleAll(elements, checked_value, check_id) {
	$
			.ajax({
				type : "POST",
				dataType : "json",
				mimeType : "application/json",
				url : "selector",
				data : "a=toggleAll&checkAll=" + checked_value,
				success : function(data) {
					$('img[id*="check_"]').attr("src",
							"img/checked_" + checked_value + ".png");
					var exportCount = data.result.export2;
					var sendCount = data.result.send;
					if (exportCount > 0) {
						$("#export-or-download").slideDown(1000);
						document.getElementById('selected-export-count').innerHTML = exportCount;
					} else {
						$("#export-or-download").hide(1000);
					}

					if (sendCount > 0) {
						$("#send-by-email").slideDown(1000);
						document.getElementById('selected-send-count').innerHTML = sendCount;
					} else {
						$("#send-by-email").hide(1000);
					}

					if (sendCount == 0 || exportCount == 0) {
						$("#info").slideDown(1000);
						$("#deselect-info").hide(1000);
					} else {
						$("#info").hide(600);
						$("#deselect-info").slideDown(600);
					}
				}
			});
}

function removeLibFilter(filterValue) {
	$.ajax({
		type : "GET",
		dataType : "json",
		url : "advanced",
		data : "a=filterLibRemove&filter=" + filterValue,
		success : function() {
			document.location.reload();
		}
	});
}

function removeBranchFilter(filterValue) {
	$.ajax({
		type : "GET",
		dataType : "json",
		url : "advanced",
		data : "a=filterBranchRemove&filter=" + filterValue,
		success : function() {
			document.location.reload();
		}
	});
}

function pin(filter) {
	$.ajax({
		type : "POST",
		dataType : "json",
		url : "pinunpin",
		data : "a=pin&filter=" + filter,
		success : function() {
			document.location.reload();
		}
	});
}

function unpin(filter) {
	$.ajax({
		type : "POST",
		dataType : "json",
		url : "pinunpin",
		data : "a=r&filter=" + filter,
		success : function() {
			document.location.reload();
		}
	});
}

function ftoggle(uri, recid, offset) {
	$
			.ajax({
				type : "POST",
				dataType : "json",
				mimeType : "application/json",
				url : "selector",
				data : "a=toggle&uri=" + uri + "&cbs=true&recid=" + recid
						+ "&offset=" + (parseInt(offset) - 1),
				success : function(data) {
					var exportCount = data.result.export2;
					var sendCount = data.result.send;
					if (exportCount > 0) {
						$("#export-or-download").slideDown(1000);
						document.getElementById('selected-export-count').innerHTML = exportCount;
					} else {
						$("#export-or-download").hide(1000);
					}

					if (sendCount > 0) {
						$("#send-by-email").slideDown(1000);
						document.getElementById('selected-send-count').innerHTML = sendCount;
					} else {
						$("#send-by-email").hide(1000);
					}

					if (sendCount == 0 || exportCount == 0) {
						$("#info").slideDown(1000);
						$("#deselect-info").hide(1000);
					} else {
						$("#info").hide(600);
						$("#deselect-info").slideDown(600);
					}
					document.getElementById("check_" + uri).src = "img/checked_"
							+ data.result.selected + ".png";

				}
			});
}

function sbntoggle(id, posInResultSet) {
	$
			.ajax({
				type : "POST",
				dataType : "json",
				mimeType : "application/json",
				url : "selector",
				data : "a=sbntoggle&id=" + id + "&pos=" + posInResultSet
						+ "&cbs=true",
				success : function(data) {
					var exportCount = data.result.export2;
					var sendCount = data.result.send;
					if (exportCount > 0) {
						$("#export-or-download").slideDown(1000);
						document.getElementById('selected-export-count').innerHTML = exportCount;
					} else {
						$("#export-or-download").hide(1000);
					}

					if (sendCount > 0) {
						$("#send-by-email").slideDown(1000);
						document.getElementById('selected-send-count').innerHTML = sendCount;
					} else {
						$("#send-by-email").hide(1000);
					}

					if (sendCount == 0 || exportCount == 0) {
						$("#info").slideDown(1000);
						$("#deselect-info").hide(1000);
					} else {
						$("#info").hide(600);
						$("#deselect-info").slideDown(600);
					}
					document.getElementById("check_" + id).src = "img/checked_"
							+ data.result.selected + ".png";

				}
			});
}

function clearSelection() {
	$.ajax({
		type : "POST",
		dataType : "json",
		url : "selector",
		data : "a=clean",
		success : function() {
			$("#info").slideDown(1000);
			$("#deselect-info").hide(1000);
			$("#export-or-download").hide(1000);
			$("#send-by-email").hide(1000);
			$('img[id*="check_"]').attr("src", "img/checked_false.png");
			$('input[id*="select_all_"]').attr("checked", false);
		}
	});
}

function removeSearchEntry(id) {
	$.ajax({
		type : "POST",
		url : "history",
		data : "id=" + id,
		success : function() {
			document.location.reload();
		}
	});
}

function removeElementResearchHistory(id) {
	$.ajax({
		type : "POST",
		url : "folioResearchHistory",
		data : "idSearch=" + id,
		success : function() {
			document.location.reload();
		}
	});
}





function add2Bibliography(id) {
	$.ajax({
		type : "POST",
		url : "bibliography",
		data : "a=add&uri=" + id,
		success : function() {
			document.location.reload();
		}
	});
}

function removeFromBibliography(id) {
	$.ajax({
		type : "POST",
		url : "bibliography",
		data : "a=remove&uri=" + id,
		success : function() {
			document.location.reload();
		}
	});
}

function createTag(tagNameText, uri) {
	$.ajax({
		type : "POST",
		url : "newtag",
		data : "tag=" + tagNameText.value + "&uri=" + uri,
		success : function() {
			document.location.reload();
		}
	});
}

function addNewTag(id) {
	$("#newtag_" + id).html(
			"<input id=\"taglabel\" type=\"text\" name=\"tag\" onchange=\"createTag(this,'"
					+ id + "')\">");
	$("#taglabel").focus();
}

function createWishList(wlnameText, uri) {
	$.ajax({
		type : "POST",
		url : "newwishlist",
		data : "name=" + wlnameText.value + "&uri=" + uri,
		success : function() {
			document.location.reload();
		}
	});
}

function addNewWishList(id) {
	$("#newwishlist_" + id).html(
			"<input id='wlname' type=\"text\" onchange=\"createWishList(this,'"
					+ id + "')\">");
	$("#wlname").focus();
}

function add2WishList(id) {
	$.ajax({
		type : "POST",
		url : "add2Wishlist",
		data : "id=" + document.getElementById('add2wl_' + id).value + "&uri="
				+ id,
		success : function() {
			document.location.reload();
		}
	});
}

function removeFromWishList(wishlist_id, item_id) {
	$.ajax({
		type : "POST",
		url : "removeFromWishlist",
		data : "wid=" + wishlist_id + "&uri=" + item_id,
		success : function() {
			document.location.reload();
		}
	});
}

function removeFromTag(tag_id, item_id) {
	$.ajax({
		type : "POST",
		url : "removeFromTag",
		data : "tagid=" + tag_id + "&uri=" + item_id,
		success : function() {
			document.location.reload();
		}
	});
}

function addNewReview(item_id) {
	$("#newreview_" + item_id)
			.html(
					"<form action=\"newreview\" method=\"post\"><input id=\"review\" type=\"text\" name=\"review\"><input type=\"hidden\" name=\"location\" value=\""
							+ document.location.href
							+ "\"><input type=\"hidden\" name=\"uri\" value=\""
							+ item_id + "\"></form>");
	$("#newreview").focus();
}

function removeReview(review_id) {
	$.ajax({
		type : "POST",
		url : "removeReview",
		data : "reviewId=" + review_id,
		success : function() {
			document.location.reload();
		}
	});
}

function reloadAll(collectionCode) {
	if (window.location.href.indexOf("advanced?a=reset") !== -1) {
		$
				.ajax({
					type : "GET",
					url : "updateCollection",
					data : "collection_data=" + collectionCode,
					success : function() {
						var url = window.location.href;
						var newUrl = changeParam("collection_data", url,
								collectionCode);
						window.location = newUrl;
					}
				});
	}

}

function resetAll() {
	// $('.autocomplete-w1').remove();
	$("#q").val('');
}

function resetAdvanced() {
	// $('.autocomplete-w1').remove();
	$("input").val('');
}

function changeParam(key, sourceURL, newValue) {
	var rtn = sourceURL.split("?")[0], param, params_arr = [], queryString = (sourceURL
			.indexOf("?") !== -1) ? sourceURL.split("?")[1] : "";
	if (queryString !== "") {
		params_arr = queryString.split("&");
		for (var i = params_arr.length - 1; i >= 0; i -= 1) {
			param = params_arr[i].split("=")[0];
			if (param === key) {
				// params_arr.splice(i, 1);
				params_arr[i] = key + "=" + newValue;
			}
		}
		rtn = rtn + "?" + params_arr.join("&");
	}
	console.log("inside method: " + rtn);
	return rtn;
}

function closeModalPopup() {
	$.prettyPhoto.close();
}

function submitAndGo(dest) {
	document.form.destination.value = dest;
	document.form.submit();
}

// -----------------------------------> FAB

function cattura(f) {
	var ans = "";
	// var lang = cercaLingua();
	var lingua = "ita";
	if (_lingua == "en") {
		lingua = "eng";
	}
	$('.check_link').each(function(i, v) {
		var an = v.childNodes[0].id.replace("check_", "").trim();
		if (i > 0) {
			ans += "|";
		}
		ans += an;
	});
	var form = $('<form></form>', {
		target : "_blank",
		action : "https://opac.museogalileo.it/ABLFull/Print",
		method : "POST"
	});
	$("<input></input>", {
		type : "hidden",
		name : "lang",
		value : lingua
	}).appendTo(form);
	$("<input></input>", {
		type : "hidden",
		name : "ans",
		value : ans
	}).appendTo(form);
	$("<input></input>", {
		type : "hidden",
		name : "formato",
		value : f
	}).appendTo(form);

	$(document.body).append(form);

	form.submit();
}
		
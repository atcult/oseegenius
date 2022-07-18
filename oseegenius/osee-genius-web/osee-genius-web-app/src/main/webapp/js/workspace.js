function wtoggle(uri,category)
{
	$.ajax({
		type: "POST",
		dataType: "json",
		mimeType: "application/json",
		url: "selector",
		data: "a=wtoggle&uri="+uri+"&cbs="+(category != 'dg')+"&scope=workspace",
		success: function(data)
		{
			var exportCount = data.result.export2;
			var sendCount = data.result.send;
			
			if (exportCount > 0)
			{
				$(".workspace-action-enabled").show();
				$(".workspace-action-disabled").hide();
			} else 
			{
				$(".workspace-action-enabled").hide();
				$(".workspace-action-disabled").show();
			}
			document.getElementById("check_"+uri).src="img/checked_"+data.result.selected+".png";
			
		}
	});	
}

function clearWorkspaceSelection()
{
	$.ajax({
		type: "POST",
		dataType: "json",
		url: "selector",
		data: "a=clean",
		success: function()
		{
			$(".workspace-action-enabled").hide();
			$(".workspace-action-disabled").show();
			$('img[id*="check_"]').attr("src","img/checked_false.png");
		}
	});	
}
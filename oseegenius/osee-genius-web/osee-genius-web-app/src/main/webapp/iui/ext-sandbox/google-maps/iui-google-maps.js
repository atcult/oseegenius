/*
   Copyright (c) 2007-9, iUI Project Members
   See LICENSE.txt for licensing terms
   ************
   LAST UDPATE: Oct 30th, 2010 - remi.grumeau@gmail.com
   ************
*/

iui.gmap = {

	map: null,
	lat: null,
	lng: null,
	zoom: 8,
	mapdiv: null,
	maptype: 'ROADMAP',
	navigationControl: true,
	opt : null,

	load: function(callback)
	{
		if(typeof(google)=='undefined') {
			var gmapscript = document.createElement("script");
			gmapscript.type = "text/javascript";
			gmapscript.src = "http://maps.google.com/maps/api/js?sensor=false";
			document.getElementsByTagName('head')[0].appendChild(gmapscript);
			
			function isLoaded()
			{
				if(typeof(google)=='object')
					eval(callback);
				else
					setTimeout(isLoaded, 300);
			}
			setTimeout(isLoaded, 300);
		}
		else
			eval(callback);
	},
	
	init: function() 
	{
		if(iui.gmap.map==null)
		{
			var latlng = new google.maps.LatLng(iui.gmap.lat,iui.gmap.lng);
			var maptype=google.maps.MapTypeId[iui.gmap.maptype];
			iui.gmap.opt = {
				draggable: true,
				zoom: iui.gmap.zoom,
				center: latlng,
				navigationControl: iui.gmap.navigationControl,
				mapTypeId: maptype
			}
			iui.gmap.map = new google.maps.Map(document.getElementById(iui.gmap.mapdiv), iui.gmap.opt);
		}
		else
			iui.gmap.update();
	},

	update: function() 
	{
		google.maps.event.trigger(iui.gmap.map, 'resize');
		var latlng = new google.maps.LatLng(iui.gmap.lat,iui.gmap.lng);
		iui.gmap.map.setCenter(latlng);
		iui.gmap.map.setZoom(iui.gmap.zoom);
	},

	locate: function() 
	{
		document.getElementById('locatePrompt').style.display='block';

		iui.gmap.init();
		if(navigator.geolocation)
			iui.gmap.watchId = navigator.geolocation.getCurrentPosition(setLocation, errorCallback, {maximumAge:6000});
		else
			alert('Geolocalisation not available');

		function errorCallback()
		{	alert('Error occured...');	}
		
		function setLocation(position)
		{
			iui.gmap.watchId = null;

			iui.gmap.lat = position.coords.latitude;
			iui.gmap.lng = position.coords.longitude;
			iui.gmap.update();
			document.getElementById('locatePrompt').style.display='none';

			var latlng = new google.maps.LatLng(iui.gmap.lat,iui.gmap.lng);			
			var marker = new google.maps.Marker({
				position: latlng,
				map: iui.gmap.map
			});
		}
	},

	clear: function()
	{
		if(document.getElementById(iui.gmap.mapdiv))
			document.getElementById(iui.gmap.mapdiv).innerHTML='';
		iui.gmap.map = null;
	}

}
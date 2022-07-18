function setWidthHeight ()
{
	newWidth =
		window.document.documentElement.clientWidth
		- window.document.getElementById ("subMenu").clientWidth
		- window.document.getElementById ("options").clientWidth
		- (window.document.getElementById ("copyright").clientHeight * 2);
	newHeight =
		window.document.documentElement.clientHeight
		- window.document.getElementById ("menu").clientHeight
		- (window.document.getElementById ("copyright").clientHeight * 4);
	window.document.getElementById("subMenu").style.height = newHeight;
	window.document.getElementById("options").style.height = newHeight;
	window.document.getElementById("content").style.height = newHeight;
	window.document.getElementById("content").style.width = newWidth-20;
}

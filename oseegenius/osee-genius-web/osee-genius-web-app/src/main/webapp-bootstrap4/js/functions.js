// JavaScript Document

function changeFontSize(type) {
	$('body').removeClass('normal medium large').addClass(type);	
	$.ajax({
		type: "GET",
		dataType: "json",
		url: "changefs",
		data: "size="+type
	});		
}

var current_item  = new Array();
var interval_item = new Array();

function boxhpViewItem(box,itemid) {
	$(box+' .controls a').removeClass('active');
	$(box+' .controls a:eq('+itemid+')').addClass('active');
	
	$(box+' .item').hide();
	$(box+' .item:eq('+itemid+')').show();
	current_item[box] = itemid;
	clearInterval(interval_item[box]);
	
}

function boxhpViewItemSlide(box,duration) {
	var tot_items = $(box+' .item').length;
	$(box+' .item').hide();	
	if(!current_item[box]) {
		current_item[box] = 0;
		$(box+' .item').first().show();
	} else {
		$(box+' .item:eq('+current_item[box]+')').show();
	}	
	if(tot_items>1) {
		interval_item[box] = setInterval(function() {
			current_item[box]++;
			if(current_item[box]==tot_items) current_item[box] = 0;
			$(box+' .item').hide();
			$(box+' .item:eq('+current_item[box]+')').show();
			$(box+' .controls a').removeClass('active');
			$(box+' .controls a:eq('+current_item[box]+')').addClass('active');
		},duration);
	}
}

function goTo(id) {
	$('.slidemenu ul').slideUp();
	$('#letter-'+id+' ul.lvl-1').slideDown(function() {
	  $('html,body').animate({
      scrollTop: '+='+parseFloat($('#letter-'+id).offset().top-200)+'px'
    }, 'fast');														 
  });
}


$(document).ready(function() {
	
	/* sliderHP */
	boxhpViewItemSlide('#box1',3000);
	boxhpViewItemSlide('#box2',3000);
	boxhpViewItemSlide('#box3',3000);
	boxhpViewItemSlide('#box4',3000);	
	$('.boxHP').hover(function() { box = "#"+$(this).attr('id'); clearInterval(interval_item[box]); }, function() { box = "#"+$(this).attr('id'); boxhpViewItemSlide(box,4000); } );
	/* sliderHP */
	
	$('.slidemenu h3').click(function(event) {
		event.preventDefault();
		$(this).next('ul').slideToggle();														 
	})
	
	$('.multi').click(function(event) {
		event.preventDefault();		
		id = $(this).attr("id");
		$('#children-'+id).slideToggle();
	})

// This function use JQuery UI, without doesen't work.	
/*	$(function() {	
		$('select#h').selectmenu({style:'dropdown'});
		$('select#s').selectmenu({style:'dropdown'}); 
		$('select#o').selectmenu({style:'dropdown'}); 
		$('select#i').selectmenu({style:'dropdown'}); 
		$('select#sort').selectmenu({style:'dropdown'}); 
		$('select#num').selectmenu({style:'dropdown'}); 
		//tendina selezione per la parte del community plugin
		$('select#add2wl').selectmenu({style:'dropdown'});
		//$('select#[id*="add2wl_"]').selectmenu({style:'dropdown'});
		//
		
		$('select.custom').selectmenu({style:'dropdown'});
		
		$("button, input:submit").button();
	});
*/

})
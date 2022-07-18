<!-- saved from url=(0022)http://internet.e-mail -->
<%@ page contentType="text/css;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<html:xhtml/>

/* here are the styles */

#footer a { 
/*stylesheet doesn't work without this empty first element...*/   
}

#footer a {
	color: #8c8d8d;
	text-decoration: none;
}

#footer {    
    font-size: 10pt;
    text-align: center;
    margin: 5em 10px;
    padding: 1em 0;
    border-top: 1px solid #CCC;
    color: #8c8d8d;
}
body.normal {
    font-size: 75%;
}
body {
    font-family: Verdana, sans-serif;
}

#header {
	background: white url('../../img/cr_background-body.jpg') no-repeat;	
	top: 0;
	left: 0;
	margin: 0;
	padding: 0;
	width: 100%;
	height: 200px;
	min-width: 960px;
	position: fixed;
	background-size: 100% 82%;
}

body {
	margin: 0px;
	font-size: 75%;
}

#topmenu2 {
    top: 60px;
    right: 10px;
    height: 62px;
	min-width: 350px;
	background-color: white;
	box-shadow: -3px 3px 3px #99b2bf;
	border: 1px solid #d6dce0;
	text-align: right;
	position: absolute;
}
#authority-logo {
	position: absolute;
	top: 7px;
	left: 10px;
}

 
#topmenu2 table {
    height: 100%;
    width: 100%;
}
#topmenu2 table tr td {
    width: 50%;
    text-align: center;
    vertical-align: middle;
}
#topmenu2 table tr td a {
    outline: none;
    text-decoration: none;
}
.blueButton {
    background-color: #76b3db;
    background: linear-gradient(to bottom, #4d97c8 0%,#76b3db 100%);
    background: -moz-linear-gradient(top, #4d97c8 0%, #76b3db 100%); /* FF3.6+ */
	background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#4d97c8), color-stop(100%,#76b3db)); /* Chrome,Safari4+ */
	background: -webkit-linear-gradient(top, #4d97c8 0%,#76b3db 100%); /* Chrome10+,Safari5.1+ */
	background: -o-linear-gradient(top, #4d97c8 0%,#76b3db 100%); /* Opera 11.10+ */
	background: -ms-linear-gradient(top, #4d97c8 0%,#76b3db 100%); /* IE10+ */
    color: white;
    padding: 3px;
    margin: 1px 10px;
    box-shadow: -1px 3px 1px #76b3db inset;
}
#topmenu {
    position: absolute;
    right: 120px;
    height: 30px;
}
#topmenu ul, #topmenu2 ul {
    margin: 0;
    padding: 0;
}
#topmenu ul li, #topmenu2 ul li {
    display: inline;
    margin: 0;
    padding: 0 10px 0 5px;
    list-style: none;
    height: 30px;
    line-height: 30px;
    border-right: 1px solid white;
}
#topmenu ul li a, #topmenu2 ul li a, #catalogoRagazzi ul li a {
    color: white;
    text-decoration: none;
}
#topmenu ul li.last, #topmenu2 ul li.last {
    border-right: 0;
}
.banca_dati{
	margin-left: 10px;
	margin-top: 106px;
	display: inline;
}
#content {
	margin-top: 198px;
}
#collection_span {
	color: #00568e;
	font-weight: bold;
	display: inline-block;
	width: 146px;
}
#permanent_select {
    width: 210px;
    max-height: 40px;
    vertical-align: middle;
    height: 23px;
}
#pageSize {
	color: #00568e;
	font-weight: bold;
	display: inline-block;
	display: inline;
}

#form-elements {
	position: fixed;
	z-index: 3;
	top: 100px;
	color: #00568e;
	font-weight: bold;
}

#contentLabel {	
	margin-left: 10px;
}

textarea {
	vertical-align: bottom;	
}

.nextPreviousButton {
    padding: 0;
    border: none;
    background: none;
    background-color: #fff;    
    font-size: 10pt;
    font-weight: 700;
    padding: 7px 10px;    
    max-width: 500px;
    color: #104E8B;
    border-radius: 2px;
    padding-top: 6px;
    padding-bottom: 6px;    
    font-family: Microsoft Sans Serif, Verdana, Geneva, Arial, Helvetica, sans-serif;
    font-weight: bold;
    cursor: pointer;
}

.contentTable {
	padding-left: 10px;
}
.heading {
	width: 80%;
}
.crossReferenceCount, .authorityCount, .documentCount {
	text-align: center;
	width: 5%;
}
 
.firstLine {
	font-size: 12px; 
	font-family: Microsoft San Serif, Arial Unicode MS, sans-serif;
}
 
#xRef-content {
	margin-left: 10px;
}

#xRef-detail {
	text-align: left;
}

#xRef-detail td {
	padding-right: 40px;
}

a {
	text-decoration: none;
	color: #00568E;
}

.nav-button {
	line-height: 1.5em;
	border: 1px solid #d3d3d3;
	background: #e6e6e6 url(images/background-ui-select.png) 50% 50% repeat-x;
	font-weight: normal;
	color: #555555;	
}

.nav-button:hover {
	border: 1px solid #999999;
	background: #dadada url(images/background-ui-select.png) 50% 50% repeat-x;
	font-weight: normal;
	color: #212121;
}

#errors {
	color: red;
}

table{
	margin: 0;
	padding: 0;
	border-collapse: collapse;
	border: none;
}

#content table.contentTable{	
	margin-bottom: 1em;
}

#content table.contentTable tr.contentTableHeading{
	background-color: #bbbbbb;
	text-align: left;
}

#content table.contentTable tr.contentTableHeadingCopies{
	background-color: #FEBD61;
	text-align: left;
}

#content table.contentTable tr.contentTableRow0{
	background-color: #eeeeee;
	vertical-align:top;
}

#content table.contentTable tr.contentTableRow1{
	background-color: #eeeeee;
	vertical-align:top;	
}

#content table.contentTable th{
	border: 1px solid #aaaaaa;
	padding: 0.5ex;
}

#content table.contentTable td{
	border: 1px solid #bbbbbb;
	padding: 0.5ex;
}

/* Here are the styles for maintenance  p

/***********************************/
/* LANGUAGES                       */
/***********************************/
#languages {
	position: absolute;
	top: 8px; right: 80px;
}


#fontsizer {
	position: absolute;
	top: 8px; right: 10px;
}

#topmenu ul li.first {
    padding-left: 27px;
    background: transparent url('../../img/icon-biblioteche.png') no-repeat 0 0px;
}


.headerTable  {
	position: fixed;
}

.crossHeading {
	padding: 10px;
	font-weight: bold;
}
/***********************************/
/* INFODATATABLE                   */
/***********************************/
table.infoDataTable {
	margin: 10px 0;
	width: 100%;
}
table.infoDataTable thead tr th, table.infoDataTable tbody tr td {
	padding: 3px;
	text-align: left;
	line-height: 1.5em; height: 1.5em;
}
table.infoDataTable tbody tr.row0 td {
	padding-top: 1em;
	color: #2B628C;
	border-bottom: 2px solid #CCC;
}
table.infoDataTable tbody tr.row1 td {
	background-color: #f0f0f0;
}
table.infoDataTable tbody tr.row2 td {
	background-color: white;
}

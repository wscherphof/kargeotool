<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<%@page pageEncoding="UTF-8"%>

<tiles:importAttribute/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html:html>
    <head>
        <title><fmt:message key="index.title"/></title>
        <script language="JavaScript" type="text/JavaScript" src="<html:rewrite page="/js/validation.jsp" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/json2.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/js/utils.js" module=""/>"></script>
        <link rel="stylesheet" href="<html:rewrite page="/styles/geo-ov.css" module=""/>" type="text/css" media="screen" />
        <!--[if lte IE 6]>
            <link href="<html:rewrite page="/styles/geo-ov-ie6.css" module=""/>" rel="stylesheet" media="screen" type="text/css" />
            <script type="text/javascript" src="<html:rewrite page="/js/ie6fixes.js" module=""/>"></script>
        <![endif]-->
        <!--[if IE 7]> <link href="<html:rewrite page="/styles/geo-ov-ie7.css" module=""/>" rel="stylesheet" media="screen" type="text/css" /> <![endif]-->
    </head>
    <body>
        <tiles:insert definition="headerlinks"/>
        <div id="contentcontainer">
            <tiles:insert name='content'/>
        </div>
    </body>
</html:html>
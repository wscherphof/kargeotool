<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<tiles:importAttribute/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html:html>
    <head>
        <title>KAR in GIS - edit form frame</title>
        <script language="JavaScript" type="text/JavaScript" src="<html:rewrite page="/js/validation.jsp" module=""/>"></script>
        <link rel="stylesheet" href="<html:rewrite page="/styles/kar-gis.css" module=""/>" type="text/css" media="screen" />
        <script type="text/javascript" src="<html:rewrite page="/js/utils.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/dwr/engine.js" module=""/>"></script>
        <script type="text/javascript" src="<html:rewrite page="/dwr/interface/Editor.js" module=""/>"></script>
    </head>
    <body class="form">
        <tiles:insert name="content"/>
    </body>
</html:html>
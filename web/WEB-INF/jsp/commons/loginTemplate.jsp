<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<%@page pageEncoding="UTF-8"%>

<tiles:importAttribute/>

<html:html>
    <head>
        <title><fmt:message key="index.title"/></title>
        <script language="JavaScript" type="text/JavaScript" src="<html:rewrite page="/js/validation.jsp" module=""/>"></script>
        <link rel="stylesheet" href="<html:rewrite page="/styles/geo-ov.css" module=""/>" type="text/css" media="screen" />
    </head>
    <body>
        <div id="header">
            <div id="headerTitle">Geo OV platform</div>
        </div>
        <div id="contentcontainer">
            <!-- BEGIN content -->
            <tiles:insert name='content'/>
            <!-- EINDE content -->
        </div>
    </body>
</html:html>
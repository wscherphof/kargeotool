<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<tiles:importAttribute/>

<html:html>
    <head>
        <tiles:insert definition="headerinfo"/>
        <script language="JavaScript" type="text/JavaScript" src="<html:rewrite page="/js/validation.jsp" module=""/>"></script>
        <link rel="stylesheet" href="<html:rewrite page="/styles/main.css" module=""/>" type="text/css" media="screen" />
        <link rel="stylesheet" href="<html:rewrite page="/styles/kar-gis.css" module=""/>" type="text/css" media="screen" />
    </head>
    <body>
        <div id="headerbg">
            <div id="headerTitle">Kar in Gis</div>
        </div>
        <div id="contentcontainer">
            <!-- BEGIN content -->
            <tiles:insert name='content'/>
            <!-- EINDE content -->
        </div>
    </body>
</html:html>